/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nds.control.event.DefaultWebEvent;
import nds.control.web.AjaxController;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.schema.Column;
import nds.schema.DisplaySetting;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.MessagesHolder;
import nds.util.PairTable;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.util.Validator;
import nds.util.WebKeys;

import org.directwebremoting.WebContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;

//import org.directwebremoting.WebContextFactory;
import org.directwebremoting.WebContext;
import com.Ostermiller.util.CGIParser;
import com.Ostermiller.util.NameValuePair;;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class AjaxUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger(AjaxUtils.class.getName());	 
	/**
	 * Get jsonObj for nds.control.event.DefaultWebEvent, and return nds.control.util.Result.toJSONString() 
	 * @param jsonObj should be parsed to DefaultWebEvent
	 * @return should be nds.control.util.Result.toJSONString() 
	 * @throws Exception
	 */
	public static Result handle(JSONObject jo, nds.query.QuerySession qsession, int userId, Locale locale ) throws Exception{

        DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
        String command=jo.getString("command");
//        UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
        if(qsession!=null)
        	event.put("nds.query.querysession",qsession);
        event.put("JAVA.UTIL.LOCALE", locale); 	
        event.setParameter("command", command );
        String operatorid=String.valueOf(userId);
        event.setParameter("operatorid", operatorid);
        /**
         * Some event will have json object contain parameters, some not (UpdateGrid) 
         */
        if(nds.util.Tools.getYesNo(jo.opt("parsejson"), false)){
        	String key;Object value;
        	for(Iterator it=jo.keys();it.hasNext();){
        		key= (String)it.next();
        		value= jo.get(key);
        		if(value!=null && value instanceof String) value= ((String)value).trim(); 
        		if(JSONObject.NULL.equals(value)) value=null;
        		
        		if(value!=null &&  (value instanceof JSONArray)){
        			value=nds.util.JSONUtils.toStringArray((JSONArray)value);
        		}
        		event.put(key, value);
        	}
        }else{
        	event.put("jsonObject", jo);
        }
        // this is to set no user transacton, and each row operation will start its own transaction
        if( jo.optBoolean("bestEffort", false) || 
        		"N".equals(jo.optString("nds.control.ejb.UserTransaction", "N"))
        )event.put("nds.control.ejb.UserTransaction" , "N");
        ClientControllerWebImpl scc = (ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.WEB_CONTROLLER);
		Result r=  new Result();
        try{
            ValueHolder vh=null;
	        vh=scc.handleEvent(event);
	        Object d= vh.get("data");
	        if(d instanceof JSONObject){
	        	JSONObject data= (JSONObject)d;
	        	Object qr=data.opt("qresult");
	        	if(qr !=null && (qr instanceof QueryResultImpl)){
	        		// it may contain buttons, so should convert to button html
	    			WebContext ctx = (WebContext)jo.get("org.directwebremoting.WebContext");
	        		
	    			HttpServletRequest request = ctx.getHttpServletRequest();
	    			convertButtonHtml((QueryResultImpl)qr, request);
	        	}
	        }
			r.setCode(Tools.getInt( vh.get("code"), 0));
			r.setMessage( MessagesHolder.getInstance().translateMessage((String)vh.get("message"), locale));
			r.setData( d);
        }catch(Throwable t){
        	r.setCode(-1);
			r.setMessage(MessagesHolder.getInstance().translateMessage(t.getMessage(), locale));
        }
		r.setCallbackEvent(jo.optString("callbackEvent",command));
		return r;
	}
	 
	/**
	 * 
	 * @param jsonObj should be parsed to QueryRequestImpl, javascript object is like:

	function QueryRequest(){
		this.table="C_OderItem";
		this.column_masks=[1,5];//Column.MASK_CREATE_EDIT,Column.MASK_QUERY_SUBLIST
		this.column_include_uicontroller=false; // optional, default false
		this.init_query=true;// opt, default to true
							 // if true, will use dir_perm and fixedColumns for where clause construction,
							 // else these params will be ignored since param_expr contains these filters
							 
		this.dir_perm=1; //opt, default to 1, Directory.READ, Directory.WRITE will be used in where clause construction 
		this.fixedColumns="fixed_columns_a=b&fixed_columns_c=d" // opt, will be used in where clause construction
		this.param_expr=Expression.toString() // optional
		this.param_str="aa=bb&cc=dd" // this is set by user from ui, in format like URL.queryString 
		this.start=0;
		this.range=10;
		this.order_columns="1,3";
		this.order_asc=false;	
		this.quick_search_column=""; //opt
		this.quick_search_data=""; // opt
		this.accepter_id="" // opt, in format like 'column_'+accepterId (main) or 'eo_<COLUMNID>__<FKTABLE.AKID>'
		this.column=102;// opt, this should be id of column for which current query search data 
		this.subtotal=false;// opt, if true, will generate full range subtotal line
		this.must_be_active=true;// opt, default to true, if false, will inculde data that has "isactive" set to "N"
	}

	 * @return QueryResult.toJSONString()
	 * @throws Exception
	 */	
	public static QueryRequestImpl parseQuery(JSONObject jo, QuerySession qsession, int userId, Locale locale) throws Exception{
		QueryEngine engine =QueryEngine.getInstance();
		QueryRequestImpl query = engine.createRequest(qsession);
		
		TableManager manager =TableManager.getInstance();
		Table table= manager.getTable(jo.getString("table"));
		query.setMainTable(table.getId());
		
		Object masks= jo.get("column_masks");
		int[] cmasks;
		
		if(masks instanceof JSONArray){
			cmasks= new int[((JSONArray)masks).length()];
			for(int i=0;i<cmasks.length;i++){
				cmasks[i]= ((JSONArray)masks).getInt(i);
			}
		}else{
			cmasks= new int[1];
			cmasks[0]= Tools.getInt(masks, -1);
		}
		logger.debug( Tools.toString(cmasks)+", jo:"+ masks+", masks class:"+ masks.getClass());
		//Select
		query.addSelection(table.getPrimaryKey().getId());
		query.addColumnsToSelection(cmasks, jo.optBoolean("column_include_uicontroller",false));
		
		// Where
		Expression expr=null,expr2;
		String cs;
		if( jo.optBoolean("init_query", true)){
			// first time construct query obj, will fetch filter condition in 
			//this.dir_perm=1; //opt, default to 1, Directory.READ, Directory.WRITE will be used in where clause construction 
			//this.fixedColumns="fixed_columns_a=b&fixed_columns_c=d" // opt, will be used in where clause construction
			PairTable fixedColumns=PairTable.parseIntTable(jo.optString("fixedcolumns"), null);
			for( Iterator it=fixedColumns.keys();it.hasNext();){
	        	Integer key=(Integer) it.next();
	            Column col=manager.getColumn( key.intValue());
	            ColumnLink cl=new ColumnLink( col.getTable().getName()+"."+ col.getName());
	            expr2= new Expression(cl,"="+ fixedColumns.get(key),null);
	            expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
	        }
			// directory perm
			int dirPerm= jo.optInt("dir_perm", nds.security.Directory.READ);
			expr2 =SecurityUtils.getSecurityFilter(table.getName(), dirPerm, userId, qsession);
			
			if(expr2!=null && !expr2.isEmpty()){
	        	expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
	        }
			if(dirPerm==nds.security.Directory.WRITE){
				// try filter status column for only status=1 rows
				Column column= table.getColumn("status");
		    	if ( column!=null){
		    		ColumnLink cl=new ColumnLink(new int[]{column.getId()});
		    		expr2= new Expression(cl,"=1",nds.util.MessagesHolder.getInstance().getMessage(locale, "not-submit"));
		        	expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
		    	}
			}
			// dropdown filter on the column
			Column acceptorColumn=  TableManager.getInstance().getColumn(jo.optInt("column",-1));
			boolean mustBeActive= jo.optBoolean("must_be_active", true);
			expr2=(acceptorColumn==null?null:QueryUtils.getDropdownFilter(acceptorColumn,mustBeActive));
			if(expr2!=null)expr=expr2.combine(expr, SQLCombination.SQL_AND,null); 
		}
			
		
		//when init, will also try param_expr for additional setting
		// this is used in command.UpdateGridData
		//query filter have already set in param_expr
		cs=jo.optString("param_expr"); 
		if ( Validator.isNotNull(cs)){
            // expression contains all param conditions
			expr2=new Expression(cs);
			//logger.debug("param_expr:"+ expr2.toString());
        	expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
        	//logger.debug("after param_expr"+expr );
		}
		
		//check param_str, this is set by user in query form
		cs= jo.optString("param_str");
		if ( Validator.isNotNull(cs)){
			expr2=parseQueryString(cs, locale);
			//logger.debug("param_str:"+ expr2.toString());
			if(expr2!=null && !expr2.isEmpty())expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
			//logger.debug("after param_str"+expr );
		}else{
		/**
		 *  system will try to load recent one week data if "param_str" is not set (this will cover 
		 *  the one week range) and "fixedColumns" not set (this occurs when item table is shown)
		 *  
		 */
		//if( jo.optBoolean("tryrecent", false)){
		if(Validator.isNull(jo.optString("fixedcolumns"))){
			//default to recent one week and status=1 
			Column column;
	    	ArrayList columns=table.getIndexedColumns();
	    	boolean firstDateColumnFound=false;
	    	for(int i=0;i< columns.size();i++){
	    		column= (Column)columns.get(i);
	    		if(column.getName().equalsIgnoreCase("STATUS")){
	    			ColumnLink cl=new ColumnLink(new int[]{column.getId()});
		    		expr2= new Expression(cl,"=1",nds.util.MessagesHolder.getInstance().getMessage(locale, "not-submit"));
		        	expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
	    		}else{
		    		if(!firstDateColumnFound && (column.getType()== Column.DATE || column.getType()== Column.DATENUMBER)){
			    		ColumnLink cl=new ColumnLink(new int[]{column.getId()});
			    		if( column.getType() == Column.DATE){
				    		expr2= new Expression(null,table.getName() +"."+column.getName()+" between sysdate-7 and sysdate",column.getDescription(locale)+ nds.util.MessagesHolder.getInstance().getMessage(locale, "in-one-week"));
				    		
			    		}else{
			    			// date number
			            	/**
			            	 * One week range
			            	 */
			            	java.util.Calendar c= java.util.Calendar.getInstance();
			            	c.setTimeInMillis(System.currentTimeMillis());
			            	c.add(java.util.Calendar.DAY_OF_MONTH, -7);
			            	String startDate=  ((java.text.SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(c.getTime());
			            	String endDate=   ((java.text.SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(new java.util.Date());
			            	expr2= new Expression(null,table.getName() +"."+column.getName()+" between "+ startDate +" and "+ endDate,column.getDescription(locale)+ nds.util.MessagesHolder.getInstance().getMessage(locale, "in-one-week"));
			    		}
			    		expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
			    		firstDateColumnFound=true;
		    		}
	    		}
	    	}
		  }//end fixedcolumns	
		//}
		}
		
		
		int[] ids;
		String data_search=jo.optString("quick_search_data");
        if(data_search!=null && data_search.trim().length() > 0){
             cs= jo.optString("quick_search_column");
             ids=QueryUtils.parseIntArray(cs);
             ColumnLink clnk2= (new ColumnLink(ids));
             String cond=  checkCondition( clnk2.getLastColumn(),  data_search,locale);
             if(cond !=null){
                 expr2= new Expression(clnk2,cond, null);
                 expr= expr2.combine(expr, SQLCombination.SQL_AND, null);
             }
         }	
        /*由于在Column.Filter 上增加过滤器，此过滤器也将作用于界面，故此处将根据
	     *      accepter_id 的结构解析出对应的Column 上的过滤器，主要的依据是 "column_" 后的id 内容
	     * 详见 nds.control.ejb.command.ObjectColumnObtain                
        */
        Column returnColumn=manager.getColumn(jo.optInt("column",-1)) ;//
        if(returnColumn==null) returnColumn=QueryUtils.getReturnColumn(jo.optString("accepter_id"));
        if(returnColumn!=null){
        	//add column's filter to expr, 并且filter 不是wildcard filter, 否则web查询界面上会预先配置好，见search.jsp#q_form_param_expr
        	if(returnColumn.getFilter()!=null && !returnColumn.isFilteredByWildcard() ){
        		Expression exprFilter= new Expression(null, returnColumn.getFilter(), 
        				returnColumn.getDescription(locale)+ MessagesHolder.getInstance().getMessage(locale, "-have-special-filter"));
        		if(expr!=null) expr= expr.combine(exprFilter, SQLCombination.SQL_AND, null);
        		else expr= exprFilter;
        	}
        }
        
		if(expr!=null){
			//logger.debug("query expr"+expr.toString());
			query.addParam(expr);
		}
		
		// range
        int startIdx=jo.optInt("start", 0);
        if( startIdx < 0)
            startIdx=0;
        int range= jo.optInt("range", QueryUtils.DEFAULT_RANGE);
		query.setRange(startIdx, range);
		// order
        ids=QueryUtils.parseIntArray(jo.optString("order_columns"));
        if(ids !=null) {
            boolean b= jo.optBoolean("order_asc",true);
            query.setOrderBy(ids, b);
        }
        query.enableFullRangeSubTotal(jo.optBoolean("subtotal", false));
        return query;
	}
	
    
    /**
     * yfzhu 2005-05-15 发现关于LimitValue 的字段在界面上直接输入描述选项时查询会出现错误。
     * 例如：状态字段 输入"提交" 时应该由系统自动转换为2
     * 如果发现Column.isValueLimited=true, 将设法替换其中的内容
     * 当前不处理增加了比较符的输入，即如果rawCondtion 含有除了 描述以外的符号，如"=", ">"之类
     * 将无法转换
     * @param rawCondition 形如 "未提交"，"2"等 
     * @return 重构的condition
     */
    private static String checkCondition(Column col, String rawCondition,Locale locale){
    	if (rawCondition==null) return rawCondition;
    	if(col.isValueLimited()){
    		String real= 
    			TableManager.getInstance().getColumnValueByDescription(col.getId(), rawCondition.trim(),locale);
    		if(real!=null) {
    			//logger.debug("Found " + col + ":" + rawCondition + " converted to real:"+ real+ "," + StringUtils.replace(rawCondition, rawCondition.trim(), real));
    			return StringUtils.replace(rawCondition, rawCondition.trim(), real);
    		}
    	}
    	return rawCondition;
    }    
    /**
     * Parse string to Expression
     * @param params in format like URL.queryString, should not be null
     * @return Expression for QueryRequest construction
     */
    private static Expression parseQueryString(String params,Locale locale) throws Exception{
    	CGIParser parser= new CGIParser(params,"UTF-8");
    	java.util.Enumeration e=parser.getParameterNames();
    	HashMap map=new HashMap();
    	while(e.hasMoreElements()){
    		String key= (String)e.nextElement();
    		map.put(key, parser.getParameterValues(key));
    	}
    	return QueryUtils.parseCondition(map, locale);
    }
    
    /**
     * Convert inner Buttons to HTML code. By default, the button value is numeric data, after
     * conversion, the value will be like "<a href=''>caption</a>", so can set on screen directly.
     * 
     * PK must be the first column in query.
     * 
     * @param qr QueryResult 
     * @param request 
     */
    public  static void convertButtonHtml(QueryResultImpl qr, HttpServletRequest request) throws Exception{
    	QueryResultMetaData meta= qr.getMetaData();
    	TableManager manager= TableManager.getInstance();
    	QueryRequest query= qr.getQueryRequest();
    	int[] displayColumnIndices=query.getDisplayColumnIndices();
    	
    	Column col;String btnString;int objectId;
    	
    	col= manager.getColumn( meta.getColumnId(1));
    	if( col.getId()!= query.getMainTable().getPrimaryKey().getId() )
    		throw new Error("Internal error: first column of query must be pk:"+ query.toSQL());
    	
    	for(int i=0;i< meta.getColumnCount();i++){
    		col=manager.getColumn( meta.getColumnId(i+1));
    		if(col.getDisplaySetting().getObjectType()!=DisplaySetting.OBJ_BUTTON ) continue;
    		// so it's button column
    		int column=displayColumnIndices[i];
        	nds.web.button.ButtonCommandUI uic= (nds.web.button.ButtonCommandUI)col.getUIConstructor();
        	qr.beforeFirst();
	        for(int row=0;row< qr.getRowCount();row++){
	        	qr.next();
	        	objectId=Tools.getInt(qr.getObject(1),-1);
	        	if(objectId ==-1) continue;
	        	btnString=uic.constructHTML(request, col, objectId);
	        	qr.updateCell(row, column, btnString);
	        }
    	}
    	qr.beforeFirst();
    }
}
