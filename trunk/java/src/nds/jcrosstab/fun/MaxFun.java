/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

/**
 * @author yfzhu@agilecontrol.com
 */

public class MaxFun extends FunBase {
	
	public static String NAME="max";
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
        	double max = Double.MIN_VALUE;
            for (int i = 0; i < valueSet.v.size(); i++) {
                double iValue = ((Double) valueSet.v.get(i)).doubleValue();
                if (iValue > max) {
                    max = iValue;
                }
            }
            return new Double(max);
        }
	}
}
