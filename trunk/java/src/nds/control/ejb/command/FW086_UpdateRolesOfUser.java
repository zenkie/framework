package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import org.json.*;


/**
 * 
 *
 */

public class FW086_UpdateRolesOfUser extends Command {
	/**
	 * @param event
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	  int operatorId=Tools.getInt((String)event.getParameterValue("operatorid"), -1);
		if(operatorId!=0) throw new NDSException("无权限");
		JSONObject o= event.getJSONObject();
		
	  	QueryEngine engine=QueryEngine.getInstance();

	  	
		boolean hasError=false;
		PreparedStatement stmt=null;
	    ResultSet rs=null;
		Connection conn= engine.getConnection();
		try{
			String corpId=  o.getString("CorpID");
			String userId= o.getString("UserID");
			
			String roles= o.getString("RoleIDs");
			int clientId= Tools.getInt(engine.doQueryOne("select id from ad_client where domain='"+ corpId+"'",conn),-1);
			if(clientId==-1){
				throw new NDSException("公司未找到:"+ corpId);
			}
			int uid=  Tools.getInt(engine.doQueryOne("select id from users where ad_client_id="+ clientId +" and saasvendor='fw086' and saasuser='"+ userId +"'",conn),-1);
			if(uid==-1){
				throw new nds.security.UserNotFoundException("用户未找到:"+ userId);
			}
			conn.createStatement().executeUpdate("delete from groupuser where userid="+ uid);
			
			
			String sql="insert into groupuser(id,userid, groupid,ad_client_id) values(get_sequences('groupuser'), ?,?,?)";
	        stmt= conn.prepareStatement(sql);
	        int c;
	        StringTokenizer st=new StringTokenizer(roles,",");
	        
	        while(st.hasMoreTokens()){
	        	int rid=Tools.getInt(st.nextToken(), -1);
	        	if(rid!=-1){
	        		stmt.setInt(1, uid);
	        		stmt.setInt(2, rid);
	        		stmt.setInt(3, clientId);
					stmt.executeUpdate();
	        	}
			}
			
		}catch(Throwable t){
	  		if(t instanceof NDSException) throw (NDSException)t;
	  		logger.error("exception",t);
	  		throw new NDSException(t.getMessage(), t);
	  	}finally{
	        try{stmt.close();}catch(Exception ea){}
	        try{conn.close();}catch(Exception e){}
	  	} 
		
		ValueHolder holder= new ValueHolder();
		holder.put("message", "用户角色更新成功");
		holder.put("code","0");
		return holder;
  }
  
}