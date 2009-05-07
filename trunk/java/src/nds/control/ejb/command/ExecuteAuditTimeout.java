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
import nds.control.util.EJBUtils;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;

import com.liferay.util.Encryptor;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schema.Table;
import nds.security.Directory;
import nds.security.User;

/**
 * TimeOut action on phase instance
 */

public class ExecuteAuditTimeout extends Command {
	private final static int ACTION_ACCEPT=1;
	private final static int ACTION_REJECT=2;
	private final static int ACTION_ASSIGN=3;
	private final static int ACTION_CANCEL_ASSIGN=4;
	/**
	 * @param event contains 
	 *  "au_phaseinstance_id"	- au_phaseinstance.id
	 *  "timeout_action"		- time out action to be executed, see au_phase.timeout_action
	 * 							  "A", "R", "P" ("W" is ommited)
	 *  "timeout_program"       - program that when timeout_action='P', see au_phase.timeout_program
	 *  
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	//logger.debug(event.toDetailString());
  	String action=(String) event.getParameterValue("timeout_action");
  	String program = (String) event.getParameterValue("timeout_program");
  	int phaseInstanceId= Tools.getInt(event.getParameterValue("au_phaseinstance_id"),-1);
  	
    QueryEngine engine = QueryEngine.getInstance() ;
  	User usr=helper.getOperator(event);
  	int userId= usr.id.intValue();

  	ValueHolder holder= new ValueHolder();
	String message=null;
    Connection con=null;
	try{
		con= engine.getConnection();
		
		
		if("P".equals(action)){
			action= AuditUtils.executeProgram(program,phaseInstanceId );
		}
		
		String state=action;
		/*if(!("A".equals(state) || "R".equals(state) || "W".equals("state"))) 
			throw new NDSException("Internal Error: unexpected state:"+ state + " of phase instance(id="+ phaseInstanceId+") timeout execution ");
		*/
		con.createStatement().executeUpdate("update au_phaseinstance set state ='"+ state+"', modifieddate=sysdate, modifierid="+userId+", LAST_COMMENTS='Timeout' where id ="+ phaseInstanceId);
   		ValueHolder vh = AuditUtils.executePhaseInstance(phaseInstanceId,
					userId);
		if ("A".equals(vh.get("state"))) {
			// wholely accepted, so do object submit, if needed
			Table table = (Table) vh.get("table");
			int objectid = ((Integer) vh.get("objectid")).intValue();
			logger.debug("Submit " + table.getName() + " no="
					+ vh.get("docno"));

			// 	set status of object to 1 if that column exists
			if (table.getColumn("status") != null)
				con.createStatement().executeUpdate(
						"update " + table.getRealTableName()
								+ " set status=1 where id=" + objectid);
			SPResult result = helper.submitObject(table, objectid, userId,
					event);
			message = ("(" + vh.get("docno") + ")@audit-info@:"
					+ vh.get("message") + ". @submit-info@:"
					+ result.getMessage() + "<br>");
		} else {
			message = ("(" + vh.get("docno") + ")@audit-info@:"
					+ vh.get("message") + "<br>");

		}
   		holder.put("code","0");
        holder.put("message",message.toString()) ;
	    
	    
  	}catch(Exception e){
  		logger.error("Found exception",e);
  		if(e instanceof NDSException) throw (NDSException)e;
  		else throw new NDSException("@exception@:"+ e.getMessage(), e);
  	}finally{
  		try{if(con!=null)con.close();}catch(Throwable t){}
  	}
	return holder;
  }
  
  
  
}