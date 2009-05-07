/******************************************************************
*
*$RCSfile: ClientNotFoundException.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/03/14 10:52:10 $

********************************************************************/

package nds.sms;
import nds.util.NDSRuntimeException;
/**
 * Allow for nested exception
 */
public class ClientNotFoundException extends NDSRuntimeException
{

    public ClientNotFoundException()
    {
    }

    public ClientNotFoundException(String s)
    {
        super(s);
    }

    public ClientNotFoundException(String s, Exception exception)
    {
        super(s,exception);
    }

}
