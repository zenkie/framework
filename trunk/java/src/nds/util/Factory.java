/******************************************************************
*
*$RCSfile: Factory.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: Factory.java,v $
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

import java.util.Vector;

/**
 * A factory is responsible to create and properly initialize
 * dynamically loaded classes. The use of dynamic linking allows
 * simpler management and stronger decoupling between the core
 * classes and the actors.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2005/03/15 11:23:26 $
 */

public interface Factory extends Actor {

    /**
     * Create the instance of a class given its name.
     */
    Object create(String name);

    /**
     * Create the instance of a class and, if configurable, use
     * the given configurations to configure it.
     */
    Object create(String name, Configurations conf);

    /**
     * Create a vector of instances given a vector
     * of strings indicating their respective names.
     */
    Vector create(Vector names);

    /**
     * Create a vector of instances with given configurations.
     */
    Vector create(Vector names, Configurations conf);

}
