/******************************************************************
*
*$RCSfile: StateMachine.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: StateMachine.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.2  2004/02/02 10:42:59  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/12/28 14:20:01  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\control\\ejb\\StateMachine.java

package nds.control.ejb;

import java.rmi.RemoteException;
import java.util.HashMap;

import javax.ejb.SessionContext;

import nds.control.event.NDSEvent;
import nds.control.util.AjaxUtils;
import nds.control.util.EJBUtils;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.WebKeys;


/**
 * This class is a Universal front back end of an application
 * which ties all EJB components together dynamically at runtime.
 *
 * This class should not be updated to handle various event types.
 * This class will use ActionHandlers to handle events that require
 * processing beyond the scope of this class.
 *
 * A second option to event handling is to do so in the XML descriptor
 * itself.
 *
 * State may be stored in the attributeMap
 * Configuration of this file is via an XML descriptor.

 *
 */
public class StateMachine implements java.io.Serializable {
	private static Logger logger= LoggerManager.getInstance().getLogger(StateMachine.class.getName());

    private ClientControllerBean sccejb;
    private ModelUpdateManager mum;
    private HashMap orderTable;
    private HashMap attributeMap;
    private HashMap handlerMap;
    private SessionContext sc;
    
    
    public StateMachine(ClientControllerBean sccejb, SessionContext sc) throws Exception{
        this.sccejb = sccejb;
        this.sc = sc;
        this.mum = new ModelUpdateManager();
        attributeMap = new HashMap();
        handlerMap = new HashMap();

        
    }
    public void destroy() throws Exception{
    }
    
    public ValueHolder handleEvent(NDSEvent ese) throws NDSException, RemoteException{
        String eventName = ese.getEventName();
        ValueHolder holder=null;
        //logger.debug("Handle event: "+ese.getEventName());
        if (eventName != null) {
            String handlerName = getHandlerName(eventName);
            StateHandler handler = null;
            try {
                 if (handlerMap.get(eventName) != null) {
                    handler = (StateHandler)handlerMap.get(eventName);
                 } else {
                     handler = (StateHandler)Class.forName(handlerName).newInstance();
                     handlerMap.put(eventName, handler);
             }
            } catch (Exception ex) {
                logger.error("Error loading " + handlerName + " :" + ex);
            }
            if (handler != null) {
                handler.init(this);
                // do the magic
                handler.doStart();
                holder=handler.perform(ese);
                handler.doEnd();
            }else{
                logger.info("StateHanlder not found for event: "+ese.getEventName());
            }
        }


        return holder;//(mum.getUpdatedModels(ese));
    }

    private String getHandlerName(String eventName) {
//        try {
            // yfzhu modified at 2003-10-02 to load map from configurations
            // eventName has format like "java:comp/env/event/LoginEvent"
            int pl= eventName.lastIndexOf("/");
            if( pl > 0) eventName= eventName.substring(pl+1);
            String hn=EJBUtils.getApplicationConfigurations().getProperty("controller.event."+ eventName);
            logger.debug("Found handler for event '"+ eventName+"':"+ hn);
            return hn;
            /*
            InitialContext ic = new InitialContext();
            return  (String)ic.lookup(eventName);*/
  /*      } catch (javax.naming.NamingException ex) {
            logger.error("Counld not found handler for event '"+ eventName+"'");
        }
        return null;*/

    }


    public void setAttribute(String key, Object value) {
        attributeMap.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributeMap.get(key);
    }

    public ClientControllerBean getClientControllerBean() {
        return sccejb;
    }

    public SessionContext getSessionContext() {
        return sc;
    }
    
    

}
