/******************************************************************
*
*$RCSfile: ServletContextManager.java,v $ $Revision: 1.8 $ $Author: Administrator $ $Date: 2006/03/13 01:15:57 $
*
*$Log: ServletContextManager.java,v $
*Revision 1.8  2006/03/13 01:15:57  Administrator
*no message
*
*Revision 1.7  2006/01/31 02:59:20  Administrator
*no message
*
*Revision 1.6  2006/01/17 10:34:43  Administrator
*no message
*
*Revision 1.5  2006/01/07 11:45:53  Administrator
*no message
*
*Revision 1.4  2005/12/18 14:06:14  Administrator
*no message
*
*Revision 1.3  2005/05/16 07:34:12  Administrator
*no message
*
*Revision 1.2  2005/03/16 09:04:50  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.5  2004/02/02 10:42:37  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/09/29 07:37:21  yfzhu
*before removing entity beans
*
*Revision 1.3  2003/08/17 14:25:08  yfzhu
*before adv security
*
*Revision 1.2  2002/12/17 05:53:45  yfzhu
*no message
*
*Revision 1.4  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.3  2001/11/29 00:48:31  yfzhu
*no message
*
*Revision 1.2  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/

package nds.control.web;

import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.model.dao._RootDAO;
//import nds.olap.LimitValueFormatter;
import nds.schema.TableManager;
import nds.util.Configurations;
import nds.util.Director;
import nds.util.Manager;
import nds.util.MessagesHolder;
import nds.util.NativeTools;
import nds.util.ServletContextActor;
import nds.util.SysLogger;
import nds.util.Tools;
import nds.util.Validator;
import nds.util.WebKeys;

/**
Maintains application level information
*/
public class ServletContextManager implements ServletContextActor,java.io.Serializable {
    private Logger logger=LoggerManager.getInstance().getLogger(ServletContextManager.class.getName());
    private Manager manager;
    private  static boolean doLog=true;
    private void debug(String str) {
        if(doLog)
            System.out.println("[ServletContextManager] "+ str);
    }

    public ServletContextManager() {
        manager=new Manager();
    }
    private Object create(String name, ServletContext context) {
        Object actor= manager.create(name, this.getClass().getClassLoader());
        if( actor instanceof ServletContextActor) {
            ((ServletContextActor) actor).init(context);
        }
        return actor;
    }

    public void init(Director dir) {}
    /**
    @roseuid 3BF287F5009C
    */
    public Object getActor(String role) {
        return manager.getActor(role);
    }
    public void init(ServletContext context) {
        WebUtils.setServletContext(context);
        /*        Context initial=null;
                try{
                    initial= new InitialContext();
                }catch(NamingException e){
                }*/
        try {
            // conf
            Configurations conf=getConfigurations(context);
            manager.setRole(WebKeys.CONFIGURATIONS,conf);

            // hibernate session
            URL url=getClass().getResource("/nds.hibernate.xml");
            _RootDAO.initialize(url);
            logger.debug("Hibernate initialized using " + url);
            
            nds.query.QueryEngine.getInstance().init(conf.getProperties());
            Object actor;
            // support different db type
            actor= create("nds.db.DBController", context);

            // init logger
//            LoggerManager.getInstance().init(conf.getProperties(), true);
//            logger=LoggerManager.getInstance().getLogger(ServletContextManager.class.getName());
            // url mapping
            
            actor =create("nds.control.web.URLMappingManager", context);
            manager.setRole(WebKeys.URL_MANAGER, actor);
            // flow
            actor =create("nds.control.web.FlowProcessor",context);
            manager.setRole(WebKeys.FLOW_PROCESSOR, actor);
            // request
            actor =create("nds.control.web.RequestProcessor",context);
            manager.setRole(WebKeys.REQUEST_PROCESSOR, actor);
            // controller
            actor =create("nds.control.web.ClientControllerWebImpl",context);
            manager.setRole(WebKeys.WEB_CONTROLLER, actor);
            // table manager
            TableManager tm=nds.schema.TableManager.getInstance();
            // yfzhu changed at 2003-09-22 to load table path from nds.properties
            Properties props=conf.getConfigurations("schema").getProperties();
            tm.init(props);

            manager.setRole(WebKeys.TABLE_MANAGER,tm );
            // security manager
            actor =create("nds.control.web.SecurityManagerWebImpl",context);
            manager.setRole(WebKeys.SECURITY_MANAGER,actor );
 
            actor =create("nds.control.web.AttachmentManager",context);
            manager.setRole(WebKeys.ATTACHMENT_MANAGER,actor );

            // init MessagesHolder
            MessagesHolder mh= MessagesHolder.getInstance();
            mh.init(conf.getProperty("message.config", "/portal/server/default/deploy/nds.war/WEB-INF/lang/Language"));
            // olap config
            //LimitValueFormatter.init(conf.getProperty("messges.olap.config", "content/olap"));
            
            SysLogger.getInstance().init(conf.getProperty("jndi.datasource","java:/DataSource"), 
            		Tools.getInt(conf.getProperty("syslog.cache"), SysLogger.DEFAULT_QUEUE_SIZE),
            		Tools.getInt(conf.getProperty("syslog.sleep"), SysLogger.DEFAULT_SLEEP_SECONDS));
            
            // start job mamanger as last
            actor =create("nds.schedule.JobManager",context);
            manager.setRole(WebKeys.JOB_MANAGER,actor );
            // sms support if enabled, currently no incoming sms request supported
            if("true".equalsIgnoreCase(conf.getProperty("sms.enabled", "false"))){
            	actor=create("nds.sms.SMProcessor", context);
            	manager.setRole(WebKeys.SM_PROCESSOR,actor);
            }
            /*actor=create("nds.io.Main", context); 
            manager.setRole(WebKeys.POSAUTOMATOR,actor );*/
            // yfzhu 2003-08-04 more more ServerManager with web
//            actor= create("nds.net.ServerManager", context);
//            manager.setRole(WebKeys.SYNCMANAGER, actor);

            
            actor =create("nds.web.config.PortletConfigManager",context);
            manager.setRole(WebKeys.PORTLETCONFIG_MANAGER,actor );
            
            /**
             * Welcome page manager
             */
            String welcomePageManagerClass=  conf.getProperty("portal.welcome.manager", "nds.web.welcome.DefaultManager");
            actor =create(welcomePageManagerClass,context);
            manager.setRole(WebKeys.USER_WELCOME_MANAGER,actor );
            
            
   			//check license
   			nds.util.LicenseManager.validateLicense("Agile ERP","2.0",  conf.getProperty("license","/license.xml") );
            logger.debug("ServletContextManager initialized.");
            

        } catch(Exception e) {
            if(logger !=null)logger.error(" Error initializing ServletContextManager", e);
            else e.printStackTrace();
        }
        
        logger.debug("Manager in ServletContextManager "+manager+" initialized successfully.");
    }

    /**
    @roseuid 3BF30E9602DA
    */
    public void destroy() {
        // remove JNDI name
        /*Context initial=null;
        try {
            initial= new InitialContext();
            initial.unbind(JNDINames.CONTEXTMANAGER_WEB);
        } catch(NamingException e) {
            logger.error("Unable to unbind this from JNDI tree", e);
        }*/

        manager.destroyAll();
        manager.clear();
        SysLogger.getInstance().destroy();
        NativeTools.unload();
        debug("Manager in ServletContextManager "+ manager+" destroied");
    }
    private Configurations getConfigurations(ServletContext context)throws Exception {
        // load from ejbutils, which will initlized by nds.control.StartupEngine
        return nds.control.util.EJBUtils.getApplicationConfig();
    }
    public static void initLogger(ServletContext context) {
        // Initialize the logging system. As this class is instanciated both
        // on the server side and on the client side, we need to differentiate
        // the logging initialisation. This method is only called on the client
        // side, so we instanciate the log for client side here.

        /*if (!LoggerManager.getInstance().isInitialized()) {
            println(" initializing logger system");
    }else{
            println(" Reinitializing logger system");
    }
        LoggerManager.getInstance().init(conf.getProperties(), true);
         Logger adlogger = null;
         if ( adlogger == null ) {
            try{
            String logClass= conf.getProperty("log.addtional");
            adlogger = (Logger)Class.forName(logClass).newInstance();
            adlogger.init("NDS.Web",conf.getProperty("log.logfile"), conf.getProperty("log.loglevel"));
            }catch(Exception e){
                System.err.println("[MainServlet ] Could not get log configuration, using default" );
                e.printStackTrace();
            }
    }
        LoggerManager.getInstance().setAdditionalLogger(adlogger);*/
    }

}
