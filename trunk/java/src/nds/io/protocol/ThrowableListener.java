/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package nds.io.protocol;

import java.util.EventListener;

/**
 * An interface used to handle <tt>Throwable</tt> events.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface ThrowableListener
   extends EventListener
{
   /**
    * Process a throwable.
    *
    * @param type    The type off the throwable.
    * @param t       Throwable
    */
   void onThrowable(int type, Throwable t);
}
