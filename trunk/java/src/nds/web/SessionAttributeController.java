/******************************************************************
*
*$RCSfile: SessionAttributeController.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/03/13 01:12:47 $
*
*$Log: SessionAttributeController.java,v $
*Revision 1.2  2006/03/13 01:12:47  Administrator
*no message
*
*Revision 1.1  2006/01/17 10:35:12  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.2  2004/02/02 10:42:42  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.1  2001/11/20 22:36:10  yfzhu
*no message
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/

package nds.web;

import javax.servlet.http.*;

import nds.schema.TableManager;
import nds.security.User;
import nds.util.DestroyListener;
import nds.util.WebKeys;
import nds.control.util.SecurityUtils;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.SessionContextManager;
import nds.log.*;
import nds.control.web.*;
import java.util.*;
/**
 * Change UserWeblImpl's Locale if in Portal environment
 * 
 * @author yfzhu@agilecontrol.com
 */
public class SessionAttributeController implements  HttpSessionAttributeListener{
	private static Logger logger= LoggerManager.getInstance().getLogger(SessionAttributeController.class.getName());
    public SessionAttributeController() {
    	//System.out.println("Created SessionAttributeController");
    }
	/** Notification that an attribute has been added to a session. Called after the attribute is added.*/
    public void attributeAdded ( HttpSessionBindingEvent event ){
    	updateLocale(event);
    	checkUser(event);
    }
	/** Notification that an attribute has been removed from a session. Called after the attribute is removed. */
    public void attributeRemoved ( HttpSessionBindingEvent event ){
    	//updateLocale(event);
    }
	/** Notification that an attribute has been replaced in a session. Called after the attribute is replaced. */
    public void attributeReplaced ( HttpSessionBindingEvent event ){
    	/**
    	 * Notify webUser of locale changed
    	 */
    	updateLocale(event);
    	checkUser(event);
    }
    private void checkUser(HttpSessionBindingEvent event){
    	if(!"USER_ID".equals(event.getName())){
    		return;
    	}
       	HttpSession session=event.getSession();
		/**
		* yfzhu 2004-09-19 add support for portal auth
		* Liferay will add user name in session context with "j_username" if authenticated
		*/
		Object obj=event.getValue();
		try{
		if(obj !=null && (obj instanceof String)){
			/**
			*  由于 lportal 的限制，名称一律设置为小写
			*  参见：com.liferay.portal.ejb.UserManagerImpl#_authenticate
			*  登录的时候由系统强行设置登录名为小写后验证
			*  // there's also a same handle method in nds.control.web.SessionContextManager
			*/
			String domainName=( (String )obj).toLowerCase(); //like email address
			
			String uName, adclientName ;
			int p=domainName.lastIndexOf("@");
			if ( p>0){
				 uName= domainName.substring(0,p );
				 adclientName= domainName.substring(p+1);
				 User usr= SecurityUtils.getUser(uName,adclientName);
				 
				 if(usr.getId().intValue() !=-1){
					nds.control.util.ValueHolder holder=new nds.control.util.ValueHolder();
					holder.put("user", usr);
					holder.put("remote_address", session.getAttribute("IP_ADDRESS"));
 	    			SessionContextManager manager= (SessionContextManager)session.getAttribute(WebKeys.SESSION_CONTEXT_MANAGER);
 	    			if(manager!=null){
 	    				// sometimes (webservice) manager may not exists in session though USER_ID set in attribute
	 	    			UserWebImpl user=((UserWebImpl) manager.getActor(nds.util.WebKeys.USER));
	 	    			
						user.performUpdate(holder,session);
						// reload locale again if locale set into attribute first
	 	    			Locale locale=(Locale)event.getSession().getAttribute(org.apache.struts.Globals.LOCALE_KEY);
	 	    			if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
	 	    			user.setLocale(locale);
 	    			}
				 }else 
				 	logger.info("Could not found nds user(isactive) for " + domainName);
			}else logger.info("Looks like invalid: " + domainName);
		}else{
			logger.error("j_username is empty in session!");
		}    	
		}catch(Exception e){
    		logger.error("found error for valuebound", e);
    	}
    }
    private void updateLocale(HttpSessionBindingEvent event){
    	if(org.apache.struts.Globals.LOCALE_KEY.equals(event.getName())){
    		try{
    			Locale locale=(Locale)event.getSession().getAttribute(org.apache.struts.Globals.LOCALE_KEY);
    			if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
    			SessionContextManager manager= (SessionContextManager)event.getSession().getAttribute(WebKeys.SESSION_CONTEXT_MANAGER);
    			if(manager!=null){
    				UserWebImpl usr=((UserWebImpl) manager.getActor(nds.util.WebKeys.USER));
    				if(usr!=null){
    					usr.setLocale(locale);
    	    			logger.debug("Locale of "+ usr.getUserId()+" changed to "+locale);
    				}
    			}
					
    		}catch(Exception e){
    			logger.error("found error for valuebound", e);
    		}
    	}    	
    }
    

   

}
