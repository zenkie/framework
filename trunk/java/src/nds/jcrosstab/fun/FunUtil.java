/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class FunUtil {
    /**
     * Special value which indicates that a <code>double</code> computation
     * has returned the MDX null value. 
     */
    public static final double DoubleNull = 0.000000012345;

    /**
     * Special value which indicates that a <code>double</code> computation
     * has returned the MDX EMPTY value. 
     */
    public static final double DoubleEmpty = -0.000000012345;

    /**
     * Special value which indicates that an <code>int</code> computation
     * has returned the MDX null value.
     */
    public static final int IntegerNull = Integer.MIN_VALUE + 1;

    /**
     * Null value in three-valued boolean logic.
     * Actually, a placeholder until we actually implement 3VL.
     */
    public static final boolean BooleanNull = false;	
    /**
     * Placeholder which indicates a value NULL.
     */
    public static final Object nullValue = new Double(DoubleNull);

    /**
     * Placeholder which indicates an EMPTY value.
     */
    public static final Object EmptyValue = new Double(DoubleEmpty);


    static double avg(SetWrapper sw, boolean includeNull) {
        double sum = 0.0;
        for (int i = 0; i < sw.v.size(); i++) {
            sum += ((Double) sw.v.get(i)).doubleValue();
        }
        double avg;
        if(includeNull)  avg= sum /  ((double) sw.v.size()+ sw.nullCount);
        else
        	avg= sum /  (double) sw.v.size();
        return avg;
    }
    static double var(SetWrapper valueSet, boolean biased) {
    	double stdev = 0.0;
        double avg = avg(valueSet, false);
        for (int i = 0; i < valueSet.v.size(); i++) {
            stdev += Math.pow((((Double) valueSet.v.get(i)).doubleValue() - avg),2);
        }
        int n = valueSet.v.size();
        if (!biased) {
            n--;
        }
        return stdev / (double) n;
    	
    }
    private final static String[] VALIDGROUPBYFUNCTION=new String[]{
		"SUM","MAX", "COUNT", "MIN"
	};
	/**
	 * //和平均有关的函数，包括avg, var,stdev，都不能让数据库进行group by 操作
	    //而计数，最大，最小，累计等，可以先使用数据库完成有关group by运算
	 * @param func
	 * @return
	 */
	public static boolean isValidGroupByFunction(String func){
		func=func.toUpperCase();
		for(int i=0;i< VALIDGROUPBYFUNCTION.length;i++)
			if(VALIDGROUPBYFUNCTION[i].equals(func)) return true;
		return false;
	}    
    
}
