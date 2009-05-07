/******************************************************************
*
*$RCSfile: SbSequenceRemote.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: SbSequenceRemote.java,v $
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

public interface SbSequenceRemote extends javax.ejb.EJBObject {

 public int getNextNumberInSequence(String name) throws java.rmi.RemoteException;
}