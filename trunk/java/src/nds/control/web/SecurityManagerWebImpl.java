/******************************************************************
*
*$RCSfile: SecurityManagerWebImpl.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/11/16 02:57:21 $
*
*$Log: SecurityManagerWebImpl.java,v $
*Revision 1.2  2005/11/16 02:57:21  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.6  2004/03/31 10:42:37  yfzhu
* Add SessionListener support, so other module such as SMSProcessor can be notified when user
* get in or out
*
*Revision 1.5  2004/02/02 10:42:37  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/09/29 07:37:21  yfzhu
*before removing entity beans
*
*Revision 1.3  2003/03/30 08:11:33  yfzhu
*Updated before subtotal added
*
*Revision 1.2  2002/12/17 05:53:45  yfzhu
*no message
*
*Revision 1.5  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.4  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.3  2001/11/29 00:48:31  yfzhu
*no message
*
*Revision 1.2  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/

package nds.control.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.swing.event.EventListenerList;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.Director;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.ServletContextActor;


/**

*/
public class SecurityManagerWebImpl implements ServletContextActor, SessionManager,
    java.io.Serializable {
    private Logger logger= LoggerManager.getInstance().getLogger(SecurityManagerWebImpl.class.getName());
    private Hashtable sessions;//key sessionId(String) value: SessionInfo
    private Vector validSessions;//element sessionId(String), must be one in sessions

    private EventListenerList listenerList = new EventListenerList();


    public SecurityManagerWebImpl() {
        sessions=new Hashtable();
        validSessions=new Vector();
    }
    public void init(Director director) {
        try {
            sessions.clear();
            validSessions.clear();
        } catch (Exception ce) {
            logger.error("Error getting SbSecurityManager",ce);
            throw new NDSRuntimeException("Error getting SbSecurityManager",ce);
        }
        logger.debug("SecurityManagerWebImpl initialized.");

    }
    public void init(ServletContext context) {

    }
    public void destroy() {
        // call ejb remove on self/shopping cart/etc.
        logger.debug("SecurityManagerWebImple destroied.");
    }


    /**
    If user is not valid, the UserWebImpl will clear local DirectoryCache before check directory permission
    @roseuid 3BF3CA410296
    */
    public boolean isValid(String sessionId) {
        return validSessions.contains(sessionId);
    }
    public void setValid(String sessionId, boolean valid) {
        boolean b= isValid(sessionId);
        if(b && !valid)
            validSessions.removeElement(sessionId);
        else if( !b & valid) {
            validSessions.addElement(sessionId);
        }
        logger.debug("successfully set session "+sessions.get(sessionId)+" to "+ valid);
    }
    /**
     * @return Collection of SessionInfo
    @roseuid 3BF3D58C0012
    */
    public Collection getLoginSessions() {
        return sessions.values();
    }
    /**
     * Get SessionInfo of the specified session id
     */
    public SessionInfo getSession(String sessionId){
        return (SessionInfo)sessions.get(sessionId);
    }
    /**
    @roseuid 3BF3D5F4021B
    */
    public void unregister(String sessionId) {
        // notify all session listener about the logout info
        fireSessionUnRegistered((SessionInfo) sessions.get(sessionId));

        validSessions.removeElement(sessionId);
        SessionInfo si=(SessionInfo)sessions.remove(sessionId);
        if( si!=null){
        	UserWebImpl user=si.getUserWebImpl();
        	if(user!=null) user.destroy();
        }
        // destroy session info
        logger.debug("session :"+sessionId+" unregistered.");
    }

    /**
    @roseuid 3BF3D82A0229
    */
    public void register(int userId, String usrName, String sessionId, long time, String hostIP,UserWebImpl user) {
        SessionInfo info=new SessionInfo(userId,usrName, sessionId,time,hostIP, user);
        sessions.put(sessionId, info);
        if(! validSessions.contains(sessionId))
            validSessions.addElement(sessionId);
        // notify all session listener about the login info
        fireSessionRegistered((SessionInfo) sessions.get(sessionId));
        logger.debug("session :"+info+ " registered." );
    }

    public void addSessionListener(SessionListener l)
    {
        listenerList.add(SessionListener.class, l);
    }

    /**
     * @param listener
     * @roseuid 404A978D03AD
     */
    public void removeSessionListener(SessionListener l)
    {
        listenerList.remove(SessionListener.class, l);
    }
    private void fireSessionRegistered(SessionInfo event)
    {
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SessionListener.class) {
                ((SessionListener) listeners[i + 1]).sessionRegistered(event);
            }
        }

    }
    private void fireSessionUnRegistered(SessionInfo event)
    {
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SessionListener.class) {
                ((SessionListener) listeners[i + 1]).sessionUnRegistered(event);
            }
        }

    }


}
