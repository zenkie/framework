package nds.util.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface SequenceRemoteHome extends EJBHome {
    public SequenceRemote create(String name) throws RemoteException, CreateException;
    public SequenceRemote findByPrimaryKey(String primaryKey) throws RemoteException, FinderException;
}