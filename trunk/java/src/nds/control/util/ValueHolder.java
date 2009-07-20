/******************************************************************
*
*$RCSfile: ValueHolder.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/07 11:45:46 $
*
*$Log: ValueHolder.java,v $
*Revision 1.2  2006/01/07 11:45:46  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import nds.util.*;
import java.util.*;
/**
 * Return value from EJB layer after request sending via ClientController
 */
public class ValueHolder  implements java.io.Serializable{
    /**
     * Since it will not be used by multi-thread, we use HashMap here
     */
    private HashMap data;
    public ValueHolder(){
        data=new HashMap();
    }
    /**
     * put all elements into value holder directly
     * @param m
     */
    public ValueHolder(Map m){
        data=new HashMap(m);
    }
    public Object get(String name){
        return data.get(name);
    }
    public Iterator keys(){
        return data.keySet().iterator();
    }
    /**
     * Provide remove method here to enable client side modification on recieved ValueObject
     */
    public void remove(String name){
        data.remove(name);
        
    }
    /**
     * 
     * @return false if "code" param is not 0
     */
    public boolean isOK(){
    	return  Tools.getInt(data.get("code"), 0)==0;
    }
    /**
     * Provide setter method here to enable client side modification on recieved ValueObject
     */
    public void put(String name, Object value){
        data.put(name, value);
    }
    public String toDebugString(){
    	return Tools.getDetailInfo(data);
    }
    /**
     * To Http url query part string like:
     * "nds.util.ValueHolder_message=%k3%kf&nds.util.ValueHolder_id=113"
     * @param holder
     * @param encoding
     * @param locale holder message may contains "@xxx@" like message key, which should be 
     * translated by MessageHolder 
     * @return each param in query will have prefix of WebKeys.VALUE_HOLDER_PREFIX
     * @throws java.io.IOException
     */
    public String toQueryString(ValueHolder holder, String encoding,Locale locale) throws java.io.IOException{
    	String prefix= WebKeys.VALUE_HOLDER_PREFIX;
    	
    	MessagesHolder mh= MessagesHolder.getInstance();
    	StringBuffer sb=new StringBuffer();
    	int i=0;
    	for(Iterator it= holder.keys();it.hasNext();){
    		String key=(String) it.next();
    		if(i!=0){
    			sb.append("&");
    		}
			i++;
			sb.append(prefix).append(key).append("=").
			append(java.net.URLEncoder.encode( mh.translateMessage(holder.get(key).toString(),locale),
					(encoding==null?"UTF-8":encoding)));
    	}
    	return sb.toString();
    }
    /**
     * Remove part in <param>str</param> which is constructed by #toQueryString
     * @param query  format like "a=b&c=d"
     */
    public static String  removeValueHolderPart(String query){
    	if(query==null) return null;
    	StringBuffer sb=new StringBuffer();
    	int i=0;
    	StringTokenizer st=new StringTokenizer(query,"&");
    	while(st.hasMoreTokens()){
    		String t=st.nextToken();
    		if(! t.startsWith( WebKeys.VALUE_HOLDER_PREFIX)){
    			if(i!=0) {
    				sb.append("&");
    			}
				i++;
    			sb.append(t);
    		}
    	}
    	return sb.toString();
		
    }
}
