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

public class FW086_AdClientRemove extends Command {
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
	Connection conn= engine.getConnection();
	try{
		
		ArrayList params=new ArrayList();
		params.add( jo.getString("CorpID"));
		
		engine.executeStoredProcedure("ad_client_drop", params, false, conn);
		
        
	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
        try{conn.close();}catch(Exception e){}
  	} 
	
	ValueHolder holder= new ValueHolder();
	holder.put("message", "公司删除成功");
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