package nds.web.action;

import java.util.Locale;

import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.WebAction;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;

public class ObjMenuItemAction extends ObjButtonAction {

	public String toHTML(Locale locale) {
		/*
		 <li class="ListCopyTo">
		<a href="javascript:pc.doListCopyTo()">¸´ÖÆ</a>
		</li>
		 */
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
				if(nds.util.Validator.isNull(target)) target="_self";
				sb.append(this.getScript()).append("\" target=\"").
						append(target).append("\"");
				break;
			case JavaScript:
				sb.append("javascript:").append(this.getScript()).append("\"");
				break;
			case AdProcess:
				// get ad_process.id according to process name
				try{
					int processId= Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select id from ad_process where name="+ QueryUtils.TO_STRING(this.getScript()))
						,-1);
					if(processId==-1) throw new NDSException("ad_process with name "+this.getScript()+" not found" );
					sb.append("javascript:popup_window('/html/nds/schedule/addpi.jsp?id=").
						append(processId).append("')\"");
				}catch(Throwable t ){
					sb.append("javascript:alert('Error:").
					append(t.getMessage()).append("')\"");
				}
				break;
			case StoredProcedure:
			case BeanShell:
			case Python:				
			case OSShell:
				sb.append("javascript:oc.webaction(").append(this.getId()).append(",");
				if(this.getComments()!=null) sb.append("'").append(this.getComments()).append("',");
				else sb.append("null,");
				
				if(nds.util.Validator.isNotNull(target)){
					sb.append("'").append( target ).append("'");			
				}else{
					sb.append("null");
				}
				
				sb.append(")\"");
				break;
			default:
				sb.append("javascript:").append(this.getScript()).append("\"");			
		}		
		
		
		sb.append(" />");
		sb.append(StringUtils.escapeForXML(this.getDescription())).append("</a></li>");
		return sb.toString();			
	}

}
