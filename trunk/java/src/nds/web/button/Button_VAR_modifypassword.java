package nds.web.button;

import javax.servlet.http.HttpServletRequest;

import nds.schema.Column;
import nds.util.WebKeys;

public class Button_VAR_modifypassword extends ButtonCommandUI_Impl{
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.NDS_URI+"/var/var_client_password.jsp?objectid="+objectId);
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
