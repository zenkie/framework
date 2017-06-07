/******************************************************************
*
*$RCSfile: WebKeys.java,v $ $Revision: 1.5 $ $Author: Administrator $ $Date: 2006/01/31 03:01:58 $
*
*$Log: WebKeys.java,v $
*Revision 1.5  2006/01/31 03:01:58  Administrator
*no message
*
*Revision 1.4  2006/01/07 11:47:34  Administrator
*no message
*
*Revision 1.3  2005/05/16 07:34:20  Administrator
*no message
*
*Revision 1.2  2005/03/30 13:13:59  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.3  2003/09/29 07:37:24  yfzhu
*before removing entity beans
*
*Revision 1.2  2002/12/17 05:54:24  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.1  2001/11/20 22:36:10  yfzhu
*no message
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.util;

/**
 * This interface contains all the keys that are used to
 * store data in the different scopes of web-tier. These
 * values are the same as those used in the JSP
 * pages (useBean tags).
 */
public class WebKeys {

    public static String getPrdname() {
		return PRDNAME;
	}
	public static final String LOGGER = "nds.web.logger";// ServletContext
    public static final String CONFIGURATIONS = "nds.web.configs";// ServletContext
    public static final String REQUEST_PROCESSOR = "nds.web.rps";// ServletContext
    public static final String FLOW_PROCESSOR = "nds.web.flows";// ServletContext
    public static final String URL_MANAGER="nds.web.urls";// ServletContext
    public static final String WEB_CONTROLLER = "nds.web.webController";// Session Context
    public static final String SM_PROCESSOR = "nds.web.smProcessor";// Session Context
    public static final String TABLE_MANAGER="nds.web.tableManager";// ServletContext
    public static final String SECURITY_MANAGER="nds.web.securityManager";// ServletContext
    public static final String ATTACHMENT_MANAGER="nds.web.AttachmentManager";// ServletContext
    public static final String JOB_MANAGER="nds.schedule.JobManager";// ServletContext
    public static final String SERVLET_CONTEXT_MANAGER = "nds.web.svletctxmgr";// Session Context
    public static final String SESSION_CONTEXT_MANAGER = "nds.web.sesonctxmgr";// Session Context
    public static final String  PORTLETCONFIG_MANAGER="nds.web.config.PortletConfigManager";// ServletContext
    public static final String  USER_WELCOME_MANAGER="nds.web.welcome.Manager";// ServletContext
    public static final String  PLUGIN_CONTROLLER="nds.io.plugincontroller";// ServletContext
    public static final String  LIC_MANAGER="nds.web.licmanager";// ServletContext
    
    // user information, is UserWebImpl
    public static final String USER = "nds.web.authorization";
    public static final String POSAUTOMATOR = "nds.web.posautomator";

    public static final String MODEL_MANAGER = "nds.web.models";// Session Context
    /**
     * This request-scoped attribute uniquely determines the
     * order associated with a user request.
     */
    public static final String REQUEST_ID = "nds.web.requestId";

    /**
     * The value holder as an attribute in HTTPServletRquest
     */
    public static final String VALUE_HOLDER="nds.control.util.ValueHolder";
    
    public static final String DEFAULT_WEB_EVENT="nds.control.event.DefaultWebEvent";
    /**
     * The message from server-side as an attribute in HTTPServletRquest
     */
    public static final String VALUE_HOLDER_MESSAGE="nds.control.util.ValueHolder_message";
    public static final String VALUE_HOLDER_CODE="nds.control.util.ValueHolder_code";
    public static final String VALUE_HOLDER_PREFIX="nds.control.util.ValueHolder_";
    
    public static final String CACHE_MANAGER="nds.web.cache";
    /**
     * 配置文件的存放位置
     * 用来统一整个系统的配置
     * by Hawkins
     */
    public static final String NDS_PROPERTIES_FILE = "/aic/conf/nds.properties";

    public static final String HELP_CONTROL="nds.web.helpControl";// ServletContext

    //add SyncManager for Active POS
    public static final String SYNCMANAGER = "nds.web.syncmanager";
    
    /**
     * if /objext/sheet_title.jsp should be referred, using 
     * request.getContextPath()+NDS_URI+"/objext/sheet_title.jsp
     */
    //public static final String NDS_URI="";
    public static final String NDS_URI="/html/nds";
    public static final String WEB_CONTEXT_ROOT=""; // for servlet finding 
    //增加产品名称
    public static final String PRDNAME="bos20";

}
