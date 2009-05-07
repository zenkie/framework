/******************************************************************
*
*$RCSfile: ClientController.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: ClientController.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.2  2004/02/02 10:42:58  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import nds.control.event.NDSEvent;
import nds.control.util.ValueHolder;
import nds.util.NDSException;

public interface ClientController extends EJBObject {
    /**
     * Feeds the specified event to the state machine of the business logic.
     * @return ValueHolder of returned values
     */
    public ValueHolder handleEvent(NDSEvent ese) throws NDSException, RemoteException;

}