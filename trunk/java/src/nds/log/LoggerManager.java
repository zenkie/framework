/******************************************************************
*
*$RCSfile: LoggerManager.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:18 $
*
*$Log: LoggerManager.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:18  Administrator
*init
*
*Revision 1.3  2004/02/02 10:42:39  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/09/29 07:37:11  yfzhu
*before removing entity beans
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.log;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;


import org.apache.log4j.PropertyConfigurator;

/**
 * Logging service acting as a wrapper around the Jakarta Log4j logging
 * framework.
 *
 * @version $Id: LoggerManager.java,v 1.1.1.1 2005/03/15 11:23:18 Administrator Exp $
 */
public class LoggerManager
{
    /**
     * List of <code>Log</code> instances (i.e. <code>Category</code>
     * objects for Log4j) indexed on the category's name.
     */
    private Hashtable logCategories = new Hashtable();

    /**
     * Has initialization been performed yet ?
     */
    private boolean isInitialized = false;

    /**
     * Is Log4j in the classpath ?
     */
    private boolean isLog4jInClasspath = false;

    /**
     * The singleton's unique instance
     */
    private static LoggerManager instance;
    /**
     * the alternate logger
     */
    private Logger additionalLogger=null;
    /**
     * Initialization
     */
    private LoggerManager()
    {
        // Check if Log4j is in the classpath. If not, use a dummy
        // implementation that does nothing. This is to make it easy on user
        // who do not want to have to download log4j and put it in their
        // classpath !
        this.isLog4jInClasspath = true;
        try {
            Class aClass =
                Class.forName("org.apache.log4j.PropertyConfigurator");
        } catch (ClassNotFoundException e) {
            System.out.println("[LoggerManager] Not found Log4J in classpath");
            this.isLog4jInClasspath = false;
        }
    }
    /**
     * Set alternate logger for logging message
     */
    public void setAdditionalLogger(Logger logger){
/*        if( logger instanceof CategoryLogger){
            System.out.println("[LoggerManager] CategoryLogger "+ logger+ " was used as additional logger");
            Thread.dumpStack();
        }*/
        additionalLogger=logger;
        // update all loggers
        Iterator it= logCategories.values().iterator();
        while(it.hasNext()){
            Logger log= (Logger) it.next();
            if(log instanceof CategoryLogger){
                ((CategoryLogger)log).setAdditionalLogger(logger);
            }
        }
    }

    /**
     * @return the unique singleton instance
     */
    public static synchronized LoggerManager getInstance()
    {
        if (instance == null) {
            instance = new LoggerManager();
        }
        return instance;
    }
    /**
     * Initialize the logging system. Need to be called once before calling
     * <code>getLog()</code>.
     *
     * @param props the properties which contains log configuration info or null
     *        to initialize a dummy logging system, meaning that all log calls
     *        will have no effect. This is useful for unit testing for
     *        instance where the goal is not to verify that logs are printed.
     *  @param forceInit if true, the Logger system should be initialized no matter
     *        it has already be initialized or not
     */
    public void init(Properties props, boolean forceInit){
        // If logging system already initialized, do nothing
        if (isInitialized() && (forceInit==false)) {
            return;
        }

        if (props != null) {
            if (this.isLog4jInClasspath) {
//                PropertyConfigurator.resetConfiguration();
                PropertyConfigurator.configure(props);
            }

        }

        this.isInitialized = true;

    }
    public void init(Properties props){
        init(props, false);
    }
    public void init(String theFileName){
        init(theFileName, false);
    }
    /**
     * Initialize the logging system. Need to be called once before calling
     * <code>getLog()</code>.
     *
     * @param theFileName the file name (Ex: "/log_client.properties") or null
     *        to initialize a dummy logging system, meaning that all log calls
     *        will have no effect. This is useful for unit testing for
     *        instance where the goal is not to verify that logs are printed.
     *  @param forceInit if true, the Logger system should be initialized no matter
     *        it has already be initialized or not
     */
    public void init(String theFileName, boolean forceInit)
    {
        // If logging system already initialized, do nothing
        if (isInitialized() && (forceInit ==false) ) {
            return;
        }
//        Thread.dumpStack();
        if (theFileName != null) {

            if (this.isLog4jInClasspath) {

                try{
                    //URL url = this.getClass().getResource(theFileName);
                    //String url = theFileName;
                    java.io.File file=new java.io.File(theFileName);
                    URL url = file.toURL();

                    if (url != null) {
                        // Initialize Log4j
                        System.out.println("[LoggerManager] Loading "+url);
//                        PropertyConfigurator.resetConfiguration();
                        PropertyConfigurator.configure(url);
                    } else {
                        // Failed to configure logging system, simply print
                        // a warning on stderr
                        System.err.println("[LoggerManager] <Warning> Failed to configure " +
                            "logging system : Could not find file [" +
                            theFileName + "]");
                    }
                }catch(MalformedURLException e){
                    System.out.println(e.getMessage());
                }

            }

        }

        this.isInitialized = true;
    }

    /**
     * @param theCategoryName the category's name. Usually, it is the full
     *        name of the class being logged, including the package name
     * @return the <code>Log</code> instance associated with the specified
     *         category name
     */
    public synchronized Logger getLogger(String theCategoryName)
    {
        // Check first if initialization has been performed
        if (!isInitialized()) {
            // try using default logger configuration
            String propFile=System.getProperty("nds.config.path","/nds.properties");
            try{init(propFile);}catch(Exception e){
                throw new RuntimeException("Not ininitialized, and default configuration "+propFile+" not found either.");
            }
            System.out.println("[LoggerManager] Loaded "+propFile+" by default for configuration.");
        }

        Logger log = (Logger)this.logCategories.get(theCategoryName);

        if (log == null) {

            if (this.isLog4jInClasspath) {
                log = new CategoryLogger(theCategoryName);
                ((CategoryLogger)log).setAdditionalLogger(additionalLogger);
            } else {
                log = new DummyLogger(theCategoryName);
            }

            this.logCategories.put(theCategoryName, log);

        }

        return log;
    }

    /**
     * @return true if the logging system has already been initialized.
     */
    public boolean isInitialized()
    {
        return this.isInitialized;
    }
    
}
