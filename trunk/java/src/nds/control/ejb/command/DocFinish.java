package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.control.web.AttachmentManager;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.Directory;
import nds.security.User;
import nds.util.Attachment;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.WebKeys;
import nds.ws.Doc;

/**
 * WebService method for finish()
 * 
 * Get docno for i_doc table, and locate the real table name and id of that 
 * table record, submit if table allows for that
 * 
 * Will update i_doc records afterwards.
 *  
 * @see nds.ws.DocComponent
 */

public class DocFinish extends Command {
    /**
     * 
     * @param event - should have following parameters inside:
     *    docno  - docno record in i_doc table 
     *    sessionid - current session id of the webservice request
     */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	String docno=(String)event.getParameterValue("docno");
  	List al=QueryEngine.getInstance().doQueryList("select ad_table_id, record_id, state, lastsession,id from i_doc where ad_client_id="+
  			usr.adClientId+ " and docno='"+  docno+"'");

  	Doc doc=null;
  	if(al.size()==0){
  		doc= new Doc(-1,docno,"",Doc.DOCTYPE_RESPONSE, Doc.CODE_DOC_NOT_FOUND,"@invalid-doc-number@");
  	}else{
  		TableManager manager= TableManager.getInstance();
  		Table table= manager.getTable( Tools.getInt( ((List)al.get(0)).get(0), -1));
  		int objectId=Tools.getInt( ((List)al.get(0)).get(1), -1);
  		//state:'C':Created,'I':Processing, 'A':Abort,'F':Finished
  		String state= (String)((List)al.get(0)).get(2);
  		String lastsession=(String)((List)al.get(0)).get(3);
  		int idocId=Tools.getInt( ((List)al.get(0)).get(4), -1);

  		// check permission on destination table, instead of i_doc table
  		String returnMessage=null;
  		
  		boolean hasSubmitObjectPerm= table.isActionEnabled(table.SUBMIT)&&
				SecurityUtils.hasObjectPermission(usr.getId().intValue(), 
						usr.getName(), table.getName(), objectId,Directory.SUBMIT,event.getQuerySession());
  		if(hasSubmitObjectPerm){
  			// submit that table
  			SPResult result=helper.submitObject(table, objectId, usr.getId().intValue(),event);
  			if(result.getCode()!=0){
  				throw new NDSEventException(result.getDebugMessage() );
  			}else 
  				returnMessage= result.getDebugMessage();
  		}else{
  			returnMessage="@doc-state-updated@";
  		}
		doc=new Doc(idocId, docno,"",Doc.DOCTYPE_RESPONSE, Doc.CODE_OK, returnMessage);
		try{
			QueryEngine.getInstance().executeUpdate("update i_doc set modifieddate=sysdate, modifierid="+
				usr.getId()+", state='F', lastsession='"+
			event.getParameterValue("sessionid")+"' where id="+ idocId);
		}catch(Throwable t){
			logger.error("Fail to update i_doc id="+ idocId, t);
			throw new NDSEventException("@update-doc-failed@" );
		}
			
  	}
  	
	ValueHolder v= new ValueHolder();
	v.put("doc", doc);
	v.put("message", "@complete@");
	
	return v;
  }
}