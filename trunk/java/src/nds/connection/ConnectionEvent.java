//Source file: F:\\work\\sms\\src\\nds\\connection\\ConnectionEvent.java

package nds.connection;


public class ConnectionEvent extends java.util.EventObject
{
   /**
    * Opened
    */
   public static int CONNECTION_OPENED = 4;
   /**
    * Closed normally, if close with error, status will be CONNECTION_CLOSED
    */
   public static int CONNECTION_CLOSED = 2;
   /**
    * Closed with error
    */
   public static int CONNECTION_ERRORED = 3;
   private String errormsg;
   private int status;
   private ConnectionModel source;

   /**
    * @roseuid 404A97E903A0
    */
   public ConnectionEvent(ConnectionModel cm, int status)
   {
       super(cm);
       this.source= cm;
       this.status = status;
   }
   public ConnectionEvent(ConnectionModel cm, int status, String errorMsg)
   {
       super(cm);
       this.source= cm;
       this.status = status;
       this.errormsg=errorMsg;
   }

   /**
    * @return String
    * @roseuid 4046CECA02B9
    */
   public String getErrorMessage()
   {
    return null;
   }

   /**
    * @return String
    * @roseuid 4046CECE0120
    */
   public String getHostName()
   {
       return source.getHostName();
   }

   /**
    * @return int
    * @roseuid 4046CEDF0000
    */
   public int getPort()
   {
    return source.getPort();
   }

   /**
    * @return int
    * @roseuid 4046CEEC021B
    */
   public int getStatus()
   {
      return status;
   }
   public String getStatusDescription(){
       String desc="unknown";
       switch(status){
           case 2:
               desc= "close";break;
           case 4:
               desc= "open";break;
           case 3:
               desc="error";break;
       }
       return desc;
   }
   public boolean isOpen(){
       return status== CONNECTION_OPENED;
   }

   /**
    * @return boolean
    * @roseuid 4046CEF7027E
    */
   public boolean isError()
   {
       return status== CONNECTION_ERRORED;
   }

   /**
    * @return nds.connection.ConnectionModel
    * @roseuid 4046D01C02E3
    */
   public ConnectionModel getConnectionModel()
   {
       return source;
   }
   public String toString(){
       return "server:"+ getHostName()+ ",status:"+ getStatusDescription();
   }

}
