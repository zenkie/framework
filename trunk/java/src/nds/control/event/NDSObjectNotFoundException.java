/******************************************************************
*
*$RCSfile: NDSObjectNotFoundException.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/31 02:58:30 $
*
*$Log: NDSObjectNotFoundException.java,v $
*Revision 1.2  2006/01/31 02:58:30  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.1  2001/11/13 07:19:00  yfzhu
*no message
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
public class NDSObjectNotFoundException extends NDSException
{

    public NDSObjectNotFoundException()
    {
    }

    public NDSObjectNotFoundException(String s)
    {
        super(s);
    }

    public NDSObjectNotFoundException(String s, Exception exception)
    {
        super(s,exception);
    }

}
