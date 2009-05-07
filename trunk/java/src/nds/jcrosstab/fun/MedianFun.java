/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

import java.util.*;
/**
 * @author yfzhu@agilecontrol.com
 */

public class MedianFun extends FunBase {
	
	public static String NAME="median";
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
        	double[] asArray = new double[valueSet.v.size()];
            for (int i = 0; i < asArray.length; i++) {
                asArray[i] = ((Double) valueSet.v.get(i)).doubleValue();
            }
            Arrays.sort(asArray);

            /*
             * The median is defined as the value that has exactly the same
             * number of entries before it in the sorted list as after.
             * So, if the number of entries in the list is odd, the
             * median is the entry at (length-1)/2 (using zero-based indexes).
             * If the number of entries is even, the median is defined as the
             * arithmetic mean of the two numbers in the middle of the list, or
             * (entries[length/2 - 1] + entries[length/2]) / 2.
             */
            int length = asArray.length;
            Double result = ((length & 1) == 1)
                // The length is odd. Note that length/2 is an integer expression,
                // and it's positive so we save ourselves a divide...
                ? new Double(asArray[length >> 1])
                : new Double((asArray[(length >> 1) - 1] + asArray[length >> 1]) / 2.0);
            return result;

        }
	}
}
