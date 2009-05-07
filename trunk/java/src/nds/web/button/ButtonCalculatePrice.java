/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.*;

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
import nds.util.Tools;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.util.SecurityUtils;
import nds.control.web.*;
/**
 * For c_order/c_invoice update pricelist/pricelimit/priceactual according to pricelist set
 */
public class ButtonCalculatePrice  extends ButtonCommandUI_Impl{

	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "CalculatePrice";
	}	
	

	/**
	 * 
	 * @return true when user has write permission on the directory of the order table 
	 *  当前单据未提交，且价目表已经设置，并且用户具有修改单据的权限
	 *     
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			userWeb=((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));
			java.util.List list= QueryEngine.getInstance().doQueryList("select status, m_pricelist_id from "+ column.getTable().getRealTableName()+" where id="+objectId);
			if(list!=null){
				b= 1==Tools.getInt( ((List)list.get(0)).get(0),-1);
				int priceListId= Tools.getInt(((List)list.get(0)).get(1),-1);
				if(b) b= priceListId!=-1;
			}
			if(b) b=userWeb.hasObjectPermission(column.getTable().getName(), objectId, Directory.WRITE);			
		}catch(Throwable t){
			logger.error("Could not update price on: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
		
}
