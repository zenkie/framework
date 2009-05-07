package nds.portlet.action;

import java.util.Hashtable;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;
/**
 * ActionHandler factory.
 */
public final class ActionHandlerFactory {

    private static ActionHandlerFactory instance=null;
    private static Logger logger= LoggerManager.getInstance().getLogger(ActionHandlerFactory.class.getName());

    private Hashtable ActionHandlers;//key: ActionHandler name(String), value: ActionHandler
    private ActionHandlerFactory() {
        ActionHandlers=new Hashtable();
    }
    /**
     * Get ActionHandler according its name. ActionHandler are all in package named nds.control.ejb.Handler
     * and class name must be same as to the request name, for easier location.
     * @throws NDSExcption if ActionHandler could not be loaded
     */
    public ActionHandler getHandler(String name) throws NDSException {
        ActionHandler handler=(ActionHandler) ActionHandlers.get(name);
        if( handler == null) {
            Class c=null;
            try {
                // try figure the special ActionHandler name, such as PromotionAShtSubmit
                c= Class.forName("nds.portlet.action.handler."+ name.trim());
            } catch (ClassNotFoundException e) {
                    throw new NDSException("Internal Error: can not find class to handle event with ActionHandler:"+ name, e);

            }
            try{
                handler =(ActionHandler) c.newInstance();
                ActionHandlers.put(name, handler);
                logger.debug("Handler :"+ name +" created and ready for handling.");
            }catch(Exception e3){
                throw new NDSException("Internal Error: can not instantiate class to handle event with ActionHandler:"+ name, e3);
            }
        }
        return handler;
    }

    public static synchronized ActionHandlerFactory getInstance() {
        if(instance==null) {
            instance=new ActionHandlerFactory();
        }
        return instance;
    }
}
