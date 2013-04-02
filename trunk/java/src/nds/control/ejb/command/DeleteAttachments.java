package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.AttachmentManager;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.Attachment;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.WebKeys;

/**
 * Delete attachments of specified record
 */

public class DeleteAttachments extends Command {
    /**
     * @param event - should have following parameters inside:
     *    table  - table id (int)
     * 	  column - column id (int)
     *    objectid - object id (int)
     */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	helper.checkDirectoryWritePermission(event, usr);
	ValueHolder v= new ValueHolder();
	
	TableManager manager= TableManager.getInstance();
	Table table = manager.findTable(event.getParameterValue("table",true));
	Column col= manager.getColumn( Tools.getInt(event.getParameterValue("column",true), -1));
	String sql = "update " + table.getRealTableName()+
	" set "+ col.getName()+"=null" ;
	if( table.getColumn("modifierid")!=null) sql +=", modifierid=" +helper.getOperator(event).getId().intValue();
	if(table.getColumn("modifieddate")!=null) sql +=", modifieddate=sysdate";
	sql += " where id="+ event.getParameterValue("objectid",true);
	QueryEngine engine = QueryEngine.getInstance() ;
	Vector sqls= new Vector();
    sqls.addElement(sql);
    try{
        engine.doUpdate(sqls);
    }catch(Exception e){
       // throw new NDSEventException(e.getMessage() );
    	logger.debug("DeleteAttachments is:"+e.getMessage());
    }
    /**
     * ejb layer should be seperated from web layer
     */
    //FIXME should consider seperation
    AttachmentManager attm=(AttachmentManager)WebUtils.getServletContextManager().getActor(WebKeys.ATTACHMENT_MANAGER);
	Attachment att= attm.getAttachmentInfo(usr.getClientDomain()+"/" + table.getRealTableName()+"/"+col.getName(),  ""+event.getParameterValue("objectid",true), -1);
    if(att!=null)attm.deleteAttachment(att);
    v.put("next-screen", "/html/nds/objext/upload.jsp?table=" + event.getParameterValue("table") + "&column=" + event.getParameterValue("column") + "&objectid=" + event.getParameterValue("objectid") + "&input=" + event.getParameterValue("input"));
	v.put("message", "@clear-additional-links@");
	v.put("url", "");
	return v;
  }
}