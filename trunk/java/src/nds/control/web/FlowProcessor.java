/******************************************************************
*
*$RCSfile: FlowProcessor.java,v $ $Revision: 1.4 $ $Author: Administrator $ $Date: 2006/01/07 11:45:53 $
*
*$Log: FlowProcessor.java,v $
*Revision 1.4  2006/01/07 11:45:53  Administrator
*no message
*
*Revision 1.3  2005/05/27 05:01:47  Administrator
*no message
*
*Revision 1.2  2005/03/30 13:13:55  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.2  2002/12/17 05:53:45  yfzhu
*no message
*
*Revision 1.5  2002/01/04 01:43:22  yfzhu
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
//Source file: F:\\work2\\tmp\\nds\\control\\web\\FlowProcessor.java

package nds.control.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.flowhandler.DefaultFlowHandler;
import nds.control.web.flowhandler.FlowHandler;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.TableManager;
import nds.util.Director;
import nds.util.NDSException;
import nds.util.WebKeys;
import java.util.*;

public class FlowProcessor  implements nds.util.ServletContextActor,java.io.Serializable {
    private Logger logger= LoggerManager.getInstance().getLogger(FlowProcessor.class.getName());

    private DefaultFlowHandler defaultHandler;

    private Director director;
    public FlowProcessor() {
        defaultHandler= new DefaultFlowHandler();
    }
    public void destroy() {
        defaultHandler.destroy();
        director=null;
    }
    public void init(Director director) {
        this.director=director;
        defaultHandler.init(director);
    }
    public void init(ServletContext context) {
        //this.context = context;
        defaultHandler.init(context);
        if(logger ==null)
            logger= LoggerManager.getInstance().getLogger(FlowProcessor.class.getName());
        logger.debug("FlowProcessor initialized.");

    }

    /**
    * This method is the core of the RequestProcessor. It receives all requests
    *  and generates the necessary events.
    *
    * There are two types for page direct:
    *   HttpServletResponse.sendRedirect
    *   RequestDispatcher.forward
    *
    *  The first one occurs when destination url contains question mark(?), which means
    *  the flow designer know firmly where and which parameter the flow should go with.
    *  So we will discard all information currently held in HttpServletRequest, and re-
    *  direct to that page. The only exception in this situation is the ValueHolder returned
    *  from server-side. We will put it in URL's query string as named in WebKeys.MESSAGE
    *
    *  The second case occurs when destination url contains no query information, so
    *  forward method used to let the next page retrieve the message freely
    *
    */
    public void processFlow(HttpServletRequest request,  HttpServletResponse  response)
    throws NDSException ,ServletException, java.io.IOException {
    	Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
    	if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
    	FlowHandler handler = getHandler(request);
        if( handler !=null) {
            String url= handler.processFlow( request);
            //logger.debug("url:"+ url);
            if(url !=null) {

                if( url.indexOf("?")>0){
                    // this url contains query string, we just do redirect
                    /* When the server returns some message (in ValueHolder, the redirect
                        page should also have access to it, so we append it to query string
                        2.0 modification by yfzhu , when valueholder contains "next-screen",
                        forward to that page instead of redirect
                    */
                    ValueHolder holder=(ValueHolder)request.getAttribute(WebKeys.VALUE_HOLDER);
                    if( holder!=null){
                        url +="&"+holder.toQueryString( holder,request.getCharacterEncoding(),locale );
                        	//"&"+WebKeys.MESSAGE+"="+ java.net.URLEncoder.encode((String)holder.get("message"), request.getCharacterEncoding());
                    }
                    if( holder.get("next-screen")!=null){
                    	// this page is send by holder, forward to that page
                    	WebUtils.getServletContext().getRequestDispatcher(url).forward(request,response);
                    }else{
                    	//logger.debug("Direct to :"+ request.getContextPath()+url);
                    	response.sendRedirect(request.getContextPath()+url);
                    }
                }else
                    WebUtils.getServletContext().getRequestDispatcher(url).forward(request,response);
            } else {
                logger.debug(" Direct URL not found according to handler :"+ handler+" for url:"+request.getPathInfo()+"");
            }
        } else {
            logger.debug("Handler not found for request:"+ request.getPathInfo()+"");
        }
    }

    /**
     * This method load the necessary FlowHandler class necessary to process a the
     * request for the specified request.
     *
     * order:
     *      HttpServletRequest.getParameter("flow-handler")
     *      URLMapping.getFlowHandler()
     *      DefaultFlowHandler( if request has "valueholder" attribute, that is, the requestProcessor
     *              has something returned from server,and may want to be displayed.
     */
    private FlowHandler getHandler(HttpServletRequest request) throws NDSEventException {
        FlowHandler handler = null;
        String flowProcessorString =(String) request.getParameter("flow-handler");
        String selectedUrl=null;
        if(flowProcessorString ==null) {
            selectedUrl= request.getPathInfo();
            //logger.debug(" selectedUrl=" + selectedUrl);
            // URLMappingManager is initialized in MainServlet.init()
            URLMappingManager manager=(URLMappingManager)director.getActor(WebKeys.URL_MANAGER);
            URLMapping map;
            map=manager.getMappingByURL(selectedUrl);
            if(map !=null) {
                flowProcessorString=map.getFlowHandler();
            }
        }
        if( flowProcessorString !=null) {
            try {
                handler = (FlowHandler)getClass().getClassLoader().loadClass(flowProcessorString).newInstance();
                handler.init(WebUtils.getServletContext());
            } catch (Exception ex) {
                throw new NDSEventException("Can not find FlowHandler for "+flowProcessorString,ex) ;
            }
        }
        if( handler ==null) {
            if(request.getAttribute(WebKeys.VALUE_HOLDER)!=null) {
                handler= defaultHandler;
                //logger.debug("Found valueholder in request, use default flow handler");
            } else {
                handler= defaultHandler;
                //logger.debug("No FlowHandler found for request "+selectedUrl);
            }
        }
        return handler;
    }


}
