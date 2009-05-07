/******************************************************************
*
*$RCSfile: RequestProcessor.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/12/18 14:06:14 $
*
*$Log: RequestProcessor.java,v $
*Revision 1.2  2005/12/18 14:06:14  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.2  2002/12/17 05:53:45  yfzhu
*no message
*
*Revision 1.5  2001/11/29 00:48:31  yfzhu
*no message
*
*Revision 1.4  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\control\\web\\RequestProcessor.java

package nds.control.web;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.reqhandler.DefaultRequestHandler;
import nds.control.web.reqhandler.RequestHandler;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Director;
import nds.util.NDSException;
import nds.util.ServletContextActor;
import nds.util.WebKeys;

/**
 * This is the web tier controller for the sample application.
 *
 * This class is responsible for processing all requests received from
 * the Main.jsp and generating necessary events to modify data which
 * are sent to the ShoppingClientControllerWebImpl.
 *
 */
public class RequestProcessor  implements ServletContextActor, java.io.Serializable {
    private static Logger logger=LoggerManager.getInstance().getLogger(RequestProcessor.class.getName());
    private DefaultRequestHandler defaultHandler;
    private Director director;
    /** Empty constructor for use by the JSP engine. */
    public RequestProcessor() {
        defaultHandler=new DefaultRequestHandler();
    }
    public void init(Director director) {
        this.director=director;
        defaultHandler.init(director);
    }
    public RequestHandler getDefaultRequestHandler() {
        return defaultHandler;
    }
    public void destroy() {
        defaultHandler.destroy();
    }
    public void init(ServletContext context) {
        defaultHandler.init(context);
        logger.debug("RequestProcessor initialized.");

    }
    public ValueHolder processRequest(HttpServletRequest request, RequestHandler handler) throws NDSException {
        NDSEvent event = null;
        ClientControllerWebImpl scc = (ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.WEB_CONTROLLER);
        ValueHolder v=null;
        if (handler != null) {
            //handler.setServletContext(context);
        	
            handler.doStart(request);
            event = handler.processRequest(request);
            if (event != null) {
                /**
                 * Add security information here
                 */
                if(event instanceof DefaultWebEvent){
                    UserWebImpl user= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(WebKeys.USER));
                    String operatorid=""+user.getUserId();
                    ((DefaultWebEvent)event).setParameter("operatorid", operatorid);
                }
                
                v = scc.handleEvent(event);
                if( v !=null) {
                    //logger.debug("Valueholder (msg="+v.get("message")+") returned from EJB controller, attach it to Request for further handler");
                    request.setAttribute(WebKeys.VALUE_HOLDER, v);// attach to request, so the next screen page can get and display it
                    Collection updatedModelList=(Collection) v.get("UpdateModelList");
                    WebUtils.getSessionContextManager(request.getSession()).notifyListeners(updatedModelList, v, request.getSession());
                } else {
                    logger.debug("Nothing returned from EJB controller.");
                }
            } else {
                logger.debug("Handler "+ handler+ " did not generate event.");
            }
            handler.doEnd(request);
        }
        return v;

    }
    /**
    * This method is the core of the RequestProcessor. It receives all requests
    *  and generates the necessary events.
    *  @return ValueHolder the value that ejb layer returned
    */
    public ValueHolder processRequest(HttpServletRequest request, String handlerClasss) throws NDSException {
        RequestHandler handler = getHandler(request,handlerClasss);
        return processRequest(request, handler);
    }

    /**
    * This method is the core of the RequestProcessor. It receives all requests
    *  and generates the necessary events.
    *  @return ValueHolder the value that ejb layer returned
    */
    public ValueHolder processRequest(HttpServletRequest request) throws NDSException {
        RequestHandler handler = getHandler(request);
        return processRequest(request, handler);
    }
    private RequestHandler getHandler(HttpServletRequest request) throws NDSEventException {
        return getHandler(request,null);
    }
    /**
     * This method load the necessary RequestHandler class necessary to process a the
     * request for the specified request.
     * order:
     *      HttpServletRequest.getParameter("request-handler")
     *      URLMapping.getRequestHandler()
     *      DefaultRequestHandler
     *
     */
    private RequestHandler getHandler(HttpServletRequest request, String handlerClass) throws NDSEventException {
        RequestHandler handler = null;
        String requestProcessorString =(handlerClass==null? (String)request.getParameter("request-handler"):handlerClass);
        if(requestProcessorString ==null) {
            String selectedUrl = request.getPathInfo();
            // URLMappingManager is initialized in MainServlet.init()
            URLMappingManager manager=(URLMappingManager)director.getActor(WebKeys.URL_MANAGER);
            //logger.debug("selectUrl="+ selectedUrl);
            
            URLMapping map=manager.getMappingByURL(selectedUrl);
            //logger.debug("1:" + map);
            if(map !=null)
                requestProcessorString=map.getRequestHandler();
        }
        if( requestProcessorString !=null) {
            try {
                handler = (RequestHandler)getClass().getClassLoader().loadClass(requestProcessorString).newInstance();
                if ( handler instanceof DefaultRequestHandler){
                	// so handler extended DefaultRequestHandler will be initialiezed first
                	((DefaultRequestHandler)handler).init(director);
                }
            } catch (Exception ex) {
                throw new NDSEventException("Can not find RequestHandler for "+requestProcessorString,ex) ;
            }
        }
        if(handler==null)
            handler= defaultHandler;
        return handler;
    }

}
