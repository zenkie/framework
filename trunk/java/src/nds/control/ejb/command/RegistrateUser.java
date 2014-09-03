package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.sql.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.directwebremoting.WebContext;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.web.*;
import nds.control.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.saasifc.UserUtils;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;
import com.liferay.portal.action.LoginAction.*;
/**
. 
 	  
*/
public class RegistrateUser  extends Command{
	private String  defaultRootId;  
	private static Logger logger= LoggerManager.getInstance().getLogger(RegistrateUser.class.getName());
	  
	private static final String GET_USER_BY_MAIL="select u.name, u.passwordhash, u.isactive, u.description, c.domain, u.ad_client_id, u.email from users u , ad_client c where u.email=? and c.id=u.ad_client_id";

	public  RegistrateUser(){
		defaultRootId=( EJBUtils.getApplicationConfigurations().getProperty("default.root.id","0"));
		
	}
	/*
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}
*/
    /**
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
    	ValueHolder vh=null;
    	try{
	    	long beginTime= System.currentTimeMillis();
	        logger.debug(event.toDetailString());
	        JSONObject jo= event.getJSONObject();
        	// login session 
			WebContext ctx = (WebContext)jo.get("org.directwebremoting.WebContext");
			String serverValidCode= (String)ctx.getSession().getAttribute("nds.control.web.ValidateMServlet");
			if(serverValidCode ==null ) throw new NDSEventException("Internal error, nds.control.web.ValidateMServlet not set in session attribute");

			JSONObject jor=jo.getJSONObject("params");
			String userValidCode=jor.getString("verifyCode").trim();
			 logger.debug("userValidCode "+userValidCode);
			 logger.debug("serverValidCode "+serverValidCode);
			if(!serverValidCode.equalsIgnoreCase(userValidCode)){
	        	throw new NDSEventException("@error-verify-code@");
	        }
			
			Iterator it = jor.keys();  
            while (it.hasNext()) {  
                String key = (String) it.next();  
                String value = jor.getString(key);  
                event.setParameter(key, value);
            }  
			 
	        // call ObjectCreate event to insert data to db
	        event.setParameter("command","ObjectCreate");
	        event.setParameter("directory","WX_REGUSER_LIST");
	        event.setParameter("operatorid", defaultRootId);
	        event.setParameter("best_effort","true");
	        
	        String wxappid=(String) jor.getString("WXAPPID");
	        String email=(String) jor.getString("PHONENUMBER")+"@syman.cn";
	        logger.debug("email ->"+email);
	        helper.getOperator(event);
	        vh = helper.handleEvent(event);
	
	        if(Tools.getInt( vh.get("code"), 0) != 0 ){
	        	throw new NDSException((String)vh.get("message"));
	        }
	        
	        User user=getUser(email);
	        //logger.debug("user"+user.email);
	        WebUtils.loginSSOUser(user,ctx.getHttpServletRequest() , ctx.getHttpServletResponse());
	        //com.liferay.portal.action.LoginAction.login(ctx.getHttpServletRequest(), ctx.getHttpServletResponse(),user.email,user.passwordHash, false);
	        Cookie cookie=new Cookie("name",user.name);
			cookie.setMaxAge(1000000);
			ctx.getHttpServletResponse().addCookie(cookie);
	        SessionContextManager scmanager= WebUtils.getSessionContextManager(ctx.getSession());
	        UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
	        //logger.debug("user"+usr.getUserName());
	        vh.put("message", "@registrate-success@");
	        vh.put("next-screen", "/register.jsp");
	        if(wxappid!=null){
	        	String domin=String.valueOf(QueryEngine.getInstance().doQueryOne("select t.domain from web_client t where t.wxnum='"+wxappid+"'"));
	        	WeUtilsManager Wemanage =WeUtilsManager.getInstance();
				Wemanage.getAdClientTemplateFolder(domin);
	        }
	        return vh;
    	}catch(Throwable t){
    		logger.error("Error", t);
    		vh=new ValueHolder();
			vh.put("code", "-1");
			vh.put("message",	WebUtils.getExceptionMessage(t, TableManager.getInstance().getDefaultLocale()));
    		try{
    			vh.put("next-screen", "/register.jsp?err="+ java.net.URLEncoder.encode(	WebUtils.getExceptionMessage(t, TableManager.getInstance().getDefaultLocale()), "UTF-8") );
    		}catch(Throwable t2){
    			
    		}
    	}
    	return vh;
    }
    
    
	/**
	 * Will check user information in "users" table
	 * @param vendor
	 * @param userId
	 * @return null if user not found, exception when error found (db error)
	 */
    public static User getUser(String email) throws NDSException {
        User usr=null;
        Connection con=null;
        PreparedStatement pstmt= null;
        ResultSet rs= null;
        try {
            con= QueryEngine.getInstance().getConnection();
            pstmt= con.prepareStatement(GET_USER_BY_MAIL);
            pstmt.setString(1,email);
            rs= pstmt.executeQuery();
            if( rs.next()){
                usr= new User();
                usr.name=(rs.getString(1));
                usr.passwordHash=(rs.getString(2));
                usr.isActive= "Y".equals( rs.getString(3));
                usr.description=(rs.getString(4));
                usr.clientDomain=(rs.getString(5));
                usr.adClientId = rs.getInt(6);
                usr.email= rs.getString(7);
            }
        }
        catch(Exception e) {
            logger.debug("Errors found when trying to get operator from event.",e);
            throw new NDSException("@exception@", e);
            
        }finally{
            if( rs!=null){try{rs.close();}catch(Exception e){}}
            if( pstmt!=null){try{pstmt.close();}catch(Exception e){}}
            if( con!=null){try{con.close();}catch(Exception e){}}
        }
        return usr;
		
	} 
}