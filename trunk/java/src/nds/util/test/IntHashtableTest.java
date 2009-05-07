/******************************************************************
*
*$RCSfile: IntHashtableTest.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: IntHashtableTest.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.test;

import java.util.Enumeration;
import java.util.Iterator;

import junit.framework.TestCase;
import nds.util.IntHashtable;

public class IntHashtableTest  extends TestCase {
     public IntHashtableTest(String name) {
          super(name);
    }
    public void testClear(){
        IntHashtable t=new IntHashtable(5);
        // make this table more huge
        for(int i=0;i< 100;i++){
            t.put(i,i+"");
        }
        t.clear();
        assertEquals(0, t.values().size());
        assertNull(t.get(-1));
        assertNull(t.get(32));
        Enumeration enu= t.keys();
        while(enu.hasMoreElements()){
            int j= ((Integer)enu.nextElement()).intValue();
            assertEquals(-1, j);
        }

    }
    public void testValues(){
        IntHashtable t=new IntHashtable(5);
        // make this table more huge
        for(int i=0;i< 100;i++){
            t.put(i,i+"");
        }
        Iterator it= t.values().iterator();
        int j=0;
        while( it.hasNext()){
            Object obj= it.next();
            assertEquals(j+"", obj);
            j++;
        }
    }
    public void testRemove(){
        IntHashtable t=new IntHashtable(5);
        // make this table more huge
        for(int i=0;i< 100;i++){
            t.put(i,new Integer(i));
        }
        assertEquals(new Integer(55), t.get(55));
        t.remove(55);
        assertNull(t.get(55));
    }
    public void testGet(){
        IntHashtable t=new IntHashtable(5);
        // make this table more huge
        for(int i=0;i< 100;i++){
            t.put(i,new Integer(i));
        }
        for(int i=101;i< 200;i++){
            t.put(i,i+"");
        }
        for( int i=0;i< 100;i++){
            assertEquals(new Integer(i), t.get(i));
        }
        assertNull( t.get(100));
        for( int i=101;i< 200;i++){
            assertEquals(i+"", t.get(i));
        }

    }
    public void testPut(){
        IntHashtable t=new IntHashtable(5);
        assertEquals(0, t.values().size());
        boolean b=false;
        try{
            t.put(-1, "eror");
        }catch(IllegalArgumentException e){
            b=true;
        }
        assertTrue(b);
        for(int i=0;i< 3;i++){
            t.put(i,i+"");
            assertEquals(i+1, t.values().size());
        }
        for( int i=0; i< 3;i++){
            assertEquals(i+"", t.get(i));
        }
        assertNull( t.get(-1));
        // make this table more huge
        for(int i=0;i< 1000;i++){
            t.put(i,i+"");
            assertEquals(i+4, t.values().size());
        }
        for( int i=0; i< 1000;i++){
            assertEquals(i+"", t.get(i));
        }
        assertNull( t.get(-1));
    }
}