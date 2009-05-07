package nds.util.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface SequenceRemote extends EJBObject {
    public Integer getCurvalue() throws RemoteException;
    public void setCurvalue(Integer curvalue) throws RemoteException;
    public String getName() throws RemoteException;
    public int getNextKeyAfterIncrementingBy(int blockSize) throws java.rmi.RemoteException;

}