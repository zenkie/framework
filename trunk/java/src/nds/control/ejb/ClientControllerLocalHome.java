package nds.control.ejb;

import javax.ejb.CreateException;

public interface ClientControllerLocalHome extends javax.ejb.EJBLocalHome {
    public ClientControllerLocal create() throws CreateException;
}