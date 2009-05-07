package nds.control.ejb.command;

import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.sql.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schema.*;
import nds.security.*;

/**
 * Create/Update record in i_doc table, and redirect
 * screen to a download page for nea file
 *
 */

public class LocalProcess_MDISTRIBUTION extends LocalProcess {
	/**
	 	update security filter on ORDER_FILTER and WAREHOUSETO_FILTER
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	ValueHolder holder= super.execute(event);
  	
  	User usr=helper.getOperator(event);
  	TableManager tm= TableManager.getInstance();
  	QueryEngine engine=QueryEngine.getInstance();
  	
  	int columnId =Tools.getInt( event.getParameterValue("columnid"), -1);
  	int objectId= Tools.getInt( event.getParameterValue("objectid"), -1);
  	Table table= tm.getTable("C_V_SPO_ORDER");
		Connection conn =null;
  		PreparedStatement pstmt=null;
  	
  	try{
  		QuerySession qs= event.getQuerySession();
  		Expression sf=SecurityUtils.getSecurityFilter("C_V_SPO_ORDER",Directory.READ, usr.getId().intValue(), qs);
  		QueryRequestImpl query= engine.createRequest(event.getQuerySession());
  		query.setMainTable(table.getId());
  		query.addSelection(table.getPrimaryKey().getId());
  		query.addParam(sf);
  		
  		String orderFilter=" ID IN ("+query.toPKIDSQL(true)+")";

  		table= tm.getTable("M_WAREHOUSE");
  		query= engine.createRequest(event.getQuerySession());
  		query.setMainTable(table.getId());
  		query.addSelection(table.getPrimaryKey().getId());
  		sf=SecurityUtils.getSecurityFilter("M_WAREHOUSE",Directory.READ, usr.getId().intValue(), qs);
  		query.addParam(sf);
  		String whsFilter=" ID IN ("+query.toPKIDSQL(true)+")";
  		
  		conn=engine.getConnection();
  		pstmt=conn.prepareStatement("update m_distribution set order_filter=? , warehouseto_filter=? where id=?");
  		pstmt.setString(1, orderFilter);
  		pstmt.setString(2, whsFilter);
  		pstmt.setInt(3, objectId);
  		pstmt.executeUpdate();
  		
  	}catch(Throwable t){
  		logger.error("Fail", t);
  		throw new NDSEventException(t.getLocalizedMessage(),t);
  	}finally{
  		try{if(pstmt!=null)pstmt.close();}catch(Throwable t){}
  		try{if(conn!=null)conn.close();}catch(Throwable t){}
  	}
	return holder;
  }
}