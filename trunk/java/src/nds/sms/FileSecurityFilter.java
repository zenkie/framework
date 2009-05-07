package nds.sms;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import nds.connection.FilterChain;
import nds.connection.Message;
import nds.connection.MessageFilter;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSRuntimeException;
import nds.util.Tools;
/**
 * Filter for valid phone number, line by line stored in file specified by property "file",
 * direction set in "direction", "output" for message sender filter, "input" for receiver filter
 *
 * Currently the mechanism does not support rejection, only allowance supported
 * I think if rejection is needed, one more property should be provide: "mechanism" ( "deny"| "permit")
 */
public class FileSecurityFilter implements MessageFilter{
    private static Logger logger=LoggerManager.getInstance().getLogger(FileSecurityFilter.class.getName());
    private HashMap users; // key : user phone number( string), value : this
    private boolean isInput; // true for input, false for output
    public FileSecurityFilter() {
        users=new HashMap();
    }
    /**
     * @param request
     * @param chain
     * @return the new composite message or null if filter decide that
     * this message should be erased and not send/recieve
     * @roseuid 4048C3ED00BF
     */
    public Message doFilter(Message request, FilterChain chain){
        String usr;
        // if isInput set to true, then all incoming message will be checked for valid sender
        if( isInput) usr= request.getSender();
        else usr= request.getReceiver();
        // must exists in users, for permission
        if ( users.get(usr)==null){
            return null;
        }else
            return chain.doFilter(request);

    }

    /**
     * @param props
     sms.filter.A.direction=input
     sms.filter.A.file=e:/aic/sms/allow.txt
     *
     */
    public void init(Properties props){

        String d=  props.getProperty("direction", "input");
        if( "input".equalsIgnoreCase(d)) isInput= true;
        else isInput=false;

        logger.debug("direction= "+ d);
        String fileName=  props.getProperty("file");
        if ( fileName ==null) throw new NDSRuntimeException("Not found 'file' property for FileSecurityFilter");
        logger.debug("file= "+ fileName);
        
        String body= Tools.getFileContent(fileName);
        StringTokenizer st=new StringTokenizer(body);
        while( st.hasMoreTokens()){
            String no= st.nextToken();
            logger.debug("allowed for "+ no);
            users.put(no, this);
        }

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
   public int getDirection(){return DUPLEX_FILTER;}
}
