/******************************************************************
*
*$RCSfile: InitializationException.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: InitializationException.java,v $
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
 * This exception is thrown when a Configurable object is initialized
 * with illegal parameters and cannot complete its initialization.
 *
 * <p>When such exception is thrown, the object is not guaranteed
 * to be usable and the factory should behave accordingly.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2005/03/15 11:23:26 $
 */

public class InitializationException extends InstantiationException {

    public InitializationException() {
        super();
    }

    public InitializationException(String message) {
        super(message);
    }
}
