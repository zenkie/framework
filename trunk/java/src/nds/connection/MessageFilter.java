//Source file: F:\\work\\sms\\src\\nds\\connection\\MessageFilter.java

package nds.connection;

import java.util.Properties;

public interface MessageFilter
{
   public static int OUT_FILTER = 1;
   public static int IN_FILTER = 2;
   public static int DUPLEX_FILTER = 3;

   /**
    * @param request
    * @param chain
    * @return the new composite message or null if filter decide that
    * this message should be erased and not send/recieve
    * @roseuid 4048C3ED00BF
    */
   public Message doFilter(Message request, FilterChain chain);

   /**
    * @param props
    * @roseuid 4048C48600D3
    */
   public void init(Properties props);


   /**
    * @roseuid 4048CB7A031D
    */
   public void destroy();

   /**
    * OUT_FILTER, IN_FILTER, DUPLEX_FILTER
    * @return int
    * @roseuid 4048D5C500D7
    */
   public int getDirection();
}
