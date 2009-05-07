
package nds.control.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.*;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import nds.control.event.DefaultWebEvent;
import nds.control.util.NavNode;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.TimeLog;
import nds.util.Tools;
import nds.util.Validator;
import nds.util.WebKeys;

import org.codehaus.xfire.transport.http.XFireConfigurableServlet;

import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.service.UserLocalServiceUtil;

 

/**
 * Add support for basic authentication for ws client
 * 
 * @author yfzhu@agilecontrol.com
 */
public class SecuredXFireConfigurableServlet extends XFireConfigurableServlet {
    private Logger logger=LoggerManager.getInstance().getLogger(SecuredXFireConfigurableServlet.class.getName());
	private DataSource datasource;
	private final static String GET_IPRULE="select u.login_ip_rule from users u, ad_client a where u.name=? and u.ad_client_id=a.id and a.domain=?";	

    /**
     * Delegates to {@link XFireServletController#doService(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
        throws ServletException, IOException
    {
    	if(authenticate(request, response))
    		super.doGet(request, response);
    }
    
    /**
     * Delegates to {@link XFireServletController#doService(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
    	if(authenticate(request, response))
    		super.doPost(request, response);
    }
    /**
     * Check authentication in session attribute "USER_ID"
     * @param request
     * @param response
     * @return
     * @throws ServletException
     * @throws IOException
     */
    protected boolean authenticate(HttpServletRequest request,
                         HttpServletResponse response)
        throws ServletException, IOException
    {
    	String username=(String)(request.getSession(true).getAttribute("USER_ID"));
    	boolean isAuthenticated=(username!=null);
    	if(!isAuthenticated){    	
    		try{
    		String credentials =request.getHeader("Authorization");
	    	String password=null;
	    	if (credentials!=null){
	            try
	            {
	                credentials = credentials.substring(credentials.indexOf(' ')+1);
	                credentials = nds.util.B64Code.decode(credentials,nds.util.StringUtils.ISO_8859_1);
	                int i = credentials.indexOf(':');
	                username = credentials.substring(0,i);
	                password = credentials.substring(i+1);
	                logger.debug("username=" + username+", password="+ password);
	            }
	            catch (Exception e) 
	            {
	                e.printStackTrace();
	            }
	        }
	    	
	    	if(Validator.isNotNull(username) && password!=null){
	    		String companyId=WebUtils.getServletContext().getInitParameter("company_id");
	    		int authResult = Authenticator.SUCCESS;

	    		Map headerMap = new HashMap();

	    		Enumeration enu1 = request.getHeaderNames();

	    		while (enu1.hasMoreElements()) {
	    			String name = (String)enu1.nextElement();

	    			Enumeration enu2 = request.getHeaders(name);

	    			List headers = new ArrayList();

	    			while (enu2.hasMoreElements()) {
	    				String value = (String)enu2.nextElement();

	    				headers.add(value);
	    			}

	    			headerMap.put(name, (String[])headers.toArray(new String[0]));
	    		}

	    		Map parameterMap = request.getParameterMap();

	    		
	    		authResult = UserLocalServiceUtil.authenticateByUserId(
	    				companyId, username, password,headerMap,parameterMap );
	    		logger.debug("check password:"+ (authResult==Authenticator.SUCCESS));
	    		if(authResult ==Authenticator.SUCCESS )
	    			isAuthenticated=authenticateByLoginIPRule(request.getRemoteAddr(), username);
				
				if(isAuthenticated){
					WebUtils.getSessionContextManager(request.getSession());
					
					request.getSession().setAttribute("USER_ID", username);
					// set this session timeout to a limit value
					Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
					int interval= Tools.getInt(conf.getProperty("webservice.session.timeout"), 30);
					request.getSession().setMaxInactiveInterval( interval* 60);
				}
	    	}
    		}catch(Throwable t){
    			logger.error("Failed in authication", t );
    		}
    	}
    	if(!isAuthenticated){
    		// send request basic authentication to client
    		response.setContentType("text/html");
            response.setHeader("WWW-Authenticate",
                              "basic realm=\"Agile ERP\"");
                              //WebUtils.getCompanyInfo()+'"');
            response.sendError(401);    		
    	}else{
    		logger.debug("User "+ username +" requests webservice");
    		logger.debug(Tools.toString(request));
    	}
    	
    	return isAuthenticated;
    }
	/**
	 * Get connection to nds2 schema
	 * @return
	 * @throws Exception
	 */
	private Connection getConnection() throws Exception{
		return QueryEngine.getInstance().getConnection();
	}
	/**
	 * Check login ip satisfy iprule in "users.login_ip_rule", which is a regular expression
	 * @param ip
	 * @param login currently only support userid, not email
	 * @return Authenticator.FAILURE or Authenticator.SUCCESS
	 */
	private boolean authenticateByLoginIPRule(String ip, String userId) throws Exception{
		//default to success
		boolean authResult = true;
		Connection conn= getConnection();
		PreparedStatement pstmt= null;
		java.sql.ResultSet rs=null;
		String ipRule=null;
		try{
			/**
			  *  由于 lportal 的限制，名称一律设置为小写
			  *  参见：com.liferay.portal.ejb.UserManagerImpl#_authenticate
			  *  登录的时候由系统强行设置登录名为小写后验证
			  */
			String domainName=userId;
			String uName, adclientName ;
			int p=domainName.indexOf("@");
			if ( p<=0){
				return authResult;
				//throw new AuthException("user id is invalid:"+userId);
			}
			uName= domainName.substring(0,p );
			adclientName= domainName.substring(p+1);
			
			pstmt= conn.prepareStatement(GET_IPRULE);
			
	    	pstmt.setString(1, uName);
	    	pstmt.setString(2, adclientName);
	    	rs=pstmt.executeQuery();
	    	if(rs.next()){
	    		ipRule=rs.getString(1);
	    		if(ipRule!=null && ipRule.trim().length()>1 ){
	    			try{
	    				Pattern reg= Pattern.compile(ipRule);
		    			if(!reg.matcher(ip).find()){
		    				logger.debug("Error ip rule:"+ipRule +" for ip:"+ ip+" of user:"+ userId);
		    				authResult=false;
		    			}
	    			}catch(Exception e){
	    				logger.error("Could not parse ip rule:"+ipRule +" for ip:"+ ip+" of user:"+ userId+":"+e);
	    				authResult=false;
	    			}
	    		}
	    	}else{
	    		// not find the user, let following authenticator to check it
	    	}
	    	
		}finally{
			try{if(rs!=null)rs.close();}catch(Exception e3){}
			try{if(rs!=null)pstmt.close();}catch(Exception e3){}
			try{if(rs!=null)conn.close();}catch(Exception e){}
		}		
		return authResult;
	}    
}
