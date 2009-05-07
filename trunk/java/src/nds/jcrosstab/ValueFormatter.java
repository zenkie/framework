/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab;

/**
 * Fact data formatter
 *   
 * @author yfzhu@agilecontrol.com
 */

public interface ValueFormatter {
	/**
	 * Format member 
	 * @param fact the value to be formatted, may be null
	 * @return
	 */
	public String format(java.lang.Number fact);
}
