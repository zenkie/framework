package nds.web.action;

import java.util.*;

import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.WebAction;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
/**
 * 列表栏出现在业务列表界面
 * 按钮和菜单只是决定是在按钮栏的显示位置，动作效果完全一样。
 * @author yfzhu
 *
 */
public class ListMenuItemAction extends WebActionImpl {

	public String toHTML(Locale locale,Map env) {
		/*<li class="ListCopyTo">
		<a href="javascript:pc.doListCopyTo()">复制</a>
		</li>*/
		StringBuffer sb=new StringBuffer("<li><a ");
		String s=this.getIconURL();
		if( nds.util.Validator.isNotNull( s)){
			sb.append("style=\"background-image:url(").append(s).append(")\" ");
		}
		sb.append("href=\"");
		
		WebAction.ActionTypeEnum ate= this.getActionType();
		String target=this.getUrlTarget();
		switch(ate){
			case URL:
				//locate to div id = "portal-content" if not set target
				if(nds.util.Validator.isNull(target)) target="portal-content";
				if(target.startsWith("_")){
					sb.append(this.getScript()).append("\" target='").
						append(this.getUrlTarget()).append("'>");
				}else{
					sb.append("javascript:pc.navigate('").append(this.getScript()).append("','").
						append(this.getUrlTarget()).append("')\">");
				}
				break;
			case JavaScript:
				sb.append("javascript:").append(this.getScript()).append("\">");
				break;
			case AdProcess:
				// get ad_process.id according to process name
				try{
					int processId= Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select id from ad_process where name="+ QueryUtils.TO_STRING(this.getScript()))
						,-1);
					if(processId==-1) throw new NDSException("ad_process with name "+this.getScript()+" not found" );
					sb.append("javascript:showObject('/html/nds/schedule/addpi.jsp?id=").
						append(processId).append("')\">");
				}catch(Throwable t ){
					sb.append("javascript:alert('Error:").
					append(t.getMessage()).append("')\">");
				}
				break;
			case StoredProcedure:
			case BeanShell:
			case Python:
			case OSShell:
				sb.append("javascript:pc.webaction(").append(this.getId()).append(",");
				if(this.getComments()!=null) sb.append("'").append(this.getComments()).append("',");
				else sb.append("null,");
				
				if(nds.util.Validator.isNotNull(target)){;
					sb.append("'").append(target).append("'");			
				}else{
					sb.append("null");
				}
				
				sb.append(")\">");
				break;
			default:
				sb.append("javascript:").append(this.getScript()).append("\">");			
		}		
		
		
		sb.append(MessagesHolder.getInstance().getMessage4(locale,StringUtils.escapeForXML(this.getDescription())));
		sb.append("</a></li>");
		return sb.toString();
	}

	@Override
	public String toHREF(Locale locale, Map env) {
		// TODO Auto-generated method stub
		return null;
	}

}
