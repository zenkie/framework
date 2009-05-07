//Source file: F:\\work\\sms\\src\\nds\\connection\\Message.java

package nds.connection;

import java.util.Date;

public interface Message extends java.io.Serializable
{
    /**
     * Being constructed.
     */
    public final static int INITIAL=1;
    /**
     * in filter chain or sending out.
     */
    public final static int PROCESSING=2;
    /**
     * sending out.
     */
    public final static int SENT=3;
    /**
     * recieved.
     */
    public final static int RECIEVED=4;
    /**
     * Invalid message, and discard, you can retrieve more information
     * from #getProperty("info") when finding message discard.
     */
    public final static int DISCARD=5;
   /**
    * @return String
    * @roseuid 4047D4EB0155
    */
   public String getReceiver();


   public Object getProperty(String name);
   
   public String getProperty(String name, String defaultValue);

   public void setProperty(String name, String value);

   /**
    * @param s
    * @roseuid 4047D5430061
    */
   public void setReceiver(String s);

   /**
    * @return String
    * @roseuid 4047D4FD0265
    */
   public String getContent();

   /**
    * @param c
    * @roseuid 4047D5060398
    */
   public void setContent(String c);

   /**
    * @return String
    * @roseuid 4047D51601C3
    */
   public String getMsgID();

   /**
    * @return Date
    * @roseuid 4047D51F0179
    */
   public Date getCreationDate();

   /**
    * @param d
    * @roseuid 4047D55E0085
    */
   public void setCreationDate(Date d);

   /**
    * @return String
    * @roseuid 4047D5260162
    */
   public String getSender();

   /**
    * @param s
    * @roseuid 4047D554031D
    */
   public void setSender(String s);

   /**
    * message valid time in hours
    * 
    * You can add filter to processor, to certify message validation. For instance,
    * DurationFilter will check this one with creation time, if message created too long,
    * will discard this message
    * 
    * @param du int in hours
    */
   public void setDuration(int  seconds);

   /**
    * Message valid duration hours after creation date, return -1 if not specified by
    * #setDuration
    * @return in hours after creation date set by #setCreationDate
    */
   public int getDuration();
   /**
    * @return String
    * @roseuid 4047F62A0092
    */
   public String toXML();

   /**
    * @return String
    * @roseuid 4047FA940281
    */
   public String getParentMessageID();

   /**
    * @return String
    * @roseuid 40489B4102B8
    */
   public String getType();

   /**
    * @return int
    * @roseuid 4048D7B003E0
    */
   public void setStatus(int status);

   /**
    * @return int
    * @roseuid 4048D7B90158
    */
   public int getStatus();
}
