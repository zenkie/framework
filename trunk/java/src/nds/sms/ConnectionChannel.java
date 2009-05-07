//Source file: F:\\work\\sms\\src\\nds\\sms\\ConnectionChannel.java

package nds.sms;

import java.util.Iterator;

import nds.connection.AbstractConnection;
import nds.connection.ConnectionEvent;
import nds.connection.ConnectionFailedException;
import nds.connection.Message;
import nds.connection.MessageBundle;
import nds.util.LifeCycle;
import nds.util.Tools;

/**
 * The channel to send/read messages. 
 * Extenders will the storage format of the messages, such as to file system (SMFileChannel)
 * or to http client (SMHttpChannel), or to database (not implemented yet)
 * 
 * ConnectionChannel is wrappered by SMConnection, which will determine which implementation class
 * will be used (while init properties)
 * @see SMConnection
 */
public class ConnectionChannel extends AbstractConnection
{
   /**
    * Periodically read from specified device and notify messagelisteners that message arrived,
    * if message discard, it will call logMessage to record.
    */
   protected ChannelReader channelReader;
   /**
    * If channel reader failed, this will also be set to false, with message sending out
    */
   private boolean isConnected=false;

   /**
    * Every speciefied time, and read from channel, then put recieving messages to incomeQueue
    */
   protected class ChannelReader implements Runnable, LifeCycle{
       private boolean running=false;

       public void run(){
           if ( running==true) return;
           logger.debug("ChannelReader started");
           running=true;
           // wait seconds each time read channel
           int interval= Tools.getInt( props.getProperty("sms.read.interval","5"), 5);
           // Retry times to check if errors found, set to -1 if want to retry endlessly
           int maxRetryCount = Tools.getInt( props.getProperty("sms.read.retry","3"), 3);
           int retry=0;
           isConnected=true;
           String lastErrorMsg=null;
           while ( (retry <=maxRetryCount ||maxRetryCount <0)  && running){
               lastErrorMsg=null; // clear previous error
               try{
                   MessageBundle mb= readFromChannel();
                   if ( mb !=null && mb.size()>0){
                       logger.debug("ChannelReader get message count "+ mb.size());
                       for ( Iterator it=mb.iterator();it.hasNext();){
                           Message msg=(Message)it.next();
                           Message ret= inputChain==null?msg: inputChain.duplicate().doFilter(msg);
                           if (ret !=null && ret.getStatus() != Message.DISCARD){
                               msg.setStatus(Message.RECIEVED);
                               logMessage(ret);
                               fireMessageArrived( ret);
                           }else{
                               if (ret !=null){
                                   logMessage(ret);
                               }else{
                                   msg.setStatus(Message.DISCARD);
                                   msg.setProperty("info", "set to null after filter");
                                   logMessage(msg);
                               }
                           }
                       }
                   }
                   if(running) Thread.sleep( interval * 1000 );
               }catch(Exception e){
                   logger.error("Error reading channel:"+ e, e);
                   lastErrorMsg= e.getMessage();
                   retry ++;
               }
           }//end while
           logger.debug("ChannelReader stopped ");
           running= false;
           isConnected=false;
           tearDownConnection();
           // notify all
           ConnectionEvent ce=new ConnectionEvent(cmodel,lastErrorMsg == null? ConnectionEvent.CONNECTION_CLOSED:ConnectionEvent.CONNECTION_ERRORED ,lastErrorMsg);
           fireConnectionClosed(ce);
       }
       public void start() throws Exception{
           run();
       }
       public void stop() throws InterruptedException{
           running=false;
           //@todo better wait until the reader thread stopped
       }
       public boolean isStarted(){
           return running;
       }
   }
   /**
    * @roseuid 404A98FC035D
    */
   public ConnectionChannel()
   {

   }
   protected void checkConnection()  throws ConnectionFailedException{
   	logger.debug("checkConnection");
   }
   /**
    * Start channel, if channel has already started, do nothing
    * after connect successfully, ConnectionEvent will be dispatched.
    */
   public void connect() throws ConnectionFailedException{
       try{
           if ( channelReader !=null && channelReader.isStarted()){
               // reader started already
           	checkConnection();
           }
           setupConnection();
           channelReader= new ChannelReader();
           Thread t=new Thread(channelReader);
           t.setDaemon(true);
           t.start();
           isConnected=true;
           ConnectionEvent ce=new ConnectionEvent(cmodel, ConnectionEvent.CONNECTION_OPENED);
           fireConnectionEstablished(ce);
       }catch(Exception e){
           logger.error("Could not connect", e);
           throw new ConnectionFailedException( "Could not connect", e);
       }
   }
   public int disconnect(){
       try{
           if ( channelReader !=null && channelReader.isStarted()){
               channelReader.stop();
               // only when reader stopped completely, will ConnectionEvent be sent
               // so no more setting here
           }
       }catch(Exception e){
       }
       /* this will be set in channelReader */
       //isConnected=false;
       return 0;
   }
   public boolean isConnected(){
       return isConnected;
   }
   /**
    * Periodically this method will be called
    * @return MessageBundle
    * @roseuid 404A7B580355
    */
   protected MessageBundle readFromChannel()
   {
       logger.debug("readFromChannel");
       return null;
   }
   protected  void write(Message msg){
       // do things should be implemented in subclass
       logger.debug("write");
   }
   /**
    * Will be called before open channel reader during #connect
    * you can override this to establish connection here.
    */
   protected void setupConnection() throws ConnectionFailedException{
       logger.debug("setupConnection");

   }
   /**
    * Will be called after channel reader stopped
    * You can override this to logout or close folder watcher here.
    */
   protected void tearDownConnection(){
       logger.debug("tearDownConnection");
   }

}
