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
 * When value is negative, load class "negative-row"
 * @author yfzhu@agilecontrol.com
 */

public class NegativeAlerter extends ColumnAlerterSupport{
	private final static String NegativeAlerterCss="negative-row";
	Legend legend;
	public NegativeAlerter(){
		legend=new Legend();
		legend.addItem("negative-row", "negative-row");
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
			if(d.doubleValue()<0){
				return NegativeAlerterCss;
			}else{
				return null;
			}
		}else{
			logger.error("Not numeric column "+ col + ", but set NegativeAlerter as intepreter");
			return null;
		}
	}
	
	private static  NegativeAlerter instance;
	public static NegativeAlerter getInstance(){
		if(instance==null){
			instance= new NegativeAlerter();
		}
		return instance;
	}
	
}
