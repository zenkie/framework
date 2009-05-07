package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.process.ProcessUtils;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import nds.ahyy.*;


/**
 * æ∫º€≈≈√˚
 * call C_PROJECT_BIDPRICE(projectno)
 */

public class AHYY_BidPrice extends Command {
	/**
	 * @param event
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	event.setParameter("directory", "C_PROJECT_CTRL_LIST");
  	helper.checkDirectoryWritePermission(event, usr);
  	QueryEngine engine=QueryEngine.getInstance();
  	
	int projectCtrlId=Tools.getInt( event.getParameterValue("objectid",true), -1);
	

	TableManager manager= TableManager.getInstance();
	
	String message="@complete@";	
	Connection conn= engine.getConnection();
	try{
		String projectNo= (String) engine.doQueryOne("select p.no from c_project_ctrl c, c_project p where p.id=c.c_project_id and c.id="+ projectCtrlId, conn);

		int pid=Tools.getInt(engine.doQueryOne("select id from ad_process where PROCEDURENAME='C_PROJECT_BIDPRICE'",conn),-1);
	  	if(pid==-1) 
	  		throw new NDSException("Internal Error: process 'C_PROJECT_BIDPRICE' is not found");
	  	String ad_processqueue_name="DEFAULT";
	  	String recordno="";
		List params= engine.doQueryList("select name, valuetype,nullable,orderno from ad_process_para where ad_process_id="+pid+" order by orderno asc", conn);
		DefaultWebEvent evt=new DefaultWebEvent("params");
		evt.setParameter("c_project", projectNo );
		
	    int piId=ProcessUtils.createAdProcessInstance(pid,ad_processqueue_name, recordno,usr,params,evt.getData(),conn);
    	ValueHolder hd=ProcessUtils.executeImmediateADProcessInstance(piId , usr.id.intValue(), false);
	  	if(!hd.isOK()){
	  		throw new NDSException("@process-instance-failed@:"+ hd.get("message"));
	  	}
	  	message=(String) hd.get("message");

	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
        try{conn.close();}catch(Exception e){}
  	} 
	
	ValueHolder holder= new ValueHolder();
	holder.put("message", message);
	holder.put("code","0");
	return holder;
  }
}