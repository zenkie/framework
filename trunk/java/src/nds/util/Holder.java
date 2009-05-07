// ===========================================================================// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Holder.java,v 1.1.1.1 2005/03/15 11:23:26 Administrator Exp $
// ---------------------------------------------------------------------------

package nds.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/* --------------------------------------------------------------------- */
/**
 * @version $Id: Holder.java,v 1.1.1.1 2005/03/15 11:23:26 Administrator Exp $
 * @author Greg Wilkins
 */
public class Holder
    extends AbstractMap
    implements LifeCycle,
               Serializable
{
    /* ---------------------------------------------------------------- */
    protected String _name;
    protected String _displayName;
    protected String _className;
    protected Properties _initParams;

    protected transient Class _class;

    /* ---------------------------------------------------------------- */
    /** Constructor for Serialization.
     */
    protected Holder()
    {}

    /* ---------------------------------------------------------------- */
    protected Holder(String name,
                     String className)
    {
        if (name==null || name.length()==0)
            throw new IllegalArgumentException("No name for "+className);

        if (className==null || className.length()==0)
            throw new IllegalArgumentException("No classname");

        _className=className;
        _name=name;
        _displayName=name;
    }


    /* ------------------------------------------------------------ */
    public String getName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    public void setDisplayName(String name)
    {
        _name=name;
    }

    /* ------------------------------------------------------------ */
    public String getDisplayName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    public String getClassName()
    {
        return _className;
    }
    public void setInitParameters(Properties props){
        _initParams= props;
    }

    /* ------------------------------------------------------------ */
    public void setInitParameter(String param,String value)
    {
        put(param,value);
    }

    /* ---------------------------------------------------------------- */
    public String getInitParameter(String param)
    {
        if (_initParams==null)
            return null;
        return (String)_initParams.get(param);
    }

    /* ---------------------------------------------------------------- */
    public Map getInitParameters()
    {
        return _initParams;
    }

    /* ------------------------------------------------------------ */
    public Enumeration getInitParameterNames()
    {
        if (_initParams==null)
            return Collections.enumeration(Collections.EMPTY_LIST);
        return Collections.enumeration(_initParams.keySet());
    }

    /* ------------------------------------------------------------ */
    /** Map entrySet method.
     * FilterHolder implements the Map interface as a
     * configuration conveniance. The methods are mapped to the
     * filter properties.
     * @return The entrySet of the initParameter map
     */
    public synchronized Set entrySet()
    {
        if (_initParams==null)
            _initParams=new Properties();
        return _initParams.entrySet();
    }

    /* ------------------------------------------------------------ */
    /** Map put method.
     * FilterHolder implements the Map interface as a
     * configuration conveniance. The methods are mapped to the
     * filter properties.
     */
    public synchronized Object put(Object name,Object value)
    {
        if (_initParams==null)
            _initParams=new Properties();
        return _initParams.put(name,value);
    }

    /* ------------------------------------------------------------ */
    /** Map get method.
     * FilterHolder implements the Map interface as a
     * configuration conveniance. The methods are mapped to the
     * filter properties.
     */
    public synchronized Object get(Object name)
    {
        if (_initParams==null)
            return null;
        return _initParams.get(name);
    }

    /* ------------------------------------------------------------ */
    public void start()
        throws Exception
    {
        _class=Loader.loadClass(this.getClass(),_className);
    }

    /* ------------------------------------------------------------ */
    public synchronized Object newInstance()
        throws InstantiationException,
               IllegalAccessException
    {
        if (_class==null)
            throw new InstantiationException("No class for "+this);
        return _class.newInstance();
    }

    /* ------------------------------------------------------------ */
    public boolean isStarted()
    {
        return _class!=null;
    }

    /* ------------------------------------------------------------ */
    public void stop()
    {
        _class=null;
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return _name+"["+_className+"]";
    }

}





