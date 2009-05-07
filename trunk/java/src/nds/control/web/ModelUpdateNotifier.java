/******************************************************************
*
*$RCSfile: ModelUpdateNotifier.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: ModelUpdateNotifier.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/11/29 00:48:31  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/

package nds.control.web;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpSession;

import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
/**
 * This class is responsible for providing methods to add objects as listeners
 * for a particular model update event and for notifying the listeners when the
 * event actually occurs.
 */
public class ModelUpdateNotifier implements java.io.Serializable {

    private HashMap listenerMap;

    public ModelUpdateNotifier() {
        listenerMap = new HashMap();
    }
    /**
     * @param updatedModelList - collections with elements type of String,
     *        are model type name. So the listeners which registered to that
     *        model type will be notified to update.
     * @param value - the ValueHolder which contains information needed for Listener to
     *        update its status
     * @see #addListener, RequestProcessor#processRequest
     *
     */
    public void notifyListeners(Collection updatedModelList, ValueHolder value, HttpSession session) throws
        NDSEventException {
        if(updatedModelList ==null)
            return;
        for (Iterator it1 = updatedModelList.iterator() ; it1.hasNext() ;) {
            String modelType = (String) it1.next();
            Collection listeners = (Collection)listenerMap.get(modelType);
            if (listeners != null) {
                for (Iterator it2 = listeners.iterator(); it2.hasNext(); ) {
                    ((ModelUpdateListener) it2.next()).performUpdate(value,session);
                }
            }
        }
    }

    public void addListener(String modelType, Object listener) {

        if (listenerMap.get(modelType) == null) {
            ArrayList listeners = new ArrayList();
            listeners.add(listener);
            listenerMap.put(modelType,listeners);
        } else {
            ((ArrayList) listenerMap.get(modelType)).add(listener);
        }
    }
}

