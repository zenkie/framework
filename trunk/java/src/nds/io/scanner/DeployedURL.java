package nds.io.scanner;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import nds.log.Logger;
import nds.log.LoggerManager;

public class DeployedURL {

    private static Logger log= LoggerManager.getInstance().getLogger(DeployedURL.class.getName());


   /**
    * A container and help class for a deployed URL.
    * should be static at this point, with the explicit scanner ref, but I'm (David) lazy.
    */
      URL url;
      long deployedLastModified;

      public DeployedURL(final URL url)
      {
         this.url = url;
      }

      public void deployed()
      {
         deployedLastModified = getLastModified();
      }
      public boolean isFile()
      {
         return url.getProtocol().equals("file");
      }

      public File getFile()
      {
         return new File(url.getFile());
      }

      public boolean isRemoved()
      {
         if (isFile())
         {
            File file = getFile();
            return !file.exists();
         }
         return false;
      }

      public long getLastModified()
      {
         try
         {
            URLConnection connection;
            if (url != null)
            {
               connection = url.openConnection();
            } else
            {
               connection = url.openConnection();
            }
            // no need to do special checks for files...
            // org.jboss.net.protocol.file.FileURLConnection correctly
            // implements the getLastModified method.
            long lastModified = connection.getLastModified();

            return lastModified;
         }
         catch (java.io.IOException e)
         {
            log.info("Failed to check modfication of deployed url: " + url, e);
         }

         return -1;
      }

      public boolean isModified()
      {
         long lastModified = getLastModified();
         if (lastModified == -1) {
            // ignore errors fetching the timestamp - see bug 598335
            return false;
         }
         return deployedLastModified != lastModified;
      }

      public int hashCode()
      {
         return url.hashCode();
      }

      public boolean equals(final Object other)
      {
         if (other instanceof DeployedURL)
         {
            return ((DeployedURL)other).url.equals(this.url);
         }
         return false;
      }

      public String toString()
      {
         return super.toString() +
         "{ url=" + url +
         ", deployedLastModified=" + deployedLastModified +
         " }";
      }
}