/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import javax.transaction.UserTransaction;
import nds.io.PluginController;
import nds.control.event.NDSEventException;
import nds.control.util.EJBUtils;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.*;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class ProcessUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger(ProcessUtils.class.getName());
	/**
	 * contains timeout in seconds in JobDataMap
	 */
	public static final String TRANSACTION_TIMEOUT="transaction.timeout";
	
	public static int createAdProcessInstance( int processId, String ad_processqueue_name,
			String recordno, User user, List params, 
			Map event, Connection conn) throws Exception{
		return createAdProcessInstance(-1, processId,ad_processqueue_name,recordno,user,params,event,conn);
	}
	/**
	 * Create process instance and return instance id if creates with no error
	 * @param pi_id ad_pinstance.id the id of the created instance
	 * @param pid ad_process.id
	 * @param ad_processqueue_name ad_processqueue.name
	 * @param recordno
	 * @param classOrProc
	 * @param userId
	 * @param params elment is List (elements order:name,valueType,nullable,orderno,value)
	 * @param event contains value for parameters,note event value must has same parameter name for parameter list of process
	 * @return
	 * @throws Exception
	 */
	public static int createAdProcessInstance(int pi_id,int processId, String ad_processqueue_name,
			String recordno, User user, List params, 
			Map event, Connection conn) throws Exception{
	  	PreparedStatement pstmt=null;
	  	
		String pClassName=(String) QueryEngine.getInstance().doQueryOne("select nvl(CLASSNAME,PROCEDURENAME) from ad_process where id="+processId, conn);
	  	
		int userId=user.getId().intValue();

		ArrayList list =new ArrayList();
        list.add(user.getId());
        list.add(ad_processqueue_name);
        //if record no is same, will delete the old one first
        list.add(recordno);
        list.add(pClassName);
        
        list.add(""); // parameters of pinstance
        list.add(new Integer(pi_id)); // ad_pinstance.id
        
        ArrayList res=new ArrayList();
        res.add(Integer.class);
        
        logger.debug("ad_pinstance_create(userId="+ userId+",queue="+ad_processqueue_name+",recordno="+recordno+",clss="+pClassName+",param='', piid="+pi_id+")" );
        Collection result=QueryEngine.getInstance().executeFunction("ad_pinstance_create", list, res, conn );
        
        // sp returned value contains create pinstance id
        int processInstanceId=Tools.getInt(result.toArray()[0],-1);
        if(processInstanceId!=-1){
        	// insert query to para named "query"
        	pstmt= conn.prepareStatement("insert into ad_pinstance_para(id, ad_client_id,ad_org_id,ad_pinstance_id,name,P_STRING,P_NUMBER,P_DATE,info,isactive,orderno) values (get_sequences('ad_pinstance_para'),?,?,?,?,?,?,?,?,'Y',?)");
        	String valueType,name, value;
        	boolean nullable;
        	int orderno;
        	for(int i=0;i< params.size();i++){
	        	name=(String)((List)params.get(i)).get(0);
	        	valueType= (String)((List)params.get(i)).get(1);
	        	nullable = "Y".equals(((List)params.get(i)).get(2));
	        	orderno= Tools.getInt(((List)params.get(i)).get(3),i*10);
	        	value =(String)event.get(name.toUpperCase());
        		if(nullable&&Validator.isNull(null))value=" ";
	        	
	        	if(!nullable && Validator.isNull(value)) throw new NDSException("Parameter "+ name +" not set.");
	        	
	        	//even when value is null, the record should be inserted into db
	        	// nds.cxtab.CxtabReport will do update on ad_pinstance_para, if ad_pinstance_para
	        	// not set, then update will fail. so must insert ad_pinstance_para here
	        	// yfzhu marked following line in 2009-04-18 
	        	
	        	//if(Validator.isNull(value)) continue;
	        	
	        	pstmt.setInt(1, user.adClientId);
	        	pstmt.setInt(2, user.adOrgId);
	        	pstmt.setInt(3, processInstanceId);
	        	pstmt.setString(4, name);
	        	if("S".equals(valueType)){// String
	        		System.out.print("createAdProcessInstance ->"+name);
	        		System.out.print("createAdProcessInstance ->"+name+" value "+value);
	              if (value.getBytes().length > 4000) {
		                logger.warning("param name=" + name + " value too long, cut for ad_pinstance_para:" + value);
		      
		                value = StringUtils.shortenInBytes(value, 2000, "..");
		                }
	        		pstmt.setString(5, value);
	        		pstmt.setNull(6, Types.DOUBLE);
	        		pstmt.setNull(7, Types.NUMERIC); // date is now number(8)
	        		pstmt.setNull(8, Types.VARCHAR);
	        		
	        	}else if("D".equals(valueType)){//Date
	        		pstmt.setNull(5,Types.VARCHAR);
	        		pstmt.setNull(6, Types.DOUBLE);
	        		pstmt.setBigDecimal(7,  QueryUtils.paseInputDateNumber(value,true));
	        		pstmt.setNull(8, Types.VARCHAR);
	        		
	        	}else if("N".equals(valueType)){//Number
	        		pstmt.setNull(5,Types.VARCHAR);
	        		pstmt.setBigDecimal(6,  new BigDecimal(value));
	        		pstmt.setNull(7, Types.NUMERIC); // date is now number(8)
	        		pstmt.setNull(8, Types.VARCHAR);
	        		
	        	}else if("B".equals(valueType)){ //Clob
	        		pstmt.setNull(5,Types.VARCHAR);
	        		pstmt.setNull(6, Types.DOUBLE);
	        		pstmt.setNull(7, Types.NUMERIC); // date is now number(8)
	        		pstmt.setString(8, value);
	        		
	        	}else throw new NDSException("@fail-to-create-task@:unknown value type="+ valueType);
	        	pstmt.setInt(9, orderno);
	        	//pstmt.executeUpdate();
	        	try {
	        		pstmt.executeUpdate();
	        	} catch (SQLException e) {
	        		logger.error("Fail to do insert in ad_pinstance_para: name=" + name + ", valuetype=" + valueType + ", value=" + value, e);
	        		throw e;
	        	}
        	}
        }else{
        	throw new NDSEventException("@fail-to-create-task@:pinstanceid=-1");
        }		
		return processInstanceId;
	}

	/**
     * Execute immediately one ad_pinstance record, no matter its queue status
     * Call 
     * @param ad_pinstance_id ad_pinstance.id
     * @param userId the user that execute this procedure
	 *  @param doTransaction should this execution maintain transaction itself, or just ommit transcation
	 *   control since outside has already done it, or internal one will handle it.
	 * @return false if something error, outside should handle transcation carefully.
     * @throws Exception
     */
    public static ValueHolder executeImmediateADProcessInstance(int ad_pinstance_id, int userId, boolean doTransaction) throws Exception{

    	List al = (List)QueryEngine.getInstance().doQueryList("select p.ID, p.CLASSNAME,p.PROCEDURENAME from ad_pinstance i, ad_process p where i.id="+ad_pinstance_id+" and p.id=i.ad_process_id").get(0);
		int processId=Tools.getInt( al.get(0),-1);
		String clazz= (String) al.get(1);
		String proc= (String) al.get(2);
		ProcessInfo pi= new ProcessInfo("", processId);
		pi.setAD_PInstance_ID(ad_pinstance_id);
		
		ProcessInfoUtil.setParameterFromDB(pi);
		pi.setAD_User_ID( userId); // should be set after setParameterFromDB
		//boolean b=false;
		ValueHolder holder=null;
		long startTime= System.currentTimeMillis();
		if(Validator.isNotNull(clazz)){
			Properties ctx= new Properties();
	        Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
        	int i= Tools.getInt(conf.get("process.transaction.timeout"),-1);
        	int piTransactionTimeout= 60*60*24*10; 
        	if(i>0) piTransactionTimeout= i*60;
			ctx.setProperty(ProcessUtils.TRANSACTION_TIMEOUT, String.valueOf( piTransactionTimeout));
			holder=startClass(ctx, clazz, pi, doTransaction) ;
		}else if(Validator.isNotNull(proc)){
			holder=startProcedure(proc, pi );
		}else{
			throw new NDSException("Found pinstance id="+ ad_pinstance_id + " has neither class nor procedure set.");
		}
		processDone(ad_pinstance_id,(System.currentTimeMillis()- startTime)/1000);
		return holder;
    }	
    /**************************************************************************
	 *  Start Java Class.
	 *      instanciate the class implementing the interface ProcessCall.
	 *  The class can be a Server/Client class (when in Package
	 *  org compiere.process or org.compiere.model) or a client only class
	 *  (e.g. in org.compiere.report)
	 *
	 *  This method is transaction enabled using UserTransction
	 * 
	 *  @param ctx  Context contains param piTransactionTimeout, which is from property "process.transaction.timeout"*60
	 *  @param ClassName    name of the class to call
	 *  @param pi	process info
	 *  @param doTransaction should this execution maintain transaction itself, or just ommit transcation
	 *   control since outside has already done it
	 *  @return     true if success
	 *  @see org.compiere.model.ProcessCall
	 */
    public static ValueHolder startClass (Properties ctx, String ClassName, ProcessInfo pi, boolean doTransaction)
	{
		logger.debug("startClass - " + ClassName+", "+ pi);
		boolean retValue = false;
		ProcessCall myObject = null;
    	UserTransaction ut=null;
    	ValueHolder holder=null;

    	logger.debug("startClass "+ClassName);
    	//try plugin path first, then local one
    	PluginController pc=(PluginController) WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PLUGIN_CONTROLLER);
    	myObject= pc.newPluginProcess(ClassName);
    	try{

    		if(myObject==null){
    			//load in local
    			Class myClass = Class.forName(ClassName);
    			myObject = (ProcessCall)myClass.newInstance();
    		}
    	}catch (Throwable e) {
    		logger.error("Fail to load class " + ClassName, e);
    		pi.setSummary("Error Start Class " + ClassName + ":" + e.getMessage(), true);
    	}

    	if (myObject == null){
    		holder = new ValueHolder();
    		holder.put("code","-1");
    		holder.put("message", ClassName+ " not valid");
    	}else if ((!doTransaction) && (((ProcessCall)myObject).internalTransaction()))
    	{
    		pi.setSummary("Transaction conflict", true);
    		holder = new ValueHolder();
    		holder.put("code", "-1");
    		holder.put("message", "Transaction conflict");
    	}
    	try
    	{
    		if ((doTransaction) && (!((ProcessCall)myObject).internalTransaction())){
    			//how long will it run for most, in minutes, default to 1 day
    			int timeout=Tools.getInt(ctx.get(TRANSACTION_TIMEOUT), 14400);
    			ut= EJBUtils.getUserTransaction();
    			ut.setTransactionTimeout(timeout); // forever, different to handleEvent in ClientController
    			ut.begin();
    			ctx.put("javax.transaction.UserTransaction", ut);
    		}
    		//当任务线程 new 新的 Transaction是 插件设置了 autocomit问题
    		holder = myObject.startProcess(ctx, pi);
    		// commit transaction
    		if(ut!=null)
    			ut.commit();		

    	}catch (Throwable e){
    		try{ if(ut!=null && doTransaction)ut.rollback();}catch(Throwable e2){
    			logger.error("Could not rollback.", e2);
    		}
    		pi.setSummary("Error Start Class " + ClassName, true);
    		logger.error("ProcessCtl.startClass - " + ClassName, e);
    		holder =new  ValueHolder();
    		holder.put("code","-1");
    		holder.put("message", "Error Start Class " + ClassName+":"+e);
    	}
	 /*

    	try
    	{
    		if(doTransaction){
    			//how long will it run for most, in minutes, default to 1 day
    			int timeout=Tools.getInt(ctx.get(TRANSACTION_TIMEOUT), 14400);
    			ut= EJBUtils.getUserTransaction();
    			ut.setTransactionTimeout(timeout); // forever, different to handleEvent in ClientController
    			ut.begin();
    		}
    		//try plugin path first, then local one
    		nds.io.PluginController pc=(nds.io.PluginController) WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PLUGIN_CONTROLLER);
    		myObject= pc.newPluginProcess(ClassName);
    		if(myObject==null){
    			//load in local
    			Class myClass = Class.forName(ClassName);
    			myObject = (ProcessCall)myClass.newInstance();
    		}

    		if (myObject == null){
    			holder = new ValueHolder();
    			holder.put("code","-1");
    			holder.put("message", ClassName+ " not valid");
    		}else{
    			holder = myObject.startProcess(ctx, pi);
    		}
    		// commit transaction
    		if(doTransaction)
    			ut.commit();	
    			
    	}catch (Throwable e){
    		try{ if(ut!=null && doTransaction)ut.rollback();}catch(Throwable e2){
    			logger.error("Could not rollback.", e2);
    		}
    		pi.setSummary("Error Start Class " + ClassName, true);
    		logger.error("ProcessCtl.startClass - " + ClassName, e);
    		holder =new  ValueHolder();
    		holder.put("code","-1");
    		holder.put("message", "Error Start Class " + ClassName+":"+e);
    	}	*/	
		return holder;
	}   //  startClass


	/**************************************************************************
	 *  Start Database Process
	 *  @param ProcedureName PL/SQL procedure name
	 *  @param pi orocess info
	 *  @return true if success
	 */
    public static ValueHolder startProcedure (String ProcedureName, ProcessInfo pi)
	{
		//  execute on this thread/connection
		logger.debug("startProcedure --" +ProcedureName + "(" + pi.getAD_PInstance_ID() + ")");
		ValueHolder holder=null;;
		try
		{
			ArrayList para=new ArrayList();
			para.add(new Integer(pi.getAD_PInstance_ID() ));
			QueryEngine.getInstance().executeStoredProcedure(ProcedureName, para, false);
			holder= getProcedureResult(pi.getAD_PInstance_ID());
		}
		catch (Exception e)
		{
			logger.error("startProcess", e);
			pi.setSummary ( "ProcessRunError" + " " + e.getLocalizedMessage());
			pi.setError (true);
			holder= new ValueHolder();
			holder.put("code","-1");
			holder.put("message", e.getLocalizedMessage() );
		}
		return holder;
	}
    /**
     * 
     * @param pinstanceId id of AD_PInstance
     * @return ValueHoder code from AD_PInstance.result and message from AD_PInstance.ERRORMSG
     * @throws Exception
     */
    private static ValueHolder getProcedureResult(int pinstanceId ) throws Exception{
    	ValueHolder holder= new ValueHolder();
    	List al=(List)QueryEngine.getInstance().doQueryList("select result, errormsg from AD_PInstance where id="+ pinstanceId).get(0);
    	int code=Tools.getInt( al.get(0), 0);
    	String msg=(String) al.get(1);;
    	holder.put("code", String.valueOf(code));
    	if(nds.util.Validator.isNull(msg)){
    		msg= "@complete@";
    	}
    	holder.put("message", msg);
    	return holder;
    }
	/**
	 * Process finished
	 * @param processInstanceId ad_pinstance_id
	 */
    public static void processDone(int processInstanceId, long seconds){
			try{
				QueryEngine.getInstance().executeUpdate(
						"update ad_process set tot_count=nvl(tot_count,0)+1, tot_seconds=nvl(tot_seconds,0)+"+ 
						seconds+ " where id=(select ad_process_id from ad_pinstance where id="+processInstanceId+")");
			}catch(Exception e){
				logger.error("Fail to log the execution time for process "+ processInstanceId, e);
			}
	}
	
	
}
