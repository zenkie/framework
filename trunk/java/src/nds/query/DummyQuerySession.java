/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.query;

import java.util.*;

import nds.schema.TableManager;

/**
 * DummyQuerySession which can be shared in no session query environment
 * This one has no session info.
 * @author yfzhu@agilecontrol.com
 */

public class DummyQuerySession implements  QuerySession {
	private static DummyQuerySession instance=null;
	private long creationTime;
	private DummyQuerySession(){
		creationTime=System.currentTimeMillis();
		
	}
	public Locale getLocale(){
		return TableManager.getInstance().getDefaultLocale();
	}
	public static DummyQuerySession getInstance(){
		if(instance==null) instance=new DummyQuerySession();
		return instance;
	}
	private static Enumeration emptyEnum;
	static{
		emptyEnum= (new Vector()).elements(); 
	}
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
    public long getCreationTime(){
    	return creationTime;
    }
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
    public String getId(){
    	return "null";
    }

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
    public Object getAttribute(String name){
    	return null;
    }
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
    public Enumeration getAttributeNames(){
    	return emptyEnum;
    }
    
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
    public void setAttribute(String name, Object value){
    	
    }
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
    public void removeAttribute(String name){
    	
    }
    /**
     * Security grade is for column level security control. QuerySession is associated 
     * with user, which contains security grade property. 
     * 
     * User should only get access to columns that have security level lower than him.
     *  
     * @return security grade of current session(user)
     */
    public int getSecurityGrade(){
    	return 0;
    }
    
    
    public String toDebugString(){
    	return "dummy";
    }
}
