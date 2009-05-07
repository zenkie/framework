//Source file: f:\\tmp\\mail\\NotificationManager.java

package nds.mail;

import java.sql.Connection;
import java.util.Properties;

import javax.mail.Session;
import javax.naming.InitialContext;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.ObjectQueue;
/**
 * NotificationManager doing Send mail or sms, this processing is asynchronous
 * singleton mode
 */
public class NotificationManager implements java.io.Serializable
{
    private static Logger logger= LoggerManager.getInstance().getLogger(NotificationManager.class.getName());
    private static NotificationManager instance=null;
    private Properties props;
    private boolean shouldNotify= false;
    private ObjectQueue queue;
    private static boolean initialized= false;
   /**
    * @roseuid 3E6990140040
    */
   private NotificationManager()
   {
       queue= new ObjectQueue(500);// maximum to 500, when exceeded, the supplier will be blocked
       queue.setInDataPreparing(true); // so the subscriber will always wait
   }
   public String getProperty(String pn, String defaultValue){
       if (pn ==null) return defaultValue;
       return props.getProperty(pn, defaultValue);
   }
   private void InsertPropertyIfNotNull(Properties props, String name, String value){
       if( value !=null)props.setProperty(name, value);
   }
   private void InsertPropertyIfNotNull( InitialContext ic, String env, String pn, String propName,Properties props){
       try{
       Object o=ic.lookup(env+pn);
       if ( o !=null) props.setProperty(propName,(String)o);
       logger.debug(propName + "=" + o);
       }catch(Exception e){
           logger.debug("Could not load property:"+ pn);
       }
   }
   private void StartMailDeamon() {
       try{
       if (!this.shouldNotify ) return;
       MailDaemon md= new MailDaemon(queue,props);
       Thread t= new Thread(md);
       t.setDaemon(true);
       t.start();
       }catch(Exception e){
           logger.error("Could not start mail daemon", e);
       }
   }
   //retrieve attribute from Context
   public void init(){
       if(initialized) return;

        this.props=new Properties();
        try {
/*            InitialContext ic = new InitialContext();
            String env="java:comp/env/notify/";

            InsertPropertyIfNotNull(ic,env,  "user", "mail.user", props);
            InsertPropertyIfNotNull(ic,env,  "password","mail.smtp.password", props);
            InsertPropertyIfNotNull(ic,env,  "from", "mail.from",props);
            InsertPropertyIfNotNull(ic, env, "port", "mail.smtp.port",props);
            InsertPropertyIfNotNull(ic,env,  "mailserver", "mail.host",props);
            InsertPropertyIfNotNull(ic,env,  "weburl","weburl", props);
            InsertPropertyIfNotNull(ic,env,  "debug", "mail.debug",props);
            InsertPropertyIfNotNull(ic,env,  "shouldNotify","shouldNotify", props);
*/
            Properties appConf= nds.control.util.EJBUtils.getApplicationConfigurations();
            InsertPropertyIfNotNull(props, "mail.user", appConf.getProperty("controller.notify.user"));
            InsertPropertyIfNotNull(props, "mail.smtp.password", appConf.getProperty("controller.notify.password"));
            InsertPropertyIfNotNull(props,"mail.from", appConf.getProperty("controller.notify.from"));
            InsertPropertyIfNotNull(props, "mail.smtp.port", appConf.getProperty("controller.notify.port"));
            InsertPropertyIfNotNull(props,"mail.host", appConf.getProperty("controller.notify.mailserver"));
            InsertPropertyIfNotNull(props,"weburl", appConf.getProperty("controller.notify.weburl"));
            InsertPropertyIfNotNull(props, "mail.debug", appConf.getProperty("controller.notify.debug"));
            InsertPropertyIfNotNull(props, "shouldNotify", appConf.getProperty("controller.notify.shouldNotify"));

            props.setProperty("mail.transport.protocol", "smtp");
            // insert into session to it
            Session session= Session.getDefaultInstance(props,null);
            if(session==null) {
                logger.error("Could not init MailSession");
                shouldNotify= false;
            }else props.put("MailSession", session);
            // insert connection to it
            // yfzhu marked up since connection will not return back(bug)
            //props.put("Connection", nds.query.QueryEngine.getInstance().getConnection() );
        } catch (Exception ex) {
            logger.error("Counld not init NotificationManager", ex);
            shouldNotify= false;
            return;
        }
        try{
            shouldNotify= (new Boolean(props.getProperty("shouldNotify","false"))).booleanValue() ;
        }catch(Exception e){
            logger.debug("Could not parse 'shouldNotify' in properties, default to false:"+ e.getMessage() );
        }
        StartMailDeamon();
        initialized=true;
   }
   public void init(Properties props){
       if(initialized) return;
       this.props= props;
       try{
           shouldNotify= (new Boolean(props.getProperty("shouldNotify","false"))).booleanValue() ;
       }catch(Exception e){
           logger.debug("Could not parse 'shouldNotify' in properties, default to false:"+ e.getMessage() );
       }
       StartMailDeamon();
       initialized=true;
   }
   /**
    * Send mail using that provided by #prepareMail
    */
   public void SendMail(MailMsg msg){
       try{
           if ( !shouldNotify || msg ==null) return ;
           queue.addElement(msg);
       }catch(Exception e){
           logger.error("Error sending mail:" + msg+ ":"+ e);
       }
   }
   /**
    * Prepare mail directly in this thread( different to handleObject)
    */
   public MailMsg prepareMail(int tableId, int objectId, String tableAction, StringBuffer briefMsg, StringBuffer detailMsg, Connection con){
       if ( !shouldNotify) return null;
       MailRobotSession rs= new MailRobotSession();
       if( con !=null) {
           Properties p2= new Properties(props);
           p2.put("Connection", con);
           rs.init(p2);
       }else {
           rs.init(props);
       }
       return rs.prepareMail(tableId, objectId, tableAction, briefMsg,detailMsg);

   }
    /** Handle object in a seperate thread.
    * @param tableId
    * @param objectId
    * @param tableAction
    * @param briefMsg
    * @param detailMsg
    * @roseuid 3E68B4E80375
    */
   public void handleObject(int tableId, int objectId, String tableAction, StringBuffer briefMsg, StringBuffer detailMsg, Connection con)
   {
       if ( !shouldNotify) return;
       try{

           MailRobotSession rs= new MailRobotSession();
           if( con !=null) {
               Properties p2= new Properties(props);
               p2.put("Connection", con);
               rs.init(p2) ;
           }else  rs.init(props) ;
           MailMsg msg=rs.prepareMail(tableId, objectId, tableAction, briefMsg,detailMsg);
           if (msg !=null) queue.addElement(msg);
       }catch(Exception e){
           logger.error("Could not handle object("+ tableId +","+ objectId+ ","+ tableAction+","+ briefMsg+","+ detailMsg +")", e);
       }
   }
   public static NotificationManager getInstance(){
       if ( instance ==null){
           instance= new NotificationManager();
       }
       return instance;
   }
 /*  private class HandleProcess implements Runnable{
       int tableId;
       int objectId;
       String tableAction;
       StringBuffer briefMsg;
       StringBuffer detailMsg;
       Properties properties;
       public void run(){
           try{
               //currently only mail supported
               RobotSession rs= new MailRobotSession();
               rs.init(properties);
               rs.handleObject(tableId, objectId, tableAction, briefMsg,detailMsg);
           }catch(Exception e){
               logger.error("Could not handle object("+ tableId +","+ objectId+ ","+ tableAction+","+ briefMsg+","+ detailMsg +")", e);
           }
       }
   }*/
}
