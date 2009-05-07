/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package nds.io.scanner;

import javax.swing.event.EventListenerList;

import nds.log.Logger;
import nds.log.LoggerManager;
/**
 * An abstract support class for implementing a deployment scanner.
 *
 * <p>Provides the implementation of period-based scanning, as well
 *    as Deployer integration.
 *
 * <p>Sub-classes only need to implement {@link DeploymentScanner#scan}.
 *
 */
public abstract class AbstractDeploymentScanner   implements DeploymentScanner
{
   protected Logger log= LoggerManager.getInstance().getLogger(this.getClass().getName());
   protected EventListenerList listenerList = new EventListenerList();
   /** The scan period in milliseconds */
   protected long scanPeriod = 5000L;

   /** True if period based scanning is enabled. */
   protected boolean scanEnabled = true;

   /** The scanner thread. */
   protected ScannerThread scannerThread;

   /** HACK: Shutdown hook to get around problems with system service shutdown ordering. */
   private Thread shutdownHook;


   /////////////////////////////////////////////////////////////////////////
   //                           DeploymentScanner                         //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Period must be >= 0.
    */
   public void setScanPeriod(final long period)
   {
      if (period < 0)
         throw new IllegalArgumentException("ScanPeriod must be >= 0; have: " + period);

      this.scanPeriod=period;
   }

   public long getScanPeriod()
   {
      return scanPeriod;
   }

   public void setScanEnabled(final boolean flag)
   {
      this.scanEnabled=flag;
   }

   public boolean isScanEnabled()
   {
      return scanEnabled;
   }

   /** This is here to work around a bug in the IBM vm that causes an
    * AbstractMethodError to be thrown when the ScannerThread calls scan.
    * @throws Exception
    */
   public abstract void scan() throws Exception;

   /////////////////////////////////////////////////////////////////////////
   //                           Scanner Thread                            //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Should use Timer/TimerTask instead?  This has some issues with
    * interaction with ScanEnabled attribute.  ScanEnabled works only
    * when starting/stopping.
    */
   public class ScannerThread
      extends Thread
   {
      /** We get our own logger. */
      protected Logger log = LoggerManager.getInstance().getLogger(ScannerThread.class.getName());

      /** True if the scan loop should run. */
      protected boolean enabled;

      /** True if we are shutting down. */
      protected boolean shuttingDown;

      /** Lock/notify object. */
      protected Object lock = new Object();

      public ScannerThread(boolean enabled)
      {
         super("ScannerThread");

         this.enabled = enabled;
      }

      public void setEnabled(boolean enabled)
      {
         this.enabled = enabled;

         synchronized (lock)
         {
            lock.notifyAll();
         }

         log.debug("Notified that enabled: " + enabled);
      }

      public void shutdown()
      {
         enabled = false;
         shuttingDown = true;

         synchronized (lock)
         {
            lock.notifyAll();
         }

            log.debug("Notified to shutdown");

         // jason: shall we also interrupt this thread?
      }

      public void run()
      {
         log.info("Running");

         while (!shuttingDown)
         {

            // If we are not enabled, then wait
            if (!enabled)
            {
               try
               {
                  log.debug("Disabled, waiting for notification");
                  synchronized (lock)
                  {
                     lock.wait();
                  }
               }
               catch (InterruptedException ignore) {}
            }

            loop();
         }

         log.info("Shutdown");
      }

      public void doScan()
      {
         // Scan for new/removed/changed/whatever
         try {
            scan();
         }
         catch (Exception e) {
            log.error("Scanning failed; continuing", e);
         }
      }

      protected void loop()
      {
         while (enabled)
         {
            doScan();

            // Sleep for scan period
            try
            {
               //log.debug("Sleeping...");
               Thread.sleep(scanPeriod);
            }
            catch (InterruptedException ignore) {}
         }
      }
   }

   public void addDeploymentListener(DeploymentListener l){
       listenerList.add(DeploymentListener.class, l);
   }

   public void removeDeploymentListener(DeploymentListener l){
       listenerList.remove(DeploymentListener.class, l);
   }

   protected void fireURLDeployed(DeployedURL url)
   {
       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if (listeners[i] == DeploymentListener.class) {
               ((DeploymentListener) listeners[i + 1]).urlDeployed(new DeploymentEvent(url));
           }
       }

   }
   protected void fireURLModified(DeployedURL url)
   {
       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if (listeners[i] == DeploymentListener.class) {
               ((DeploymentListener) listeners[i + 1]).urlModified(new DeploymentEvent(url));
           }
       }

   }
   protected void fireURLRemoved(DeployedURL url)
   {
       Object[] listeners = listenerList.getListenerList();
       // Process the listeners last to first, notifying
       // those that are interested in this event
       for (int i = listeners.length - 2; i >= 0; i -= 2) {
           if (listeners[i] == DeploymentListener.class) {
               ((DeploymentListener) listeners[i + 1]).urlRemoved(new DeploymentEvent(url));
           }
       }

   }

   /////////////////////////////////////////////////////////////////////////
   //                     Service/ServiceMBeanSupport                     //
   /////////////////////////////////////////////////////////////////////////

   public void createService() throws Exception
   {

      // setup + start scanner thread
      scannerThread = new ScannerThread(false);
      scannerThread.setDaemon(true);
      scannerThread.start();
      log.debug("Scanner thread started");

      // HACK
      //
      // install a shutdown hook, as the current system service shutdown
      // mechanism will not call this until all other services have stopped.
      // we need to know soon, so we can stop scanning to try to avoid
      // starting new services when shutting down

      final ScannerThread _scannerThread = scannerThread;
      shutdownHook = new Thread("DeploymentScanner Shutdown Hook")
         {
            ScannerThread scannerThread = _scannerThread;

            public void run()
            {
               scannerThread.shutdown();
            }
         };

      try
      {
         Runtime.getRuntime().addShutdownHook(shutdownHook);
      }
      catch (Exception e)
      {
         log.error("Failed to add shutdown hook", e);
      }
   }

   public void startService() throws Exception
   {
      synchronized( scannerThread )
      {
         // scan before we enable the thread, so JBoss version shows up afterwards
         scannerThread.doScan();

         // enable scanner thread if we are enabled
         scannerThread.setEnabled(scanEnabled);
      }
   }

   public void stopService() throws Exception
   {
      // disable scanner thread
      if( scannerThread != null )
         scannerThread.setEnabled(false);
   }

   public void destroyService() throws Exception
   {

      // shutdown scanner thread
      if( scannerThread != null )
      {
         synchronized( scannerThread )
         {
            scannerThread.shutdown();
         }
      }

      // HACK
      //
      // remove the shutdown hook, we don't need it anymore
      try
      {
         Runtime.getRuntime().removeShutdownHook(shutdownHook);
      }
      catch (Exception ignore)
      {
      } // who cares really

      // help gc
      shutdownHook = null;
      scannerThread = null;
   }
}
