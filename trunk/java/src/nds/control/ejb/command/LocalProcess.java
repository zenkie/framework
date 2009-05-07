package nds.control.ejb.command;

import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schema.TableManager;
import nds.security.User;

/**
 * Create/Update record in i_doc table, and redirect
 * screen to a download page for nea file
 *
 */

public class LocalProcess extends Command {
	/**

	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	TableManager tm= TableManager.getInstance();
  	QueryEngine engine=QueryEngine.getInstance();
  	
  	int columnId =Tools.getInt( event.getParameterValue("columnid"), -1);
  	int objectId= Tools.getInt( event.getParameterValue("objectid"), -1);
  	int tableId=tm.getColumn(columnId).getTable().getId();
  	try{

	  	// check i_doc table 
		String docno=(String)( engine.doQueryOne(
				"select docno from i_doc where ad_table_id="+ tableId+ " and record_id="+objectId ));
	
		if(docno!=null){
			//already exists
			engine.executeUpdate("update i_doc set modifieddate=sysdate, state='I', modifierid="+
					usr.getId()+" where  ad_table_id="+ tableId+ " and record_id="+objectId );
			
		}else{
			docno=StringUtils.hash( tableId  +"."+ objectId);
			engine.executeUpdate("insert into i_doc(id, ad_client_id, ad_org_id, ownerid,modifierid, "+
					"creationdate,modifieddate,isactive,docno,ad_table_id, record_id,state,lastsession)"+
					"select get_sequences('i_doc'), ad_client_id, ad_org_id, id,id,sysdate,sysdate,'Y', '"+
					docno+"',"+ tableId+","+objectId+",'I',null from users where id="+ usr.getId());
			
		}
		String formRequest="/servlets/binserv/IDoc?docno=" +docno; 
	  	ValueHolder holder= new ValueHolder();
		holder.put("message", "@complete@");
		holder.put("next-screen",formRequest);
		holder.put("docno",docno);
		holder.put("code","0");
		return holder;
  	}catch(Throwable t){
  		logger.error("Fail", t);
  		throw new NDSEventException(t.getLocalizedMessage(),t);
  	}
  }
}