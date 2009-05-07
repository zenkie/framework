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

public class VarianceFun extends FunBase {
	
	public static String NAME="var";
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
        	
            return new Double( var(valueSet, biased));
        }
	}
}
