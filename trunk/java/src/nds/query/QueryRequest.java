/******************************************************************
*
*$RCSfile: QueryRequest.java,v $ $Revision: 1.5 $ $Author: Administrator $ $Date: 2006/03/13 01:13:55 $
*
*$Log: QueryRequest.java,v $
*Revision 1.5  2006/03/13 01:13:55  Administrator
*no message
*
*Revision 1.4  2005/10/25 08:12:53  Administrator
*no message
*
*Revision 1.3  2005/08/28 00:27:04  Administrator
*no message
*
*Revision 1.2  2005/05/16 07:34:17  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.4  2003/09/29 07:37:28  yfzhu
*before removing entity beans
*
*Revision 1.3  2003/05/29 19:40:17  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/04/03 09:28:21  yfzhu
*<No Comment Entered>
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryRequest.java

package nds.query;

import java.io.Serializable;
import java.util.ArrayList;

import nds.schema.Table;

/**
 * 封装查询请求
 */
public interface QueryRequest extends Serializable {
	/**
	 * QuerySession contains the request environment such as user info
	 * @return QuerySession of that request
	 * @since 2.0
	 */
	public QuerySession getSession();
    /**
     * @return SQL string representing this query request
     * @roseuid 3B8534F101F0
     */
    public String toSQL() throws QueryException;
    /**
     * This method returns a sql only querying data of requested range.<p>
     *
     * If we take an orginal sql( can retrieved from toSQL() ), such as:<br>
     *   select field1,field2,field3…… from table where where_clause order by orderby_clause
     * <p>
     * Then this method will return string as:<br>
     *      select field1,field2,field3…… from ( <br>
     *      select rownum row_num,field1,field2,field3…… from ( <br>
     *      select field1,field2,field3…… from table where where_clause order by orderby_clause))<br>
     *      where row_num between range_start and range_end;<br>
     */
    public String toSQLWithRange() throws QueryException;
    /**
     * If we encapsulate a sql of request as:<br>
     * select t1.c1, t2.c1, t2.c2 from t1,t2 where t1.c1=?? ..<br>
     * then the count sql should be:<br>
     * select count(*) from t1,t2 where...<p>
     *
     * This is very useful for segment display of a large query result
     *
     * @return SQL that count the row that query would get
     *
     */
    public String toCountSQL() throws QueryException;

    /**
     * @param replaceVariables if true, will replace wildcard variables to QuerySession attributes
     * @return description of param conditions
     */
    public String getParamDesc(boolean replaceVariables);
    /**
     * Similiar to #toCountSQL, in such format of return string:
     *   select t1.id from t1,t2 where ...
     * This is used for sub query.
     * @param replaceVariables if true, will replace wildcard variables to QuerySession attributes
     * @return SQL that only select primarky key of main table
     * @throws QueryExpression
     */
    public String toPKIDSQL(boolean replaceVariables) throws QueryException;
    /**
     * 通过request获得result后，由哪张页面进行显示，一般result会放在
     * HttpServletRequest."result" attribute 中
     * @roseuid 3B8535170118
     */
    public String getResultHandler();

    /**
     * 当前请求是针对哪张表
     * @roseuid 3B85352403C0
     */
    public Table getMainTable();
    /**
     * 一张查询页面的最大记录数
     */
    public int getRange();
    /**
     * query 将对结果分页显示，这里返回该查询结果的显示页中，
     * 第一条记录在查询结果中的行号
     */
    public int getStartRowIndex();

    /**
     * elements: Column, including both the to-be-displayed columns and those not showing
     * @see getDisplayColumnIndices
     * @roseuid 3B8537350103
     */
    public ArrayList getAllSelectionColumns();

    /**
     * 并非所有的在Select中的列都需要显示，对于来自于从表的column，我们都会同时
     * 取出该column所在表的主键，以便引用。对于这些主键，就不需要显示，例如：
     * select order.name, a.name, a.id from order, employee a where \
     *      order.id=? and a.id=order.auditorID
     *
     * a.id虽然在select 列中，但不是要显示的内容。我们规定，所有的从表要显示的
     * 字段，其后必须跟随对于表的主键。
     * 对于上面的例子，返回值为 [0, 1]
     *
     * @roseuid 3B8537AA027E
     */
    public int[] getDisplayColumnIndices();

    /**
     * @return column name concatenated by references.
     * 举例：要显示的是定单的申请人所在的部门名称，跨越的column是：
     *  order.applierID, employee.departmentID, department.name
     *  对应的column名称分别是:申请人, 部门，名称。则合成的名称为：
     *      申请人部门名称
     *  @param showNullableIndicator if true, the not nullable column will have (*) on its desc
     */
    public String[] getDisplayColumnNames(boolean showNullableIndicator);

    public String[] getDisplayColumnNames2(boolean showNullableIndicator);

    /**
     * 在select 语句中总共有多少个column
     */
    public int getSelectionCount();
    /**
     * 返回<code>position</code>指明的select 中对应的列，position 从0开始，
     * 最大为 getSelectionCount()-1，
     * 返回的int[] 的每一项是Column.getId()
     *
     */
    public int[] getSelectionColumnLink(int position);

    public ArrayList getAllSelectionColumnLinks();
    /**
     * Just selection titles
     * @return Selection's description in order as getAllSelectionColumnLinks
     * 
     */
    public ArrayList getAllSelectionDescriptions();

    /**
     *  在<code>position</code>位置上的selection是否显示
     */
    public boolean isSelectionShowable(int position);
    /**
     * 查询参数个数
     */
    public int getParamCount();
    /**
     * 返回<code>position</code>指明的查询参数中对应的列，position 从0开始，
     * 最大为 getParamCount()-1
     * 返回的int[] 的每一项是Column.getId()
     */
    public int[] getParamColumnLink(int position);
    /**
     * 返回<code>position</code>指明的查询参数中对应的列的参数值，position 从0开始，
     * 最大为 getParamCount()-1
     * 返回的String为单纯的条件，就如同查询页面上的input输入的内容
     */
    public String getParamValue(int position);
    /**
     * 查询语句中order by部分是升序否，如果order by 语句中有多个字段，返回第一个
     */
    public boolean isAscendingOrder()  ;
    /**
     * 查询语句中order by部分的column，如果order by 语句中有多个字段，返回第一个
     * 返回的int[] 的每一项是Column.getId()
     * 扩展返回OrderColumn 里面包含所有的排序字段 而不是第一个，此为本表字段排序
     */
    public int[] getOrderColumnLink();

    /**
     * 查询语句中order by部分的column，如果order by 语句中有多个字段，返回第一个
     * 返回的int[] 的每一项是Column.getId()
     * 扩展返回OrderColumnlink 里面包含所有的排序字段 而不是第一个，此为link字段长度大于1
     */
    public int[] getOrderColumnLinks();
    
    /**
     * Similiar to #getDisplayColumnIndices, except that when
     * pk and ak are set not show, the indices will also be excluded.
     */
    public int[] getReportDisplayColumnIndices(boolean pk, boolean ak);

    /**
     * 返回当前的SQL语句，用来生成报告，结果为所有的符合条件的结果
     */
    public String getSQLForReport(boolean pk, boolean ak) throws QueryException;
    /**
     * 返回当前的SQL语句，用来生成报告，结果为当前页面的显示的数据
     */
    public String getSQLForReportWithRange(boolean pk, boolean ak) throws QueryException;

    /**
     * 如果maintable 中有字段 getSumMethod !=null, 在做查询的时候就可以调用本方法
     * 获得在查询结果全范围的统计（当页统计在QueryResult中获得)
     * 当然，在调用本方法前，最好先通过#isFullRangeSubTotalEnabled()来判断是否需要
     * @return null if not full range sql (schema decide it)
     * @throws QueryException
     */
    public String toFullRangeSubTotalSQL() throws QueryException;

    /**
     * 在QueryRequest构造的时候可以要求进行全范围的统计（在QueryRequestImpl中有
     * enableFullRangeSubTotal()方法）
     * @return
     */
    public boolean isFullRangeSubTotalEnabled();

    //--------- following added by yfzhu at 2003-08-26 to support advanced query

    /**
    * @param columnLinks 如果是xxxID，则保留后续value 不做条件解释, 否则将作为界面输入项
    *  进行部分解释。
    * @param value 对value 的解析将不仅仅是对应表的内容，而是一个查询条件句，如：
    *	多值返回  in (data1, data2, ...) data1,data2必须是ID值,
    *   查询结果返回　in ( SELECT ID FROM table2 where table2.no like '%df%') (SELECT ID FROM不变)
    * @param condition OR/AND/NOT OR/NOT AND
    */
    //public void addParam(int[] columnLinks, String value,  int condition) throws QueryException;
    /**
    *  类似于VB的方式, 他的内容与已经设置进入的其他条件是与，或、非的关系，这样配置可以简单一些
    */

    public String addParam(Expression expr) throws QueryException;
    /**
     * @return If param is set using addParam(expr), then this fucntion returns input expr,
     *   else return null
     */
    public Expression getParamExpression();
    //--------- above  added by yfzhu at 2003-08-26 to support advanced query

}
