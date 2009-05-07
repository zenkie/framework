/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.net.URLEncoder;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.security.Directory;
import nds.security.LoginFailedException;
import nds.util.StringUtils;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
/**
 * Create i_doc records if not found, and redirect to /servlets/binserv/IDoc for nea file
 * download 
 * 
 * Specific command object will check whether user has permission to download the idoc object or not(
 * probably will update security information on the records, for instance, m_distribution
 * will update order_filter and warehouseto_filter)
 *  
 */
public class ButtonLocalProcess extends ButtonCommandUI_Impl{
	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "LocalProcess_"+ column.getTable().getName();
	}	
	/**
	 * This is to get the UI handle page, if special page should be directed and get interaction
	 * between user and machine, this should be extended. 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId){
		String docno=StringUtils.hash(  column.getTable().getId()+"."+ objectId);
		String formRequest=URLEncoder.encode("/servlets/binserv/IDoc?docno=" +docno); 
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append("/control/command?command=").append( this.getCommand(request, column,objectId)).append(
		"&objectid=").append(objectId).append("&columnid=").append(column.getId()).append("&delegator=")
		.append(getDelegator(request, column,objectId)).append("&formRequest=").append(formRequest);
		return sb.toString();
	}
		
}
