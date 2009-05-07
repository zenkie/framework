package nds.web.button;


import javax.servlet.http.HttpServletRequest;

import nds.schema.Column;
import nds.util.WebKeys;

public class Button_VAR_charge extends ButtonCommandUI_Impl{
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		StringBuffer sb = new StringBuffer();
		sb.append("/var/client_charge.jsp?isvar=true&&clientid="+objectId);
		 
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
