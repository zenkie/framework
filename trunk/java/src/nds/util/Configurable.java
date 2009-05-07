/******************************************************************
*
*$RCSfile: Configurable.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: Configurable.java,v $
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
Initialize the class by passing its configurations.
*/
public interface Configurable {

    /**Initialize the class by passing its configurations.
    @roseuuuid 3A6D300600CE
    */
    public void init(Configurations conf) throws InitializationException;
}
