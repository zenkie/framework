/******************************************************************
*
*$RCSfile: QueryResultImpl.java,v $ $Revision: 1.5 $ $Author: Administrator $ $Date: 2005/12/18 14:06:16 $
*
*$Log: QueryResultImpl.java,v $
*Revision 1.5  2005/12/18 14:06:16  Administrator
*no message
*
*Revision 1.4  2005/11/16 02:57:21  Administrator
*no message
*
*Revision 1.3  2005/05/27 05:01:49  Administrator
*no message
*
*Revision 1.2  2005/03/23 17:56:02  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:23  Administrator
*init
*
*Revision 1.6  2004/02/02 10:42:54  yfzhu
*<No Comment Entered>
*
*Revision 1.5  2003/05/29 19:40:18  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/04/03 09:28:21  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/03/30 08:11:53  yfzhu
*Updated before subtotal added
*
*Revision 1.2  2002/12/17 08:45:37  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.1.1.1  2002/01/08 03:40:23  Administrator
*My new CVS module.
*
*Revision 1.8  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.7  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.6  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.5  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.4  2001/11/14 23:31:27  yfzhu
*no message
*
*Revision 1.3  2001/11/11 12:45:39  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryResultImpl.java

package nds.query;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import org.json.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.SQLTypes;
import nds.schema.SumMethodFactory;
import nds.schema.TableManager;
import nds.util.CollectionValueHashtable;
import nds.util.StringUtils;
import nds.util.Tools;
/**
 */
public class QueryResultImpl implements QueryResult , JSONString{
    private static Logger logger= LoggerManager.getInstance().getLogger(QueryResultImpl.class.getName());

    protected int cursor;
    // element ArrayList( size is according to resultSet.getMetaData().getColumnCount())
    protected ArrayList rows;
    protected QueryResultMetaDataImpl meta;
    protected QueryRequest request;
    protected int[] displayColumnIndices;// just QueryRequest.getDisplayColumnIndices(), note it starts from 0
    protected int totalRowCount;// total row count including those not fetched
    
    protected ArrayList fullRangeRowData; // full range row data
      
    protected TableManager manager;
    protected QueryResultImpl(){}
    /**
     *@param totalRowCount the total row count including those not fetched
     *@param resultWithRange if true, we take result set as having retrieved only part of whole data from
     *      database( as range and startIndex in <code>req</code>.
     *
     * @see QueryRequestImpl#toSQLWithRange
     * @roseuid 3B854142038E
     */
    public QueryResultImpl(ResultSet rs, QueryRequest req, int totalRowCount, boolean resultWithRange)throws SQLException {
        manager= TableManager.getInstance();
        this.totalRowCount=totalRowCount;
        meta=new QueryResultMetaDataImpl(req);
        request=req;

        displayColumnIndices=req.getDisplayColumnIndices();
        rows=new ArrayList();
        /*Will take only a subset of records from resultset. QueryRequest designate
          which part of result should be retrieved out(see QueryRequest.getRange()
          and QueryRequest.getStartRowIndex()*/
        int startIndex=resultWithRange?0: req.getStartRowIndex();
        int range= req.getRange();
        //Move to start of index
        if( startIndex >0)
            rs.absolute(startIndex);
        else if(startIndex<0){
        	rs.absolute( (totalRowCount-range<0? 0:totalRowCount-range ));
        }
        /*for (int i=0; i<startIndex; i++) {
            rs.next();
    }*/
        //Now read in desired number of results
        for (int i=0; i<range; i++) {
            if (rs.next()) {
                // generate result
                rows.add(getRow( rs));
            } else {
                break;
            }
        }
        cursor=-1;
        prepareFullRangeSubTotal(req);
        //updateRequestStartIndex();
    }
    public QueryResultImpl(ResultSet rs, QueryRequest req, int totalRowCount)throws SQLException {
        manager= TableManager.getInstance();
        this.totalRowCount=totalRowCount;
        meta=new QueryResultMetaDataImpl(req);
        request=req;

        displayColumnIndices=req.getDisplayColumnIndices();
        rows=new ArrayList();

        int startIndex=0;

        //Now read in desired number of results
        for (int i=0; i<totalRowCount; i++) {
            if (rs.next()) {
                // generate result
                rows.add(getRow( rs));
            } else {
                break;
            }
        }
        cursor=-1;
        prepareFullRangeSubTotal(req);
        //updateRequestStartIndex();
    }
    /**
     *   if start index <0, meaning last page, we'll change 
     */   
    private void updateRequestStartIndex(){
    	
    	int startIdx=this.getQueryRequest().getStartRowIndex();
    	if(startIdx<0){
    		startIdx=this.getTotalRowCount() - this.getRowCount();
    		if(startIdx<0) startIdx=0;
    	}
    	((QueryRequestImpl)this.getQueryRequest()).setRange(startIdx, this.getQueryRequest().getRange());
    }
    /**
     * Additional message that will describe this result. 
     * When result is dummy, this message can be error information
     * @return null if no addional message
     */
    protected String getAdditionalMessage(){ return null;}
    /**
     * Will contain data
     */
    public String toJSONString() {
    	try{
    		JSONObject o= toJSONObject(true);
    		return o.toString();
    	}catch(Exception t){
    		logger.error("Fail to convert to json", t);
    		throw new Error("Interal error:"+t);        
    	}
    }
    
    
    public JSONObject toJSONObject() throws JSONException{
    	return toJSONObject(true);
    }
    /**
     * Convert to json matrix array, each row is JSONArray 
     * @return if has "alerts" property, it will be a json object, which contains
     *  rows and their css classes, row id are indexed like "tr_*", e.g. tr_1 is for
     *  first row of result (start from 1) 
     */
    public JSONObject toJSONObject(boolean withData) throws JSONException{
    	JSONObject jo=new JSONObject();
    	jo.put("start", this.getQueryRequest().getStartRowIndex());
    	jo.put("totalRowCount", this.totalRowCount);
    	jo.put("rowCount", getRowCount());
    	jo.put("queryDesc", this.request.getParamDesc(true));
    	jo.put("message", getAdditionalMessage());
    	
    	if(withData){
    		jo.put("rows", nds.util.JSONUtils.toJSONArray(rows));
    	}
    	// construct subtotal row
    	jo.put("subtotalRow", nds.util.JSONUtils.toJSONArray(getSubtotalRow()));
    	boolean isFullRangeSubTotalEnabled=request.isFullRangeSubTotalEnabled();
    	jo.put("isFullRangeSubTotalEnabled", isFullRangeSubTotalEnabled );
    	if(isFullRangeSubTotalEnabled){
    		jo.put("fullRangeSubTotalRow",nds.util.JSONUtils.toJSONArray(fullRangeRowData));	
    	}
    	/*
    	 * "alerts" will be set only for special page request
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
			}	    	 */
    	return jo;
    }
      
    /**
     * Get full range subtotal value in String, must check existance of
     * value using isFullRangeSubTotalEnabled
     */
    public String getFullRangeSubTotal(int column, boolean needNBSP){
        if( isFullRangeSubTotalEnabled(column)){
            ArrayList al=new ArrayList();
            if(column  <1 || column  > displayColumnIndices.length) {
                logger.debug( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
                if( needNBSP ){
                    return "&nbsp;";
                }else return "";
            }
            int idInRowItems=displayColumnIndices[column -1];
            return (String)fullRangeRowData.get(idInRowItems);
        }else{
            return needNBSP?"&nbsp;":"";
        }

    }    
    
    private void prepareFullRangeSubTotal(QueryRequest req) throws SQLException{
        if ( !req.isFullRangeSubTotalEnabled()) return;
        fullRangeRowData=new ArrayList();
        ResultSet rs=null; double d;
        try {
            String sql= req.toFullRangeSubTotalSQL() ;
            rs=QueryEngine.getInstance().doQuery(sql);
            if( rs.next() ){
                ResultSetMetaData mt= rs.getMetaData();
                for( int i=1;i<= mt.getColumnCount();i++) {
                    d=rs.getDouble(i);
                    if ( rs.wasNull() ) fullRangeRowData.add(null);
                    else fullRangeRowData.add(((java.text.DecimalFormat)QueryUtils.floatPrintFormatter.get()).format(d));
                }
            }
        }
        catch (QueryException ex) {
            logger.error("Error", ex);
        }finally{
            if( rs !=null){try{ rs.close();}catch(Exception ee){}}
        }

    }   
    /**
     * Update result set data. 
     * @param row start from 0
     * @param col start from 0
     * @param data new value for that cell
     */
    public void updateCell(int row, int col, Object data){
    	((ArrayList)rows.get(row)).set(col,data);
    }
    
    /**
     * Get current row of result set, the first row is definitely the id of main table
     * from 2.0, the array may contain various data type, not simple String
     */
    private ArrayList getRow(ResultSet rs) throws SQLException {
        String nbsp="";
        ArrayList rowItems=new ArrayList();
        ResultSetMetaData mt= rs.getMetaData();

        Object s;
        for( int i=1;i<= mt.getColumnCount();i++) {
        	s=rs.getObject(i);
        	//trick for oracle clob type, if use getObject, it will return Clob Object, whose toString()
        	//will display as oracle.sql.CLOB@3a084d
        	//if use getString directy in result set, it will return string if we set 
        	// "SetBigStringTryClob" to true in connection property
        	if(s instanceof java.sql.Clob) {
        		s=((java.sql.Clob)s).getSubString(1, (int) ((java.sql.Clob)s).length());
        	}
            
/// yfzhu marked up following lines since 2.0, will checked later when using getString()
/// or getOjbect()  
/*          String s;
 			int type=SQLTypes.convertToSQLType( mt.getColumnType(i));
            switch(type) {
                    case SQLTypes.DECIMAL:
                    case SQLTypes.NUMERIC:
                    BigDecimal vb=rs.getBigDecimal(i);
                    if( vb==null) s="";
                    else {
                        s=""+vb;
                    }
                    break;
                    case SQLTypes.BIGINT:
                    case SQLTypes.INT:
                    case SQLTypes.SMALLINT:
                    case SQLTypes.TINYINT:
                    	int intValue=rs.getInt(i);
                    // check for Range limited column,*defer to when getString(column)
                    // @see Column.isValueLimited(), Column.getValueInterpeter()
                    	s=""+intValue;
                    break;
                    case SQLTypes.FLOAT:
                    try {
                        s=((java.text.DecimalFormat)QueryUtils.floatFormatter.get()).format(rs.getFloat(i));
                        //logger.debug("getting float: "+rs.getFloat(i)+",converting to :"+s );
                    } catch(Exception e) {
                        s=nbsp;
                    }
                    break;
                    case SQLTypes.REAL:
                    case SQLTypes.DOUBLE:
                    try {
                        s=((java.text.DecimalFormat)QueryUtils.floatFormatter.get()).format(rs.getDouble(i));
                        //logger.debug("getting double: "+rs.getDouble(i)+",converting to :"+s );
                    } catch(Exception e) {
                        s=nbsp;
                    }

                    break;
                    case SQLTypes.TIME:
                    case SQLTypes.TIMESTAMP:
                    
                    try {
                        s=((java.text.SimpleDateFormat)QueryUtils.timeFormatter.get()).format(rs.getTimestamp(i));
                    } catch(Exception e) {
                        s=nbsp;
                    }
                    case SQLTypes.DATE:
                    try {
                        s=((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format(rs.getDate(i));
                    } catch(Exception e) {
                        s=nbsp;
                    }
                    break;
                    case SQLTypes.VARCHAR:
                    case SQLTypes.LONGVARCHAR:
                    case SQLTypes.CHAR:
                    s=rs.getString(i);
                    if( s ==null)
                        s="";
                    break;
                    default:
                    throw new SQLException("Unexpected column type:"+type);
            }//end switch
*/            
            rowItems.add(s);
        }// end for columns
        return rowItems;
    }

    /**
     * @roseuid 3B8AFCFB037C
     */
    public QueryRequest getQueryRequest() {
        return request;
    }

    /**
     * @roseuid 3B8AFCFB039A
     */
    public QueryResultMetaData getMetaData() {
        return meta;
    }

    /**
     * @roseuid 3B8AFCFB03B8
     */
    public int getRowCount() {
        return rows.size();
    }
    public int getTotalRowCount() {
        return totalRowCount;
    }
    /**
     * No matter what real type it is, return String
     * @roseuid 3B8AFCFB03D6
     */
    public String getString(int column, boolean needNBSP) throws QueryException {
        return getString( column, needNBSP, false);
    }
    /**
     * May return null, object are rawly retrieved from ResultSet
     * @param column start from 1
     * @return
     */
    public Object getObject(int column){
        if(column  <1 || column  > displayColumnIndices.length) {
            //nmdemo bugs found
            logger.error( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
            return null;
            //throw new QueryException( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
        }
        int idInRowItems=displayColumnIndices[column -1];
        return ((ArrayList)rows.get(cursor)).get(idInRowItems);
    }
    /**
     * yfzhu add viewOnly support for print, so double data  and int data can has format as :
     * "####,##0.00"
     */
    public String getString(int column, boolean needNBSP, boolean viewOnly) throws QueryException{
        if(column  <1 || column  > displayColumnIndices.length) {
            //nmdemo bugs found
            logger.debug( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
            if( needNBSP ){
                return StringUtils.NBSP;
            }else return "";
            //throw new QueryException( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
        }
        int idInRowItems=displayColumnIndices[column -1];
        Column col= manager.getColumn(meta.getColumnId(column));
        
        Object value=((ArrayList)rows.get(cursor)).get(idInRowItems);
        String ret=null;
        if(value!=null){
        	try{
	        	if( col.getType()== Column.DATENUMBER){
	        		ret= value.toString();
	        	}else{
	        		int sqlType=col.getSQLType();
		        	if ( sqlType == SQLTypes.DOUBLE ||sqlType==SQLTypes.FLOAT ){
		        		ret= QueryUtils.getDecimalFormat(col.getScale()).format((BigDecimal)value);
		        	}else if( sqlType==SQLTypes.DATE){
		        		ret=((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format((java.util.Date) value);
		        	}else if( sqlType==SQLTypes.TIMESTAMP || sqlType==SQLTypes.TIME ){
		        		ret=((java.text.SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get()).format((java.util.Date) value);
		        	}else{
		        		ret= value.toString();
		        	}
	        	}
        	}catch(Exception e){
        		logger.error("Fouind error when get value: " + value + " class:" + value.getClass()+":" +e, e);
        		throw new QueryException(e.getMessage(), e);
        	}
        }else{
        	ret="";
        } 
        //String ret=(String)((ArrayList)rows.get(cursor)).get(idInRowItems);
        if( col.isValueLimited() || col.getValueInterpeter() !=null) {
            Locale locale;
            if(request!=null&& request.getSession()!=null ) locale= request.getSession().getLocale();
            else locale= TableManager.getInstance().getDefaultLocale();
        	ret=manager.getColumnValueDescription(col.getId(), ret,locale);
            // yfzhu 2005-03-01 add direct return so interpreters such as
            // URLInterpreter generated value will not be escaped from html tag
            if( needNBSP && (ret ==null || ret.trim().equals(""))){
                ret= StringUtils.NBSP;
            }
            return ret;
        }else if (viewOnly){
            try{
            // added by yfzhu at 2003-02-22 for print support
            if( (col.getType() == Column.NUMBER)
            			&& ret!=null && ret.length() > 0 ) {
                if ( col.getSQLType() == SQLTypes.DOUBLE || col.getSQLType()==SQLTypes.FLOAT )
                    ret= ((java.text.DecimalFormat)QueryUtils.floatPrintFormatter.get()).format(new Double(ret));
                else ret= ((java.text.DecimalFormat)QueryUtils.intPrintFormatter.get()).format(new Long(ret));
            }
            }catch(Exception eee){
                logger.debug("Could not convert ret:" + ret);
            }
        }else{
            try{
            // added by yfzhu at 2003-04-04 for ratio error indication
                if( needNBSP &&   (col.getType() == Column.NUMBER )
                		&& ret!=null && ret.length() > 0 ) {
                    if(!( col.getSQLType() == SQLTypes.DOUBLE || col.getSQLType()==SQLTypes.FLOAT )){
                        // int type, if found value is double then error indication
                        double dd= (new Double(ret)).doubleValue() ;
                        if ( dd !=(int)dd ) return "<span class='error_int'>"+ ret + "</span>";
                        else if ( dd > Math.pow(10,col.getLength())) return "<span class='error_int'>&nbsp;- - &nbsp;</span>";
                    }
                }
            }catch(Exception ere){
                logger.debug("Could not convert ret:" + ret);
            }
        }
        if( needNBSP && (ret ==null || ret.trim().equals(""))){
            return StringUtils.NBSP;
        }
        /* yfzhu modified here at 2003-03-15 for following reason:
        some data in <input> should maintain their original value int
        database, which will retrieve their data using getString(NoNeedNBSP)
        while the data just be displayed normally will be retrived using
        getString(needNBSP), so the escapance of Html tag should be deferred
        to getString() method */


        // replace with html tag
        if(needNBSP) ret=StringUtils.escapeHTMLTags(ret);

        //yfzhu mark above at 2003-03-15

        return ret;

    }
    /**
     * Add non breaking space (&nbsp;) if string is null or "".
     * This is useful when showing on html.
     * @param needNBSP if true, nbsp will be added to result string
     */
    public String getString(int column) throws QueryException{
        return getString( column, false, false);
    }
    /**
     * Get alternate key's value of current row
     * @return null if ak not found in the display columns
     * @throws QueryException
     */
    public Object getAKValue()throws QueryException{
    	int akPos= meta.getMaintableAKPosInSelection();
    	if(akPos==-1 ) return null;
    	return this.getObject(akPos);
    }
    /**
     * @roseuid 3B8AFCFC0049
     */
    public int getObjectID(int column) throws QueryException {
        if(column  <1 || column  > displayColumnIndices.length) {
            //nmdemo bugs found, and no throw now
            logger.debug( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
            return -1;
//            throw new QueryException( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
        }
        int idInRowItems=displayColumnIndices[column -1];
        if( column == displayColumnIndices.length) {
            // the last column
            if(idInRowItems ==( meta.getSelectionColumnCount() -1))// idInRowItems is the last item in selection list
                return -1;
        } else if(  (displayColumnIndices[column] ==  idInRowItems +1)) {
            /* the next column is also to be displayed*/
            return -1;
        }

        Object s=(((ArrayList)rows.get(cursor)).get(idInRowItems+1));
        int i=-1;
        try {
            i= Integer.parseInt(s+"");
        } catch(Exception e) {
//            logger.debug("Not a integer at column "+ column+"[value="+ s+"]");
//            throw new QueryException("Unexpected error(110) at column "+ column+".", e);
        }
        return i;
    }

    /**
     * @roseuid 3B8AFCFC00A3
     */
    public boolean next() {
        cursor ++;
        if( cursor < rows.size())
            return true;
        return false;
    }
    /**
     * Moves the cursor to the front of this ResultSet object, just before the first row. 
     * This method has no effect if the result set contains no rows.
     *
     */
    public void beforeFirst(){
    	cursor =-1;
    }
    /**
     * Every result has an additional row to summerize the values in columns whose
     * sum-method is not null
     * @param column
     * @return true if column's sum-method is not null
     */
    public boolean isSubTotalEnabled(int column){
        Column col= manager.getColumn(meta.getColumnId(column));
        return col.getSubTotalMethod() !=null;
    }

    /**
     * @param column
     * @return QueryRequest.isFullRangeSubTotalEnabled() && isSubTotalEnabled(int column)
     */
    public boolean isFullRangeSubTotalEnabled(int column){
        return request.isFullRangeSubTotalEnabled() && isSubTotalEnabled(column);
    }
    /**
     * 
     * @return array list which has same size as rows, for column which has no subtotal method
     * will be null,elements are Double
     */
    private ArrayList getSubtotalRow() {
    	ArrayList row=new ArrayList();
    	
        for( int i=1;i<= meta.getSelectionColumnCount();i++) {
        	row.add(null);
        }
        for(int i=0;i<displayColumnIndices.length;i++ ){
        	int column= i+1;
        	if( isSubTotalEnabled(column)){
                ArrayList al=new ArrayList();
                int idInRowItems=displayColumnIndices[column -1];
                Column col= manager.getColumn(meta.getColumnId(column));
                for(int j=0;j< getRowCount();j++){
                    al.add( ((ArrayList)rows.get(j)).get(idInRowItems));
                }
                Double d= new Double( SumMethodFactory.getInstance().
                        createSumMethod( col.getSubTotalMethod() ).calculate(al));
                // format to specified precision
                int scale=  col.getScale();
                if(scale==0){
	                row.set(idInRowItems, new Long(Math.round(d.doubleValue())));
                }else{
	                long s= (long)Math.pow(10,scale);
	                row.set(idInRowItems, new Double(Math.round(d.doubleValue()*  s )/(s*1.0)));
                }
            }
        }
        return row;
    }
    /**
     * Get subtotal value( double ) in String, return "" or StringUtils.NBSP if this
     * column has no subtotal value
     * @param column
     * @return double value's string presentation
     */
    public String getSubTotalValue(int column, boolean needNBSP){
        if( isSubTotalEnabled(column)){
            ArrayList al=new ArrayList();
            if(column  <1 || column  > displayColumnIndices.length) {
                logger.debug( "Column index "+column+" out of bound [1,"+displayColumnIndices.length+"].");
                if( needNBSP ){
                    return StringUtils.NBSP;
                }else return "";
            }
            int idInRowItems=displayColumnIndices[column -1];
            Column col= manager.getColumn(meta.getColumnId(column));
            for(int i=0;i< getRowCount();i++){
                al.add( ((ArrayList)rows.get(i)).get(idInRowItems));
            }
            return ((java.text.DecimalFormat)QueryUtils.floatPrintFormatter.get()).format(SumMethodFactory.getInstance().
                    createSumMethod( col.getSubTotalMethod() ).calculate(al));
        }else{
            return needNBSP?StringUtils.NBSP:"";
        }
    }

    
    
}
