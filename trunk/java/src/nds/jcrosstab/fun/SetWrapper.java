/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold aggregate function parameter values
 * @author yfzhu@agilecontrol.com
 */

public class SetWrapper {
    List v = new ArrayList();
    public int errorCount = 0, nullCount = 0;
    /*public List getList(){
    	return v;
    }*/
    public void addValue(Object o){
		if (o == null || o == FunUtil.nullValue) {
			nullCount++;
        } else if (o instanceof Throwable) {
            // Carry on summing, so that if we are running in a
            // BatchingCellReader, we find out all the dependent cells we
            // need
        	errorCount++;
        } else if (o instanceof Double) {
        	v.add(o);
        } else if (o instanceof Number) {
        	v.add(new Double( ((Number) o).doubleValue()));
        } else {
        	v.add(o);
        }		
	}
}