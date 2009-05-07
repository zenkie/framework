package nds.control.web;

import java.util.Collection;
/**
 * The session listener holder, which receive session regester/unregister information
 * and notify all SessionListern of that information.
 *
 * The concrete SessionManager now in nds.control.web package is SecurityManagerWebImpl,
 * while listeners are from nds.sms.SMProcessor
 */
public interface SessionManager {
    public void addSessionListener(SessionListener l);

    /**
     * @param listener
     * @roseuid 404A978D03AD
     */
    public void removeSessionListener(SessionListener l);

    public Collection getLoginSessions();

    /**
     * Get SessionInfo of the specified session id
     */
    public SessionInfo getSession(String sessionId);

    public void unregister(String sessionId) ;

    public void register(int userId, String usrName, String sessionId, long time, String hostIP, UserWebImpl user) ;

    public boolean isValid(String sessionId) ;

    public void setValid(String sessionId, boolean valid) ;
}