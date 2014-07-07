package nds.web.button;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import nds.schema.Column;
import nds.schema.TableManager;
import nds.util.WebKeys;

/*
 * 直接界面排序操作
 * 因此界面需要2种操作向上或向下
 * 
 * 替换构造button样式
 * <a href="javascript:pc.doModify()"><img src="/html/nds/oto/themes/01/images/tb_modify.gif">向上</a>
 * <a href="javascript:pc.doModify()"><img src="/html/nds/oto/themes/01/images/tb_modify.gif">向下</a>
 * <input class="cbutton" type="button" name="col_43865" value="排序" onclick="javascript:btn_dialog_window(&quot;/control/command?command=ButtonCommand&amp;
 * objectid=96&amp;columnid=43865&amp;delegator=asdfasdfasdfadsfasdfadsf&quot;)">
 */
public class Button_Orderno_Control extends ButtonCommandUI_Impl{
	
	public String constructHTML( HttpServletRequest request, Column column, int objectId ){
		if(!this.isValid(request, column, objectId)){
			return "&nbsp;";
		}
		StringBuffer sb=new StringBuffer();
		//up
		String caption=getCaption(request, column,objectId);
		sb.append("<a class='ordno' name='col_up_").append(column.getId());
		String popType=getPopupType(request, column,objectId);
		sb.append("' onclick='javascript:").append(popType).append("(\"");
		sb.append(getHandleURL(request, column,objectId,"up")).append("&active=up").append("\"");
		sb.append(")'");
		sb.append(">");
		sb.append("<img src=\"/html/nds/oto/themes/01/images/orderup.gif\">");
		sb.append("</a>");
		//down
		sb.append("<a class='orddown' name='col_down_").append(column.getId());
		sb.append("' onclick='javascript:").append(popType).append("(\"");
		sb.append(getHandleURL(request, column,objectId,"down")).append("&active=down").append("\"");
		sb.append(")'");
		sb.append(">");
		sb.append("<img src=\"/html/nds/oto/themes/01/images/orderdown.gif\">");
		sb.append("</a>");
		return sb.toString();
	}
	
	
	/**
	 * This is to get the UI handle page, if special page should be directed and get interaction
	 * between user and machine, this should be extended. 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId,String active){
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append("/control/command?command=").append( this.getCommand(request, column,objectId)).append(
		"&objectid=").append(objectId).append("&columnid=").append(column.getId()).append("&delegator=")
		.append(getDelegator(request, column,objectId,active));
		return sb.toString();
	}
	

	protected String getDelegator(HttpServletRequest request, Column column, int objectId,String active){
		return (column.getValueInterpeter().replace("nds.web.button.",""))+"@"+active;
	}
	
	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "ButtonCommandforOrder";
	}
	
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_NODIALOG;
	}	
}
