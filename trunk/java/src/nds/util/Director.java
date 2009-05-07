/******************************************************************
*
*$RCSfile: Director.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: Director.java,v $
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
import java.util.Enumeration;

/**
 * A Director is an actor manager. Actors refer to their director to
 * get the actor associated to the respective role. This is useful
 * to decouple the acting role from the actual class performing
 * that part in the play.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2005/03/15 11:23:26 $
 */

public interface Director {

    /**
     * Get the actor currently playing the given role.
     */
    Object getActor(String role);

    /**
     * Set the actor for the role.
     */
    void setRole(String role, Object actor);

    /**
     * Get the roles currently set.
     */
    Enumeration getRoles();

}
