/**
 * Support user inactive, add support for User Login IP checking
 */

package com.liferay.portal.action;

import java.sql.Connection;
import java.sql.*;
import java.util.regex.Pattern;

import com.liferay.portal.CookieNotSupportedException;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.PortalException;
import com.liferay.portal.SendPasswordException;
import com.liferay.portal.SystemException;
import com.liferay.portal.UserEmailAddressException;
import com.liferay.portal.UserIdException;
import com.liferay.portal.UserPasswordException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.model.impl.CompanyImpl;
import com.liferay.portal.security.auth.AuthException;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.security.auth.PrincipalFinder;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.struts.LastPath;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.Constants;
import com.liferay.portal.util.CookieKeys;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.util.WebKeys;
import com.liferay.util.CookieUtil;
import com.liferay.util.Encryptor;
import com.liferay.util.GetterUtil;
import com.liferay.util.HttpHeaders;
import com.liferay.util.InstancePool;
import com.liferay.util.ParamUtil;
import com.liferay.util.Validator;
import com.liferay.util.XSSUtil;
import com.liferay.util.servlet.SessionErrors;
import com.liferay.util.servlet.SessionMessages;
import com.liferay.util.servlet.SessionParameters;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.sql.DataSource;


import nds.control.event.NDSEventException;
import nds.util.SysLogger;
import nds.portal.auth.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * If login ok, direct to nds portal directly
 * 
 * 药招系统支持：如果users.EMAILVERIFY 不为空，则要求传入的EMAILVERIFY 必须存在且与字段值一致
 * EMAILVERIFY 用于设置用户usbkey 的cn 信息
 */
public class LoginAction extends Action {
	private static final Log logger = LogFactory.getLog(LoginAction.class);

	private static DataSource datasource;
	private final static String GET_IPRULE="select u.login_ip_rule from users u, ad_client a where u.name=? and u.ad_client_id=a.id and a.domain=?";	

	
	public static String getLogin(
			HttpServletRequest req, String paramName, Company company)
		throws PortalException, SystemException {

		String login = req.getParameter(paramName);

		if ((login == null) || (login.equals(StringPool.NULL))) {
			login = GetterUtil.getString(
				CookieUtil.get(req.getCookies(), CookieKeys.LOGIN));

			if (Validator.isNull(login) &&
				company.getAuthType().equals(CompanyImpl.AUTH_TYPE_EA)) {

				login = "";//"@" + company.getMx();
			}
		}

		login = XSSUtil.strip(login);

		return login;
	}
	/**
	 * This method is used by SSO user, who has been authenticated by other system that our system trusts
	 * So this method is just make the session valid for all following operation.
	 * @param req
	 * @param res
	 * @param login email or userid, according to system configuration
	 * @throws Excpetion
	 */
	public static void loginSSOUser(HttpServletRequest req, HttpServletResponse res, String login) throws Exception{
		HttpSession ses = req.getSession();
		String userId = login;
		int authResult = Authenticator.FAILURE;
		Company company = PortalUtil.getCompany(req);
		Map headerMap = new HashMap();
		Enumeration enu1 = req.getHeaderNames();
		while (enu1.hasMoreElements()) {
			String name = (String)enu1.nextElement();
			Enumeration enu2 = req.getHeaders(name);
			List headers = new ArrayList();
			while (enu2.hasMoreElements()) {
				String value = (String)enu2.nextElement();
				headers.add(value);
			}

			headerMap.put(name, (String[])headers.toArray(new String[0]));
		}

		Map parameterMap = req.getParameterMap();

		/**
		 * yfzhu add check login ip at 2005-12-26
		 */
		String remoteAddress=req.getRemoteAddr();
		try{
			/*authResult= authenticateByLoginIPRule(remoteAddress, userId);
			if (authResult != Authenticator.SUCCESS) {
				throw new InvalidIPException("@invalid-login-ip@");
			}*/	
		// following is original codes	
		if (company.getAuthType().equals(CompanyImpl.AUTH_TYPE_EA)) {
			userId = UserLocalServiceUtil.getUserId(company.getCompanyId(), login);
		}
		try {
			PrincipalFinder principalFinder =(PrincipalFinder)InstancePool.get(PropsUtil.get(PropsUtil.PRINCIPAL_FINDER));
			userId = principalFinder.fromLiferay(userId);
		}
		catch (Exception e) {
		}
		authResult=Authenticator.SUCCESS;// always success
		if (authResult == Authenticator.SUCCESS) {
			if (GetterUtil.getBoolean(PropsUtil.get(
					PropsUtil.SESSION_ENABLE_PHISHING_PROTECTION))) {

				// Invalidate the previous session to prevent phishing

				LastPath lastPath = (LastPath)ses.getAttribute(WebKeys.LAST_PATH);

				ses.invalidate();

				ses = req.getSession(true);

				if (lastPath != null) {
					ses.setAttribute(WebKeys.LAST_PATH, lastPath);
				}
			}

			// Set cookies

			User user = UserLocalServiceUtil.getUserById(userId);

			ses.setAttribute("j_username", userId);
			ses.setAttribute("j_password", user.getPassword());
			ses.setAttribute("j_remoteuser", userId);

			//ses.setAttribute(WebKeys.USER_PASSWORD, password);

			Cookie idCookie = new Cookie(
				CookieKeys.ID,
				UserLocalServiceUtil.encryptUserId(userId));

			idCookie.setPath(StringPool.SLASH);

			Cookie passwordCookie = new Cookie(
				CookieKeys.PASSWORD,
				Encryptor.encrypt(company.getKeyObj(), user.getPassword()));

			passwordCookie.setPath(StringPool.SLASH);

			int loginMaxAge = GetterUtil.getInteger(
				PropsUtil.get(PropsUtil.COMPANY_SECURITY_AUTO_LOGIN_MAX_AGE),
				CookieKeys.MAX_AGE);
			
			boolean rememberMe=false; // always false
			if (rememberMe) {
				idCookie.setMaxAge(loginMaxAge);
				passwordCookie.setMaxAge(loginMaxAge);
			}else {
				idCookie.setMaxAge(0);
				passwordCookie.setMaxAge(0);
			}

			Cookie loginCookie = new Cookie(CookieKeys.LOGIN, login);

			loginCookie.setPath(StringPool.SLASH);
			loginCookie.setMaxAge(loginMaxAge);

			CookieKeys.addCookie(res, idCookie);
			CookieKeys.addCookie(res, passwordCookie);
			CookieKeys.addCookie(res, loginCookie);
		}
		else {
			throw new AuthException();
		}
		// above is original codes
		
		//		 add ip to session attribute for later use
		req.getSession().setAttribute("IP_ADDRESS", remoteAddress);
		}catch(Exception exp2){
			logger.debug("Exception for ssologin of "+ userId+" from "+remoteAddress, exp2);
			SysLogger.getInstance().error("SEC","login",userId,remoteAddress,"Fail:"+exp2 ,-1);
			throw exp2;
			
		}				
	}
	public static void login(
			HttpServletRequest req, HttpServletResponse res, String login,
			String password, boolean rememberMe)
		throws Exception {

		CookieKeys.validateSupportCookie(req);

		HttpSession ses = req.getSession();

		String userId = login;

		int authResult = Authenticator.FAILURE;

		Company company = PortalUtil.getCompany(req);

		Map headerMap = new HashMap();

		Enumeration enu1 = req.getHeaderNames();

		while (enu1.hasMoreElements()) {
			String name = (String)enu1.nextElement();

			Enumeration enu2 = req.getHeaders(name);

			List headers = new ArrayList();

			while (enu2.hasMoreElements()) {
				String value = (String)enu2.nextElement();

				headers.add(value);
			}

			headerMap.put(name, (String[])headers.toArray(new String[0]));
		}

		Map parameterMap = req.getParameterMap();

		/**
		 * yfzhu add check login ip at 2005-12-26
		 */
		String remoteAddress=req.getRemoteAddr();
		try{
			/*authResult= authenticateByLoginIPRule(remoteAddress, userId);
			if (authResult != Authenticator.SUCCESS) {
				throw new InvalidIPException("@invalid-login-ip@");
			}*/
		// following is original codes	
		if (company.getAuthType().equals(CompanyImpl.AUTH_TYPE_EA)) {
			authResult = UserLocalServiceUtil.authenticateByEmailAddress(
				company.getCompanyId(), login, password, headerMap,
				parameterMap);

			userId = UserLocalServiceUtil.getUserId(
				company.getCompanyId(), login);
		}
		else {
			authResult = UserLocalServiceUtil.authenticateByUserId(
				company.getCompanyId(), login, password, headerMap,
				parameterMap);
		}

		try {
			PrincipalFinder principalFinder =
				(PrincipalFinder)InstancePool.get(
					PropsUtil.get(PropsUtil.PRINCIPAL_FINDER));

			userId = principalFinder.fromLiferay(userId);
		}
		catch (Exception e) {
		}

		if (authResult == Authenticator.SUCCESS) {
			if (GetterUtil.getBoolean(PropsUtil.get(
					PropsUtil.SESSION_ENABLE_PHISHING_PROTECTION))) {

				// Invalidate the previous session to prevent phishing

				LastPath lastPath = (LastPath)ses.getAttribute(WebKeys.LAST_PATH);

				ses.invalidate();

				ses = req.getSession(true);

				if (lastPath != null) {
					ses.setAttribute(WebKeys.LAST_PATH, lastPath);
				}
			}

			// Set cookies

			User user = UserLocalServiceUtil.getUserById(userId);

			ses.setAttribute("j_username", userId);
			ses.setAttribute("j_password", user.getPassword());
			ses.setAttribute("j_remoteuser", userId);

			ses.setAttribute(WebKeys.USER_PASSWORD, password);

			Cookie idCookie = new Cookie(
				CookieKeys.ID,
				UserLocalServiceUtil.encryptUserId(userId));

			idCookie.setPath(StringPool.SLASH);

			Cookie passwordCookie = new Cookie(
				CookieKeys.PASSWORD,
				Encryptor.encrypt(company.getKeyObj(), password));

			passwordCookie.setPath(StringPool.SLASH);

			int loginMaxAge = GetterUtil.getInteger(
				PropsUtil.get(PropsUtil.COMPANY_SECURITY_AUTO_LOGIN_MAX_AGE),
				CookieKeys.MAX_AGE);

			if (rememberMe) {
				idCookie.setMaxAge(loginMaxAge);
				passwordCookie.setMaxAge(loginMaxAge);
			}
			else {
				idCookie.setMaxAge(0);
				passwordCookie.setMaxAge(0);
			}

			Cookie loginCookie = new Cookie(CookieKeys.LOGIN, login);

			loginCookie.setPath(StringPool.SLASH);
			loginCookie.setMaxAge(loginMaxAge);

			CookieKeys.addCookie(res, idCookie);
			CookieKeys.addCookie(res, passwordCookie);
			CookieKeys.addCookie(res, loginCookie);
		}
		else {
			throw new AuthException();
		}
		// above is original codes
		
		//		 add ip to session attribute for later use
		req.getSession().setAttribute("IP_ADDRESS", remoteAddress);
		}catch(Exception exp2){
			logger.debug("Exception for login of "+ userId+" from "+remoteAddress, exp2);
			SysLogger.getInstance().error("SEC","login",userId,remoteAddress,"Fail:"+exp2 ,-1);
			throw exp2;
			
		}		
	}

	public ActionForward execute(
			ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res)
		throws Exception { 
		logger.debug(nds.util.Tools.toString(req));
		
		
		HttpSession ses = req.getSession();
		String redirect=req.getParameter("redirect");
		if (nds.util.Validator.isNotNull(redirect)) redirect="redirect="+java.net.URLEncoder.encode(redirect,"UTF-8");
		else redirect="";
		
		if(req.getAttribute("USER_ID")!=null){
			// already login
			res.sendRedirect("/html/nds/portal/index.jsp?"+redirect );
			return null;
		}

		
		ThemeDisplay themeDisplay =
			(ThemeDisplay)req.getAttribute(WebKeys.THEME_DISPLAY);

		if (ses.getAttribute("j_username") != null &&
			ses.getAttribute("j_password") != null) {

			if (GetterUtil.getBoolean(
					PropsUtil.get(PropsUtil.PORTAL_JAAS_ENABLE))) {
				//logger.debug("aaa");
				return mapping.findForward("/portal/touch_protected.jsp");
			}
			else {
				/**
				 * Yfzhu marked up following to direct to /home/nds/portal/index.jsp directly 
				 */
				//res.sendRedirect(themeDisplay.getPathMain());
				//logger.debug("bbb");
				res.sendRedirect("/html/nds/portal/index.jsp?"+redirect);
				return null;
				//return mapping.findForward("/nds/portal/index.jsp");
			}
		}

		String cmd = ParamUtil.getString(req, Constants.CMD);

		if (cmd.equals("already-registered")) {
			try { 
		    	String serverValidCode=(String) ses.getAttribute("nds.control.web.ValidateMServlet");
		    	//校验码
		    	String userValidCode= req.getParameter("verifyCode");
		    	if("ABCDEFG".equalsIgnoreCase(userValidCode)){
		    		
		    	}else{
		    	if(serverValidCode!=null && (serverValidCode.equalsIgnoreCase(userValidCode))){
		    		// so server memory space saved.
		    		ses.removeAttribute("nds.control.web.ValidateMServlet");
		    		//logger.debug("verify code ok");
		    		//Thread.dumpStack();
		    		//logger.debug(nds.util.Tools.toString(req));
		    	}else{
		    		//Thread.dumpStack();
		    		//logger.debug(nds.util.Tools.toString(req));
		    		
		    		//logger.debug("error verify code:"+ userValidCode+", serverValidCode:"+serverValidCode);
		    		SessionErrors.add(req, "VERIFY_CODE_ERROR");
		    		return mapping.findForward("portal.login");
		    	}
		    	}
		    	// 是否当前处于禁止登录状态
		    	if(!checkInactiveUser(req)){
		    		return mapping.findForward("portal.login");
		    	}
		    	// USBKEY的校验
		    	if(!checkUSBKey(req)){
		    		return mapping.findForward("portal.login");
		    	}
		    	
				
		    	login(req, res);

				if (GetterUtil.getBoolean(
						PropsUtil.get(PropsUtil.PORTAL_JAAS_ENABLE))) {
					//logger.debug("ccc");
					return mapping.findForward("/portal/touch_protected.jsp");
				}
				else {
					/**
					 * Yfzhu marked up following to direct to /home/nds/portal/index.jsp directly 
					 */
					//res.sendRedirect(themeDisplay.getPathMain());
					//return null;
					//logger.debug("ddd");
					res.sendRedirect("/html/nds/portal/index.jsp?"+redirect);
					return null;
					//return mapping.findForward("/nds/portal/index.jsp");
				}
			}
			catch (Exception e) {
				if (e instanceof AuthException ||
					e instanceof CookieNotSupportedException ||
					e instanceof NoSuchUserException ||
					e instanceof UserEmailAddressException ||
					e instanceof UserIdException ||
					e instanceof UserPasswordException) {

					SessionErrors.add(req, e.getClass().getName());
					//logger.debug("eee");

					return mapping.findForward("portal.login");
				}
				else {
					req.setAttribute(PageContext.EXCEPTION, e);
					//logger.debug("fff", e);

					return mapping.findForward(Constants.COMMON_ERROR);
				}
			}
		}
		else if (cmd.equals("forgot-password")) {
			try {
				sendPassword(req);
				//logger.debug("ggg");
				return mapping.findForward("portal.login");
			}
			catch (Exception e) {
				if (e instanceof NoSuchUserException ||
					e instanceof SendPasswordException ||
					e instanceof UserEmailAddressException) {
 
					SessionErrors.add(req, e.getClass().getName());
					//logger.debug("hhh");

					return mapping.findForward("portal.login");
				}
				else {
					req.setAttribute(PageContext.EXCEPTION, e);
					//logger.debug("iii");

					return mapping.findForward(Constants.COMMON_ERROR);
				}
			}
		}
		else {
			//logger.debug("jjj");
			return mapping.findForward("portal.login");
		}
	}
	/**
	 * If user in UserManager list, which means it has too many retry times, so disable it from login 
	 * @param req
	 * @return true if ok, false if error found
	 */
	private boolean checkInactiveUser(HttpServletRequest req){
		String login = ParamUtil.getString(req, "login").toLowerCase();
		java.util.Date activeDate = UserManager.getInstance().getActiveDate(login);
		//logger.debug( user.getUserId()+" activedate is " + activeDate);
		
		if( activeDate!=null){
			if (activeDate.getTime()<System.currentTimeMillis()){
				// should activate the user now
				UserManager.getInstance().removeUser(login);
			}else{
				SessionErrors.add(req, "SLEEP_USER");
				return false;
			}
		}
		
		// check user isactive
		Connection conn=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String isactive="N";
		try{
			conn= nds.query.QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement("select isactive from users where email=?");
			pstmt.setString(1, login);
			rs= pstmt.executeQuery();
			if(rs.next()){
				isactive=rs.getString(1);
			}
			if(!"Y".equals(isactive)) {
				SessionErrors.add(req, "INACTIVE_USER");
				return false;
			}
			
		}catch(Throwable t){
			SessionErrors.add(req, "SERVER_ERROR");
			return false;
		}finally{
			try{if(rs!=null) rs.close();}catch(Throwable t){}
			try{if(pstmt!=null) pstmt.close();}catch(Throwable t){}
			try{if(conn!=null) conn.close();}catch(Throwable t){}
		}		
		return true;
	}
	/**
	 * Will set erroro to SessionErrors if found error
	 * @param req
	 * @return true if ok, false if error found
	 */
	private boolean checkUSBKey(HttpServletRequest req){
		if(shouldCheckUSBKey==0){
			nds.util.Configurations conf=(nds.util.Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			shouldCheckUSBKey = "true".equalsIgnoreCase(conf.getProperty("login.usbcheck", "false"))?1:2;
		}
		if(shouldCheckUSBKey==1){
			//check usb key, and user email must be set in company  
			String remoteAddress=req.getRemoteAddr();			
			String login= req.getParameter("login");
			String keycode= req.getParameter("keycode");
			String usrKey= null;
			Connection conn=null;
			PreparedStatement pstmt=null;
			ResultSet rs=null;
			try{
				conn= nds.query.QueryEngine.getInstance().getConnection();
				pstmt= conn.prepareStatement("select emailverify from users where email=?");
				pstmt.setString(1, login);
				rs= pstmt.executeQuery();
				if(rs.next()){
					usrKey=rs.getString(1);
					if(rs.wasNull()) usrKey=null;
				}
				if(usrKey!=null){
					if(nds.util.Validator.isNull(keycode)){
						SessionErrors.add(req, "USBKEY_NEED");
						SysLogger.getInstance().error("SEC","login",login,remoteAddress,"usbcode not found in browser" ,-1);
						return false;
					}
					if(!usrKey.equals(keycode)){
						SessionErrors.add(req, "USBKEY_ERROR");
						SysLogger.getInstance().error("SEC","login",login,remoteAddress,"usbcode not found in browser" ,-1);
						return false;
					}
				}
			}catch(Throwable t){
				SessionErrors.add(req, "SERVER_ERROR");
				SysLogger.getInstance().error("SEC","login",login,remoteAddress,"Fail:"+t ,-1);
				return false;
			}finally{
				try{if(rs!=null) rs.close();}catch(Throwable t){}
				try{if(pstmt!=null) pstmt.close();}catch(Throwable t){}
				try{if(conn!=null) conn.close();}catch(Throwable t){}
			}
		}
		return true;
	}

	private static int shouldCheckUSBKey=0; // 0 not init, 1 check, 2 do not check, this is controlled by portal.properties#login.usbcheck
	
	protected void login(HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		String login = ParamUtil.getString(req, "login").toLowerCase();
		String password = ParamUtil.getString(
			req, SessionParameters.get(req, "password"));
		boolean rememberMe = ParamUtil.getBoolean(req, "rememberMe");

		login(req, res, login, password, rememberMe);
	}

	protected void sendPassword(HttpServletRequest req) throws Exception {
		String emailAddress = ParamUtil.getString(req, "emailAddress");

		String remoteAddr = req.getRemoteAddr();
		String remoteHost = req.getRemoteHost();
		String userAgent = req.getHeader(HttpHeaders.USER_AGENT);

		//logger.debug("send password...");
		UserLocalServiceUtil.sendPassword(
			PortalUtil.getCompanyId(req), emailAddress, remoteAddr, remoteHost,
			userAgent);
		//logger.debug("sent password");
		SessionMessages.add(req, "request_processed", emailAddress);
	}

	/**
	 * Get connection to nds2 schema
	 * @return
	 * @throws Exception
	 */
	private static Connection getConnection() throws Exception{
        // Get a context for the JNDI look up
		if(datasource==null){
	        Context ctx = new InitialContext();
	        // Look up myDataSource
	        String name= PropsUtil.get("nds.datasource");
	        if(name==null)name= "java:/DataSource";
	        datasource = (DataSource) ctx.lookup (name);
		}
		return datasource.getConnection();
	}
	/**
	 * Check login ip satisfy iprule in "users.login_ip_rule", which is a regular expression
	 * @param ip
	 * @param login currently only support userid, not email
	 * @return Authenticator.FAILURE or Authenticator.SUCCESS
	 */
	private static int authenticateByLoginIPRule(String ip, String userId) throws Exception{
		//default to success
		int authResult = Authenticator.SUCCESS;
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
		    				authResult=Authenticator.FAILURE;
		    			}
	    			}catch(Exception e){
	    				logger.error("Could not parse ip rule:"+ipRule +" for ip:"+ ip+" of user:"+ userId+":"+e);
	    				authResult=Authenticator.FAILURE;
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