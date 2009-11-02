/******************************************************************
*
*$RCSfile: QueryUtils.java,v $ $Revision: 1.15 $ $Author: Administrator $ $Date: 2006/07/12 10:11:00 $
********************************************************************/
package nds.query;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.*;

import org.json.*;
import bsh.Interpreter;

import nds.control.check.ColumnCheckImpl;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.*;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.PairTable;
import nds.util.StringBufferWriter;
import nds.util.Tools;
import nds.util.Validator;
import nds.util.WebKeys;

/**
 * Contains various methods related to query
 */
public final class QueryUtils {
    private final static Logger logger=LoggerManager.getInstance().getLogger(QueryUtils.class.getName());
	private final static String GET_LIMIT_VALUE_COMMENTS="select DESCRIPTION ,comments from ad_limitvalue where AD_LIMITVALUE_GROUP_ID="+
	"(select AD_LIMITVALUE_GROUP_ID from ad_column where id=?) order by orderno asc";
	private final static String GET_COLUMN_COMMENTS="select comments from ad_column where id=?";
	private final static String GET_TABLE_COMMENTS="select comments from ad_table where id=?";
    private static final String GET_USER_ENV="select name, value from ad_user_attr where isactive='Y'";
 
    /**
     * As xxxFormat are not threadsafe, we wrapper all of them with ThreadLocal
     */
    /*public final static SimpleDateFormat inputDateFormatter=new SimpleDateFormat("yyyy/MM/dd");
    public final static SimpleDateFormat dateTimeSecondsFormatter=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public final static SimpleDateFormat smallDateTimeSecondsFormatter=new SimpleDateFormat("MM/dd HH:mm");
    public final static SimpleDateFormat smallTimeFormatter=new java.text.SimpleDateFormat("HH:mm:ss");
    public final static DateFormat dateFormatter =new SimpleDateFormat("yyyy/MM/dd");
    public final static DateFormat dateNumberFormatter =new SimpleDateFormat("yyyyMMdd");
    public final static DateFormat timeFormatter =new SimpleDateFormat("yyyy/MM/dd HH:mm");
    public final static DecimalFormat floatFormatter=new DecimalFormat("#0.00");
    public final static DecimalFormat intPrintFormatter=new DecimalFormat("###,###,###");
    public final static DecimalFormat floatPrintFormatter=(new DecimalFormat("###,###,##0.00"));*/
    
    public static ThreadLocal inputDateFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
    	SimpleDateFormat a=new SimpleDateFormat("yyyy/MM/dd");
    	a.setLenient(false);
    	return a;}};
    public static ThreadLocal dateTimeSecondsFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
    	SimpleDateFormat a=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	a.setLenient(false);
    	return a;}};
    public static ThreadLocal smallDateTimeSecondsFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
    	SimpleDateFormat a=new SimpleDateFormat("MM/dd HH:mm");
    	a.setLenient(false);
    	return a;}};
    public static ThreadLocal smallTimeFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
    	SimpleDateFormat a=new SimpleDateFormat("HH:mm:ss");
    	a.setLenient(false);
    	return a;}};
    public static ThreadLocal dateFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
    	SimpleDateFormat a=new SimpleDateFormat("yyyy/MM/dd");
    	a.setLenient(false);
    	return a;}};
    public static ThreadLocal dateNumberFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
    	SimpleDateFormat a=new SimpleDateFormat("yyyyMMdd");
    	a.setLenient(false);
    	return a;}};
    public static ThreadLocal timeFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
    	SimpleDateFormat a=new SimpleDateFormat("yyyy/MM/dd HH:mm");
    	a.setLenient(false);
    	return a;}};
    public static ThreadLocal floatFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
		return new DecimalFormat("#0.00");}};
    public static ThreadLocal intPrintFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
       		return new DecimalFormat("###,###,###");}};
    public static ThreadLocal floatPrintFormatter=new ThreadLocal(){protected synchronized Object initialValue() {
   		return new DecimalFormat("###,###,##0.00");}};

     /**
     * The max chars display in one column, if more chars to be shown,appending "..."
     */
    public final static int MAX_COLUMN_CHARS=255;
    /**
     * Default range of query result, @see QueryRequest.getRange()
     */
    public final static int DEFAULT_RANGE=10;
    public final static int MAXIMUM_RANGE=100;
    public final static int[] SELECT_RANGES = new int[]{10,20,30,50,100};
//    public final static int MAXIMUM_RANGE=20;//100;
//    public final static int[] SELECT_RANGES = new int[]{10,20};//,30,50,100};
    
    private final static String LAST_COMMENTS="select u_comments.creationdate, users.id, users.name, u_comments.content  from u_comments, users where u_comments.id=(select max(id) from u_comments where tablename=? and record_id=?) and users.id=u_comments.ownerid";
    
    private static PairTable formats=new PairTable();// key:  

    public final static int MAX_SELECT_COUNT=50;
    private final static int MAX_PARAM_COUNT=30;
    private final static int EXCLUDE_VALUE=0;// column.getValues() must be validate, while 0 is default not valid
    
    /**
     * Generate sql like '%xxx%'(both, default) or 'xxx%'(left) for string query
     */
	private static boolean isLeftSideMatchOnly= false; 
	static{
		// for webclient.multiple=true, will try to figure out which client currently searching on
		 nds.util.Configurations conf= (nds.util.Configurations)nds.control.web.WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
		 isLeftSideMatchOnly= ! ("both".equals(conf.getProperty("query.wildcard.match","both")));
	}

    /*static{
    	inputDateFormatter.setLenient(false);
    	dateTimeSecondsFormatter.setLenient(false);
    	smallDateTimeSecondsFormatter.setLenient(false);
    	dateNumberFormatter.setLenient(false);
    	
    }*/
    public static Object evalScript(
            String script)
    {
        // Create a PrintStream to capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream( baos );

        // Create an interpreter instance with a null inputstream,
        // the capture out/err stream, non-interactive
        Interpreter bsh = new Interpreter( null, pout, pout, false );


        // Eval the text, gathering the return value or any error.
        Object result = null;
        String error = null;
        // Eval the user text
        try {
            bsh.eval( "retValue" +script );
            result= bsh.get("retValue");
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        /*pout.flush();
        scriptOutput.append( baos.toString() );*/
        return result;
    }    
    /**
     * 
     * @param scale 小数点后的位数
     * @return
     */
    public static DecimalFormat getDecimalFormat(int scale){
    	DecimalFormat  f= (DecimalFormat)formats.get(new Integer(scale));
    	if(f==null){
    		String s= "#0.";
    		for(int i=0;i< scale;i++) s+= "0";
      		f= new DecimalFormat(s);
    		formats.put(new Integer(scale), f);
    	}
    	return f;
    }
    /**
     *   accepter_id  in format like: 
     * 			for single object query:    ${form}.column_${columnId}  such as  single_object_modify.column_28
     *          for multiple object    :    ${from}.tab${tabId}_column_${columnId} such as form_search.tab0_column_22 
     * 此处将根据
     *      accepter_id 的结构解析出对应的Column 上的过滤器，主要的依据是 "column_" 后的id 内容
     * @param accepter_id
     * @return null if not found return column from param
     */
    public static Column getReturnColumn(String accepter_id){
    	try{
	    	if(Validator.isNull(accepter_id)) return null;
	    	int pos=accepter_id.indexOf("column_");
	    	if(pos<0 ) return null;
	    	String id= accepter_id.substring(pos+7);
	    	int cid= Tools.getInt(id, -1);
	    	return TableManager.getInstance().getColumn(cid);
    	}catch(Exception e){
    		logger.error("Could not parse column id from accepter_id :"+accepter_id, e);
    		return null;
    	}
    }   
    public static Expression getDropdownFilter(Column dropdownColumn) throws QueryException{
    	return getDropdownFilter(dropdownColumn,true);
    }
    /**
     * Get filter for list of dropdown column 
     * @param dropdownColumn the column that will be displayed as dropdown box, 
     *        Note for some instance, we just want to show a dropdown type table to show as
     *        a UI component, so the <param>dropdownColumn</param> may be the very AK of that
     *        table. 
     * @param shouldBeActive whether enable "isactive" filter or not. When doing report
     * analisys, will not check this filter      
     * @return expression to construct filter for the dropdown list, or null if no filter
     */
    public static Expression getDropdownFilter(Column dropdownColumn, boolean shouldBeActive) throws QueryException{
    	Expression expr=null;
    	Table table= dropdownColumn.getReferenceTable();
    	/**
    	 * For instance when we just want to table's AK column shown as dropdown list
    	 */
    	if(table==null){
    		// this case, column is AK of table
    		table= dropdownColumn.getTable();
         	if(shouldBeActive && table.isAcitveFilterEnabled()) {
         		expr=new Expression(new ColumnLink(new int[]{table.getColumn("isactive").getId()}),"=Y",null);
         	}
    	}else{
    		// this case, column is FK type 
	     	if(shouldBeActive && table.isAcitveFilterEnabled()) {
	     		expr=new Expression(new ColumnLink(new int[]{table.getColumn("isactive").getId()}),"=Y",null);
	     	}
	    	
			//add column's filter to expr, for wildcard filter, it will constructed elsewhere
			if(dropdownColumn.getFilter()!=null && !dropdownColumn.isFilteredByWildcard()){
				Locale locale= TableManager.getInstance().getDefaultLocale();
				Expression exprFilter= new Expression(null, dropdownColumn.getFilter(),
						dropdownColumn.getDescription(locale)+ MessagesHolder.getInstance().getMessage(locale, "-have-special-filter"));
				if(expr!=null) expr= expr.combine(exprFilter, SQLCombination.SQL_AND, null);
	            else expr= exprFilter;
			}
    	}
    	return expr; 
    }
    /**
     * Parse <code>input</code> to part of SQL WHERE clause.
     * 例如，在界面上用户对字段 employee.name (字符型)输入 "Tom"，则构造的Where子句为:
     *      (employee.name LIKE '%Tom%')<br>
     *     对于employee.birthday(日期型)输入"1968/1/0 - 1971/3/12"，则构造的子句：
     *      (employee.birthday BETWEEN '1968/1/0' and '1971/3/12')
     *     对于employee.age(数字型)输入"<25 ",则构造的子句:
     *      (employee.age < 25 ）
     *
     * @param columnName 对应字段的名称，下文用<column>表示
     * @param input 输入条件.
     *      对于字符型（type=Column.STRING)
     *              格式任意 <input>::={[=]<string>}，
     *                  如果首字母为'='，表示完全匹配，<output>:=={ (<column> = '<string>' )}
     *                  否则进行模糊匹配, 生成的SQL 子句为<output>:=={ (<column> LIKE '%<string>%' )}
     *
     *      对于日期型(type=Column.DATE)
     *          <input>   ::={ <operator> <date> | <date> - <date> }
     *          <date>    ::={ yyyy/MM/dd[,hh:mm] }  注:yyyy为4位数的年，MM为月，其他类推
     *          <operator>::={ > | < | = | >= | <= }
     *          <output>  ::={ (<column> <operator> '<date>' ) |
     *                          ( <column> BETWEEN '<date>' AND '<date>' )}
     *       对于数字型(type=Column.NUMBER)
     *          <input>   ::={ <operator> <numeric> }
     *          <output>  ::={ (<column> <operator> <numeric> )}
     *
     *  @param type 为Column.STRING, Column.DATE, Column.NUMBER
     *  @return String 为SQL WHERE子句的一部分，用以构成完整的Select语句
     *  @throw QueryException 如果type 不是识别类型，或者输入有误
     */
    public static String toSQLClause(String columnName, String input, int type)throws QueryException {
        String ret=null;
        int hyphen;
        switch( type) {
	        case Column.STRING:
	            input=input.trim();
	            if( input.startsWith("=") ||input.startsWith("＝") ) {
	                ret=" ("+columnName+" = '"+input.substring(1)+"') ";
	            } else{
	            	String lcseInput=input.toLowerCase();
	            	if(lcseInput.startsWith("is ") || lcseInput.startsWith("in ")) {
	            		ret=" ("+columnName+" "+ input +") ";
	            	}else{
	            		if(input.contains("*") ){
	            			ret= " ("+columnName+" LIKE '"+input.replace("*", "%")+"') ";
	            		}else if( input.contains("%")){
	            			ret= " ("+columnName+" LIKE '"+input+"') ";
	            		}else{
	            			
		            		ret= " ("+columnName+" LIKE '"+(isLeftSideMatchOnly?"":"%")+input+"%') ";
	            		}
	            	}
	            }
	            break;
	
	        case Column.NUMBER:
	        case Column.DATENUMBER:
                String oper1=parseOperator(input);
                if( "".equals(oper1.trim())){
                	// contains "~", which is used for between
                	hyphen= input.indexOf('~');
                	if ( input.indexOf("~")> 0){
                		ret=" ( "+ columnName+" BETWEEN "+ input.substring(0, hyphen)+
							" AND "+input.substring(hyphen+1) + ") ";
                	}else{
	                	// no operator, use "="
	                    ret=" ("+columnName+"="+input+") ";
                	}
                }else{
                    // input contains operator
                    ret=" ("+columnName+input+") ";
                }

                break;
            case Column.DATE:
                /*---  yfzhu modified at 2003-07-16 for function will disable index feature --*/
                input=input.trim();
                hyphen= input.indexOf('~');
                boolean isStartWithOperator="=><iI".indexOf(input.charAt(0))>-1;
                if( hyphen <0 ||isStartWithOperator) {
                    // one date
                    String oper= parseOperator(input);
                    String date= parseStringExcludeOperator(input);
                    if( ! oper.trim().equals("")) {
                        //ret=" ( trunc("+columnName+") "+oper+toDate(date) +") ";
                        ret=" ("+columnName+oper+toDate(date) +") ";
                    } else {
                        // no date range, we will make whole day as range
                        //ret=" ( trunc("+ columnName+")= "+ toDate(date)+") ";
                        ret=" ( "+ columnName+" BETWEEN "+ toDate(date)+" AND "+toDate(date) + "+ .99999) ";

                    }
                } else {
                    // two date
                    //ret=" (trunc("+columnName+") BETWEEN "+ toDate(input.substring(0,hyphen))+" AND "+
                    //    toDate(input.substring(hyphen+1))+") ";
                    ret=" ("+columnName+" BETWEEN "+ toDate(input.substring(0,hyphen))+" AND "+
                        toDate(input.substring(hyphen+1))+"+ .99999) ";
                }
                break;
             default:
                logger.error("Unexpected type when calling toSQLClause( "+columnName+","+input+","+type+")");
                throw new QueryException("Unexpected type");
        }
        return ret;
    }
    /**
     * Get description of input parameter
     * @param column the column which has the type, and accept the input, normally the last element of column link
     * @param columnDesc column description, not name
     */
    public static String toSQLClauseDesc(Column column, String columnDesc, String input, int type,Locale locale)throws QueryException {
        String ret=null;
        switch( type) {
		        case Column.STRING:
		            input=input.trim();
		            if( input.startsWith("=") || input.startsWith("＝")) {
		                ret=" ("+columnDesc+" = "+input.substring(1)+") ";
		            } else {
		            	String lcseInput=input.toLowerCase();
		            	if(lcseInput.startsWith("is ") || lcseInput.startsWith("in ")) {
		            		ret= " ("+columnDesc+" "+input+") ";
		            	}else
		            		ret= " ("+columnDesc+" "+MessagesHolder.getInstance().getMessage(locale, "sql-contains") +" " +input+") ";
		            }
		            break;
                case Column.NUMBER:
                case Column.DATENUMBER:
	                String oper1=parseOperator(input);
	                if( "".equals(oper1.trim())){
	                    // add support for limit-value
	                    if ( column.isValueLimited() ){
	                        input=TableManager.getInstance().getColumnValueDescription(column.getId(), input.trim(), locale);
	                    }
	                    if(input.indexOf('~')>0){
	                    	ret=" ("+columnDesc+":"+input+") ";
	                    }else{
	                    	ret=" ("+columnDesc+"="+input+") ";
	                    }	                    
	                }else{
	                    ret=" ("+columnDesc+input+") ";
	                }
	                break;
                case Column.DATE:
                	input=input.trim();
	                int hyphen= input.indexOf('~');
	                if( hyphen <0) {
	                    // one date
	                    String oper= parseOperator(input);
	                    String date= parseStringExcludeOperator(input);
	                    if( ! oper.trim().equals("")) {
	                        ret=" ("+columnDesc+oper+toDateDesc(date) +") ";
	                    } else {
	                        ret=" ( "+ columnDesc+" = "+ toDateDesc(date)+") ";
	                    }
	                } else {
	                    ret=" ("+columnDesc+ " "+MessagesHolder.getInstance().getMessage(locale, "sql-between") +" "+ 
	                    toDateDesc(input.substring(0,hyphen))+" "+MessagesHolder.getInstance().getMessage(locale, "sql-to") +" "+
	                    toDateDesc(input.substring(hyphen+1))+") ";
	                }
	                break;
                default:
                logger.error("Unexpected type when calling toSQLClauseDesc( "+columnDesc+","+input+","+type+")");
                throw new QueryException("Unexpected type");
        }
        return ret;
    }
    /**
     * Every element in <code>s</code> should be an int
     */
    public static int[] parseIntArray(String[] s) {
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
         /**
	      * Parse <code>s</code> to an int[], s should has following format:
	      * "xxx,xxx,..."
	      */
	     public static  int[] parseIntArray(String s) {
	         if(s==null) return null;
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
     * get operator string from <code>input<code>
     * operator includes " ",>,<,=, IN, IS
     */
    private static String parseOperator(String str) {
        char[] cs=str.toCharArray();
        int i;
        for(i=0;i< cs.length;i++) {
            char c=cs[i];
            if( c==' ' || c=='>' || c=='<' || c=='=')
                continue;
            // add "in" operator
            if( (c=='i' || c=='I' ) && i<(cs.length-1)&&(cs[i+1]=='N' || cs[i+1]=='n') ){
                i++;
            }
            // add "not in" operator
            if( (c=='n' || c=='N' ) && i<(cs.length-1)&&(cs[i+1]=='O' || cs[i+1]=='o') && i<(cs.length-2)&&(cs[i+2]=='t' || cs[i+2]=='T')&& i<(cs.length-3)&&(cs[i+3]==' ')&& i<(cs.length-4)&&(cs[i+4]=='I' || cs[i+4]=='i')&& i<(cs.length-5)&&(cs[i+5]=='n' || cs[i+5]=='N')){
                i++;
            }
            // add "is" operator
            if( (c=='i' || c=='I' ) && i<(cs.length-1)&&(cs[i+1]=='S' || cs[i+1]=='s') ){
                i++;
            }
            break;
        }
        return str.substring(0, i);
    }
    /**
     * get other string except operator string from <code>input<code>
     */
    private static String parseStringExcludeOperator(String str) {
        char[] cs=str.toCharArray();
        int i;
        for(i=0;i< cs.length;i++) {
            char c=cs[i];
            if( c==' ' || c=='>' || c=='<' || c=='=')
                continue;
//          add "in" operator
            if( (c=='i' || c=='I' ) && i<(cs.length-1)&&(cs[i+1]=='N' || cs[i+1]=='n') ){
                i++;
            }
           // add "not in" operator
            if( (c=='n' || c=='N' ) && i<(cs.length-1)&&(cs[i+1]=='O' || cs[i+1]=='o') && i<(cs.length-2)&&(cs[i+2]=='t' || cs[i+2]=='T')&& i<(cs.length-3)&&(cs[i+3]==' ')&& i<(cs.length-4)&&(cs[i+4]=='I' || cs[i+4]=='i')&& i<(cs.length-5)&&(cs[i+5]=='n' || cs[i+5]=='N')){
                i++;
            }
            // add "is" operator
            if( (c=='i' || c=='I' ) && i<(cs.length-1)&&(cs[i+1]=='S' || cs[i+1]=='s') ){
                i++;
            }            
            break;
        }
        return str.substring( i);
    }
    /**
     * Get next day of <code>today</code>.
     * if today= '1999/12/31', then return '2000/1/1'
     * @param today in format as defined in inputDateFormatter
     * @return date in format as defined in inputDateFormatter
     */
    private static String getNextDay(String today) throws QueryException {
        try {
        	SimpleDateFormat sdf= (SimpleDateFormat) inputDateFormatter.get();
            java.util.Date day=sdf.parse(today);
            return sdf.format(Tools.getNextDay(day));
        } catch(Exception e) {
            logger.error("Error", e);
            throw new QueryException( "@date-format-error@:"+ today);
        }
    }
    /**
     * Different database has different function changing string to date type.
     * Such as, for Oracle, date changing should be like:
     *  to_date(string, DateFormat).
     *
     * Current implementation only support Oracle
     * @todo Use "Abstract Factory" pattern to incorporate more databases
     * $changelog
     *    add date format check if not a valid date format, return <param>date</param>
     *    2005-05-12
     */
    private static String toDate(String date) {
    	
    	SimpleDateFormat sdf= (SimpleDateFormat) dateNumberFormatter.get();
    	try{
    		java.util.Date d= sdf.parse(date);
    	}catch(Exception e2){
    		sdf= (SimpleDateFormat) inputDateFormatter.get();
        	try{
        		java.util.Date d= sdf.parse(date);
        	}catch(Exception e){
        		return date;
        	}
    	}
        return "trunc(to_date('"+date+"','"+ sdf.toPattern()+"'))";
    }
    private static String toDateDesc(String date) {
        return date;
    }
    public static String toHTMLControlForm(QueryRequest req)  throws QueryException{
        return toHTMLControlForm(req,null,"");
    }
    public static String toHTMLControlForm(QueryRequest req, Expression userExpr) throws QueryException {
        return toHTMLControlForm(req,userExpr,"");
    }
    /**
     * 一般情况下，页面创建request的信息都是放在一个Form中，我们让QueryRequest自动
     * 产生相应的Form，这样页面可以很容易地请求同样query的其他记录（一张页面仅仅显示有限记录）
     * // no this one anymore, we sometimes will insert more parameters(not relate to query) to it
     *    see /objext/sheet_item.jsp for sample
     * /deprecate the form/ <form name="formName" method="put" action="/nds/servlets/query">
     *      <input type='hidden' name='table' value='12'>        // 主要操作的表
     *      <input type='hidden' name='start' value='1115'> // start from 1
     *      <input type='hidden' name='range' value='50'>
     *      <input type='hidden' name='select_count' value='3'>// 下面select的个数
     *      <input type='hidden' name='chosen_column_selection' value='1'>// value是select的编号，可以用select/[value]/columns 获得具体selection 的column
     *      <input type='hidden' name='chosen_column_selection' value='0'>// 指明顺序，0在1之后。 缺省情况下select_count 已经指明有几个selection，如果没有chosen_column_selection, 则列出所有selection
     *      <input type='hidden' name='select/0/columns' value='COLUMN1,COLUMN2'>// select/no，首列必从maintable的某个字段开始，后续列是其referenceTable上的某列,no表示显示顺序，因为select很多
     *      <input type='hidden' name='select/0/show' value='false'> // 需要选出的列是否在页面上显示链接，仅仅对colums.size>1的selection有效，缺省为false
     *      <input type='hidden' name='select/1/columns' value='COLUMN3,COLUMN4,...'>
     *      <input type='hidden' name='select/1/show' value='true'>
     *      ...
     *      <input type='hidden' name='param_count' value='3'>// 下面parameter的个数
     *      <input type='hidden' name='param/0/columns' value='COLUMN1,COLUMN2'> // 参数0对应的column
     *      <input type='hidden' name='param/0/value' value='encode(>=100)'> // value 用encode防止有编码问题
     *      ...
     *      <input type='hidden' name='order/columns' value='COLUMN1/COLUMN2..'>//order对应的列
     *      <input type='hidden' name='order/asc' value='true'> //是否升序，缺省不设
     *      <input type='hidden' name='resulthandler' value='../query.jsp'> // result 的显示页面
     * // </form>
     * @param userExpr, if not null, will use this as sql where clause instead of req.getParamExpression
     *   考虑的原因，在界面上的查询结果由以下三种过滤器构成（即req.getParamExpression)：
     *    用户的输入条件[userExpr] and 界面的状态条件（已提交、未提交）and 权限条件
     *   只有用户输入条件是随时变化的，而 后两者可以重新构造，所以将用户输入放置在HTMLControlForm，
     *   其他条件不放。
     *   @param nameSpace will be used as portlet element
     */
    public static String toHTMLControlForm(QueryRequest req, Expression userExpr, String nameSpace) throws QueryException {
        TableManager manager= TableManager.getInstance();
        StringBuffer buf=new StringBuffer();
        StringBufferWriter writer=new StringBufferWriter(buf);

        writer.pushIndent();
        writer.println("<input type='hidden' name='table' value='"+
                       req.getMainTable().getId()+"'>");
        writer.println("<input id='"+nameSpace+"list_form_start' type='hidden' name='start' value='"+
                       (req.getStartRowIndex()+1)+"'>");
        writer.println("<input id='"+nameSpace+"list_form_range' type='hidden' name='range' value='"+
                       req.getRange()+"'>");
        // selections
        writer.println("<input type='hidden' name='select_count' value='"+
                       req.getSelectionCount()+"'>");
        int[] clink;
        for( int i=0;i< req.getSelectionCount();i++) {
            clink=req.getSelectionColumnLink(i);
            if(!req.isSelectionShowable(i))
                continue;
            writer.print("<input type='hidden' name='select/"+i+"/columns' value='" );
            for( int j=0;j< clink.length;j++) {
                writer.print( (j>0?",":"")+clink[j]);
            }
            writer.print("'>");
        }
        // selection titles yfzhu 2005-10-24
        ArrayList al=  req.getAllSelectionDescriptions();
        writer.println("<input type='hidden' name='select_desc' value='");
        for(int i=0;i< al.size();i++){
        	writer.print( (i>0?",":"")+(String)al.get(i));
        }
        writer.print("'>");
        // parameters
        // yfzhu 2003-09-03 add expression support
        if( userExpr ==null){
            if ( req.getParamExpression() ==null){
                /*
                 * Since 2.0, will use Expression only
                writer.println("<input type='hidden' name='param_count' value='"+
                               req.getParamCount()+"'>");
                for( int i=0;i< req.getParamCount();i++) {
                    clink=req.getParamColumnLink(i);
                    writer.println("<input type='hidden' name='param/"+i+"/columns' value='" );
                    for( int j=0;j< clink.length;j++) {
                        writer.print( (j>0?",":"")+clink[j]);
                    }
                    writer.print("'>");
                    writer.println("<input type='hidden' name='param/"+i+"/value' value=\""+
                                   nds.util.StringUtils.escapeForXML(req.getParamValue(i))+"\">" );
                }
                 */  
            	 Expression exp2;
            	 Expression exp=null;
            	 for( int i=0;i< req.getParamCount();i++) {
            	 	clink=req.getParamColumnLink(i);
            	 	String value=nds.util.StringUtils.escapeForXML(req.getParamValue(i));
            	 	exp2= new Expression(new ColumnLink(clink), value,null);
            	 	if(exp!=null) exp= exp.combine(exp2,SQLCombination.SQL_AND, null);
            	 	else exp=exp2;
            	 }
            	 if(exp !=null){
            	 	writer.println("<input id='"+nameSpace+"param_expr' type='hidden' name='param_expr' value='"+
                        exp.toHTMLInputElement()+"'>");
            	 }

            	 
            }else{
                writer.println("<input id='"+nameSpace+"param_expr' type='hidden' name='param_expr' value='"+
                               /*nds.util.StringUtils.escapeForXML*/( req.getParamExpression().toHTMLInputElement())+"'>");
            }
        }else{
            // just serialize user expr only
            writer.println("<input id='"+nameSpace+"param_expr' type='hidden' name='param_expr' value='"+
                           ( userExpr.toHTMLInputElement())+"'>");
        }
        if( req.getOrderColumnLink() !=null) {//Hawke add 'id'
            writer.print("<input type='hidden' id='"+nameSpace+"list_form_ordercolumns' name='order/columns' value='");
            clink=req.getOrderColumnLink();
            for( int j=0;j< clink.length;j++) {
                writer.print( (j>0?",":"")+clink[j]);
            }
            writer.print("'>");//Hawke add 'id'
            writer.println("<input type='hidden' id='"+nameSpace+"list_form_orderasc' name='order/asc' value='"+
                           (req.isAscendingOrder()?"true":"false")+"'>");
        }else{
            // let a null value there
            writer.println("<input type='hidden' id='"+nameSpace+"list_form_ordercolumns'  name='order/columns' value=''>");
            writer.println("<input type='hidden' id='"+nameSpace+"list_form_orderasc'  name='order/asc' value='false'>");
        }
        if( req.getResultHandler()!=null)
            writer.println("<input type='hidden' id='"+nameSpace+"list_form_resulthandler' name='resulthandler' value='"+
                           req.getResultHandler()+"' >");
        // support for subtotal
        if( req.getMainTable().isSubTotalEnabled() ){
            writer.println("<input type='hidden' id='"+nameSpace+"list_form_fullrange' name='fullrange_subtotal' value='"+
                           req.isFullRangeSubTotalEnabled()+"' >");
        }
        writer.popIndent();
        //writer.println("</form>");
        return buf.toString();
    }
    
    /**
     * 只要有排序相关的一部分就可以了
     * 即,start,range,order/columns,order/asc
     */
    public static String toHTMLControlFormStayPage(QueryRequest req) {
        TableManager manager= TableManager.getInstance();
        StringBuffer buf=new StringBuffer();
        StringBufferWriter writer=new StringBufferWriter(buf);

        writer.pushIndent();
        writer.println("<input type='hidden' name='start' value='"+
                       (req.getStartRowIndex()+1)+"'>");
        writer.println("<input type='hidden' name='range' value='"+
                       req.getRange()+"'>");
        int[] clink;
        if( req.getOrderColumnLink() !=null) {//Hawke add 'id'
            writer.println("<input type='hidden' id='orderColumns' name='order/columns' value='");
            clink=req.getOrderColumnLink();
            for( int j=0;j< clink.length;j++) {
                writer.print( (j>0?",":"")+clink[j]);
            }
            writer.print("'>");//Hawke add 'id'
            writer.println("<input type='hidden' id='orderAsc' name='order/asc' value='"+
                           (req.isAscendingOrder()?"true":"false")+"'>");
        }
        writer.popIndent();
        //writer.println("</form>");
        return buf.toString();
    }
    /**
	 * Replace variable in <param>sqlWithVariable</param> by attributes
	 * in session.
	 * Variables has format as $xxx$, such as $AD_Client_ID$, and
	 * if attribute found in session has that name, then the sql string 
	 * will be replace, sample:
	 * "select id from ad_client_id where ad_client_id in ($ad_client_id$)"
	 * will be replace to 
	 * "select id from ad_client_id where ad_client_id in (10993)"
	 * if there's $ad_client_id$=10993
	 * 
	 * @param sql
	 * @param session
	 * @return
	 */
	public static String replaceVariables(String sql, QuerySession session){
		if (session ==null) return sql;
		if( sql ==null) return null;
		//method: search sqlWithVariables one by one, when found "$",
		//check to the next "$", and try to found attribute value of
		//that, if not found, take first $ as nothing, go to next.
		StringBuffer sb=new StringBuffer();
		int p= 0,p1,p2;
		while(p < sql.length()){
			p1= sql.indexOf("$", p);
			if(p1>-1){
				//found
				p2=sql.indexOf("$", p1+1);
				if(p2>-1){
					//found second
					String n= sql.substring(p1, p2+1);
					Object v= session.getAttribute(n);
					if (v!=null) {
						//replace variable to attribute value
						sb.append(sql.substring(p, p1)).append(v);
						p=p2+1;
					}else{
						//remain the fake variable, not include last $
						sb.append(sql.substring(p, p2));
						p=p2;
					}
					
				}else{
					// not found the second $, so no variable any more
					sb.append(sql.substring(p));
					break;
				}
			}else{
				// not found the first $,so no variable any more
				sb.append(sql.substring(p));
				break;
			}
		}
		return sb.toString();
	}		
	public  static void main(String[] ds) throws Exception{
		QuerySessionImpl se=new QuerySessionImpl();
		se.setAttribute("$C_BPARTNER_NAME$", "test");
		se.setAttribute("$CCC$", "test2");
		String s=" = '$C_BPARTNER_NAME$') )$CDE$CCC$";
		System.out.println(replaceVariables(s, se));
		
		
	}
	/**
	 * For help on the limit values 
	 * @param col
	 * @return key: value desc, value: value comments
	 * @throws Exception
	 */
	public static PairTable getLimitValueComments(Column col)throws Exception{
		ResultSet rs =null;
		Connection conn=null;
		PreparedStatement pstmt= null;
		PairTable pt=new PairTable();
		try{
			conn= QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement(GET_LIMIT_VALUE_COMMENTS);
			pstmt.setInt(1, col.getId());
			rs=pstmt.executeQuery();
			while(rs.next()){
				pt.put(rs.getString(1), rs.getString(2));
			}
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception ee){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception ee){}
			if(conn!=null) try{conn.close();}catch(Exception ee){}
		}
		return pt;
	}
	public static String getComments(Column col) throws Exception{
		ResultSet rs =null;
		Connection conn=null;
		PreparedStatement pstmt= null;
		try{
			conn= QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement(GET_COLUMN_COMMENTS);
			pstmt.setInt(1, col.getId());
			rs=pstmt.executeQuery();			
			if(rs.next()){
				return rs.getString(1);
			}
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception ee){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception ee){}
			if(conn!=null) try{conn.close();}catch(Exception ee){}
		}
		return "";
	}
	public static String getComments(Table  tb) throws Exception{
		ResultSet rs =null;
		Connection conn=null;
		PreparedStatement pstmt= null;
		try{
			conn= QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement(GET_TABLE_COMMENTS);
			pstmt.setInt(1, tb.getId());
			rs=pstmt.executeQuery();			
			if(rs.next()){
				return rs.getString(1);
			}
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception ee){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception ee){}
			if(conn!=null) try{conn.close();}catch(Exception ee){}
		}
		return "";
	}
	
	/**
	 * Last comments of the record in specified table, note table should be converted
	 * to real table in database. So mutiple ad_table will share the same comments if
	 * their realtable is the same.
	 * @param realTableName should be the real table name of ad_table
	 * @param recordId
	 * @return null if none found, else Object[]{creationdate, userid, username, contents};
	 * @throws Exception
	 */
	public static Object[] getLastComments(String  realTableName, int recordId) throws Exception{
		ResultSet rs =null;
		Connection conn=null;
		PreparedStatement pstmt=null;
		Object[] s=new Object[4];
		try{
			 conn=QueryEngine.getInstance().getConnection();
			 pstmt= conn.prepareStatement(LAST_COMMENTS);
			 pstmt.setString(1, realTableName);
			 pstmt.setInt(2, recordId);
			 
			 rs=pstmt.executeQuery();
			 if(rs.next()){
			 	s[0]= rs.getObject(1);
			 	s[1]= rs.getObject(2);
			 	s[2]=rs.getObject(3);
			 	s[3]=rs.getObject(4);
			 	return s;
			 }
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception ee){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception ee){}
			if(conn!=null) try{conn.close();}catch(Exception ee){}
		}
		return null;
		
	}
	public static java.util.Date dateNumberToDate(int dn) throws java.text.ParseException {
		return ((SimpleDateFormat)dateNumberFormatter.get()).parse(String.valueOf(dn));
	}
	/**
	 * Used only for datenumber type column
	 * @param obj
	 * @param canBeNull
	 * @return
	 * @throws NDSException
	 */
	public static BigDecimal paseInputDateNumber(Object obj,boolean canBeNull)throws NDSException {
		SimpleDateFormat dateNumberFormatter2= (SimpleDateFormat) dateNumberFormatter.get();
		SimpleDateFormat inputDateFormatter2= (SimpleDateFormat) inputDateFormatter.get();
		
		java.util.Date  returnDate= new java.util.Date( System.currentTimeMillis());
		if(obj==null||"".equals(obj) ){
            if (canBeNull==true) return null;
            else 
            	return new BigDecimal(dateNumberFormatter2.format(returnDate));
        }
        String str= obj.toString().trim();
        try {
           	if(str.indexOf("/")>-1)
           		returnDate= new java.util.Date( inputDateFormatter2.parse(str).getTime());// YYYY/MM/DD
           	else
           		returnDate= new java.util.Date( dateNumberFormatter2.parse(str).getTime());// try YYYMMDD also
        } catch(java.text.ParseException e) {
            logger.error("date-format-error:"+ str+":"+ e);
        	throw new NDSException("@date-format-error@:"+ str);
        }
        return new BigDecimal(dateNumberFormatter2.format(returnDate));      
	}
	/**
     * if <param>sqlType</param> is SQLTypes.TIMESTAMP
     * First parse input as standard dateTimeSecondsFormatter.parten, if exception found,
     * Will try to fectch part in <param>str</param> in following order:
     * year(if small than 3, will add "20"), month, day, hour, minute, second 
     * 
     * day and hour must be seperated by space(s), if only one part found(no spaces in str),
     * will take as previous part(year,month,day)
     * 
     * year,month,day can be seperated by anything in ":/-";
     * hour,minute,second can be seperated by ":/-"
     * 
     * if <param>sqlType</param> is not SQLTypes.TIMESTAMP
     * First parse input as standard inputDateFormatter.parten, if error found,
     * will do as above
     * 
     * @param str 
     * @param canBeNull can the input be null, if true, will return null if input is null,
     * if false, will return current date if input is null or error
     * @param sqlType, value of Column.getSQLType()
     * @return
     * @throws NDSException
     */
     public static java.sql.Date parseInputDate(Object obj,boolean canBeNull, int sqlType) throws NDSException {
          java.sql.Date returnDate = new java.sql.Date( System.currentTimeMillis());
          if(obj==null||"".equals(obj) ){
            if (canBeNull==true) return null;
            else return returnDate;
          }
          String str= obj.toString();
          try {
              if(sqlType != SQLTypes.TIMESTAMP  ){
              	if(str.indexOf("/")>-1)
              		returnDate= new java.sql.Date( ((SimpleDateFormat)inputDateFormatter.get()).parse(str).getTime());// YYYY/MM/DD
              	else
              		returnDate= new java.sql.Date( ((SimpleDateFormat)dateNumberFormatter.get()).parse(str).getTime());// try YYYMMDD also
              }else{
              	returnDate = new java.sql.Date( ((SimpleDateFormat)dateTimeSecondsFormatter.get()).parse(str).getTime());
              }
          } catch(java.text.ParseException e) {
              logger.debug("Not a valid date format:"+ str);
              //throw new NDSEventException("日期 "+str+" 格式不对，请用‘"+ inputDateFormatter.toPattern()+" ’形");
              // try using rules
              StringTokenizer st=new StringTokenizer(str.toString(), " :/-");
              Calendar c= Calendar.getInstance();
              c.setLenient(true);
              c.setTimeInMillis(System.currentTimeMillis());
              String s;
              if(st.hasMoreTokens()){
              	//year
              	s= st.nextToken();
              	if (s.length()<3) s="20"+s;
              	c.set(Calendar.YEAR, Tools.getInt(s, c.get(Calendar.YEAR)));
              }
              if(st.hasMoreTokens()) c.set(Calendar.MONTH, Tools.getInt(st.nextToken(), c.get(Calendar.MONTH)+1)-1);
              if(st.hasMoreTokens()) c.set(Calendar.DAY_OF_MONTH, Tools.getInt(st.nextToken(), c.get(Calendar.DAY_OF_MONTH)));
              if(st.hasMoreTokens()) c.set(Calendar.HOUR_OF_DAY, Tools.getInt(st.nextToken(), c.get(Calendar.HOUR_OF_DAY)));
              if(st.hasMoreTokens()) c.set(Calendar.MINUTE, Tools.getInt(st.nextToken(), c.get(Calendar.MINUTE)));
              if(st.hasMoreTokens()) c.set(Calendar.SECOND, Tools.getInt(st.nextToken(), c.get(Calendar.SECOND)));
              returnDate= new java.sql.Date(c.getTimeInMillis());
          }
          return returnDate;
    }	
     /**
 	* get <select> </select> html tag for quick select box
 	* the select option will be item table's showable columns' column id
 	* will set AK column as the selected one
 	* @param columnList elements are Column or Any type that can be converted to int (Column.id)
 	*
 	*/
 	public static String getQuickSearchComboBox(String comboboxName,Collection columnList, Locale locale){
 		StringBuffer sb=new StringBuffer("<select class='select_quick' id='"+comboboxName+"' id='"+ comboboxName+"' name='"+ comboboxName +"'>");
 		int i=0;String value, desc;
 		TableManager manager= TableManager.getInstance();
     	Column col=null;
         for( Iterator it=columnList.iterator();it.hasNext();){
            Object o=  it.next();
            if(o instanceof Column) col=(Column)o;
            else col=  manager.getColumn(Tools.getInt(o, -1));
            
            if( col.isVirtual()==true) {
            	if(!col.isColumnLink()){
	            	continue;
            	}
            }
             if( col.getReferenceTable() !=null){
 				value= col.getId()+","+ col.getReferenceTable().getAlternateKey().getId();
 				/*desc=col.getDescription(locale)+
             		col.getReferenceTable().getAlternateKey().getDescription(locale);*/
             }else{
             	value= col.getId()+"";
             	//desc=col.getDescription(locale);
             }
             desc=col.getDescription(locale);
             sb.append("<option value='"+value +"' "+(col.isAlternateKey()?"selected":"")+ ">" +desc+ "</option>");
             i++;
         }

 		sb.append("</select>");
 		return sb.toString();
 	}     
 	/**
 	 * Some table has rowurl(column "url") set, such as u_clob, when click on it, should go to the page that 
 	 * is specified, in format NDS_PATH+table.getRowURL()+"?"
 	 	for other tables return NDS_PATH+"/object/object.jsp?table="+table.getId()
 	 * @param table
 	 * @return
 	 */
 	public static String getTableRowURL(Table table){
 		if(nds.util.Validator.isNotNull(table.getRowURL())) 
 			return WebKeys.NDS_URI +  table.getRowURL() +"?";
 		else
 			return WebKeys.NDS_URI +"/object/object.jsp?table="+table.getId();
 	}
 	/**
 	 * 
 	 * @param table
 	 * @param objPage set in object page
 	 * @return
 	 */
 	public static String getTableRowURL(Table table, boolean objPage){
 		if(nds.util.Validator.isNotNull(table.getRowURL())) 
 			return WebKeys.NDS_URI +  table.getRowURL() +"?";
 		else
 			return objPage? WebKeys.NDS_URI +"/object/object.jsp?table="+table.getId():
 				WebKeys.NDS_URI +"/object/object.jsp?table="+table.getId();
 	}
	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param day day time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp day)
	{
		return TO_DATE(day, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  columnType  Column.getType()
	 *  @param  pattern such as "9G999G990D00", "YYYY-MM-DD HH24:MI"
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	@see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *   */
	public static String TO_CHAR (String columnName, int columnType, String pattern)
	{
		if (columnName == null || pattern == null || columnName.length() == 0)
			throw new IllegalArgumentException("TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		retValue.append(",'").append(pattern).append("'");

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

	/**
     * Load session attributes for later query filter
     * @param userId user id
     * @param qid the query session id
     * @since 2.0
     */
    public static QuerySession createQuerySession(int userId, String qid, Locale locale) throws Exception{
    	QuerySessionImpl qsession=new QuerySessionImpl(qid);
    	Connection con= null;
        ResultSet rs=null, rs2=null;
        Statement pstmt=null, stmt2=null;
        TableManager tm= TableManager.getInstance();
        ((QuerySessionImpl)qsession).setLocale(locale);
        try{
            con=QueryEngine.getInstance().getConnection();
            pstmt= con.createStatement();
            rs= pstmt.executeQuery(GET_USER_ENV);
            
            String name,value, variable;
            ColumnLink cl;
            while( rs.next()){
                name= rs.getString(1);
                value= rs.getString(2);
                //reverse from other table to user
                //sample: USER.C_BPARTNER_ID.AD_ORG_ID
                //to get ad_org_id value, we should do
                //select ad_org_id from C_BPARTNER where ... user.id=$userid
                
                try{
                	cl= (new ColumnLink(value));
                }catch(Exception t){
                	throw new Exception("Could not load " + value +" for "+ name+":"+ t);
                }
                QueryRequestImpl quest=QueryEngine.getInstance().createRequest(null);
                quest.setMainTable(cl.getColumns()[0].getTable().getId());
                quest.addSelection(cl.getColumnIDs(),false, null);
                quest.addParam(cl.getColumns()[0].getTable().getPrimaryKey().getId(), userId+"");
                String sql= quest.toSQL();
                //logger.debug("Loading variable:"+name+"::"+ sql);
                stmt2=con.createStatement();
                rs2= stmt2.executeQuery(sql);
                if(rs2.next()){
                	qsession.setAttribute(name, rs2.getObject(1));
                	//logger.info("Load attribute "+ name+"="+ rs2.getObject(1));
                }else{
                	logger.error("Could not load variable for attribute name:"+ name);
                }
				rs2.close();
				stmt2.close();
            }
        }finally{
            if( rs2 !=null) try{rs2.close();}catch(Exception e){}
            if( stmt2 !=null) try{stmt2.close();}catch(Exception e2){}
        	
            if( rs !=null) try{rs.close();}catch(Exception e){}
            if( pstmt !=null) try{pstmt.close();}catch(Exception e2){}
            if( con!=null) try{con.close();}catch(Exception e3){}
        }
        return qsession;
    }	
    /**
     * User can set their query condition in HTML form. Form may have multiple tabs.
     * @param paramMap key: String, value: String[], normally from HttpServletRequest.getParameterMap()
     * @param locale Locale for expression description
     * @return Expression for query conditions set by user using query form
     * @throws QueryException
     */
 	public static Expression parseCondition(Map paramMap, Locale locale) throws QueryException{
    	int tabCount= Tools.getInt(getRequestParameter(paramMap, "tab_count"), -1);
        if(tabCount==-1)
            return parseConditionWithoutTab(paramMap, locale);
        else
            return parseConditionWithTab(paramMap, tabCount, locale);

 	}
 	
    /**
     * @param req map for request parameters, key: String, value: String[]
     * 
     * Handle new query page which support tab control, for old compatinance, use parseConditionWithoutTab
     */
    private static Expression parseConditionWithTab(Map req,  int tabCount, Locale locale)throws QueryException{
    	
    	String cs;
        String param, paramSQL;
        int tabSQLCombination;
        int[] ids;
        Expression exprAll=null, expr=null, expr2=null;
        TableManager manager=TableManager.getInstance();
        int paramCount= Tools.getInt(getRequestParameter(req,"param_count"), MAX_PARAM_COUNT);
        for(int tabIdx=0;tabIdx<tabCount;tabIdx++){
            tabSQLCombination= Tools.getInt(getRequestParameter(req, "tab"+tabIdx+"_sql_combination"), SQLCombination.SQL_AND);
            expr=null;
            for( int i=0;i<paramCount;i++) {
                expr2=null;
                param="tab"+tabIdx+"_param/"+i+"/columns";
                cs=getRequestParameter(req,param);
                if( cs ==null)
                    continue;
                ids=QueryUtils.parseIntArray(cs);
                if( ids ==null)
                    throw new QueryException("Intenal Error: can not parse '"+ param +"' to int[]");
                param="tab"+tabIdx+"_"+"param/"+i+"/value";
                cs=getRequestParameter(req,param);
                // adv query
                paramSQL=getRequestParameter(req, "tab"+tabIdx+"_"+"param/"+i+"/sql");
                if( paramSQL !=null && !paramSQL.equals("")){
                    // sql entered, in two format:
                    // 1. in ( id1, id2,...)
                    // 2. in (select table.id from xxx,xxx where xxx)
                    // will add following format:
                    // columnID  $sql

                    // ids has reference table' AK column, remove it
                    ids= removeLastElement(ids);
                    expr2=new Expression(new ColumnLink(ids), paramSQL, "("+ ( new ColumnLink(ids)).getDescription(locale) 
                    		+ MessagesHolder.getInstance().getMessage(locale,"-satisfy-")+  cs +")" ); // param contains description
                    //query.addParam(ids,  paramSQL ,"("+ ( new ColumnLink(ids)).getDescription() + " 满足 "+ cs +")" ); // param contains description
                }else{
                    if( cs !=null && !cs.equals("")) {
                        // mind that GUI may send colum of values
                        Column lastColumn= manager.getColumn(ids[ids.length-1]);
                        if(lastColumn !=null && lastColumn.getValues(locale) !=null) {
                            try{
                                if( (new Integer(cs.trim()).intValue()) ==EXCLUDE_VALUE) {
                                    continue;
                            }}catch(NumberFormatException enfe){}
                        }
                        //query.addParam(ids,cs);
                        expr2=new Expression(new ColumnLink(ids), cs, null);
                    }
                }
                if ( expr2 !=null){
                    //logger.debug("Expr2=" + expr2);
                    if(expr==null) expr=expr2;
                    else expr=new Expression(expr,expr2, SQLCombination.SQL_AND ,null);
                }
            }//end for param in one tab
            if ( expr !=null){
                if(exprAll==null) exprAll  = expr;
                else exprAll= new Expression(exprAll,expr,tabSQLCombination ,null);
            }

        }// end one tab
        //logger.debug("ExprAll=" + expr);

      //  query.addParam(exprAll);
        return exprAll;
    }
    /**
     * 
     * @param req map for request parameters, key: String, value: String[]
     * @param locale
     * @return
     * @throws QueryException
     * @throws IOException
     */
    private static Expression parseConditionWithoutTab(Map req, Locale locale)throws QueryException{
    	String cs;
        String param, paramSQL;
        int[] ids;
        TableManager manager=TableManager.getInstance();
        Expression expr=null, expr2=null;

        int paramCount= Tools.getInt(getRequestParameter(req,"param_count"), MAX_PARAM_COUNT);
        for( int i=0;i<paramCount;i++) {
            expr2=null;
            param="param/"+i+"/columns";
            cs=getRequestParameter(req,param);
            if( cs ==null)
                continue;
            ids=QueryUtils.parseIntArray(cs);
            if( ids ==null)
                throw new QueryException("Intenal Error: can not parse '"+ param +"' to int[]");
            param="param/"+i+"/value";
            cs=getRequestParameter(req,param);
            // adv query
            paramSQL=getRequestParameter(req, "param/"+i+"/sql");
            if( paramSQL !=null && !paramSQL.equals("")){
                // sql entered, in two format:
                // 1. in ( id1, id2,...)
                // 2. in (select table.id from xxx,xxx where xxx)
                // will add following format:
                // columnID  $sql

                // ids has reference table' AK column, remove it
                ids= removeLastElement(ids);
                expr2=new Expression(new ColumnLink(ids), paramSQL, "("+ ( new ColumnLink(ids)).getDescription(locale) + MessagesHolder.getInstance().getMessage(locale,"-satisfy-")+ cs +")" ); // param contains description
//                query.addParam(ids,  paramSQL ,"("+ ( new ColumnLink(ids)).getDescription() + " 满足 "+ cs +")" ); // param contains description
            }else{
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
                    //query.addParam(ids,cs);
                    expr2=new Expression(new ColumnLink(ids), cs, null);
                }
            }
            if ( expr2 !=null){
                //logger.debug("Expr2=" + expr2);
                if(expr==null) expr=expr2;
                else expr=new Expression(expr,expr2, SQLCombination.SQL_AND ,null);
            }

           }//end for
    //       query.addParam(expr);
           return expr;
    } 	
    /**
     * 
     * @param requestParamMap from HttpServletRequest.getParameterMap. key: String value: String[]
     * @param key 
     * @return null or first element in String[] for specified key
     */
    private static String getRequestParameter(Map requestParamMap, String key) {
        String[] s=(String[])requestParamMap.get(key);
        if( s !=null)
            return s[0];
        return null;
    }
    /**
     * @return remove last element of the specified array, if <param>data</param>
     *  has only one or none element, throw exception
     */
    private static int[] removeLastElement(int[] data) throws QueryException{
        if( data ==null || data.length < 2) throw new QueryException("Internal Error: Input array must has at least 2 elements.");
        int[] c=new int[data.length -1];
        System.arraycopy(data, 0, c, 0, data.length -1);
        return c;
    }
    
}
