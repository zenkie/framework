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
 * Suspend or resume the process queue attached to
 */
public class ButtonHandleProcessQueue extends ButtonCommandUI_Impl{
	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "SwitchQueueJobState";
	}	

	protected String getCaption(HttpServletRequest request, Column column, int objectId ){
		Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
		if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
		String caption=null;
		try{
			String queueName=(String) QueryEngine.getInstance().doQueryOne("select name from ad_processqueue where id="+ objectId);
			JobManager jbm= (nds.schedule.JobManager) WebUtils.getServletContextManager().getActor( nds.util.WebKeys.JOB_MANAGER);
			// trigger has same name with job and the queue
			boolean isRunning= jbm.isQueueJobRunning(queueName);
			if(isRunning ) caption="stop-queuejob";
			else caption="start-queuejob";
     		caption= MessagesHolder.getInstance().getMessage(locale,caption);
		}catch(Exception e){
			logger.error("Could not get queue name according to id="+objectId,e);
			caption= column.getDescription(locale);
		}
		return caption;
	}
	
}
