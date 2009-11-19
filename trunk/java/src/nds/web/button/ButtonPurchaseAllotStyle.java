package nds.web.button;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import nds.control.util.SecurityUtils;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.security.Directory;
import nds.util.Tools;
import nds.util.WebKeys;

/**
 * 
取Y_PURCHASE_ITEM中的M_PROCUT_ID，如该列不为NULL，则提示“该面料已分款，不能重复分款！”。
否则根据Y_PURCHASE_ITEM表中的Y_PURCHASE_ID取到Y_PURCHASE表中LIST_TYPE的值，如为FAB，
则打开视图Y_PURCHASE_ALLOT_FAB，如为ACC，则打开视图Y_PURCHASE_ALLOT_ACC。

 * @author yfzhu
 *
 */
public class ButtonPurchaseAllotStyle extends ButtonCommandUI_Impl{
	/**
	 * 
	 * @return true when user can do submit, and the mio's parent is unsubmitted
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			int status= Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select M_PRODUCT_ID from Y_PURCHASE_ITEM where id="+ objectId), -1);
			return  status==-1;
		}catch(Throwable t){
			logger.error("Could not check user permission on ButtonReclaim: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}		
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		String targetTable=null;
		try{
			String li= (String)QueryEngine.getInstance().doQueryOne(
					"select a.LIST_TYPE from Y_PURCHASE a,  Y_PURCHASE_ITEM b where a.id=b.Y_PURCHASE_ID and b.id="+ objectId);
			if("FAB".equals(li)) targetTable="Y_PURCHASE_ALLOT_FAB";
			else if("ACC".equals(li))targetTable="Y_PURCHASE_ALLOT_ACC";
			else targetTable= "Y_PURCHASE_ITEM";
		}catch(Throwable t){
			logger.error("error", t);
			targetTable="Y_PURCHASE_ITEM";
		}	
		Table tb= nds.schema.TableManager.getInstance().getTable(targetTable);
		int tbid= (tb==null?-1: tb.getId());
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
				append(WebKeys.NDS_URI+ "/object/object.jsp?table="+ tbid+"&id="+objectId);
		return sb.toString();
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_BLANK;
	}

}
