/******************************************************************
*
*$RCSfile: CloseTreeTable.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/03/13 01:16:45 $
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
 * This action will recreate data structure of the table for later online analysis processing
 */
public class CloseTreeTable extends Command {
	
    /**
     * User should have write permission on that table
     * @param event - can has following parameters:
     *  table  -  table id
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
    	TableManager manager= TableManager.getInstance();
    	int tableId=Tools.getInt( event.getParameterValue("table",true),-1);
    	Table table= manager.getTable(tableId);
    	if(!table.isTree())throw new NDSException("@not-tree-table@");
    	// following check directory permission needs this parameter
    	event.setParameter("directory", table.getSecurityDirectory());
    	helper.checkDirectoryWritePermission(event, helper.getOperator(event));
    	
    	helper.executeStoredProcedure(table.getRealTableName()+"_close", new ArrayList(),false);
    	
    	ValueHolder vh=new ValueHolder();
    	vh.put("message","OK");
    	return vh;
        
    }
   
}
