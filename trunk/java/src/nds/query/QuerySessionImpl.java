/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.query;

import java.util.*;

import nds.schema.TableManager;
import nds.util.*;
/**
 * QuerySessionImpl can accept attributes lately used by QuerRequest to
 * construct user specific queries;  
 * 
 * In nds.web.WebUserImpl, the user's QuerySession will be constructed
 * when he login and authenticated.
 * 
 * @author yfzhu@agilecontrol.com
 */

public class QuerySessionImpl implements  QuerySession {
	private long creationTime =System.currentTimeMillis();
	private String id;
	private int securityGrade;
	private Locale locale=TableManager.getInstance().getDefaultLocale();
	private Map attributes = new HashMap(3);
	
	public  QuerySessionImpl(String id){
		this.id=id;
	}
	public  QuerySessionImpl(){
		// this is a very simple implementation, may conflicts with other QuerySession Object
		this.id=""+Sequences.getNextID("querysession");
	}
	public Locale getLocale(){
		return locale;
	}
	public void setLocale(Locale loc){
		if(loc !=null)
			this.locale=loc;
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
    	return id;
    }

    /**
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     *
     * @param name a string specifying the name of the object
     *
     * @return the object with the specified name
     *
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    public Object getAttribute(String name){
    	name= name.toUpperCase().trim();
    	Object o= attributes.get(name);
    	if(o==null){
    		if( "$SYSDATE$".equals(name)){
    			o= ((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format(new Date());
    		}else if("$SYSDATENUM$".equals(name)){
    			o= ((java.text.SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(new Date());
    		}
    	}
    	return o;
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
    	return Collections.enumeration(attributes.keySet());
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
    public synchronized void setAttribute(String name, Object value){
    	attributes.put(name.toUpperCase(),value);
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
    	attributes.remove(name.toUpperCase());
    }
    /* ------------------------------------------------------------ */
    /**
     */
    public void setAttributes(Map attributes)
    {
        this.attributes=attributes;
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
    	return securityGrade;
    }
    
    public void setSecurityGrade(int secgrade){
    	this.securityGrade=secgrade;
    }
    
    public String toDebugString(){
    	return nds.util.Tools.toString(attributes);
    }
}
