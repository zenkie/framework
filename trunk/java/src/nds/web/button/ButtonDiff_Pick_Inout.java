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
import nds.query.QueryEngine;
import nds.security.Directory;
import nds.security.LoginFailedException;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.util.SecurityUtils;
import nds.control.web.*;
/**
 * For M_Pick, to get diff between picking list and inout sheet
 * will redirect to /whs/pkl.jsp with object id as parameter
 */
public class ButtonDiff_Pick_Inout extends ButtonCommandUI_Impl{
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
		append(WebKeys.NDS_URI+ "/whs/pkldiff.jsp?id="+objectId );
		return sb.toString();
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_TOP;
	}
	/**
	 * 
	 * @return true when m_inout_id set on the record, and user has read permission on m_inout with id= m_inout_id
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			// check object state first, that will use less time than check permission
			int inoutId=nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select m_inout_id from m_pick where id="+objectId), -1);
			if(inoutId !=-1 ){
				userWeb=((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));
				b=userWeb.hasObjectPermission("M_INOUT", inoutId, Directory.READ);
			}
		}catch(Throwable t){
			logger.error("Could not check m_pick's inout id on ButtonDiff_Pick_Inout: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
	
}
