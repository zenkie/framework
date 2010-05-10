/******************************************************************
*
*$RCSfile: QueryRequestImpl.java,v $ $Revision: 1.6 $ $Author: Administrator $ $Date: 2006/03/13 01:13:55 $
*
*$Log: QueryRequestImpl.java,v $
*Revision 1.6  2006/03/13 01:13:55  Administrator
*no message
*
*Revision 1.5  2005/10/25 08:12:53  Administrator
*no message
*
*Revision 1.4  2005/08/28 00:27:04  Administrator
*no message
*
*Revision 1.3  2005/05/16 07:34:17  Administrator
*no message
*
*Revision 1.2  2005/03/30 13:13:57  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.6  2003/09/29 07:37:28  yfzhu
*before removing entity beans
*
*Revision 1.5  2003/08/17 14:25:14  yfzhu
*before adv security
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryRequestImpl.java

package nds.query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import nds.schema.*;
import nds.util.*;
import java.util.*;

import nds.log.*;

/**
 * 根据客户在页面上的输入构造查询请求。这个类将使用TableManager完成SQL语句的创建。
 */
public abstract class QueryRequestImpl implements QueryRequest {
	protected transient Logger logger= 
		 LoggerManager.getInstance().getLogger(this.getClass().getName());
	/**
	 * session contains environment attributes
	 */
	protected QuerySession session;
	protected transient Table mainTable;
	
	public QuerySession getSession(){
		return session;
	}
	public void setSession(QuerySession s){ session=s;}
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
	 * @return
	 */
	protected String replaceVariables(String sql){
		return QueryUtils.replaceVariables(sql,session);
	}
	/**
	 * Add param directly to where clause
	 * @param filter
	 * @throws QueryException
	 */
	public abstract void addParam(String filter)throws QueryException ;
    /**
     * 查询条件，将构成SQL 中 WHERE 子句，这里对应的是在二级表（从表）上设条件
     * @param mainTableColumnID the column in main table
     * @param refTableColumnID the reference table column that query parameter
     * is set on, normally will has primary key(id) equals to <code>mainTableColumnID</code>
     * @param value the query parameter, see QueryUtils.toSQLClause() for more details
     * @throws QueryException if <code>mainTableColumnID</code> is not in mainTable
     * @roseuid 3B8231AD0299
     */
    public abstract void addParam(int mainTableColumnID, int refTableColumnID, String value)throws QueryException ;

    public abstract void addParam(int mainTableColumnID, String value, String desc) throws QueryException ;

    public  abstract void addParam(int mainTableColumnID, String value) throws QueryException;

    /**
    * @param columnLinks 如果是xxxID，则保留后续value 不做条件解释, 否则将作为界面输入项
    *  进行部分解释。
    * @param value 对value 的解析将不仅仅是对应表的内容，而是一个查询条件句，如：
    *	多值返回  in (data1, data2, ...) data1,data2必须是ID值,
    *   查询结果返回　in ( SELECT ID FROM table2 where table2.no like '%df%') (SELECT ID FROM不变)
    * @param condition OR/AND/NOT OR/NOT AND
    */
    //public abstract void addParam(int[] columnLinks, String value,  int condition) throws QueryException;

    public abstract Expression getParamExpression();

    /**
    *  类似于VB的方式, 他的内容与已经设置进入的其他条件是"与"的关系，这样配置可以简单一些
    *  目前支持此方法的多次调用，则getParamExpression()将返回的是他们的交集
    *  @return where clause 仅包含设置了条件的部分，像多表左链接的语句未包括
    */
    public  abstract String addParam(Expression expr) throws QueryException;

    public  abstract void addParam(int[] columnLinks, String value)  throws QueryException ;

    public  abstract void addParam(int[] columnLinks, String value, String desc) throws QueryException ;

    /**
     * Add selection item to be retrieved, the item is from secendary table,
     * with primary key referred by primary table row. In this condition, not
     * only the very item should be selected, but also do the primary table column, if
     * <code>showAK</code> is set to true.
     *
     * 这里需要指出的是，对于要显示的字段来自从表的情况，如果<code>showAK</code>为true，
     * 则主表对应引用字段也要被选出，( 作为显示字段的hyperlink显示在页面上)，以方
     * 便用户查看该字段对应对象的其他内容。在这种情况下，定义主表字段紧随从表字段之后。
     *
     * @param mainTableColumnID column of main table, note this should also be
     * selected but displayed only as a link on reTableColumn.
     * @param refTableColumnID the column to be selected to show
     * @param showAK If true, the reference table primary key will also be added to
     *      selection, and will be ordered next to <code>refTableColumnID</code>
     * @roseuid 3B830A780298
     */
    public  abstract void addSelection(int mainTableColumnID, int refTableColumnID, boolean showAK)
    throws QueryException;
    /**
     * Add selection item to be retrieved, each item is from the table that previous
     * item referred as reference table. The item before last item should also be
     * selected out( and will be displayed as link one page) if <code>showAK</code>
     * is set to <code>true</code>
     *
     * @param columnLinks column IDs in order of mainTable, refTable, refTable's refTable...
     * @param showAK if set to true, the column's PK will also be selected out.
     * @param title the head title for selection item.
     * @roseuid 3B830A780298
     */
    public  abstract void addSelection(int[] columnLinks, boolean showAK, String title)throws QueryException;

    /**
     * This is to add a none-defined column into selection.
     * It's used by nds.cxtab.CxtabReport solely.
     * @param selectItem
     * @param desc
     * @throws QueryException
     */
    public abstract void addSelection(String selectItem, String desc) throws QueryException;
    /**
     * Add column to be selected
     * @param mainTableColumnID the column of the main table, for columns of
     * referenct table, refers to addSelection(int,int, int)
     * @roseuid 3B830AC70129
     */
    public  abstract void addSelection(int mainTableColumnID) throws QueryException;
    /**
     * 
     * @param tableID
     * @param includeFilter true if need to include filter in the where clause,
     * use false only when copy records from other tables, which have the same
     * real table name as this table.
     * @throws QueryException
     * @see nds.control.ejb.command.CopyTo
     * @since 2.0
     */
    public void setMainTable(int tableID, boolean includeFilter)throws QueryException{
    	setMainTable(tableID, includeFilter, null);
    }
    /**
     * equal to setMainTable(tableID, true)
     */
    public void setMainTable(int tableID)throws QueryException {
    	setMainTable(tableID, true,null);
    }
    /**
     * 
     * @param tableID
     * @param includeFilter
     * @param additionalFilter as another filter set on ad_table.filer
     * @throws QueryException
     */
    public abstract void setMainTable(int tableID, boolean includeFilter, String additionalFilter)throws QueryException ; 
    /**
     * @roseuid 3B8309EA0135
     */
    public  abstract void setOrderBy(int[] cols, boolean ascending) throws QueryException;
    /**
     * You can use this one to replace setOrderBy
     * @param cols
     * @param ascending
     * @throws QueryException
     */
    public  abstract void addOrderBy(int[] cols, boolean ascending) throws QueryException;

    /**
     * 查找范围，当找到数据列表后，取出从startIdx(包括0)开始，最多range条的记录。
     * @param startIdx start from 0
     * @param range size of rows selected
     */
    public  abstract void setRange(int startIdx, int range) ;

    /**
     * 由谁(jsp or servlet)处理查询结果
     * getServletConfig().getServletContext().getRequestDispatcher(url).forward(req, resp);
     * @param url the handler of QueryResult to be generated. If null, use
     * REFERENCE attribute of Request.
     * @roseuid 3B84C8C70060
     */
    public  abstract void setResultHandler(String url) ;

    /**
     * 有些select语句的结构很复杂，我们考虑直接写SQL的方式
     * @roseuid 3B86EC9E0315
     */
    public abstract void setSQL(String sql);

    /**
     * @param replaceVariables if true, will replace wildcard variables to QuerySession attributes
     * @return description of param conditions
     */
    public abstract String getParamDesc(boolean replaceVariables);

    /**
     * Similiar to #toCountSQL, in such format of return string:
     *   select t1.id from t1,t2 where ...
     * This is used for sub query.
     * @param replaceVariables if true, will replace wildcard variables to QuerySession attributes
     * @return SQL that only select primarky key of main table
     * @throws QueryExpression
     */
    public  abstract String toPKIDSQL(boolean replaceVariables) throws QueryException;

    public  abstract String toCountSQL() throws QueryException;

    /** This is a sample converting a normal sql string to sql with range:
     *
     *  original sql:
     *
     *  SELECT Customer.NO b0,Customer.NAME b1,a0.CUSTOMERSORTDETAIL b2,a0.ID b3,a1.NO b4,a1.ID b5,Customer.BEGINDATE b6,Customer.ENDDATE b7,Customer.STORENO b8,Customer.COUNTRY b9,Customer.PROVINCE b10,Customer.CITY b11,Customer.ADDRESS b12,Customer.POSTCODE b13,Customer.LINKMAN b14,Customer.LINKMANDEPT b15,Customer.POSITION b16,Customer.OFFICEPHONE b17,Customer.OFFICEFAX b18,Customer.HOMEPHONE b19,Customer.MOBILE b20,Customer.REGISTERBANK b21,Customer.TAXNO b22,Customer.BANKNO b23,Customer.SAVAMT b24,a2.NAME b25,a2.ID b26,a3.NAME b27,a3.ID b28,Customer.CREATIONDATE b29,Customer.MODIFIEDDATE b30,Customer.PERMISSION b31
     *   FROM Customer,Department a1,Users a3,CustomerSort a0,Users a2 WHERE (a0.ID (+)=Customer.CUSTOMERSORTID) AND (a1.ID (+)=Customer.DEPARTMENTID) AND (a2.ID (+)=Customer.OWNERID) AND (a3.ID (+)=Customer.MODIFIERID) AND ( (Customer.ID=0) )
     *
     *  converted to:
     *
     *   SELECT b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11,b12,b13,b14,b15,b16,b17,b18,b19,b20,b21,b22,b23,b24,b25,b26,b27,b28,b29,b30,b31
     *   FROM ( SELECT ROWNUM row_num, b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11,b12,b13,b14,b15,b16,b17,b18,b19,b20,b21,b22,b23,b24,b25,b26,b27,b28,b29,b30,b31 FROM (
     *   SELECT Customer.NO b0,Customer.NAME b1,a0.CUSTOMERSORTDETAIL b2,a0.ID b3,a1.NO b4,a1.ID b5,Customer.BEGINDATE b6,Customer.ENDDATE b7,Customer.STORENO b8,Customer.COUNTRY b9,Customer.PROVINCE b10,Customer.CITY b11,Customer.ADDRESS b12,Customer.POSTCODE b13,Customer.LINKMAN b14,Customer.LINKMANDEPT b15,Customer.POSITION b16,Customer.OFFICEPHONE b17,Customer.OFFICEFAX b18,Customer.HOMEPHONE b19,Customer.MOBILE b20,Customer.REGISTERBANK b21,Customer.TAXNO b22,Customer.BANKNO b23,Customer.SAVAMT b24,a2.NAME b25,a2.ID b26,a3.NAME b27,a3.ID b28,Customer.CREATIONDATE b29,Customer.MODIFIEDDATE b30,Customer.PERMISSION b31
     *   FROM Customer,Department a1,Users a3,CustomerSort a0,Users a2 WHERE (a0.ID (+)=Customer.CUSTOMERSORTID) AND (a1.ID (+)=Customer.DEPARTMENTID) AND (a2.ID (+)=Customer.OWNERID) AND (a3.ID (+)=Customer.MODIFIERID) AND ( (Customer.ID=0) ) ))
     *   WHERE row_num BETWEEN 0 AND 5
     */
    public  abstract String toSQLWithRange()throws QueryException ;
    /**
     * This first column must be PK of the main table, which will not count in display columns
     * @roseuid 3B8AFCFB0083
     */
    public  abstract String toSQL() throws QueryException ;

    /**
     * @roseuid 3B8AFCFB00D3
     */
    public  abstract String getResultHandler() ;


    /**
     * Construct Request querying all data in main table with all columns. If that column
     * is a reference table column, replace with that table's AK.
     * Default will include all columns that showable, equals to call 
     * addAllShowableColumnsToSelection(action, true)
     * @param action, can be one of Column.ADD, Column.MODIFY, Column.QUERY_LIST
     * @see Column#ADD, Column#MODIFY, Column#QUERY
     */
    public void addAllShowableColumnsToSelection(int action)throws QueryException {
        addAllShowableColumnsToSelection(action,true);
    }
    /**
     * 
     * Add columns which has any of the bit masks set in specified positions.
     * For instance, getColumns([0,3]) will return columns which
     * is showable when creation form <b>OR</b> modifiable in update form.
     * refer to Column.isMaskSet for mask information. 
     * @param columnMasks elements shoule be 0-9
     * @param includeUIController if false, will not add column that getDisplaySetting().isUIController()==true
     * @throws QueryException
     */
    public void addColumnsToSelection(int columnMasks[], boolean includeUIController )throws QueryException {
    	addColumnsToSelection(columnMasks,includeUIController, -1 );	 
    }
    /**
     * 
     * @param columnMasks
     * @param includeUIController
     * @param maxSelectionColumnCount at most how many columns should be selected, -1 or 0 for no limit
     * @throws QueryException
     */
    public void addColumnsToSelection(int columnMasks[], boolean includeUIController, int maxSelectionColumnCount )throws QueryException {
    	if( mainTable ==null)
            throw new QueryException("MainTable must be set before calling this");
    	if(columnMasks==null || columnMasks.length==0) return;
    	 ArrayList columns=mainTable.getColumns(columnMasks,includeUIController, 
    			 (session==null?0:session.getSecurityGrade()) );
    	 for(int i=0;i<columns.size() ;i++) {
            Column col= (Column) columns.get(i);
            if( col.getReferenceTable() !=null) {
                Column col2=col.getReferenceTable().getAlternateKey();
                this.addSelection(col.getId(),col2.getId(),true);
            } else {
                this.addSelection(col.getId());
            }
            if(maxSelectionColumnCount>0 && i>= (maxSelectionColumnCount-1)) break;
        }    	 
    }    
    /**
     * 
     * @param action
     * @param includeUIController if false, will not add column that getDisplaySetting().isUIController()==true 
     * @throws QueryException
     */
    public void addAllShowableColumnsToSelection(int action, boolean includeUIController) throws QueryException {
        if( mainTable ==null)
            throw new QueryException("MainTable must be set before calling this");
        ArrayList columns=mainTable.getShowableColumns(action);
        for(int i=0;i<columns.size();i++) {
            Column col= (Column) columns.get(i);
            if(col.getDisplaySetting().isUIController() && !includeUIController) continue;
                if( col.getReferenceTable() !=null) {
                    Column col2=col.getReferenceTable().getAlternateKey();
                    this.addSelection(col.getId(),col2.getId(),true);
                } else {
                    this.addSelection(col.getId());
                }
        }
    }
    /**
     * All modifiable columns
     * @param action
     * @throws QueryException
     */
    public  void addAllModifiableColumnsToSelection(int action) throws QueryException {
        if( mainTable ==null)
            throw new QueryException("MainTable must be set before calling this");
        ArrayList columns=mainTable.getModifiableColumns(action);
        addColumnsToSelection(columns, false);
    	
    }    
    public void addColumnsToSelection(ArrayList columns, boolean includeUIController) throws QueryException {
    	for(int i=0;i<columns.size();i++) {
            Column col= (Column) columns.get(i);
            if(col.getDisplaySetting().isUIController() && !includeUIController) continue;
                if( col.getReferenceTable() !=null) {
                    Column col2=col.getReferenceTable().getAlternateKey();
                    this.addSelection(col.getId(),col2.getId(),true);
                    /*if(needAk2){
                        Column col3=col.getReferenceTable().getAlternateKey2();
                        if(col3 != null)
                            this.addSelection(col.getId(),col3.getId(),true);
                    }*/
                } else {
                    this.addSelection(col.getId());
                }
        }    	
    }

    /**
     * Wrapper this request to a string so can be stored in HTML page, note it's base64 encoded
     * @see QueryRequestImpl.toQueryRequest
     */
    public  abstract String toStorageString()throws IOException;
    /**
     * reverse to string to a QueryRequestImpl, note this string must be generated by
     * QueryRequestImpl.toStorageString
     */
    public static QueryRequestImpl toQueryRequest(String content)throws IOException,ClassNotFoundException {
        byte[] data=Base64.decode(content.toCharArray());
        ByteArrayInputStream bais=new ByteArrayInputStream(data);
        ObjectInputStream ois= new ObjectInputStream(bais);
        Object obj=ois.readObject();
        return (QueryRequestImpl)obj;
    }
    public  abstract int getSelectionCount() ;
    public  abstract int[] getSelectionColumnLink(int position);

    public  abstract boolean isSelectionShowable(int position) ;
    public  abstract int getParamCount();
    /**
     * Similiar to #getDisplayColumnIndices, except that when
     * pk and ak are set not show, the indices will also be excluded.
     */
    public  abstract  int[] getReportDisplayColumnIndices(boolean pk, boolean ak);
    /**
     * 提取用来制作报告（report）的SQL语句,Hawke
     */
    public  abstract String getSQLForReport(boolean pk, boolean ak) throws QueryException ;
    /**
     * 提取用来制作报告（report）的SQL语句,Hawke
     */
    public  abstract String getSQLForReportWithRange(boolean pk,boolean ak) throws QueryException ;
    /**
     * 如果maintable 中有字段 getSumMethod !=null, 在做查询的时候就可以调用本方法
     * 获得在查询结果全范围的统计（当页统计在QueryResult中获得)
     * 当然，在调用本方法前，最好先通过#isFullRangeSubTotalEnabled()来判断是否需要
     * @return null if not full range sql (schema decide it)
     * @throws QueryException
     */
    public  abstract String toFullRangeSubTotalSQL() throws QueryException;

    /**
     * 在QueryRequest构造的时候可以要求进行全范围的统计（在QueryRequestImpl中有
     * enableFullRangeSubTotal()方法）
     * @return
     */
    public  abstract boolean isFullRangeSubTotalEnabled();
    public  abstract  void enableFullRangeSubTotal(boolean  b);
    
    /**
     * To group by sql 
     * @return all selection columns will be set as category column, so the return sql
     * will be like:
     *   select <selections>, <facts> from <maintable>, <fkTable> where <filter> group by <selections>
     * @throws QueryException
     */
    public String toGroupBySQL(List facts) throws QueryException{

    	throw new QueryException("not implimented");
    }
}
