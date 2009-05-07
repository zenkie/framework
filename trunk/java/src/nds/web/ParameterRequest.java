/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Tools;
import java.util.*;
/**
 * 
 * @author yfzhu@agilecontrol.com
 * HttpServletRequest that can add parameter to it
 */

public class ParameterRequest extends HttpServletRequestWrapper {
	private static Logger logger=LoggerManager.getInstance().getLogger(ParameterRequest.class.getName());
	private Map params;
	public ParameterRequest(HttpServletRequest req) {
		this(req, null);
	}
	/**
	 * 
	 * @param params will be used for getParameter first
	 */
	public ParameterRequest(HttpServletRequest req, Map  p) {
		super(req);
		params= p;
		logger.debug(Tools.toString(p));
		/*if(req.getParameterMap()!=null)params.putAll(req.getParameterMap());
		if(p!=null)params.putAll(p);*/
	}
	public String getParameter(String name){
		Object obj= params.get(name);
		if(obj !=null) return obj.toString();
		else return super.getParameter(name);
	}
	public void setParameter(String name, String value){
		if(params ==null) params=new HashMap();
		params.put(name, value);
	}
/*	public String[] getParameterValues(String name){
		Object obj= params.get(name);
		if(obj ==null) return null;
		if( obj.getClass().isArray()) return (String[])obj;
		else	return new String[]{obj.toString()};
	}
	public Enumeration getParameterNames(){
		return Collections.enumeration(params.keySet());
	}
	public Map getParameterMap(){
		return params;
	}*/

}