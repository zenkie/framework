/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.alert;

import nds.query.*;
import nds.schema.*;
import nds.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import nds.log.*;
/**
 * For column of limit value group.
 * Will lasy load the specified class into memory
 * So this class will be singleton 
 * @author yfzhu@agilecontrol.com
 */

public class LimitValueAlerter extends  ColumnAlerterSupport{
	private final static String GET_CSSCLASS="select cssclass from ad_limitvalue where ad_limitvalue_group_id =(select ad_limitvalue_group_id from ad_column where id=?) and value=?";
	private final static String GET_LEGEND="select cssclass,description from ad_limitvalue where cssclass is not null and ad_limitvalue_group_id =(select ad_limitvalue_group_id from ad_column where id=?) order by orderno";
	private Hashtable cssClasses; // key : col.getId()+ data: value: css
	private Hashtable legends; //key: column.getId(), value: Legend
	public LimitValueAlerter(){
		cssClasses=new Hashtable();
		legends=new Hashtable();
	}
	
	public Legend getLegend(Column col){
		Legend l= (Legend)legends.get(new Integer(col.getId()));
		if(l==null && col.isValueLimited()){
			Connection con=null;
	        PreparedStatement pstmt=null ;
	        ResultSet rs=null;
	        try {
	        	con=QueryEngine.getInstance().getConnection() ;
	            pstmt= con.prepareStatement(GET_LEGEND);
	            pstmt.setInt(1, col.getId());
	            rs= pstmt.executeQuery();
	            int pid; String value;
	            while( rs.next() ){
	                if(l==null) l=new Legend();
	                l.addItem(rs.getString(1), rs.getString(2));
	            }
	        }catch (Exception ex) {
	        	logger.error("Error",ex);
	        }finally{
	            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
	            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
	            try{ con.close() ;} catch(Exception e3){}
	        }
	        if(l==null) l= Legend.EMPTY_LEGEND;
	        legends.put(new Integer(col.getId()),l);
		}
		return l;
	}
	/**
	 * Clear cache
	 *
	 */
	public void clear(){
		cssClasses.clear();
		legends.clear();
	}
	/**
	 * Get css class of current row in result set, current column
	 * is specified, start from 1. Normally this is used in object list table
	 * @param rs
	 * @param column start from 1
	 * @return such as "red-row" or "bold-row", or null/"" if no class suit
	 */
	public String getRowCssClass(QueryResult qrs, int column, Column col){
		Object v=qrs.getObject(column);
		if(v==null) return null;
		String key=col.getId()+"_"+v;
		String s=(String) cssClasses.get(key);
		
		if( s==null){
	        Connection con=null;
	        PreparedStatement pstmt=null ;
	        ResultSet rs=null;
	        try {
	        	con=QueryEngine.getInstance().getConnection() ;
	            pstmt= con.prepareStatement(GET_CSSCLASS);
	            pstmt.setInt(1, col.getId());
	            pstmt.setString(2, v.toString());
	            rs= pstmt.executeQuery();
	            int pid; String value;
	            if( rs.next() ){
	                s= rs.getString(1);
	            }
	        }catch (Exception ex) {
	        	logger.error("Error",ex);
	        }finally{
	            try{ if(rs !=null)rs.close() ;}catch(Exception e){}
	            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
	            try{ con.close() ;} catch(Exception e3){}
	        }
			if( Validator.isNull(s)) s=""; // keep not null, so will not search next time
			cssClasses.put(key, s);
		}
		return s;
	}

	
	private static  LimitValueAlerter instance;
	public static LimitValueAlerter getInstance(){
		if(instance==null){
			instance= new LimitValueAlerter();
		}
		return instance;
	}
	
}
