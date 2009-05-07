/******************************************************************
*
*$RCSfile: ControlException.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/31 02:59:20 $
*
*$Log: ControlException.java,v $
*Revision 1.2  2006/01/31 02:59:20  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/

package nds.control.web;
import nds.util.NDSException;
/**
 * Allow for nested exception
 */
public class ControlException extends NDSException
{

    public ControlException()
    {
    }

    public ControlException(String s)
    {
        super(s);
    }

    public ControlException(String s, Exception exception)
    {
        super(s,exception);
    }

}
