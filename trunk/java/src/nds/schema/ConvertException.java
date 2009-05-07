/******************************************************************
*
*$RCSfile: ConvertException.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/31 03:01:31 $
*
*$Log: ConvertException.java,v $
*Revision 1.2  2006/01/31 03:01:31  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema;

import nds.util.NDSException;
/**
 * An exception that provides information on a convertion process from string to SQLType
 */
public class ConvertException extends NDSException{

    public ConvertException() {
    }


    public ConvertException(String s)
    {
        super(s);
    }

    public ConvertException(String s, Exception exception)
    {
        super(s,exception);
    }

}
