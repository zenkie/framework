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

import javax.servlet.http.HttpServletRequest;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;

/**
 * Reload init files, including URLMappings, Configurations, Loggers
 */
public class ReconfigHandler extends RequestHandlerSupport {
    private static Logger logger= LoggerManager.getInstance().getLogger(ReconfigHandler.class.getName());

    public ReconfigHandler() {
    }
    /**
     * Just return a DefaultWebEvent with event name "event/ReconfigEvent", so
     * the server side will also reconfig according to this signal.
     *
     * In this method, not only an event create, environment including urlmapping,logger
     * configurations etc also reconfigured.
     */
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
        DefaultWebEvent event=new DefaultWebEvent("ReconfigEvent");

        // do nothing, deprecated

        return event;
    }
}
