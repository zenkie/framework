package nds.web.button;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.Tools;
import nds.util.WebKeys;

public class ButtonViewaddnum extends ButtonCommandUI_Impl{

	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		int pid=-1;
		int gid=-1;
		try{
		Connection conn= nds.query.QueryEngine.getInstance().getConnection();
		QueryEngine engine=QueryEngine.getInstance();
		  pid=Tools.getInt(engine.doQueryOne("select g.d_cusbook_id from D_CUSBOOKITEM g where g.id="+objectId, conn), -1);
		  gid=Tools.getInt(engine.doQueryOne("select t.id from D_CUSINNUM t where t.d_cusbook_id="+pid, conn), -1);
		
		   } catch (Exception e) {
			   logger.debug("D_CUSINNUM id is not get!",e);
		   }
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
				append(WebKeys.NDS_URI+ "/object/object.jsp?table=15793&id="+gid);

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
