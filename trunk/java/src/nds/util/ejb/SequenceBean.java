package nds.util.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

public abstract class SequenceBean implements EntityBean {
    EntityContext entityContext;
	public int getNextKeyAfterIncrementingBy(int blockSize)
	{
		this.setCurvalue(new Integer(this.getCurvalue().intValue()+ blockSize));
		return this.getCurvalue().intValue();
	}

	public String ejbCreate(String name)
	{
		this.setName(name);
		this.setCurvalue(new Integer(0));
		return name;
	}
    public void ejbLoad() throws RemoteException {
    }
    public void ejbStore() throws RemoteException {
    }
    public void ejbRemove() throws RemoveException, RemoteException {
    }
    public void ejbActivate() throws RemoteException {
    }
    public void ejbPassivate() throws RemoteException {
    }
    public void setEntityContext(EntityContext entityContext) throws RemoteException {
        this.entityContext = entityContext;
    }
    public void unsetEntityContext() throws RemoteException {
        entityContext = null;
    }
    public abstract Integer getCurvalue();
    public abstract void setCurvalue(Integer curvalue);
    public abstract String getName();
    public abstract void setName(String name);
    public void ejbPostCreate(String name) throws CreateException, RemoteException {
    }
}