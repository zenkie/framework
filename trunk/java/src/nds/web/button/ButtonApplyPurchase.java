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
先调用存储过程Y_UPDATE_APPLYPURCHASE(p_id in number，p_user_id in number)
传入表Y_MATERIAL_PLAN_SITEM的ID，当前操作用户ID
然后根据Y_MATERIAL_PLAN_SITEM的Y_MATERIAL_ID，取Y_MATERIAL表中的MATERIAL_TYPE
如为FAB，则打开视图Y_APPLYPURCHASE_FAB，如为ACC，则打开视图Y_APPLYPURCHASE_ACC。
 * @author yfzhu
 *
 */
public class ButtonApplyPurchase extends ButtonCommandUI_Impl{
	
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		String targetTable=null;
		
		try{
			UserWebImpl userWeb=((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));
			ArrayList params=new ArrayList();
			params.add(new Integer(objectId));
			params.add(new Integer(userWeb.getUserId()));
			
			QueryEngine.getInstance().executeStoredProcedure("Y_UPDATE_APPLYPURCHASE", params, false);
			
			String li= (String)QueryEngine.getInstance().doQueryOne(
					"select a.MATERIAL_TYPE from Y_MATERIAL a,  Y_MATERIAL_PLAN_SITEM b where a.id=b.Y_MATERIAL_ID and b.id="+ objectId);
			if("FAB".equals(li)) targetTable="Y_APPLYPURCHASE_FAB";
			else if("ACC".equals(li))targetTable="Y_APPLYPURCHASE_ACC";
			else targetTable= "Y_MATERIAL_PLAN_SITEM";
		}catch(Throwable t){
			logger.error("error", t);
			targetTable="Y_MATERIAL_PLAN_SITEM";
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
