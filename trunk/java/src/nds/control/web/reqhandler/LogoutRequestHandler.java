/******************************************************************
*
*$RCSfile: LogoutRequestHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:16 $
*
*$Log: LogoutRequestHandler.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/
package nds.control.web.reqhandler;

import javax.servlet.http.HttpServletRequest;

import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.WebKeys;

public class LogoutRequestHandler extends RequestHandlerSupport {
    private static Logger logger=LoggerManager.getInstance().getLogger(LogoutRequestHandler.class.getName());

    public LogoutRequestHandler() {}

    /**
     * Loggout current user
     * @return null
    */
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
        UserWebImpl usr=(UserWebImpl)WebUtils.getSessionContextManager( request.getSession()).getActor(WebKeys.USER);
        String ra=request.getRemoteAddr(), rh=request.getRemoteHost();
        String remote=null;
        if( rh.equals(ra))remote= rh;
        else remote=ra+"["+rh+"]";

        if(usr !=null)   logger.info("User "+usr.getUserName()+" logged out from machine:"+ remote);
        // yfzhu 2004-05-10 so login.jsp will prompt for new login

        request.getSession().invalidate();
        if(usr.getUserId()!=-1){
            logger.debug("set ignoreHttpAuthorizationHeader");
            request.getSession().setAttribute("ignoreHttpAuthorizationHeader", "true");
        }
        return null;
    }
}
