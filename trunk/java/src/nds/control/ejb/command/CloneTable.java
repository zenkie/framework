/******************************************************************
*
*$RCSfile: CloneTable.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/06/24 00:32:31 $
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
 * Clone table
 */
public class CloneTable extends Command {
	
    /**
     * User should have write permission on that table
     * @param event - can has following parameters:
     *  srctable  - src table name
     *  destable - dest table name
     *  destdesc - dest table description  
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
      	User usr=helper.getOperator(event);
      	
      	TableManager manager= TableManager.getInstance();

      	Table table= manager.getTable("AD_Table");
    	// following check directory permission needs this parameter
    	event.setParameter("directory", table.getSecurityDirectory());
    	helper.checkDirectoryWritePermission(event, helper.getOperator(event));
    	ArrayList params=new ArrayList();
    	params.add(event.getParameterValue("srctable",true));
    	params.add(event.getParameterValue("destable",true));
    	params.add(event.getParameterValue("destdesc",true));
    	helper.executeStoredProcedure("ad_table_clone", params, false);
    	ValueHolder vh=new ValueHolder();
    	vh.put("message","OK");
    	return vh;
        
    }
      
}
