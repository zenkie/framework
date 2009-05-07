/******************************************************************
*
*$RCSfile: LoginRequestHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: LoginRequestHandler.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.5  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.4  2001/11/29 00:48:36  yfzhu
*no message
*
*Revision 1.3  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.web.reqhandler;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.B64Code;
import nds.util.StringUtils;

/**
 * Handle login information from web, will append remote address to the Event, so
 * server side can log more detailed information about client
 */
public class LoginRequestHandler extends RequestHandlerSupport {
    private static Logger logger=LoggerManager.getInstance().getLogger(LoginRequestHandler.class.getName());
    public LoginRequestHandler() {}
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
        WebUtils.getSessionContextManager(request.getSession(true));
        DefaultWebEvent event=new DefaultWebEvent("LoginEvent");


        //yfzhu 2004-05-10 add attribute support, and parameter take precedence of attribute
        // so attribute first and parameter second

        // check request header 'Authorization'
        String credentials =request.getHeader("Authorization");
        if (credentials!=null)
          {
              try
              {
                  credentials = credentials.substring(credentials.indexOf(' ')+1);
                  credentials = B64Code.decode(credentials,StringUtils.ISO_8859_1);
                  int i = credentials.indexOf(':');
                  String username = credentials.substring(0,i);
                  String password = credentials.substring(i+1);
                  event.setParameter("j_username",username);
                  event.setParameter("j_password",password);
              }
              catch (Exception e)
              {
                  logger.error("Error parsing credentials::"+ credentials, e );
              }
          }

/*        Enumeration enum=request.getAttributeNames();
        while(enum.hasMoreElements()) {
            String name=(String) enum.nextElement();
            Object v= request.getAttribute(name);
            if( v instanceof String)     event.setParameter(name, (String)v);
        }*/
        Enumeration enu= request.getParameterNames();
        while(enu.hasMoreElements()) {
            String name=(String) enu.nextElement();
            String[] value= request.getParameterValues(name);
            if( value.length == 1)
                event.setParameter(name, value[0]);
            else
                event.setParameter(name, value);
        }
        //yfzhu 2004-09-02 added for test code
        if( request.getAttribute("j_username")!=null) event.setParameter("j_username", request.getAttribute("j_username").toString());
        if( request.getAttribute("j_password")!=null) event.setParameter("j_password", request.getAttribute("j_password").toString());

        // append remote address infor of this request
        String ra=request.getRemoteAddr(), rh=request.getRemoteHost();
        String remote=null;
        if( rh.equals(ra))remote= rh;
        else remote=ra+"["+rh+"]";
        event.setParameter("remote_address",remote);
        // session id of the request
        event.setParameter("sessionid", request.getSession(true).getId());
        return event;
    }
}
