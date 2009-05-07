/******************************************************************
*
*$RCSfile: Status.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: Status.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/
package nds.util;


/**
* This interface must be implemented by all those classes that are queried for their status at runtime.
*/
public interface Status {

    /**
     Returns information about the status of the implementing class.
    @roseuuuid 3A6D303B0020
    */
    public String getStatus();
}
