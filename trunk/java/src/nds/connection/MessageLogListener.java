//Source file: F:\\work\\sms\\src\\nds\\connection\\MessageListener.java

package nds.connection;

import java.util.EventListener;
import java.util.Properties;

public interface MessageLogListener   extends EventListener
{

   /**Log message info
    * @param msg
    * @roseuid 40468CB00385
    */
   public void onLogMessage(Message msg);
   /**
    * @param props
    * @roseuid 404A98FC003D
    */
   public void init(Properties props);

   /**
    * No exceptio should be raised
    * @roseuid 404A98FC00AB
    */
   public void destroy();

}
