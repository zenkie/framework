//Source file: F:\\work\\sms\\src\\nds\\sms\\SMHttpChannel.java

package nds.sms;

import java.io.IOException;

import nds.connection.ConnectionFailedException;
import nds.connection.Message;
import nds.connection.MessageBundle;
import nds.util.Tools;

import org.xml.sax.SAXException;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * 发送message 用 http://server/nds/servlet?action=send&msg=MessageBundle.toXML()
 * 接收message 用
 * http://server/nds/sms.jsp?command=HandleSMS?action=get
 *
 * 返回的msg为MessageBundle.toXML()
 */
public class SMHttpChannel extends ConnectionChannel
{

    private WebConversation wc ;
    /**
     * Periodically this method will be called
     * @return MessageBundle nerver be null
     */
    protected synchronized MessageBundle readFromChannel(){

        String urlget= props.getProperty("sms.net.url", "http://localhost:8080/sms/server")+"?action=get";
        try {

            WebResponse res= wc.getResponse(urlget);
            if(res !=null){
                int code= Tools.getInt(res.getHeaderField("sms.code"), -1);
                String content=  res.getText();
                if( code ==0){
                    // ok
                    MessageBundle b =  MessageBundle.parse(content);
                    return b;
                }else{
                    logger.debug("error retrieving sms from url "+ urlget + ", reponse(code="+ code+"):"+ content);
                }
            }else{
                logger.error("fail to get response from url:"+ urlget);
            }

        }catch (Exception ex) {
            logger.error("Fail to get sms from http url:"+urlget, ex);
        }
        return MessageBundle.EMPTY;

    }
    /**
     * Will be called before open channel reader during #connect
     * you can override this to establish connection here.
     */
    protected void setupConnection() throws ConnectionFailedException{
        wc = new WebConversation();
        String userName= props.getProperty("sms.net.username", "nds");
        String passwd= props.getProperty("sms.net.password");
        String urlinit= props.getProperty("sms.net.url", "http://localhost:8080/sms/server")+"?action=init";

        wc.setAuthorization(userName, passwd);

        try {
            WebResponse res= wc.getResponse(urlinit);
            if(res !=null){
                int code= Tools.getInt(res.getHeaderField("sms.code"), -1);
                String content=  res.getText();
                if( code !=0){
                    // error
                    throw new ConnectionFailedException("Fail to init http connection:"+ content);
                }
            }else{
                throw new ConnectionFailedException("Fail to get response from url:"+ urlinit);
            }
        }catch (SAXException ex) {
            throw new ConnectionFailedException("Internal error:" + ex, ex);
        }catch (IOException ex) {
            throw new ConnectionFailedException("Internal error:" + ex, ex);
        }

    }
    /**
     * Will be called after channel reader stopped
     * You can override this to logout or close folder watcher here.
     */
    protected void tearDownConnection(){
        logger.debug("tearDownConnection");
   }
   /**
    * Write message to http connection
    *
    */
   protected  void write(Message msg){
       String urlput= props.getProperty("sms.net.url", "http://localhost:8080/sms/server");
       try {
           WebRequest req= new PostMethodWebRequest(urlput);
           req.setParameter("action", "send");
           req.setParameter("message", msg.toXML());
           WebResponse res= wc.getResponse(req);
           if(res !=null){
               int code= Tools.getInt(res.getHeaderField("sms.code"), -1);
               String content=  res.getText();
               if( code ==0){
                   // ok
                    msg.setStatus(Message.SENT);
                   return ;
               }else{
                   msg.setStatus(Message.DISCARD);
                   logger.debug("error write sms to url "+ urlput + ", reponse(code="+ code+"):"+ content);
               }
           }else{
               msg.setStatus(Message.DISCARD);
               logger.error("fail to send sms to url:"+ urlput);
           }

       }catch (Exception ex) {
           msg.setStatus(Message.DISCARD);
           logger.error("Fail to send sms to http url:"+urlput, ex);
       }

   }

}
