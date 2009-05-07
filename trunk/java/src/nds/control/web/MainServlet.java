/******************************************************************
*
*$RCSfile: MainServlet.java,v $ $Revision: 1.6 $ $Author: Administrator $ $Date: 2006/03/13 01:15:57 $
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\control\\web\\MainServlet.java

package nds.control.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.control.util.EJBUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.LoginFailedException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.WebKeys;
public class MainServlet extends HttpServlet {
    private static Logger logger= null;
    int cnt=0;
    public void init() {
        logger= LoggerManager.getInstance().getLogger(this.getClass().getName());
        // check file, if invalid, exit system
        /*if((!"60330d99bafa0937a852a327921ca013".equals(Tools.getFileCheckSum(this.getClass(), "nds.util.LicenseManager")))||
        		(!"fa03aa487e2d61e24b15f3f8a5557a65".equals(Tools.getFileCheckSum(this.getClass(), "nds.util.Tools")))||
				(!"3fc662c2a6df1ed371388060f866a30c".equals(Tools.getFileCheckSum(this.getClass(), "nds.util.NativeTools")))){
        	Thread t=new Thread(new Runnable(){
        		public void run(){
        			try{
        				Thread.sleep(1000);
        				logger.error("Important file changed, will exit.");
        				System.exit(1099);
        			}catch(Throwable e){
        			}
        		}
        	});
        	t.start();
        }*/
    }

    public  void doPost(HttpServletRequest request, HttpServletResponse  response)
    throws IOException, ServletException {
        doGet(request, response);
    }

    public  void doGet(HttpServletRequest request, HttpServletResponse  response)
    throws IOException, ServletException {
    	//logger.debug( nds.util.Tools.toString(request));
    	HttpSession session = request.getSession(true);
//        logger.debug("MainServlet: uri " + request.getRequestURI());
//        logger.debug("MainServlet: url " + request.getRequestURL());
        String selectedURL = request.getPathInfo();
//        logger.debug("MainServlet: path " + selectedURL);
        //        WebUtils.setServletContext(this.getServletContext());
        URLMapping current = getURLMappingManager().getMappingByURL(selectedURL);
        // if user not log in, show login window
        if ((current != null) && current.isSecured()) {
            UserWebImpl user= getUserWebImpl(session);
            if(!user.isLoggedIn() ) {
                // yfzhu added 2004-05-10 so login.jsp can handle correctly
                request.setAttribute("Authorization", "fail");

                response.sendRedirect("/");//request.getContextPath()+"/login.jsp");// do not change it! normally URLMapping will not has /nds
                return;
            }
        }
        doProcess(request,response);

    }

    private void doProcess(HttpServletRequest request,HttpServletResponse  response) throws ServletException ,IOException {
        try {
   			//check license
        	cnt++;
   			if(cnt%20==10){
   				nds.util.LicenseManager.validateLicense("Agile ERP","2.0",  EJBUtils.getApplicationConfigurations().getProperty("license","/license.xml") );
   			}
        	
        	//logger.debug( nds.util.Tools.toString(request));
        	getRequestProcessor().processRequest(request);
        	//logger.debug( nds.util.Tools.toString(request));
            getFlowProcessor().processFlow(request,response);
            //logger.debug( nds.util.Tools.toString(request));
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
            request.setAttribute("error",ex);
            request.setAttribute("iserror",Boolean.TRUE); //2005-10-27,通过此属性，供页面判断是出错返回的页面还是初始化的页面，参见nds.taglib.input.Checkbox 
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
