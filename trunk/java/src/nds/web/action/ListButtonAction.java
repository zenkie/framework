package nds.web.action;

import java.util.Locale;

import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.WebAction;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
/**
 * 列表栏出现在业务列表界面
 * 按钮和菜单只是决定是在按钮栏的显示位置，动作效果完全一样。
 * @author yfzhu
 *
 */
public class ListButtonAction extends WebActionImpl {

	public String toHTML(Locale locale) {
		/*<input type="button" class="cbutton" value="help" onclick="javascript:pc.queryList()"/>*/
		StringBuffer sb=new StringBuffer();
		sb.append("<input type='button' class='cbutton' id=\"wa_").
			append(this.getId()).append("\" name=\"wa_").append(this.getId()).append("\" value=\"");

		sb.append(StringUtils.escapeForXML(this.getDescription())).append("\" onclick=\"");
		
		WebAction.ActionTypeEnum ate= this.getActionType();
		String target=this.getUrlTarget();
		switch(ate){
			case URL:
				//locate to div id = "portal-content" if not set target
				if(nds.util.Validator.isNull(target)) target="portal-content";
				if(target.startsWith("_")){
					sb.append("popup_window('").append(this.getScript()).append("','").
					append(this.getUrlTarget()).append("')\"/>");
				}else{
					sb.append("pc.navigate('").append(this.getScript()).append("','").
						append(this.getUrlTarget()).append("')\"/>");
				}
				break;
			case JavaScript:
				sb.append("javascript:").append(this.getScript()).append("\"/>");
				break;
			case AdProcess:
				// get ad_process.id according to process name
				try{
					int processId= Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select id from ad_process where name="+ QueryUtils.TO_STRING(this.getScript()))
						,-1);
					if(processId==-1) throw new NDSException("ad_process with name "+this.getScript()+" not found" );
					sb.append("javascript:showObject('/html/nds/schedule/addpi.jsp?id=").
						append(processId).append("')\"/>");
				}catch(Throwable t ){
					sb.append("javascript:alert('Error:").
					append(t.getMessage()).append("')\"/>");
				}
				break;
			case StoredProcedure:
			case BeanShell:
			case OSShell:
				sb.append("javascript:pc.webaction(").append(this.getId()).append(",");
				if(this.getComments()!=null) sb.append("'").append(this.getComments()).append("',");
				else sb.append("null,");
				
				if(nds.util.Validator.isNotNull(target)){
					sb.append("'").append( target ).append("'");			
				}else{
					sb.append("null");
				}
				
				sb.append(")\"/>");
				break;
			default:
				sb.append("javascript:").append(this.getScript()).append("\"/>");			
		}		
		
		return sb.toString();
	}

}
