/******************************************************************
*
*$RCSfile: SessionController.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/17 10:35:06 $
*
*$Log: SessionController.java,v $
*Revision 1.2  2006/01/17 10:35:06  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.2  2004/02/02 10:42:42  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.1  2001/11/20 22:36:10  yfzhu
*no message
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/

package nds.web; 

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import nds.util.DestroyListener;
import nds.util.WebKeys;
import nds.control.web.ClientControllerWebImpl;
import nds.log.*;
import nds.control.web.*;
import java.util.*;

public class SessionController implements  HttpSessionListener{
	private static Logger logger= LoggerManager.getInstance().getLogger(SessionController.class.getName());
    public SessionController() {
        //System.out.println("SessionContext Loader:"+ this.getClass().getClassLoader());
    }
    private  static boolean doLog=false;
    private void debug(String str) {
        logger.debug(str);
        //System.out.println("[SessionController] "+ str);
    }
    public void sessionCreated(HttpSessionEvent se){
    	logger.debug("session created: " + se.getSession().getId());
    }
    public void sessionDestroyed(HttpSessionEvent se){
    	logger.debug("session destroied: " + se.getSession().getId());
       try{
       	SecurityManagerWebImpl sm=(SecurityManagerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.SECURITY_MANAGER);
        if(sm!=null){
        	
        	sm.unregister(se.getSession().getId());
        }else
        	logger.debug("SecurityManagerWebImpl not found ?!");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
  

}
