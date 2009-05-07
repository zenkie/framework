package nds.web.button;

import javax.servlet.http.HttpServletRequest;

import nds.schema.Column;
import nds.util.WebKeys;
/**
 * 
 * Select web_client template folder
 *
 */
public class ButtonDisplaytemplate extends ButtonCommandUI_Impl{
	protected String getHandleURL(HttpServletRequest request, Column column,int objectId){
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.NDS_URI+"/webclient/select_template.jsp");
		
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
