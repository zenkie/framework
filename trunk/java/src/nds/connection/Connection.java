//Source file: F:\\work\\sms\\src\\nds\\connection\\Connection.java

package nds.connection;

import java.util.Properties;

public interface Connection
{

   /**
    * @param String name
    * @param String value
    * @roseuid 40468AAB0055
    */
   public void setProperty( String name,  String value);

   /**
    * @param String name
    * @param String defaultValue
    * @return String
    * @roseuid 40468AD802D8
    */
   public String getProperty(String name, String defaultValue);

   /**
    * @return Hashtable
    * @roseuid 40468AEF0306
    */
   public Properties getProperties();

   /**
    * @param props
    * @roseuid 4047DB9802D2
    */
   public void init(Properties props);

   /**
    * @roseuid 404688BA01F6
    */
   //public void connect(ConnectionModel cmodel) throws ConnectionFailedException;

   /**
    * Will contruct a new ConnectionModel according to properties
    */
   public void connect() throws ConnectionFailedException;

   /**
    * @return int 0 for ok
    * @roseuid 40468B5E0010
    */
   public int disconnect();

   /**
    * @param msg
    * @roseuid 4046E50C009C
    */
   public void sendMessage(Message msg);

   /**
    * @param listener
    * @roseuid 4046D1690352
    */
   public void addConnectionListener(ConnectionListener listener);

   /**
    * Add listener for message log
    * @param l
    */
   public void addMessageLogListener(MessageLogListener l);

   public void removeMessageLogListener(MessageLogListener l);
   /**
    * @param listener
    * @roseuid 4046D183036D
    */
   public void removeConnectionListener(ConnectionListener listener);

   /**
    * @param lsnr
    * @roseuid 4046E7F2032D
    */
   public void addMessageListener(MessageListener lsnr);

   /**
    * @param lsnr
    * @roseuid 4046E803037B
    */
   public void removeMessageListener(MessageListener lsnr);

   /**
    * @return boolean
    * @roseuid 40489E4A0156
    */
   public boolean isConnected();

   /**
    * @return ConnectionModel
    * @roseuid 40489E9101D0
    */
   public ConnectionModel getConnectionModel();
}
