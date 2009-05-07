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
 * For M_Pick, will create a new order by picking list
 */
public class ButtonCreateOrder extends ButtonCommandUI_Impl{

	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "CreateOrderByPKL";
	}	
	

	/**
	 * 
	 * @return true when user has write permission on the directory of the order table 
	 *  必须判断用户将生成的是采购订单，还是销售订单，才能进行目录写权限判断
	 *  在订单类型中的基础类型可以判断目标单据类型
	 *     
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			userWeb=((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));
			String docBaseType= (String )QueryEngine.getInstance().doQueryOne("select docbasetype from c_doctype where id=(select C_ORDER_DOCTYPE_ID from m_pick where id="+ objectId+")");
			if(docBaseType!=null && docBaseType.length()>1){
				String firstChar= docBaseType.substring(0,1);
				String dir=null;
				if(firstChar.equals("S")){
					// sale
					dir="C_V_SO_ORDER_LIST";
				}else if(firstChar.equals("P")){
					// purchase
					dir="C_V_PO_ORDER_LIST";
				}else{
					//unknown
					
				}
				if(dir!=null)b=userWeb.isPermissionEnabled(dir, Directory.WRITE);
			}
		}catch(Throwable t){
			logger.error("Could not check m_pick's inout id on ButtonDiff_Pick_Inout: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
		
}
