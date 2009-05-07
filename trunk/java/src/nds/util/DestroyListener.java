/******************************************************************
*
*$RCSfile: DestroyListener.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: DestroyListener.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/
package nds.util;

/**
 * Implement this interface if your class needs to be notified of the
 * destruction of the servlet.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2005/03/15 11:23:26 $
 */

public interface DestroyListener {

    /**
    @roseuuuid 3A6D2B4603DC
    */
    public void destroy();
}
