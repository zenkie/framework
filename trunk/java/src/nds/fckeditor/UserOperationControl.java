package nds.fckeditor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nds.control.web.*;
import nds.util.*;
import nds.velocity.WebappLoader;
import net.fckeditor.requestcycle.*;

/**
 * Implements path builder and action authentication
 * @author yfzhu
 *
 */
public class UserOperationControl implements UserAction, UserPathBuilder {
	private static final Log logger = LogFactory.getLog(UserOperationControl.class);	
	/**
	 * root path for websites of clients 
	 */
	private String clientWebRoot;
	
	public UserOperationControl(){
	    Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);	    
	    clientWebRoot=conf.getProperty("client.webroot","/act/webroot");
		
	}
	/* (non-Javadoc)
	 * @see net.fckeditor.requestcycle.UserAction#isEnabledForFileBrowsing(javax.servlet.http.HttpServletRequest)
	 */
	public boolean isEnabledForFileBrowsing(final HttpServletRequest request) {
		UserWebImpl userWeb =null;
    	try{
    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));	
    	}catch(Throwable userWebException){
    		
    	}
    	if(userWeb==null || userWeb.getUserId()==UserWebImpl.GUEST_ID ){
    		logger.warn("UserWebImpl not find or is guest when request client web file");
    		return false;
    	}
    	
    	//String domain= userWeb.getClientDomain();
		
		return true;
	}

	/* (non-Javadoc)
	 * @see net.fckeditor.requestcycle.UserAction#isEnabledForFileUpload(javax.servlet.http.HttpServletRequest)
	 */
	public boolean isEnabledForFileUpload(final HttpServletRequest request) {
		UserWebImpl userWeb =null;
    	try{
    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));	
    	}catch(Throwable userWebException){
    		
    	}
    	if(userWeb==null || userWeb.getUserId()==UserWebImpl.GUEST_ID ){
    		logger.debug("UserWebImpl not find or is guest when request client web file");
    		return false;
    	}
    	
    	// user has permission to write U_NEWS table
    	try{
    		userWeb.checkPermission("U_NEWS_LIST",nds.security.Directory.WRITE);
    	}catch(Throwable t){
    		logger.debug("no permission to wirte U_NEWS_LIST:"+ t);
    		return false;
    	}
		return true;
	}
	/**
	 *  
	 * @param request
	 * @return clientWebRoot+"/"+domain
	 */
	public String getUserFilesPath(final HttpServletRequest request){
		UserWebImpl userWeb =null;
    	try{
    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));	
    	}catch(Throwable userWebException){
    		
    	}
    	if(userWeb==null || userWeb.getUserId()==UserWebImpl.GUEST_ID ){
    		logger.warn("UserWebImpl not find or is guest when request client web file");
    		throw new nds.util.NDSRuntimeException("UserWebImpl not find or is guest when request client web file");
    	}
    	String domain= userWeb.getClientDomain();
    	return clientWebRoot+"/"+domain;
		
	}
}



