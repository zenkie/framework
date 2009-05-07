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

public class FW086_AdClientCreate extends Command {
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
//    ResultSet rs=null;
	Connection conn= engine.getConnection();
	Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
	String templateClient=conf.getProperty("newclient.template","demo");
						     
	try{
		String shortName= jo.optString("ShortName");
		String corpName= jo.optString("CorpName");
		String corpId=  jo.getString("CorpID");
		
		if(Validator.isNull(shortName))shortName=corpName;
		if(Validator.isNull(corpName)) corpName=shortName;
		if(Validator.isNull(shortName))shortName=corpId;
		if(Validator.isNull(corpName)) corpName=corpId;
		

		  
		  ArrayList params=new ArrayList();
		  params.add(jo.getString("AdminEmail"));
		  params.add(jo.getString("AdminGUID"));// 
		  params.add("fw086");// 
		  params.add(new Integer(100));//
		  params.add(templateClient);
		  params.add(shortName);//
		  params.add("test");
		  params.add(corpId);
		  
		  SPResult result=engine.executeStoredProcedure("ad_client_saas_new", params, true, conn);
		  String message= result.getMessage();
		  int code= result.getCode();// this is ad_client_id that created
		  String domain =(String )engine.doQueryOne("select domain from ad_client where id="+ code, conn);
		  boolean isMultipleClientEnabled= "true".equals(conf.getProperty("webclient.multiple","false"));					
		  if(isMultipleClientEnabled){
			// clone web folder in /act/webroot/$domain
			String webRoot= conf.getProperty("client.webroot","/act/webroot");
			String srcClientFolder= webRoot+"/"+  templateClient;
			String destClientFolder=webRoot+"/"+  domain;

			logger.debug("copy dir "+ srcClientFolder+" to "+ destClientFolder);
			nds.util.FileUtils.delete(destClientFolder);
			nds.util.FileUtils.copyDirectory(srcClientFolder, destClientFolder);
			

		  }		

/*		
		// update root
		String sql="update users set saasvendor='fw086', saasuser="+ QueryUtils.TO_STRING(jo.getString("AdminGUID"))+", phone="+ 
			QueryUtils.TO_STRING(jo.optString("AdminPhone")) +", phone2="+ 
			QueryUtils.TO_STRING(jo.optString("AdminMobile")) +" where email='"+
			jo.getString("AdminEmail")+"' and ad_client_id=(select id from ad_client where domain='"+corpId +"')";
		logger.debug(sql);
		conn.createStatement().executeUpdate(sql);
		
		if(isMultipleClientEnabled){
		// not all schema contains web_client, such like Next99
			sql="update web_client set name=?, shortname=?, LOGOIMG=?, domain=?, contactor=?,phone=?,fax=?,email=?,address=?,postcode=? where ad_client_id=(select id from ad_client where domain=?)";
			
			logger.debug(sql); 
	        stmt= conn.prepareStatement(sql);
	        stmt.setString(1,corpName);
	        stmt.setString(2,shortName);
	        stmt.setString(3,jo.optString("CorpLogoURL"));
	        stmt.setString(4,getWebDomain(jo.optString("WebSite")));
	        stmt.setString(5,jo.optString("LegalPerson"));
	        stmt.setString(6,jo.optString("Phone"));
	        stmt.setString(7,jo.optString("Fax"));
	        stmt.setString(8,jo.optString("Email"));
	        stmt.setString(9,jo.optString("Address"));
	        stmt.setString(10,jo.optString("ZipCode"));
	        stmt.setString(11,corpId);
	        stmt.executeUpdate();
		}
        // update images for this client
        //nds.control.util.WebClientUtils.createImages(corpId, corpName, conf.getProperty("newclient.saying",""));
*/        
	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
        try{if(stmt!=null)stmt.close();}catch(Exception ea){}
        try{conn.close();}catch(Exception e){}
  	} 
	
	ValueHolder holder= new ValueHolder();
	holder.put("message", "公司创建成功");
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