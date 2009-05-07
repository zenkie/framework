package nds.control.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import nds.control.ejb.ClientControllerHome;
import nds.control.web.RequestProcessor;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.JNDINames;
import nds.util.WebKeys;

/**
 * This is a utility class for obtaining EJB references.
 */
public final class EJBUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger(EJBUtils.class.getName());
    private static Configurations conf;
    /**
     * This method will only be called when conf is null, which means no
     * pre-defined process initilize the configuration, normally in web server
     * context.
     */
    private static void initConfigurations(){
        System.out.println("[Control.EJBUtils] Ininitializing logger system");
        String propFile=System.getProperty("nds.config.path","/nds.properties");

        LoggerManager.getInstance().init(propFile,true);
        try{
            //URL url = EJBUtils.class.getResource(propFile);//"/nds.properties");
            //Configurations config= new Configurations(url.openStream());
            File file = new File( propFile);
            java.net.URL url = file.toURL();
            conf= new Configurations(url.openStream());

        }catch(Exception e){
            e.printStackTrace();
        }

    }
    /**
     * Create a new configuration.
     */
    public static Configurations createConfigurations(String propFile) throws Exception{
        File file = new File( propFile);
        java.net.URL url = file.toURL();
        return  new Configurations(url.openStream());

    }
    /**
     * This method should be called when in web server context, where should exist
     * a load-on-start servlet who will load configurations from disk file.
     * If this method is not call on startup, configuration will be retrieved from
     * file specified by system property System.getProperty("nds.config.path","/nds.properties");
     */
    public static void initConfigurations(Configurations config){
        conf= config;
    }
    /**
        * Call this method before initConfigurations(config) means load configruations from
        * file specified by system property
        */
    public static Configurations getApplicationConfig(){
           if(conf != null)return conf;
           else{
               // initilize the configuration file according to system property instead
               initConfigurations();
           }
           return conf;
    }
    /**
     * Call this method before initConfigurations(config) means load configruations from
     * file specified by system property
     */
    public static Properties getApplicationConfigurations(){
        if(conf != null)return conf.getProperties();
        else{
            // initilize the configuration file according to system property instead
            initConfigurations();
        }
        return conf.getProperties();
    }

    public static ClientControllerHome getClientControllerHome() throws javax.naming.NamingException {
            InitialContext initial = new InitialContext();
            Object objref = initial.lookup(JNDINames.CLIENTCONTROLLER_EJBHOME);
            return (ClientControllerHome)
                PortableRemoteObject.narrow(objref, ClientControllerHome.class);
    }
    public static UserTransaction getUserTransaction() throws javax.naming.NamingException {
        InitialContext initial = new InitialContext();
        String jndi=getApplicationConfigurations().getProperty("jndi.usertransaction","javax.transaction.UserTransaction");
        Object objref = initial.lookup(jndi);
        return (UserTransaction)objref;

    }
    /**
     * 
     * @return EJBUtils.getUserTransaction
     */
    public static UserTransaction beginTransaction() throws Exception {
    	UserTransaction ut=EJBUtils.getUserTransaction();
    	ut.setTransactionTimeout(30*60); // 30 minutes default
    	ut.begin();
    	return ut;
    }
    /**
     * 
     * @param transObj UserTransaction
     */
    public static void rollbackTransaction(Object transObj){
    	UserTransaction ut=(UserTransaction) transObj;
    	try{ ut.rollback();}catch(Throwable e){
            logger.error("Could not rollback.", e);
        }
    }
    /**
     * 
     * @param transObj UserTransaction
     */
    public static void commitTransaction(Object transObj){
    	UserTransaction ut=(UserTransaction) transObj;
    	try{ ut.commit();}catch(Throwable e){
            logger.error("Could not commit.", e);
        }
    }
    /**
     * Get logger of specified category
     */
    public static Logger getLogger(String name) {
        if (!LoggerManager.getInstance().isInitialized()) {

            //LoggerManager.getInstance().init("/nds.properties");
            LoggerManager.getInstance().init(conf.getProperties());
            /*try{
                InitialContext initial = new InitialContext();
                // logger of ejb is initialized when WebLogic start up
                // @see nds.StartupEngine
                Logger defaultLogger =(Logger) initial.lookup(JNDINames.LOGGER);
                LoggerManager.getInstance().setAdditionalLogger(defaultLogger);
            }catch(Exception e){
                System.out.println("Error getting logger from JNDI tree:");
                e.printStackTrace();
            }*/
        }
        return LoggerManager.getInstance().getLogger(name);
    }


/*    public static Mailer createMailerEJB()
                      throws javax.naming.NamingException,
                               CreateException,  RemoteException {
            InitialContext initial = new InitialContext();
            Object objref = initial.lookup(JNDINames.MAILER_EJBHOME);
            MailerHome home = (MailerHome)
            PortableRemoteObject.narrow(objref, MailerHome.class);
            return (Mailer) home.create();
    }
*/

/**
 * Will try get user information from request, and verify it's ok
 * @param request
 * @throws nds.security.LoginFailedException
 */
    public static void authenticate(HttpServletRequest request) throws nds.security.LoginFailedException{
        SessionContextManager mgr=WebUtils.getSessionContextManager(request.getSession(true));
        if( mgr !=null){
            UserWebImpl user = (UserWebImpl)mgr.getActor(nds.util.WebKeys.USER);
            if(user.isLoggedIn()){
                return;
            }
        }
        //try get from header
        try{
            RequestProcessor rp=(RequestProcessor)WebUtils.getServletContextManager().getActor( WebKeys.REQUEST_PROCESSOR);
            nds.control.web.reqhandler.RequestHandler handler= new nds.control.web.reqhandler.LoginRequestHandler();
            ValueHolder vh=rp.processRequest(request,handler);
        }catch(Exception e){
            throw new nds.security.LoginFailedException("Authoriztion failed:"+ e, e);
        }
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     * 
     * @param pckgname
     *            the package name to search
     * @return a list of classes that exist within that package, elements are string for class name
     * @throws ClassNotFoundException
     *             if something went wrong
     */
    private static List getClassesForPackage(String pckgname) throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
        ArrayList directories = new ArrayList();//<File>
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = pckgname.replace('.', '/');
            // Ask for all resources for the path
            Enumeration resources = cld.getResources(path);//<URL>
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode( ((URL)resources.nextElement()).getPath(), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }
 
        ArrayList classes = new ArrayList(); //<Class> 
        // For every directory identified capture all the .class files
        for (Iterator it=directories.iterator();it.hasNext();) {
        	File directory= (File) it.next();
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (int i=0;i<files.length;i++) {
                	String file= files[i];
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directory.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }
    //private final static SimpleDateFormat sheetNoDateFormatter=new SimpleDateFormat("yyMMdd");
    //private final static DecimalFormat sheetNoFormatter=new DecimalFormat("0000") ;

}
