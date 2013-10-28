package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AuditUtils;
import nds.control.util.EJBUtils;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.monitor.MonitorManager;
import nds.monitor.ObjectActionEvent;
import nds.monitor.ObjectActionEvent.ActionType;
import nds.query.*;
import nds.util.*;

import com.liferay.util.Encryptor;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schema.Table;
import nds.schema.TableManager;
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
	
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}
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
    Connection localcon=null;
    
    boolean bool2 = false;
    
		try {
			con = engine.getConnection();
			String[] objectStr;
			int[] ids;
			int i2;
			if ((objectStr = event.getParameterValues("itemid")) != null) {
				ids = new int[objectStr.length];
				for (int i1 = 0; i1 < ids.length; i1++) {
					ids[i1] = Integer.parseInt(objectStr[i1]);

					if ((i2 = Tools
							.getInt(engine
									.doQueryOne(
											"select pi.id from au_phaseinstance pi, au_pi_user u where u.au_pi_id=pi.id and pi.id=? and pi.state='W' " +
											"and ((u.ad_user_id=? and u.assignee_id is not null ) or (u.ad_user_id=? and u.assignee_id is null ) or (u.assignee_id=?)) and u.state='W' order by id desc",
											new Object[] {
													Integer.valueOf(ids[i1]),
													Integer.valueOf(userId),
													Integer.valueOf(userId),
													Integer.valueOf(userId) }), -1)) != ids[i1]){
						throw new NDSException("@audit-record-changed@:("+ ids[i1] + ")");
					}
				}
			} else {
				Table localTable;
				if ((localTable = TableManager.getInstance().findTable(
						event.getParameterValue("table"))) == null) {
					throw new NDSEventException("@choose-menu@");
				}

				if ((i2 = event.getObjectId(localTable, usr.adClientId,
						con, false)) < 0) {
					throw new NDSEventException("@choose-menu@");
				}
				int k;
				if ((k = Tools
						.getInt(engine
								.doQueryOne("select pi.id from au_phaseinstance pi, au_pi_user u where u.au_pi_id=pi.id and pi.ad_table_id="
										+ localTable.getId()
										+ " and pi.record_id="
										+ i2
										+ " and pi.state='W' and ((u.ad_user_id="
										+ userId
										+ " and u.assignee_id is null ) or (u.assignee_id="
										+ userId
										+ ")) and u.state='W' order by id desc"),
								-1)) < 1) {
					throw new NDSException("@no-permission@");
				}
				ids = new int[] { k };
				bool2 = true;
			}
    
    /*
	try{
		String[] objectStr = event.getParameterValues("itemid");
	       
	    if(objectStr==null){
	        throw new NDSEventException("@choose-menu@");
	    }
	   */ 
			boolean bool3 = false;
			con.setAutoCommit(false);
		/*	
		con= engine.getConnection();
	    String res = "", s; int errCount=0;
	    int[] ids= new int[objectStr.length];
	    */
	    
	    
	    StringBuffer message =new StringBuffer();
	    if( action == ACTION_ACCEPT || action == ACTION_REJECT){
	    	// accept or reject process
		    for(int i=0;i<ids.length;i++){
		    	Savepoint localSavepoint = con.setSavepoint();
		    	try{
			    	Locale local = event.getLocale(); 
			        localcon = engine.getConnection(); 
			    	assigneeId = ids[i];
					List au_phaselist = QueryEngine.getInstance().doQueryList(
					"select pi.ad_table_id, pi.record_id,pi.AU_PROCESS_ID,p.orderno,pi.RECORD_DOCNO from au_phaseinstance pi, au_phase p where p.id=pi.au_phase_id and pi.id=?",
					new Object[] { Integer.valueOf(assigneeId) },localcon);
					int ptableid = Tools.getInt(((List) au_phaselist.get(0)).get(0), -1);
					int pcordid = Tools.getInt(((List) au_phaselist.get(0)).get(1), -1);
					Tools.getInt(((List) au_phaselist.get(0)).get(2),-1);
					Tools.getInt(((List) au_phaselist.get(0)).get(3),-1);
					String recodeno = (String) ((List) au_phaselist.get(0)).get(4);
					Table ptable = TableManager.getInstance().getTable(ptableid);
					int n=Tools.getInt(QueryEngine.getInstance().doQueryOne("select status from "+ ptable.getRealTableName()
							+ " where id=?",new Object[] { Integer.valueOf(pcordid) },(Connection) localcon), -1);
					
					if (n != ACTION_ASSIGN){
						throw new NDSException(ptable.getDescription(local)
										+ " "
										+ recodeno
										+ " @not-in-audit-status@");
					}

	    		ValueHolder vh=AuditUtils.doAudit(ids[i],userId,accept,comments,con);
	    		logger.debug("test ~~~~~~~~~");
	    		if( "A".equals(vh.get("state"))){
					// wholely accepted, so do object submit, if needed
					Table table=(Table) vh.get("table");
					int objectid=( (Integer) vh.get("objectid")).intValue();
					logger.debug("Submit " + table.getName()+ " no="+ vh.get("docno"));
		    		
					//QueryUtils.lockRecord(table, objectid, con);
		    		
	        		// 	set status of object to 1 if that column exists
	        		if(table.getColumn("status")!=null)
	        			con.createStatement().executeUpdate("update "+ table.getRealTableName()+" set status=1 where id="+ objectid);
					SPResult result =helper.submitObject(table, objectid, userId, event,con);
					if(result.getCode()!=0){
						//2010-05-05 add to last comments
						con.createStatement().executeUpdate("update au_phaseinstance set LAST_COMMENTS=LAST_COMMENTS ||"+ 
								QueryUtils.TO_STRING(result.getMessage())+" where id=(select au_pi_id from "+ table.getRealTableName()+" where id="+ objectid+")");
					}
					JSONObject cxt=new JSONObject();
					cxt.put("source", this);
					cxt.put("connection", con);
					cxt.put("statemachine", this.helper.getStateMachine());
					cxt.put("javax.servlet.http.HttpServletRequest", event.getParameterValue("javax.servlet.http.HttpServletRequest", true));
					ObjectActionEvent oae=new ObjectActionEvent(table.getId(),objectid, usr.adClientId,ObjectActionEvent.ActionType.SUBMIT, usr, cxt);
					MonitorManager.getInstance().dispatchEvent(oae);
					
		        	message.append("("+ vh.get("docno")+")@audit-info@:"+ vh.get("message")+". @submit-info@:"+ result.getMessage()+(bool2 ? "" : "<br>"));
				}else{
					message.append("("+ vh.get("docno")+")@audit-info@:"+ vh.get("message")+(bool2 ? "" : "<br>"));
				}
	    		bool3 = bool2;
		    	}catch (Throwable e) {
		    		logger.error("fail to audit " + ids[i], e);
		    		con.rollback(localSavepoint);
		    		message.append((bool2 ? "" : new StringBuilder().append("(Line ").append(i + 1).append(")").toString()) + "@exception@:" + e.getMessage() + (bool2 ? "" : "<br>"));
		    		}
		     }
	    }else if (action== ACTION_ASSIGN ){
	    	for(int i=0;i< ids.length;i++){
	    		ValueHolder vh=AuditUtils.doAssign(ids[i],userId, assigneeId,con);
	    	}
	    	message.append("@complete@");
	    	bool3 = bool2;
	    }else if(action == ACTION_CANCEL_ASSIGN){ 
	    	for(int i=0;i< ids.length;i++){
	    		ValueHolder vh=AuditUtils.doCancelAssign(ids[i],userId,con);
	    	}
	    	message.append("@complete@");
	    	bool3 = bool2;
	    }
        holder.put("message",message.toString()) ;
        if (bool2){
	       JSONObject res= new JSONObject();
	       res.put("success", bool3);
	       holder.put("restResult", res);
       }
        con.commit();
	    
	    
  	}catch(Exception e){
  		logger.error("Found exception",e);
  		if(e instanceof NDSException) throw (NDSException)e;
  		else throw new NDSException("@exception@:"+ e.getMessage(), e);
  	}finally{
  		try{if(con!=null)con.close();}catch(Throwable t){}
  		try{if(localcon!=null)localcon.close();}catch(Throwable t){}
  		
  	}
	return holder;
  }
  
  
  
}