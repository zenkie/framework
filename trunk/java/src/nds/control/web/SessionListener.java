package nds.control.web;

import java.util.EventListener;

/** listener that should be implemented to listen for any session register/unregister events */
public interface SessionListener extends EventListener
{

    /**
     * This event is fired when a session is registered into SecurityManager
     * @see SecurityManagerWebImpl
     */
    public void sessionRegistered(SessionInfo e);

    /**
     * This event is fired when a session is unregistered into SecurityManager
     * @see SecurityManagerWebImpl
     */
    public void sessionUnRegistered(SessionInfo e);
}
