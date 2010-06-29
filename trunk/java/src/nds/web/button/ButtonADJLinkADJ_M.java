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

 
 /*
取	C_ADJUSTITEM 的id，打开		C_ADJUST_MONTYITEM
*/
public class ButtonADJLinkADJ_M extends ButtonCommandUI_Impl{
	/**
	 * 
	 * @return true when user can do submit, and the mio's parent is unsubmitted
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			int status= Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select count(*) from C_ADJUST_MONTYITEM where C_ADJUST_MONTY_ID="+ objectId), -1);
			return  status>0;
		}catch(Throwable t){
			logger.error("Could not check user permission on ButtonReclaim: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}		
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		String targetTable=null;
		try{targetTable="C_ADJUST_MONTY";
		}catch(Throwable t){
			logger.error("error", t);
			targetTable="C_ADJUSTITEM";
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
		return POPUP_TARGET_LARGE_DIALOG;
	}

}
