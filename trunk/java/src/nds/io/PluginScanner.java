package nds.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.swing.event.EventListenerList;

import nds.control.web.WebUtils;
import nds.io.scanner.DeployedURL;
import nds.io.scanner.DeploymentEvent;
import nds.io.scanner.DeploymentListener;
import nds.io.scanner.URLDeploymentScanner;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.FileUtils;
import nds.util.NDSRuntimeException;
import nds.util.WebKeys;
/**
 * Scan worker for PluginManager, only one thread starts for all plugin jars
 * @author yfzhu
 *
 */
public class PluginScanner{
	private static Logger logger= LoggerManager.getInstance().getLogger(PluginManager.class.getName());

	protected EventListenerList listenerList = new EventListenerList();

	private ClassLoader loader;
	private URLDeploymentScanner scanner;
	private URL scanURL;
	public PluginScanner() {
		try{
			//new sanner
			scanner= new URLDeploymentScanner();
			scanner.setExtensionFilter(".jar"); // only jar is supported (no zip)
			scanner.setRecursiveSearch(false);
			scanner.setURLComparator("nds.io.scanner.DeploymentSorter");
			//scanner.setScanPeriod(60*1000);// default to one minute
	
			Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
			scanURL= toUrl(conf.getProperty("dir.plugin", "plugin"));
			 
			logger.debug("scan plugin in "+scanURL);
			scanner.addURL(scanURL);//(url, null, new ExtensionFilter(".jar"));
	

			scanner.addDeploymentListener(new DeploymentListener(){
			       public void urlDeployed(DeploymentEvent de){
			    	   initClassLoader();
			    	   fireURLDeployed((DeployedURL)de.getSource());
			       }
			       public void urlModified(DeploymentEvent de){
			    	   initClassLoader();
			    	   fireURLModified((DeployedURL)de.getSource());
			       }
			       public void urlRemoved(DeploymentEvent de){
			    	   initClassLoader();
			    	   fireURLRemoved((DeployedURL)de.getSource());
			       }
			});
			scanner.createService();
			scanner.setScanEnabled(true);

			scanner.startService();	
			
		}catch(Exception e){
			logger.error("Fail to start scanner for plugin", e);
			throw new NDSRuntimeException("Fail to start scanner", e);
		}
	}
	private void initClassLoader(){
		//rescan dirs for jars
		ArrayList<URL> jars=new ArrayList(); //holding jar file list
		
		try{
			File f=new File(scanURL.toURI());
			File[] js= FileUtils.getFiles(f, null,".jar");
			for(int i=0;i<js.length;i++) jars.add(js[i].toURI().toURL());
		}catch(Throwable t){
			logger.error("Fail to add jars in url :"+ scanURL, t);
		}

		URL[] s=new URL[jars.size()];
		loader=new URLClassLoader( jars.toArray(s), this.getClass().getClassLoader() ); 
		
	}
	public URLDeploymentScanner getURLDeploymentScanner(){
		return scanner;
	}
	public ClassLoader getPluginClassLoader(){
		return loader;
	}
	public void destroy(){
		try {
			scanner.destroyService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Fail to destroy plugin scanner", e);
		}
		scanner=null;
		loader=null;
	}
/**
    * Convert a string to a node
    */
   private URL toUrl(String value) throws MalformedURLException
   {
      try
      {
         return new URL(value);
      } catch (MalformedURLException e)
      {
         File file = new File(value);
         if (!file.isAbsolute())
         {
        	//relative to portal.properties#dir.nea.root+"/plugin"
	        Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
	        String root=conf.getProperty("dir.nea.root","/portal/act.nea");
        	
            file = new File(root, value);
         }

         try
         {
            file = file.getCanonicalFile();
         } catch (IOException ioe)
         {
            throw new MalformedURLException("Can not obtain file: " + ioe);
         }

         return file.toURI().toURL();
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
	/*public static PluginScanner getInstance() {
		if(instance==null) instance= new PluginScanner();
		return instance;
		
	}*/
}
