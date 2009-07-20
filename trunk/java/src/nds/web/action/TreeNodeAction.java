package nds.web.action;

import java.util.Locale;
import java.util.Map;

import nds.schema.*;
import nds.control.web.*;
import nds.util.*;
import nds.query.*;

/**
 * For node in ad_tablecategory tree
 * @author yfzhu
 *
 */
public class TreeNodeAction extends WebActionImpl {
	
	public String toHTML(Locale locale) {
		//<tree icon="/html/nds/images/table.gif"  text="<%=StringUtils.escapeForXML(tdesc)%>" action="javascript:pc.navigate('<%=tableId%>')"/>
		
		StringBuffer sb=new StringBuffer();
		WebAction.ActionTypeEnum ate= this.getActionType();
		
		// xml in content, anything that concord with xloadtree.js spec, can contain multiple nodes
		if(ate.equals(WebAction.ActionTypeEnum.URL) && this.getScript().startsWith("<") ){
			sb.append(this.getScript());
			
		}else{
			// create just one node
			sb.append("<tree icon=\"");
			String s=this.getIconURL();
			if( nds.util.Validator.isNull( s)){
				s=((Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS))
					.getProperty("webaction.treenode.iconurl","/html/nds/images/table.gif");			
				
			}
			sb.append(s).append("\" text=\"").append(StringUtils.escapeForXML(this.getDescription()))
			.append("\" action=\"");
			
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
					if(this.getComments()!=null) sb.append("\"").append(this.getComments()).append("\"");
					else sb.append("null,");
					
					if(nds.util.Validator.isNotNull(target)){;
						sb.append("\"").append( target ).append("\"");			
					}else{
						sb.append("null");
					}
					sb.append(")\"/>");
					break;
				
				default:
					sb.append("javascript:").append(this.getScript()).append("\"/>");
					
			}
		}
		return sb.toString();
	}

}
