/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class LicenseException extends NDSRuntimeException{
	public LicenseException()
    {
    }

    public LicenseException(String s)
    {
        super(s);
    }

    public LicenseException(String s, Exception exception)
    {
        super(s,exception);
        
    }	
}
