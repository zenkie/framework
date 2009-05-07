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
import nds.util.*;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.query.*;
import nds.control.web.*;
/**
 * For split l_order, used on both L_TRANSPORTITEM and L_ORDER
 */
public class ButtonSplitLOrder extends ButtonCommandUI_Impl{
	/**
	 * This is to get the UI handle page, if special page should be directed and get interaction
	 * between user and machine, this should be extended. 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId){
		try{
			Table table= column.getTable();
			if( "L_TRANSPORTITEM".equals(table.getRealTableName())){
				objectId= nds.util.Tools.getInt( QueryEngine.getInstance().doQueryOne("select l_order_id from L_TRANSPORTITEM where id="+objectId), -1);
			}
			StringBuffer sb = new StringBuffer();
			sb.append(WebKeys.WEB_CONTEXT_ROOT).
			append(WebKeys.NDS_URI+ "/wuliu/splitorder.jsp?objectid=").append(objectId);
			return sb.toString();
		}catch(Throwable t){
			logger.error("Erro loading column=:"+ column +", objectid="+objectId,t );
			return "";
		}
	}
	/**
	 * Popup type, POPUP_TARGET_SMALL_DIALOG
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_SMALL_DIALOG;
	}
	/**
	 * Should display the button or just blank
	 * @return
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		try{
			if(!super.isValid(request, column, objectId)) return false;
			UserWebImpl usr=(UserWebImpl) WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER);
			Table table= column.getTable();
			if( "L_TRANSPORTITEM".equals(table.getRealTableName())){
				// l_transport 未提交
				
				objectId= nds.util.Tools.getInt( QueryEngine.getInstance().doQueryOne("select l_order_id from L_TRANSPORTITEM where id="+objectId), -1);
			}
			if( Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from l_order where status=1 and id="+ objectId+" and check1<>'S' and state in ('P','D')"),-1)==0 ){
				// 单据可修改，且未拆分过(check<>'S'), 状态为待上车或已上车
				return false;
			}
			if(( usr.getObjectPermission( "L_ORDER", objectId)& nds.security.Directory.WRITE)==nds.security.Directory.WRITE){
				return true;
			}
			
			return false;
		}catch(Exception e){
			logger.debug("Erro check permission:" + e );
			return false;
		}
	}	
}
