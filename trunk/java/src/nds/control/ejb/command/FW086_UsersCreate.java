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

public class FW086_UsersCreate extends Command {
	/**
	 * @param event
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	  int operatorId=Tools.getInt((String)event.getParameterValue("operatorid"), -1);
		if(operatorId!=0) throw new NDSException("无权限");
		JSONObject o= event.getJSONObject();
		
	  	QueryEngine engine=QueryEngine.getInstance();

	  	Table userTable= TableManager.getInstance().getTable("users");
		boolean hasError=false;
		PreparedStatement stmt=null;
		PreparedStatement stmtUpdate=null;
	    ResultSet rs=null;
		Connection conn= engine.getConnection();
		try{
			String corpId=  o.getString("CorpID");
		  	JSONArray ja= o.getJSONObject("Staffs").optJSONArray("Staff");
		  	if(ja==null){
		  		JSONObject a= o.getJSONObject("Staffs").optJSONObject("Staff");
		  		if(a!=null){
		  			ja=new JSONArray();
		  			ja.put(a);
		  		}
		  	}
			int clientId= Tools.getInt(engine.doQueryOne("select id from ad_client where domain='"+ corpId+"'",conn),-1);
			if(clientId==-1){
				throw new NDSException("公司未找到:"+ corpId);
			}
			String sqlInsert="insert into users(id,ad_client_id,isactive,creationdate,modifieddate, truename, description,title,name,email,LASTRESULT,comments,birthday,phone,phone2,saasvendor,saasuser) "+
			"values(?, ?, 'Y', sysdate,sysdate, ?, ?,?,?,?,?,?,?,?,?,?,?)";
			logger.debug(sqlInsert);

			String sqlUpdate="update users set truename=?, description=?,title=?,LASTRESULT=?,comments=?,birthday=?,phone=?,phone2=? ,saasuser=? "+
			"where ad_client_id=? and email=?";
			logger.debug(sqlUpdate);
			stmtUpdate= conn.prepareStatement(sqlUpdate);
			stmt= conn.prepareStatement(sqlInsert);
	        int c;
	        if(ja!=null && ja.length()>0)for(int i=0;i<ja.length();i++){
				JSONObject jo= ja.getJSONObject(i);
				c=1;
				// update is for 'root' user , when create, we do not have that user's saasuser account information
				stmtUpdate.setString(c++, jo.optString("StaffName",""));
				stmtUpdate.setString(c++, jo.optString("Alias",""));
				stmtUpdate.setString(c++, parseTitle(jo.optString("Sex")));
				stmtUpdate.setString(c++, jo.optString("Address","")); //LASTRESULT
				stmtUpdate.setString(c++, jo.optString("CardID",""));//comments
				
				int b= parseBirthday(jo.optString("Birthday"));
				if(b==-1)stmtUpdate.setNull(c++,java.sql.Types.NUMERIC);
				else stmtUpdate.setInt(c++, b);
				
				stmtUpdate.setString(c++, jo.optString("OfficePhone",""));
				stmtUpdate.setString(c++, jo.optString("MobilePhone",""));
				stmtUpdate.setString(c++, jo.getString("UserID"));
				stmtUpdate.setInt(c++,clientId);
				stmtUpdate.setString(c++,  jo.getString("Email"));
				
				int cnt= stmtUpdate.executeUpdate();
				
				if(cnt ==0){
					int userId=  engine.getSequence("USERS",conn);
					c=1;
					stmt.setInt(c++, userId);
					stmt.setInt(c++,clientId);
					stmt.setString(c++, jo.optString("StaffName",""));
					stmt.setString(c++, jo.optString("Alias",""));
					stmt.setString(c++, parseTitle(jo.optString("Sex")));
					stmt.setString(c++, jo.optString("UserName",""));
					stmt.setString(c++, jo.getString("Email"));
					stmt.setString(c++, jo.optString("Address",""));
					stmt.setString(c++, jo.optString("CardID",""));
					
					b= parseBirthday(jo.optString("Birthday"));
					if(b==-1)stmt.setNull(c++,java.sql.Types.NUMERIC);
					else stmt.setInt(c++, b);
					
					stmt.setString(c++, jo.optString("OfficePhone",""));
					stmt.setString(c++, jo.optString("MobilePhone",""));
	
					stmt.setString(c++, "fw086");
					stmt.setString(c++, jo.getString("UserID"));
	
					
					stmt.executeUpdate();
					helper.doTrigger("AC", userTable,userId, conn);		
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
		holder.put("message", "用户创建成功");
		holder.put("code","0");
		return holder;
  }
  private String parseTitle(String sex){
	  if(sex==null) return "";
	  if("F".equalsIgnoreCase(sex)) return "女士";
	  if("M".equalsIgnoreCase(sex)) return "先生";
	  return "";
  }
  
  private int parseBirthday(String b){
	  if(b==null || b.length()< 8) return -1;
	  return Tools.getInt( b.substring(0,8),-1);
  }
}