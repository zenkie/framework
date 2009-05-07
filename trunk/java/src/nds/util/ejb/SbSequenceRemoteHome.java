/******************************************************************
*
*$RCSfile: SbSequenceRemoteHome.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: SbSequenceRemoteHome.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.ejb;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
public interface SbSequenceRemoteHome extends javax.ejb.EJBHome {
  SbSequenceRemote create() throws CreateException, RemoteException;
}
