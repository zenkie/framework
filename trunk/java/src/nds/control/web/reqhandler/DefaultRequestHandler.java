/******************************************************************
*
*$RCSfile: DefaultRequestHandler.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:14 $
*
*$Log: DefaultRequestHandler.java,v $
*Revision 1.2  2005/12/18 14:06:14  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.2  2003/03/30 08:11:47  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.6  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.5  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.4  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.3  2001/11/08 20:58:17  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
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
 * If no RequestHandler found in URLMapping of specified web page, this class will
 * be respondent to the processing.
 *
 * The steps are:
 *  Check for event name in URLMapping, if not found, return null
 *  Create DefaultWebEvent containing all reqest parameters, with event name from
 *      URLMapping, and return it.
 *
 * Future enhancement: add validation class for screen.
 *
 */
public class DefaultRequestHandler extends RequestHandlerSupport implements ServletContextActor {
    private static Logger logger=LoggerManager.getInstance().getLogger(DefaultRequestHandler.class.getName());
    protected Director director;
    private URLMappingManager manager=null;
    public DefaultRequestHandler() {}
    public void init(Director director) {
        this.director=director;
    }
    public void destroy() {
        director=null;
    }
    public void init(ServletContext context) {
        //manager=(URLMappingManager)director.getActor(WebKeys.URL_MANAGER);
        //this.setServletContext(context);
    }
    /**
     * This process will append current user's information to EJB layer verification, that is
     * set "operatorid" in event @see nds.control.ejb.Command#getOperator
     *
     * @return null if event=null in request.
     */
    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
        String selectedUrl = request.getPathInfo();
        // URLMappingManager is initialized in MainServlet.init()
        URLMappingManager manager=(URLMappingManager)director.getActor(WebKeys.URL_MANAGER);
        URLMapping map=manager.getMappingByURL(selectedUrl);
        String eventName=null;
        if(map !=null)
            eventName=map.getEvent();
        if(eventName ==null){
            /** yfzhu 2004-06-06 changed to CommandEvent, no longer null*/
            //return null;
            eventName= "CommandEvent";
        }

        DefaultWebEvent event=new DefaultWebEvent(eventName);
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
    private String commandName(String command){
        if(command.endsWith("Create")){
            return "ObjectCreate";
        }else if(command.endsWith("Modify")){
            return "ObjectModify";
        }else if(command.endsWith("Delete")){
            return "ObjectDelete";
        }else if(command.endsWith("Submit")){
            return "ObjectSubmit";
        }else if(command.endsWith("Rollback")){//nmdemo
            return "ObjectRollback";
        }else if(command.endsWith("Permit")){//nmdemo
            return "ObjectPermit";
        }else if(command.endsWith("Audit")){//nmdemo
            return "ObjectAudit";
        }
        return command;
    }
}
