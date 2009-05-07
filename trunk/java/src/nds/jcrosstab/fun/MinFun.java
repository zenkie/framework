/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

/**
 * @author yfzhu@agilecontrol.com
 */

public class MinFun extends FunBase {
	
	public static String NAME="min";
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
        	double min = Double.MAX_VALUE;
            for (int i = 0; i < valueSet.v.size(); i++) {
                double iValue = ((Double) valueSet.v.get(i)).doubleValue();
                if (iValue < min) {
                    min = iValue;
                }
            }
            return new Double(min);
        }
	}
}
