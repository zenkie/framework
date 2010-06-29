package nds.io;

import nds.util.*;
import nds.io.scanner.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import javax.swing.event.EventListenerList;


import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;

/**
 * 基于ServiceLoader实现
 * 
 * 内置一个独立的classloader和目录监视器，进行基于目录的jar包的加载
 * 1个jar 包中可以有多个plugin，而一旦pluginmanager监控的目录中发生了jar 文件的改变
 * 目前最简单的实践就是将整个plugin缓存清除，然后重新加载
 * 
 * 更好的方式可能是针对每个jar 而不是全体jar进行变更监控，仅卸载变更的jar中的plugin
 * 
 * 
 * @author yfzhu@agilecontrol.com
 *
 */
public class PluginManager<S> implements DeploymentListener {
    private static Logger logger= LoggerManager.getInstance().getLogger(PluginManager.class.getName());
	

	private Class<S> clazz;
	private PluginScanner scanner;
	private ServiceLoader<S> serviceLoader; 
	private Hashtable<String, S> plugins;
	/**
	 * 
	 * @param clz the plugin class 
	 */
	public PluginManager(Class<S> clz, PluginScanner scanner){
		this.clazz=clz;
		this.scanner=scanner;
		plugins=new Hashtable();
	}
	/**
	 * Call this when plugin manager no longer used 
	 */
	public void destroy() throws Exception{
		scanner.removeDeploymentListener(this);
		plugins.clear();
		serviceLoader.reload();
		serviceLoader=null;
		logger.debug("PluginManager for "+ clazz.getName()+" destroied");
	}
	/**
	 * create thread to monitor scan paths
	 */
	public synchronized void init() {
		if(serviceLoader!=null){
			throw new NDSRuntimeException("Plugin manager already initialized");
		}
		
		scanner.addDeploymentListener(this);
		
		reload();
	}
	
	public void urlDeployed(DeploymentEvent det){reload();}
    public void urlModified(DeploymentEvent de){reload();}
    public void urlRemoved(DeploymentEvent de){reload();}
	
	
	/**
	 * clear all cache and reload from disk, when
	 * scaner find any jar changes, this method will be called
	 * 
	 */
	public void reload(){
		
		try{
		synchronized(plugins){
			plugins.clear();

			if(serviceLoader!=null){
				serviceLoader.reload();
			}
			serviceLoader =ServiceLoader.load(clazz, scanner.getPluginClassLoader());
			
			for(S plugin:serviceLoader){
				plugins.put(plugin.getClass().getName(), plugin);
				logger.debug("load "+ plugin.getClass().getName());
			}
			
		}
		}catch(Throwable t){
			logger.error("Fail to reload plugin manager of class "+ this.clazz.getName(), t);
		}
	}
	/**
	 * Find plugin object of specfied class. 
	 * @param name class name
	 * @return null if not found
	 */
	public S findPlugin(String name){
		return plugins.get(name);
	}

	
}
