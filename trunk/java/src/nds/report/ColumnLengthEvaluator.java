/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.report;

import nds.schema.*;
import nds.util.*;
import java.util.*;
import nds.query.*;
import java.sql.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.model.dao.*;
import nds.model.*;
import org.hibernate.*;


/**
 * 计算字段的显示占位长度，对应于数据库表真实字段的字段将从ad_column.statsize中获得，虚拟字段将根据coltype的值。
 * ad_column.statsize 的确切存放内容为字段的平均字节数。对于显示占位，String 类型的可保持，date型的当有其他
 * 固定值(date 为10，datetime为19），数字型的当为 statsize * 3, limitvalue 为limitvalue 中的最大字节长度 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class ColumnLengthEvaluator {
	private static Logger logger= LoggerManager.getInstance().getLogger(ColumnLengthEvaluator.class.getName());
	/**
	 * 重新计算ad_column.statsize
	 * @param gatherDBStat if true, will retriever avg_col_len from USER_TAB_COLUMNS into ad_column.statsize again
	 */
	public void recalStateSize(boolean gatherDBStat) throws Exception{
		logger.debug("Begin recalculate column stat size...");
		QueryEngine engine=QueryEngine.getInstance();
		TableManager manager=TableManager.getInstance();
		Vector v=new Vector();
		if(gatherDBStat==true){
			// remove all statsize infor
			v.addElement("update ad_column set statsize=null");
			engine.doUpdate(v);
			engine.executeStoredProcedure("ad_column_statistics", Collections.EMPTY_LIST, false );
		}
		// fill those with statsize is null
		AdColumnDAO dao= new AdColumnDAO();
		AdColumn adColumn, clColumn; Column col; 
		List ls =dao.find("from AdColumn a where a.StatSize is null and a.IsActive='Y'");
		int length;
		for(Iterator it=ls.iterator();it.hasNext();){
			adColumn=(AdColumn) it.next();
			col= manager.getColumn(adColumn.getId().intValue());
			if(col==null){
				logger.error("Not found column in tablemanager:id="+ adColumn.getId()+" name="+ adColumn.getName());
				continue;
			}
			if(col.isColumnLink()){
				// get last column's size
				clColumn= dao.load( new Integer( col.getColumnLink().getLastColumn().getId()));
				if(clColumn ==null){
					logger.error("Not found ad_column with id="+ col.getColumnLink().getLastColumn().getId());
				}else{
					adColumn.setStatSize(clColumn.getStatSize());
				}
			}else{
				// get from columnLength, maximum to 20 for String
				switch( col.getType()){
					case Column.STRING: length= (col.getLength()>20?20:col.getLength());break;
					case Column.DATE: length= 7;break;
					case Column.NUMBER: length= col.getLength() /3; break;
					case Column.DATENUMBER: length=8; break;
					default: 
						logger.error("Unsupported colum type:" + col.getType()+" of Column " + col);
						length= 10;
				}
				adColumn.setStatSize(new Integer( length ));
			}
			dao.saveOrUpdate(adColumn);
		}
		logger.debug("End recalculate column stat size.");
	}
	/**
	 * Get print length (bytes )of the column. 
	 * @param col
	 * @throws Exception
	 */
	public int getColumnPrintLength(Column col) throws Exception{
		int len;
		if(col.isColumnLink())col= col.getColumnLink().getLastColumn();
		// find column.statsize in db
		int length;
		if( col.isValueLimited()){
			// if statsize is bigger than value descriptions, will use statsize 
			length= (col.getStatSize()!=-1 ? col.getStatSize(): 0);
			PairTable pt= col.getValues(Locale.US);
			for(Iterator it=pt.values();it.hasNext();){
				String s=(String)it.next();
				if( s.getBytes().length>length) length= s.getBytes().length;
			}
		}else{
			if (col.getStatSize()!=-1){
				// using column type to confirm
				switch( col.getType()){
				case Column.STRING: length=(( col.getStatSize()) >=0?col.getStatSize():30) ;break;
				case Column.DATE:
					if(col.getSQLType()== SQLTypes.TIMESTAMP) length=20;
					else length= 10;
					break;
				case Column.NUMBER: length= (( col.getStatSize()) >=0?col.getStatSize():30) *3; break;
				case Column.DATENUMBER: length= 8; break;
				default: 
					throw new NDSException("Unsupported colum type:" + col.getType()+" of Column " + col);
				}			
			}else{
				// char max=20, number max=10
				switch( col.getType()){
				case Column.STRING: length= (col.getLength()>20?20:col.getLength());break;
				case Column.DATE:
					if(col.getSQLType()== SQLTypes.TIMESTAMP) length=20;
					else length= 10;
					break;
				case Column.NUMBER: length= col.getLength()>10?10: col.getLength(); break;
				case Column.DATENUMBER: length=8; break;
				default: 
					throw new NDSException("Unsupported colum type:" + col.getType()+" of Column " + col);
				}			
				
			}		
		}
		return length;
	}
	/**
	 * Get print length (bytes )of the column. 
	 * @param col
	 * @throws Exception
	 */
	/*public int getColumnPrintLength(Column col) throws Exception{
		org.hibernate.Session session=null;
		AdColumnDAO dao= new AdColumnDAO();
		try{
		int len;
		if(col.isColumnLink())col= col.getColumnLink().getLastColumn();
		// find column.statsize in db
		session=dao.createSession();
		AdColumn adColumn;
		adColumn=dao.load(new Integer(col.getId()), session);
		int length;
		if( col.isValueLimited()){
			// if statsize is bigger than value descriptions, will use statsize 
			length= (adColumn.getStatSize()!=null ? adColumn.getStatSize().intValue(): 0);
			PairTable pt= col.getValues(Locale.US);
			for(Iterator it=pt.values();it.hasNext();){
				String s=(String)it.next();
				if( s.getBytes().length>length) length= s.getBytes().length;
			}
		}else{
			if (adColumn.getStatSize()!=null){
				// using column type to confirm
				switch( col.getType()){
				case Column.STRING: length=(( adColumn.getStatSize().intValue()) >=0?adColumn.getStatSize().intValue():30) ;break;
				case Column.DATE:
					if(col.getSQLType()== SQLTypes.TIMESTAMP) length=20;
					else length= 10;
					break;
				case Column.NUMBER: length= (( adColumn.getStatSize().intValue()) >=0?adColumn.getStatSize().intValue():30) *3; break;
				case Column.DATENUMBER: length= 8; break;
				default: 
					throw new NDSException("Unsupported colum type:" + col.getType()+" of Column " + col);
				}			
			}else{
				// char max=20, number max=10
				switch( col.getType()){
				case Column.STRING: length= (col.getLength()>20?20:col.getLength());break;
				case Column.DATE:
					if(col.getSQLType()== SQLTypes.TIMESTAMP) length=20;
					else length= 10;
					break;
				case Column.NUMBER: length= col.getLength()>10?10: col.getLength(); break;
				case Column.DATENUMBER: length=8; break;
				default: 
					throw new NDSException("Unsupported colum type:" + col.getType()+" of Column " + col);
				}			
				
			}		
		}
		return length;
		}finally{
			if(session !=null){
    			try{dao.closeSession();}catch(Exception e2){}
    		}
		}
	}*/
}
