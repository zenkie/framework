/******************************************************************
*
*$RCSfile: QueryException.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:23 $
*
*$Log: QueryException.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.query;
import nds.util.NDSException;
/**
 * Allow for nested exception
 */
public class QueryException extends NDSException
{

    public QueryException()
    {
    }

    public QueryException(String s)
    {
        super(s);
    }

    public QueryException(String s, Exception exception)
    {
        super(s,exception);
    }
}
