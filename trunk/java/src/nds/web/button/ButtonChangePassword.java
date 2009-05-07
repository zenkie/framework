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
 * For Users table
 */
public class ButtonChangePassword extends ButtonCommandUI_Impl{
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
		append(WebKeys.NDS_URI+ "/security/changepassword.jsp?objectid=").append(objectId);
		return sb.toString();
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_BLANK;
	}
	/**
	 * Should display the button or just blank
	 * @return
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		try{
		return (WebUtils.getDirectoryPermission("USERS_LIST", request ) & nds.security.Directory.WRITE)==nds.security.Directory.WRITE;
		}catch(Exception e){
			logger.debug("Erro check permission for directory 'user_list':" + e );
			return false;
		}
	}	
}
