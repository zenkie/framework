/**
 * Support user inactive, add support for User Login IP checking
 */

package com.liferay.portal.action;

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
import java.util.Iterator;
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
import nds.schema.TableManager;
import nds.util.Configurations;
import nds.util.License;
import nds.util.LicenseManager;
import nds.util.LicenseWrapper;
import nds.util.SysLogger;
import nds.util.Tools;
import nds.portal.auth.*;
import nds.query.QueryEngine;
import nds.query.QueryUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.security.*;
import java.text.SimpleDateFormat;

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
		 * add server url
		 */
		String remoteAddress=req.getRemoteAddr();
		String serverUrl="";//= Tools.toString(req);
		Enumeration  enu=req.getHeaders("Origin"); 
		if(enu.hasMoreElements()){ 
			serverUrl=(String)enu.nextElement(); 
			   if(nds.control.web.WebUtils.getServerUrl()==null)nds.control.web.WebUtils.setServerUrl(serverUrl);
		}
		//System.out.print("serverUrl "+serverUrl);
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
		//req.getSession().setAttribute("SERVERURL", serverUrl);
     
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
		    		// so server memory space saved. used later
		    		//ses.removeAttribute("nds.control.web.ValidateMServlet");
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
		    		logger.debug("checkInactiveUser");
		    		return mapping.findForward("portal.login");
		    	}
		    	// USBKEY的校验
		    	if(!checkUSBKey(req)){
		    		logger.debug("checkUSBKey");
		    		return mapping.findForward("portal.login");
		    	}
		    	// 检查mac keyfile是否存在或有效
		    	if(!check_mac(req)){
		    		logger.debug("check_mac");
		    		return mapping.findForward("portal.vaildkey");
		    	}
		    	login(req, res);
		    	String limtnum=check_num();
				logger.debug("redirect is "+limtnum);
				if(limtnum!=null){
	        	Thread t=new Thread(new Runnable(){
	        		public void run(){
	        			try{
	        				Thread.sleep(60*60*1000);
	        				logger.error("users or pos limit number!, will exit.");
	        				System.exit(1099);
	        			}catch(Throwable e){
	        			}
	        		}
	        	});
	        	t.start();
	        	//limtnum=java.net.URLEncoder.encode(limtnum,"UTF-8");
	        	//logger.debug("redirect is "+limtnum);
	        	res.sendRedirect(limtnum);
	        	return null;
				}
				
				if (GetterUtil.getBoolean(
						PropsUtil.get(PropsUtil.PORTAL_JAAS_ENABLE))) {
					logger.debug("ccc");
					return mapping.findForward("/portal/touch_protected.jsp");
				}
				else {
					/**
					 * Yfzhu marked up following to direct to /home/nds/portal/index.jsp directly 
					 */
					//res.sendRedirect(themeDisplay.getPathMain());
					//return null;
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
			int rowCount = 0;
			if(rs.next()){
				isactive=rs.getString(1);
				rowCount++;
			if(!"Y".equals(isactive)) {
				SessionErrors.add(req, "INACTIVE_USER");
				return false;
			}
			}else if(rowCount==0){
				SessionErrors.add(req, NoSuchUserException.class.getName());
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
	private int usbkeyMethod=1;// 1: ahyy, 2: feitian
	/**
	 * Will set erroro to SessionErrors if found error
	 * @param req
	 * @return true if ok, false if error found
	 */
	private boolean checkUSBKey(HttpServletRequest req){
		if(shouldCheckUSBKey==0){
			nds.util.Configurations conf=(nds.util.Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			shouldCheckUSBKey = "true".equalsIgnoreCase(conf.getProperty("login.usbcheck", "false"))?1:2;
			String method=conf.getProperty("login.auth.method", "ahyy");
			if("feitian".equals(method)) usbkeyMethod=2;
			else if("ahyy".equals(method)) usbkeyMethod=1;
			else throw new nds.util.NDSRuntimeException("unsupported login.auth.method="+method);
		}
		if(shouldCheckUSBKey==1){
			//这里有两种实现：安徽药招模式和飞天诚信模式
			
			switch(usbkeyMethod){
			case 2:
				return authFeitianMethod(req);
			case 1:
				return authAhyyMethod(req);
			default:
				throw new nds.util.NDSRuntimeException("unsupported login.auth.method="+usbkeyMethod);
			}
			
		}
		return true;
	}
/**
 * * 飞天诚信模式系统安全配置内容如下：
login.usbcheck=true
login.auth.method=feitian
login.auth.except.addr=$WAN_ADDR$


login.usbcheck=true 时将校验client.auth.method的内容，识别为feitian时有后续规则校验。
WAN_ADDR是不必验证USBKEY的地址，如内网地址 192.168.1.100，用户使用此域名访问时，不必进行USBKEY认证。

实现方法：
修改LOGIN.JSP/INDEX.JSP，如果当前登录网站匹配$WAN_ADDR$，不检测USBKEY。
若不匹配，则检测USBKEY存在性。
若USBKEY存在，则根据用户输入的验证码(4位随机码)作为需要挑战码，用USBKEY的密钥进行加密（此密钥也保存在服务器USERS.EMAILVERIFY中），将加密结果作为keycode传到服务器。服务器从USERS.EMAILVERIFY获取密钥，从session中通过"nds.control.web.ValidateMServlet" 属性获取验证码，用密钥对验证码进行HMAC_MD5加密，与客户端传入的 keycode 比对，如果成功，表示USBKEY身份认证通过，继续后续的密码认证。否则失败退出。

如果客户端网址匹配$WAN_ADDR$，将不检测USBKEY
 * @param req
 * @return
 */
	private boolean authFeitianMethod(HttpServletRequest req){
		nds.util.Configurations conf=(nds.util.Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		String exceptAddr=conf.getProperty("login.auth.except.addr");
		String remoteAddress=req.getRemoteAddr();
		
		String login= req.getParameter("login");
		String keycode= req.getParameter("keycode");
		String serverValidCode=(String) req.getSession().getAttribute("nds.control.web.ValidateMServlet");
		
		String usrKey= null;
		Connection conn=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			java.net.URL url = new java.net.URL(req.getRequestURL().toString());
			boolean isIntranet=url.getHost().equals(exceptAddr);
			if(isIntranet) return true;

			conn= nds.query.QueryEngine.getInstance().getConnection();
			if(nds.schema.TableManager.getInstance().getColumn("C_STORE", "USBKEY")!=null){
				pstmt= conn.prepareStatement("select nvl(u.emailverify, c.usbkey) from users u, c_store c where u.email=? and c.id(+)= u.c_store_id");
			}else
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

				HMAC_MD5 hm = new HMAC_MD5(usrKey.getBytes());
				hm.addData(serverValidCode.getBytes());
				
				hm.sign();
				
				String realKeycode= hm.toString();
				if(!realKeycode.equals(keycode)){
					logger.debug("realKeycode="+realKeycode+", keycode="+keycode+", serverValidCode="+serverValidCode+",usrKey="+usrKey);
					SessionErrors.add(req, "USBKEY_ERROR");
					SysLogger.getInstance().error("SEC","login",login,remoteAddress,"usbcode error in browser" ,-1);
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
		return true;
	}
	private boolean authAhyyMethod(HttpServletRequest req){

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

	/**
	 * return check usenum posnum
	 * @param res 
	 * @param req 
	 */
	private String check_num(){
	    int user_num=0;
	    int pos_num=0;
	    int  cut_usr=0;
	    int  cut_pos=0;
	    String redirect=null;
	    Connection conn = null; 
	    String company=null;
	    String expdate=null;
	    boolean isexp=false;
	  	try{
	  	// logger.debug("upload keyfile is"+mac);
	    Iterator b=LicenseManager.getLicenses();

	    while (b.hasNext()) {
	    	LicenseWrapper o = (LicenseWrapper)b.next();
	    	user_num=o.getNumUsers();
	    	pos_num=o.getNumPOS();
	    	company=o.getName();
	    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    	expdate = df.format(o.getExpiresDate());
	    	isexp=o.getExpdate();
	    }
		conn= nds.query.QueryEngine.getInstance().getConnection();
		QueryEngine engine=QueryEngine.getInstance();
		cut_usr=Tools.getInt(engine.doQueryOne("select count(*) from users t where t.isactive='Y' and t.IS_SYS_USER!='Y'", conn), -1);
		cut_pos=Tools.getInt(engine.doQueryOne("select count(*) from c_store t where t.isactive='Y' and t.isretail='Y'", conn), -1);
		logger.debug("cut_usr is "+cut_usr);
		logger.debug("cut_pos is "+cut_pos);
		if(isexp){
			 redirect="/html/prg/expFail.jsp?exp="+expdate;
	    }else if(cut_usr>user_num||cut_pos>pos_num){
			redirect="/html/prg/cutinfo.jsp?cp="+java.net.URLEncoder.encode(company,"UTF-8")+"&cu="+cut_usr+"&cs="+cut_pos+"&un="+user_num+"&pn="+pos_num+"&exp="+expdate;
		} 
		logger.debug("redirect is"+redirect);
		try{if(conn!=null) conn.close();}catch(Throwable t){}
	   } catch (Exception e) {
		   logger.debug("check mackey invaild",e);
	    }finally{
	    	try{if(conn!=null) conn.close();}catch(Throwable t){}
		}
		return redirect;
	}

	

	/**
	 * return check_mac address
	 * @param req 
	 * 
	 */
	private boolean check_mac(HttpServletRequest req){

//		Connection conn = null; 
//		Object sc=null;
		String mac =null;
//		ResultSet rs = null;
//		PreparedStatement pstmt = null;
		//if mac pass exists
		//get mac file to check it
		try{
//			conn= nds.query.QueryEngine.getInstance().getConnection();
//			pstmt= conn.prepareStatement("select mac from users where id=?");
//			pstmt.setInt(1, 893);
//			rs= pstmt.executeQuery();
//			if(rs.next()){
//				sc=rs.getObject(1);
//				if(sc instanceof java.sql.Clob) {
//					mac=((java.sql.Clob)sc).getSubString(1, (int) ((java.sql.Clob)sc).length());
//	        	}else{
//	        		mac=(String)sc;
//	        	}	
//			}
			Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			String licfile=conf.getProperty("license",null);
			if(licfile!=null){
				mac= nds.util.Tools.readFile(licfile);
			}
			logger.debug("keyfile :"+mac);
			if(mac==null){
				SessionErrors.add(req, "VERIFY_KEYFILE_ERROR");
				return false;
				}
//			try{if(conn!=null) conn.close();}catch(Throwable t){}
		}catch(Throwable t){
			return false;
		}finally{
//			try{if(rs!=null) rs.close();}catch(Throwable t){}
//			try{if(pstmt!=null) pstmt.close();}catch(Throwable t){}
//			try{if(conn!=null) conn.close();}catch(Throwable t){}
		}	
		//else
		nds.util.Configurations conf=(nds.util.Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		try{
			LicenseManager.validateLicense("jackrain","5.0", mac);
			/*
		    Iterator b=LicenseManager.getLicenses();
		    while (b.hasNext()) {
		    LicenseWrapper o = (LicenseWrapper)b.next();
	    	logger.debug("ltype :"+o.getLicenseType().toString());
	   
	    	if(o.getLicenseType()==License.LicenseType.COMMERCIAL){
				//logger.error("The license should contain machine code.");
				//return;
				//if(license.getExpiresDate().getTime() - license.getCreationDate().getTime() > 1L * 100 * 24 * 3600* 1000  ){
					if(System.currentTimeMillis() >o.getExpiresDate().getTime()  ){
					//logger.error("Non-Commercial license valid duration should not be greater than 100 days");
					logger.error("your company service day is old!!!!!!!");
					o.setMms(LicenseManager.sendmss(o.getName(),o.getExpiresDate()));
					//nds.control.web.WebUtils.setMms(o.getMms());
				}
		    }else if(o.getLicenseType()==License.LicenseType.EVALUATION){
				// Evaluation should not have valid duration over 31 days
				logger.debug("Evaluation should not have valid duration over");
				//if(license.getExpiresDate().getTime() - license.getCreationDate().getTime() > 1L * 31 * 24 * 3600* 1000  ){
				if(System.currentTimeMillis() >o.getExpiresDate().getTime()  ){
					logger.error("Evaluation license valid duration should not be greater than 30 days");
					o.setExpdate(true);
			    	//nds.control.web.WebUtils.setLtype(o.getLicenseType());
					//return;
				}
		    }
		    }
		    */
			return true;
		} catch (Exception e) {
			logger.debug("check mackey invaild",e);
			SessionErrors.add(req, "VERIFY_KEY_ERROR");
			return false;
		}
	}
}
class HMAC_MD5
{
	
	/**
	* Digest to be returned upon completion of the HMAC_MD5.
	*/
	private byte digest[];

	/**
	* Inner Padding.
	*/
	private byte kIpad[];

	/**
	* Outer Padding.
	*/
	private byte kOpad[];

	/**
	* Outer and general purpose MD5 object.
	*/
	private MessageDigest md5;
	/**
	* Inner MD5 object.
	*/
	private MessageDigest innerMD5;

	/**
	* Constructor
	* @throws NoSuchAlgorithmException if the MD5 implementation can't be found.
	* If this occurs or you need a faster implementation see www.bouncycastle.org
	* for something much better.
	*/
	public HMAC_MD5(byte key[]) throws NoSuchAlgorithmException
	{
		md5 = MessageDigest.getInstance("MD5");
		innerMD5 = MessageDigest.getInstance("MD5");
		int kLen = key.length;

		// if key is longer than 64 bytes reset it to key=MD5(key)
		if (kLen > 64)
		{
			md5.update(key);
			key = md5.digest();
		}

		kIpad = new byte[64];	// inner padding - key XORd with ipad

		kOpad = new byte[64];	// outer padding - key XORd with opad

		// start out by storing key in pads
		System.arraycopy(key, 0, kIpad, 0, kLen);
		System.arraycopy(key, 0, kOpad, 0, kLen);

		// XOR key with ipad and opad values
		for (int i = 0; i < 64; i++)
		{
			kIpad[i] ^= 0x36;
			kOpad[i] ^= 0x5c;
		}

		clear();	// Initialize the first digest.
	}


	/**
	* Clear the HMAC_MD5 object.
	*/
	public void clear()
	{
		innerMD5.reset();
		innerMD5.update(kIpad);	// Intialize the inner pad.

		digest = null;				// mark the digest as incomplete.
	}

	/**
	* HMAC_MD5 function.
	*
	* @param text Text to process
	*
	* @param key Key to use for HMAC hash.
	*
	* @return hash
	*/
	public void addData(byte text[])
	{
		addData(text, 0, text.length);
	}

	/**
	* HMAC_MD5 function.
	*
	* @param text Text to process
	*
	* @param textStart	Start position of text in text buffer.
	* @param textLen Length of text to use from text buffer.
	* @param key Key to use for HMAC hash.
	*
	* @return hash
	*/
	public void addData(byte text[], int textStart, int textLen)
	{
		innerMD5.update(text, textStart, textLen);	// then text of datagram.
	}

	public byte [] sign()
	{
		md5.reset();

		/*
		* the HMAC_MD5 transform looks like:
		*
		* MD5(K XOR opad, MD5(K XOR ipad, text))
		*
		* where K is an n byte key
		* ipad is the byte 0x36 repeated 64 times
		* opad is the byte 0x5c repeated 64 times
		* and text is the data being protected
		*/

		// Perform inner MD5

		digest = innerMD5.digest();				// finish up 1st pass.

		 // Perform outer MD5

		md5.reset();								// Init md5 for 2nd pass.
		md5.update(kOpad);							// Use outer pad.
		md5.update(digest);							// Use results of first pass.
		digest = md5.digest();						// Final result.

		return digest;
	}

	/**
	* Validate a signature against the current digest.
	* Compares the hash against the signature.
	*
	* @param signature
	*
	* @return True if the signature matches the calculated hash.
	*/
	public boolean verify(byte signature[])
	{
		// The digest may not have been calculated.  If it's null, force a calculation.
		if (digest == null)
			sign();

		int sigLen = signature.length;
		int digLen = digest.length;

		if (sigLen != digLen)
			return false;	// Different lengths, not a good sign.

		for (int i = 0; i < sigLen; i++)
			if (signature[i] != digest[i])
				return false;	// Mismatch. Misfortune.

		return true;	// Signatures matched. Perseverance furthers.
	}

	/**
	*  Return the digest as a HEX string.
	*
	* @return a hex representation of the MD5 digest.
	*/
	public String toString()
	{
		// If not already calculated, do so.
		if (digest == null)
			sign();

		StringBuffer r = new StringBuffer();
		final String hex = "0123456789ABCDEF";
		byte b[] = digest;

		for (int i = 0; i < 16; i++)
		{
			int c = ((b[i]) >>> 4) & 0xf;
			r.append(hex.charAt(c));
			c = ((int)b[i] & 0xf);
			r.append(hex.charAt(c));
		}

		return r.toString();
	}
	

	
}