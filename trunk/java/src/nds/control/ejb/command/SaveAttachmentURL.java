/******************************************************************
*
*$RCSfile: SaveAttachmentURL.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/08/28 00:27:02 $
*
********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.ejb.command.tree.TreeNodeHolder;
import nds.control.ejb.command.tree.TreeNodeManager;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.NavNode;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.*;
import nds.schema.*;

/**
 * Save url info into table records
 */
public class SaveAttachmentURL extends Command {
    /**
     * @param event - should have following parameters inside:
     *    table  - table id (int)
     * 	  column - column id (int)
     *    objectid - object id (int)
     *    fileurl - file link
     *    upload - if "true", create link as /objext/attach.jsp?table=1032&column=10039&objectid=9290
     * 	  if false, use linke specified by "fileurl"
     */
	public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
		helper.checkDirectoryWritePermission(event, helper.getOperator(event));
		ValueHolder v= new ValueHolder();
		
		boolean isUploadFile= Tools.getBoolean(event.getParameterValue("upload"), false);
		String url="";
		if(isUploadFile){ 
			url= "/servlets/binserv/Attach?table="+ event.getParameterValue("table")+
			"&column="+ event.getParameterValue("column")+"&objectid="+ event.getParameterValue("objectid");
			// redirect to reload page
			v.put("next-screen", WebKeys.NDS_URI+"/objext/upload.jsp?table="+ event.getParameterValue("table")+
			"&column="+ event.getParameterValue("column")+"&objectid="+ event.getParameterValue("objectid"));
		}else{
			// default will goto info screen
			url= (String)event.getParameterValue("fileurl");
		}
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable( Tools.getInt(event.getParameterValue("table"), -1));
		Column col= manager.getColumn( Tools.getInt(event.getParameterValue("column"), -1));
		String sql = "update " + table.getRealTableName()+
		" set "+ col.getName()+"='" +StringUtils.escapeForSQL(url)+"'" ;
		if( table.getColumn("modifierid")!=null) sql +=", modifierid=" +helper.getOperator(event).getId().intValue();
		if(table.getColumn("modifieddate")!=null) sql +=", modifieddate=sysdate";
		sql += " where id="+ event.getParameterValue("objectid");
		QueryEngine engine = QueryEngine.getInstance() ;
		Vector sqls= new Vector();
        sqls.addElement(sql);
        try{
            engine.doUpdate(sqls);
        }catch(Exception e){
            throw new NDSEventException(e.getMessage() );
        }
		
		
		v.put("message", "@additional-links-success@");
		return v;
    }
}
