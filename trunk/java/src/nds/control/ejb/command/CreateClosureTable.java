/******************************************************************
*
*$RCSfile: CreateClosureTable.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/03/13 01:16:45 $
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
 * Create closure table and stored procedure for later population.
 * The input table must be tree typel
 */
public class CreateClosureTable extends Command {
	
    /**
     * User should have write permission on that table
     * @param event - can has following parameters:
     *  table  -  table id
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
      	User usr=helper.getOperator(event);
      	
      	int columnId =Tools.getInt( event.getParameterValue("columnid",true), -1);
      	int tableId= Tools.getInt( event.getParameterValue("objectid",true), -1);
      	TableManager manager= TableManager.getInstance();

      	Table table= manager.getTable(tableId);
    	if(!table.isTree())throw new NDSException("@not-tree-table@");
    	// following check directory permission needs this parameter
    	event.setParameter("directory", table.getSecurityDirectory());
    	helper.checkDirectoryWritePermission(event, helper.getOperator(event));
    	
    	ClosureTable ct=new ClosureTable(table);
    	try{
    		ct.createClosureTable(true, true);
    	}catch(Throwable t){
    		logger.error("Fail to create closure table for "+ table, t);
    		throw new NDSException("Fail to create closure table for "+ table, t);
    	}
    	ValueHolder vh=new ValueHolder();
    	vh.put("message","OK");
    	return vh;
        
    }
    /**
     * For tree table to reconstruct closure table
     * If table named "xxx", the closure table should be named to "xxx_cl"
     * and procedure "xxx_close" will be the reconstruct process
     * 
     * @author yfzhu@agilecontrol.com
     */

    private class ClosureTable {
    	private Table table; 
    	public ClosureTable(Table tb){
    		if(!tb.isTree()) throw new NDSRuntimeException(tb+ " is not tree table");
    		table=tb;
    	}
    	public String getClosureTableName(){
    		return table.getName()+"_cl";
    	}
    	
    	/**
    	 * Create table ended with "_cl", which has tree columns:
    	 *   parent_id, id, distance
    	 * @param checkTableExists if true, will first check table existance
    	 * @param dropIfExists if true, will drop the table if exists, when checkTableExists is set to false,
    	 *  this param will not be used
    	 * @throws Exception
    	 */
    	public void createClosureTable(boolean checkTableExists, boolean dropIfExists)throws Exception{
    		boolean tableExists=false;
    		if(checkTableExists){
    			try{
    				QueryEngine.getInstance().doQueryOne("select id,parent_id, distance from "+ getClosureTableName());
    				tableExists=true;
    			}catch(Throwable t){
    				
    			}
    		}
    		if(tableExists){
    			if(!dropIfExists) return;
    			else{
    				try{
    					QueryEngine.getInstance().executeUpdate(getTableDropDDL());
    				}catch(Throwable t){
    					
    				}
    			}
    		}
    		
    		ArrayList al= new ArrayList();
    		al.add(getTableCreationDDL());
    		al.add(getIndexCreationDDL());
    		al.add(getProcedureCreationDDL());
    		QueryEngine.getInstance().doUpdate(al);
    	}
    	public String getTableDropDDL(){
    		return "drop table "+ getClosureTableName();
    	}
    	public String getTableCreationDDL(){
    		return "create table "+getClosureTableName()+ " ( parent_id number(10), id number(10) not null, distance number(10) not null, primary key(parent_id, id))";
    	}
    	public String getIndexCreationDDL(){
    		return "create index idx_"+ getClosureTableName()+ " on "+getClosureTableName()+"(id)" ;
    	}
    	public String getPopulateDML(){
    		return "exec "+ table.getName()+"_close;";
    	}
    	/**
    	 * Create procedure for closure
    	 * @return
    	 */
    	public String getProcedureCreationDDL(){
    		/**
    		 * May not write line seperator in the creation block.
    		 */
    		StringBuffer sb=new StringBuffer();
    		StringBufferWriter w= new StringBufferWriter(sb);
    		String tableName=table.getName();
    		String closureTableName=getClosureTableName();
    		w.print("create or replace procedure "+ tableName+ "_close is ");
    		w.print("v_distance number(10);");
    		w.print("begin ");
    		w.print("EXECUTE IMMEDIATE 'TRUNCATE TABLE "+closureTableName+"';");
    		w.print("v_distance := 0;");
    		w.print("INSERT INTO "+closureTableName+" (parent_id, id, distance)SELECT id, id, v_distance FROM "+tableName+";");
    		w.print("loop ");
    		w.print("v_distance := v_distance + 1;");
    		w.print("INSERT INTO "+closureTableName+"(parent_id, id, distance) ");
    		w.print("SELECT "+closureTableName+".parent_id, "+tableName+".id, v_distance ");
    		w.print("FROM "+closureTableName+", "+tableName+" ");
    		w.print("WHERE "+closureTableName+".id = "+tableName+"."+ table.getParentNodeColumn().getName()+" AND "+closureTableName+".distance = v_distance - 1;");
    		w.print("exit when(SQL%ROWCOUNT =0);");
    		w.print("END LOOP;");
    		w.print("end;");
    		
    		return sb.toString();
    	}

    }   
}
