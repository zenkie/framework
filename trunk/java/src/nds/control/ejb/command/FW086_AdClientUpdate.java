package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

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

public class FW086_AdClientUpdate extends Command {
	/**
	 * @param event
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	int operatorId=Tools.getInt((String)event.getParameterValue("operatorid"), -1);
	if(operatorId!=0) throw new NDSException("无权限");
	JSONObject jo= event.getJSONObject();
	
  	QueryEngine engine=QueryEngine.getInstance();
  	
	TableManager manager= TableManager.getInstance();
	
	boolean hasError=false;
	PreparedStatement stmt=null;
	PreparedStatement stmt2=null;
    ResultSet rs=null;
	Connection conn= engine.getConnection();
	try{
		
		String sql="update web_client set name=?, shortname=?, LOGOIMG=?, domain=?, contactor=?,phone=?,fax=?,email=?,address=?,postcode=? where ad_client_id=(select id from ad_client where domain=?)";
		
		logger.debug(sql);
        stmt= conn.prepareStatement(sql);
        stmt.setString(1,jo.getString("CorpName"));
        stmt.setString(2,jo.getString("ShortName"));
        stmt.setString(3,jo.optString("CorpLogoURL"));
        stmt.setString(4,getWebDomain(jo.optString("WebSite")));
        stmt.setString(5,jo.optString("LegalPerson"));
        stmt.setString(6,jo.optString("Phone"));
        stmt.setString(7,jo.optString("Fax"));
        stmt.setString(8,jo.optString("Email"));
        stmt.setString(9,jo.optString("Address"));
        stmt.setString(10,jo.optString("ZipCode"));
        stmt.setString(11,jo.getString("CorpID"));
        stmt.executeUpdate();
        
	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
        try{stmt.close();}catch(Exception ea){}
        try{conn.close();}catch(Exception e){}
  	} 
	
	ValueHolder holder= new ValueHolder();
	holder.put("message", "公司修改成功");
	holder.put("code","0");
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