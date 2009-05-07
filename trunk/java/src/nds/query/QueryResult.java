/******************************************************************
*
*$RCSfile: QueryResult.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/12/18 14:06:16 $
*
*$Log: QueryResult.java,v $
*Revision 1.3  2005/12/18 14:06:16  Administrator
*no message
*
*Revision 1.2  2005/03/23 17:56:01  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.3  2003/04/03 09:28:21  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/03/30 08:11:53  yfzhu
*Updated before subtotal added
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
//Source file: F:\\work2\\tmp\\nds\\query\\QueryResult.java

package nds.query;

import java.io.Serializable;
import java.util.Locale;

import org.json.*;
/**
 * 存放查询结果
 */
public interface QueryResult extends Serializable {

    /**
     * @roseuid 3B8381B00131
     */
    public QueryRequest getQueryRequest();
    /**
     * Retrieves the  number, types and properties of
     * this <code>QueryResult</code> object's columns.
     *
     * @return the description of this <code>QueryResult</code> object's columns
     * @roseuid 3B83833C0181
     */
    public QueryResultMetaData getMetaData();

    /**
     * the row count of one page. Page is defined by Query.getStartRowIndex() and
     * Query.getRange()
     *
     * @roseuid 3B8381DD00E6
     */
    public int getRowCount();
    /**
     * Total row count of the result. This is different to getRowCount when
     * result is only a subset of a query request result, in which the query request
     * has startIndex and range set to request only one page of limited information.
     */
    public int getTotalRowCount();

    /**
     * Many columns are not string type, but we provide only this method for
     * simplicity. Some columns such status(int), permissions(int) will be
     * converted to readable string before this method called.
     *
     * @param column index of column( start from 1) to return value.
     * @return decorated meaningful string
     * @throws QueryException if column index out of bound.
     * @roseuid 3B8384C60083
     */
    public String getString(int column) throws QueryException;
    /**
     * Add non breaking space (&nbsp;) if string is null or "".
     * This is useful when showing on html.
     * @param needNBSP if true, nbsp will be added to result string
     */
    public String getString(int column, boolean needNBSP) throws QueryException;
    /**
     * yfzhu add viewOnly support for print, so double data  and int data can has format as :
     * "####,##0.00"
     */
    public String getString(int column, boolean needNBSP, boolean viewOnly) throws QueryException;
    /**
     * May return null, object are rawly retrieved from ResultSet
     * @param column
     * @return
     * @since 2.0
     */
    public Object getObject(int column);

    /**
     * @return -1 if column does not represent a primary key of a table
     * @throws QueryException if column index out of bound.
     * @roseuid 3B83857F022D
     */
    public int getObjectID(int column)throws QueryException;


    /**Moves the cursor down one row from its current position. A QueryResult
     * cursor is initially positioned before the first row; the first call to
     * the method next makes the first row the current row; the second call
     * makes the second row the current row, and so on.
     *
     * If an input stream is open for the current row, a call to the method
     * next will implicitly close it. A QueryResult object's warning chain is
     * cleared when a new row is read.
     *
     * @return true if the new current row is valid; false if there are no more rows
     * @throws QueryException - if a database access error occurs
     * @roseuid 3B83855A0393
     */
    public boolean next() throws QueryException;

    /**
     * Moves the cursor to the front of this ResultSet object, just before the first row. 
     * This method has no effect if the result set contains no rows.
     *
     */
    public void beforeFirst();
    /**
     * Every result has an additional row to summerize the values in columns whose
     * sum-method is not null
     * @param column
     * @return true if column's sum-method is not null
     */
    public boolean isSubTotalEnabled(int column);

    /**
     * @param column
     * @return QueryRequest.isFullRangeSubTotalEnabled() && isSubTotalEnabled(int column)
     */
    public boolean isFullRangeSubTotalEnabled(int column);

    /**
     * Get subtotal value( double ) in String, return "" or "&nbsp;" if this
     * column has no subtotal value
     * @param column
     * @return double value's string presentation
     */
    public String getSubTotalValue(int column, boolean needNBSP);
    /**
     * Get full range subtotal value in String, must check existance of
     * value using isFullRangeSubTotalEnabled
     */
    public String getFullRangeSubTotal(int column, boolean needNBSP);
    /**
     * Get alternate key's value of current row
     * @return null if ak not found in the display columns
     * @throws QueryException
     * @since 2.0
     */
    public Object getAKValue()throws QueryException;
    
    /**
     * Convert to json matrix array, each row is JSONArray 
     * @param  withData with data or not (only meta info such as row count)
     * @return 
     */
    public JSONObject toJSONObject(boolean withData) throws JSONException;
    /**
     * Convert to json matrix array, each row is JSONArray, with data 
     * @return 
     */
    public JSONObject toJSONObject() throws JSONException;
}
