/******************************************************************
*
*$RCSfile: IntHashtable.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: IntHashtable.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
/**
* key: any int is allowed, except -1, which will be taken as invalid.
*/
public class IntHashtable implements java.io.Serializable{
    public IntHashtable()
    {
        this(5);
    }
    public IntHashtable(int i)
    {
        keys = new int[i];
        for(int j=0;j< i;j++) keys[j]=-1;
        values = new Object[i];
    }

    public void clear()
    {
        for(int i = 0; i < count; i++)
        {
            keys[i] = -1;
            values[i] = null;
        }

    }

    public int containsKey(int k)
    {
        for(int j = 0; j < count; j++)
        {
            int s1 = keys[j];
            if(k== s1)
                return j;
        }

        return -1;
    }

    public Object get(int k)
    {
        if(count == 0)
            return null;
        int i = containsKey(k);
        if(i == -1)
            return null;
        else
            return values[i];
    }

    public void put(int k, Object obj)
    {
    	if( k == -1 ) throw new IllegalArgumentException(" -1 is not allowed as key");
        if(count == keys.length)
        {
            int as[] = new int[count * 2];
            Object aobj[] = new Object[count * 2];
            for(int i = 0; i < count; i++)
            {
                as[i] = keys[i];
                aobj[i] = values[i];
            }

            keys = as;
            values = aobj;
        }
        keys[count] = k;
        values[count++] = obj;
    }

    public void remove(int s)
    {
        int i = containsKey(s);
        if(i == -1)
            return;
        keys[i] = -1;
        values[i] = null;
        if(i == count - 1)
            count--;
    }

	public Enumeration keys(){
		Vector v=new Vector();
		for( int i=0;i<count;i++){
			if( keys[i] !=-1)
				v.addElement(new Integer(keys[i]));
		}
		return v.elements();
	}
	public Collection keyCollection(){
		Vector v=new Vector();
		for( int i=0;i<count;i++){
			if( keys[i] !=-1)
				v.addElement(new Integer(keys[i]));
		}
		return v;
	}
	public Collection values(){
		Vector v=new Vector();
		for( int i=0;i<count;i++){
			if( values[i] !=null)
				v.addElement(values[i]);
		}
		return v;
	}
    public int size(){
        return count;
    }
    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer(count * 10);
        stringbuffer.append('[');
        for(int i = 0; i < count; i++)
            if(keys[i] != -1)
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

    private int keys[];
    private Object values[];
    private int count;


}