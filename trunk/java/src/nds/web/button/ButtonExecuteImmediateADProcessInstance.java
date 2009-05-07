/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.Hashtable;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.security.LoginFailedException;
import nds.util.*;
import nds.control.event.NDSEventException;
import nds.schedule.JobManager;
import nds.schema.*;
import nds.control.web.*;
/**
 * Immediately execute ad_pinstance_id
 * Note this command should call special pages, not using Command pattern ( becaseof the complication
 * of transaction control)
 */
public class ButtonExecuteImmediateADProcessInstance extends ButtonCommandUI_Impl{
	/*protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "ExecuteImmediateADProcessInstance";
	}*/	
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_TOP;
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
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append(WebKeys.NDS_URI+ "/schedule/exec.jsp?objectid=").append(objectId);
		return sb.toString();
	}
}
