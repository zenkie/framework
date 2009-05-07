/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

import java.text.DecimalFormat;
/**
 * Aggregate function, will accept parameter for algorithm. and accepting data set, when execution requested
 * result will return.
 * 
 * @author yfzhu@agilecontrol.com
 */
 
public interface Fun {
	/**
	 * Set function parameter for algorithm condition, some function will act in difference according to 
	 * different parameter
	 * @param param
	 * @param value
	 */
	public void addParameter(String param, String value);
	/**
	 * Execute and return result 
	 * @return
	 */
	public Object execute(SetWrapper set);
	
	/**
	 * 
	 * @return java.sql.Types.INTEGER or java.sql.Types.DOUBLE
	 */
	public int getReturnType();
	
	/**
	 * Format return value
	 * @return
	 */ 
	public DecimalFormat getValueFormatter();
	
	/**
	 * Set value formatter
	 * @param ft
	 */
	public void setValueFormatter(DecimalFormat ft);
}
