package nds.web.button;

import javax.servlet.http.HttpServletRequest;

import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.WebKeys;

public class ButtonMatchColorMaterial  extends ButtonCommandUI_Impl{
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append(WebKeys.NDS_URI+ "/object/object.jsp?table=Y_STYLE_BOM_PREPARE&&fixedcolumns=&id="+objectId);
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
