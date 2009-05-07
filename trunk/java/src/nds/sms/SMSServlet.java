//Source file: F:\\work\\sms\\src\\nds\\sms\\SMSServlet.java

package nds.sms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.connection.MessageBundle;
import nds.control.util.ValueHolder;
import nds.control.web.RequestProcessor;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.ParamUtils;
import nds.util.WebKeys;

/**
 * A simple servlet to handle sms request including send and recieve, you can call this using
 * url like "http://host/web/servlet?action=get&wait=true" to get xml message bundle
 * or "http://host/web/servlet?action=send&message=13061613691hello" to send short message,
 * for more complicate messages, put it in a form to post

          parameters set in request are:
           action - 'send' | 'get' |'init'
           wait - 'true' | 'false', valid when action='get', if wait is set to true, then
                  http will be blocked until at least one message retrieved.
           message - short message toXML() if action='send'
           response header : contains "sms.code" if none zero, errors found, the output get the error content
           when action='get', the out content will be messagebunlde.toxml()
 *
 */
public class SMSServlet extends HttpServlet {

        private Logger logger=LoggerManager.getInstance().getLogger(SMSServlet.class.getName());

        private static final String GET_USER_CODE="select usercode from smsclient where userid=?";

        private static final String CONTENT_TYPE = "text/html; charset=GB2312";
        /**Initialize global variables*/
        public void init() throws ServletException {}

        public  void doPost(HttpServletRequest request, HttpServletResponse  response)
        throws IOException, ServletException {
            doGet(request, response);
        }

        /**Process the HTTP Get request
          parameters set in request are:
           smsaction - 'send' | 'get'|'init'

           wait - 'true' | 'false', valid when action='get', if wait is set to true, then
                  http will be blocked until at least one message retrieved.
           message - short message toXML() if action='send'
           response header : contains "sms.code" if none zero, errors found, the output get the error content
           when action='get', the out content will be messagebunlde.toxml()

         */
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            HttpSession session = request.getSession(true);

            response.setContentType(CONTENT_TYPE);
            PrintWriter out = response.getWriter();

            //User ID
            int uid = -1;
            UserWebImpl user=null;
            try {
                // authenticate user
                nds.control.util.EJBUtils.authenticate(request);
                user = (UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER);
                uid = user.getUserId();
            } catch(Exception e) {
                //e.printStackTrace();
            }
            if(uid == -1){
                response.setHeader("sms.code", "1"); // none zero for error
                out.print("Please login first.");
                return;
            }

            SMProcessor processor= (SMProcessor)WebUtils.getServletContextManager().getActor( WebKeys.SM_PROCESSOR );

            String action= request.getParameter("smsaction");

            if ( "get".equals(action)){
                // if wait ,then will wait until as least one message retrieved
                boolean wait= ParamUtils.getBooleanParameter(request, "wait");
                MessageBundle bd=processor.getMessages(uid, wait);
                response.setHeader("sms.code", "0"); // none zero for error
                out.print(bd.toXML());
            }else if( "send".equals(action)){
                // send
                String msg= request.getParameter("message");
                try{
                    // override msg's sender using connection information, so can prevent
                    // some stleathy client from peculating account of others
                    ShortMessage smg= ShortMessage.parse(msg , getUserCode(user));
                    processor.sendMessage(smg);
                }catch(Exception e){
                    logger.error("Could not parse short message:" + msg , e);
                    response.setHeader("sms.code", "3");
                    out.print("Could not parse to short message:\n" +msg );
                }
                response.setHeader("sms.code", "0"); // none zero for error
            }else if ("init".equals(action)){
                response.setHeader("sms.code", "0");
                out.print("OK");
            }else{
                // forward to main servlet
                try {
                    ValueHolder vh=getRequestProcessor().processRequest(request);
                    if(vh.get("code") !=null)response.setHeader("sms.code",""+vh.get("code"));
                    out.print(vh.get("message"));
                } catch (Exception ex){
                    logger.error("Error handling request", ex);
                    response.setHeader("sms.code", "2"); // none zero for error
                    out.print(ex.getLocalizedMessage() );
                }
            }

        }
        /**
         * Try fetch user property "code" from UserWebImpl, if not found, fetch value from
         * database.
         */
        private String getUserCode(UserWebImpl user) throws Exception{
            return SMDBUtils.getUserCode(user.getUserId());
        }
        private RequestProcessor getRequestProcessor() {
            return (RequestProcessor)WebUtils.getServletContextManager().getActor(WebKeys.REQUEST_PROCESSOR);
        }

}
