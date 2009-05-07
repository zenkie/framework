/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;

import nds.control.event.*;
import nds.control.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.*;
import java.util.*;
/**
 * Wrapper of command serlvet request, will fetch request's parameter
 * from DefaultWebEvent first
 * 
 * @author yfzhu@agilecontrol.com
 * @since 2.0
 */

public class NDSServletRequest extends ServletRequestWrapper {
	private static Logger logger=LoggerManager.getInstance().getLogger(NDSServletRequest.class.getName());
	private DefaultWebEvent event;
	public NDSServletRequest(ServletRequest req) {
		this(req, null);
	}
	/**
	 * If event is null, will trying to fetch the event from
	 * (DefaultWebEvent)req.getAttribute("nds.control.event.DefaultWebEvent"),
	 * else will trying to
	 * vh=(ValueHolder)req.getAttribute("nds.control.utl.ValueHolder"), 
	 * and fetch vh.get("nds.control.event.DefaultWebEvent") if vh is not null
	 * 
	 * @param req
	 * @param event
	 */
	public NDSServletRequest(ServletRequest req, DefaultWebEvent event) {
		super(req);
		if(event ==null){
			Object obj= req.getAttribute(WebKeys.DEFAULT_WEB_EVENT);
			if(obj!=null && obj instanceof DefaultWebEvent){
				event= (DefaultWebEvent)obj;
			}
			if(event ==null){
				obj= req.getAttribute(WebKeys.VALUE_HOLDER);
				if(obj!=null && obj instanceof ValueHolder){
					obj= ((ValueHolder)obj).get(WebKeys.DEFAULT_WEB_EVENT);
					if(obj instanceof DefaultWebEvent){
						event= (DefaultWebEvent)obj;
					}
				}
			}
		}
		//logger.debug("event="+ event);
		this.event=event;
	}
	public String getParameter(String name){
		String[] s=this.getParameterValues(name);
		if( s!=null && s.length>0) return s[0];
		return null;
		
	}
	public String[] getParameterValues(String name){
		String[] v=null;
		if(event !=null) {
			v= event.getParameterValues(name);
		}
		if(v==null) v=super.getParameterValues(name);
		return v;
	}
	public Enumeration getParameterNames(){
		if(event !=null) {
			return Collections.enumeration(event.getData().keySet());
		}
		return super.getParameterNames();
		
	}
	public Map getParameterMap(){
		if(event !=null) {
			return event.getData();
		}
		return super.getParameterMap();
	}

}
