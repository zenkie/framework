
package nds.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.Tools;
/**
 * 主要目的是根据标准的Query.jsp页面输入内容（参见QueryInputHandler)构造出where子句的
 * 内容。目前的实现会在返回的HttpServletRequest 中添加"result"属性（Attribute)，值为String
 * ,形如：
 * {columnName:=value}{columnName2:=value} 不考虑value可能会包含{}等特殊字符的情况
 * columnName代表的是主表相应字段，如果将主表某字段引发的从表的某字段的值作为查找条件，通常出现在referenceTable!=null
 * 的column，这里仍然仅放置主表的那个字段
 *
 * #yfzhu created at 2002-12-23 for PromotionASht.ProductSet column
 */
public class QuerySQLHandler extends HttpServlet {
    private final static String QUERY_SERVLET="/servlets/query";
    private final static String QUERY_ERRORPAGE=nds.util.WebKeys.NDS_URI+"/error.jsp";
    private final static int MAX_SELECT_COUNT=50;
    private final static int MAX_PARAM_COUNT=30;
    private final static int EXCLUDE_VALUE=0;// column.getValues() must be validate, while 0 is default not valid

    private static Logger logger=LoggerManager.getInstance().getLogger(QuerySQLHandler.class.getName());
    /**
     * @roseuid 3B84B0A20211
     */
    public void init() throws ServletException {
        //nds.control.web.MainServlet.initEnvironment(this.getServletContext());

    }
    /**
     * 一般情况下，页面创建request的信息都是放在一个Form中，我们让QueryRequest自动
     * 产生相应的Form，这样页面可以很容易地请求同样query的其他记录（一张页面仅仅显示有限记录）
     * <form name="formName" method="put" action="/nds/servlets/query">
     *      <input type='hidden' name='return_type' value='n'|'m'|'s'> // n表示不返回，m表示返回多个行，n返回一行
     *      <input type='hidden' name='accepter_id' value='input control name'>//返回页面的控件(input type=text)的id
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
     *      <input type='hidden' name='param_count' value='3'>// 下面parameter的个数
     *      <input type='hidden' name='param/0/columns' value='COLUMN1,COLUMN2'> // 参数0对应的column
     *      <input type='hidden' name='param/0/value' value='encode(>=100)'> // value 用encode防止有编码问题
     *      ...
     *      <input type='hidden' name='order_select' value='1'>//order对应的列, value对应selection的第几个
     *
     *      <input type='hidden' name='order/asc' value='true'> //是否升序，缺省不设
     *      <input type='hidden' name='resulthandler' value='../query.jsp'> // result 的显示页面，使用绝对路径表示从contextPath开始，如contextPath="/nds"，而resulthandler为"/query/result.jsp"则最终页面为"/nds/query/result.jsp"
     *
     *
     * </form>
     *  Condition 2:
     *      ? table=xxx&column=xxx&id=xxx&show_all=true&aciontype="modify|add|query, query is default" // this is for objectview.jsp( in TableQueryModel)
     *      note actiontype is for deciding what action command from GUI, @see table.getShowableColumns(int) for details
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        try {
            //logger.debug(toString(req));
        	Locale locale = (Locale)req.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
        	if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
        	TableManager manager=TableManager.getInstance();
            int mainTableId=Tools.getInt(getRequestParameter(req,"table"), -1);
            if( mainTableId == -1) {
                throw new QueryException("Intenal Error: table must be set");
            }
            int startIdx=Tools.getInt(getRequestParameter(req,"start"), 1) -1;
            if( startIdx < 0)
                startIdx=0;
            int range= Tools.getInt(getRequestParameter(req,"range"), QueryUtils.DEFAULT_RANGE);
            SQLBuilder builder=new SQLBuilder();


                String cs;
                String param;
                int[] ids;


                int paramCount= Tools.getInt(getRequestParameter(req,"param_count"), MAX_PARAM_COUNT);
                for( int i=0;i<paramCount;i++) {
                    param="param/"+i+"/columns";
                    cs=getRequestParameter(req,param);
                    if( cs ==null)
                        continue;
                    ids=parseIntArray(cs);
                    if( ids ==null)
                        throw new QueryException("Intenal Error: can not parse '"+ param +"' to int[]");
                    param="param/"+i+"/value";
                    cs=getRequestParameter(req,param);

                    if( cs !=null && !cs.equals("")) {
                        // mind that GUI may send colum of values
                        Column lastColumn= manager.getColumn(ids[ids.length-1]);
                        if(lastColumn !=null && lastColumn.getValues(locale) !=null) {
                           //nmdemo add check for cs value which may not be int, but as " in (10,2)"
                           /* following is orginal one
                            if( (new Integer(cs.trim()).intValue()) ==EXCLUDE_VALUE) {
                                continue;
                            }
                            */
                            try{
                            if( (new Integer(cs.trim()).intValue()) ==EXCLUDE_VALUE) {
                                continue;
                            }}catch(NumberFormatException enfe){}
                        }
                        builder.addParam(manager.getColumn(ids[0]).getName(),cs);
                    }
                }
            req.setAttribute("result",builder.toSQLString() );
            String resultHandler= getRequestParameter(req,"resulthandler");
            this.getServletContext().getRequestDispatcher(resultHandler).forward(req,res);
            return;
        } catch (Exception ex) {
            // direct to Error page to handle
            NDSException e=new NDSException("Error when treating query input from :"+req.getRequestURL()+ ":\n"+ toString(req),ex);
            req.setAttribute("error",ex);
            //Hawke Begin
            if(req.getParameter("formRequest")!=null)
            {
              //request.removeAttribute("error");
              getServletContext().getRequestDispatcher(req.getParameter("formRequest").toString()).forward(req,res);
            }
            //Hawke end
            // there has no flow for this page, direct it to unknown page
            String errorURL= this.QUERY_ERRORPAGE;
            getServletContext().getRequestDispatcher(errorURL).forward(req,res);
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
     * Parse <code>s</code> to an int[], s should has following format:
     * "xxx,xxx,..."
     */
    public int[] parseIntArray(String s) {
        try {
            ArrayList is= new ArrayList();
            StringTokenizer st=new StringTokenizer(s,",");
            while(st.hasMoreTokens()) {
                Integer v=new Integer(st.nextToken());
                is.add(v);
            }
            int[] ret=new int[is.size()];
            for(int i=0;i<ret.length;i++) {
                ret[i]=( (Integer)is.get(i)).intValue();
            }
            return ret;
        } catch(Exception e) {
            //logger.debug("can not parse '"+s+"'as int[]");
            return null;
        }
    }
    /**
     * Every element in <code>s</code> should be an int
     */
    public int[] parseIntArray(String[] s) {
        if( s ==null )
            return null;
        try {
            int[] is=new int[s.length];
            for( int i=0;i< s.length;i++) {
                is[i]= (new Integer(s[i])).intValue();
            }
            return is;
        } catch(Exception e) {
            return null;
        }
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
     * This class will accept all param input and return a string, currently we will
     * not build SQL(mysql) directly here, only a simple string recording all data
     */
    private class SQLBuilder{
        private ArrayList params=new ArrayList();
        /**
         *@param colName string representing the column name of query where, note in
         * this realization, colname will only be the one in main table
         * @param param the user input
         */
        public void addParam(String colName, String param){

            params.add("{"+colName+":=" + param +"}");
        }

        public String toSQLString() throws NDSException{
            if (params.size()==0) throw new NDSException("必须输入至少一个条件!");
            StringBuffer sb=new StringBuffer();
            for (int i=0;i< params.size();i++) sb.append((String)params.get(i));
            return sb.toString() ;
        }
    }
}
