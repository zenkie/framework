/******************************************************************
*
*$RCSfile: QueryInputHandler.java,v $ $Revision: 1.12 $ $Author: Administrator $ $Date: 2006/07/12 10:10:59 $
*
*$Log: QueryInputHandler.java,v $
*Revision 1.12  2006/07/12 10:10:59  Administrator
*add audit control
*
*Revision 1.11  2006/06/24 00:34:48  Administrator
*no message
*
*Revision 1.10  2006/03/28 02:24:59  Administrator
*no message
*
*Revision 1.9  2006/03/13 01:13:55  Administrator
*no message
*
*Revision 1.8  2005/12/18 14:06:15  Administrator
*no message
*
*Revision 1.7  2005/10/25 08:12:53  Administrator
*no message
*
*Revision 1.6  2005/08/28 00:27:04  Administrator
*no message
*
*Revision 1.5  2005/05/16 07:34:17  Administrator
*no message
*
*Revision 1.4  2005/04/27 03:25:33  Administrator
*no message
*
*Revision 1.3  2005/04/18 03:28:18  Administrator
*no message
*
*Revision 1.2  2005/03/23 17:56:01  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.7  2004/02/02 10:42:54  yfzhu
*<No Comment Entered>
*
*Revision 1.6  2003/09/29 07:37:28  yfzhu
*before removing entity beans
*
*Revision 1.5  2003/08/17 14:25:14  yfzhu
*before adv security
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryInputHandler.java

package nds.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.*;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import nds.control.event.DefaultWebEvent;
import nds.control.util.AjaxUtils;
import nds.control.util.ValueHolder;
import nds.control.web.NDSServletRequest;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.*;
import nds.web.*;
/**
 * 根据页面输入建立QueryRequest，并产生显示页面。
 * //from 2003-09-06 do not support 调用QueryEngine生成QueryResult，
 * 从模型上说，每个调用QueryHandler.service的请求，都包含有一个controlForm，
 * 在这个form中，有如下信息：
 * 1   primarytable (tableID) 查哪张表的信息
 * 2   selection (array of columnID[], in display order)显示哪些字段，如果是从
 *      表的字段，是和主表的哪个字段关联的
 * 3   where (array of columnID[],with input values)条件，条件可能针对主表，
 *      也可能针对从表的某个字段
 * 4   orderby(columnID[], ascending or descending)排序
 * 5   range(startIdx, range)分页显示查询结果的哪些记录
 * 6   direct URL查询结果给谁（显示）
 *
 * 根据这些条件构造QueryRequest，这个类将负责建立SQL语句，然后由QueryEngine负
 * 责执行查询并构造QueryResult，并作为request的attribute传递给相应的jsp 或 servlet显示。
 */
public class QueryInputHandler extends HttpServlet {
    private final static String QUERY_SERVLET=nds.util.WebKeys.WEB_CONTEXT_ROOT+ "/servlets/query";
    private final static String QUERY_ERRORPAGE=nds.util.WebKeys.NDS_URI+"/error.jsp";

    private static Logger logger=LoggerManager.getInstance().getLogger(QueryInputHandler.class.getName());
	private static boolean isMultipleClientEnabled= false;
	static{
		// for webclient.multiple=true, will try to figure out which client currently searching on
		 Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
		 isMultipleClientEnabled= "true".equals(conf.getProperty("webclient.multiple","true"));
	}
    
    /**
     * @roseuid 3B84B0A20211
     */
    public void init() throws ServletException {
        //nds.control.web.MainServlet.initEnvironment(this.getServletContext());

    }
    
    
    /**
     * Parse query request by form elements, for form that contains "query_json"
     * use parseQueryByJson instead
     * 
     * 一般情况下，页面创建request的信息都是放在一个Form中，我们让QueryRequest自动
     * 产生相应的Form，这样页面可以很容易地请求同样query的其他记录（一张页面仅仅显示有限记录）
     * <form name="formName" method="put" action="/nds/servlets/query">
     *      <input type='hidden' name='return_type' value='n'|'m'|'s'> // n表示不返回，m表示返回多个行，n返回一行
     *      <input type='hidden' name='accepter_id' value='input control name'>//返回页面的控件(input type=text)的id
     *     in format like: 
     * 			for single object query:    ${form}.column_${columnId}  such as  single_object_modify.column_28
     *          for multiple object    :    ${from}.tab${tabId}_column_${columnId} such as form_search.tab0_column_22 
     * 
     *      2005-11-17 yfzhu 由于在Column.Filter 上增加过滤器，此过滤器也将作用于界面，故此处将根据
     *      accepter_id 的结构解析出对应的Column 上的过滤器，主要的依据是 "column_" 后的id 内容
     * 
     *      <input type='hidden' name='table' value='13'>            // 主要操作的表
     *      <input type='hidden' name='start' value='1115'> // 查询起始index
     *      <input type='hidden' name='range' value='50'>   // 查询的范围
     *      <input type='hidden' name='select_count' value='3'>// 下面select的个数
     *      <input type='hidden' name='show_maintableid' value='true'> // 是否显示查询记录的id，如果为true,则显示在第一列
     *      <input type='hidden' name='column_selection' value='1,0,3'>// 1,0,3是select的编号，有顺序，可以用select/[value]/columns 获得具体selection 的column
     *      <input type='hidden' name='select/0/columns' value='COLUMN1,COLUMN2'>// select/no，首列必从maintable的某个字段开始，后续列是其referenceTable上的某列,no表示显示顺序，因为select很多
     *      <input type='hidden' name='select/0/show' value='false'> // 需要选出的列是否在页面上显示链接，仅仅对colums.size>1的selection有效，缺省为true
     *      <input type='hidden' name='select/1/columns' value='COLUMN3,COLUMN4,...'>
     *      <input type='hidden' name='select/1/show' value='true'>
     *      <input type='hidden' name='select/1/url' value='/basicinfo/employee.jsp'>// 对应的url
     *      ...
     *
     *      <input type='hidden' name='param_expr' value='<expr>...</expr>'> this is Expression as param, all param/x/xxx will be ignored then
     *
     *      <input type='hidden' name='param_count' value='3'>// 下面parameter的个数
     *      <input type='hidden' name='param/0/columns' value='COLUMN1,COLUMN2'> // 参数0对应的column
     *      <input type='hidden' name='param/0/value' value='encode(>=100)'> // value 用encode防止有编码问题
     *      ...
     *      <input type='hidden' name='order_select' value='1'>//order对应的列, value对应selection的第几个
     *
     *      <input type='hidden' name='order/asc' value='true'> //是否升序，缺省不设
     *      <input type='hidden' name='resulthandler' value='../query.jsp'> // result 的显示页面，使用绝对路径表示从contextPath开始，如contextPath="/nds"，而resulthandler为"/query/result.jsp"则最终页面为"/nds/query/result.jsp"
     *--------------
     *      <input type='hidden' name='quick_search_column' value='maintable_column_id'> // 以主表的哪一个字段作为快速查找字段，首列必从maintable的某个字段开始，后续列是其referenceTable上的某列,no表示显示顺序，因为select很多
     *      <input type='hidden' name='qiick_search_data' value='xxx'> // 快速查找的字段的值
     * 		<input type='hidden' name='quick_search_filterid' value='xxx'>
     * </form>
     *  Condition 2:
     *      ? table=xxx&column=xxx&id=xxx&show_all=true&aciontype="modify|add|query, query is default" // this is for objectview.jsp( in TableQueryModel)
     *      note actiontype is for deciding what action command from GUI, @see table.getShowableColumns(int) for details
     * -------------2003-08-30 add support for advanced query
     * If form's input data contains
     *     <input type='hidden' id=column_$(id)_sql name='param/$id/sql' value='select xxx.id from xxx where yyy'>
     * whose value is not null, then the value of
     *     <input type='hidden' id=column_$(id)     name='param/$id/value' value='包含xxx'>
     * will only contains description of that sql
     *
     * If preferId specified, Expression can also be obtained from database.
     * If preferId!=oldPreferId, then user changed the prefer selection.
     *  preferId= "0" 当选中的时候，将使得页面被刷新，过滤器设置为0
     *  preferId= "1" 当选中的时候，如果当前设置为0，则调用addPreference,>9则调用copyPreferenceAs
     *  preferId= "2" 当选中的时候，如果当前设置为0，则无动作， >9删除并回到0，同时保留最后一次的查询
     *   <input type='hidden' name="preferId" value='<%=preferId%>'>
     *   <input type='hidden' name="oldPreferId" value='<%=preferId%>'>
     *   <input type='hidden' name="preferDesc" value=''>
     * -------------above added at 2003-08-30
     *      * @throws Exception
     */
    private QueryRequestImpl parseQueryByFormElements(HttpServletRequest req) throws Exception{
		Locale locale = (Locale)req.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
		if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
    	TableManager manager=TableManager.getInstance();
        int mainTableId=Tools.getInt(getRequestParameter(req,"table"), -1);
        if( mainTableId == -1) {
            Table tb=manager.findTable(getRequestParameter(req,"table"));
            if (tb ==null)
                throw new QueryException("Intenal Error: table must be set");
            else
                mainTableId= tb.getId() ;
        }
        int startIdx=Tools.getInt(getRequestParameter(req,"start"), 1) -1;
        if( startIdx < 0)
            startIdx=0;
        int range= Tools.getInt(getRequestParameter(req,"range"), QueryUtils.DEFAULT_RANGE);
        UserWebImpl userWeb = ((UserWebImpl)WebUtils.getSessionContextManager(req.getSession(true)).getActor(nds.util.WebKeys.USER));	

        /**
         * 对于运宝这样的系统，guest上来可能是在用其他的域名网站访问，guest不能绑定具体的ad_client
         */
        QueryRequestImpl query=QueryEngine.getInstance().createRequest(
        		(userWeb.isGuest() && isMultipleClientEnabled )? null:userWeb.getSession());
        query.setMainTable(mainTableId);
        query.setRange(startIdx,range);
		/**
		 There's 2 types for selection clause construction:
		  1. --     "show_all" + "actiontype", will select all columns visiable in specified action type, such as Column.QUERY_LIST, with PK in first place
		  2. --     "column_selection" + "show_maintableid" + "select_desc", columns are specified one by one
		*/
        String cs, title;
        String param, paramSQL;
        int[] ids;

        boolean addAllShowableColumnsToSelection=parseBoolean(getRequestParameter(req,"show_all"),false);
        if( addAllShowableColumnsToSelection) {
            String actionType=req.getParameter("actiontype");
            int action=Column.QUERY_LIST;
            if( "add".equalsIgnoreCase(actionType))
                action= Column.ADD;
            else if( "modify".equalsIgnoreCase(actionType))
                action=Column.MODIFY;

            query.addSelection(manager.getTable(mainTableId).getPrimaryKey().getId());
            query.addAllShowableColumnsToSelection(action);
        } else {

            boolean showMainTableId= parseBoolean(getRequestParameter(req,"show_maintableid"),false);
            if( showMainTableId) {
                query.addSelection(manager.getTable(mainTableId).getPrimaryKey().getId());

            }
            ArrayList titles= parseStringArray( getRequestParameter(req,"select_desc"));
            
            int[] selectChosen= QueryUtils.parseIntArray( getRequestParameter(req,"column_selection"));
            int selectCount= Tools.getInt(getRequestParameter(req,"select_count"), QueryUtils.MAX_SELECT_COUNT);
            if( selectChosen !=null)
                selectCount=selectChosen.length;
            
            for( int i=0;i<selectCount ;i++) {
                int pos;
                if(selectChosen !=null)
                    pos=selectChosen[i];
                else
                    pos=i;
                param="select/"+pos+"/columns";
                cs=getRequestParameter(req,param);
                if( cs ==null)
                    continue;
                ids=QueryUtils.parseIntArray(cs);// return null if parse found error
                if( ids ==null) {
                    throw new QueryException("Intenal Error: can not parse '"+ param +"' to int[]");
                }
                param="select/"+i+"/show";
                cs=getRequestParameter(req,param);
                boolean b=parseBoolean(cs, true);// default is tho show link
                // add to query
                
                title= (titles.size()>i) ? (String)titles.get(i): null  ;
                if(title ==null){
            	for(int ids_j=0;ids_j< ids.length;ids_j++) {
                    title += manager.getColumn(ids[ids_j]).getDescription(locale);
                }}
            	query.addSelection(ids,b, title);
            }
        }
            Expression expr;
            expr=parseCondition(req);

            int filterId=Tools.getInt(getRequestParameter(req,"quick_search_filterid"), -1);
            if(filterId!=-1){
            	String expXML=(String) QueryEngine.getInstance().doQueryOne("select expression from c_filter where id="+ filterId);
            	if(Validator.isNotNull(expXML)){
                	Expression expr2=new Expression(expXML);
                	if(expr!=null)expr= expr.combine(expr2, SQLCombination.SQL_AND, null);
                    else expr=expr2;
                	logger.debug("expr:"+expr);
            	}
            }else{
            
                String data_search=getRequestParameter(req,"quick_search_data");
                if(data_search!=null && data_search.trim().length() > 0){
                    cs= getRequestParameter(req,"quick_search_column");
                    ids=QueryUtils.parseIntArray(cs);
                    ColumnLink clnk2= (new ColumnLink(ids));
                    String cond=  checkCondition( clnk2.getLastColumn(),  data_search,locale);
                    if(cond !=null){
	                    Expression expr2= new Expression(clnk2,cond, null);
	                    logger.debug("expr2:"+expr2);
	                    if(expr!=null)expr= expr.combine(expr2, SQLCombination.SQL_AND, null);
	                    else expr=expr2;
	                    logger.debug("expr:"+expr);
                    }
                    //query.addParam(ids, checkCondition( (new ColumnLink(ids)).getLastColumn(),  data_search));
                    //logger.debug("Found quick_search_column:" + cs +":"+cs );
                }
            }
            /*2005-11-17 yfzhu 由于在Column.Filter 上增加过滤器，此过滤器也将作用于界面，故此处将根据
		     *      accepter_id 的结构解析出对应的Column 上的过滤器，主要的依据是 "column_" 后的id 内容
		     * 详见 nds.control.ejb.command.ObjectColumnObtain                
            */
            Column returnColumn=QueryUtils.getReturnColumn(getRequestParameter(req,"accepter_id"));
            if(returnColumn!=null){
            	//add column's filter to expr
            	if(returnColumn.getFilter()!=null && !returnColumn.isFilteredByWildcard()){
            		Expression exprFilter= new Expression(null, returnColumn.getFilter(), 
            				returnColumn.getDescription(locale)+ MessagesHolder.getInstance().getMessage(locale, "-have-special-filter"));
            		if(expr!=null) expr= expr.combine(exprFilter, SQLCombination.SQL_AND, null);
            		else expr= exprFilter;
            	}
            }
            
            
            logger.debug("expr:"+expr);
            query.addParam(expr);
            req.setAttribute("userExpr", expr);
            /**-- MR293 yfzhu modified above --*/

            /* -- yfzhu added at 2003-03-31 for sub-total support **/
            if( query.getMainTable().isSubTotalEnabled() ){
                String fullrange_subtotal= getRequestParameter(req,"fullrange_subtotal");
                try{
                    boolean bFullRange=false;
                    bFullRange= (new Boolean(fullrange_subtotal)).booleanValue() ;
                    query.enableFullRangeSubTotal(bFullRange);
                }catch(Exception e){
                    logger.error("Error getting fullrange_subtotal from request:"+ fullrange_subtotal+", not a boolean:"+e);
                }
            }
            /** --added above for sub-total --*/

            // order
            ids=QueryUtils.parseIntArray(getRequestParameter(req,"order/columns"));
            if(ids !=null) {
                boolean b= parseBoolean(getRequestParameter(req,"order/asc"),true);
                query.setOrderBy(ids, b);
            } else {
                int orderIdx=Tools.getInt(getRequestParameter(req,"order_select"), -1);
                //logger.debug("using order of selection:"+ orderIdx );
                if(orderIdx !=-1) {
                    param="select/"+orderIdx+"/columns";
                    cs=getRequestParameter(req,param);
                    if( cs !=null) {
                        //logger.debug("using order column:"+ cs );
                        ids=QueryUtils.parseIntArray(cs);// return null if parse found error
                        boolean b= parseBoolean(getRequestParameter(req,"order/asc"),true);
                        query.setOrderBy(ids, b);
                    }
                }
            }
        return query;
    }
    /**
     * Create query object according to req, there are two types:
     * one is to create a json query object (using "query_json"), 
     * another is to set "select","from","where","order" in details in httpservletrequest
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        logger.debug(Tools.toString(req));
        try {
        	QueryRequestImpl query=null;
        	String jsonQuery= req.getParameter("query_json");
        	if(Validator.isNull(jsonQuery))
        		query= parseQueryByFormElements(req);
        	else{
        		JSONObject jo= new JSONObject(jsonQuery);
                UserWebImpl userWeb = ((UserWebImpl)WebUtils.getSessionContextManager(req.getSession(true)).getActor(nds.util.WebKeys.USER));	
        		query=AjaxUtils.parseQuery(jo, userWeb.getSession(), userWeb.getUserId(), userWeb.getLocale());
        	}

            req.setAttribute("query",query);
            req.setAttribute("result", "You can not get result from HttpServletRequest!");

            String resultHandler= getRequestParameter(req,"resulthandler");
            //logger.debug("Direct request to uri(with result in request attribute) :"+ resultHandler);
            if( resultHandler ==null || "".equals(resultHandler)) {
                //default to Query servlet
                resultHandler= QUERY_SERVLET;
                query.setResultHandler(resultHandler);
            } else {
                // reattach the handler to result for next time showing
                query.setResultHandler(resultHandler); // so the resulthandler still contains the parameter
                /* resultHandler may contain parameters, will remove them and set to request.attributes
                */
                try{
                java.net.URL theURL= new java.net.URL("http://localhost"+ resultHandler);
                String sParams= theURL.getQuery();
                if( sParams!=null  && sParams.trim().length() > 0){
                    resultHandler= theURL.getPath();
                    // Using QuerySevletRequest now
                    req= setRequestParam(sParams, req);
                }
//                logger.debug("the resultHandler removed param url is:"+ resultHandler);
                }catch(Exception eurl){
                	logger.error("Could not parse url:"+resultHandler, eurl );
                }
            }

            this.getServletContext().getRequestDispatcher(resultHandler).forward(req,res);
            return;
        } catch (Exception ex) {
           	logger.error("found exception:"+ ex, ex);
            // direct to Error page to handle
            NDSException e=new NDSException("Error when treating query input from :"+req.getRequestURL()+ ":\n"+ toString(req),ex);
            req.setAttribute("error",ex);
            //Hawke Begin
            if(req.getParameter("formRequest")!=null)
            {
              //request.removeAttribute("error");
            	logger.debug("forward to " + req.getParameter("formRequest"));
              getServletContext().getRequestDispatcher(req.getParameter("formRequest").toString()).forward(req,res);
              return;
            }
            //Hawke end
            // there has no flow for this page, direct it to unknown page
            String errorURL= this.QUERY_ERRORPAGE;
        	logger.debug("forward to " + errorURL);
            getServletContext().getRequestDispatcher(errorURL).forward(req,res);
        }
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
    private String checkCondition(Column col, String rawCondition,Locale locale){
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
     *
     * @param req
     * @param query
     * @return null if no need to change
     * @throws QueryException
     * @throws IOException
     */
    private Expression parsePreference(String module,HttpServletRequest req, QueryRequestImpl query)throws QueryException,IOException{

        Expression expr;
        /*  preferId= "0" 当选中的时候，将使得页面被刷新，过滤器设置为0
        *  preferId= "1" 当选中的时候，如果当前设置为0，则调用addPreference,>9则调用copyPreferenceAs
        *  preferId= "2" 当选中的时候，如果当前设置为0，则无动作， >9删除并回到0，同时保留最后一次的查询
        */
        int preferId= ParamUtils.getIntAttributeOrParameter(req, "preferId",0);
        int oldPreferId= ParamUtils.getIntAttributeOrParameter(req, "oldPreferId",0);
        if( preferId ==0) {
            // set to 8 user-defined query
            req.setAttribute("preferId", "8");
            return null; // so new query will be constructed accroding to input
        }

        HttpSession session= req.getSession();
        SessionContextManager manager= WebUtils.getSessionContextManager(session);
        UserWebImpl usr=(UserWebImpl)manager.getActor(WebKeys.USER);
        /**
         * 如果preferId 未发生改变，交由原处理进程继续处理
         * 注：在此地可以扩展，加入Cache处理方法，直接通过preferId获取已经解析好的Expression
         */
        if (preferId== oldPreferId) {
            if( preferId==8 ) return null; // let following parseCondition to retrieve user input expre from Form
            else return new Expression(); // nothing changed, let page load from db.
        }

        if( preferId > 9) {
            // clear start to zero
            query.setRange(0, query.getRange());
            // 选择
            // load from user
            req.setAttribute("preferId", preferId+"");
            return new Expression();
        }else{

            try{
                if ( preferId==4){
                    // null filter , 空过滤器
                    req.setAttribute("preferId", "4");
                    // set a null expression
                    expr= new Expression();
                    return expr;
                }
                //操作：保存、修改、删除
                // reset preferId to 0
                req.setAttribute("preferId","0"); // 缺省过滤器, 后续动作将不在 query 上addParam
                if( preferId ==1){
                    // 保存
                    if( oldPreferId ==8) {
                        //新增
                        expr=parseCondition(req);
                        usr.addPreference(module,
                                 ParamUtils.getParameter(req, "preferDesc"),expr.toString() );
                        usr.addPreference(module+ ".range", "range", query.getRange()+"");
                        //query.addParam(expr); // 不设过滤器，因为界面会自动匹配过滤器
                        //return new Expression();
                    }else if ( oldPreferId > 9){
                        // 另存为, 已经不支持
                        logger.error("Save as is no longer supported");
                    }else{
                        logger.error("Found invalid old prefer id (id="+ oldPreferId + " when save ");
                    }

                }else if ( preferId==2){
                    // 删除
                    if ( oldPreferId > 9){
                        usr.deletePreference(oldPreferId);
                        //return new Expression();
                    }else logger.error("Found invalid old prefer id (id="+ oldPreferId + " when delete ");
                }else if ( preferId==3){
                    // Default
                    if ( oldPreferId > 9){
                        usr.setDefaultPreference(module,oldPreferId, query.getRange() );
                    }
                }
            }catch(UpdateException e){
                logger.error("Could not do operation on pereference", e);
                throw new IOException("设置偏好时出现异常："+ e);
            }
            return new Expression();// 缺省过滤器, 后续动作将不在 query 上addParam
        }
        //return null;
    }
    /**
     * @param saveToQuery, if true , will call query.addParam(Expression) immeidately, else,
     *  only return the contructed Expression.
     */
    private Expression parseCondition(HttpServletRequest req)throws QueryException,IOException{
        String expr=req.getParameter("param_expr");
        if ( Validator.isNotNull(expr)){
            // expression contains all param conditions
            Expression  e=new Expression(expr);
            //query.addParam(e);
            return e;
        }else{
        	Locale locale = (Locale)req.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
        	if(locale==null)locale= TableManager.getInstance().getDefaultLocale();

        	Map paramMap= req.getParameterMap();
        	return QueryUtils.parseCondition(paramMap, locale);
        }

    }

    /**
     * Parse <code>s</code> to a boolean vaule, if errors found, return <code>def</code>
     */
    public boolean parseBoolean(String s, boolean def) {
        if( "true".equalsIgnoreCase(s))
            return true;
        else if( "false".equalsIgnoreCase(s))
            return false;
        return def;
    }
    
    /**
     * Parse <param>s</param> seperated by comma, if elements is null or "null", replaced with empty string
     * @param s
     * @return
     */
    private ArrayList parseStringArray(String s){
            ArrayList is= new ArrayList();
    		if (Validator.isNull(s)) return is; 
            StringTokenizer st=new StringTokenizer(s,",");
            String token;
            while(st.hasMoreTokens()) {
            	token= st.nextToken();
            	if( Validator.isNull(token)) token="";
                is.add(token);
            }
            return is;
    }
    
    private String toString(HttpServletRequest req) {
        StringBuffer buf=new StringBuffer();
        Enumeration enu=req.getAttributeNames();
        buf.append("------Attributes--------\r\n");
        while( enu.hasMoreElements()) {
            String att= (String)enu.nextElement();
            buf.append(att+" = "+ req.getAttribute(att)+"\r\n");
        }
        buf.append("------Parameters--------\r\n");
        enu=req.getParameterNames();
        while( enu.hasMoreElements()) {
            String param= (String)enu.nextElement();
            buf.append(param+" = "+ getRequestParameter(req,param)+"\r\n");
        }
        buf.append("------Headers--------\r\n");
        enu=req.getHeaderNames();
        while( enu.hasMoreElements()) {
            String param= (String)enu.nextElement();
            buf.append(param+" = "+ getRequestParameter(req,param)+"\r\n");
        }
        buf.append("\n\rContext path:"+req.getContextPath());
        buf.append("\n\rLocale:"+req.getLocale());
        buf.append("\n\rMethod:"+req.getMethod());
        buf.append("\n\rPathInfo:"+req.getPathInfo());
        buf.append("\n\rPathTranslated:"+req.getPathTranslated());
        buf.append("\n\rQueryString:"+req.getQueryString());
        buf.append("\n\rRemoteAddr:"+req.getRemoteAddr());
        buf.append("\n\rRemoteHost:"+req.getRemoteHost());
        buf.append("\n\rRequestURI:"+req.getRequestURI());
        buf.append("\n\rRequestURL:"+req.getRequestURL());
        return buf.toString();
    }
    /**
     * @param path like "/nds/show.jsp"
     * @return like "/nds/"
     */
    private String getDirectoryOfPath(String path) {

        int lash= path.lastIndexOf('/');
        if( lash !=-1) {
            return path.substring(0, lash+1);
        } else {
            return "";
        }
    }
    private String getRequestParameter(HttpServletRequest req, String param) {
        String s=req.getParameter(param);
        if( s !=null)
            return s.trim();
        return null;
    }
    /**
     *  @param param like "aaa=xxx&bb=yyy"
     */
    private HttpServletRequest setRequestParam(String param, HttpServletRequest req){
        StringTokenizer st=new StringTokenizer(param,"&");
        HashMap map=new HashMap();
        while( st.hasMoreTokens()){
            String s= st.nextToken();
            int equalPos= s.indexOf("=");
            if( equalPos > 0){
                map.put(s.substring(0,equalPos), s.substring(equalPos+1));
//                logger.debug(s.substring(0,equalPos)+"="+s.substring(equalPos+1));
            }
        }
        return new ParameterRequest(req, map);
    }
}


