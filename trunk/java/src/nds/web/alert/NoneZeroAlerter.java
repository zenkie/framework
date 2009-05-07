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

public class NoneZeroAlerter extends ColumnAlerterSupport{
	private final static String NoneZeroAlerterCss="nonezero-row";
	Legend legend;
	public NoneZeroAlerter(){
		legend=new Legend();
		legend.addItem("nonezero-row", "nonezero-row");
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
			if(d.doubleValue()!=0){
				return NoneZeroAlerterCss;
			}else{
				return null;
			}
		}else{
			logger.error("Not numeric column "+ col + ", but set NoneZeroAlerter as intepreter");
			return null;
		}
	}
	
	private static  NoneZeroAlerter instance;
	public static NoneZeroAlerter getInstance(){
		if(instance==null){
			instance= new NoneZeroAlerter();
		}
		return instance;
	}
	
}
