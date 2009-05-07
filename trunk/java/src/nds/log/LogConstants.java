/******************************************************************
*
*$RCSfile: LogConstants.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:18 $
*
*$Log: LogConstants.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:18  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.log;

public interface LogConstants{
    public static final int EMERGENCY = 1;
    public static final int ALERT = 2;
    public static final int CRITICAL = 4;
    public static final int NOTICE = 8;
    public static final int ERROR = 16;
    public static final int WARNING = 32;
    public static final int INFO = 64;
    public static final int DEBUG = 128;
}
