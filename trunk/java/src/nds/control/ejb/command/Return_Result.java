package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.query.*;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.WebKeys;

public class Return_Result extends Command {
public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	java.util.Locale locale= event.getLocale();
	  	String str="";
	  	String value="";
	  	String sql_in="IN(";
	  	String str_in=nds.util.MessagesHolder.getInstance().translateMessage("@contain@",locale)+"(";
	  	String str_notin=nds.util.MessagesHolder.getInstance().translateMessage("@no-contain@",locale)+"(";
	  	String str_notin_m="(";
	  	Boolean flag_in=true;
 	  	String sql_notin="IN(";
 	  	Boolean flag_notin=true;
	  	String sqlxml="";
	  	String clkstr="";
	  	JSONObject joxml=null;
	  	JSONObject jofilter=null;
		JSONObject jsonxml=null;
		Boolean flag=false;
		Expression expr=null;
		Expression exprxml=null;
        jsonxml=new JSONObject();
        Table table=null;
        String returnType=jo.optString("returnType");
	  	try {
			JSONArray ja=(JSONArray)jo.getJSONArray("params");
			TableManager manager =TableManager.getInstance();
			table= manager.findTable(jo.opt("table"));
			for(int i=0;i<ja.length();i++){
                  jo=ja.getJSONObject(i);
                  str=(String)jo.get("value");
                 if(str.indexOf("xml")==-1){
                	 clkstr=(String)jo.get("clkstr");
                	 value=(String)jo.get("id");
                	 if(jo.get("condition").equals("IN")){
                		 if(!flag_in){
                			 sql_in=sql_in+','+jo.get("value");
                			 str_in=str_in+','+jo.get("id");
                		 }else{
                			 sql_in=sql_in+jo.get("value");
                			 str_in=str_in+jo.get("id");
                			 flag_in=false;
                		 }
                	 }else{
                		 if(!flag_notin){
                			 sql_notin=sql_notin+','+jo.get("value");
                			 str_notin=str_notin+','+jo.get("id");
                			 str_notin_m=str_notin_m+','+jo.get("id");
                		 }else{
                			 sql_notin=sql_notin+jo.get("value");
                			 str_notin=str_notin+jo.get("id");
                			 str_notin_m=str_notin_m+jo.get("id");
                			 flag_notin=false;
                		 }
               	  	} 
                 }
			}
			if(!sql_in.equals("IN(")){
				sql_in=sql_in+")";
				str_in=str_in+")";
				/*if(returnType.equals("a")){
					expr=new Expression(new ColumnLink(clkstr),sql_in,null);
				}else{
					expr=new Expression(new ColumnLink(clkstr),sql_in,str_in);
				}
				*/
				expr=new Expression(new ColumnLink(clkstr),sql_in,str_in);
			}
            for(int i=0;i<ja.length();i++){
                jo=ja.getJSONObject(i);
                str=(String)jo.get("value");
                if(str.indexOf("xml")!=-1){	 
                	flag=true;
                	String xml="";
                	str =(String)jo.get("set");
                	joxml=org.json.XML.toJSONObject(str);
                	jofilter=(org.json.JSONObject)joxml.get("filter");
                	sqlxml=sqlxml+(String)jofilter.get("sql");
                	xml=(String)jofilter.get("expr");
                	if(!xml.equals("")){
                		exprxml=new Expression(xml);
                	}
                	if(jo.get("condition").equals("IN")){
                      	if(expr!=null){
                      		expr=expr.combine(exprxml, SQLCombination.SQL_OR,null);
                      	}else{
                      		expr=exprxml;
                      	}
                	}else if(jo.get("condition").equals("NOT IN")){
                		if(expr!=null){
                			expr=expr.combine(exprxml, SQLCombination.SQL_AND_NOT,null);
                		}else{
                			expr=new Expression(null, "1=1","");
                			expr=expr.combine(exprxml, SQLCombination.SQL_AND_NOT,null);
                		}
                	}
                }
			}
            if(!sql_notin.equals("IN(")){
				sql_notin=sql_notin+")";
				str_notin=str_notin+")";
				str_notin_m=str_notin_m+")";
				if(expr!=null){
					/*if(returnType.equals("a")){
						expr=expr.combine(new Expression(new ColumnLink(clkstr),sql_notin,null), SQLCombination.SQL_AND_NOT,null);
					}else{
						expr=expr.combine(new Expression(new ColumnLink(clkstr),sql_notin,str_notin_m), SQLCombination.SQL_AND_NOT,null);
					}
					*/
					expr=expr.combine(new Expression(new ColumnLink(clkstr),sql_notin,str_notin_m), SQLCombination.SQL_AND_NOT,null);
				}else{
					sql_notin="NOT "+sql_notin;
					/*if(returnType.equals("a")){
						expr=new Expression(new ColumnLink(clkstr),sql_notin,null);
					}else{
						expr=new Expression(new ColumnLink(clkstr),sql_notin,str_notin);
					}*/
					expr=new Expression(new ColumnLink(clkstr),sql_notin,str_notin);
				}
			}   
			WebContext ctx = WebContextFactory.get();
			HttpServletRequest request = ctx.getHttpServletRequest();
	  		SessionContextManager scmanager= WebUtils.getSessionContextManager(request.getSession(true));
	  		UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
	  		locale= usr.getLocale();
	  		QueryEngine engine =QueryEngine.getInstance();
			QueryRequestImpl query = engine.createRequest(usr.getSession());
			query.setMainTable(table.getId());
			query.addSelection(table.getPrimaryKey().getId());
			if(expr!=null)query.addParam(expr);
            nds.schema.Filter fo=new nds.schema.Filter();
            Expression paremExpr= query.getParamExpression();
            String paramExpStr=(paremExpr==null?"":paremExpr.toHTMLInputElement());
            String desc="";

            desc=query.getParamDesc(false); 
            fo.setSql("IN("+ encode(query.toPKIDSQL(false))+")");
            
            /*if(returnType.equals("a")){
            	 desc=query.getParamDesc(false); 
            	 fo.setSql("IN("+ encode(query.toPKIDSQL(false))+")");
            }else{
            	 desc=query.getParamDesc(true);
            	 fo.setSql("IN("+ encode(query.toPKIDSQL(true))+")");
            }*/
            fo.setDescription(desc);
            //fo.setSql("IN("+ encode(query.toPKIDSQL(true))+")");
            fo.setExpression(paramExpStr);
            jsonxml.put("desc", desc);
            if(returnType.equals("a")){
            	jsonxml.put("filterxml",paramExpStr);
            }else{
            	jsonxml.put("filterxml",fo.toXML());           	
            }
            jsonxml.put("sql",fo.getSql());  	
		} catch (JSONException t) {
			throw new NDSException(t.getMessage(), t);	
		}
   	  	ValueHolder holder= new ValueHolder();
   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
   		holder.put("code","0");
   		holder.put("data",jsonxml);
   		return holder;
   	  }

		private String encode(String sql){
		    String s=  nds.util.StringUtils.replace(sql,"\"", "\\\"");
		    return s;
		} 

}
