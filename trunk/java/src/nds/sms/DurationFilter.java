package nds.sms;
import java.util.Properties;

import nds.connection.FilterChain;
import nds.connection.Message;
import nds.connection.MessageFilter;
import nds.util.Tools;
/**
 * Filter input message which is wait in queue too long time.
 * Maximum time duration can be set by "duration" in properties, in hour unit
 * 
 * Message#setDuration can override this property(values not equal to -1)
 */
public class DurationFilter implements MessageFilter{
    private int durationHour= 24;// default to one day
    public DurationFilter() {
    }
    /**
     * @param request
     * @param chain
     * @return the new composite message or null if filter decide that
     * this message should be erased and not send/recieve
     * @roseuid 4048C3ED00BF
     */
    public Message doFilter(Message request, FilterChain chain){
        long t= request.getCreationDate().getTime();
        int du= request.getDuration();
        if(du<0) du= durationHour; 
        if (  (System.currentTimeMillis()- t)/ (1000*3600) >=du ){
            // over time
            return null;
        }
        // do next filter
        return chain.doFilter(request);
    }

    /**
     * @param props need "duration", in hour unit.
     */
    public void init(Properties props){
        durationHour= Tools.getInt(props.getProperty("duration", "24"), 24);
    }


    /**
     * Do nothing
     */
    public void destroy(){

    }

    /**
     * OUT_FILTER, IN_FILTER, DUPLEX_FILTER
     * @return int
     * @roseuid 4048D5C500D7
     */
   public int getDirection(){return IN_FILTER;}
}
