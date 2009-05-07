package nds.util;

import java.util.Hashtable;
public class Sequences {
    private static Hashtable seqs; // key: String (Sequence name) value: Integer( current value of sequence)
    static {
        seqs=new Hashtable();
    }
    public static int getNextID(String name){
        Integer i;
        i= (Integer)seqs.get(name);
        if( i==null ){
            i=new Integer(1);
            seqs.put(name, i);
        }else{
        	if(i.intValue()==Integer.MAX_VALUE) i=0;
        	else
        		i = new Integer( i.intValue() + 1);
            seqs.put(name, i);
        }
        return i.intValue();
    }
}