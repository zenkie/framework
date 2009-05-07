
package nds.export;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.net.Scheduler;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.util.*;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.HashMap;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.management.Notification;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Start ExportManager periodically, read config file from command line argument
 * 
 */
public class ExportMBean  extends ServiceMBeanSupport {
    private Logger logger= LoggerManager.getInstance().getLogger(ExportMBean.class.getName());
    private ExportManager manager;
	private int jobInterval; //sleep interval between running jobs
	private int checkInterval;// check for waiting jobs, if not found, will sleep that time
	private int workerCount; //max worker concurrently doing export job
	
	public ExportMBean(){}
	
    public ExportMBean(Properties props) {
    	checkInterval=Tools.getInt(props.getProperty("check_interval", "5"), 5);
    	jobInterval=Tools.getInt(props.getProperty("job_interval", "5"), 5);
    	workerCount= Tools.getInt(props.getProperty("max_workers", "1"), 1);
        
    }
    
	/**check for waiting jobs, if not found, will sleep that time
	 * @return Returns the checkInterval.
	 */
	public int getCheckInterval() {
		return checkInterval;
	}
	/**
	 * @param checkInterval The checkInterval to set.
	 */
	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}
	/**sleep interval between running jobs
	 * @return Returns the jobInterval.
	 */
	public int getJobInterval() {
		return jobInterval;
	}
	/**
	 * @param jobInterval The jobInterval to set.
	 */
	public void setJobInterval(int jobInterval) {
		this.jobInterval = jobInterval;
	}
	/**max worker concurrently doing export job
	 * @return Returns the workerCount.
	 */
	public int getWorkerCount() {
		return workerCount;
	}
	/**
	 * @param workerCount The workerCount to set.
	 */
	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
	}
	protected void startService() throws Exception {
		manager=new  ExportManager(this);
        
        Thread expSchedler= new Thread(manager);
        expSchedler.setDaemon(true);
        expSchedler.start();		
	}
	protected void stopService() throws Exception {
		manager.stop();
		manager=null;
	}

    private static void usage() {
        System.err.println("Usage:\n  java nds.export.ExportDaemon [-s properties]");
        System.err.println("\nOptions:");
        System.err.println("  -s : indicates the property file");
        System.exit(1);
    }

    public static void main(String[] argument) throws Exception{
        ExportMBean m=null;
        try{

          String propfile=System.getProperty("nds.config.path","/nds.properties");

          if(argument !=null)for (int i = 0; i < argument.length; i++) {
            if (argument[i].equals("-s")) {
              if (i + 1 < argument.length)
                propfile = argument[++i];
                if(propfile==null) propfile=System.getProperty("nds.config.path","/nds.properties");
            }
          }
          System.setProperty("applicationPropertyFile",propfile);
          InputStream is= new FileInputStream(propfile);
          Configurations confs = new Configurations(is);
          LoggerManager.getInstance().init(confs.getProperties(),true);
          //export excel will use this
          TableManager tm=nds.schema.TableManager.getInstance();
          // yfzhu changed at 2003-09-22 to load table path from nds.properties
          Properties props=confs.getConfigurations("schema").getProperties();
          tm.init(props);
          
          Properties netProps=confs.getConfigurations("export").getProperties();
          m= new ExportMBean(netProps);
          // init db connection pool
          Class.forName(netProps.getProperty("jdbc.drivers", "oracle.jdbc.driver.OracleDriver"));
          DataSource ds= setupDataSource(netProps.getProperty("jdbc.uri"));
          QueryEngine.getInstance(ds).init(confs.getProperties());
          

       }catch(Exception e){
          e.printStackTrace();
          usage();
          return;
       }
        m.startService();
        while( true) {
            try{
                Thread.sleep(1000*60*60); // sleep one hour
            }catch(Exception e){
                e.printStackTrace() ;
            }
        }
    }
    public static DataSource setupDataSource(String connectURI) throws Exception{
        //
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool connectionPool = new GenericObjectPool(null);

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,null);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

        return dataSource;
    }

}    