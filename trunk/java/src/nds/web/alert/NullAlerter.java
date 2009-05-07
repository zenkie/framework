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
import nds.util.*;
/**
 * When value is null, load class "null-row"
 * @author yfzhu@agilecontrol.com
 */

public class NullAlerter extends ColumnAlerterSupport{
	private final static String NullAlerterCss="null-row";
	private Legend legend;
	public NullAlerter(){
		legend=new Legend();
		legend.addItem("null-row", "null-row");
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
			Object v=rs.getObject(column);
			if (v==null) return NullAlerterCss;
			return null;
	}
	
	private static  NullAlerter instance;
	public static NullAlerter getInstance(){
		if(instance==null){
			instance= new NullAlerter();
		}
		return instance;
	}
	
}
