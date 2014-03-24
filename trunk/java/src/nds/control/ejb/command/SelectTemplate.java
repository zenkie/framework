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
		//System.out.print("SelectTemplate is going!");
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null;
	  	java.util.Locale locale= event.getLocale();
  	    QueryEngine engine=QueryEngine.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
   		int tmpid=-1;
   		JSONObject rs=new JSONObject();
   		try{
	   		params=jo.getJSONObject("params");
	   		int clientId= usr.adClientId;  //params.getInt("clientId");
	   		String templateid=params.getString("templateid");

	   		String colname="";
	   		List li=QueryEngine.getInstance().doQueryList("select id,TPCLASS from ad_site_template where id='"+templateid+"'");
	        if (li.size() > 0) {
	        	tmpid = Tools.getInt(((List)li.get(0)).get(0), -1);
	        	colname = String.valueOf(((List)li.get(0)).get(1)).trim()+"_TMP";
	         }
            pstmt= conn.prepareStatement("update WEB_CLIENT_TMP set "+colname+"=? where ad_client_id=?");		
   			pstmt.setInt(1,tmpid);
   			pstmt.setInt(2,clientId);
		    pstmt.executeUpdate();
	   		rs.put("tmpid", tmpid);
		   
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
   		holder.put("data",rs);
   		return holder;  
   	  }
   


}
