package nds.control.ejb.command;

import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.process.ProcessUtils;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import java.util.*;

import nds.security.Directory;
import nds.security.User;

/**
 * Create crosstab runner process instance, including following parameters:
 * 	select filter (where clause)
 *  ad_cxtab template
 *  pre-process params for ad_cxtab
 *  schedule information
 * 
 * @author yfzhu@agilecontrol.com 
 */
public class CreateCxtabRunnerProcessInstance extends Command {
	 /** 
	  * @param event contains 
	 * 		"filter" - description of the current filter
	 * 		"filter_expr" - nds.query.Expression 
	 * 		"filter_sql"  - string like 'in (13,33)'
	 * 		"cxtab*"		- name of ad_cxtab (AK)
	 * 		"chk_run_now*"	- run immediate or by schedule
	 *		
	 *		--- some parameters prefixed with "preps_" will set to pre-process of ad_cxtab
	 *
	 * 		"queue"  - process queue
	 * 		"recordno"		- process instance record no
	 * 		"filename"		- file name to save the report (xls)	
	 * 		"filetype"		- type of file, "xls"|"html"|"tab"
	 * @return  
	 * 		Create instance of nds.process.CxtabRunner first, then
	 * 		if by immediately, execute that pi and create that report as html and save to user report folder
	 *      if by schedule, return infor.jsp
	 */
	
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	
  	
	User user=helper.getOperator(event);
	int userId=user.getId().intValue();
	String dir= TableManager.getInstance().getTable("AD_PINSTANCE").getSecurityDirectory();
	event.setParameter("directory",  dir);	
  	//helper.checkDirectoryWritePermission(event, user); do not check 2007-9-17
  	
	String ad_processqueue_name=(String)event.getParameterValue("queue",true);
	
    //	  create ad_pinstance into specified queue
    String recordno=(String) event.getParameterValue("recordno",true);
    if(Validator.isNull(recordno)) recordno="";

  	String cxtabName= (String)event.getParameterValue("cxtab",true);
  	
    // execute immediate?
    String chk_run_now=(String)event.getParameterValue("chk_run_now",true);
    if("Y".equals(chk_run_now)){
    	event.setParameter("filetype","htm"); // this will be used by nds.cxtab.CxtabReport
    }else
    	event.setParameter("filetype","xls");
  	Connection conn=null;
  	PreparedStatement pstmt=null;
  	
  	QueryEngine engine= QueryEngine.getInstance();
    try{
	    conn= engine.getConnection();
	    // process id, that is nds.process.CxtabRunner
	    
	  	int pid=Tools.getInt(engine.doQueryOne("select id from ad_process where classname='nds.process.CxtabRunner' and isactive='Y'",conn),-1);
	  	if(pid==-1) 
	  		throw new NDSException("Internal Error: process 'nds.process.CxtabRunner' is not found");
	  	
		// check process read permission
		if(!SecurityUtils.hasObjectPermission(conn,userId, user.getName(),
				"AD_PROCESS",pid, Directory.READ,event.getQuerySession())) 
			throw new NDSException("@no-permission@(AD_PROCESS)");
	  	
	    int queueId=Tools.getInt(engine.doQueryOne("select id from ad_processqueue where ad_client_id="+ 
	    		user.adClientId+" and name="+QueryUtils.TO_STRING(ad_processqueue_name)+" and isactive='Y'",conn),-1);
		// check process queue read permission
		if(!SecurityUtils.hasObjectPermission(conn,userId, user.getName(),
				"AD_PROCESSQUEUE",queueId, Directory.READ,event.getQuerySession())) 
			throw new NDSException("@no-permission@(AD_PROCESSQUEUE)");
	    
		
		List params= engine.doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
		
		// get pre-process params
		int preprocessId=Tools.getInt(engine.doQueryOne("select AD_PROCESS_ID from ad_cxtab where ad_client_id="+ 
	    		user.adClientId+" and name="+QueryUtils.TO_STRING(cxtabName)+" and isactive='Y'",conn),-1);
		if(preprocessId!=-1){

			List preprocessParams= engine.doQueryList("select 'preps_'|| name, valuetype,nullable,orderno+1000 from ad_process_para where ad_process_id="+preprocessId+" order by orderno asc", conn);
			if(preprocessParams!=null && preprocessParams.size()>0){
				params.addAll(preprocessParams);
			}
		}
		
        int piId=ProcessUtils.createAdProcessInstance(pid,ad_processqueue_name, recordno,user,params,event.getData(),conn);
        	
	    ValueHolder v=new ValueHolder();
        if("Y".equals(chk_run_now)){
        	// run immdiate
      		// the transaction will be maintained by out side ClientControllerBean
        	ValueHolder hd=ProcessUtils.executeImmediateADProcessInstance(piId , userId, false,conn);
    	  	if(!hd.isOK()){
    	  		throw new NDSException("@process-instance-failed@:"+ hd.get("message"));
    	  	}
        	String filename= (String)event.getParameterValue("filename",true);
        	v.put("next-screen", "/servlets/binserv/GetFile?filename="+ URLEncoder.encode(filename+".htm","UTF-8"));
        }else{
        
    	    v.put("message", "@task-generated@");
    	    v.put("next-screen", "/html/nds/info.jsp");
    	    v.put("ad_processinstance_id", new Integer(piId));
        }
	    return v;
	 }catch(Throwable e){
	 	logger.error("", e);
	 	if(!(e instanceof NDSException ))throw new NDSEventException("“Ï≥£", e);
	 	else throw (NDSException)e;
    }finally{
    	if(conn!=null){
    		try{conn.close();}catch(Throwable t){}
    	}
    }
  }
}