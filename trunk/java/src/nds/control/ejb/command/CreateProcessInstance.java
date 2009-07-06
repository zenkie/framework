package nds.control.ejb.command;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.process.ProcessUtils;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;

 
/**
 * Create proccess instance with parameters set
 *
 */

public class CreateProcessInstance extends Command {
	/**
	 * 
	 * @param event params:
	 *  queue			- queue name of ad_processqueue
	 *	pid				- process id
	 *     
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	int pid= Tools.getInt(event.getParameterValue("pid",true), -1);
  	
	User user=helper.getOperator(event);
	int userId=user.getId().intValue();
	String dir= TableManager.getInstance().getTable("AD_PINSTANCE").getSecurityDirectory();
	event.setParameter("directory",  dir);	
  	helper.checkDirectoryWritePermission(event, user);
  	
	String ad_processqueue_name=(String)event.getParameterValue("queue",true);
	
    //	  create ad_pinstance into specified queue
    String recordno=(String) event.getParameterValue("recordno",true);
    if(Validator.isNull(recordno)) recordno="";

  	
  	Connection conn=null;
  	PreparedStatement pstmt=null;
  	
  	
    try{
	    conn= QueryEngine.getInstance().getConnection();
	    int queueId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from ad_processqueue where ad_client_id="+ 
	    		user.adClientId+" and name="+QueryUtils.TO_STRING(ad_processqueue_name)+" and isactive='Y'",conn),-1);
		// check process queue read permission
		if(!user.getName().equals("root") &&  !SecurityUtils.hasObjectPermission(userId, user.getName(),
				"AD_PROCESSQUEUE",queueId, Directory.READ,event.getQuerySession())) throw new NDSException("@no-permission@");
	    
		List params= QueryEngine.getInstance().doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
        int piId=ProcessUtils.createAdProcessInstance(pid,ad_processqueue_name, recordno,user,params,event.getData(),conn);

        
	    ValueHolder v=new ValueHolder();
	    v.put("message", "@task-generated@");
	    v.put("ad_processinstance_id", new Integer(piId));
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