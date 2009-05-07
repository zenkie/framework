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
import nds.security.Directory;
import nds.security.LoginFailedException;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.util.SecurityUtils;
import nds.control.web.*;
/**
 * 使用此方法的按钮，所在表应该具有isstop字段，="Y" 时，按钮将调用 表名+"_OPEN" 存储过程，＝“N”，按钮将调用 表名＋“_CLOSE”方法
 * 
 */
public class ButtonSwitchCloseOpen extends ButtonCommandUI_Impl{

	/**
	 * Which real implementation will handle the command click event. Default sets to column.ValueInterpeter
	 * If extends this class, this method must be overriden.
	 * 
	 * 如果扩展了此类，则必须在Column.ValueInterpeter 设置上新的处理类，而此方法应该指明具体的命令执行类，故需要重载。
	 * 当然，如果重载了 getHandleURL 方法，这个方法就不会被调用了。
	 * 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getDelegator(HttpServletRequest request, Column column, int objectId){
		return  column.getTable().getRealTableName()+"_"+ (isStop(column,objectId)? "OPEN":"CLOSE");
	}	
	private boolean isStop(Column column, int objectId){
		boolean isStop=false;
		try{
			isStop=nds.util.Tools.getYesNo( (String)QueryEngine.getInstance().doQueryOne("select isstop from "+ column.getTable().getRealTableName()+" where id="+ objectId), false);
		}catch(Throwable t){
			logger.error("Fail to do isstop check on "+ column+", id="+objectId,t);
		}
		return isStop;
	}
	/**
	 * Button caption
	 * @return
	 */
	protected String getCaption(HttpServletRequest request, Column column, int objectId ){
		Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
		if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
		
		return  nds.util.MessagesHolder.getInstance().getMessage(locale, isStop(column,objectId)? "switch-open":"switch-close");
	}	
}
