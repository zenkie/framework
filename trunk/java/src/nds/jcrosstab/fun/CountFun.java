/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

import java.text.DecimalFormat;
import java.text.Format;

/**
 * Contains one param: 
 * 	"includeNull" -null value will be taken as one element or not 
 * @author yfzhu@agilecontrol.com
 */

public class CountFun extends FunBase {
	private static DecimalFormat COUNT_FORMATTER= new DecimalFormat("#0");
	public static String NAME="count";
	/**
	 * Execute and return result 
	 * @return Integer
	 */
	public Object execute(SetWrapper valueSet){
		if (valueSet.errorCount > 0) {
            return nullValue;
        }  else {
        	boolean includeNull= "Y".equals( params.get("includeNull"));
        	int cnt= valueSet.v.size();
        	if(includeNull) cnt+= valueSet.nullCount;
        	return new Integer(cnt);
        }
	}
	/**
	 * 
	 * @return java.sql.Types.INTEGER or java.sql.Types.DOUBLE
	 */
	public int getReturnType(){
		return java.sql.Types.INTEGER;
	}	
	/**
	 * Format return value
	 * @return
	 */ 
	public DecimalFormat getValueFormatter(){
		return format==null?COUNT_FORMATTER:format;
	}	
}
