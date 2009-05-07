/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;

import java.util.*;
/**
 * Create function instance according to function name
 * 
 * @author yfzhu@agilecontrol.com
 */

public final class FunFactory {
	private static HashMap functions=new HashMap();
	static {
		functions.put(SumFun.NAME, SumFun.class);
		functions.put(AvgFun.NAME,AvgFun.class);
		functions.put(CountFun.NAME, CountFun.class);
		functions.put(MaxFun.NAME, MaxFun.class);
		functions.put(MinFun.NAME, MinFun.class);
		functions.put(MedianFun.NAME,MedianFun.class);
		functions.put(StdevFun.NAME, StdevFun.class);
		functions.put(VarianceFun.NAME, VarianceFun.class);
	}
	public static Fun createFunction(String name) throws ClassNotFoundException{
		if (name==null) throw new ClassNotFoundException("function name is null");
		Class fun=(Class)functions.get(name.toLowerCase());
		if (fun==null)  throw new ClassNotFoundException("function " + name+" not found");
		try{
		return (Fun)fun.newInstance();
		}catch(Throwable t){
			throw new ClassNotFoundException("function " + name+" no access");
		}
	}
}
