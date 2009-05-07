/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package nds.io.protocol.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import nds.io.protocol.DelegatingURLConnection;
import nds.log.Logger;
import nds.log.LoggerManager;

/**
 * Provides access to system resources as a URLConnection.
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ResourceURLConnection
   extends DelegatingURLConnection
{
   private static final Logger log = 
   	LoggerManager.getInstance().getLogger(ResourceURLConnection.class.getName());
   
   public ResourceURLConnection(final URL url)
      throws MalformedURLException, IOException
   {
      super(url);
   }

   protected URL makeDelegateUrl(final URL url)
      throws MalformedURLException, IOException
   {
      String name = url.getHost();
      String file = url.getFile();
      if (file != null && !file.equals("")) {
         name += file;
      }

      // first try TCL and then SCL

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL target = cl.getResource(name);

      if (target == null) {
         cl = ClassLoader.getSystemClassLoader();
         target = cl.getResource(name);
      }
      
      if (target == null)
         throw new FileNotFoundException("Could not locate resource: " + name);

      if (true) {
         log.debug("Target resource URL: " + target);
         try {
            log.debug("Target resource URL connection: " + target.openConnection());
         }
         catch (Exception ignore) {}
      }
      
      return target;
   }
}
