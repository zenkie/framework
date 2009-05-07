package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import nds.security.User;

import java.rmi.RemoteException;
import java.sql.*;


import JOscarLib.Request.Request;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import nds.control.web.*;

public class SelectTemplate extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		TableManager manager=TableManager.getInstance();
		logger.debug(event.toDetailString());
		User usr=helper.getOperator(event);
    	// following check directory permission needs this parameter
		Table table= manager.getTable("WEB_CLIENT");
    	event.setParameter("directory", table.getSecurityDirectory());
    	helper.checkDirectoryWritePermission(event, helper.getOperator(event));
		/*
		 * 
		 if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(),usr.name,table.getName(), objectId, nds.security.Directory.WRITE, event.getQuerySession()))
		  throw new NDSException("@no-permission@");
		 * 
		 * 
		 */
		
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null;
	  	java.util.Locale locale= event.getLocale();
  	    QueryEngine engine=QueryEngine.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
   		try{
	   		params=jo.getJSONObject("params");
	   		int clientId= usr.adClientId;  //params.getInt("clientId");
	   		String template=params.getString("template");
	   		List li=QueryEngine.getInstance().doQueryList("select id from ad_site_template where foldername='"+template+"'");
	   		int  ad_site_template_id=Tools.getInt(li.get(0),-1);
	   	//	System.out.print(ad_site_template_id);
            pstmt= conn.prepareStatement("update web_client set ad_site_template_id=? where ad_client_id=?");		
   			pstmt.setInt(1,ad_site_template_id);
   			pstmt.setInt(2,clientId);
		    pstmt.executeUpdate();
		   
   		}catch(Throwable t){
   	  		if(t instanceof NDSException) throw (NDSException)t;
   	  		logger.error("exception",t);
   	  		throw new NDSException(t.getMessage(), t);	
   	  	}finally{
   	        try{pstmt.close();}catch(Exception ea){}
   	        try{conn.close();}catch(Exception e){}
   	  	} 
   	  	ValueHolder holder= new ValueHolder();
   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
   		holder.put("code","0");
   		return holder;  
   	  }
   


}
