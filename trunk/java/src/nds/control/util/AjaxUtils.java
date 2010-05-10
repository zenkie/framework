/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

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
import nds.util.*;
import nds.web.config.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
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
	
	
	public static Result handle(JSONObject jo, nds.query.QuerySession qsession, int userId, Locale locale ) throws Exception{
        ValueHolder vh=null;
        
		Result r=  new Result();
        try{
	        vh=process(jo,qsession,userId,locale);
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
			r.setMessage(WebUtils.getExceptionMessage(t, locale));
        }
		r.setCallbackEvent(jo.optString("callbackEvent",jo.getString("command")));
		return r;
		
	}
	/**
	 * Get jsonObj for nds.control.event.DefaultWebEvent, and return nds.control.util.Result.toJSONString() 
	 * @param jsonObj should be parsed to DefaultWebEvent
	 * @return should be nds.control.util.Result.toJSONString() 
	 * @throws Exception
	 */
	public static ValueHolder process(JSONObject jo, nds.query.QuerySession qsession, int userId, Locale locale ) throws Exception{

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
        return scc.handleEvent(event);
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
		/**
		 * Support both column_masks and qlcid(QueryListConfig.id), and column_masks has higher priority
		 * 2010-2-27 yfzhu
		 */
		int qlcId=jo.optInt("qlcid",-2);// -1 means meta default qlc, so -2 means not set qlc
		QueryListConfig qlc=null;
		//try loading from QueryListConfig
		if(qlcId==-1){
			qlc= QueryListConfigManager.getInstance().getMetaDefault(table.getId());
		}else if(qlcId>-1){
			qlc= QueryListConfigManager.getInstance().getQueryListConfig(qlcId);
		}		
		Object masks= jo.opt("column_masks");
		if(masks!=null){
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
		}else{
			//try loading from QueryListConfig
			if(qlc==null){
				// not set
				throw new NDSException("Neither column_masks nor qlcid is found in query object:"+ jo);
			}
			query.addSelection(table.getPrimaryKey().getId());
			List<ColumnLink> selections=qlc.getSelections(qsession.getSecurityGrade());
			for(ColumnLink c:selections){
				int[] cids=c.getColumnIDs();
				if(c.getLastColumn().getReferenceTable()!=null){
					//show ak, hide id
					int[] rids= new int[cids.length+1];
					
		    		System.arraycopy(cids, 0, rids, 0, cids.length);
		    		rids[rids.length-1]= c.getLastColumn().getReferenceTable().getAlternateKey().getId();
		    		
					query.addSelection(rids, true, c.getDescription(locale));
				}else
					query.addSelection(cids, false, c.getDescription(locale));
			}
		}
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
			if(acceptorColumn!=null && acceptorColumn.getTable()== table ){
				//at least for Filter type column, the "column"'s table is not main table to query,
				//which is set in regexpression's table json property. yfzhu 2009-11-6
				boolean mustBeActive= jo.optBoolean("must_be_active", true);
				expr2=(acceptorColumn==null?null:QueryUtils.getDropdownFilter(acceptorColumn,mustBeActive));
				if(expr2!=null)expr=expr2.combine(expr, SQLCombination.SQL_AND,null); 
			}
		}else{
			//security param
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
		cs= jo.optString("param_str");// column name in format like "tab"+tabIdx+"_param/"+i+"/columns"
		String cs2=jo.optString("param_str2");  //column in format as ColumnLink
		if ( jo.has("param_str")  ){
			expr2=parseQueryString(cs, locale);
			//logger.debug("param_str:"+ expr2.toString());
			if(expr2!=null && !expr2.isEmpty())expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
			//logger.debug("after param_str"+expr );
		} else if( jo.has("param_str2")){
			expr2=parseQueryStringInColumnLink(cs2, locale);
			logger.debug("param_str2:"+ expr2.toString());
			if(expr2!=null && !expr2.isEmpty())expr=expr2.combine(expr, SQLCombination.SQL_AND,null);
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
			            	 * One week range can be updated by portal.properties#query.date.range
			            	 */
			            	java.util.Calendar c= java.util.Calendar.getInstance();
			            	c.setTimeInMillis(System.currentTimeMillis());
			            	c.add(java.util.Calendar.DAY_OF_MONTH, - QueryUtils.DEFAULT_DATE_RANGE);
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
        /*//support negative value now,meaning starting from last page
         * if( startIdx < 0)
            startIdx=0;*/
        int range= jo.optInt("range", QueryUtils.DEFAULT_RANGE);
		query.setRange(startIdx, range);
		// order
        ids=QueryUtils.parseIntArray(jo.optString("order_columns"));
        if(ids !=null && ids.length>0) {
            boolean b= jo.optBoolean("order_asc",true);
            query.setOrderBy(ids, b);
        }else{
        	// so "order_columns" take previliage over "orderby"
    		JSONArray orderby= jo.optJSONArray("orderby");
    		if(orderby!=null){
	    		for(int i=0;i<orderby.length();i++){
	    			JSONObject od= orderby.getJSONObject(i);
	    			try{
	    				ColumnLink cl= new ColumnLink(table.getName()+"."+ od.getString("column"));
	    				query.addOrderBy( cl.getColumnIDs(), od.optBoolean("asc",true));
	    			}catch(Throwable t){
	    				logger.error("fail to parse column link:"+ table.getName()+"."+ od.optString("column"), t);
	    				throw new NDSException("order by column error:"+ od.optString("column"));
	    			}
	    			
	    		}
    		}else{
    			//orders from ui
    			orderby= jo.optJSONArray("orders");//elements can be converted to ColumnLink
    			if(orderby!=null){
    				for(int i=0;i<orderby.length();i++){
    	    			JSONObject od= orderby.getJSONObject(i);
    	    			try{
    	    				ColumnLink cl= ColumnLink.parseJSONObject(od);
    	    				query.addOrderBy( cl.getColumnIDs(), !Boolean.FALSE.equals(cl.getTag()));
    	    			}catch(Throwable t){
    	    				logger.error("fail to parse column link:"+od , t);
    	    				throw new NDSException("fail to parse column link:"+od);
    	    			}
    	    			
    	    		}
    			}else{
	    			//load for qlc
	    			if(qlc!=null){
	    				for(ColumnLink c:qlc.getOrderBys(qsession.getSecurityGrade())){
	    					query.addOrderBy(c.getColumnIDs(), !Boolean.FALSE.equals(c.getTag()));
	    				}
	    			}
    			}
    		}
        	
        }
        query.enableFullRangeSubTotal(jo.optBoolean("subtotal", false));
        return query;
	}
	
	public static QueryRequestImpl parseRestQuery(JSONObject jo, QuerySession qsession, int userId, Locale locale) throws Exception{
		return parseRestQuery(jo, qsession, userId,locale, new ArrayList());
	}
	/**
	 *
	 * 
	 * @param jsonObj should be parsed to QueryRequestImpl, javascript object is like:

参数：
	table: 	对应表的ID, or name
	columns:[column_name,…] // 通过数组指定要检索的字段，字段必须起始于table指明的表，可以通过ColumnLink方式关联到外键对应的表上的记录，详见Portal关于字段的配置。column_name 为字符串
	params:{
		combine: “and” | “or” | “and not” | “or not”
		expr1: expression,
		expr2 : expression
	}，或者 {
		expr: expression
	}，expression 描述见下：
		expression :{ 
			column : column 名称或者ColumnLink，必须起始于table，忽略主表名称。
			condition : 字符串设置对应字段的条件, 例如 20090901~20091021表示日期的范围，>10表示数字字段的范围，输入方式与PORTAL界面一致
		} 通过expression设定某个字段满足某个条件，对于诸如exists类型的请求，可以设置column为空，在condition里直接输入exists(select x from y where z)类似的语句，注意主表在整体SQL语句构造时将被赋予全名称。
	start: 从结果集合的哪一行开始获取记录（从0计算）
	range: 最多获取start行开始的多少条记录
	count: <true>| <false>, 是否计算结果集的总行数，将体现在返回结果的 count里
	orderby:[ordercol,…] 数组，每个元素都为下文对象
		ordercol:{
			column: // 排序字段名称，可以是ColumnLink，字段不必在columns里出现
			asc: <true>|<false> // 排序，true 为顺序，否则为逆序
		}
		 * @selections must be an empty one, will add selection columns in it
		 * @return
		 * 
	 * @throws Exception
	 */
	private static QueryRequestImpl parseRestQuery(JSONObject jo, QuerySession qsession, int userId, Locale locale, ArrayList selections) throws Exception{
		logger.debug(jo.toString());
		QueryEngine engine =QueryEngine.getInstance();
		QueryRequestImpl query = engine.createRequest(qsession);
		
		TableManager manager =TableManager.getInstance();
		Table table;
		int tableId=jo.optInt("table",-1);
		if(tableId>-1)table=manager.getTable(tableId);
		else table=manager.getTable(jo.optString("table"));

		if(table==null) throw new NDSException("cound not find table "+ jo.optString("table"));
		
		query.setMainTable(table.getId());
		
		JSONArray columns= jo.optJSONArray("columns");
		//ArrayList selections= new ArrayList();//elements are Column
		
		//Select
		if(columns!=null){
			for(int i=0;i<columns.length();i++){
				String colname= columns.getString(i);
				try{
					ColumnLink cl= new ColumnLink(table.getName()+"."+ colname);
					query.addSelection( cl.getColumnIDs(), false,null);
					selections.add(cl.getLastColumn());
				}catch(Throwable t){
					logger.error("fail to parse column link:"+ table.getName()+"."+ colname, t);
					throw new NDSException("column selection error:"+ colname);
				}
			}
		}else{
			JSONArray column_masks =jo.optJSONArray("column_masks");
			
			int[] columnMasks;
			if(column_masks!=null){
				columnMasks=new int[column_masks.length()];
				for(int i=0;i< columnMasks.length ;i++) columnMasks[i]=  column_masks.getInt(i);
			}else{
				columnMasks= new int[]{6};// single object view
			}
			int sgrade=0;
			if(qsession!=null)sgrade= qsession.getSecurityGrade();
			else sgrade= nds.control.util.SecurityUtils.getUser(userId).getSecurityGrade();
			ArrayList cols= table.getColumns(columnMasks, false, sgrade);
			for(int i=0;i<cols.size();i++){
				Column col= (Column)cols.get(i);
				query.addSelection(col.getId());
				selections.add(col);
			}
		}
		
		// Where
		Expression expr=null,expr2;
		// user read permission
		expr=SecurityUtils.getSecurityFilter(table.getName(), nds.security.Directory.READ, userId, qsession);
		if(!expr.isEmpty())
			expr=expr.combine(parseExpression(jo.optJSONObject("params"), table),Expression.SQL_AND,null);
		else
			expr=parseExpression(jo.optJSONObject("params"), table);
		
		query.addParam(expr);
		
		// range
        int startIdx=jo.optInt("start", 0);
        if( startIdx < 0)
            startIdx=0;
        int range= jo.optInt("range", 0);
        if(range <=0){
			Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
			range=Tools.getInt(conf.getProperty("rest.query.max.range"),-1);
			if(range==-1) range=QueryUtils.MAXIMUM_RANGE;
        }
		query.setRange(startIdx, range);
		// order
		JSONArray orderby= jo.optJSONArray("orderby");
		if(orderby!=null)
		for(int i=0;i<orderby.length();i++){
			JSONObject od= orderby.getJSONObject(i);
			try{
				ColumnLink cl= new ColumnLink(table.getName()+"."+ od.getString("column"));
				query.addOrderBy( cl.getColumnIDs(), od.optBoolean("asc",true));
			}catch(Throwable t){
				logger.error("fail to parse column link:"+ table.getName()+"."+ od.optString("column"), t);
				throw new NDSException("order by column error:"+ od.optString("column"));
			}
			
		}
		return query;
	}
	/**
	 * 
	 * @param jsonObj should be parsed to QueryRequestImpl, javascript object is like:

参数：
	table: 	对应表的ID
	columns:[column_name,…] // 通过数组指定要检索的字段，字段必须起始于table指明的表，可以通过ColumnLink方式关联到外键对应的表上的记录，详见Portal关于字段的配置。column_name 为字符串
	params:{
		combine: “and” | “or” | “and not” | “or not”
		expr1: expression,
		expr2 : expression
	}，或者 {
		expr: expression
	}，expression 描述见下：
		expression :{ 
			column : column 名称或者ColumnLink，必须起始于table，忽略主表名称。
			condition : 字符串设置对应字段的条件, 例如 20090901~20091021表示日期的范围，>10表示数字字段的范围，输入方式与PORTAL界面一致
		} 通过expression设定某个字段满足某个条件，对于诸如exists类型的请求，可以设置column为空，在condition里直接输入exists(select x from y where z)类似的语句，注意主表在整体SQL语句构造时将被赋予全名称。
	start: 从结果集合的哪一行开始获取记录（从0计算）
	range: 最多获取start行开始的多少条记录
	count: <true>| <false>, 是否计算结果集的总行数，将体现在返回结果的 count里
	orderby:[ordercol,…] 数组，每个元素都为下文对象
		ordercol:{
			column: // 排序字段名称，可以是ColumnLink，字段不必在columns里出现
			asc: <true>|<false> // 排序，true 为顺序，否则为逆序
		}
	
	 * @return QueryResult.toJSONString()
	 * @throws Exception
	 */	
	public static JSONObject doRestQuery(JSONObject jo, QuerySession qsession, int userId, Locale locale) throws Exception{
		QueryEngine engine =QueryEngine.getInstance();
		ArrayList selections=new ArrayList();
		QueryRequestImpl query = parseRestQuery(jo,qsession,userId,locale, selections);
		
		boolean bCount= jo.optBoolean("count",false);
		int count=-1;
		if(bCount) count=Tools.getInt( engine.doQueryOne( query.toCountSQL()), -1);
		String sql;
		if(count >= 0 && count<=query.getRange()){
			sql= query.toSQL();
		}else{
			sql=query.toSQLWithRange();
		}
		logger.debug(sql);
		JSONArray row;
		JSONArray res=new JSONArray();
        Statement stmt=null;
        ResultSet rs=null;
        Connection conn=null;
        java.util.Date date;
        double d;
        int dn;
        String s;

    	SimpleDateFormat dtsf=(SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get();
    	SimpleDateFormat df=(SimpleDateFormat)QueryUtils.dateNumberFormatter.get();
    	
        try{
        	conn= engine.getConnection();
        	stmt= conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY ) ;
        	rs= stmt.executeQuery(sql);
        	while(rs.next()){
        		row=new JSONArray();
        		for(int i=0;i< selections.size();i++){
        			Column col= (Column)selections.get(i);
        			Object ro= rs.getObject(i+1);
        			if(!rs.wasNull()){
        				row.put(ro);
        			}else{
        				row.put(JSONObject.NULL);
        			}
        		}
        		res.put(row);
        	}
        	// convert limit value description
        	TableManager tm= TableManager.getInstance();
        	Object cell;
            //check columns of ColumnInterpreter
            for ( int i=0;i< selections.size();i++){
            	Column col= (Column)selections.get(i);
                int colId=col.getId();
                if ( col.isValueLimited() ){
                    for ( int j=0;j< res.length();j++){
                        cell=res.getJSONArray(j).get(i);
                        try{
                        	s=tm.getColumnValueDescription(colId, String.valueOf(cell),locale);
                        	res.getJSONArray(j).put(i,s);
                        }catch(Exception e){
                            logger.error("Could not interpret cell(" + j + ","+ (i+1)+"):"+cell , e);
                        }

                    }
                }
            }        	
        }finally{
            if( rs !=null){try{ rs.close();}catch(Exception e2){}}
            if( conn !=null){try{ QueryEngine.getInstance().closeConnection(conn);}catch(Exception e){}}
        	
        }
        JSONObject j=new JSONObject();
        j.put("rows", res);
        if(bCount) j.put("count",  count);

        return j;
        
	}
	
	/**
	 * 
	 * @param jo {
		combine: “and” | “or” | “and not” | “or not”
		expr1: expression,
		expr2 : expression
	}，或者 {
		expression
	}，expression 描述见下：
		expression :{ 
			column : column 名称或者ColumnLink，必须起始于table，忽略主表名称。
			condition : 字符串设置对应字段的条件, 例如 20090901~20091021表示日期的范围，>10表示数字字段的范围，输入方式与PORTAL界面一致
		} 通过expression设定某个字段满足某个条件，对于诸如exists类型的请求，可以设置column为空，在condition里直接输入exists(select x from y where z)类似的语句，注意主表在整体SQL语句构造时将被赋予全名称。

	 * @return never null
	 * @throws Exception
	 */
    private static Expression parseExpression(JSONObject jo, Table mainTable)throws Exception{
    	
    	if(JSONObject.NULL.equals(jo)) return Expression.EMPTY_EXPRESSION;
    	Expression expr=null;
    	String combine= jo.optString("combine");
    	if(Validator.isNull(combine)){
    		//load column and description
    		String condition= jo.getString("condition");
    		String column= jo.optString("column");
    		ColumnLink cl=null;
    		if(nds.util.Validator.isNotNull(column)) 
    			cl=new ColumnLink(mainTable.getName()+"."+ column);
    		expr= new Expression(cl,condition,null );
    	}else{
    		Expression expr1=parseExpression(jo.getJSONObject("expr1"),mainTable);
    		Expression expr2=parseExpression(jo.getJSONObject("expr2"),mainTable);
    		expr= expr1.combine(expr2, Expression.parseCombination(combine),null);
    	}
    	return expr;
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
     * Parse string to Expression, in contrast to #parseQueryString, column inputs are named as ColumnLink
     * @param params in format like URL.queryString, should not be null
     * @return Expression for QueryRequest construction
     */
    private static Expression parseQueryStringInColumnLink(String params,Locale locale) throws Exception{
    	if(Validator.isNull(params)) return Expression.EMPTY_EXPRESSION;
    	CGIParser parser= new CGIParser(params,"UTF-8");
    	java.util.Enumeration e=parser.getParameterNames();
    	HashMap map=new HashMap();
    	while(e.hasMoreElements()){
    		String key= (String)e.nextElement();
    		map.put(key, parser.getParameter(key)); // we do not handle string[] here
    	}
    	return QueryUtils.parseConditionInColumnLink(map, locale);
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
