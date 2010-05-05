/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schedule;

import nds.control.util.EJBUtils;
import nds.control.web.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.process.ProcessInfo;
import nds.process.ProcessInfoUtil;
import nds.process.ProcessUtils;
import nds.query.QueryEngine;
import nds.util.ServletContextActor;
import nds.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import javax.servlet.ServletContext;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
/**
 * 处理Java 内部的任务调度，和quartz 打交道的部分，关于数据库操作，应该在外部完成。
 * @author yfzhu@agilecontrol.com
 */

public class JobManager implements java.io.Serializable,ServletContextActor,DestroyListener {
	private Logger logger= LoggerManager.getInstance().getLogger(JobManager.class.getName());	 
	private StdSchedulerFactory schedulerFactory=null;
	
	public final static String JOB_GROUP_NAME="nds";
	/**
	 * Time out when one process instance executed too long time, and so the process instance will fail
	 * This variable will be set to JobDataMap of JobDetail
	 * @see QueueExecuterJob#startClass(Properties, String, ProcessInfo)
	 */
	private int piTransactionTimeout= 60*60*24*10; // default to 10 days
	
	/**
	 * if not null, only allowed queue will run, so we can specifiy some queue only run on special
	 * host in cluster mode.
	 * 
	 * Store names of the queues
	 */
	private String[] allowedQueues;
	
	public void destroy() {
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.shutdown();
            schedulerFactory=null;
            logger.debug("JobManager destroied.");
        } catch (Exception e) {
        	logger.error("Could not destroy JobManager", e);
        }
		
    }
	public Scheduler getScheduler() throws ObjectNotFoundException{
		try{
			return schedulerFactory.getScheduler();
		}catch(Exception e){
			throw new ObjectNotFoundException(e.getMessage(), e);
		}
	}
	/**
	 * name of the queue that allowed to run in this host. this is specified by 
	 * portal.properties#process.queue.allow=DEFAULT,DOC
	 * If this parameter is not set, then all queues are allowed to run
	 * @return
	 */
	public String[] getAllowedQueues(){
		return allowedQueues;
	}
	/**
	 * Check if specified queue is allowed to run on current host.
	 * @param queueName ignore case 
	 * @return
	 */
	public boolean isQueueAllowed(String queueName){
		// check queue name in allowedQueues list
		boolean isAllowed=false;
		if(allowedQueues!=null){
			for(int k=0;k<allowedQueues.length;k++){
				if(queueName.equalsIgnoreCase(allowedQueues[k])){
					isAllowed=true;
					break;
				}
			}
		}else isAllowed=true;
				
		return isAllowed;
		
	}
	public void init(Director dir) {
    }
	/**
     * We do not put the body of this method in Constructor, because sometimes the
     * ModelManager will be reinitialized, so the controller bean should also be reinstalled.
     *
     */
    public void init(ServletContext context) {
        Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
        nds.util.LicenseManager.validateLicense("Agile ERP","2.0",  EJBUtils.getApplicationConfigurations().getProperty("license","/license.xml") );        
        try{
        	int i= Tools.getInt(conf.get("process.transaction.timeout"),-1);
        	if(i>0) piTransactionTimeout= i*60;
        	
        	String aq=conf.getProperty("process.queue.allow");
        	if(aq!=null)allowedQueues=aq.split(","); 
        	
        	logger.debug("process.queue.allow="+aq);
        	
        	schedulerFactory = new StdSchedulerFactory();
        	schedulerFactory.initialize(conf.getConfigurations("schedule").getProperties());
        	Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            loadQueueJobs();
            logger.debug("JobManager initialized.");
        }catch(Exception e){
        	logger.error("Fail to inialize JobManager", e);
        }
    }
    
    /**
     * Find ad_pinstance according to the ad_process classname or procedure name, and the record_no , owner
     * @param recordNo
     * @param processClassOrProcedure
     * @param userId
     * @return queue name or null if not found
     */
    public String findQueueForProcessInstance(String recordNo, String processClassOrProcedure, int userId ) throws Exception{
    	String sql= "select q.name from ad_pinstance i, ad_process p, ad_processqueue q where i.AD_USER_ID="+
		userId+" and i.record_no='"+ recordNo+"' and i.ad_process_id=p.id and (p.classname='"+
		processClassOrProcedure +"' or p.PROCEDURENAME='"+processClassOrProcedure+"') and q.id=i.ad_processqueue_id";
    	return (String)QueryEngine.getInstance().doQueryOne(sql);
    	
    }
    
    /**
     * Check whether the queue job is running or not
     * @param queueName
     * @return
     */
    public boolean isQueueJobRunning(String queueName)  throws Exception{
    	Scheduler scheduler = schedulerFactory.getScheduler();
    	return scheduler.getTriggerState(queueName,JOB_GROUP_NAME)==Trigger.STATE_NORMAL;
    }
    
    /**
     * If job's running, pause it, else, resume it
     * @param queueName
     * @throws Exception
     */
    public void switchQueueJobState(String queueName) throws Exception{
    	Scheduler scheduler = schedulerFactory.getScheduler();
    	if(scheduler.getTriggerState(queueName,JOB_GROUP_NAME)==Trigger.STATE_NORMAL ){
    		try{
                // tell the scheduler to interrupt our job
    			scheduler.interrupt(queueName, JOB_GROUP_NAME);
    			// try sleep for a while, then delete the job
                Thread.sleep(5000L); 
    		}catch(Exception e){
    			
    		}
    		scheduler.deleteJob(queueName,JOB_GROUP_NAME );
    	}else{
    		loadQueueJob(queueName);
    	}
    }
    /**
     *  
     * @param name
     * @throws Exception
     */
    public void deleteQueueJob(String queueName) throws Exception{
    	Scheduler scheduler = schedulerFactory.getScheduler();
    	scheduler.deleteJob(queueName,JOB_GROUP_NAME );
    }
    public void suspendQueueJob(String queueName)throws Exception{
    	Scheduler scheduler = schedulerFactory.getScheduler();
    	scheduler.pauseJob(queueName,JOB_GROUP_NAME );
    }
    public void resumeQueueJob(String queueName)throws Exception{
    	Scheduler scheduler = schedulerFactory.getScheduler();
    	scheduler.resumeJob(queueName,JOB_GROUP_NAME );
    }
    private final static String GET_QUEUE_INFO="select t.name,t.cron, q.proctype,q.id from ad_processqueue q, ad_trigger t where t.id=q.ad_trigger_id and q.name=? and q.isactive='Y'";
    private final static String GET_QUEUE_INFOS="select t.name,t.cron, q.proctype,q.id,q.name from ad_processqueue q, ad_trigger t where t.id=q.ad_trigger_id and q.isactive='Y'";

    /**
     * Check cron expression valid and find next fire time relate to current time
     * @param cronExpression
     * @return
     * @throws Exception
     */
    public static String getExpressionSummary(String cronExpression) throws Exception{
    	CronTrigger trigger=new CronTrigger();
    	trigger.setCronExpression(cronExpression);
    	trigger.setStartTime(new java.util.Date());
    	trigger.setTimeZone(TimeZone.getDefault());
    	return trigger.getExpressionSummary()+"\n"+ "next-time: "+trigger.computeFirstFireTime(null);
    }
    /**
     * Load queue job and start it
     * @param queueName
     * @throws Exception
     */
    public void loadQueueJob(String queueName) throws Exception{
    	Class jobClass=null;
    	String triggerName=null, cron=null;
    	
		// load processes of the queue
		Connection conn=null;
		PreparedStatement pstmt= null;
		ResultSet rs=null;
		try{
			conn=QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement(GET_QUEUE_INFO);
			pstmt.setString(1,queueName);
			rs= pstmt.executeQuery();
			if(rs.next()){
				triggerName= rs.getString(1);
				cron=rs.getString(2);
				if("O".equals(rs.getString(3))){
					// one time
					jobClass= OneTimeQueueExecuterJob.class;
				}else	jobClass= QueueExecuterJob.class;
				loadQueueJob(triggerName, cron, queueName,jobClass, rs.getInt(4));
			}else{
				throw new ObjectNotFoundException("Process Queue named "+queueName +" not found or not active.");
			}
			
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception e){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
			if(conn!=null) try{conn.close();}catch(Exception e){}
		}    	
    	
    }
    /**
     * Start a job with specified trigger
     * @param triggerName
     * @param cron
     * @param queueName
     * @param clazz
     * @param queueId
     * @throws Exception
     */
    private void loadQueueJob(String triggerName, String cron, String queueName, Class clazz,int queueId ) throws Exception{
    	if(Validator.isNull(triggerName)  ||  Validator.isNull(cron) || queueId==-1 || Validator.isNull(queueName)){
    		logger.error("Could not load queue job "+ queueName+ "(id="+ queueId+") since parameters wrong ");
    		return;
    	}
    	Scheduler scheduler = schedulerFactory.getScheduler();
    	
		JobDetail job = new JobDetail(queueName,JOB_GROUP_NAME, clazz);
		job.getJobDataMap().put(QueueExecuterJob.AD_PROCESSQUEUE_ID, queueId);
		job.getJobDataMap().put(ProcessUtils.TRANSACTION_TIMEOUT, piTransactionTimeout);
		/**
		 * Trigger has same same with job
		 */
		CronTrigger trigger = new CronTrigger(queueName, JOB_GROUP_NAME, queueName,
				JOB_GROUP_NAME, cron);
		scheduler.addJob(job, true);
		Date ft = scheduler.scheduleJob(trigger);
		
		logger.info(job.getFullName() + " has been scheduled to run at: " + ft
				+ " and repeat based on expression: "
				+ trigger.getCronExpression());    	
    	
    }
    /**
     * Create jobs for ad_processqueues and schedule them
     * @throws Exception
     */
    public void loadQueueJobs() throws Exception{
    	Class jobClass=null;
    	String triggerName=null, cron=null;
    	String queueName=null;
    	int queueId;
		// load processes of the queue
		Connection conn=null;
		PreparedStatement pstmt= null;
		ResultSet rs=null;
		//ArrayList failedQueues=new ArrayList(); // string for queue name
		try{
			conn=QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement(GET_QUEUE_INFOS);
			rs= pstmt.executeQuery();
			while(rs.next()){
				triggerName= rs.getString(1);
				cron=rs.getString(2);
				
				if("O".equals(rs.getString(3))){
					// one time
					jobClass= OneTimeQueueExecuterJob.class;
				}else	jobClass= QueueExecuterJob.class;
				
				queueId= rs.getInt(4);
				queueName= rs.getString(5);
				// check queue name in allowedQueues list
				
				if(!isQueueAllowed(queueName)) continue;
				
				try{
					// one failed queue should not affect the whole loading process
					loadQueueJob(triggerName, cron,queueName,jobClass, queueId);
				}catch(Exception e){
					logger.error("Fail to load queue:"+ queueName, e);
					//failedQueues.add("Fail to load queue "+ queueName+":"+e.getLocalizedMessage());
				}
			}
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception e){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
			if(conn!=null) try{conn.close();}catch(Exception e){}
		}    
		
    }
}
