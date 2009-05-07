//Source file: F:\\work\\sms\\src\\nds\\sms\\SMHttpChannel.java

package nds.sms;

import java.io.IOException;

import nds.connection.ConnectionFailedException;
import nds.connection.Message;
import nds.connection.MessageBundle;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.StringUtils;
import nds.util.Tools;

import org.xml.sax.SAXException;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import cn.net.emay.metone.api.Client;
import cn.net.emay.metone.api.MO;

import java.util.*;
/**
 * Channel that provided by EMay
 */
public class EMayChannel extends ConnectionChannel
{
	int smsLength=70 ; // default length for splitting
	Client mtClient;
    /**
     * Periodically this method will be called
     * @return MessageBundle nerver be null
     */
    protected synchronized MessageBundle readFromChannel(){
    	/**
    	 * Not implemented yet (2006-05-10)
    	 */
        return MessageBundle.EMPTY;
    }
    /**
     * Will be called before open channel reader during #connect
     * you can override this to establish connection here.
     */
    protected void setupConnection() throws ConnectionFailedException{
        
        String userName= props.getProperty("sms.net.username", "nds");
        String passwd= props.getProperty("sms.net.password");
    	mtClient = new Client(userName);
    	if(!mtClient.registEx(passwd) ) throw new ConnectionFailedException("Could not setup connection to EMay server for " + userName);
    	if( mtClient.getBalance()==0)  throw new ConnectionFailedException("No balance for " + userName);
    	
   		smsLength=Tools.getInt(props.getProperty("sms.emay.length"),70);
    }
    /**
     * Will be called after channel reader stopped
     * You can override this to logout or close folder watcher here.
     */
    protected void tearDownConnection(){
    	mtClient.logout();
        logger.debug("tearDownConnection");
   }
    public boolean isConnected(){
        return mtClient!=null && mtClient.getBalance()!=0;
    }
    protected void checkConnection()  throws ConnectionFailedException{
    	logger.debug("Check connection");
    	if (!isConnected()){
    		setupConnection();
    	}
    }
    /**
     * Split and send, each sms should be less than 70 char
     * @param receiver
     * @param content
     * @return
     */
    private boolean sendSMS(String receiver, String content){
    	if( content==null || receiver ==null ) return false;
    	
    	List substrs= splitString(content,smsLength,"]","#" );
    	boolean b=true;
    	for(int i=0;i<substrs.size();i++){
    		b= b && mtClient.sendSMS(new String[]{receiver}, (String)substrs.get(i));
    	}
    	return b;
    }
    /**
     * Split s into length limited sub string, for one sub string, it will start 
     * with prefix, and ended with suffix, for instance, 
     * split("abcd123456", 5, "]", "#") will be
     * {"1]ab#", "2]cd#","3]12#","4]34#","5]56#"}
     *  
     * @param s the string to be splitted
     * @param len max length for one sub string  
     * @param prefix for second sub string and following ones, will set after the number index
     * @param suffix if substring not ended, will append this string as notice.
     * @return elements are string 
     */
    private static List splitString(String s, int len, String prefix, String suffix){
    	ArrayList al=new ArrayList();
    	int strLen= s.length();
    	int cnt = 1;
    	int idx=0;
    	int subEndIdx; 
    	StringBuffer sub;
    	if(strLen<=len){
    		al.add(s);
    	}else{
    	while(true){
	    	sub= new StringBuffer(cnt+prefix);
	    	subEndIdx = idx + len - sub.length();
	    	if(subEndIdx < strLen) {
	    		subEndIdx = subEndIdx - suffix.length();
	    		// if endidx = idx, then the string will no longer be splitted
	    		if(subEndIdx== idx) throw new NDSRuntimeException("Length too short");
	    		sub.append( s.substring(idx,subEndIdx )).append(suffix);
	        	al.add(sub.toString());
	    	}else{
	    		sub.append( s.substring(idx,strLen ));
	        	al.add(sub.toString());
	        	break;
	    	}
	    	idx = subEndIdx;
	    	cnt ++;
    	}
    	}
    	return al;
    }
    public static void main(String[] s) throws Exception{
    	System.out.println(Tools.toString(splitString("abcd123456789123456789123456789123456789", 5, "]", "#").toArray()));
    	System.out.println(Tools.toString(splitString("abcd123456", 9, "]", "#").toArray()));
    }
   /**
    * Write message to http connection
    *
    */
   protected  void write(Message msg){
   	//logger.debug("Sending sms to "+ msg.getReceiver() + ": "+StringUtils.shorten(msg.getContent(),20));
   	
       	   if(sendSMS(msg.getReceiver(), msg.getContent())){	
                // ok
       	   		msg.setStatus(Message.SENT);
           }else{
           		if(mtClient.getBalance()==0 || mtClient.getEachFee()==0){
           			throw new NDSRuntimeException("No balance or connection closed.");
           		}
           		logger.error("Fail to send sms to "+ msg.getReceiver() + ": "+StringUtils.shorten(msg.getContent(),20));
                msg.setStatus(Message.DISCARD);
           }

   }

}
