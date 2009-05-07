/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

/**
 * Contains one param: 
 * 	"includeNull" -null value will be taken as one element or not 
 * @author yfzhu@agilecontrol.com
 */

public class AvgFun extends FunBase {
	
	public static String NAME="avg";
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
        	boolean includeNull= "Y".equals( params.get("includeNull"));
            return new Double(avg(valueSet, includeNull));
        }
	}
}
