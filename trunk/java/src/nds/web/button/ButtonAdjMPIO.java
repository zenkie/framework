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
 * For M_Pick, will adjust mpio move_qty and locator with this picking list
 * If there're some material not exists in mpio or qty exceeds, will generate a new picking list
 * with parent id set to this picking list.
 * 
 * So the picking lists are chained by parent_id column, this is quite useful when need to find the
 * original order of the m inout.
 */
public class ButtonAdjMPIO extends ButtonCommandUI_Impl{

	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "AdjustMPIOByPKL";
	}	
	

	/**
	 * 
	 * @return true when m_inout_id (for plan only) set on the record, and user has write permission 
	 * on m_inout with id= m_inout_id
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			// check object state first, that will use less time than check permission
			int inoutId=nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select io.id from m_pick p, m_inout io where io.nodetype='P' and io.status=1 and io.id=p.m_inout_id and p.id="+objectId),-1);
			if(inoutId != -1 ){
				userWeb=((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));
				// get m_inout pos: M_V_IVP_INOUT or M_V_OSP_INOUT
				String doctype=(String)QueryEngine.getInstance().doQueryOne("select DOCTYPE from m_inout where id="+ inoutId);
				String dir=null;
				if("MMS".equals(doctype)) dir="M_V_OSP_INOUT_LIST";
				else if("MMR".equals(doctype)) dir="M_V_IVP_INOUT_LIST";
				else {
					logger.error("Internal error: unexpeced doctype of m_inout id="+ inoutId);
					// to avoid error, take one for default;
					dir="M_V_IVP_INOUT_LIST";
				}
				b=userWeb.hasObjectPermission(dir, inoutId, Directory.WRITE);
			}
		}catch(Throwable t){
			logger.error("Could not check m_pick's inout id: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
		
}
