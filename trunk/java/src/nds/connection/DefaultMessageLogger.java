//Source file: F:\\work\\sms\\src\\nds\\connection\\MessageLogger.java

package nds.connection;

import java.util.Properties;

import nds.log.Logger;
import nds.log.LoggerManager;
/**
 * Log message, such as to file or to database
 */
public class DefaultMessageLogger implements MessageLogListener
{
    private static Logger logger= LoggerManager.getInstance().getLogger(DefaultMessageLogger.class.getName());

   /**
    * @param msg
    * @roseuid 404937F3024B
    */
   public void onLogMessage(Message msg){
       logger.info(msg.toString());
   }
   public void init(Properties props){}

   /**
    * @roseuid 404A98FC00AB
    */
   public void destroy(){}


}
