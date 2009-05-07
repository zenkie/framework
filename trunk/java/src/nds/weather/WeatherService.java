package nds.weather;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.net.Scheduler;
import nds.query.QueryEngine;
import nds.util.Configurations;
import nds.util.CronParser;
import nds.util.NDSException;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

public class WeatherService {
    private transient Logger logger= LoggerManager.getInstance().getLogger(WeatherService.class.getName());
    private Scheduler expSchedler;
    Properties props;
    public WeatherService(Properties props) {
           this.props= props;
    }
    public void destroy(){
        try{
            if (expSchedler !=null){
                expSchedler.kill() ;
            }
        }catch(Exception e2){
            logger.error("Error in stop Schedule", e2);
        }
        logger.debug("WeatherService destroied.");
    }



    public void StartDaemon() throws NDSException{
            String expRule=props.getProperty("cron","0   0,8,12,19   *   *    *");
            CronParser cp=new CronParser(expRule);
            //Util.log("up rule:"+cp.getPlainMeaning());
            logger.info("Start Deamon :"+cp.getPlainMeaning());
            expSchedler= new Scheduler("nds.weather.Fetcher",
                                       "Weather Fetcher Daemon",
                                       expRule);
            expSchedler.init(props);
            expSchedler.start();
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
    private static void usage() {
        System.err.println("Usage:\n  java nds.net.WeatherService [-c properties] [-1 cities]");
        System.err.println("\nOptions:");
        System.err.println("  -c : indicates the property file");
        System.err.println("  -1 : indicates cities to fetch now, seperated by comma");
        System.err.println("\nSuch as:");
        System.err.println("java nds.weather.WeatherService -s /nds.properties -1 shanghai,guangzhou");
        System.exit(1);
    }
    /*
    * argument : "-s propfile -1 <city1[,city2,...]>"
    * such as
    *      java nds.weather.WeatherService -s /nds.properties -1 shanghai,guangzhou
    * will fetch immediatly and exit
    */
    public static void main(String[] argument) throws Exception{
        WeatherService m=null;
        boolean fetchNow=false;
        String cities ="";
        Properties netProps=null;
        try{

          String propfile=System.getProperty("nds.config.path","/nds.properties");

          if(argument !=null)for (int i = 0; i < argument.length; i++) {
            if (argument[i].equals("-c")) {
              if (i + 1 < argument.length)
                propfile = argument[++i];
                if(propfile==null) propfile=System.getProperty("nds.config.path","/nds.properties");
            }else if( argument[i].equals("-1")){
                if (i + 1 < argument.length){
                    fetchNow= true;
                    cities= argument[++i];
                }
            }

          }
          System.setProperty("applicationPropertyFile",propfile);
          InputStream is= new FileInputStream(propfile);
          Configurations confs = new Configurations(is);
          LoggerManager.getInstance().init(confs.getProperties(),true);
          netProps=confs.getConfigurations("weather").getProperties();
          m= new WeatherService(netProps);
          // init db connection pool
          Class.forName(netProps.getProperty("jdbc.drivers", "oracle.jdbc.driver.OracleDriver"));
          DataSource ds= setupDataSource(netProps.getProperty("jdbc.uri"));
          QueryEngine.getInstance(ds);

       }catch(Exception e){
          e.printStackTrace();
          usage();
          return;
       }
       if (fetchNow==false){
        m.StartDaemon();
        while( true) {
            try{
                Thread.sleep(1000*60*60); // sleep one hour
            }catch(Exception e){
                e.printStackTrace() ;
            }
        }
       }else{
           Fetcher f=new Fetcher();
           f.init(netProps);
           StringTokenizer st=new StringTokenizer(cities,",");
           while(st.hasMoreTokens() ){
               String city= st.nextToken();
               WeatherObject wo=f.fetch(city);
               if(wo ==null){
                   System.out.println("city: " + city + " not found.");
               }else{
                   System.out.println("city: "+ city);
                   System.out.println(wo.toString());
               }
           }
           System.exit(0);
       }
    }

}