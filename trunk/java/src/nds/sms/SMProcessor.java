//Source file: F:\\work\\sms\\src\\nds\\sms\\SMProcessor.java

package nds.sms;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.servlet.ServletContext;

import nds.connection.AbstractConnection;
import nds.connection.ConnectionEvent;
import nds.connection.ConnectionFailedException;
import nds.connection.ConnectionListener;
import nds.connection.Message;
import nds.connection.MessageBundle;
import nds.connection.MessageListener;
import nds.connection.MessageLogListener;
import nds.control.web.SecurityManagerWebImpl;
import nds.control.web.SessionInfo;
import nds.control.web.SessionListener;
import nds.control.web.SessionManager;
import nds.util.CommandExecuter;
import nds.util.Configurations;
import nds.util.Director;
import nds.util.NDSRuntimeException;
import nds.util.ServletContextActor;
import nds.util.Tools;
import nds.util.WebKeys;
/**
 * SMProcessor is the fascade to sms device(channel), all clients(http session) will register
 * to SMProcessor to request and send messages. SMProcess contains a channel to sms device and
 * all registered client information. 
 * 
 * SMProcessor read messages from internal connection, and dispatch to specific client object queue
 * 
 * The platform specific sms device program can be contained within this process by using property 
 * "sms.daemon.contain=true", and command be set using "sms.daemon.cmd"
 * 
 * Support client throttle using property
 * throttle=true,
 * throttle.A.send.maxcount=10 ( total msg to send is 10)
 * throttle.A.send.maxperday=10 ( max msg to send per day is 10)
 */
public class SMProcessor extends AbstractConnection implements ServletContextActor, SessionListener
{
   private SMConnection connection; // contains connection to sms device
   private SMClientManager manager; // contains http clients connected, each client will construct a queue in SMClientManager 
   public static final int QUEUE_MAXLENGTH=100;
   public static final long RECONNECT_WAIT_TIME=1000; //1 seconds
   private boolean  bForceShutdown=false;// if true, connection will not try to connect again and again
   private Director director;
   private String daemonCommand=null;
   /**
    * Will contain daemon process.
    */
   private CommandExecuter daemonController;
   /**
    * @throw Exception if failed to establish connection to database
    */
   public SMProcessor() throws Exception
   {
       connection=new SMConnection();
       manager= new SMClientManager();
   }
   /**
    * Load configuration started from "sms"
    */
   public void init(Director director){
       this.director = director;
       Configurations conf= (Configurations) director.getActor(WebKeys.CONFIGURATIONS);
       if( conf==null) {
           throw new NDSRuntimeException("Could not find Configurations");
       }
//       Configurations c=conf.getConfigurations("sms");;

       if( conf !=null) props= conf.getProperties();
       connection.init(props);

       // load previous manager information from disk file if needed.
       // deprecated, the client infor will be cached to db now, so no need to hash to file
       /*if (Tools.getBoolean(props.getProperty("sms.recover", "true"), true)){
           // serialize to disk file, overwrite
           loadManager();
       }*/
       //  alway retry connection unless destroy
       connection.addConnectionListener(new ConnectionListener(){
                   public void connectionEstablished(ConnectionEvent e){
                       logger.info("Connected.");
                   }
                   public void connectionClosed(ConnectionEvent e){
                       logger.info("Disconnected.");
                       // try reconnect
                       if( bForceShutdown == false){
                           try{
                               logger.debug("Will sleep "+ (RECONNECT_WAIT_TIME/1000)+ " seconds then retry connection.");
                               Thread.sleep(RECONNECT_WAIT_TIME);
                               connect();
                           }catch(Exception exp){
                               logger.error("Errors found when connect:"+ exp);
                           }

                       }

                   }

               });
       //  Forward message to specific client queue
       connection.addMessageListener(new MessageListener(){
           public void onMessageArrived(Message msg){
               distpatchMessageToClient(msg);
           }
       });

       // listener to session register/unregister information
       SessionManager smwi=(SessionManager)director.getActor(WebKeys.SECURITY_MANAGER);
       if(smwi !=null)smwi.addSessionListener(this);
       else{
           throw new NDSRuntimeException("Could not find SecurityManagerWebImpl");
       }

       // log according to property "sms.logger"
       String lsr=props.getProperty("sms.logger", "nds.connection.DefaultMessageLogger");
       MessageLogListener l=null;
       try{
       l=(MessageLogListener) Thread.currentThread().getContextClassLoader().loadClass(lsr).newInstance();
       }catch(Exception e){
           logger.error("Could not load "+ lsr+ " as MessageLogListener, use default.", e );
           l= new nds.connection.DefaultMessageLogger();
       }
       l.init(props);
       connection.addMessageLogListener(l);

       try{
           connect();
           logger.debug("SMProcessor initialized.");
       }catch(Throwable e){
           throw new NDSRuntimeException("SMProcessor failed ", e);
       }
       try{
       		boolean startDeamon= "true".equalsIgnoreCase(conf.getProperty("sms.daemon.contain"));
       		if(startDeamon){
       			daemonCommand= conf.getProperty("sms.daemon.cmd", "/portal/bin/gammu.exe --smsd FILES /act/conf/smsdrc");
       			daemonController= new CommandExecuter("smsdaemon.log");
       			daemonController.backgroundRun(daemonCommand);
       			logger.debug("SMS Daemon started");
       		}
       }catch(Throwable t){
       		logger.error("Fail to startup daemon process: ",t);
       }
       
   }
   /**
    * @param context
    * @roseuid 404A98F001C4
    */
   public void init(ServletContext context)
   {

   }
   /**
    * cached sms will be stored to disk if needed, and can also be loaded when
    * init(Director) called
    */
   public void destroy(){
       bForceShutdown=true;
       super.destroy();
       /* deprecated, since messges will all cached to db whenever received. they will not lost
        * event if this processor abort*/
        /* if (Tools.getBoolean(props.getProperty("sms.recover", "true"), true)){
           // serialize to disk file, overwrite
           writeManager();
       }*/
       manager.clear();

       // unregister this listner
       SessionManager smwi=(SessionManager)director.getActor(WebKeys.SECURITY_MANAGER);
       if(smwi !=null)smwi.removeSessionListener(this);
       // stop daemon too
       try{
       if(daemonCommand!=null && daemonController!=null){
       		daemonController.stopBackgroundProcess(daemonCommand);
       		logger.debug("SMS Daemon stopped.");
       }
       }catch(Throwable t){
       	logger.error("Fail to stop SMS Daemon", t);
       }
   }
   /**
    * Get messages of specified client connected
    * @param uid client id , -1 for default client whose usercode is empty
    * @param wait if ture, will wait until at least one message will be retrieved
    * @return Empty MessageBundle(size=0) if specified client is not find
    * @throws ClientNotFoundException if client with specified id not found in SMClientManager
    */
   public MessageBundle getMessages(int uid, boolean wait) throws ClientNotFoundException{
       ClientMsgQueue client=null;//manager.g manager.get(new Integer(uid));
       client= manager.get(uid);
       if(client==null) throw new ClientNotFoundException("Client with id="+uid+" not found.");
       MessageBundle bundle=new MessageBundle();
       if (client ==null) return bundle;
       // has one message, then return that one, has none, wait
       if ( client.size() > 0){
           while ( client.size()>0){
               bundle.addMessage( client.nextElement());
           }
       }else{
           if(wait){
           		Message msg= client.nextElement();
           		if(msg!=null)
           			bundle.addMessage(msg );
           }
       }

       return bundle;
   }
   /**
    * @return Collection of SessionInfo
    */
   public Collection onlineClients()
   {
       return manager.getAll();
   }
   public boolean isConnected(){
       return connection.isConnected();
   }
   /**
    * When disconnect, you can specify where serialize current recieved message to disk or
    * not, using property "sms.recover" (true|false). If true, current connections with their
    * un-fetched messages will be serialized to disk file specified by "sms.recover.file"
    *
    * All the information will be retrieved back when connect
    *
    * "sms.recover" default to true
    *
    * @see connect
    *
    */
   public int disconnect(){
       bForceShutdown=true;
       int i= connection.disconnect();
       if (i!=0)  return i;
       return i;
   }
   /* load from file
    * @deprecated since 2.0 since all messages will be written to db when received
    */
   private void loadManager() {
       String fn=props.getProperty("sms.recover.file", "./smsquee.dat");
       ObjectInputStream is=null;
       SMClientManager m=null;
       try {
           is= new ObjectInputStream(new FileInputStream(fn));

       }catch (IOException ex) {
           logger.error("Could not read from file:"+ fn, ex);
       }
       try {
           if(is !=null)m=(SMClientManager)is.readObject();
       }
       catch (Exception ex) {
           logger.error("Could not read as SMClientManager from file:"+ fn, ex);
       }
       if( m!=null) manager=m;

   }
   /**
    * 
    *@deprecated since 2.0 since all messages will be written to db when received
    */
   private void writeManager(){
       String fn=props.getProperty("sms.recover.file", "./smsquee.dat");
       try {
           ObjectOutputStream os= new ObjectOutputStream(new FileOutputStream(fn));
           os.writeObject(manager);
       }
       catch (IOException ex) {
           logger.error("Could not write SMClientManager from file:"+ fn, ex);
       }
   }
   /**
    * If property "sms.recover" is set to true, we will try to load all previous client infomation
    * with their object queue from disk file when connect. The file isspecified by "sms.recover.file".
    * All recieved message but not fetched will be there.
    */
   public void connect(){
        try {
            connection.connect();
        }
        catch (ConnectionFailedException ex) {
            logger.error("Connection failed.", ex);
        }
   }

   protected void write(Message msg){
       connection.write(msg);
   }
   /**
    * This method is called when the raw message arrived from connection,
    * and we should find the correct client as receiver, and put the message to his
    * queue.
    *
    * The message's receiver will be changed after parsing
    */
   private void distpatchMessageToClient(Message msg){
       String receiver= msg.getReceiver();
       String sep= props.getProperty("sms.processor.receiver.seperator", " ");
       // this is the client's usercode
       String client= Tools.getFirstPart(receiver, sep);
       String secReceiver=Tools.getLastPart(receiver, sep);

       // put to client's queue
       msg.setReceiver(secReceiver);
       ClientMsgQueue queue= manager.get(client);
       if ( queue !=null) {
           try{
               queue.addElement((ShortMessage)msg);
           }catch(Exception e){
               logger.error("Discard message since failed to add to client queue:"+msg);
               msg.setStatus(msg.DISCARD);
           }
       }else{
           // queue not found, the client is not valid
           logger.info("Discard message since could not find client queue:"+ msg);
           msg.setStatus(msg.DISCARD);
       }

   }
   /**
    * This event is fired when a session is registered into SecurityManager
    * Implements SessionListener
    * @see SecurityManagerWebImpl
    */
   public void sessionRegistered(SessionInfo e){
       try{
           manager.put(e);
       }catch(Exception ex){
           logger.error("Could not register session to SMClientManager, sessionId="+ e.getSessionId()+" ip="+ e.getHostIP()+", user="+e.getUserName() , ex);
       }
   }

   /**
    * This event is fired when a session is unregistered into SecurityManager
    * Implements SessionListener
    * @see SecurityManagerWebImpl
    */
   public void sessionUnRegistered(SessionInfo e){
       // do nothing, no need to remove session info from manager
   }


}
