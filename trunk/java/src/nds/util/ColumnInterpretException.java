/******************************************************************
*
*$RCSfile: ColumnInterpretException.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: ColumnInterpretException.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
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
package nds.util;



public class ColumnInterpretException extends NDSException {

    public ColumnInterpretException() {
        super();
    }

    public ColumnInterpretException(String message) {
        super(message);
    }
    public ColumnInterpretException(String s, Exception exception){
        super(s,exception);
    }
}
