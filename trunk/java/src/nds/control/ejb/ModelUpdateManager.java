/******************************************************************
*
*$RCSfile: ModelUpdateManager.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: ModelUpdateManager.java,v $
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
//Source file: F:\\work2\\tmp\\nds\\control\\ejb\\ModelUpdateManager.java

package nds.control.ejb;

import java.util.Collection;

import nds.control.event.NDSEvent;

/**
 * This class uses the NDSEvent type to deduce the list of
 * models that need to be updated because of this event.
 */
public class ModelUpdateManager  implements java.io.Serializable {

    public ModelUpdateManager() {
    }

     /**
     * @return a list of names of models that could have changed due to this event.
     * The names chosen to refer to models is taken from JNDINames.
     * @see com.sun.j2ee.blueprints.petstore.util.JNDINames
     */
    public Collection getUpdatedModels(NDSEvent ese) {
        return null;
    }

}