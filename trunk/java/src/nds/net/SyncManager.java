package nds.net;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.Director;
import nds.util.NDSException;
import nds.util.Sequences;
import nds.util.ServletContextActor;
import nds.util.WebKeys;
/**
 *Work as fascade to other modules
 */
public class SyncManager implements SessionManager, ServletContextActor,java.io.Serializable {
    public final static String DEFAULT_PROPERTY_FILE="/sync.properties";
    private static boolean debug= false;
    private transient Logger logger= LoggerManager.getInstance().getLogger(SessionController.class.getName());
    private transient SessionController controller;
    Director director;
    Properties props;
    private RelayManager relayMgr;

/*    public static boolean isDebug(){
        return debug;
    }
    public static void  setDebug(boolean d){ debug= d;}
*/


    public SyncManager(){}
    public SyncManager(Properties props) {
        this.props= props;
        controller=new SessionController(props, this);
    }

    public RelayManager getRelayManager(){
        return relayMgr;
    }
    public void executeXML(String clientName, String msg) throws NDSException{
        if ( controller.isConnected()== true){
            controller.sendXML( this.getSessionMsgPrefix()  +Sequences.getNextID("SessionMsg"),msg, clientName);
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
        // start relay manager
        relayMgr= new RelayManager();
        relayMgr.init(props.getProperty("PosDB.Upload.RelayLogDir",props.getProperty("PosDB.Upload.RootDir", "/posdb")));
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
    // -- implements SessionManager
    public String getSessionMsgPrefix(){
        return "NDSJava_";
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

//        SyncManager m= new SyncManager(props);
            controller= new SessionController(props, this);
            Connect();
            logger.debug("SyncManager initialized.");
        }catch(Exception e){
            logger.error("SyncManager failed ", e);
        }

    }
    public void init(ServletContext context) {

    }
    public void destroy(){
        DisConnect();
        logger.debug("SyncManager destroied.");
    }
    private static void usage() {
        System.err.println("Usage:\n  java nds.net.SyncManager [-p properties]");
        System.err.println("\nOptions:");
        System.err.println("  -p : indicates the property file");
        System.exit(1);
    }

    public static void main(String[] argument) throws Exception{
        SyncManager m=null;
        try{
          String propfile = DEFAULT_PROPERTY_FILE;

          if(argument !=null)for (int i = 0; i < argument.length; i++) {
            if (argument[i].equals("-p")) {
              if (i + 1 < argument.length)
                propfile = argument[++i];
                if(propfile==null) propfile=DEFAULT_PROPERTY_FILE;
            }
          }
          System.setProperty("applicationPropertyFile",propfile);
          InputStream is= new FileInputStream(propfile);
          Configurations confs = new Configurations(is);
          m= new SyncManager(confs.getConfigurations("net").getProperties());
          LoggerManager.getInstance().init(confs.getProperties(),true);

      }catch(Exception e){
          e.printStackTrace();
          usage();
          return;
       }

        m.Connect();
        while( true) {
            try{
                Thread.sleep(1000*60*60); // sleep one hour
            }catch(Exception e){
                e.printStackTrace() ;
            }
        }
    }
}