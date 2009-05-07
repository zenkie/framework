//Source file: F:\\work\\sms\\src\\nds\\connection\\AbstractConnection.java

package nds.connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.event.EventListenerList;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.Tools;
import nds.util.Validator;
/**
 * include more filters using 'filters.additional'
 */
public abstract class AbstractConnection implements Connection
{
   protected Logger logger= LoggerManager.getInstance().getLogger(this.getClass().getName());
   protected EventListenerList listenerList = new EventListenerList();
   /**
    * filter income messages
    */
   protected FilterChain inputChain=null;
   /**
    * filter output messages
    */
   protected FilterChain outputChain=null;
   protected Properties props;
   protected ConnectionModel cmodel;
   /**
    * @roseuid 404A978B00A4
    */
   public AbstractConnection()
   {

   }

   /**
    * Notify message listener to handle this message
    * @param msg
    * @roseuid 4048BD5B0010
    */
   protected void fireMessageArrived(Message msg)
   {
       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if ( MessageListener.class==(Class)listeners[i]) {
               ((MessageListener) listeners[i + 1]).onMessageArrived(msg);
           }
       }

   }
   protected void fireMessageToLog(Message msg)
   {

       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if ( MessageLogListener.class==listeners[i]) {
               ((MessageLogListener) listeners[i + 1]).onLogMessage(msg);
           }
       }

   }

   /**
    * @param e
    * @roseuid 4048BD7B00A1
    */
   protected void fireConnectionEstablished(ConnectionEvent event)
   {
       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if ( listeners[i]==ConnectionListener.class) {
               ((ConnectionListener) listeners[i + 1]).connectionEstablished(event);
           }
       }

   }

   /**
    * @param e
    * @roseuid 4048BD9C02CE
    */
   protected void fireConnectionClosed(ConnectionEvent event)
   {
       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if (listeners[i]==ConnectionListener.class) {
               ((ConnectionListener) listeners[i + 1]).connectionClosed(event);
           }
       }

   }
   /**
    * @return listeners of specified class
    */
   protected Collection getListeners(Class cls){
       Object[] listeners = listenerList.getListenerList();

       ArrayList al=new ArrayList();
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if (listeners[i] == cls) {
               al.add(listeners[i]);
           }
       }
       return al;
   }
   /**
    * Log message when its state changed, such as discard, sent, init
    * Default log to Logger, can be redirected to database
    * @param msg
    * @roseuid 40493633016A
    */
   protected void logMessage(Message msg)
   {
       fireMessageToLog(msg);
   }

   /** Message may be modified after written
    * For instance , msg status will be changed to DISCARD if found error
    * or sender will be appended by more string after channel
    * @param msg
    * @return int
    * @roseuid 404A7A790084
    */
   protected abstract void write(Message msg);


   /**
    * @param String name
    * @param String value
    * @roseuid 404A978B0387
    */
   public void setProperty(String name,  String value)
   {
       props.setProperty(name,value);
   }

   /**
    * @param String name
    * @param String defaultValue
    * @return String
    * @roseuid 404A978C0111
    */
   public String getProperty( String name,  String defaultValue)
   {
       return props.getProperty(name,defaultValue );
   }

   /**
    * @return Hashtable
    * @roseuid 404A978C02A0
    */
   public Properties getProperties()
   {
       return props;
   }

   /**
    * @param props sample
    * sms.filters.input=A,B,C
    * sms.filter.A.class=nds.sms.filter.Someclass
    * sms.filter.A.maxCount=10
    * sms.filter.A.minCount=1
    * sms.filter.B.class=nds.sms.filter.ClassB
    * sms.filter.B.sender=yfzhu
    * sms.fitlers.output=D,E,F
    *
    * @roseuid 404A978C0304
    */
   public void init(Properties props)
   {
       if(this.props==null) this.props=new Properties();
       this.props.putAll(props);
       
       // initilize connection model
       int port =-1;
       port= Tools.getInt(props.getProperty("sms.net.port","-1"), -1);
       this.cmodel =new ConnectionModel( props.getProperty("sms.net.host","localhost"),port );
/*
       ArrayList inputFilters=new ArrayList(), outputFilters=new ArrayList() ;
       //load default chain filters, seperated by comma
       initFilters( props.getProperty("sms.filters.input"), inputFilters, conf);
       initFilters(props.getProperty("sms.filters.output"), outputFilters,conf);
       inputChain = new FilterChain(inputFilters);
       outputChain= new FilterChain(outputFilters);
*/
   }
   public void setupInputChain(String filters,Configurations conf ){
   		ArrayList f=new ArrayList();
   		initFilters(filters, f, conf);
   		inputChain = new FilterChain(f);
   }
   public void setupOutputChain(String filters, Configurations conf){
		ArrayList f=new ArrayList();
   		initFilters(filters, f, conf);
   		outputChain = new FilterChain(f);
   	
   }
   /**
    * @param filters sample "A,B,C"
    * @param fList contains FilterHolder
    */
   private void initFilters(String filters, ArrayList fList, Configurations conf){
       if (Validator.isNull(filters)) return;
       MessageFilter filter;
       String sf;
       StringTokenizer st= new StringTokenizer(filters,",");
       while(st.hasMoreTokens()){
           sf = st.nextToken();
           logger.debug("loading "+ props.getProperty("sms.filter."+ sf+".class") );
           FilterHolder fh=new FilterHolder(sf, props.getProperty("sms.filter."+ sf+".class"));
           fh.setInitParameters( conf.getConfigurations("sms.filter."+sf).getProperties());
           try{
               fh.start();
               fList.add(fh);
           }catch(Exception e){
               logger.error("Can not start filter " + e,e );
           }
       }

   }
   /**
    * @return int None-Zero value means error
    * @roseuid 404A978D0048
    */
   public abstract void connect() throws ConnectionFailedException;

   /**
    * @return int None-Zero value means error
    * @roseuid 404A978D00B6
    */
   public abstract int disconnect();

   /**
    * Will do filter first, if message is not null after then
    * will be sent by write, and then do log
    * @param msg
    * @roseuid 404A978D0124
    */
   public void sendMessage(Message msg){
       // each time filtering, the chain will be constructed as new (internal pointer will be set to 0)
       Message ret= outputChain==null?msg: outputChain.duplicate().doFilter(msg);
       if (ret !=null && ret.getStatus() != Message.DISCARD){
           this.write(ret);
       }
       // message may be changed and set status code inside
       if(ret!=null)this.logMessage(ret);
       else{
           msg.setStatus(Message.DISCARD);
           msg.setProperty("info", "set to null after filter");
           this.logMessage(msg);
       }

   }
   /**
    * Add listener for message log
    * @param l
    */
   public void addMessageLogListener(MessageLogListener l){
       listenerList.add(MessageLogListener.class, l);
   }

   public void removeMessageLogListener(MessageLogListener l){
       listenerList.remove(MessageLogListener.class, l);
   }

   /**
    * @param listener
    * @roseuid 404A978D0263
    */
   public void addConnectionListener(ConnectionListener l)
   {
       listenerList.add(ConnectionListener.class, l);
   }

   /**
    * @param listener
    * @roseuid 404A978D03AD
    */
   public void removeConnectionListener(ConnectionListener l)
   {
       listenerList.remove(ConnectionListener.class, l);
   }

   /**
    * @param lsnr
    * @roseuid 404A978E0119
    */
   public void addMessageListener(MessageListener l)
   {
       listenerList.add(MessageListener.class, l);
   }

   /**
    * @param lsnr
    * @roseuid 404A978E0277
    */
   public void removeMessageListener(MessageListener l)
   {
       listenerList.remove(MessageListener.class, l);
   }

   /**
    * @roseuid 404A978E03DE
    */
   public abstract boolean isConnected();

   /**
    * @return ConnectionModel
    * @roseuid 404A978F006E
    */
   public ConnectionModel getConnectionModel()
   {
       return cmodel;
   }
   /**
    * @remove all listeners and stop connection
    */
   protected void destroy(){
       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if (listeners[i] == MessageLogListener.class) {
               ((MessageLogListener)listeners[i+1]).destroy();
           }
           listenerList.remove((Class)listeners[i],(EventListener)listeners[i+1] );
       }
       disconnect();
   }
}
