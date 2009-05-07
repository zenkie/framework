package nds.net;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.Configurations;
import nds.util.CronParser;
import nds.util.Director;
import nds.util.NDSException;
import nds.util.Sequences;
import nds.util.ServletContextActor;
import nds.util.WebKeys;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 *Work as fascade to other modules
 */
public class ServerManager implements SessionManager, ServletContextActor,java.io.Serializable {
    //public final static String DEFAULT_PROPERTY_FILE="/nds.properties";
    private SendDownloadRequest autoRequestDownload;
    private static boolean debug= false;
    public static boolean isDebug(){
        return debug;
    }
    private String lastDownloadFile=null, lastDownloadClientName=null;
    public static void  setDebug(boolean d){ debug= d;}

    private transient Logger logger= LoggerManager.getInstance().getLogger(SessionController.class.getName());
    private transient SessionController controller;
    private Scheduler expSchedler;
    Director director;
    Properties props;
    String syncManagerID;// format : ndsjava@act/Work
    public ServerManager(){}
    public ServerManager(Properties props) {
        this.props= props;
        controller=new SessionController(props,this);
        syncManagerID= props.getProperty("SyncManager.ID", "ndsjava@act/Work");
    }

    public void executeXML(String clientName, String msg) throws NDSException{
        if ( controller.isConnected()== true){
            controller.sendXML( "NDSMGR_" +Sequences.getNextID("SessionMsg"),msg, clientName);
        }else{
            throw new NDSException("连接尚未建立!");
        }
    }
    /**
     * If any unhandlable error
     */
    public void Connect() throws NDSException{
        try{
        controller.start() ;
        }finally{
        autoRequestDownload= new SendDownloadRequest();
        autoRequestDownload.init(props);

        autoRequestDownload.setServerManager(this);
        Thread t= new Thread(autoRequestDownload);
        t.start();
        }
    }
    public boolean isConnected(){
        return controller.isConnected();
    }
    public void DisConnect(){
        try{
            controller.kill();
        }catch(Exception e){
            logger.error("Error in disconnect SessionController", e);
        }
    }
    /**
     * Start daemon process exporting pos data periodically
     */
    public void StartExportDaemon() throws NDSException{
        String expRule=props.getProperty("cron","0   *   *   *     1-5");
        CronParser cp=new CronParser(expRule);
        //Util.log("up rule:"+cp.getPlainMeaning());
        logger.info("Export POS data :"+cp.getPlainMeaning());

        expSchedler= new Scheduler("nds.net.ExportPOSData",
                                   "ExportPOSData daemon",
                                   expRule);
        expSchedler.init(props);
        expSchedler.start();
    }

    //------------------ ServletContext------
    /**
     *  Get configuration from director's actor, and start doing actions
     *
     */
    public void init(Director director) {
        this.director= director;
        Configurations conf= (Configurations) director.getActor(WebKeys.CONFIGURATIONS);
        if( conf==null) {
            logger.error("Could not find configurations");
            return;
        }
        Configurations c=conf.getConfigurations("net");;

        if( c !=null) props= c.getProperties();
        try{

//        ServerManager m= new ServerManager(props);
            controller= new SessionController(props, this);
            Connect();
            logger.debug("ServerManager initialized.");
        }catch(Exception e){
            logger.error("ServerManager failed ", e);
        }
        try{
            // added at 2003-09-25 to enable start/off export daemon
            if("on".equalsIgnoreCase(c.getProperty("daemon.export", "off"))||
                        "1".equalsIgnoreCase(c.getProperty("daemon.export")))
                StartExportDaemon();
        }catch(Exception e2){
            logger.error("StartExportDeamon failed ", e2);
        }

    }
    public void init(ServletContext context) {

    }
    public void destroy(){
        DisConnect();
        try{
            if (expSchedler !=null){
                expSchedler.kill() ;
            }
        }catch(Exception e2){
            logger.error("Error in stop Schedule", e2);
        }
        logger.debug("ServerManager destroied.");
    }
    public String getLastDownloadFile(){
        if(lastDownloadFile==null){
            getDownloadInfo();
        }
        return lastDownloadFile;
    }
    public String getLastDownloadClientName(){
        if(lastDownloadClientName==null){
            getDownloadInfo();
        }
        return lastDownloadClientName;
    }

    private void getDownloadInfo(){
        // read from db
        String sql= "select name, value from appsetting where name = 'last_download_file' or name = 'last_download_client'";
        ResultSet rs=null;
        try{
            rs=QueryEngine.getInstance().doQuery(sql,true);
            if ( rs.next() ){
                if( "last_download_file".equals( rs.getString(1)))
                   this.lastDownloadFile = rs.getString(2);

                if( "last_download_client".equals( rs.getString(1)))
                   this.lastDownloadClientName = rs.getString(2);
            }
            // the second record
            if ( rs.next() ){
                if( "last_download_file".equals( rs.getString(1)))
                   this.lastDownloadFile = rs.getString(2);

                if( "last_download_client".equals( rs.getString(1)))
                   this.lastDownloadClientName = rs.getString(2);
            }


        }catch(Exception e){
            logger.error("During get last download info" ,e);
        }finally{
            try{rs.close();}catch(Exception e2){}
        }

    }
    public void pauseRequestFileDownload(){
        autoRequestDownload.pause();
    }
    public void resumeRequestFileDownload(){
        autoRequestDownload.resume() ;
    }
    // -- implements SessionManager
    public String getSessionMsgPrefix(){
        return "NDSMGR_";
    }
    public void markFileHandled(String clientName, String fileName){
        lastDownloadFile=fileName;
        lastDownloadClientName=clientName;
        // write to db
        try{
            QueryEngine  engine=QueryEngine.getInstance() ;
            Vector v=new Vector();
            v.addElement( "update appsetting set value='"+ lastDownloadFile + "' where name = 'last_download_file'");
            v.addElement( "update appsetting set value='"+ lastDownloadClientName + "' where name = 'last_download_client'");
            engine.doUpdate(v);
        }catch(Exception e){
            logger.error("During update last download info" ,e);
        }

    }
    public boolean fileHandled(String clientName, String fileName){
        return clientName.equalsIgnoreCase(lastDownloadClientName) && fileName.equalsIgnoreCase(lastDownloadFile);
    }
    public void sendRequestDownloadMsg() throws Exception{
        if(isConnected()  ){
            SessionMsg msg=new SessionMsg("NDSMGR_");
            msg.setTo(syncManagerID);
            msg.setThreadID("RequestFileDownload_" + Sequences.getNextID("RequestFileDownload"));
            msg.addParam("CommandType", "RequestFileDownload");
            msg.addParam("ClientName", "NDSMGR");
            msg.addParam("LastDownloadFile",getLastDownloadFile()  );
            msg.addParam("LastDownloadClientName", getLastDownloadClientName() );
            controller.sendMsg(msg);
        }
    }

    public void sendMsg(SessionMsg msg) throws NDSException{
        if ( controller.isConnected()== true){
            controller.sendMsg(msg);
        }else{
            throw new NDSException("连接尚未建立!");
        }
    }
    private static void usage() {
        System.err.println("Usage:\n  java nds.net.ServerManager [-s properties]");
        System.err.println("\nOptions:");
        System.err.println("  -s : indicates the property file");
        System.exit(1);
    }

    public static void main(String[] argument) throws Exception{
        String[] s={"username", "ndsjava2", "server","jbserver", "resource", "Work",
                    "password", "abc123",
            "ActivePOSVersion", "1",
            "ActivePOSURL", "ftp://yfzhu:abc123@act/posdb/download/CSC001/200306111910.sql.gz",
            "ActivePOSFileLength", "5439607",
            "ActivePOSCheckSum", "43e898729c87291a67d1fb57db25ede4",
            "CVSROOT", ":pserver:yfzhu@localhost:e:/cvsnt",
            "CVSPOSModule", "bbs",
            "CVSPassword", "Ayuh4Kw",
            "PosDB.Download.RootDir", "/posdb/download",
            "PosDB.Download.RootURL", "ftp://yfzhu:abc123@mit/posdb/download",
            "PosDB.Upload.RootURL", "ftp://yfzhu:abc123@mit/posdb/upload",
            "PosDB.Upload.RootDir", "/posdb/upload",
            "PosDB.TmpDir", "/posdb/tmp",/*import db temporoty command result file location*/
            "PosDB.Import.Command","sh /impora"

        };
        ServerManager m=null;
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
          Properties netProps=confs.getConfigurations("net").getProperties();
          m= new ServerManager(netProps);
          LoggerManager.getInstance().init(confs.getProperties(),true);
          // init db connection pool
          Class.forName(netProps.getProperty("jdbc.drivers", "oracle.jdbc.driver.OracleDriver"));
          DataSource ds= setupDataSource(netProps.getProperty("jdbc.uri"));
          QueryEngine.getInstance(ds);

       }catch(Exception e){
          e.printStackTrace();
          usage();
          return;
       }
        m.Connect();
        m.StartExportDaemon();
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