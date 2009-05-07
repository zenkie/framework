/******************************************************************
*
*$RCSfile: NDSEventException.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2006/03/13 01:16:14 $
*
*$Log: NDSEventException.java,v $
*Revision 1.3  2006/03/13 01:16:14  Administrator
*no message
*
*Revision 1.2  2006/01/31 02:58:30  Administrator
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

package nds.control.event;
import nds.util.NDSException;
/**
 * Allow for nested exception
 */
public class NDSEventException extends NDSException
{

    public NDSEventException()
    {
    }

    public NDSEventException(String s)
    {
        super(s);
    }

    public NDSEventException(String s, Throwable exception)
    {
        super(s,exception);
        
    }

}
