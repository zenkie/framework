/******************************************************************
*
*$RCSfile: StateHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: StateHandler.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
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
//Source file: F:\\work2\\tmp\\nds\\control\\ejb\\StateHandler.java

package nds.control.ejb;

import java.rmi.RemoteException;

import nds.control.event.NDSEvent;
import nds.control.util.ValueHolder;
import nds.util.NDSException;


public interface StateHandler  {

  public void init(StateMachine urc);

  public void doStart();

  public ValueHolder perform(NDSEvent event) throws NDSException, RemoteException ;

  public void doEnd();
}
