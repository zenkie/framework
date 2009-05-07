/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.alert;
import nds.query.*;
import nds.schema.*;
/**
 * Set css class on list or image on object
 * @author yfzhu@agilecontrol.com
 */

public interface ColumnAlerter {
	/**
	 * How to describe this alerter
	 * @return Legend which contains items that description each style of alert
	 */
	public Legend getLegend(Column column);
	/**
	 * Get css class of current row in result set, current column
	 * is specified, start from 1. Normally this is used in object list table
	 * @param rs
	 * @param column start from 1
	 * @return such as "red-row" or "bold-row"
	 */
	public String getRowCssClass(QueryResult rs, int column, Column col);
	/**
	 * Get image src of current row in result set.
	 * Normally used in single object ui
	 * @param rs
	 * @param column
	 * @return such as "/images/company.gif", related to NDS_PATH
	 */
	public String getRowImageSrc(QueryResult rs, int column, Column col);
}
