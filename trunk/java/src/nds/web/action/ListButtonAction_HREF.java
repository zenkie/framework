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
 * �б���������ҵ���б�����
 * ��ť�Ͳ˵�ֻ�Ǿ������ڰ�ť������ʾλ�ã�����Ч����ȫһ����
 * @author yfzhu
 *
 */
public class ListButtonAction_HREF extends WebActionImpl {

	public String toHTML(Locale locale) {
		/*<a href="javascript:pc.doAdd()" accesskey="N">
		<img src="/html/nds/images/tb_new.gif"/>
		����
		</a>*/
		StringBuffer sb=new StringBuffer("<a id=\"wa_").append(this.getId()).append("\" href=\"");
		
		WebAction.ActionTypeEnum ate= this.getActionType();
		String target=this.getUrlTarget();
		switch(ate){
			case URL:
				//locate to div id = "portal-content" if not set target
				if(nds.util.Validator.isNull(target)) target="portal-content";
				if(target.startsWith("_")){
					sb.append(this.getScript()).append("\" target=\"").
						append(this.getUrlTarget()).append("\"/>");
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
			case OSShell:
				sb.append("javascript:pc.webaction(").append(this.getId()).append(",");
				if(this.getComments()!=null) sb.append("'").append(this.getComments()).append("',");
				else sb.append("null,");
				
				if(nds.util.Validator.isNotNull(target)){
					sb.append("'").append( target ).append("'");			
				}else{
					sb.append("null");
				}
				
				sb.append(")\">");
				break;
			default:
				sb.append("javascript:").append(this.getScript()).append("\">");			
		}		
		
		String s=this.getIconURL();
		if( nds.util.Validator.isNotNull( s)){
			sb.append("<img src=\"").append(s).append("\"/>");
		}
		sb.append(StringUtils.escapeForXML(this.getDescription()));
		sb.append("</a>");
		return sb.toString();
	}

}