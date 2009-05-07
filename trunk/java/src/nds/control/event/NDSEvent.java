/******************************************************************
*
*$RCSfile: NDSEvent.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:15 $
*
*$Log: NDSEvent.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
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
//Source file: F:\\work2\\tmp\\nds\\control\\event\\NDSEvent.java

package nds.control.event;


public interface NDSEvent extends java.io.Serializable {
    /**
    *   Specifiy a logical name that is mapped to the event in
    *   in the Universal Remote Controller.
    */
   public String getEventName();
   
}
