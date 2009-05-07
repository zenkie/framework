/******************************************************************
*
*$RCSfile: ServletContextController.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/31 03:02:10 $
*
*$Log: ServletContextController.java,v $
*Revision 1.2  2006/01/31 03:02:10  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.1  2001/11/20 22:36:10  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.web;

import javax.servlet.ServletContextEvent;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.DestroyListener;
import nds.util.WebKeys;

/**
 * Need Servlet 2.3 Support
 */
public class ServletContextController  implements javax.servlet.ServletContextListener {
	private Logger logger= LoggerManager.getInstance().getLogger(ServletContextController.class.getName());    
	
    public ServletContextController() {
    }
    private  static boolean doLog=true;
    ///------------- implements ServletContextListener
    public void contextDestroyed(ServletContextEvent sce) {

        DestroyListener manager=null;
        try {
            manager=(DestroyListener)sce.getServletContext().getAttribute(WebKeys.SERVLET_CONTEXT_MANAGER);
        } catch(Exception e) {
            logger.error("Error trying to getting attribute named "+ WebKeys.SERVLET_CONTEXT_MANAGER+ " from context.");
        }
        if(manager !=null) {
            manager.destroy();
            logger.error("ServletContextManager "+manager+" destroied.");
        }
    }
    ///------------- implements ServletContextListener
    public void contextInitialized(ServletContextEvent sce) {
        /*try{
        ServletContext sc=sce.getServletContext();
        ServletContextActor actor=(ServletContextActor)Beans.instantiate(sc.getClass().getClassLoader(), "nds.control.web.ServletContextManager");
        actor.init(sc);
        debug("ServletContextManager "+actor+" created.");
    }catch(Exception e){
            e.printStackTrace();
            throw new NDSRuntimeException("Error initializing context", e);
    }*/

    }

}

