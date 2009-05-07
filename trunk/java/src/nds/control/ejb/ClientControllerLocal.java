package nds.control.ejb;

import nds.control.event.NDSEvent;
import nds.control.util.ValueHolder;
import nds.util.NDSException;

public interface ClientControllerLocal extends javax.ejb.EJBLocalObject {
    public ValueHolder handleEvent(NDSEvent ese) throws NDSException;
}