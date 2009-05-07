/******************************************************************
*
*$RCSfile: SavePreferenceRequestHandler.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/01/07 11:46:00 $
*
*
********************************************************************/
package nds.control.web.reqhandler;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.web.URLMapping;
import nds.control.web.URLMappingManager;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Director;
import nds.util.ServletContextActor;
import nds.util.*;
import nds.control.web.*;

/**
 * Invalidate cache for preperences
 */
public class SavePreferenceRequestHandler extends DefaultRequestHandler{

    /**
     *  Invalidate cache for module preference
     */
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
        SessionContextManager scmanager= WebUtils.getSessionContextManager(request.getSession(true));
        UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
        // Invalidate cache for module preference
        usr.invalidatePreferences( request.getParameter("module"));
        
        return super.processRequest(request);
    }

}
