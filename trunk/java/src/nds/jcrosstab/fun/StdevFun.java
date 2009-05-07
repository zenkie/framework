/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

/**
 * Contains one param: 
 * 	"biased" - biased
 * @author yfzhu@agilecontrol.com
 */

public class StdevFun extends FunBase {
	 
	public static String NAME="stdev";
	/**
	 * Execute and return result 
	 * @return 
	 */
	public Object execute(SetWrapper valueSet){
		if (valueSet.errorCount > 0) {
            return nullValue;
        } else if (valueSet.v.size() == 0) {
            return nullValue;
        } else {
        	boolean biased= "Y".equals( params.get("biased"));        	
        	double d= var(valueSet, biased);
            return new Double(Math.sqrt(d));
        }
	}
}
