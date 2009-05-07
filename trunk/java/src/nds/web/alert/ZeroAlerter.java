/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.alert;

import nds.query.QueryResult;
import nds.schema.Column;
import nds.schema.Legend;

import java.util.*;
import java.math.*;

/**
 * When value is not zero, load class "nonezero-row"
 * @author yfzhu@agilecontrol.com
 */

public class ZeroAlerter extends ColumnAlerterSupport{
	private final static String ZeroAlerterCss="zero-row";
	private Legend legend;
	public ZeroAlerter(){
		legend=new Legend();
		legend.addItem("zero-row", "zero-row");
	}	
	/**
	 * How to describe this alerter
	 * @return Legend which contains items that description each style of alert
	 */
	public Legend getLegend(Column column){
		return legend;
	}		
	/**
	 * Get css class of current row in result set, current column
	 * is specified, start from 1. Normally this is used in object list table
	 * @param rs
	 * @param column start from 1
	 * @return such as "red-row" or "bold-row"
	 */
	public String getRowCssClass(QueryResult rs, int column, Column col){
		if (col.getType()== Column.NUMBER){
			Object v=rs.getObject(column);
			if (v==null) return null;
			BigDecimal d= new BigDecimal( v+"");
			if(d.doubleValue()==0){
				return ZeroAlerterCss;
			}else{
				return null;
			}
		}else{
			logger.error("Not numeric column "+ col + ", but set ZeroAlerter as intepreter");
			return null;
		}
	}
	
	private static  ZeroAlerter instance;
	public static ZeroAlerter getInstance(){
		if(instance==null){
			instance= new ZeroAlerter();
		}
		return instance;
	}
	
}
