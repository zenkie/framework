//Source file: F:\\work\\sms\\src\\nds\\sms\\SMConnection.java

package nds.sms;

import nds.connection.AbstractConnection;
import nds.connection.ConnectionEvent;
import nds.connection.ConnectionFailedException;
import nds.connection.ConnectionListener;
import nds.connection.Message;
import nds.connection.MessageBundle;
import nds.connection.MessageListener;
import nds.connection.MessageLogListener;
import nds.util.Configurations;

/**
 * Wrapper of ConnectionChannel, which can either be http or file channel.
 * SMConnection will read from init properties to determine the channel class, using "sms.channel"
 * if property not set, using SMFileChannel by default
 */
public class SMConnection extends AbstractConnection
{
   private ConnectionChannel channel;

   /**
    * @roseuid 404A98F40292
    */
   public SMConnection()
   {

   }

   protected MessageBundle readFromChannel()
   {
       return channel.readFromChannel();
   }
   protected  void write(Message msg){
       channel.write(msg);
   }

   /**
    * Start channel, if channel has already started, do nothing
    * after connect successfully, ConnectionEvent will be dispatched.
    */
   public void connect() throws ConnectionFailedException{
       try{
           if (channel==null){

           	   String channelClass= props.getProperty("sms.channel","nds.sms.SMFileChannel");
           	   channel=(ConnectionChannel)  Class.forName(channelClass).newInstance(); 
           	   
               // proxy the connection event
               channel.addConnectionListener(new ConnectionListener(){
                   public void connectionEstablished(ConnectionEvent e){
                       fireConnectionEstablished(e);
                   }
                   public void connectionClosed(ConnectionEvent e){
                       fireConnectionClosed(e);
                   }

               });

               // proxy the message log event
               // set message logger
               String mllClass= props.getProperty("sms.logger");

               MessageLogListener l=null;
               try{
                   if(mllClass !=null)l=(MessageLogListener) Class.forName(mllClass).newInstance();
                   else
                       l= new nds.connection.DefaultMessageLogger();
               }catch(Exception e){
                   logger.error("Use DefaultMessageLogger since could not load "+ mllClass + " as MessageLogListener",e);
                   l= new nds.connection.DefaultMessageLogger();
               }
               l.init(props);
               
               addMessageLogListener(l ); // note this log listener is set on SMConnection, not channel

               // proxy the message event
               channel.addMessageListener(new MessageListener(){
                   public void onMessageArrived(Message msg){
                       fireMessageArrived(msg);
                   }
               });
               channel.init(props);
               
               Configurations c= new Configurations(this.props);
               channel.setupInputChain(props.getProperty("sms.filters.input"),c);
               channel.setupOutputChain(props.getProperty("sms.filters.output"),c);
               
               channel.connect() ;
           }else{
               if(!channel.isConnected()){
                   channel.init(props);
                   channel.connect();
               }
           }
       }catch(Exception e){
           logger.error("Could not connect", e);
           throw new ConnectionFailedException( "Could not connect", e);
       }
   }
   public int disconnect(){
       return channel.disconnect();
   }
   public boolean isConnected(){
       return channel==null?false: channel.isConnected();
   }

}
