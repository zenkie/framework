/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.schema.*;
import nds.util.*;
import nds.control.event.DefaultWebEvent;
import nds.control.util.AjaxUtils;
import nds.control.util.Result;
import nds.control.util.ValueHolder; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.lang.*;
import java.net.*;
import javax.servlet.ServletContext;
import javax.servlet.http.*;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.WebContext;
import org.json.*;
/**
 * Handle dwr request
 * 
 * Created at 2007-05-08
 * 
 * @author yfzhu@agilecontrol.com
 * @since 3.0
 */

public class AjaxController {
	private static Logger logger= LoggerManager.getInstance().getLogger(AjaxController.class.getName());	
	//How many columns returned when client request partial data in result returned in json
	private final static int PARTIAL_DATA_COLUMNS=6;
	/**
	 * Get jsonObj for nds.control.event.DefaultWebEvent, and return nds.control.util.Result.toJSONString() 
	 * @param jsonObj should be parsed to DefaultWebEvent
	 * @return should be nds.control.util.Result.toJSONString() 
	 * @throws Exception
	 */
	public String handle(String  jsonObj) throws Exception{
		Locale locale= nds.schema.TableManager.getInstance().getDefaultLocale();
		long currentTime= System.currentTimeMillis();
		try{
			WebContext ctx = WebContextFactory.get();
			HttpServletRequest request = ctx.getHttpServletRequest();
			//logger.debug("ctx.getHttpServletResponse().getCharacterEncoding()="+ctx.getHttpServletResponse().getCharacterEncoding());
	        //request.getCharacterEncoding();
	        //ctx.getHttpServletResponse().setContentType("text/html;charset=utf-8");
			ctx.getHttpServletResponse().setCharacterEncoding("UTF-8");
			//logger.debug("2ctx.getHttpServletResponse().getCharacterEncoding()="+ctx.getHttpServletResponse().getCharacterEncoding());
			logger.debug("handle("+ jsonObj+")");
			JSONObject jo= new JSONObject(jsonObj);
			/**
			 * Though I think this will destroy the seperation between web container and business logic
			 * layer, it would give developer more conveniance to handle web page for ajax request.
			 * For instance, developer can request some jsp and print html code as return result. 
			 */
			jo.put("org.directwebremoting.WebContext", ctx);
			
	        SessionContextManager scmanager= WebUtils.getSessionContextManager(request.getSession(true));
	        UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
	        locale= usr.getLocale();
	        String ret=AjaxUtils.handle(jo, usr.getSession(),usr.getUserId(), locale).toJSONString();
	        //logger.debug(ret);
        	logger.debug("Duration:"+ (System.currentTimeMillis()-currentTime)/1000.0+" s");
	        
			return ret;
		}catch(Throwable t){
			logger.error("failed",t);
			throw new NDSException(	WebUtils.getExceptionMessage(t, locale));
		}
	}
	
	/**
	 * There's a big performance issue for this method, since client portalcontrol.js has to consume
	 * great time parse and update html elements. Here we provide resulthandler for this issue
	 * If resulthandler is set in query json object, this method will forward QueryResult to that 
	 * page and set page content in "pagecontent" of JSON object for QueryResult 
	 * 
	 * @param jsonObj should be parsed to QueryRequestImpl, javascript object is like:
		@return json object string
	function QueryRequest(){
		this.table="C_OderItem";
		this.column_masks=[1,5];//Column.MASK_CREATE_EDIT,Column.MASK_QUERY_SUBLIST
		this.column_include_uicontroller=false; // optional, default false
		this.init_query=true;// opt, default to true
							 // if true, will use dir_perm and fixedColumns for where clause construction,
							 // else these params will be ignored since param_expr contains these filters
							 
		this.dir_perm=1; //opt, default to 1, Directory.READ, Directory.WRITE will be used in where clause construction 
		this.fixedcolumns="fixed_columns_a=b&fixed_columns_c=d" // opt, will be used in where clause construction
		this.param_expr=Expression.toString() // optional
		this.param_str="aa=bb&cc=dd" // this is set by user from ui, in format like URL.queryString
		this.start=0;
		this.range=10;
		this.order_column="";
		this.order_asc=false;	
		this.quick_search_column=""; //opt
		this.quick_search_data=""; // opt
		this.accepter_id="" // opt, in format like 'column_'+accepterId (main) or 'eo_<COLUMNID>__<FKTABLE.AKID>'
		this.column=102;// opt, this should be id of column for which current query search data 
		this.show_alert=false;// opt, default to false, if true, will 
		this.resulthandler="/html/nds/portal/result.jsp"; // optional, if set, will forward to that page to construct html code for the result data
		this.noresult=true/false; default to false, when true, will set "query" instead of "result" in HttpServletRequest, this is used for /html/nds/query/search_result_sql.jsp
		this.partialresult=true/false; default to false, when true, will load only first 5 columns value into returned json object.
			this is used for /html/nds/query/search.jsp to return some data to ui.
			partialresult noresult=true 
			his.tag=<json> // opt, can be anything, and will return to client in result.data.tag
	}

	 * @return QueryResult.toJSONString()
	 * @throws Exception
	 */
    public String query(String jsonObj) throws Exception{
    	
		Locale locale= nds.schema.TableManager.getInstance().getDefaultLocale();
		try{
			WebContext ctx = WebContextFactory.get();
			HttpServletRequest request = ctx.getHttpServletRequest();
	        SessionContextManager scmanager= WebUtils.getSessionContextManager(request.getSession(true));
	        UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
			locale=usr.getLocale();
			logger.debug("query("+ jsonObj+")");
			JSONObject jo= new JSONObject(jsonObj);

			Table table= TableManager.getInstance().getTable(jo.getString("table"));
			//必须具有查询的权限 20091213 yfzhu
			usr.checkPermission(table.getSecurityDirectory(), nds.security.Directory.READ);
			
			QueryResult qr=null;
			QueryRequest query=null;
			query=AjaxUtils.parseQuery(jo, usr.getSession(), usr.getUserId(), usr.getLocale());
			boolean  noresult= jo.optBoolean("noresult",false);
			if(!noresult){
				if(query.getMainTable().isBig() && 
					(Validator.isNull(jo.optString("param_str")) && Validator.isNull(jo.optString("param_str2")))
						&& Validator.isNull(jo.optString("fixedcolumns"))	
					){
					// table is big, while client does not specify parameter for search
					qr=QueryEngine.getInstance().doDummyQuery(query, 
							MessagesHolder.getInstance().translateMessage("@specify-filter-for-big-table@",locale));
				}else{
					try{
						qr=QueryEngine.getInstance().doQuery( query);
					}catch(QueryException e){
						qr=QueryEngine.getInstance().doDummyQuery(query, nds.util.StringUtils.getRootCause(e).getMessage());
					}
				}
			}
			JSONObject jr=null; // JSON QueryResult
			/**
			 * We have two ways to handle result: one is to direct result data as json object to
			 * client, so script will create lines for that result data. This is mainly used for 
			 * edit page.
			 * 
			 *  For view page, we send the html page for result data directly, so the client script
			 *  will not create lines to improve performance.
			 */
			String resulthandler=jo.optString("resulthandler");
			String condition=jo.optString("condition"); // "IN" | "NOT IN" USED BY search_result_filter.jsp
			String returnType=jo.optString("returnType");  //"a" for not replaceing environment attribute ($xxx$), and vice vesa.  used in search_result_filter.jsp
			if(Validator.isNull(resulthandler)){
				// handle buttons that defined to show in this query, will convert to html code here
				AjaxUtils.convertButtonHtml((QueryResultImpl)qr, request);
				
				jr= qr.toJSONObject(true);
				
				if(jo.optBoolean("show_alert", false)){
			    	// row style
			    	CollectionValueHashtable qrAlertHolder=new CollectionValueHashtable();
			    	QueryResultMetaData meta= qr.getMetaData();
			    	TableManager manager= TableManager.getInstance();
			    	qr.beforeFirst();
			    	int serialno=0;
			    	Integer serialnoInt;
			    	while(qr.next()){
			    		serialno++;
			    		serialnoInt=new Integer(serialno);
				    	for(int i=0;i< meta.getColumnCount();i++){
				    		Column colmn=manager.getColumn(meta.getColumnId(i+1));
				            nds.web.alert.ColumnAlerter ca=(nds.web.alert.ColumnAlerter)colmn.getUIAlerter();
				            if(ca!=null){
				            	String rowCss=ca.getRowCssClass(qr, i+1, colmn);
				            	if(rowCss !=null){
				            		qrAlertHolder.add(serialnoInt, rowCss);
				            	}
				            }
				    		
				    	}
			    	}
			    	JSONObject alerts= new  JSONObject();
			    	for(Iterator it=qrAlertHolder.keySet().iterator();it.hasNext();){
			    		serialnoInt=(Integer)it.next();
			    		alerts.put("tr_"+serialnoInt , Tools.toString(qrAlertHolder.get(serialnoInt), " ") );
			    	}
			    	jr.put("alerts", alerts);
				}
			}// end resulthandler is null
			else{
				// result handler found, construct page, and return no data to client
  				/**
  				 * Please note param "compress=false" is to prohibit  com.liferay.filters.compression.CompressionFilter from compressing file content 
  				 */
				if(noresult){
					jr=new JSONObject();
					ctx.getHttpServletRequest().setAttribute("query",query);
				}else{
					jr=  qr.toJSONObject(false);
					boolean  partialresult= jo.optBoolean("partialresult",false);
					if(partialresult){
						//only data of first 5 columns 
						JSONArray rows=new JSONArray();
						JSONArray row;
						int cnt= qr.getMetaData().getColumnCount();
						while(qr.next()){
							row=new JSONArray();
							for(int i=0;i< PARTIAL_DATA_COLUMNS && i<=cnt ;i++){
								row.put(qr.getString(i+1,false));
							}
							rows.put(row);
						}
						jr.put("rows",rows);
						// set cursor to initial position 
						qr.beforeFirst();
					}					
					ctx.getHttpServletRequest().setAttribute("result",qr);
				}
				ctx.getHttpServletRequest().setAttribute("jsonobject",jo);
				ctx.getHttpServletResponse().setCharacterEncoding("UTF-8");
				
				//ctx.getHttpServletRequest().setAttribute("com.liferay.filters.compression.CompressionFilter_ALREADY_FILTERED", Boolean.TRUE);	
				String page="";
  				if(condition==null){
  					page=ctx.forwardToString( resulthandler+"?compress=f");
  				}else{
  					if(returnType!=null){
  						page=ctx.forwardToString( resulthandler+"?compress=f&condition="+condition+"&type="+returnType);
  					}
  				}
				/*
				 * Be used for multi query 2009.3.20
				 */
			//	String page=ctx.forwardToString( resulthandler );
  				
  				//logger.debug(resulthandler+":page="+page);
  				jr.put("pagecontent", page);
				
			}
			// by using tag, client can retrive data from previous sending request
			Object tag= jo.opt("tag");
			if(tag!=null) jr.put("tag",tag);
			
			Result r=  new Result();
			r.setCode( 0); 
			r.setData(jr);
			String callbackEvent= jo.optString("callbackEvent", "ProcesssQueryResult");
			r.setCallbackEvent(callbackEvent);
			String rt= r.toJSONString();
			return rt;
		}catch(Throwable t){
			logger.error("fail", t);
			throw new NDSException( 
					MessagesHolder.getInstance().translateMessage(
							nds.util.StringUtils.getRootCause(t).getMessage(), locale) ,t);
		}
		
    }
    
}
