/******************************************************************
 *
 *$RCSfile: WebUtils.java,v $ $Revision: 1.4 $ $Author: Administrator $ $Date: 2006/06/24 00:32:46 $
 *
 *$Log: WebUtils.java,v $
 *Revision 1.4  2006/06/24 00:32:46  Administrator
 *no message
 *
 *Revision 1.3  2005/12/18 14:06:14  Administrator
 *no message
 *
 *Revision 1.2  2005/04/18 03:28:16  Administrator
 *no message
 *
 *Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
 *init 
 *
 *Revision 1.4  2004/02/02 10:42:37  yfzhu
 *<No Comment Entered>
 *
 *Revision 1.3  2003/05/29 19:40:07  yfzhu
 *<No Comment Entered>
 *
 *Revision 1.2  2003/03/30 08:11:33  yfzhu
 *Updated before subtotal added
 *
 *Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
 *Active POS
 *
 *Revision 1.3  2001/12/28 14:20:02  yfzhu
 *no message
 *
 *Revision 1.2  2002/01/04 01:43:22  yfzhu
 *no message
 *
 *Revision 1.1  2001/11/20 22:36:09  yfzhu
 *no message
 *
 *Revision 1.1  2001/11/16 11:42:40  yfzhu
 *no message
 *
 ********************************************************************/
package nds.control.web;

import java.beans.Beans;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import nds.security.Directory;
import nds.security.NDSSecurityException;
import nds.security.User;
import nds.util.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nds.control.event.NDSEventException;
import nds.query.*;
import nds.schema.*;
import nds.web.config.*;

public final class WebUtils {
	private static final Log logger = LogFactory.getLog(WebUtils.class);
	private final static String welcomeNewUser = "您好，请<a href='/login.jsp' class='newuser'>登录</a>，或<a href='/reg.jsp' class='newuser'>申请试用</a>";
	private static int count = 0;
	/**
	 * Store the value from #getComanyInfo
	 */
	private static String companyName = null;
	private static int systemMode = 0; // 1 for debug, 2 for production, 0 will
										// load from propery file

	private static ServletContext context = null;

	/**
	 * For accelerating locating adclient from domain name, make cache here Key:
	 * web_client.DOMAIN(String), value: [ad_client.id (Integer),
	 * ad_client.domain (String), AD_SITE_TEMPLATE.foldername (String)]
	 */
	private static Hashtable adClientDomainCache = new Hashtable();

	/**
	 * @param f
	 *            true for debug mode, false for production mode
	 */
	public static void setSystemDebugMode(boolean f) {
		systemMode = (f ? 1 : 2);
	}

	/**
	 * Unloading adclient from adClientDomainCache
	 * 
	 * @param adClientId
	 */
	public static void unloadAdClientId(Integer adClientId) {
		for (Iterator it = adClientDomainCache.keySet().iterator(); it
				.hasNext();) {
			Object key = it.next();
			Object[] value = (Object[]) adClientDomainCache.get(key);
			if (value[0].equals(adClientId)) {
				adClientDomainCache.remove(key);
				return;
			}
		}
	}

	/**
	 * get ad_client domain (burgeon) from web domain(www.burgeon.com.cn)
	 * 
	 * @param webDomain
	 * @return null if not found
	 */
	public static String getAdClientDomain(String webDomain) {
		Object[] dc = loadAdClient(webDomain);
		if (dc == null)
			return null;
		return (String) dc[1];
	}

	/**
	 * get ad_client id from domain
	 * 
	 * @param clientDomain
	 *            , web_client.DOMAIN, should be case insensitive
	 * @return -1 if not found
	 */
	public static int getAdClientId(String webDomain) {
		Object[] dc = loadAdClient(webDomain);
		if (dc == null)
			return -1;
		return ((Integer) dc[0]).intValue();
	}

	/**
	 * get AD_SITE_TEMPLATE.FOLDERNAME
	 * 
	 * @param clientDomain
	 *            , web_client.DOMAIN, should be case insensitive
	 * @return null if not found
	 */
	public static String getAdClientTemplateFolder(String webDomain) {
		Object[] dc = loadAdClient(webDomain);
		if (dc == null)
			return null; // default to 001
		return (String) dc[2];
	}

	/**
	 * 
	 * @param webDomain
	 *            web_client.domain
	 * @return null if not found, or [ad_client.id (Integer), ad_client.domain
	 *         (String)]
	 */
	private static Object[] loadAdClient(String webDomain) {
		if (webDomain == null)
			return null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String d = webDomain.toLowerCase();
		Object[] dc = (Object[]) adClientDomainCache.get(d);
		try {
			if (dc == null) {
				// loading from db
				conn = QueryEngine.getInstance().getConnection();
				pstmt = conn
						.prepareStatement("select c.id, c.domain, t.FOLDERNAME from web_client w, ad_client c, AD_SITE_TEMPLATE t where w.domain=? and c.id=w.ad_client_id and t.id=w.AD_SITE_TEMPLATE_ID");
				pstmt.setString(1, d);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					dc = new Object[] { new Integer(rs.getInt(1)),
							rs.getString(2), rs.getString(3) };
					adClientDomainCache.put(d, dc);
				} else {
					logger.debug("Not find client for " + webDomain);
				}
			}
		} catch (Throwable t) {
			logger.error("Fail to get ad_client.id from " + webDomain, t);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Throwable t2) {
			}
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Throwable t2) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Throwable t2) {
			}
		}
		return dc;
	}

	public static void setServletContext(ServletContext con) {
		WebUtils.context = con;
	}

	private static int serverRunInSingleCompanyMode = -1; // 0 for false, 1 for
															// true

	/**
	 * By default, NEA supports multiple companies mode, but when user
	 * authentication is based on email address, it will be single company mode
	 * 
	 * @return true if portal is using email address as the authentication mode
	 */
	public static boolean isServerRunInSingleCompanyMode() {
		if (serverRunInSingleCompanyMode != -1)
			return (serverRunInSingleCompanyMode == 1);
		try {
			ServletContext ctx = getServletContext();
			String companyId = (String) ctx
					.getAttribute(com.liferay.portal.util.WebKeys.COMPANY_ID);
			com.liferay.portal.model.Company company = com.liferay.portal.service.CompanyLocalServiceUtil
					.getCompany(companyId);

			serverRunInSingleCompanyMode = (company.getAuthType().equals(
					com.liferay.portal.model.impl.CompanyImpl.AUTH_TYPE_EA) ? 1
					: 0);
		} catch (Throwable t) {
			logger.error("Found error:" + t);
		}
		return (serverRunInSingleCompanyMode == 1);
	}

	public static ServletContext getServletContext() {
		return context;
	}

	public static ServletContextManager getServletContextManager() {
		ServletContextManager manager = null;
		if (context == null)
			throw new NDSRuntimeException(
					"Internal Error: ServletContext not set before calling getServletContextManager");
		/*
		 * if( context ==null){ //trying to get from JNDI tree Context
		 * initial=null; try{ initial= new InitialContext(); manager=
		 * (ServletContextManager)initial.lookup(JNDINames.CONTEXTMANAGER_WEB);
		 * }catch(NamingException e){
		 * debug("Error getting ServletContextManager from JNDI tree.");
		 * e.printStackTrace(); } }else
		 */
		// just get from context
		manager = (ServletContextManager) context
				.getAttribute(WebKeys.SERVLET_CONTEXT_MANAGER);
		try {
			if (manager == null) {
				// synchronized(context){
				// check again
				manager = (ServletContextManager) context
						.getAttribute(WebKeys.SERVLET_CONTEXT_MANAGER);
				if (manager == null) {
					manager = (ServletContextManager) Beans.instantiate(
							MainServlet.class.getClassLoader(),
							"nds.control.web.ServletContextManager");
					context.setAttribute(WebKeys.SERVLET_CONTEXT_MANAGER,
							manager);
					manager.init(context);
				}
				// }
			}
			return manager;
		} catch (Exception e) {
			e.printStackTrace();
			throw new NDSRuntimeException(
					"Error getting ServletContextManager", e);
		}
	}

	public static SessionContextManager getSessionContextManager(
			HttpSession session) {
		return getSessionContextManager(session, true);
	}

	/**
	 * 
	 * @param session
	 * @param create
	 *            if true, will create session manager if not exists
	 * @return
	 */
	public static SessionContextManager getSessionContextManager(
			HttpSession session, boolean create) {
		try {
			if (session == null) {
				System.out.println("Found session be null, and return null.");
				return null;
			}
			SessionContextManager manager = (SessionContextManager) session
					.getAttribute(WebKeys.SESSION_CONTEXT_MANAGER);
			if (manager == null && create) {
				// following is marked up, it's seldom when 2 threads work on
				// the same new session in the same time.
				// even when happens, remove first one does no great harm to
				// another one

				// synchronized(session){
				// manager=
				// (SessionContextManager)session.getAttribute(WebKeys.SESSION_CONTEXT_MANAGER);
				// if(manager==null){
				manager = (SessionContextManager) Beans.instantiate(
						MainServlet.class.getClassLoader(),
						"nds.control.web.SessionContextManager");
				session.removeAttribute(WebKeys.SESSION_CONTEXT_MANAGER);
				session.setAttribute(WebKeys.SESSION_CONTEXT_MANAGER, manager);
				manager.init(session);
				// }

				// }

				// load locale if guest user visit the web site
				Locale locale = (Locale) session
						.getAttribute(org.apache.struts.Globals.LOCALE_KEY);
				if (locale == null)
					session.setAttribute(org.apache.struts.Globals.LOCALE_KEY,
							TableManager.getInstance().getDefaultLocale());

			}
			return manager;
		} catch (Exception e) {
			e.printStackTrace();
			throw new NDSRuntimeException(
					"Error getting ServletContextManager", e);
		}
	}

	public static void checkDirectoryReadPermission(String dirName,
			HttpServletRequest request) throws NDSSecurityException {
		checkDirectoryPermission(dirName, Directory.READ, request);
	}

	public static void checkDirectoryWritePermission(String dirName,
			HttpServletRequest request) throws NDSSecurityException {
		checkDirectoryPermission(dirName, Directory.WRITE, request);
	}

	/**
	 * Check for whether has permission on querying specific table. We provide
	 * following rule on converting table name to directory name, which is used
	 * for assigning permissions:
	 * 
	 * if tableName tailed with "Item", the "Item" will be erased
	 * tableName+"_LIST" will be the directory to be checked.
	 * 
	 */
	public static void checkTableQueryPermission(String tableName,
			HttpServletRequest request) throws NDSSecurityException {
		String directory;
		directory = TableManager.getInstance().getTable(tableName)
				.getSecurityDirectory();

		WebUtils.checkDirectoryReadPermission(directory, request);
	}

	/**
	 * @param permission
	 *            can be one of Directory.READ or Directory.WRITE
	 */
	private static void checkDirectoryPermission(String dirName,
			int permission, HttpServletRequest request)
			throws NDSSecurityException {
		HttpSession session = request.getSession(true);
		/*
		 * //marked for guest visit if( session ==null || session.isNew()) throw
		 * new NDSSecurityException("必须登录系统才能访问受保护的对象");
		 */
		SessionContextManager manager = getSessionContextManager(session);
		UserWebImpl usr = (UserWebImpl) manager.getActor(WebKeys.USER);
		usr.checkPermission(dirName, permission);
	}

	public static boolean isDirectoryPermissionEnabled(String dirName,
			int permission, HttpServletRequest request)
			throws NDSSecurityException {
		HttpSession session = request.getSession(true);
		/*
		 * if( session ==null || session.isNew()) return false;
		 */
		SessionContextManager manager = getSessionContextManager(session);
		UserWebImpl usr = (UserWebImpl) manager.getActor(WebKeys.USER);
		return usr.isPermissionEnabled(dirName, permission);
	}

	public static int getDirectoryPermission(String dirName,
			HttpServletRequest request) throws NDSSecurityException {
		HttpSession session = request.getSession(true);
		/*
		 * if( session ==null || session.isNew()) return 0;
		 */
		SessionContextManager manager = getSessionContextManager(session);
		UserWebImpl usr = (UserWebImpl) manager.getActor(WebKeys.USER);
		return usr.getPermission(dirName);
	}

	/**
	 * Get table's uiconfig
	 * 
	 * @param Table
	 *            if null, will return ObjectUIConfig.getDefaultTableUIConfig()
	 * @return
	 */
	public static ObjectUIConfig getTableUIConfig(Table table) {
		if (table == null)
			return ObjectUIConfig.getDefaultTableUIConfig();

		PortletConfigManager pcManager = (PortletConfigManager) WebUtils
				.getServletContextManager().getActor(
						nds.util.WebKeys.PORTLETCONFIG_MANAGER);
		ObjectUIConfig uiConfig = (ObjectUIConfig) pcManager.getPortletConfig(
				table.getUIConfigId(),
				nds.web.config.PortletConfig.TYPE_OBJECT_UI);
		if (uiConfig == null)
			uiConfig = ObjectUIConfig.getDefaultTableUIConfig();
		return uiConfig;
	}

	/**
	 * Check user's permission on specified object
	 * 
	 * @param tableName
	 *            the table name of the object
	 * @param objectId
	 *            the pk id of the table
	 * @return nds.security.Directory.READ/WRITE/SUBMIT or their combination
	 * @throws Exception
	 */
	/*
	 * public static int getObjectPermission(String tableName, int objectId, int
	 * userId)throws Exception{ int perm=0;
	 * 
	 * return perm; }
	 */

	/**
	 * 将字符串转化成JavaScript能够用document.write()方法输出的文本 1. 将单引号(\')转化为双引号(\") 2.
	 * 将回车转换为\\n 3. 将换行转换为\\r 异常输出时使用，如：/header.jsp
	 * 
	 * @param str
	 * @return
	 */
	public static String stringForJsOutput(String str) {
		str = str.replace('\'', '\"');
		str = StringUtils.replace(str, "\n", "\\n");
		str = StringUtils.replace(str, "\r", "\\r");
		/*
		 * String tmp="",res=""; int i=0,j=0,k=0,l=0; k = str.indexOf("\n",i); l
		 * = str.indexOf("\r",i); j = getSmallNum(k,l); while(j >= i){ tmp =
		 * str.substring(i,j); i = j + 1; k = str.indexOf("\n",i); l =
		 * str.indexOf("\r",i); j = getSmallNum(k,l); res += tmp;
		 * if(tmp.length() > 0)res += (j==k)?"\\n":"\\r"; } j = getBigNum(k,l)
		 * == -1?str.length():getBigNum(k,l); if(i<str.length())res +=
		 * str.substring(i,j);
		 * 
		 * return res;
		 */
		return str;
	}

	/**
	 * 删除字符串中的WebKeys.MESSAGE值 在/objext/sheet_item.jsp中使用
	 * 
	 * @param msg
	 * @return
	 * @deprecated
	 */
	/*
	 * public static String removeValueholdMessage(String msg){ int index =
	 * msg.indexOf(WebKeys.MESSAGE); String res = null; if(index != -1){
	 * if(msg.charAt(index - 1) == '&') res = msg.substring(0,index - 1); else
	 * res = msg.substring(0,index); int end = msg.indexOf("&", index); if(end
	 * != -1){ res += msg.substring(end); } }else return msg; return res; }
	 */

	/**
	 * Get company information string for display. The company information will
	 * be set in configuration file, named using
	 * nds.util.WebKeys.NDS_PROPERTIES_FILE and the item is named "company.name"
	 * 
	 * @return string of the company name, if "company.name" not found in
	 *         nds.util.WebKeys.NDS_PROPERTIES_FILE, then default company name
	 *         "ACT" will be used.
	 */
	public static String getCompanyInfo() {
		// count for license test
		checkLicense();
		if (companyName != null)
			return companyName;
		Configurations conf = (Configurations) getServletContextManager()
				.getActor(WebKeys.CONFIGURATIONS);
		String name = "ACT";
		try {
			name = new String(conf.getProperty("company.name", "ACT").getBytes(
					"8859_1"), "GB2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		companyName = name;
		return name;
	}

	/**
	 * Convert throwable message to simple one. For instance, message from db
	 * contains ora- Will use MessagesHolder to do internal wildcard replacement
	 * should be parsed to readable one
	 * 
	 * @param t
	 * @return
	 */
	public static String getExceptionMessage(Throwable t, Locale locale) {
		String s = nds.util.StringUtils.getRootCause(t).getMessage();
		if (s == null)
			return nds.util.MessagesHolder.getInstance().translateMessage(
					"@unknown-exception@", locale);
		// if(isSystemDebugMode()) return
		// nds.util.MessagesHolder.getInstance().translateMessage(s, locale);
		s = s.trim();
		int p = s.indexOf("ORA-");
		int q = s.indexOf("ORA-", p + 1);
		String r;
		if (p >= 0) {
			/* convert index message */
			if (s.indexOf("00001", p) == p + 4) { // ORA-00001
				// UNIQUE CONSTRAINT(INDEX_NAME) VIOLATED
				r = parseOracle001Error(s, p, locale);
			} else {
				if (q > 0)
					r = s.substring(p + 11, q - 1);
				else
					r = s.substring(p + 11);
			}
			r = s.substring(0, p) + r;
		} else
			r = s;
		return nds.util.MessagesHolder.getInstance()
				.translateMessage(r, locale);// +"("+
												// nds.log.LoggerManager.getNDC()+")";
	}

	/**
	 * ORA-00001 unique constraint (string.string) violated
	 * 
	 * @param s
	 * @param startIdx
	 * @return
	 */
	private static String parseOracle001Error(String s, int fromIndex,
			Locale locale) {
		int idxStart = s.indexOf("(", fromIndex);
		int idxEnd = s.indexOf(")", fromIndex);
		if (!(idxStart > 0 && idxEnd > idxStart))
			return s;
		String idxName = s.substring(idxStart + 1, idxEnd);
		int idxTableEnd = idxName.indexOf('.');
		TableManager manager = TableManager.getInstance();

		String tableName = null;
		Table table = null;

		String indexName = idxName.substring(idxTableEnd + 1);
		String errorMsg = "@unique-constraint-violated@";// +idxName+"";
		try {
			// skip ad_client_id column
			List al = nds.query.QueryEngine
					.getInstance()
					.doQueryList(
							"select table_name, column_name from USER_IND_COLUMNS where column_name<>'AD_CLIENT_ID' and index_name="
									+ QueryUtils.TO_STRING(indexName));
			if (al.size() > 0) {
				StringBuffer sb = new StringBuffer(
						"@columns-that-violate-unique-constraint@:");
				for (int i = 0; i < al.size(); i++) {
					if (i > 0)
						sb.append("+");
					String tname = (String) ((List) al.get(i)).get(0);
					if (table == null) {
						table = manager.getTable(tname);
						tableName = tname;
					}
					String cname = (String) ((List) al.get(i)).get(1);
					Column col = manager.getColumn(tname, cname);
					if (col != null)
						cname = col.getDescription(locale);
					else
						cname = tname + "." + cname;
					sb.append(cname);
				}
				// if(table!=null)sb.append(",@in-table@:").append(
				// table.getDescription(locale));
				// else
				// sb.append("(@unqiue-index-name@:").append(indexName).append(")");
				errorMsg = sb.toString();
			}
		} catch (Throwable t) {
			logger.error("Faile to parse 0001 error", t);

		}
		return errorMsg;
	}

	/**
	 * If in debug mode, the sytem application dictionary can be modified, and
	 * page will show debug information for easy developing
	 * 
	 * @return true if property file has "schema.mode" set to "develope"
	 */
	public static boolean isSystemDebugMode() {
		if (systemMode == 0) {
			// load from properties
			try {
				Configurations conf = (Configurations) getServletContextManager()
						.getActor(WebKeys.CONFIGURATIONS);
				String mode = conf.getProperty("schema.mode", "production");
				if ("develope".equals(mode))
					systemMode = 1;
				else
					systemMode = 2;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return systemMode == 1;
	}

	private static void checkLicense() {
		if (true)
			return;
		if (count++ < 1)
			return;
		count = 0;
		try {
			java.sql.ResultSet rs = nds.query.QueryEngine
					.getInstance()
					.doQuery(
							"select value from appsetting where name='licd' and module='sys'");
			if (rs.next() == true) {
				String d = rs.getString(1);
				if (System.currentTimeMillis() - (new Long(d)).longValue() > 1000
						* 60 * 60 * 24 * 14) {
					System.out
							.print("\n\n\n\n########### NDS License expired ########## \n\n");
					System.exit(1);
				}
			} else {
				System.out
						.print("\n\n\n\n########### NDS License error ########## \n\n");
				System.exit(98);
			}
		} catch (Exception e) {
			System.out
					.print("\n\n\n\n########### NDS License error ########## \n\n");
			System.exit(99);

		}
	}

	public static String getMainTableLink(HttpServletRequest request) {
		int mid = ParamUtils.getIntAttributeOrParameter(request,
				"mainobjecttableid", -1);
		if (mid != -1)
			return "mainobjecttableid=" + mid + "&";
		return "";
	}

	/**
	 * Add to attribute from parameter
	 * 
	 * @param request
	 */
	public static void setMainTableLink(HttpServletRequest request) {
		if (Tools.getInt(request.getAttribute("mainobjecttableid"), -1) == -1) {
			request.setAttribute(
					"mainobjecttableid",
					""
							+ Tools.getInt(
									request.getParameter("mainobjecttableid"),
									-1));
		}
	}

	/**
	 * Make current user with the session valid for user
	 * 
	 * @param user
	 */
	public static void loginSSOUser(User user, HttpServletRequest request,
			HttpServletResponse res) throws Exception {
		com.liferay.portal.action.LoginAction.loginSSOUser(request, res,
				user.email);
	}

	public static String getUserWelcomeString(HttpServletRequest request) {
		nds.control.web.UserWebImpl userWeb = null;
		try {
			SessionContextManager scm = getSessionContextManager(
					request.getSession(), false);
			if (scm != null)
				userWeb = ((nds.control.web.UserWebImpl) scm
						.getActor(nds.util.WebKeys.USER));
		} catch (Throwable userWebException) {
		}
		if (userWeb == null || userWeb.isGuest()) {
			return welcomeNewUser;
		} else {
			return "您好，" + userWeb.getUserDescription()
					+ "，<a href='/c/portal/logout' class='reguser'>退出系统</a>";
		}
	}

	/**
	 * Parse reference column setting in wildcard filter
	 * 
	 * @param searchOnColumn
	 *            the column which may contains wildcard filter
	 * @param request
	 *            ,param wfc_<columnid> - when column isFilteredByWildcard,
	 *            values of those reference columns can be fetched here
	 * @return null if not found
	 */
	public static Expression parseWildcardFilter(Column searchOnColumn,
			javax.servlet.http.HttpServletRequest request, UserWebImpl userWeb)
			throws Exception {
		if (searchOnColumn == null || !searchOnColumn.isFilteredByWildcard())
			return null;
		Locale locale = userWeb.getLocale();

		String filter = searchOnColumn.getFilter();
		List wfc_rcs = searchOnColumn.getReferenceColumnsInWildcardFilter();
		Column wfc_rc;
		String wfc_rcv;
		for (int i = 0; i < wfc_rcs.size(); i++) {
			wfc_rc = (Column) wfc_rcs.get(i);
			wfc_rcv = request.getParameter("wfc_" + wfc_rc.getId());
			if (wfc_rcv != null) {
				// input value is ak, not id
				if (wfc_rc.getReferenceTable() != null) {
					wfc_rcv = parseReferenceColumnValue(wfc_rcv, wfc_rc,
							userWeb);
				} else {
					/*
					 * the string will replace into wildcard filter directly, so
					 * when rc is numeric type , return xxx, when string, return
					 * 'xxx', others not supported yet
					 */
					if (wfc_rc.getType() == Column.STRING) {
						wfc_rcv = QueryUtils.TO_STRING(wfc_rcv);
					}
				}
			} else {
				if (!wfc_rc.isNullable()) {
					String msg = MessagesHolder.getInstance().getMessage(
							locale, "pls-input");
					throw new NDSException(msg.replace("0",
							wfc_rc.getDescription(locale)));
				}
				wfc_rcv = "NULL";// replaced with null
			}
			filter = filter.replaceAll("@" + wfc_rc.getTable().getName() + "."
					+ wfc_rc.getName() + "@", wfc_rcv);
		}
		Expression expr = new Expression(null, filter,
				searchOnColumn.getDescription(locale)
						+ MessagesHolder.getInstance().getMessage(locale,
								"-have-special-filter"));
		return expr;
	}

	/**
	 * Parse reference column value in wildcard filter
	 * 
	 * @param value
	 * @param rc
	 *            reference column, which should has getReferenceTable()!=null
	 * @return id of
	 * @throws Exception
	 */
	private static String parseReferenceColumnValue(String objectStr,
			Column col, UserWebImpl userWeb) throws Exception {
		String ret;
		TableManager tm = TableManager.getInstance();
		QueryEngine engine = QueryEngine.getInstance();

		Table refTable = col.getReferenceTable();

		String refTableName = refTable.getName();
		String refTablleDesc = refTable.getDescription(Locale.CHINA);

		Column akColumn = refTable.getAlternateKey();
		String akNo = akColumn.getName();
		Column akCol = refTable.getAlternateKey();
		String colName = col.getName();
		// int lastIndex = -100;
		String newStr = null;

		boolean isAliasSupportTable = (refTable instanceof AliasSupportTable);
		PreparedStatement aliasPstmt = null;
		PairTable assocColumns = null;

		int refTableId = tm.getTable(refTableName).getId();
		String filterSql = null;
		// refTableName must not be real table, but column filter may use the
		// alias table as reference
		StringBuffer sqlStr = new StringBuffer("SELECT ID FROM "
				+ (refTable.getRealTableName()) + " " + refTable.getName()
				+ " WHERE "
				+ (refTable.isAcitveFilterEnabled() ? "ISACTIVE='Y' AND " : "")
				+ akNo + " = ?");

		// add refTable filter
		if (refTable.getFilter() != null) {
			sqlStr.append(" AND " + refTable.getFilter());
		}

		// add ad_client_id control, see Table.isClientIsolated for detail
		if (refTable.isAdClientIsolated()) {
			int ad_client_id = userWeb.getAdClientId();
			sqlStr.append(" and AD_CLIENT_ID=" + ad_client_id);
		}

		if (col.getFilter() != null) {
			if (col.isFilteredByWildcard()) {
				logger.warn("when getting id of "
						+ col
						+ " with ak="
						+ objectStr
						+ ", found col is filtered by wildcard, omit this filter since ui should controled");
				// yfzhu 2010-3-4 not add additional check here, ui should
				// forbid wildcard filter columns(fk one) from input
				// throw new
				// NDSException("Internal error, not support wildcard filter for "+
				// col);
			} else
				sqlStr.append(" AND (" + col.getFilter() + ")");
		}
		java.sql.Connection conn = QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		boolean isUpperCase = akCol.isUpperCase();
		BigDecimal tmpValue;
		try {
			logger.debug("object obtain sql= " + sqlStr);
			pstmt = conn.prepareStatement(sqlStr.toString());
			/*
			 * if(isAliasSupportTable){ Table
			 * aliasTable=tm.getTable(((AliasSupportTable
			 * )refTable).getAliasTable());// m_product_alias String
			 * pkAssocColumn=
			 * ((AliasSupportTable)refTable).getAssociatedColumnInAliasTable();
			 * // m_product_id, this is in m_product_alias
			 * assocColumns=((AliasSupportTable
			 * )refTable).getOtherAssociatedColumnsInAliasTable();//
			 * m_attributesetinstance_id
			 * 
			 * String
			 * aliasTableFilterSql=prepareSQLForAliasTableQuery((AliasSupportTable
			 * )refTable,helper, event); logger.debug("alias sql= "+
			 * aliasTableFilterSql);
			 * 
			 * aliasPstmt=conn.prepareStatement(aliasTableFilterSql);
			 * 
			 * }
			 */
			pstmt.setString(1, isUpperCase ? objectStr.toUpperCase()
					: objectStr);
			result = pstmt.executeQuery();
			if (result.next()) {
				ret = String.valueOf(result.getInt(1));
			} else {
				/*
				 * tmpValue =null; if(isAliasSupportTable){ tmpValue =
				 * findInAliasTable(objectStr[i], col, i, event,
				 * aliasPstmt,assocColumns,eventValueName); }
				 * if(tmpValue!=null){ resultInt[i] =tmpValue; }else{ throw new
				 * NDSEventException
				 * ("@line@ "+(i+1)+": "+objectStr[i]+"("+refTablleDesc
				 * +")@not-exists-or-invalid@"); }
				 */
				ret = "NULL";
			}
		} finally {
			if (result != null)
				try {
					result.close();
				} catch (Exception e3) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (Exception e3) {
				}
			if (aliasPstmt != null)
				try {
					aliasPstmt.close();
				} catch (Exception e3) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Exception e3) {
				}
		}
		return ret;
	}

	/**
	 * remove those columns that be hidden according to specified display
	 * condition
	 * 
	 * @param contains
	 *            current record information, such as for object modify,
	 *            addAllShowableColumnsToSelection(Column.MODIFY)
	 * @param columns
	 *            will remove hidden columns from it
	 * @param displayConditions
	 *            just the result
	 * @return columns being removed from <param>columns</param>, or null if no
	 *         one found.
	 * @deprecated not used yet
	 * 
	 */
	public static List<Column> filterHiddenColumns(QueryResult result,
			List columns, JSONArray displayConditions) throws Exception {
		if (result == null || displayConditions == null
				|| displayConditions.length() == 0)
			return null;
		ArrayList cols = new ArrayList();
		Table table = result.getQueryRequest().getMainTable();
		boolean b;
		for (int i = columns.size() - 1; i >= 0; i--) {
			Column col = (Column) columns.get(i);
			if (col.getJSONProps() != null) {
				int hc = col.getJSONProps().optInt("hide_condition", -1);
				if (hc > 0 && hc < displayConditions.length()) {
					Object o = displayConditions.get(hc);
					if (o instanceof JSONObject) {
						String c = ((JSONObject) o).getString("c");
						String v = ((JSONObject) o).getString("v");
						Column cl = table.getColumn(c);
						if (cl == null)
							throw new NDSException(
									"error in hide_condition of " + col + ": "
											+ c + " not find in table " + table);
						int pos = result.getMetaData().findPositionInSelection(
								cl);// starts from 0
						if (pos == -1)
							throw new NDSException(
									"error in hide_condition of "
											+ col
											+ ": "
											+ c
											+ " not find in object query selection list ");

						Object v2 = result.getObject(pos + 1);
						String value;
						boolean valueIsNull = (v2 == null || Validator
								.isNull(String.valueOf(v2)));
						if (valueIsNull)
							value = "null";
						else
							value = String.valueOf(v2);

						String[] vs = v.split(",");
						b = false;
						for (int j = 0; j < vs.length; j++) {
							boolean reverse = vs[j].startsWith("!");
							if (reverse)
								vs[j] = vs[j].substring(1);
							b = (vs[j].equals(value) && !reverse)
									|| (!vs[j].equals(value) && reverse);
						}
					} else {
						b = displayConditions.getBoolean(hc);
					}
					if (b) {
						// hide then
						cols.add(col);
						columns.remove(i);
					}
				}
			}
		}
		return cols;
	}

	/**
	 * Load ad_table.props accroding to record info, props are reconstructed
	 * 
	 * @return null or valid one
	 */
	public static JSONObject loadObjectPropsForClient(Table table,
			int objectId, QuerySession qsession) throws Exception {
		JSONObject jo = table.getJSONProps();
		// jo.optJSONObject(key)
		// jo.getInt(key)
		if (jo == null)
			return null;
		boolean wb = jo.has("bfclose_ac");
		JSONArray dc = jo.optJSONArray("display_condition");
		if (dc == null && wb==false)
			return null;
		JSONObject ro = new JSONObject();
		JSONArray ja = new JSONArray();
		if (dc != null) {
			for (int i = 0; i < dc.length(); i++) {
				Object io = dc.get(i);
				if (io instanceof String) {
					// sql
					String sql = QueryUtils.replaceVariables((String) io,
							qsession);
					JSONObject vb = new JSONObject();
					vb.put("OBJECTID", String.valueOf(objectId));
					vb.put("TABLEID", String.valueOf(table.getId()));
					vb.put("TABLE", String.valueOf(table.getName()));
					sql = JSONUtils.replaceVariables(sql, vb);
					int cnt = Tools.getInt(QueryEngine.getInstance()
							.doQueryOne(sql), 0);
					ja.put((cnt > 0));
				} else if (io instanceof JSONObject) {
					// limit value group, contains "c" for column and "v" for
					// value
					String c = ((JSONObject) io).getString("c");
					String v = ((JSONObject) io).getString("v");
					Column col = table.getColumn(c);
					if (col == null)
						throw new NDSException(
								"c is not a valid column on table " + table
										+ ":" + c);
					JSONObject jt = new JSONObject();
					jt.put("c", col.getId());
					jt.put("v", v);
					ja.put(jt);
				} else {
					// not supported
					throw new NDSException("Not supported type:"
							+ io.getClass() + " as " + io + " in " + jo);
				}
			}
			ro.put("display_condition", ja);
		}
		if (wb) {
			ro.put("bfclose_ac", jo.getInt("bfclose_ac"));
		}
		return ro;
	}

	/**
	 * 
	 * @param req
	 * @return 0 - ie, 1 - ff, 2 - others,3-Chrome
	 */
	public static int getBrowserType(HttpServletRequest req) {
		String s = req.getHeader("User-Agent");
		if (s == null)
			return 2;
		if (s.indexOf("MSIE") > -1)
			return 0;
		else if (s.indexOf("Firefox") > -1)
			return 1;
		else if (s.indexOf("Chrome") > -1 )
			return 3;
		else if(regex("Opera", s)) 
			return 4;
		return 2;
	}
	
    public static boolean regex(String regex,String str){  
        Pattern p =Pattern.compile(regex,Pattern.MULTILINE);  
        Matcher m=p.matcher(str);  
        return m.find();  
    } 

	/**
	 * Will guess client browser type from request header ("user-agent"), for ie
	 * and for ff is different 其实按照RFC2231的定义，多语言编码的Content-Disposition应该这么定义：
	 * 
	 * Content-Disposition: attachment;
	 * filename*="utf8''%E4%B8%AD%E6%96%87%20%E6%96%87%E4%BB%B6%E5%90%8D.txt"
	 * 
	 * 即：
	 * 
	 * filename后面的等号之前要加 * filename的值用单引号分成三段，分别是字符集(utf8)、语言(空)和urlencode过的文件名。
	 * 最好加上双引号，否则文件名中空格后面的部分在Firefox中显示不出来
	 * 注意urlencode的结果与php的urlencode函数结果不太相同，php的urlencode会把空格替换成+，而这里需要替换成%20 *
	 * 
	 * @param filename
	 * @param req
	 * @return format like "filename=%d02.xls"
	 */
	public static String getContentDispositionFileName(String filename,
			HttpServletRequest req) throws java.io.UnsupportedEncodingException {
		int bt = WebUtils.getBrowserType(req);
		logger.debug(bt);
		if (bt == 0) {
			// ie
			return "filename=\""
					+ StringUtils.replace(
							java.net.URLEncoder.encode(filename, "UTF-8"), "+",
							"%20") + "\"";
		}else if(bt==3){
			//chrome
			 return "filename=\"" + MimeUtility.encodeText(filename, "UTF8", "B") + "\"";  
		}else if(bt==4){
			 return "filename*=UTF-8''" + java.net.URLEncoder.encode(filename, "UTF8");  
		}else{
			// ff
			return "filename*=\"utf8''"
					+ StringUtils.replace(
							java.net.URLEncoder.encode(filename, "UTF-8"), "+",
							"%20") + "\"";
		}
	}

	public static Configurations getConfigurations() {
		Configurations conf = (Configurations) getServletContextManager()
				.getActor(WebKeys.CONFIGURATIONS);
		return conf;

	}

	public static String getProperty(String name, String defaultValue) {
		return getConfigurations().getProperty(name, defaultValue);
	}

	public static String getProperty(String name) {
		return getConfigurations().getProperty(name);
	}
}
