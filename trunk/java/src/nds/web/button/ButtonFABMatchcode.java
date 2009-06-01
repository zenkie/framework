package nds.web.button;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import nds.query.QueryEngine;
import nds.schema.Column;
import nds.util.WebKeys;

public class ButtonFABMatchcode extends ButtonCommandUI_Impl{
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		Connection conn=null;
		StringBuffer sb = new StringBuffer();
		try{
			QueryEngine engine=QueryEngine.getInstance();
			conn= engine.getConnection();
   			ArrayList params1=new ArrayList();
   			params1.add(objectId);
			QueryEngine.getInstance().executeStoredProcedure("Y_UPDATE_PBP_SIZE", params1, false,conn);
			sb.append(WebKeys.WEB_CONTEXT_ROOT).
			append(WebKeys.NDS_URI+ "/object/object.jsp?table=Y_PBP_FAB_SIZE_MAIN&&fixedcolumns=&id="+objectId);
			return sb.toString();
		}catch(Throwable th){
		       logger.error("exception",th);
		       return "";
		}finally{ 
             try{conn.close();}catch(Exception e){}
		}		
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_BLANK;
	}

}