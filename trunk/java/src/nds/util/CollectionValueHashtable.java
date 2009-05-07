/******************************************************************
*
*$RCSfile: CollectionValueHashtable.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/11/16 02:57:22 $
*
*$Log: CollectionValueHashtable.java,v $
*Revision 1.2  2005/11/16 02:57:22  Administrator
*no message
*
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

import java.util.*;
import java.util.Hashtable;
import java.util.Vector;


public class CollectionValueHashtable implements java.io.Serializable{
    private Hashtable table;// key: Object, value: Vector
    public CollectionValueHashtable() {
        table=new Hashtable();
    }
    public int size(){
        return table.size();
    }
    public void clear(){
        table.clear();
    }
    public Set keySet(){
    	return table.keySet();
    }
    public void add(Object key, Object value){
        Vector v= (Vector) table.get(key);
        if( v ==null) {
            v=new Vector();
            table.put(key, v);
        }
        v.addElement(value);
    }
    public Collection get(Object key){
        return (Collection) table.get(key);
    }

}