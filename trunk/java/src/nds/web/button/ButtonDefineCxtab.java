/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.LoginFailedException;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
/**
 * Define cxtab dim and measures
 */
public class ButtonDefineCxtab extends ButtonCommandUI_Impl{
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
		append(WebKeys.NDS_URI+ "/cxtab/cxtabdef.jsp?id=").append(objectId);
		return sb.toString();
	}
	/**
	 * Should display the button or just blank
	 * @return
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		try{
			UserWebImpl usr=(UserWebImpl) WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER);
			Table table= column.getTable();
			if(( usr.getObjectPermission( table.getName(), objectId)& nds.security.Directory.WRITE)==nds.security.Directory.WRITE){
				return true;
			}
			return false;
		}catch(Exception e){
			logger.debug("Erro check permission:" + e );
			return false;
		}
	}	
}
