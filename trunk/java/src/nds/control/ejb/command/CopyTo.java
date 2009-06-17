/******************************************************************
*
*$RCSfile: CopyTo.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2006/06/24 00:32:31 $
*
********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.ejb.command.tree.TreeNodeHolder;
import nds.control.ejb.command.tree.TreeNodeManager;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.EJBUtils;
import nds.control.util.NavNode;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.*;
import nds.schema.*;

/** 
 * Copy from one table's records to another table's records
 */
public class CopyTo extends Command {
	private static final int MAX_RECORD_COUNT=20;
	
    /**
     * Copy 1~20 records of the source table records to destination table
     * 
     * @param event - can has following parameters:
     *  src_table  - source  table id
     *  dest_table - destination table id
     *  fixedcolumn - fixed columns (optional)
     *  mainobjecttableid - parent table's id
     *  objectids   - source table records (optional)
     *  filter_{$x}_sql   - query that can be convert to  source table ids (optional)
     *                  {$x} is specified by "selectedItemIdx"
     *                   sql in format like "in(0,3)" or "in (select id from table where xxx)"
     * @return valueholder contains a DefaultWebEvent named WebKeys.DEFAULT_WEB_EVENT
     * which contains to be created records data
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
    	//logger.debug(event.toDetailString());
    	String nextScreen;
    	PairTable fixedColumns=PairTable.parseIntTable((String)event.getParameterValue("fixedcolumns",true), null);
    	ValueHolder vh=new ValueHolder();
    	TableManager manager= TableManager.getInstance();
    	Table destTable= manager.getTable( Tools.getInt(event.getParameterValue("dest_table",true), -1));
    	String[] s=event.getParameterValues("src_table");
    	Table srcTable=null;
    	if(s !=null && s.length==1){
    		srcTable=manager.getTable( Tools.getInt(s[0],-1));
    	}
    	if( srcTable ==null)throw new NDSException("@must-set-ds@");
    	if(destTable==null)throw new NDSException("@must-set-table@");
    	
    	String mainobjecttableid= (String)event.getParameterValue("mainobjecttableid");
    	String objectids= (String)event.getParameterValue("objectids");
    	
    	String filter=null;
    	if( Validator.isNull(objectids)){
    		// parsing src_query
    		
    		String sqls[]=event.getParameterValues("src_query_sql");
    		if(sqls !=null && sqls.length==1 && sqls[0]!=null &&  sqls[0].trim().length()>1 ){
    			filter=  " "+sqls[0];
    		}else
    			throw new NDSException("@db-filter-condition@");
    		
    	}else{
    		filter=  " IN (" +objectids+")";
    	}
    	int maxRecordCount=Tools.getInt( EJBUtils.getApplicationConfigurations().getProperty("controller.copy.max"),MAX_RECORD_COUNT);
    	// query has PK in the last selection
    	QueryRequestImpl query= getQuery(destTable,fixedColumns);
    	query.addParam( destTable.getPrimaryKey().getId(), filter);
    	query.setRange(0, maxRecordCount);
    	logger.debug(query.toSQL());
    	QueryResult result= QueryEngine.getInstance().doQuery(query);
    	if(result.getRowCount()< 2){
        	nextScreen=WebKeys.NDS_URI+"/object/object.jsp?mainobjecttableid="+mainobjecttableid+"&table="+destTable.getId()+"&action=input&fixedcolumns="+java.net.URLEncoder.encode(fixedColumns.toURLQueryString(""));
    	}else{
    		nextScreen=WebKeys.NDS_URI+"/objext/object_batchadd.jsp?mainobjecttableid="+mainobjecttableid+"&table="+destTable.getId()+"&fixedcolumns="+java.net.URLEncoder.encode(fixedColumns.toURLQueryString(""));
    	}
    	vh.put("next-screen",nextScreen);
    	if (result.getRowCount()==0) {
    		vh.put("message","@ds-not-exist@");
    		return vh;
    	} 
    	String message="@data-copy-not-saved@";

    	int recordCount= result.getRowCount();
    	DefaultWebEvent e= createCopyToEvent(result, recordCount);
    	
    	//logger.debug("generated event:"+ e.toDetailString());
    	vh.put(WebKeys.DEFAULT_WEB_EVENT, e);
    	vh.put("message",message);
    	vh.put("linecount", String.valueOf(recordCount));// will be used by batch add jsp
    	return vh;
        
    }
    /**
     * Create event according to result
     * @param result that should be create by #getQuery 
     * @param maxRecordCount -1 for unlimited 
     * @return
     * @throws QueryException
     */
    public static DefaultWebEvent createCopyToEvent(QueryResult result, int maxRecordCount) throws QueryException{
    	CollectionValueHashtable ht=new CollectionValueHashtable();
    	int cl=result.getMetaData().getColumnCount();
    	//logger.debug("column count="+ cl);
    	Object v;
    	int recordCount= result.getTotalRowCount();
    	if( recordCount>maxRecordCount && maxRecordCount>0) recordCount=maxRecordCount;
		int cnt=0;
    	while( result.next()){
    		for(int i=1 ;i< cl+1;i++){
    			v=result.getObject(i);
    			if(v==null)v="";
    			if(v instanceof java.util.Date) v= ((java.text.SimpleDateFormat)QueryUtils.inputDateFormatter.get()).format((java.util.Date)v);
    			ht.add(new Integer(i), v);
    		} 
    		cnt ++;
    		if (cnt ==recordCount ) break;
    	}
    	ArrayList scl=result.getQueryRequest().getAllSelectionColumnLinks();
    	//logger.debug("getAllSelectionColumnLinks count="+ scl.size());
    	DefaultWebEvent e=new DefaultWebEvent("copyto"); 
    	for(int i=0;i< scl.size()-1;i++){
    		Column[] clink=((ColumnLink)scl.get(i)).getColumns();
    		String name=clink[0].getName().toLowerCase();
    		for(int j=1;j<clink.length;j++){
    			name += "__" + clink[j].getName().toLowerCase();
    		}
    		//logger.debug("name="+ name);
    		Collection col= ht.get( new Integer(i+1));
    		Object[] ss=(Object[]) col.toArray();
    		
    		e.setParameter(name,ss);
    	}
    	// pk ik will be in the last selection list, they will be set to "copyfromid" param.
    	// this param will set to hidden param of creation page, and will duplicate the original's
    	// reftable records(@see nds.schema.RefByTable#isBundledWhenCreate) to newly records
    	e.setParameter("copyfromid", ((Collection)ht.get(new Integer(scl.size()))).toArray());
    	
    	// for check box to be selected
    	String[] sidx= new String[recordCount];
    	for(int i=0;i<recordCount;i++){
    		sidx[i]=""+i;
    	}
    	e.setParameter("selectedItemIdx",sidx);
    	
    	e.setParameter(DefaultWebEvent.SELECTER_NAME,"selectedItemIdx" );
    	return e;
    }
    /**
     * Filter columns in fixed columns, add PK in the last column
     * @param mainTable
     * @param fixedColumns if mainTable's column exists in fixedColumns, it will not be added to selections
     * @return
     * @throws QueryException
     */
    public static QueryRequestImpl getQuery(Table mainTable,PairTable fixedColumns) throws QueryException{
    	QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
    	// will not use main table's filter, this is because of the
    	// src table may not be the same table of dest table
    	// Sample, source table is "u_comment", dest table is "u_v_a_comment", which
    	// contains a filter named: "U_V_A_COMMENT.HANDLE_STATE='²Ý¸å'", if this filter
    	// is put over record result from u_comment, then will have nothing selected
    	query.setMainTable(mainTable.getId(), false/*not use filter*/);
        if( mainTable ==null)
            throw new QueryException("MainTable must be set before calling this");
        ArrayList columns=mainTable.getShowableColumns(Column.ADD);
        for(int i=0;i<columns.size();i++) {
            Column col= (Column) columns.get(i);
            if(col.isModifiable(Column.ADD)==false) continue;
            // escape fixed columns
            if(fixedColumns!=null && fixedColumns.get(new Integer(col.getId()))!=null) continue;
            
            if( col.getReferenceTable() !=null) {
                Column col2=col.getReferenceTable().getAlternateKey();
                query.addSelection(col.getId(),col2.getId(),false);// not show foreign table's PK
            } else {
             	query.addSelection(col.getId());
            }
        }
        query.addSelection(mainTable.getPrimaryKey().getId());
        return query;
           
    }
}
