/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schedule;

import org.quartz.*;
import nds.util.*;
import nds.control.util.EJBUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;

import java.util.*;
import java.sql.*;

import javax.transaction.UserTransaction;

import nds.process.*;
/**
 * 在参数中获取对应的JobQueue，依序执行队列中的每个任务，创建SvrProcess (根据AD_Process) 和ProcessInfo(根据AD_PInstance 和AD_PInstance_Para)，然后执行SvrProcess
 * @author yfzhu@agilecontrol.com
 */

public class QueueExecuterJob implements InterruptableJob {
	protected Logger logger= LoggerManager.getInstance().getLogger(getClass().getName());
	
	protected boolean interrupted = false;
	
	/**
	 * contains queue id in JobDataMap
	 */
	public static final String AD_PROCESSQUEUE_ID = "ad_processqueue_id";
	
	/**
	 * For cyclic queue, processes will be executed every time;
	 */
	private static final String LOAD_PROCESSES_BY_QUEUE="select i.ID,p.ID, p.CLASSNAME,p.PROCEDURENAME from ad_pinstance i, ad_process p where i.ad_processqueue_id=? and i.isactive='Y' and p.id=i.ad_process_id order by i.orderno asc";
	
	/**
	 * Which process instances should be loaded to execute, default will load all processes marked active
	 * by order of the column "orderno" in ascending order
	 * @return sql for prepared statement, only has one question mark for queue id
	 */
	protected String getLoadJobSQL(){
		return LOAD_PROCESSES_BY_QUEUE;
	}
	/**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a user
     * interrupts the <code>Job</code>.
     * </p>
     * 
     * @return void (nothing) if job interrupt is successful.
     * @throws JobExecutionException
     *           if there is an exception while interrupting the job.
     */
    public void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
    }	
	/**
	 * When should the job stop running
	 * @param queueId
	 * @return
	 */
	private long getEndTime(int queueId, java.util.Date  scheduledDate){
		long endTime;
		long maxDuration=-1;
		Connection conn= null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			conn= QueryEngine.getInstance().getConnection();
			pstmt=conn.prepareStatement("select duration from ad_processqueue where id=?");
			pstmt.setInt(1, queueId);
			rs= pstmt.executeQuery();
			if(rs.next()){
				maxDuration=rs.getInt(1);
			}
		}catch(Exception e){
			logger.error("Fail to get ad_processqueue duration for id="+queueId+":"+ e);
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception e){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
			if(conn!=null) try{conn.close();}catch(Exception e){}
		}
		if(maxDuration <=0){
			endTime= Long.MAX_VALUE;
		}else{
			// maxDuration is in minutes
			endTime= scheduledDate.getTime()+ (maxDuration*(1000*60));
		}	
		return endTime;
	}
	//public void 
	/**
	 * @param context should contains:
	 * 	"ad_processqueue_id" for ad_processqueue_id
	 */
	public void execute(JobExecutionContext context)throws JobExecutionException {
		logger.debug("Begin handling queue "+ context.getJobDetail().getName());
		
		interrupted = false;
		int cnt=0, failed=0;
		long beginTime = System.currentTimeMillis();
		JobDataMap data = context.getJobDetail().getJobDataMap();
		int queueId= data.getInt(AD_PROCESSQUEUE_ID);
		int piTransactionTimeout= data.getInt(ProcessUtils.TRANSACTION_TIMEOUT);
		Properties ctx=new Properties();
		ctx.put(ProcessUtils.TRANSACTION_TIMEOUT, new Integer(piTransactionTimeout));
		long endTime=getEndTime(queueId, context.getScheduledFireTime());
		
		SvrProcess process=null;
		// load processes of the queue
		Connection conn=null;
		PreparedStatement pstmt= null;
		ResultSet rs=null;
		try{
			conn=QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement(getLoadJobSQL());
			pstmt.setInt(1,queueId);
			rs= pstmt.executeQuery();
			while(rs.next() && !interrupted){
				if(endTime<System.currentTimeMillis()){
					//logger.debug("endTime("+ endTime+")<"+System.currentTimeMillis()+", so interrupt." );
					interrupted=true;
					break;
				}
				cnt++;
				// on queue to display
				int instanceId= rs.getInt(1);
				int processId= rs.getInt(2);
				String clazz= rs.getString(3);
				String proc=rs.getString(4);
				ProcessInfo pi= new ProcessInfo("", processId);
				pi.setAD_PInstance_ID(instanceId);
				ProcessInfoUtil.setParameterFromDB(pi);
				long startTime= System.currentTimeMillis();
				nds.control.util.ValueHolder hd=null;
				if(Validator.isNotNull(clazz)){
					hd= ProcessUtils.startClass(ctx, clazz, pi, true );
					
					failed += (hd.isOK()?0:1);
				}else if(Validator.isNotNull(proc)){
					hd=ProcessUtils.startProcedure(proc, pi );
					failed += (hd.isOK()?0:1);
				}else{
					failed ++;
					logger.error("Found pinstance id="+ instanceId + " has neither class nor procedure set.");
				}
				ProcessUtils.processDone(instanceId,(System.currentTimeMillis()- startTime)/1000);
			}
			
		}catch (Exception e){
			logger.error("Could not execute jobs in queue id="+ queueId, e);
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception e){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
			if(conn!=null) try{conn.close();}catch(Exception e){}
		}
		logQueueJobInfo(queueId,context.getJobDetail().getName(), cnt, failed,((System.currentTimeMillis()-beginTime)/1000));
	}	
	/**
	 * Log information about queue execution
	 * @param queueId
	 * @param handled total jobs handled
	 * @param failed failed jobs
	 */
	private void logQueueJobInfo(int queueId, String jobName, int cnt, int failed, long seconds){
//		 is interrupted?
		String msg;
		if(interrupted){
			msg="Queue "+jobName+ " interrupted at "+ ((java.text.SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get()).format(new java.util.Date())+", handled "+ cnt+", failed " + failed +", duration " +seconds+ " seconds"; 
			logger.info(msg);
		}else{
			msg="Finished "+jobName+ " at "+ ((java.text.SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get()).format(new java.util.Date())+", handled "+ cnt+", failed " + failed +", duration " +seconds+ " seconds";
			logger.debug(msg);
		}
		Connection conn= null;
		PreparedStatement pstmt=null;
		try{
			conn= QueryEngine.getInstance().getConnection();
			pstmt=conn.prepareStatement("update ad_processqueue set modifieddate=sysdate, lastmsg=? where id=?");
			pstmt.setString(1,  QueryUtils.TO_STRING(msg,2000));
			pstmt.setInt(2,  queueId);
			pstmt.executeUpdate();
		}catch(Exception e){
			logger.error("Fail to update queue "+ queueId,e);
		}finally{
			if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
			if(conn!=null) try{conn.close();}catch(Exception e){}
		}
		
		
	}
	
	
}
