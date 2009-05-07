/******************************************************************
*
*$RCSfile: ReconfigHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: ReconfigHandler.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
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
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.WebKeys;

/**
 * Register user information, will check sesstion attribute "nds.control.web.ValidateMServlet" defined in ValidateMServlet
 */
public class RegisterHandler extends RequestHandlerSupport {
    private static Logger logger= LoggerManager.getInstance().getLogger(RegisterHandler.class.getName());

    public RegisterHandler() {
    }
    /**
     * Find sesstion attribute "nds.control.web.ValidateMServlet" defined in ValidateMServlet, 
     * and compare with user input named "verifyCode"
     */
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
    	String serverValidCode=(String) request.getSession().getAttribute("nds.control.web.ValidateMServlet");
    	if(serverValidCode ==null ) throw new NDSEventException("Internal error, nds.control.web.ValidateMServlet not set in session attribute");
    	String userValidCode= request.getParameter("verifyCode");
    	if(serverValidCode.equalsIgnoreCase(userValidCode)){
    		
    	}else{
    		throw new NDSEventException("@error-verify-code@");
    	}
    	DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
    	event.setParameter("command","RegistrateUser");
        /** 
         * add param named "nds.query.querysession", which hold QuerySession object
         * @since 2.0
         */
        SessionContextManager scmanager= WebUtils.getSessionContextManager(request.getSession(true));
        UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
        if(usr !=null && usr.getSession()!=null)
        	event.put("nds.query.querysession",usr.getSession());
        event.put("JAVA.UTIL.LOCALE", usr.getLocale());	
        Enumeration enu= request.getParameterNames();
        while(enu.hasMoreElements()) {
            String name=(String) enu.nextElement();
            String[] value= request.getParameterValues(name);
            if(name.equalsIgnoreCase("command")){
                /* ############# tony 's method was deferred to EJB layer to implement,
                     see nds.control.ejb.CommandFactory

                event.setParameter(name, commandName(value[0]));
                ######## yfzhu marked above */
                event.setParameter(name, value[0]);

//nmdemo, ObjectPermit and ObjectRollback will also need spName                if(value[0].endsWith("Submit"))
                    event.setParameter("spName",value[0]);
            }else if( value.length == 1)
                event.setParameter(name, value[0]);
            else
                event.setParameter(name, value);
        }
        return event;
    }
}
