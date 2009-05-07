package nds.control.ejb.command;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.Configurations;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.Tools;

import org.directwebremoting.WebContext;
import org.json.JSONObject;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.tenpay.util.MD5Util;
import com.tenpay.util.TenpayUtil;

import   java.security.*;  

public class VAR_MODIFY_PASSWORD extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
		
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null;
	  	User usr=helper.getOperator(event);
	  	
	  	//helper.checkDirectoryWritePermission(event, usr);
	  	java.util.Locale locale= event.getLocale();
	  	Connection conn= QueryEngine.getInstance().getConnection();
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		String key= (String)conf.getProperty("var.passwordcord");
		String var_url= (String)conf.getProperty("var.bizportal");
		String url="";
		HttpServletRequest request=null;
   		try{
   			params=jo.getJSONObject("params");
			String userValidCode= params.getString("verifyCode");
			WebContext ctx=(WebContext) jo.opt("org.directwebremoting.WebContext");
	  		if(ctx!=null){
		  	request = ctx.getHttpServletRequest();
			String serverValidCode=(String)request.getSession().getAttribute("nds.control.web.ValidateMServlet");
	    	if(serverValidCode.equalsIgnoreCase(userValidCode)){
	    		   
	    	     }else{
	    		      throw new NDSEventException("@error-verify-code@");
	    	     }
	  		}
	  		String password1=params.getString("password1");
	  		String email=params.getString("email");
		  	int userid=usr.getId();
		  	int cnt=Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from e_client where agent_id=(select id from c_bpartner where id=(select c_bpartner_id from users where id="+userid+") and isagent='Y') and email='"+email+"'"), 0);
			if(cnt==0)	{
		  	        throw new NDSException("@no-permission@");
			}
			if(ctx!=null){
			    request = ctx.getHttpServletRequest();
			    ArrayList params1=new ArrayList();
    	        params1.add(email);
    	        ArrayList res=new ArrayList();
    	        res.add(Integer.class);
    	        java.util.Collection result=QueryEngine.getInstance().executeFunction("var_password_id", params1,res,conn );
    	        int user_id=Tools.getInt(result.toArray()[0],-1);
			    StringBuffer buf = new StringBuffer(); 
			    TenpayUtil.addBusParameter(buf, "user_id", user_id);
			    TenpayUtil.addParameter(buf, "password", password1);
			    String requestParameters = buf.toString();
			    TenpayUtil.addParameter(buf, "key", key);
			    String encode=MD5Util.MD5Encode(buf.toString()).toUpperCase();
			    buf = new StringBuffer(requestParameters);
			    TenpayUtil.addParameter(buf, "encode",encode);
			    requestParameters = buf.toString();
			   // url=java.net.URLEncoder.encode(var_url+"/html/nds/var/var_password.jsp?"+requestParameters,"UTF-8");
			    url="http://"+var_url+"/html/nds/var/var_password.jsp?"+requestParameters;
			   // System.out.print(url);
			    URL  varurl = new URL(url);
			 /*   RequestDispatcher rd = request.getRequestDispatcher(url);
			    rd.forward(request,response); 
			    */
			    URLConnection connection = varurl.openConnection();
			    InputStream is = connection.getInputStream();
			    is.close();
			 }
			} catch (Throwable t) {
				if(t instanceof NDSException) throw (NDSException)t;
		  		throw new NDSException(t.getMessage(), t);
			}
		//	request.getSession().setAttribute("url", url);
   	     	ValueHolder holder= new ValueHolder();
   		    holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
   		    holder.put("code","0");
   		    return holder;
   	  }
}

