package nds.web.config;

import java.util.Hashtable;

import javax.servlet.ServletContext;

import org.shiftone.cache.Cache;

import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.Director;
import nds.util.NDSRuntimeException;
import nds.util.ServletContextActor;
import nds.util.WebKeys;

public class PortletConfigManager implements ServletContextActor{
    private static final int DEFAULT_CACHE_TIME_OUT= 60 *300;// default to 300 minutes
    private static final int DEFAULT_CACHE_MAXIMUM_SIZE=300;// default to 30 elements cached for one session
	
    private Logger logger= LoggerManager.getInstance().getLogger(PortletConfigManager.class.getName());
    private Cache cache; // key: Object, value: Expression (user's filter on directory, or preferences)
    /**
     * @param timeout the miliseconds
     * @param maxSize the maximum elements in cache
     */
    public PortletConfigManager( ) {
    }
    /**
     * Remove from cache, so can reload configuration next time request
     * @param id
     * @param type PortletConfig.TYPE_XXX
     */
    public PortletConfig removePortletConfig(int id, int type){
    	String key=type+"_"+id;
    	PortletConfig pc= (PortletConfig)cache.remove(key);
    	if(pc!=null) cache.remove(type+"_"+ pc.getName());
    	return pc;
    }
    public int getCacheSize(){
    	return cache.size();
    }
    /**
     * Remove from cache, so can reload configuration next time request
     * @param id
     * @param type PortletConfig.TYPE_XXX
     */
    public PortletConfig removePortletConfig(String name, int type){
    	String key=type+"_"+name;
    	PortletConfig pc= (PortletConfig)cache.remove(key);
    	if(pc!=null) cache.remove(type+"_"+ pc.getId());
    	return pc;
    }
    /**
     * 
     * @param name
     * @param type PortletConfig.TYPE_XXX
     * @return
     * @throws Exception
     */
    public PortletConfig getPortletConfig(String name, int type ){
    	if(name ==null) return null;
    	try{
	    	String key=type+"_"+name;
	    	PortletConfig pc=(PortletConfig)cache.getObject(key);
	    	if(pc==null){
	    		pc=PortletConfigFactory.loadPortletConfig(name, type);
	    		if(pc!=null){
	    			cache.addObject(key, pc);
	    		}
	    	}
	    	return pc;
    	}catch(Throwable t){
    		logger.error("Fail to get PortletConfig name="+name+",type="+type, t);
    		return null;
    	}
    }
    /**
     * 
     * @param id
     * @param type PortletConfig.TYPE_XXX
     * @return
     * @throws Exception
     */
    public PortletConfig getPortletConfig(int id, int type ){
    	if(id<0) return null;
    	try{
	    	String key=type+"_"+id;
	    	PortletConfig pc=(PortletConfig)cache.getObject(key);
	    	if(pc==null){
	    		pc=PortletConfigFactory.loadPortletConfig(id, type);
	    		if(pc!=null)cache.addObject(key, pc);
	    	}
	    	return pc;
    	}catch(Throwable t){
    		logger.error("Fail to get PortletConfig id="+id+",type="+type, t);
    		return null;
    	}
    }
    
    public void init(Director director) {
        logger.debug("PortletConfigManager initialized.");
        Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
        long timeOut=DEFAULT_CACHE_TIME_OUT;
        try{
           timeOut=( new Integer(conf.getProperty("portletconf.cache.timeout"))).longValue()  ; // seconds
        }catch(Exception e){}
         int size=DEFAULT_CACHE_MAXIMUM_SIZE;
        try{
           size=( new Integer( conf.getProperty("portletconf.cache.size"))).intValue() ; // size
        }catch(Exception e2){}

        cache=org.shiftone.cache.CacheManager.getInstance().newCache(timeOut*1000, size);
         logger.debug("Create Cache, timeout="+ timeOut+" seconds, size="+ size);
    }
    public void init(ServletContext context) {

    }
    public void destroy() {
    	if(cache!=null) cache.clear();
        logger.debug("PortletConfigManager destroied.");
    }
    

}
