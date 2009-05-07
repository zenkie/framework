/******************************************************************
*
*$RCSfile: DefaultWebEvent.java,v $ $Revision: 1.8 $ $Author: Administrator $ $Date: 2005/11/16 02:57:20 $
*
*$Log: DefaultWebEvent.java,v $
*Revision 1.8  2005/11/16 02:57:20  Administrator
*no message
*
*Revision 1.7  2005/08/03 10:01:44  Administrator
*no message
*
*Revision 1.6  2005/06/16 10:19:18  Administrator
*no message
*
*Revision 1.5  2005/05/27 05:01:47  Administrator
*no message
*
*Revision 1.4  2005/04/18 03:28:16  Administrator
*no message
*
*Revision 1.3  2005/03/30 13:13:54  Administrator
*no message
*
*Revision 1.2  2005/03/16 09:04:50  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.3  2003/05/29 19:40:08  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/03/30 08:11:51  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.4  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.event;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;

import java.util.*;

import org.json.JSONObject;

import nds.util.NDSRuntimeException;
import nds.util.Tools;
/**
 * Event holding request information which no hanlder specified
 *
 */
public class DefaultWebEvent implements  NDSEvent {
    private static Logger logger= LoggerManager.getInstance().getLogger(DefaultWebEvent.class.getName());
	
    public final static String SELECTER_NAME="arrayItemSelecter";
    private int maxNonEmptyArraySize=0;

    private HashMap data;
    private String eventName;
    private Date creationDate;
    public DefaultWebEvent(String eventName){
        data=new HashMap();
        this.eventName=eventName;
        creationDate=new Date();
    }
    public Date getCreationDate(){
    	return creationDate;
    }
    /**
     * Reset event's name
     * @param e
     * @since 2.0
     */
    public void setEventName(String e){
    	this.eventName=e;
    }
    public Object clone(){
    	DefaultWebEvent e=new DefaultWebEvent(this.eventName);
    	e.data.putAll(this.data);
    	e.maxNonEmptyArraySize= this.maxNonEmptyArraySize;
    	return e;
    }
    /**
     * This is usually called in DefaultRquestHanlder
     * @param param - name of parameter to be set, note characters to change
     *                to upper case when store, that is, parameter is case-insensitive
     *
     * @see nds.control.web.reqhandler.DefaultRquestHanlder
     */
    public void setParameter(String param, String value){
        data.put(param.toUpperCase(), value==null?null:value.trim());
        if(maxNonEmptyArraySize<1) maxNonEmptyArraySize=1;
    }
    /**
     * This is used for more loosely environment
     * @param param
     * @param value should be serializable, since event will traverse across the network
     */
    public void put(String param, Object value){
        data.put(param.toUpperCase(), value);
    }
    /**
     * @param param - name of parameter to be set, note characters to change
     *                to upper case when store
     *  @param values should be String[]
     */
    public void setParameter(String param, Object[] values){
        if(values ==null ) return;

    	int nonEmptyArraySize=0;
	        for( int i=0;i< values.length;i++){
	            values[i]=values[i]==null? "":  values[i].toString().trim();
	            if( !values[i].equals(""))nonEmptyArraySize=i+1;
	        }
        data.put(param.toUpperCase(), values);
        if(maxNonEmptyArraySize<nonEmptyArraySize){
            maxNonEmptyArraySize= nonEmptyArraySize;
        }

	}
    public Iterator getParameterNames(){
        return data.keySet().iterator();
    }
    /**
     * 
     * @return data of the event
     * @since 2.0
     */
    public HashMap getData(){
    	return data;
    }
    /**
     * @return json object is it's json request
     */
    public JSONObject getJSONObject(){
    	return (JSONObject)data.get("JSONOBJECT");// always upper case
    }
    /**
     * 
     * @param name
     * @param tryJsonObject if true, will try to find "jsonObject" in event data
     *        and fetch value from that json first 
     *        Note if from json object, the name is case sensitive.
     * @return
     */
    public Object getParameterValue(String name, boolean tryJsonObject){
    	JSONObject jo=null;
    	if(tryJsonObject){jo= getJSONObject();}
    	if(jo==null){
    		return getParameterValue(name);	
    	}
    	return jo.opt(name);
    }
    /**
     * 
     * @param name
     * @param tryJsonObject if true, will try to find "jsonObject" in event data
     *        and fetch value from that json first 
     *        Note if from json object, the name is case sensitive.
     * @return
     */
    public String[] getParameterValues(String name, boolean tryJsonObject){
    	JSONObject jo=null;
    	if(tryJsonObject){jo= getJSONObject();}
    	if(jo==null){
    		return getParameterValues(name);	
    	}
    	Object a=jo.opt(name);
    	if(a==null|| JSONObject.NULL.equals(a) ) return null;
    	String[] r=null;
    	if(a instanceof org.json.JSONArray){
    		org.json.JSONArray ja=((org.json.JSONArray)a);
    		r=new String[ja.length()];
    		for(int i=0;i<ja.length();i++){
    			r[i]= ja.optString(i);
    		}
    	}else{
    		r=new String[]{ a.toString()};
    	}
    	return r;
    }
    /**
     * @param name - the name of parameter to be retrieved, note <code>name</code> is
     *              case-insentitive
     */
    public Object getParameterValue(String name){
        return data.get(name.toUpperCase());
    }
    /**
     * Returns an array of String objects containing all of the values the given
     * request parameter has, or null if the parameter does not exist.
     *
     * If the parameter has a single value, the array has a length of 1.
     * @param name - the name of parameter to be retrieved, note <code>name</code> is
     *              case-insentitive
     * @return an array of String objects containing the parameter's values
     */
    public String[] getRawParameterValues(String name){
        Object obj=data.get(name.toUpperCase());
        if( obj ==null) return null;
        String[] ret=null;
        if(obj instanceof String[]) return (String[])obj;
        if(obj.getClass().isArray()){
            Object[] objs=(Object[])obj;
            ret= new String[(objs.length > maxNonEmptyArraySize?maxNonEmptyArraySize:objs.length )];
            //logger.debug("getRawParameterValues("+name+"): ret length:"+ ret.length);
            for( int i=0;i< ret.length;i++){
                //ret[i]= ""+ objs[i];
            	ret[i]= objs[i]==null? null: objs[i].toString();
            }
        }else{
            ret= new String[1];
            ret[0]= obj.toString();
        }
        return ret;
    }
    /**
     * Find array size of parameter value specified by <code>name</code>
     * @param name
     * @return 0 if not found
     */
    private int getRawParameterValueSize(String name){
    	Object obj=data.get(name.toUpperCase());
        if( obj ==null) return 0;
        if(obj.getClass().isArray()){
            return ((Object[])obj).length;
        }else{
        	return 1;
        }
    }
    /**
     * 为解决Item 页面上的未选中行(MR102,袁艺)和内容错位(MR103,周沥青), 对该函数处理如下
     *
     * 如果在event中存在参数"arrayItemSelecter"，
     *      则将对应的参数值对应的参数作为array取出，转换为int[], 将<code>name</code>对应的array
     *      作为参数依次读取parameter，组成parameter value array 返回。
     *      举例：
     *          设 getParameterValue("arrayItemSelecter")= "selectItemIdx",
     *             getRawParameterValues("selectItemIdx") 返回 int[]={"0","1","3"}
     *          如果请求参数 getParameterValues("itemIdx")，实际返回的值是：
     *              String[]={ getParameterValue("itemIdx$0"), getParameterValue("itemIdx$1"), getParameterValue("itemIdx$3")}
     *
     *
     * 如果在event中无参数"arrayItemSelecter"，则按照getRawParameterValues处理
     *
     * @param name - the name of parameter to be retrieved, note <code>name</code> is
     *              case-insentitive
     * @return an array of String objects containing the parameter's values
     *         or null if that named parameter not exist in event
     */
    public String[] getParameterValues(String name){
        String[] nonfiltered=this.getRawParameterValues(name);
        //logger.debug("raw values for "+ name +":" + Tools.toString(nonfiltered) );
        /*-- yfzhu add null handle at 2003-04-03 for support of no-existance value retrieval
             from ejb layer, such condition occurs when <byPage> specified in column definition
             while <mask> forbid this column from showing on page. Sample:
             table=OutletDaySaleShtItem, column= billNo
        --*/
        if (nonfiltered==null) return null;
        /**-- added above at 2003-04-03 */
        String selector=(String) getParameterValue(SELECTER_NAME);
        if( selector==null) {
            return nonfiltered;
        }
        String[] idx=getRawParameterValues(selector);
        if( idx==null) return null;
        int [] is= toIntArray( idx);

        String[] ret=new String[idx.length];
        for( int i=0;i< ret.length;i++){
            ret[i]= nonfiltered[is[i]];
        }
        //logger.debug("ret for "+ name +":" + Tools.toString(ret) );

        return ret;
    }
    /**
     * Add single value to position <code>index</code> of values specified by <code>name</code>, 
     * if that values array doese not exists, will create that array in the same size as refName specified.  
     * @param name
     * @param value
     * @param refName
     * @param index note that index should be value seen from outside, not the very index of array itself
     * @since 3.0
     */
    public void setValue(String name, Object value, String refName, int index){
    	String n= name.toUpperCase(); 
    	Object values= data.get(n);
     	 Object[] v=null;
        boolean update=true;
     	 if(values ==null){
     	 	v= new Object[getRawParameterValueSize(refName)];
     	 	data.put(n, v);
     	 	//logger.debug("new array for "+ n +" with size="+ v.length);
     	 }else{
     	 	if(values.getClass().isArray()){
     	 		v=(Object[])values;
     	 	}else{
     	 		if(index >0){
     	 			throw new NDSRuntimeException("Unexpected, index must be 0 when value is not array for "+ n);
     	 		}
     	 		//replace
     	 		data.put(n, value);
     	 		//logger.debug("data "+ n +" and value "+ value);
     	 		update=false;
     	 	}
     	 }
     	if(update){
     		int realIndex=index;
	      	String selector=(String) getParameterValue(SELECTER_NAME);
	        if( selector!=null) {
	        	String[] idx=getRawParameterValues(selector);
	        	if( idx!=null){
	        		int [] is= toIntArray( idx);
	        		realIndex=is[index];
	        	}
	        }
	  	 	v[realIndex]= value;
	  	 	//logger.debug("data "+ n +" with index="+ index +" and real index " + realIndex +" , value:"+ value);
     	}
    }
    /**
     * @param idx element as int value
     */
    private int[] toIntArray(String[] idx){
        int[] ret=new int[idx.length];
        for( int i=0;i<ret.length;i++){
            ret[i]= Integer.parseInt(idx[i]);
        }
        return ret;
    }
    /**
     * Return string replacing "session attribute" in it
     * The QuerySession will be checked in event parameter named
     * "nds.query.querysession", if not found, return <param>s</param>
     * directly.
     * @param s
     * @return
     * @see nds.query.QueryUtils#replaceVariables
     */
    public String getStringWithNoVariables(String s){
    	QuerySession session=getQuerySession();
    	if(session==null) {
    		//System.out.println("-getStringWithNoVariables( is null");
    		return s;
    	}else{
    		//System.out.println( session.toDebugString());
    	}
    	return QueryUtils.replaceVariables(s, session);
    }
    public QuerySession getQuerySession(){
    	return (QuerySession) data.get("NDS.QUERY.QUERYSESSION");
    }
    /**
     * Default to Locale.CHINA, will replce by param  "JAVA.UTIL.LOCALE"
     * @return
     */
    public Locale getLocale(){
    	Locale locale= (Locale) data.get("JAVA.UTIL.LOCALE");
    	return locale==null?Locale.CHINA: locale;
    }
    /**
    *   Specifiy a logical name that is mapped to the event in
    *   in the Universal Remote Controller.
    */
   public String getEventName(){
        return "java:comp/env/event/"+eventName;
   }
    public String toString(){
        return "DefaultWebEvent["+ eventName+"]";
    }
    /**
     * Get detail information of this event
     */
    public String toDetailString(){
        //return Tools.getDetailInfo(data);
    	return getDetailInfo(data);
    }
    /**
     * There's a bug in Tools.getDetailInfo, this is a fixed one
     * Get each property value in Map, and concatenate to a readable string
     */
    public static String getDetailInfo(Map content) {
        StringBuffer s=new StringBuffer("["+ Tools.LINE_SEPARATOR);
        if( content==null)
            return "Empty";
        for( Iterator it= content.keySet().iterator(); it.hasNext();) {
            Object key= it.next();
            Object value= content.get(key);
            if (value !=null && value.getClass().isArray() ){
                try{
                value= Tools.toString( (Object[])value);
                }catch(Exception e){
                    value="array(???)";
                }
            }
            s.append(key+" = "+ value+ "("+ (value==null? "NULL": value.getClass())+")"+Tools.LINE_SEPARATOR);
        }
        s.append("]");
        return s.toString();
    }    
}
