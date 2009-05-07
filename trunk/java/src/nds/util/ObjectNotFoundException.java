/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class ObjectNotFoundException extends NDSException{
    public ObjectNotFoundException()
    {
        super();
    }

    public ObjectNotFoundException(String s)
    {
        super(s);
    }

    public ObjectNotFoundException(String s, Exception exception)
    {
        super(s,exception);
    }

}
