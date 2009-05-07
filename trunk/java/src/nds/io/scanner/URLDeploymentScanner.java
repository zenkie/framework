/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package nds.io.scanner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import nds.io.protocol.URLLister;
import nds.io.protocol.URLListerFactory;
import nds.io.protocol.URLStreamHandlerFactory;
import nds.util.StringUtils;


/**
 * A URL-based deployment scanner.  Supports local directory
 * scanning for file-based urls.
 *
 * @jmx:mbean extends="org.jboss.deployment.scanner.DeploymentScannerMBean"
 *
 * @version <tt>$Revision: 1.1.1.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class URLDeploymentScanner
   extends AbstractDeploymentScanner
{
    private static void   internalInitURLHandlers()
  {
     // Install a URLStreamHandlerFactory that uses the TCL
     URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory());

     // Preload JBoss URL handlers
     URLStreamHandlerFactory.preload();

     // Include the default JBoss protocol handler package
     String handlerPkgs = System.getProperty("java.protocol.handler.pkgs");
     if (handlerPkgs != null)
     {
        if(handlerPkgs.indexOf("nds.io.protocol")<0)handlerPkgs += "|nds.io.protocol";
     }
     else
     {
        handlerPkgs = "nds.io.protocol";
     }
     System.setProperty("java.protocol.handler.pkgs", handlerPkgs);
  }


   /** The list of URLs to scan. */
   protected List urlList = Collections.synchronizedList(new ArrayList());

   /** A set of scanned urls which have been deployed. */
   protected Set deployedSet = Collections.synchronizedSet(new HashSet());

   /** The server's home directory, for relative paths. */
   protected File serverHome;

   protected URL serverHomeURL;

   /** A sorter urls from a scaned directory to allow for coarse dependency
    ordering based on file type
    */
   protected Comparator sorter;

   /** Allow a filter for scanned directories */
   protected URLLister.URLFilter filter;
   protected Exception lastIncompleteDeploymentException;

   // indicates if we should directly search for files inside directories
   // whose names containing no dots
   //
   protected boolean doRecursiveSearch = true;


   /**
    * @jmx:managed-attribute
    */
   public void setRecursiveSearch (boolean recurse)
   {
      doRecursiveSearch = recurse;
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean getRecursiveSearch ()
   {
      return doRecursiveSearch;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setURLList(final List list)
   {
      if (list == null)
         throw new IllegalArgumentException("list");


      // start out with a fresh list
      urlList.clear();

      Iterator iter = list.iterator();
      while (iter.hasNext())
      {
         URL url = (URL)iter.next();
         if (url == null)
            throw new IllegalArgumentException("list element");

         addURL(url);
      }

         log.debug("URL list: " + urlList);
   }

   /**
    * @jmx:managed-attribute
    *
    * @param classname    The name of a Comparator class.
    */
   public void setURLComparator(String classname)
      throws ClassNotFoundException, IllegalAccessException,
      InstantiationException
   {
      sorter = (Comparator)Thread.currentThread().getContextClassLoader().loadClass(classname).newInstance();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getURLComparator()
   {
      if (sorter == null)
         return null;
      return sorter.getClass().getName();
   }
   /**
    * Set extension filter
    * @param extension the file extension including dot, such as ".txt", ".log"
    */
   public void setExtensionFilter(String extension){
       setFilter(new ExtensionFilter(extension));
   }
   /**
    */
   public void setFilter(URLLister.URLFilter filter){
       this.filter = filter;
   }
/*   public void setFilter(String classname)
   throws ClassNotFoundException, IllegalAccessException, InstantiationException
   {
      Class filterClass = Thread.currentThread().getContextClassLoader().loadClass(classname);

      filter = (URLLister.URLFilter) filterClass.newInstance();
   }*/

   /**
    * @jmx:managed-attribute
    */
   public URLLister.URLFilter getFilter()
   {
      return filter;
   }

   /**
    * @jmx:managed-attribute
    */
   public List getURLList()
   {
      // too bad, List isn't a cloneable
      return new ArrayList(urlList);
   }

   /**
    * @jmx:managed-operation
    */
   public void addURL(final URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("url");

      urlList.add(url);
      log.debug("Added url: " + url);
   }

   /**
    * @jmx:managed-operation
    */
   public void removeURL(final URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("url");

      boolean success = urlList.remove(url);
      if (success )
      {
         log.debug("Removed url: " + url);
      }
   }

   /**
    * @jmx:managed-operation
    */
   public boolean hasURL(final URL url)
   {
      if (url == null)
         throw new IllegalArgumentException("url");

      return urlList.contains(url);
   }


   /////////////////////////////////////////////////////////////////////////
   //                  Management/Configuration Helpers                   //
   /////////////////////////////////////////////////////////////////////////

   /**
    * @jmx:managed-attribute
    */
   public void setURLs(final String listspec) throws MalformedURLException
   {
      if (listspec == null)
         throw new IllegalArgumentException("listspec");


      List list = new LinkedList();

      StringTokenizer stok = new StringTokenizer(listspec, ",");
      while (stok.hasMoreTokens())
      {
         String urlspec = stok.nextToken().trim();

            log.debug("Adding URL from spec: " + urlspec);

         URL url = makeURL(urlspec);
            log.debug("URL: " + url);
         list.add(url);
      }

      setURLList(list);
   }

   /**
    * A helper to make a URL from a full url, or a filespec.
    */
   protected URL makeURL(String urlspec) throws MalformedURLException
   {
      // First replace URL with appropriate properties
      //
      urlspec = StringUtils.replaceProperties (urlspec);
      return new URL(serverHomeURL, urlspec);
   }

   /**
    * @jmx:managed-operation
    */
   public void addURL(final String urlspec) throws MalformedURLException
   {
      addURL(makeURL(urlspec));
   }

   /**
    * @jmx:managed-operation
    */
   public void removeURL(final String urlspec) throws MalformedURLException
   {
      removeURL(makeURL(urlspec));
   }

   /**
    * @jmx:managed-operation
    */
   public boolean hasURL(final String urlspec) throws MalformedURLException
   {
      return hasURL(makeURL(urlspec));
   }



   /**
    * Checks if the url is in the deployed set.
    */
   protected boolean isDeployed(final URL url)
   {
       return false;
   }

   public synchronized void scan() throws Exception
   {
      lastIncompleteDeploymentException = null;
      if (urlList == null)
         throw new IllegalStateException("not initialized");

      URLListerFactory factory = new URLListerFactory();
      List urlsToDeploy = new LinkedList();

      // Scan for deployments
  //       log.debug("Scanning for new deployments");
      synchronized (urlList)
      {
         for (Iterator i = urlList.iterator(); i.hasNext();)
         {
            URL url = (URL) i.next();
            if (url.toString().endsWith("/"))
            {
               // treat URL as a collection
               URLLister lister = factory.createURLLister(url);
               urlsToDeploy.addAll(lister.listMembers(url, filter, doRecursiveSearch));
            }
            else
            {
               // treat URL as a deployable unit
               urlsToDeploy.add(url);
            }
         }
      }

//         log.debug("Updating existing deployments");
      LinkedList urlsToRemove = new LinkedList();
      LinkedList urlsToCheckForUpdate = new LinkedList();
      synchronized (deployedSet)
      {
         // remove previously deployed URLs no longer needed
         for (Iterator i = deployedSet.iterator(); i.hasNext();)
         {
            DeployedURL deployedURL = (DeployedURL) i.next();
            if (urlsToDeploy.contains(deployedURL.url))
            {
               urlsToCheckForUpdate.add(deployedURL);
            }
            else
            {
               urlsToRemove.add(deployedURL);
            }
         }
      }

      // ********
      // Undeploy
      // ********

      for (Iterator i = urlsToRemove.iterator(); i.hasNext();)
      {
         DeployedURL deployedURL = (DeployedURL) i.next();
            log.debug("Removing " + deployedURL.url);
         undeploy(deployedURL);
      }

      // ********
      // Redeploy
      // ********

      // compute the DeployedURL list to update
      ArrayList urlsToUpdate = new ArrayList(urlsToCheckForUpdate.size());
      for (Iterator i = urlsToCheckForUpdate.iterator(); i.hasNext();)
      {
         DeployedURL deployedURL = (DeployedURL) i.next();
         if (deployedURL.isModified())
         {
               log.debug("Re-deploying " + deployedURL.url);
               urlsToUpdate.add(deployedURL);
         }
      }

      // sort to update list
      Collections.sort(urlsToUpdate, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            return sorter.compare(((DeployedURL) o1).url, ((DeployedURL) o2).url);
         }
      });

      // Undeploy in order
      for (int i = urlsToUpdate.size() - 1; i >= 0;i--)
      {
         redeploy((DeployedURL) urlsToUpdate.get(i));
      }


      // ******
      // Deploy
      // ******

      Collections.sort(urlsToDeploy, sorter);
      for (Iterator i = urlsToDeploy.iterator(); i.hasNext();)
      {
         URL url = (URL) i.next();
         DeployedURL deployedURL = new DeployedURL(url);
         if (deployedSet.contains(deployedURL) == false)
         {
            log.debug("Deploying " + deployedURL.url);
            deploy(deployedURL);
         }
      }

      // Validate that there are still incomplete deployments
      if (lastIncompleteDeploymentException != null)
      {
         try
         {
            Object[] args = {};
            String[] sig = {};
//            getServer().invoke(getDeployer(),
//                               "checkIncompleteDeployments", args, sig);
         }
         catch (Exception e)
         {
            log.error("",e);
         }
      }
   }
   protected void deploy(final DeployedURL du){
       du.deployed();

       if (!deployedSet.contains(du))
       {
          deployedSet.add(du);
       }
       fireURLDeployed(du);
   }
   /**
    * A helper to undeploy the given URL from the deployer.
    */
   protected void undeploy(final DeployedURL du)
   {
      try
      {
         deployedSet.remove(du);
         fireURLRemoved(du);
      }
      catch (Exception e)
      {
         log.error("Failed to undeploy: " + du, e);
      }
   }
   protected void redeploy(final DeployedURL du){
       du.deployed();

       if (!deployedSet.contains(du))
       {
          deployedSet.add(du);
       }
       fireURLModified(du);
   }

}
