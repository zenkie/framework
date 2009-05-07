/******************************************************************
*
*$RCSfile: DefaultFlowHandler.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/03/30 13:13:55 $
*
*$Log: DefaultFlowHandler.java,v $
*Revision 1.3  2005/03/30 13:13:55  Administrator
*no message
*
*Revision 1.2  2005/03/23 17:55:58  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:16  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
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
package nds.control.web.flowhandler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.URLMapping;
import nds.control.web.URLMappingManager;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Director;
import nds.util.NDSException;
import nds.util.ServletContextActor;
import nds.util.*;
import nds.util.WebKeys;

public class DefaultFlowHandler extends FlowHandler implements ServletContextActor {
    private static Logger logger=LoggerManager.getInstance().getLogger(DefaultFlowHandler.class.getName());

    private URLMappingManager manager=null;
    private String infoScreen=null; // the default screen working as default result page
    private Director director;
    public DefaultFlowHandler() {}

    public void init(Director director) {
        this.director=director;
    }
    public void init(ServletContext context) {
        manager=(URLMappingManager)director.getActor(WebKeys.URL_MANAGER);
        URLMapping mapping=manager.getMappingByScreen("INFO");
        if( mapping !=null)
            infoScreen= WebKeys.NDS_URI+mapping.getURL();
        if( infoScreen ==null) {
            logger.error("INFO screen not found in URLMappingManager, which working as default result page for those without next-screen\n\r"+
                         " Better create a URLMapping in URLMappingManager with screen named \"INFO\"");
        }
    }
    public void destroy() {
        director=null;
    }
   
    /**
     * Order of processing:
     *  request.getParameter("next-screen")
     *  URLMapping.getNextScreen()
     *  URLMapping.getNextScreen( request.getParameter("command"))
     *  URLMappingManager.getScreen("INFO");
     */
    public String processFlow(HttpServletRequest request) throws NDSException {
        String ns;
        Object n;
        //logger.debug(Tools.toString(request));
        ns=getNextScreen(request);
        //logger.debug("next-screen:"+ ns+", param:"+request.getParameter("next-screen") );
        if(Validator.isNotNull(ns)) return ns;

        URLMapping map;
        String selectedUrl = request.getPathInfo();
        map=manager.getMappingByURL(selectedUrl);
        if(map !=null) {
            String screen= map.getNextScreen();
            if( screen ==null) {
                screen=map.getNextScreen(request.getParameter("command"));
            }
            if(screen !=null) {
                map= manager.getMappingByScreen(screen);
                if( map!=null) {
                    return WebKeys.NDS_URI+map.getURL();
                }
            }

        }
        logger.debug("No next screen found, INFO screen used( url= "+ selectedUrl+")");
        return infoScreen;
    }
    public String toString() {
        return name;
    }
    private final static String name="DefaultFlowHanlder";
}
