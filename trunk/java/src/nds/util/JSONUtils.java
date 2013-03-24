/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.text.*;
import java.util.*;

import nds.query.QuerySession;

import org.apache.wsrp4j.log.Logger;
import org.json.*;
/**
 * Utils for JSON object handling
 * @author yfzhu@agilecontrol.com
 */

public class JSONUtils {
	
	/**
	 *  toURLQuery not support more child is JSONObject/JSONArray
	 */
	public static String toURLQuery(JSONObject jo) throws Exception {
		if (jo == null)
			return "";
		StringBuffer url_b = new StringBuffer();
		for (Iterator localIterator = jo.keys(); localIterator.hasNext();) {
			String str = (String) localIterator.next();
			Object jor = jo.get(str);
			if ((jor instanceof JSONObject) || (jor instanceof JSONArray))
				throw new NDSException(
						"Unexpected JSONObject/JSONArray found as property in JSONObject:"
								+ jo);
			str = URLEncoder.encode(str, "UTF-8");
			Object val = URLEncoder.encode(jor.toString(), "UTF-8");
			url_b.append(str).append("=").append(val).append("&");
		}
		if (url_b.length() > 1)
			url_b.deleteCharAt(url_b.length() - 1);
		return url_b.toString();
	}
	
	/**
	 * Replace variable in <param>sqlWithVariable</param> by attributes
	 * in session.
	 * Variables has format as $xxx$, such as $AD_Client_ID$, and
	 * if attribute found in session has that name, then the sql string 
	 * will be replace, sample:
	 * "select id from ad_client_id where ad_client_id in ($ad_client_id$)"
	 * will be replace to 
	 * "select id from ad_client_id where ad_client_id in (10993)"
	 * if there's $ad_client_id$=10993
	 * 
	 * @param str
	 * @param jo
	 * @return
	 */
	public static String replaceVariables(String str, JSONObject jo){
		if (jo ==null) return str;
		if( str ==null) return null;
		//method: search strWithVariables one by one, when found "$",
		//check to the next "$", and try to found attribute value of
		//that, if not found, take first $ as nothing, go to next.
		StringBuffer sb=new StringBuffer();
		int p= 0,p1,p2;
		while(p < str.length()){
			p1= str.indexOf("$", p);
			if(p1>-1){
				//found
				p2=str.indexOf("$", p1+1);
				if(p2>-1){
					//found second
					String n= str.substring(p1+1, p2);// escape $$, this is different with QueryUtils#replaceVariables
					Object v= jo.opt(n);
					if (v!=null) {
						//replace variable to attribute value
						sb.append(str.substring(p, p1)).append(v);
						p=p2+1;
					}else{
						//remain the fake variable, not include last $
						sb.append(str.substring(p, p2));
						p=p2;
					}
					
				}else{
					// not found the second $, so no variable any more
					sb.append(str.substring(p));
					break;
				}
			}else{
				// not found the first $,so no variable any more
				sb.append(str.substring(p));
				break;
			}
		}
		return sb.toString();
	}	 	
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
	 * Convert to json array
	 * @param rs, should have cursor moved before first row
	 * @param maxRows, end row at maxRows
	 * @return
	 * @throws JSONException
	 */
	public static JSONArray toJSONArray(java.sql.ResultSet rs, long maxRows) throws JSONException,java.sql.SQLException{
		int cols=rs.getMetaData().getColumnCount();
		JSONArray ja=new JSONArray(); 
		long cnt=0;
		while(rs.next() && cnt<maxRows){
			JSONArray row=new JSONArray();
			for(int i=0;i<cols;i++){
				row.put(rs.getObject(i+1));
			}
			ja.put(row);
			cnt++;
		}
		return ja;
	}
	/**
	 * Convert java array to json array, if <param>cl</param> is multi-dimensional array
	 * the converted json array will also be that.
	 * @param cl
	 * @param formats for each column of the collection row, how to format them, may be null for special
	 * column, meaning just origianl data
	 * @return JSONObject.NULL if <param>cl</param> is null
	 * @throws JSONException
	 */
	public static Object toJSONArray(Collection cl, Format[] formats) throws JSONException{
		if(cl==null) return JSONObject.NULL;
		JSONArray ja=new JSONArray();
		DateFormat dateFormatter =(DateFormat)nds.query.QueryUtils.dateTimeSecondsFormatter.get();// new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		int pos=0;
		for(Iterator it=cl.iterator();it.hasNext();){
			Object ele= it.next();
			if(ele!=null){
				if( ele instanceof Collection){
					ele= toJSONArray((Collection)ele,formats);
				}else if(ele.getClass().isArray()){
					if(ele instanceof Object[]){
						Object[] objs=(Object[])ele;
						ele= toJSONArray( Arrays.asList(objs),formats  );
					}else{
						// elements are Primitive, such as int, char, boolean
						ArrayList al=new ArrayList();
						for(int i=0;i< Array.getLength(ele);i++){
							al.add(Array.get(ele, i));
						}
						ele= toJSONArray(al,formats);
					}
				}else{
					// convert date type object to String
					if(ele instanceof Date) ele= dateFormatter.format((Date)ele);
					else{
						try{
							if(formats[pos] !=null) ele=formats[pos].format(ele);
						}catch(IllegalArgumentException  t){
							System.err.println("Fail to format "+ ele + "("+ ele.getClass()+") on pos "+ pos+ " of formatter:"+ formats[pos]+":"+ t);
							//throw t;
						}
					}
				}
			}
			ja.put(ele);
			pos++;
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
