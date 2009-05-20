package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AuditUtils;
import nds.control.util.EJBUtils;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;
import nds.schema.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schema.Table;
import nds.security.Directory;
import nds.security.User;

/**
 * Remove cxtab report temporary data in fact table, this is used 
 * for cxtab with pre_procedure defined, the handling process is
 * asynchronous with CxtabRunner
 * 
 * @see nds.process.CxtabRunner
 * @author yfzhu
 */
public class RemoveCxtabTmpData extends Command {
	/**
	 * @param event contains 
		    	event.setParameter("operatorid", String.valueOf(this.getAD_User_ID()));
		    	event.setParameter("command", "RemoveCxtabTmpData");
		    	event.setParameter("cxtab", cxtabName);
		    	event.setParameter("ad_pi_id", String.valueOf(this.getAD_PInstance_ID()));
	 * 
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	//logger.debug(event.toDetailString());
	
	  
    QueryEngine engine = QueryEngine.getInstance() ;
  	User usr=helper.getOperator(event);
  	int userId= usr.id.intValue();

  	
	ValueHolder holder= new ValueHolder();
	
    Connection conn=null;
	try{
		conn= engine.getConnection();
		String cxtabName=(String)event.getParameterValue("cxtab");
		int adPiId= Tools.getInt(event.getParameterValue("ad_pi_id"),-1);
		logger.debug("cxtab="+ cxtabName+", ad_pi_id="+adPiId+", usr="+ userId);
		
		List al=engine.doQueryList(
				"select ad_table_id, AD_PI_COLUMN_ID from ad_cxtab c where c.ad_client_id="+ 
				usr.adClientId +" and c.name="+ QueryUtils.TO_STRING(cxtabName),conn);
		int tableId= Tools.getInt( ((List)al.get(0)).get(0),-1);
		int piColumnId= Tools.getInt( ((List)al.get(0)).get(1),-1);
		Table tb= nds.schema.TableManager.getInstance().getTable(tableId);
		Column col= nds.schema.TableManager.getInstance().getColumn(piColumnId);
		if(tb!=null && col!=null){

			// check user write permission on this table
			event.put("directory", tb.getSecurityDirectory());
			helper.checkDirectoryWritePermission(event, usr);

			String sql="DELETE FROM "+ tb.getRealTableName()+ " WHERE "+ col.getName()+
				"="+  adPiId+ (tb.isAdClientIsolated()?" AND AD_CLIENT_ID="+usr.adClientId: "");
			
			int cnt=engine.executeUpdate(sql);
			logger.debug("("+cnt+" deleted) "+sql);
		}
		
        holder.put("message","@complete@") ;
	    
	    
  	}catch(Exception e){
  		logger.error("Found exception",e);
  		if(e instanceof NDSException) throw (NDSException)e;
  		else throw new NDSException("@exception@:"+ e.getMessage(), e);
  	}finally{
  		try{if(conn!=null)conn.close();}catch(Throwable t){}
  	}
	return holder;
  }
  
  
  
}