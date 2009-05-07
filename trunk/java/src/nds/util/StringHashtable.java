/******************************************************************
*
*$RCSfile: StringHashtable.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: StringHashtable.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.2  2003/09/29 07:37:24  yfzhu
*before removing entity beans
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.4  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.util;

import java.util.*
;
import java.util.Enumeration;
import java.util.Hashtable;
public class StringHashtable implements java.io.Serializable,Map
{

	private transient volatile Set keySet = null;	
    public StringHashtable()
    {
        this(10);
    }
    public StringHashtable(int i)
    {
        keys = new String[i];
        values = new Object[i];
        lengthUsed = new boolean[LENGTH_BITMAP_SIZE];
    }
    
    /**
     * @param maxStringLength the maximum key length
     */
    public StringHashtable(int initSize, int maxStringLength){
        keys = new String[initSize];
        values = new Object[initSize];
        lengthUsed = new boolean[maxStringLength];
    }
    public void clear()
    {
        for(int i = 0; i < count; i++)
        {
            keys[i] = null;
            values[i] = null;
        }

        count = 0;
        System.arraycopy(emptyFlags, 0, lengthUsed, 0, LENGTH_BITMAP_SIZE);
    }

    public boolean containsKey(Object key)
    {
    	
    	String s= key.toString();
        int i = s.length();
        if(!lengthUsed[i])
            return false;
        for(int j = 0; j < count; j++)
        {
            String s1 = keys[j];
            if(s1 != null && i == s1.length() && s.equals(s1))
                return true;
        }

        return false;
    }
    public boolean containsValue(Object value){
    	
        for(int j = 0; j < count; j++)
        {
            Object o = values[j];
            if(value.equals(o))
                return true;
        }

        return false;    	
    }
    public boolean isEmpty(){
    	return size()==0;
    }
    
    public int size(){
        int size=0;
		for( int i=0;i<count;i++){
			if( values[i] !=null)
				size ++;

		}
        return size;

    }
    public Object get(Object key)
    {
    	String s= key.toString();
        int i = s.length();
        if(!lengthUsed[i])
            return null;
        for(int j = 0; j < count; j++)
        {
            String s1 = keys[j];
            if(s1 != null && i == s1.length() && s.equals(s1))
                return values[j];
        }

        return null;
    }
    private int getKeyIdx(Object key)
    {
        String s=key.toString();
    	int i = s.length();
        if(!lengthUsed[i])
            return -1;
        for(int j = 0; j < count; j++)
        {
            String s1 = keys[j];
            if(s1 != null && i == s1.length() && s.equals(s1))
                return j;
        }

        return -1;
    }
    public Object put(Object key, Object obj)
    {
    	String s= key.toString();
        if(count == keys.length)
        {
            String as[] = new String[count * 2];
            Object aobj[] = new Object[count * 2];
            for(int i = 0; i < count; i++)
            {
                as[i] = keys[i];
                aobj[i] = values[i];
            }

            keys = as;
            values = aobj;
        }
        keys[count] = s;
        values[count++] = obj;
        int len = s ==null?0:s.length();
        lengthUsed[len] = true;
        return null;
    }

    public Object remove(Object s){
    
        Object v=null;
    	int i = getKeyIdx(s);
        if(i == -1)
            return null;
        keys[i] = null;
        v= values[i];
        values[i] = null;
        if(i == count - 1)
            count--;
        return v;
    }

	public Enumeration keys(){
		Vector v=new Vector();
		for( int i=0;i<count;i++){
			if( keys[i] !=null)
				v.addElement(keys[i]);
		}
		return v.elements();
	}
	public Iterator keyIterator(){
		Vector v=new Vector();
		for( int i=0;i<count;i++){
			if( keys[i] !=null)
				v.addElement(keys[i]);
		}
		return v.iterator();
	}	
	public Collection values(){
		Vector v=new Vector();
		for( int i=0;i<count;i++){
			if( values[i] !=null)
				v.addElement(values[i]);
		}
		return v;
	}
    public Set keySet() {
    	if (keySet == null)
    	    keySet = Collections.synchronizedSet(new KeySet());
    	return keySet;
        }

        private class KeySet extends AbstractSet {
            public Iterator iterator() {
            	return keyIterator();
            }
            public int size() {
                return count;
            }
            public boolean contains(Object o) {
                return containsKey(o);
            }
            public boolean remove(Object o) {
                return StringHashtable.this.remove(o) != null;
            }
            public void clear() {
            	StringHashtable.this.clear();
            }
        }
	public void putAll(Map t){
		throw new Error("Not supported yet");
	}
	public Set entrySet(){
		throw new Error("Not supported yet");
		
	}
    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer(count * 10);
        stringbuffer.append('[');
        for(int i = 0; i < count; i++)
            if(keys[i] != null)
            {
                if(i > 0)
                    stringbuffer.append(',');
                stringbuffer.append(' ');
                stringbuffer.append(keys[i]);
                stringbuffer.append('=');
                stringbuffer.append(values[i]);
            }

        stringbuffer.append(']');
        return stringbuffer.toString();
    }

    private String keys[];
    private Object values[];
    private int count;
    private boolean lengthUsed[];
    private static int LENGTH_BITMAP_SIZE;
    private static boolean emptyFlags[];

    static
    {
        LENGTH_BITMAP_SIZE = 100;
        emptyFlags = new boolean[LENGTH_BITMAP_SIZE];
    }
}
