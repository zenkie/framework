/******************************************************************
*
*$RCSfile: ModelUpdateListener.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: ModelUpdateListener.java,v $
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
/*
 * $Id: ModelUpdateListener.java,v 1.1.1.1 2005/03/15 11:23:15 Administrator Exp $
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2001 Sun Microsystems, Inc. Tous droits réservés.
 */

package nds.control.web;
import javax.servlet.http.HttpSession;

import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
/**
 * This interface is implemented by objects which are interested in
 * getting the model update events. For example, CustomerWebImpl implements
 * this interface to get itself updated when account model gets updated.
*/

public interface ModelUpdateListener {
    /**
     * Update according to information in <code>value</code>
     */
    public void performUpdate(ValueHolder value,HttpSession session) throws  NDSEventException;

}



