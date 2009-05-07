/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class SumFun extends FunBase {
	public static String NAME="sum";
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
            double sum = 0.0;
            for (int i = 0; i < valueSet.v.size(); i++) {
                sum += ((Double) valueSet.v.get(i)).doubleValue();
            }
            return new Double(sum);
        }
	}
}
