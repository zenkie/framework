package nds.control.ejb.command;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;

 
/**
 * Update c_filter 
 *
 */

public class UpdateFilter extends Command {
	/**
	 * 
	 * @param event params:
	 *	objectid	-	c_filter.id
	 *  filter_expr	-	expression String
	 *	filter_sql	-	sql
	 *	filter		-	filter description  
	 *  table 		- 	table id, the filter will impose on   
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	logger.debug( event.toDetailString());
  	int objectId= Tools.getInt(event.getParameterValue("objectId"), -1);
  	
	User user=helper.getOperator(event);
	int userId=user.getId().intValue();

	String dir= TableManager.getInstance().getTable("C_FILTER").getSecurityDirectory();
	event.setParameter("directory",  dir);	
  	if(!SecurityUtils.hasObjectPermission(userId, user.name, "C_FILTER",objectId, Directory.WRITE, event.getQuerySession()))
  		throw new NDSException("@no-permission@");
	
  	String filter, filterSQL, filterExpr;
  	filter=(String) event.getParameterValue("filter");
  	filterSQL=(String) event.getParameterValue("filter_sql");
  	filterExpr=(String) event.getParameterValue("filter_expr");
  	if(Validator.isNull(filterExpr) || filterExpr.equalsIgnoreCase("undefined")){
  		// it should be obtained from sql 
  	  	int tableId= Tools.getInt(event.getParameterValue("table"), -1);
  	  	Table table= TableManager.getInstance().getTable(tableId);
  		Expression expr=new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}), 
  				filterSQL, filter);
  		filterExpr= expr.toString();
  		logger.debug(filterExpr);
  	}
  	
  	Connection conn=null;
  	PreparedStatement pstmt=null;
  	
  	
    try{
    	conn= QueryEngine.getInstance().getConnection();
    	// check user sms creation permission
    	pstmt= conn.prepareStatement("update c_filter set expression=? , description=? where id=?");
    	pstmt.setString(1, filterExpr);
    	pstmt.setString(2, filter);
    	pstmt.setInt(3, objectId);
    	pstmt.executeUpdate();
        
	    ValueHolder v=new ValueHolder();
	    v.put("message", "@finished@");
	    return v;
	 }catch(Throwable e){
	 	logger.error("", e);
	 	if(!(e instanceof NDSException ))throw new NDSEventException("@exception@", e);
	 	else throw (NDSException)e;
    }finally{
    	if(pstmt!=null){
    		try{pstmt.close();}catch(Throwable t){}
    	}
    	if(conn!=null){
    		try{conn.close();}catch(Throwable t){}
    	}
    }
  }
}