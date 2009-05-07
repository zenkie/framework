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

public class FW086_ListRoles extends Command {
	/**
	 * @param event
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	int operatorId=Tools.getInt((String)event.getParameterValue("operatorid"), -1);
	if(operatorId!=0) throw new NDSException("无权限");
	JSONObject jo= event.getJSONObject();
  	QueryEngine engine=QueryEngine.getInstance();
  	JSONObject ro=new JSONObject();
	TableManager manager= TableManager.getInstance();
	
	boolean hasError=false;
	Connection conn= engine.getConnection();
	try{
		String isvId=jo.getString("ISVID");
		String appId= jo.getString("AppID");
		String corpId=jo.optString("CorpID");
		
		ro.put("ISVID", isvId);
		ro.put("AppID", appId);
		ro.put("CorpID", corpId);
		JSONArray ja=new JSONArray();
		List al=engine.doQueryList("select g.id, g.name, g.description from groups g, ad_client c where g.ad_client_id=c.id and c.domain='"+ corpId+"'", conn);
		for(int i=0;i<al.size();i++){
			List a=(List)al.get(i);
			JSONObject ao=new JSONObject();
			ao.put("RoleID", a.get(0));
			ao.put("RoleName",a.get(1));
			ao.put("RoleDesc",(a.get(2)==null? "": (String)a.get(2)));
			ja.put(ao);
		}
		JSONObject roles=new JSONObject();
		roles.put("RoleInfo", ja);
		ro.put("RoleList", roles);
		
        
	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
        try{conn.close();}catch(Exception e){}
  	} 
	
	ValueHolder holder= new ValueHolder();
	holder.put("message", "获取角色成功");
	holder.put("code","0");
	holder.put("data", ro);
	return holder;
  }
  /**
   * 
   * @param s, format may be "http://www.xxx.com/" or "www.xxx.com"
   * @return "www.xxx.com"
   */
  private String getWebDomain(String s){
	  if( Validator.isNull(s)) return null;
	  int p= s.indexOf("http://");
	  if(p>-1){
		  s= s.substring(7);
	  }
	  p=s.indexOf('/');
	  if(p>-1){
		  s=s.substring(0,p);
	  }
	  return s;
  }
}