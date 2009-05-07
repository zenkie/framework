package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AuditUtils;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;


/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schema.Table;
import nds.security.Directory;
import nds.security.User;

/**
 * Set out state and assignee, or cancel them
 */

public class AuditSetup extends Command {
	/**
	 * @param event contains 
	 *  "auditSetupAction"	- "setout" or "cancelout"
	 *  "assignee"		-  user name that will be assignee
	 *  
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	//logger.debug(event.toDetailString());
  	String action=(String) event.getParameterValue("auditSetupAction",true);
  	boolean setOut=false;
  	int assigneeId=-1; 
  	User usr=helper.getOperator(event);
  	int userId= usr.id.intValue();
    QueryEngine engine = QueryEngine.getInstance() ;
  	
  	if("setout".equals(action)){
  		setOut=true;
  		String assigneeName= (String)event.getParameterValue("assignee",true);
  		assigneeId= Tools.getInt(engine.doQueryOne("select id from users where name="+QueryUtils.TO_STRING(assigneeName)+" and ad_client_id="+ usr.adClientId), -1);
  		if (assigneeId==-1) throw new NDSException("@user-not-found@:"+ assigneeName);
  		if (assigneeId== userId)throw new NDSException("@can-not-assign-to-self@:"+ assigneeName);
  	}else if(!"cancelout".equals(action)){
  		throw new NDSException("Wrong action request");
  	}
  	//String comments= (String) event.getParameterValue("comments");
	ValueHolder holder= new ValueHolder();
	
	try{
		String sql;
		// here we have not checked the dead lock of assignment, that is one assign to another that will assign to the first 
		// one in the end.
		if (setOut)
			sql= "update users set is_out='Y', assignee_id="+ assigneeId+ " where id="+ userId;
		else
			sql="update users set is_out='N', assignee_id =null where id="+ userId;
		
		engine.executeUpdate(sql);
		holder.put("message","@complete@") ;
  	}catch(Exception e){
  		logger.error("Found exception",e);
  		if(e instanceof NDSException) throw (NDSException)e;
  		else throw new NDSException("@exception@:"+ e.getMessage(), e);
  	}
  	return holder;
  }
  
  
  
}