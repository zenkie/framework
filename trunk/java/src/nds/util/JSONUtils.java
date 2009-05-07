/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;
/**
 * Utils for JSON object handling
 * @author yfzhu@agilecontrol.com
 */

public class JSONUtils {
    //public final static DateFormat dateFormatter =new SimpleDateFormat("yyyy/MM/dd");
    /**
     * 
     * @param pt
     * @return In format like [[a,133],[b,133],[c,321]], abc are keys, 133 are values
     * @throws JSONException
     */
	public static Object toJSONArray(PairTable pt) throws JSONException{
		if(pt==null) return JSONObject.NULL;
		JSONArray ja=new JSONArray();
		for(int i=0;i< pt.size(); i++){
			JSONArray je= new JSONArray();
			je.put(pt.getKey(i));
			je.put(pt.getValue(i));
			ja.put(je);
		}
		return ja;
	}
	/**
	 * Convert java array to json array, if <param>cl</param> is multi-dimensional array
	 * the converted json array will also be that.
	 * @param cl
	 * @return JSONObject.NULL if <param>cl</param> is null
	 * @throws JSONException
	 */
	public static Object toJSONArray(Collection cl) throws JSONException{
		if(cl==null) return JSONObject.NULL;
		JSONArray ja=new JSONArray();
		DateFormat dateFormatter =(DateFormat)nds.query.QueryUtils.dateTimeSecondsFormatter.get();// new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for(Iterator it=cl.iterator();it.hasNext();){
			Object ele= it.next();
			if(ele!=null){
				if( ele instanceof Collection){
					ele= toJSONArray((Collection)ele);
				}else if(ele.getClass().isArray()){
					if(ele instanceof Object[]){
						Object[] objs=(Object[])ele;
						ele= toJSONArray( Arrays.asList(objs) );
					}else{
						// elements are Primitive, such as int, char, boolean
						ArrayList al=new ArrayList();
						for(int i=0;i< Array.getLength(ele);i++){
							al.add(Array.get(ele, i));
						}
						ele= toJSONArray(al);
					}
				}else{
					// convert date type object to String
					if(ele instanceof Date) ele= dateFormatter.format((Date)ele);
				}
			}
			ja.put(ele);
		}
		
		return ja;
	}
	/**
	 * 
	 * @param o should be array of Primitive type, such as int[], char[], boolean[] 
	 * @return
	 * @throws JSONException
	 */
	public static Object toJSONArrayPrimitive(Object o)  throws JSONException{
		// elements are Primitive, such as int, char, boolean
		ArrayList al=new ArrayList();
		for(int i=0;i< Array.getLength(o);i++){
			al.add(Array.get(o, i));
		}
		return  toJSONArray(al);
	}
	/**
	 * Convert JSONArray to ArrayList
	 * @param ja
	 * @return  null if ja is null
	 * @throws JSONException
	 */
	public static ArrayList toArray(JSONArray ja)throws JSONException{
		if(ja==null) return null;
		ArrayList al=new ArrayList();
		for(int i=0;i<ja.length();i++){
			al.add(ja.get(i));
		}
		return al;
	}
	/**
	 * Convert each element in ja to String
	 * @param ja
	 * @return
	 * @throws JSONException
	 */
	public static String[] toStringArray(JSONArray ja)throws JSONException{
		if(ja==null) return null;
		String[] r=new String[ja.length()];
		for(int i=0;i<ja.length();i++){
			r[i]=(ja.get(i).toString());
		}
		return r;
	}
	
}
