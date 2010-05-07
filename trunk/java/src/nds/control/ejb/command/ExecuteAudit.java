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
 * Accept or reject document
 */

public class ExecuteAudit extends Command {
	private final static int ACTION_ACCEPT=1;
	private final static int ACTION_REJECT=2;
	private final static int ACTION_ASSIGN=3;
	private final static int ACTION_CANCEL_ASSIGN=4;
	/**
	 * @param event contains 
	 *  "auditAction"	- "accept", "reject", "assign", "cancel_assign"
	 *  "itemid"		- only those listed in "selectedItemIdx"
	 *  "assignee"          - when doing assignment, this is to specifie the assignee's user name
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	//logger.debug(event.toDetailString());
  	String a=(String) event.getParameterValue("auditAction");
  	int action;
  	boolean accept=false;
  	String assignee=null;
  	int assigneeId=-1;
    QueryEngine engine = QueryEngine.getInstance() ;
  	User usr=helper.getOperator(event);
  	int userId= usr.id.intValue();
  	String comments = (String) event.getParameterValue("comments");
  	if("accept".equals(a)){
  		action= ACTION_ACCEPT;
  		accept = true;
  	}else if("reject".equals(a)){
  		action= ACTION_REJECT;
  	}else if("assign".equals(a)){
  		action = ACTION_ASSIGN;
  		assignee= (String) event.getParameterValue("assignee");
  		if ( Validator.isNull(assignee)) throw new NDSException("@please-set-assignee@");
  		assigneeId= Tools.getInt(engine.doQueryOne("select id from users where ad_client_id="+ usr.adClientId +" and name="+ QueryUtils.TO_STRING(assignee)), -1);
  		if(assigneeId==-1)throw new NDSException("@please-set-assignee@");
  		if (assigneeId== userId)throw new NDSException("@can-not-assign-to-self@:"+ assignee);
  	}else if("cancel_assign".equals(a)){
  		action= ACTION_CANCEL_ASSIGN;
  	}else
		throw new NDSException("Wrong action request");
  	
	ValueHolder holder= new ValueHolder();
	
    Connection con=null;
	try{
		String[] objectStr = event.getParameterValues("itemid");
	       
	    if(objectStr==null){
	        throw new NDSEventException("@choose-menu@");
	    }
	    
	    String res = "", s; int errCount=0;
	    int[] ids= new int[objectStr.length];
	    StringBuffer message =new StringBuffer();
	    if( action == ACTION_ACCEPT || action == ACTION_REJECT){
	    	// accept or reject process
	    	con= engine.getConnection();
		    for(int i=0;i<ids.length;i++){
	    		ValueHolder vh=AuditUtils.doAudit((Integer.parseInt(objectStr[i])),userId,accept,comments);
	    		if( "A".equals(vh.get("state"))){
					// wholely accepted, so do object submit, if needed
					Table table=(Table) vh.get("table");
					int objectid=( (Integer) vh.get("objectid")).intValue();
					logger.debug("Submit " + table.getName()+ " no="+ vh.get("docno"));
		    		
	        		// 	set status of object to 1 if that column exists
	        		if(table.getColumn("status")!=null)
	        			con.createStatement().executeUpdate("update "+ table.getRealTableName()+" set status=1 where id="+ objectid);
					SPResult result =helper.submitObject(table, objectid, userId, event);
					if(result.getCode()!=0){
						//2010-05-05 add to last comments
						con.createStatement().executeUpdate("update au_phaseinstance set LAST_COMMENTS=LAST_COMMENTS ||"+ 
								QueryUtils.TO_STRING(result.getMessage())+" where id=(select au_pi_id from "+ table.getRealTableName()+" where id="+ objectid+")");
					}
		        	message.append("("+ vh.get("docno")+")@audit-info@:"+ vh.get("message")+". @submit-info@:"+ result.getMessage()+"<br>");
				}else{
					message.append("("+ vh.get("docno")+")@audit-info@:"+ vh.get("message")+"<br>");
				}
		    }
	    }else if (action== ACTION_ASSIGN ){
	    	for(int i=0;i< ids.length;i++){
	    		ValueHolder vh=AuditUtils.doAssign((Integer.parseInt(objectStr[i])),userId, assigneeId);
	    	}
	    	message.append("@complete@");
	    }else if(action == ACTION_CANCEL_ASSIGN){
	    	for(int i=0;i< ids.length;i++){
	    		ValueHolder vh=AuditUtils.doCancelAssign((Integer.parseInt(objectStr[i])),userId);
	    	}
	    	message.append("@complete@");
	    }
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