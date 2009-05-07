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
 * When value is null, load class "notnull-row"
 * @author yfzhu@agilecontrol.com
 */

public class NotNullAlerter extends ColumnAlerterSupport{
	private final static String NotNullAlerterCss="notnull-row";
	Legend legend;
	public NotNullAlerter(){
		legend=new Legend();
		legend.addItem("notnull-row", "notnull-row");
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
			if (v!=null) return NotNullAlerterCss;
			return null;
	}
	
	private static  NotNullAlerter instance;
	public static NotNullAlerter getInstance(){
		if(instance==null){
			instance= new NotNullAlerter();
		}
		return instance;
	}
	
}
