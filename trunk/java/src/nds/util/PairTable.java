/*
 * 
 */
package nds.util;

import java.util.*;
import org.json.*;
/**
 * 支持小型的Hashtable，所不同的是，Key,Value的集合是以传人的方式被读出来，不象
 * Hashtable不能保证原有顺序
 * 目前不支持多线程， Key的相同的标准是 equal
 */
public class PairTable  implements java.io.Serializable,JSONString{
    private ArrayList keys;
    private ArrayList values;
    public final static PairTable EMPTY_PAIRTABLE=new PairTable();
    public PairTable()
    {
        keys=new ArrayList();
        values=new ArrayList();
    }
    public HashMap toHashMap(){
    	HashMap map=new HashMap();
    	for(int i=0;i< keys.size();i++){
    		map.put( keys.get(i), values.get(i));
    	}
    	return map;
    }
    public void clear()
    {
        keys.clear();
        values.clear();

    }
    /**
     * PairTable is ordered one, so can specify position of 
     * the element in table
     * @param pos pos should be less than keys.size()
     * @return null if pos is not in list;
     */
    public Object getKey(int pos){
    	if( pos<0 || pos> keys.size()) return null;
    	return keys.get(pos);
    }
    /**
     * 
     * @param k
     * @return 
     */
    public boolean containsKey(Object k)
    {
        for(int j = 0; j < keys.size(); j++)
        {
            Object s1 = keys.get(j);
            if( (k==null && s1==null ) || (k!=null && k.equals(s1)))
                return true;
        }

        return false;
    }
    /**
     * 
     * @param k
     * @return -1 if not found k in the key list
     */
    private int getKeyPos(Object k){
    	for(int j = 0; j < keys.size(); j++)
        {
            Object s1 = keys.get(j);
            if( (k==null && s1==null ) || (k!=null && k.equals(s1)))
                return j;
        }

        return -1;
    }
    public Object get(Object k)
    {
        if(keys.size() == 0)
            return null;
        int i = getKeyPos(k);
        if(i == -1)
            return null;
        else
            return values.get(i);
    }
    public Object getValue(int i){
    	return values.get(i);
    }
    /**
     * 
     * @param value
     * @return any key whose value is equal to specified <param>value</param>
     */
    public Object getKey(Object value)
    {
        if(keys.size() == 0)
            return null;
    	for(int j = 0; j < values.size(); j++)
        {
            Object s1 = values.get(j);
            if(value.equals(s1))
                return keys.get(j);
        }

        return null;
    }
    /**
     * 
     * @param k
     * @param obj
     */
    public synchronized void put(Object k, Object obj)
    {
    	if( k == null ) throw new IllegalArgumentException(" Null is not allowed as key");
    	int i = getKeyPos(k);
    	if( i ==-1){
    		keys.add(k);
    		values.add(obj);
    	}else{
    		keys.set(i, k);
    		values.set(i,obj);
    	}
    }

    public synchronized void remove(Object key)
    {
    	int i = getKeyPos(key);
        if(i == -1)
            return;
        keys.remove(i);
        values.remove(i);
    }

	public Iterator keys(){
		return keys.iterator();
	}
	public List keyList(){
		return keys;
	}
	/**
	 * Reverse as input order
	 * @return
	 */
	public Iterator keysReversed(){
		ArrayList al=new ArrayList();
		for(int i= keys.size()-1;i>-1;i--) al.add( keys.get(i));
		return al.iterator();
	}
	public Iterator values(){
		return values.iterator();
	}
	public List valueList(){
		return values;
	}
    public int size(){
        return keys.size();
    }
    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer(keys.size() * 10);
        stringbuffer.append('[');
        for(int i = 0; i < keys.size(); i++){
                stringbuffer.append(' ');
                stringbuffer.append(keys.get(i));
                stringbuffer.append('=');
                stringbuffer.append(values.get(i));
            }

        stringbuffer.append(']');
        return stringbuffer.toString();
    }
    /**
     * Different with url is no url.encoding 
     * @param namespace
     * @return 
     */
    public String toParseString(String namespace){
        StringBuffer stringbuffer = new StringBuffer();
        if(namespace==null)namespace="";
        for(int i = 0; i < keys.size(); i++){
    		if(i>0) stringbuffer.append('&');
            stringbuffer.append(namespace).append(keys.get(i));
            stringbuffer.append('=');
            stringbuffer.append( values.get(i).toString());
        }
        return stringbuffer.toString();    	
    }
    /**
     * to url query string, with name prefixed by <param>namespace</param>
     * for instance, "a=b&c=d", if namespace="fixed_columns_" then will be
     * "fixed_columns_a=b&fixed_columns_c=d"
     * You can encode to url using java.net.URLEncoder
     * @param namespace
     * @return 
     */
    public String toURLQueryString(String namespace){
        StringBuffer stringbuffer = new StringBuffer();
        if(namespace==null)namespace="";
        for(int i = 0; i < keys.size(); i++){
        		if(i>0) stringbuffer.append('&');
                stringbuffer.append(namespace+keys.get(i));
                stringbuffer.append('=');
                stringbuffer.append(StringUtils.escapeHTMLTags( values.get(i).toString()));
        }
        return stringbuffer.toString();    	
    }
    /**
     * 
     * @param pt
     * @return In format like [[a,133],[b,133],[c,321]], abc are keys, 133 are values
     * @throws JSONException
     */
	public Object toJSONArray() throws JSONException{
		JSONArray ja=new JSONArray();
		for(int i=0;i< this.size(); i++){
			JSONArray je= new JSONArray();
			je.put(this.getKey(i));
			je.put(this.getValue(i));
			ja.put(je);
		}
		return ja;
	}
	/**
	 * @return in format like {"a":133, "b":133, "c" 321}
	 */
    public String toJSONString(){
    	JSONObject jo=new JSONObject();
    	for(int i = 0; i < keys.size(); i++){
    		try{
    			jo.put(keys.get(i).toString(),values.get(i));
    		}catch(JSONException e){
    			System.err.println("Error in PairTable.toJSONString:"+ e);
    		}
    	}
    	return jo.toString(); 
    }
    /**
     * 
     * @param query
     * @param namespace
     * @return PairTable with key: Integer (normally it will be String) 
     */
    public static PairTable parseIntTable(String query,String namespace){
    	PairTable pt=new PairTable();
    	if(Validator.isNull(query)) return pt;
    	StringTokenizer st=new StringTokenizer(query,"&");
    	String p , name, value;
    	int pos;
    	while ( st.hasMoreTokens()){
    		p= st.nextToken();
    		pos=p.indexOf("=") ;
    		name= p.substring(0, pos );
    		value= p.substring( pos+1 );
    		if(namespace !=null && namespace.length()>0){
    			name= StringUtils.replace(name, namespace,"",1);
    		}
    		pt.put( new Integer(name), value);
		}
    	return pt;    	
    }
    
    /**
     * Parse "fixed_columns_a=b&fixed_columns_c=d"
     * @param query
     * @param namespace
     * @return
     */
    public static PairTable parse(String query,String namespace){
    	PairTable pt=new PairTable();
    	if(Validator.isNull(query)) return pt;
    	StringTokenizer st=new StringTokenizer(query,"&");
    	String p , name, value;
    	int pos;
    	while ( st.hasMoreTokens()){
    		p= st.nextToken();
    		pos=p.indexOf("=") ;
    		name= p.substring(0, pos );
    		value= p.substring( pos+1 );
    		if(namespace !=null && namespace.length()>0){
    			name= StringUtils.replace(name, namespace,"",1);
    		}
    		pt.put(name, value);
		}
    	return pt;
    	
    }
    
    public static void main(String[] args){
    	System.out.println(PairTable.parse("fixed_columns_a=b&fixed_columns_c=d", "fixed_columns_").toString());
    }


}