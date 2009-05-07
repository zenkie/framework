//Source file: F:\\work\\sms\\src\\nds\\connection\\MessageListener.java

package nds.connection;

import java.util.EventListener;

public interface MessageListener   extends EventListener
{

   /**
    * @param msg
    * @roseuid 40468CB00385
    */
   public void onMessageArrived(Message msg);
}
