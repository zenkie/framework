/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web;
import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.LoginFailedException;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.control.web.binhandler.*;
/**
 * Return binary stream to client, such as File, Report, PDF, Excel
 * @author yfzhu@agilecontrol.com
 */

public class BinaryServlet extends HttpServlet {
	    private static Logger logger= null;
	    private Hashtable handlers;//key: path infor(String), value: BinaryHandler
	    
	    public void init() {
	        logger= LoggerManager.getInstance().getLogger(this.getClass().getName());
	        handlers= new Hashtable();
	    }

	    /*public  void doPost(HttpServletRequest request, HttpServletResponse  response)
	    throws IOException, ServletException {
	        doProcess(request,response);
	    }*/

	    public  void doGet(HttpServletRequest request, HttpServletResponse  response)
	    throws IOException, ServletException {
	        doProcess(request,response);
	    }
	    public  void doPost(HttpServletRequest request, HttpServletResponse  response)
	    throws IOException, ServletException {
	        doProcess(request,response);
	    }

	    private void doProcess(HttpServletRequest request,HttpServletResponse  response) throws ServletException ,IOException {
	        try {
	        	String pathInfo= request.getPathInfo();
	        	BinaryHandler handler= getBinaryHandler(pathInfo);
	        	handler.process(request, response);
	        	return;
	        } catch (LoginFailedException ex){
	            request.setAttribute("error",ex);
	            request.getSession().setAttribute("ignoreHttpAuthorizationHeader", "true");
	            //2004-11-17 changed by yfzhu to embed into protal
	            //this.getServletContext().getRequestDispatcher("/login.jsp").forward(request,response);
	            this.getServletContext().getRequestDispatcher("/").forward(request,response);
	            return;
	        } catch (Throwable ex) {
	            // direct to Error page to handle
	        	logger.error("Could not process: "+ex, ex);
	            request.setAttribute("error",ex);
	            //request.setAttribute("errorMsg",ex.getMessage());//By Hawke
	        }
	        //Hawke Begin
	        if(request.getParameter("formRequest")!=null)
	        { 
	          //request.removeAttribute("error");
	         logger.debug("forward to :" + request.getParameter("formRequest"));
	          getServletContext().getRequestDispatcher(request.getParameter("formRequest").toString()).forward(request,response);
	          return;
	        }
	        //Hawke end
	        // there has no flow for this page, direct it to unknown page
	        String errorURL= getURLMappingManager().getMappingByScreen("ERROR").getURL();
	        if(errorURL ==null)
	            logger.error("Screen named \"ERROR\" not found in URLMappings");
	        else{
	        	logger.debug("forward to :" + WebKeys.NDS_URI+ errorURL);
	            getServletContext().getRequestDispatcher(WebKeys.NDS_URI+ errorURL).forward(request,response);
	        }
	        //this.getServletContext().getRequestDispatcher(request.getRequestURL().toString()).forward(request,response);
	    }

	    /**
	     * Get handler according its name. Command are all in package named nds.control.ejb.handler
	     * and class name must be same as to the request name, for easier location.
	     * @param name like "/GetFile?filename=dddd.xls", or "/GetFile/dddd.xls"
	     * @throws NDSExcption if Command could not be loaded
	     */
	    public BinaryHandler getBinaryHandler(String name) throws Exception {
	    	if(name!=null){
	    		int p=name.indexOf('/',1);
	    		if(p>0){
	    			name= name.substring(0, p);
	    		}
	    	}
	        BinaryHandler handler=(BinaryHandler) handlers.get(name);
	        if( handler == null) {
	            Class c=null;
                // try figure the special handler name, such as PromotionAShtSubmit
                c= Class.forName("nds.control.web.binhandler."+ name.trim().replaceAll("/",""));
                handler=(BinaryHandler) c.newInstance();
                handlers.put(name, handler);
                logger.debug("BinaryHandler :"+ name +" created and ready for handling.");
	        }
	        return handler;
	    }	    
	    private URLMappingManager getURLMappingManager() {
	        return (URLMappingManager)WebUtils.getServletContextManager().getActor(WebKeys.URL_MANAGER);
	    }
	    private RequestProcessor getRequestProcessor() {
	        return (RequestProcessor)WebUtils.getServletContextManager().getActor(WebKeys.REQUEST_PROCESSOR);
	    }
	    private FlowProcessor getFlowProcessor() {
	        return (FlowProcessor)WebUtils.getServletContextManager().getActor(WebKeys.FLOW_PROCESSOR);
	    }
	    private UserWebImpl getUserWebImpl(HttpSession session) {
	        return (UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER);
	    }

	    public static void println(String s) {
	        System.out.println("MainServlet: "+ s);
	    }
	}

