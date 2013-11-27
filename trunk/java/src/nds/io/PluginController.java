package nds.io;
import java.util.*;
import javax.servlet.ServletContext;

import nds.control.web.WebUtils;
import nds.control.web.binhandler.BinaryHandler;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.Director;
import nds.util.ServletContextActor;
import nds.util.WebKeys;
import nds.control.ejb.*;
import nds.process.*;
import nds.web.button.*;
import nds.web.shell.*;

public class PluginController implements ServletContextActor{
	private Logger logger= LoggerManager.getInstance().getLogger(PluginController.class.getName());
	
	private PluginManager<Command> cmdManager;
	private PluginManager<ProcessCall> psManager;
	private PluginManager<ShellCmd> shellManager;
	private PluginManager<BinaryHandler> binhander;
	
	private PluginScanner scanner;
	public void init(Director director) {
        logger.debug("PluginController initialized.");
        Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
        
        /**
         * Following is to disable cache of URLConnection, so plugin can be reloaded without such error:

java.util.ServiceConfigurationError: nds.control.ejb.Command: Error reading configuration file
        at java.util.ServiceLoader.fail(ServiceLoader.java:207)
        at java.util.ServiceLoader.parse(ServiceLoader.java:284)
        at java.util.ServiceLoader.access$200(ServiceLoader.java:164)
        at java.util.ServiceLoader$LazyIterator.hasNext(ServiceLoader.java:332)
        at java.util.ServiceLoader$1.hasNext(ServiceLoader.java:415)
        at nds.io.PluginManager.reload(PluginManager.java:94)
        at nds.io.PluginManager.urlModified(PluginManager.java:74)
        at nds.io.PluginScanner.fireURLModified(PluginScanner.java:165)
        at nds.io.PluginScanner$1.urlModified(PluginScanner.java:58)
        at nds.io.scanner.AbstractDeploymentScanner.fireURLModified(AbstractDeploymentScanner.java:218)
        at nds.io.scanner.URLDeploymentScanner.redeploy(URLDeploymentScanner.java:460)
        at nds.io.scanner.URLDeploymentScanner.scan(URLDeploymentScanner.java:393)
        at nds.io.scanner.AbstractDeploymentScanner$ScannerThread.doScan(AbstractDeploymentScanner.java:167)
        at nds.io.scanner.AbstractDeploymentScanner$ScannerThread.loop(AbstractDeploymentScanner.java:178)
        at nds.io.scanner.AbstractDeploymentScanner$ScannerThread.run(AbstractDeploymentScanner.java:157)
Caused by: java.io.FileNotFoundException: JAR entry META-INF/services/nds.control.ejb.Command not found in E:\portal\act.nea\plugin\command-ext.jar
        at sun.net.www.protocol.jar.JarURLConnection.connect(JarURLConnection.java:122)
        at sun.net.www.protocol.jar.JarURLConnection.getInputStream(JarURLConnection.java:132)
        at java.net.URL.openStream(URL.java:1009)
        at java.util.ServiceLoader.parse(ServiceLoader.java:279)
        ... 13 more

        http://stackoverflow.com/questions/1374438/disappearing-jar-entry-when-loading-using-spi
        http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6549146
         */
        try{
        String serverURL= conf.getProperty("server.url", "http://localhost");
        new java.net.URL(serverURL).openConnection().setDefaultUseCaches(false);
        }catch(Throwable t){}

        scanner=new PluginScanner();
        
        cmdManager=new PluginManager<Command>(Command.class, scanner);
        cmdManager.init();
        
        psManager=new PluginManager<ProcessCall>(ProcessCall.class, scanner);
        psManager.init();
        
        shellManager=new PluginManager<ShellCmd>(ShellCmd.class, scanner);
        shellManager.init();
        
        binhander=new PluginManager<BinaryHandler>(BinaryHandler.class, scanner);
        binhander.init();
        
    }
	/**
	 * Usage:
	 * 
		nds.io.PluginController pc=(nds.io.PluginController) WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PLUGIN_CONTROLLER);
        Command cmd= pc.findPluginCommand(command);

	 * @param name
	 * @return
	 */
	    public PluginScanner getPluginScanner()
	   {
	    return scanner;
	   }
	    
	 public BinaryHandler findPluginBinhandle(String name){
			return binhander.findPlugin(name);
	}
	
	public Command findPluginCommand(String name){
		return cmdManager.findPlugin(name);
	}
	public ShellCmd findPluginShellCmd(String name){
		return shellManager.findPlugin(name);
	}
	public Iterator<ShellCmd> listShellCmds(){
		return shellManager.plugins();
	}
	/**
	 * Each process object will be new one
	 * @param name
	 * @return null if not found
	 */
	public ProcessCall newPluginProcess(String name){
		ProcessCall pc=psManager.findPlugin(name);
		if(pc!=null){
			try{
				return pc.getClass().newInstance();
			}catch(Throwable t){
				logger.error("fail to new instance of "+ pc.getClass()+":"+t);
			}
		}
		return null;
	}
    public void init(ServletContext context) {

    }
    public void destroy() {
    	try{cmdManager.destroy();}catch(Throwable t){}
    	try{psManager.destroy();}catch(Throwable t){}
    	try{shellManager.destroy();}catch(Throwable t){}
    	try{binhander.destroy();}catch(Throwable t){}
    	scanner.destroy();
    	
        logger.debug("PluginController destroied.");
    }
}
