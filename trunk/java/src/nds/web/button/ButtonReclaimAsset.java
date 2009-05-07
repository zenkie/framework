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
 * For OA_ASSET_USE table
 */
public class ButtonReclaimAsset extends ButtonCommandUI_Impl{

	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "ReclaimAsset";
	}	
	/**
	 * 
	 * @return true when user can do auditing, and the asset is in use
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			// check object state first, that will use less time than check permission
			String useState= (String)QueryEngine.getInstance().doQueryOne(
					"select ASSET_STATE from OA_ASSET_USE where id="+objectId);
			if( "O".equals(useState)/* out*/){
				userWeb=((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	 
				b=SecurityUtils.hasObjectPermission(userWeb.getUserId(), userWeb.getUserName(),
						column.getTable().getName(),objectId, Directory.AUDIT, userWeb.getSession() );
			}else{
				b=false;
			}
		}catch(Throwable t){
			logger.error("Could not check user permission on ButtonReclaim: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
}
