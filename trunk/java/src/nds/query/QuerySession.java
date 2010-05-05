/*
 *
 */
package nds.query;

import java.io.Serializable;
import java.util.*;
/**
 * Add support for mutiple session environment, attribute value can be retrieved
 * from the session, and used for query construction or message log or quota control.
 * 
 * Note attribute name is case-insensitive
 * @author yfzhu@agileControl.com
 * @since 2.0
 */
public interface QuerySession extends Serializable {
	/**
	 * Locale of the session
	 * @return
	 */
	public Locale getLocale();
	/**
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @return a <code>long</code> specifying when this session was created,
     * expressed in milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    public long getCreationTime();
    /**
     * Returns a string containing the unique identifier assigned to this
     * session. The identifier is assigned by the servlet container and is
     * implementation dependent.
     *
     * @return a string specifying the identifier assigned to this session
     *
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    public String getId();

    /**
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name a string specifying the name of the object
     *
     * @return the object with the specified name
     *
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    public Object getAttribute(String name);
    /**
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this session.
     *
     * @return an <code>Enumeration</code> of <code>String</code> objects
     * specifying the names of all the objects bound to this session
     *
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    public Enumeration getAttributeNames();
    
    /**
     * Binds an object to this session, using the name specified. If an object
     * of the same name is already bound to the session, the object is
     * replaced.
     *
     *
     * <p>If the value passed in is null, this has the same effect as calling
     * <code>removeAttribute()<code>.
     *
     * @param name the name to which the object is bound; cannot be null
     *
     * @param value the object to be bound
     *
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    public void setAttribute(String name, Object value);
    /**
     * Removes the object bound with the specified name from this session.
     * If the session does not have an object bound with the specified name,
     * this method does nothing.
     *
     *
     * @param name the name of the object to remove from this session
     *
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    public void removeAttribute(String name);
    
    /**
     * Security grade is for column level security control. QuerySession is associated 
     * with user, which contains security grade property. 
     * 
     * User should only get access to columns that have security level lower than him.
     *  
     * @return security grade of current session(user)
     */
    public int getSecurityGrade();
    
    public String toDebugString();
	
}
