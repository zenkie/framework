package nds.web.action;

import java.sql.Connection;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import nds.control.util.*;
import nds.control.web.UserWebImpl;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.WebAction;
import nds.util.BshScriptUtils;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;

public class ObjButtonAction extends WebActionImpl {

	public String toHTML(Locale locale, Map env) {
		/*
		 <input type="button" value="����(S)" onclick="oc.doModify()" 
		 accesskey="S" name="Modify" class="cbutton"/>
		 */
		
		StringBuffer sb=new StringBuffer();
		sb.append("<input type=\"button\" class=\"cbutton\" value=\"");
		sb.append(MessagesHolder.getInstance().getMessage4(locale,StringUtils.escapeForXML(this.getDescription()))).append("\" ");
		sb.append("name=\"").append(StringUtils.escapeForXML(this.getName())).append("\" ");
		sb.append("onclick=\"");
		
		WebAction.ActionTypeEnum ate= this.getActionType();
		String target=this.getUrlTarget();
		switch(ate){
			case URL:
				if(nds.util.Validator.isNull(target)) target="_self";
				sb.append("popup_window('");
				sb.append(this.getScript()).append("',target='").
					append(target).append("')\"");
				break;
			case JavaScript:
				sb.append("").append(this.getScript()).append("\"");
				break;
			case AdProcess:
				// get ad_process.id according to process name
				try{
					int processId= Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select id from ad_process where name="+ QueryUtils.TO_STRING(this.getScript()))
						,-1);
					if(processId==-1) throw new NDSException("ad_process with name "+this.getScript()+" not found" );
					sb.append("popup_window('/html/nds/schedule/addpi.jsp?id=").
						append(processId).append("')\"");
				}catch(Throwable t ){
					sb.append("alert('Error:").
					append(t.getMessage()).append("')\"");
				}
				break;
			case StoredProcedure:
			case Python:
			case BeanShell:
			case OSShell:
				sb.append("oc.webaction(").append(this.getId()).append(",");
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
				sb.append("").append(this.getScript()).append("\"");			
		}		
		
		
		sb.append(" />");
		String f= StringUtils.replace(sb.toString(),"$OBJECTID$", 
				String.valueOf(getValueFromMap("objectid", env, null,true)));

		f= StringUtils.replace(f,"$MAINTABLE$", 
				String.valueOf(getValueFromMap("maintable", env, null,true)));
		
		return f;
	}
	/**
	 * Check whether this action can display in specified session or not.
	 * @param env contains web environment, mainly "httpservletrequest", "connection"
	 * 	Must have "objectid" and "maintable" inside
	 * Variables in filter can have "$OBJECTID$" for current record id      
	 * @return true if can be displayed
	 */
	public boolean canDisplay(Map env) throws Exception{
		boolean b=false;
		if(nds.util.Validator.isNull(filter)) return true;
		try{

			String f= StringUtils.replace(filter,"$OBJECTID$", 
					String.valueOf(getValueFromMap("objectid", env, null,true)));
	
			f= StringUtils.replace(f,"$MAINTABLE$", 
					String.valueOf(getValueFromMap("maintable", env, null,true)));
			
			HttpServletRequest request=(HttpServletRequest) getValueFromMap("httpservletrequest", env, null,true);
			UserWebImpl userWeb= (UserWebImpl)getValueFromMap("userweb", env, null,true);
			Connection conn= (Connection)getValueFromMap("connection", env, null,true);
			
			f=QueryUtils.replaceVariables(f,userWeb.getSession());
	
			switch(filterType){
			case SQL:
				// replace environment variables
				
				int cnt= Tools.getInt(QueryEngine.getInstance().doQueryOne(f,conn), -1);
				b=(cnt>0);
				break;
				
			case BEANSHELL:
					Object ret=BshScriptUtils.evalScript(f,new StringBuffer(),false, env);
					//when null, return false
					if(ret!=null){
						if(ret instanceof Boolean) b= ((Boolean)ret).booleanValue();
						else if(ret instanceof java.lang.Number) b=((Number)ret).intValue()>0;
						else b= Tools.getBoolean(ret, false);
					}
				
				
				break;
			case PYTHON:
				b =PythonScriptUtils.convertInt( PythonScriptUtils.evalScript(f,new StringBuffer(),false, env))>0;
				break;
			}
		}catch(Throwable t){
			logger.error("Fail to eval display condition of web action id="+ this.id, t);
		}
		return b;
	}
	@Override

	public String toHREF(Locale locale,Map env) {
		/*<a href="javascript:pc.doAdd()" accesskey="N">
		<img src="/html/nds/images/tb_new.gif"/>
		����
		</a>*/
		StringBuffer sb=new StringBuffer("<a id=\"wa_").append(this.getId()).append("\" href=\"");
		
		WebAction.ActionTypeEnum ate= this.getActionType();
		String target=this.getUrlTarget();
		switch(ate){
			case URL:
				if(nds.util.Validator.isNull(target)) target="_self";
				sb.append("javascript:popup_window('");
				sb.append(this.getScript()).append("',target='").
					append(target).append("')\">");
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
				sb.append("javascript:oc.webaction(").append(this.getId()).append(",");
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
		sb.append(MessagesHolder.getInstance().getMessage4(locale,StringUtils.escapeForXML(this.getDescription())));
		sb.append("</a>");
		
		String f= StringUtils.replace(sb.toString(),"$OBJECTID$", 
				String.valueOf(getValueFromMap("objectid", env, null,true)));

		f= StringUtils.replace(f,"$MAINTABLE$", 
				String.valueOf(getValueFromMap("maintable", env, null,true)));
		
		return f;
	}

}
