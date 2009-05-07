package nds.portlet.util;


import java.util.*;
import javax.portlet.ActionRequest;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import nds.control.event.DefaultWebEvent;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Tools;

import org.apache.struts.util.RequestUtils;

import com.liferay.portal.SystemException;
import com.liferay.portal.model.Layout;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.WebKeys;
import com.liferay.util.Http;
import com.liferay.util.StringUtil;
import com.liferay.portlet.ActionRequestImpl;
import com.liferay.portlet.RenderRequestImpl;
/*
 contains methods commonly used by portlet handling
 This class will encapsulate portal-vendor-specific method
*/
public class PortletUtils {
	/**
	* Used by #getParameter, to retreieve value from request
	*/
	public final static int ORDER_REQUEST_PREFER=1;
	public final static int ORDER_PREFER_REQUEST=2;
	public final static int ORDER_REQUEST_ATTRIBUTE_PREFER=3;
	public final static int ORDER_ATTRIBUTE_REQUEST_PREFER=4;
	
    private static Logger logger= LoggerManager.getInstance().getLogger(PortletUtils.class.getName());

    public PortletUtils() {
    }
    /**
     * Find value of specified parameter in following order:
     * 	 1. PortletRequest.getParameter, if isNull, then
     * 	 2. PortletRequest.getAttribute("CURRENT_URL"), which in format like:
     * 			CURRENT_URL = /c/portal/layout?p_l_id=PRI.1003.3&table=c_v_po_order&id=657
     * @param req
     * @return
     */
    public static String findRequestParameter(PortletRequest req, String name){
    	String value=null;
    	String queryStr= getHttpServletRequest(req).getQueryString();
		if(nds.util.Validator.isNotNull(queryStr)){
			String[] params = StringUtil.split(queryStr, "&");
			for (int i = 0; i < params.length; i++) {
				String[] kvp = StringUtil.split(params[i], "=");
				if ((kvp.length == 2) && kvp[0].equals(name)) {
					value= kvp[1];
				}
			}
		}else{
			value= req.getParameter(name);
	    	if(nds.util.Validator.isNull(value)){
	    		String url= (String)getHttpServletRequest(req).getAttribute("CURRENT_URL");
	    		if(nds.util.Validator.isNotNull(url)){
	    			value= Http.getParameter(url, name, false);
	    		}
	    	}
		}
    	return value;
    }
    public static javax.servlet.http.HttpServletRequest  getHttpServletRequest(PortletRequest req ) {
         return ((RenderRequestImpl) req).getHttpServletRequest();
    }

	
    public static String getPortletTitle(String portletId, HttpServletRequest request,PortletConfig config)  {
          //com.liferay.portal.model.Portlet portlet=PortletManagerUtil.getPortletById(PortalUtil.getCompanyId(request), portletId);
          String portletTitle=null;
//          if(portlet !=null) {
              Locale locale = PortalUtil.getLocale(request);

              portletTitle= config.getResourceBundle(locale).getString(WebKeys.JAVAX_PORTLET_TITLE);
//          }
          return portletTitle;
 //throws com.liferay.portal.SystemException
    }
    public static boolean IsNarrowPortlet(String portletId, HttpServletRequest request,PortletConfig config)  {
    	return false;
        /*try {
            com.liferay.portal.model.Portlet portlet = PortletManagerUtil.
                getPortletById(PortalUtil.getCompanyId(request), portletId);
            return portlet.isNarrow();
        }
        catch (SystemException ex) {
            logger.error("Could not parse portlet from request with id=" + portletId, ex );
            return false;
        }*/
    }
    /**
    * @see getParameter(PortletRequest,String, 1);
    */
	public static String getParameter(PortletRequest req, String name){
		return getParameter(req, name,1);
	}
	 /**
    * @see getParameter(PortletRequest,String, 1);
    */
	public static String getParameter(PortletRequest req, String name, String defaultValue){
		return getParameter(req, name,1, defaultValue);
	}
	public static String getParameter(PortletRequest req, String name, int order,String defaultValue){
		String s1=getParameter(req, name,order);
		if(nds.util.Validator.isNull(s1)) s1=defaultValue;
		return s1;
	}
    /**
    * @param order 
    */
	public static String getParameter(PortletRequest req, String name, int order){
		String s1=null;
		if( order==ORDER_REQUEST_PREFER){
			s1=req.getParameter(name);
			if(nds.util.Validator.isNull(s1)) s1= req.getPreferences().getValue(name, null);
			
		}else if( order==ORDER_PREFER_REQUEST){
			s1= req.getPreferences().getValue(name, null);
			if(nds.util.Validator.isNull(s1))s1= req.getParameter(name);
		}else if( order==ORDER_REQUEST_ATTRIBUTE_PREFER){
			s1=req.getParameter(name);
			Object o;
			if(nds.util.Validator.isNull(s1)) {
				o= req.getAttribute(name);
				if(o!=null && o instanceof String) s1=(String)o;
			}
			if(nds.util.Validator.isNull(s1))s1= req.getPreferences().getValue(name, null);
		}else if( order ==ORDER_ATTRIBUTE_REQUEST_PREFER){
			Object o;
			if(nds.util.Validator.isNull(s1)) {
				o= req.getAttribute(name);
				if(o!=null && o instanceof String) s1=(String)o;
			}
			if(nds.util.Validator.isNull(s1))s1=req.getParameter(name);
			if(nds.util.Validator.isNull(s1))s1= req.getPreferences().getValue(name, null);
		}else{
			throw new IllegalArgumentException("Error input order:"+ order);
		}
		return s1;
	}
    /**
     * The default page url to open when event from this portlet has no reciever on the same page
     * @return String
     */
    public static String getDefaultEventHandleLayout(PortletConfig config, PortletRequest req){
        return "";
    }
    /**
     *
     * @param req RenderRequest
     * @return String[] portlet id, or null if can not retrieve portlet ids from req
     */
    public static String[] getLayoutPortlets(PortletRequest req){
    	throw new Error("Deprecated method for calling nds.portlet.util.PortletUtils#getLayoutPortlets");
        /*javax.servlet.http.HttpServletRequest httpReq=null;
        if (req instanceof RenderRequest)httpReq = getHttpServletRequest((RenderRequest)req);
        else if(req instanceof ActionRequest)httpReq = getHttpServletRequest((ActionRequest)req);
        if (httpReq ==null){
            logger.debug("Unknown request type");
            return null;
        }
        Layout layout=(Layout)httpReq.getAttribute(WebKeys.LAYOUT);
        if( layout==null){
            logger.debug("Could not retrieve layout info from RenderRequest");
            return null;
        }
        return layout.getPortletIds();*/
    }


    public static  String toString(PortletRequest req) {
        StringBuffer buf=new StringBuffer();
        Enumeration enu=req.getAttributeNames();
        buf.append("Following is from PortletRequest:\n\r------Attributes--------\r\n");
        while( enu.hasMoreElements()) {
            String att= (String)enu.nextElement();
            buf.append(att+" = "+ req.getAttribute(att)+"\r\n");
        }
        buf.append("------Parameters--------\r\n");
        enu=req.getParameterNames();
        while( enu.hasMoreElements()) {
            String param= (String)enu.nextElement();
            String s= Tools.toString(req.getParameterValues(param));
            buf.append(param+" = "+ s+"\r\n");
        }
        buf.append("------Properties--------\r\n");
        enu=req.getPropertyNames();
        while( enu.hasMoreElements()) {
            String param= (String)enu.nextElement();
            String s= (req.getProperty(param));
            buf.append(param+" = "+ s+"\r\n");
        }
        buf.append("------Preferences--------\r\n");
        enu=req.getPreferences().getNames();
        while( enu.hasMoreElements()) {
            String param= (String)enu.nextElement();
            String s= (req.getPreferences().getValue(param,""));
            buf.append(param+" = "+ s+"\r\n");
        }

        buf.append("\n\rContext path:"+req.getContextPath());
        buf.append("\n\rLocale:"+req.getLocale());
        buf.append("\n\rAuthType:"+req.getAuthType());
        buf.append("\n\rPortletMode:"+req.getPortletMode());
        buf.append("\n\rRemoteUser:"+req.getRemoteUser());
        buf.append("\n\rWindowState:"+req.getWindowState());
        return buf.toString();
    }
    
	public static String getMessage(PortletConfig config, Locale loc,String name){
		return getMessage(config,loc,name,null);
	}
	/**
	* @param defaultName if resource of name not found, will try to found resource for 
	*      defaultName
	*/ 
	public static String getMessage(PortletConfig config, Locale loc, String name, String defaultName){
		ResourceBundle bundle= config.getResourceBundle(loc);
		if(bundle==null) {
			logger.debug("Could not found bundle in PortletConfig " + config);
			return name+"?";
		}
		try{
			return bundle.getString(name);
		}catch(MissingResourceException e){
			try{
				return (defaultName==null?name:bundle.getString(defaultName));
			}catch(MissingResourceException e2){
				return 	name+"?";
			}
		}
	}
	public static String getMessage(PageContext pageContext, String name){
		return getMessage(pageContext,name,null);
	}
	public static String getMessage(PageContext pageContext, String name, String defaultName){

		String value = null;

		try {
			value= RequestUtils.message(pageContext, null, null, name);
		}
		catch (Exception e) {
			logger.debug("Could not found bundle in pageContext " + pageContext);
		}
		if(value==null){
			try{
				value= (defaultName==null?name:RequestUtils.message(pageContext, null, null, defaultName));
			}catch(Exception e2){
				return 	name+"?";
			}
		}
		return value==null?defaultName:value;
	}
	/**
	 * Create request according to request request, same as nds.control.web.request.DefaultRequestHandler
	 * @param request
	 * @param eventName
	 * @return event
	 */
	public static DefaultWebEvent createEvent(PortletRequest request, String eventName){
		DefaultWebEvent event=new DefaultWebEvent(eventName);
        /** 
         * add param named "nds.query.querysession", which hold QuerySession object
         * @since 2.0
         */
		javax.servlet.http.HttpServletRequest r=null;
		if(request instanceof ActionRequest) r= PortletUtils.getHttpServletRequest((ActionRequest)request);
		else r= PortletUtils.getHttpServletRequest((RenderRequest)request);
		
		SessionContextManager scmanager= WebUtils.getSessionContextManager(
				r.getSession());
        UserWebImpl usr=(UserWebImpl)scmanager.getActor(nds.util.WebKeys.USER);
        logger.debug("usr:="+ usr);
        if(usr !=null && usr.getSession()!=null)
        	event.put("nds.query.querysession",usr.getSession());
		event.setParameter("operatorid",""+usr.getUserId());
        
        Enumeration enu= request.getParameterNames();
        while(enu.hasMoreElements()) {
            String name=(String) enu.nextElement();
            String[] value= request.getParameterValues(name);
            if(name.equalsIgnoreCase("command")){
                /* ############# tony 's method was deferred to EJB layer to implement,
                     see nds.control.ejb.CommandFactory

                event.setParameter(name, commandName(value[0]));
                ######## yfzhu marked above */
                event.setParameter(name, value[0]);

//nmdemo, ObjectPermit and ObjectRollback will also need spName                if(value[0].endsWith("Submit"))
                    event.setParameter("spName",value[0]);
            }else if( value.length == 1)
                event.setParameter(name, value[0]);
            else
                event.setParameter(name, value);
        }
        return event;		
	}
}
