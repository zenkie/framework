package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import java.util.*;

import nds.security.User;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

import JOscarLib.Request.Request;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import nds.control.web.*;

/**
 * 
 * 将用户在/html/nds/option/option.jsp设置的内容保存到AD_USER_PREF表中
 * name 一律转换为大写
 * 
 *
 */
public class SaveOption  extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	
	  	JSONObject params=null;
	  	java.util.Locale locale= event.getLocale();
//	  	JSONObject returnObj=new JSONObject();
	  	User usr=helper.getOperator(event);
    	int userid=usr.getId();
    	if(userid==nds.control.web.UserWebImpl.GUEST_ID) throw new NDSException("@no-permission@");
    	//helper.checkDirectoryWritePermission(event, usr);
  	    QueryEngine engine=QueryEngine.getInstance();
        boolean hasError=false;
        MessagesHolder mh= MessagesHolder.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
   		try{
	   		params=jo.getJSONObject("params");
	   		String name,value,sql;
	   		sql="delete from ad_user_pref t where t.module NOT LIKE '%print' AND t.module NOT LIKE 'cxtab%' AND t.module != 'qlc' and t.ad_user_id="+userid;
   			JSONArray names=params.names();
   			pstmt= conn.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt= conn.prepareStatement("insert into ad_user_pref(id,ad_user_id,module,name,value) values( get_sequences('ad_user_pref'), ?, 'ad_option',?,?)");
		   for(int i= 0;i< names.length();i++){ 
			   name=names.getString(i);
		       value=params.getString(name);
		       if(value==null) continue;
		       pstmt.setInt(1,  userid);
		       pstmt.setString(2, name.toUpperCase());
		       pstmt.setString(3,value);
		       pstmt.executeUpdate();
		   }
		   // Reload user options
		   try{
			   WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
			   UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(wc.getHttpServletRequest().getSession()).getActor(nds.util.WebKeys.USER));	
			   userWeb.loadUserOptions();
		   }catch(Throwable t2){
			   logger.error("Could not fetch http session infor from json:"+t2);
		   }
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