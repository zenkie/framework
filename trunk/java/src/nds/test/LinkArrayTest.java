package nds.test;

import java.util.ArrayList;
import java.util.Hashtable;

public class LinkArrayTest {
    private Hashtable htColumns=new Hashtable();
    public LinkArrayTest() {
    }
    public void test(){
        ArrayList al=new ArrayList();
        ArrayList c=new ArrayList();
        Column[] cs=new Column[10];
        cs[0]= new Column("c0",0);
        cs[1]= new Column("c1",1);
        cs[2]= new Column("c2",2);
        cs[3]= new Column("c3",3);
        cs[4]= new Column("c4",4);
        cs[5]= new Column("c5",5);
        cs[6]= new Column("c6",6);
        cs[7]= new Column("c7",7);
        cs[8]= new Column("c8",8);
        cs[9]= new Column("c9",9);

        al.add(cs[1]);
        al.add(cs[7]);
        al.add(cs[8]);

        c=new ArrayList();
        c.add(cs[2]);
        c.add(cs[4]);
        htColumns.put(cs[1], c);
        c=new ArrayList();
        c.add(cs[3]);
        htColumns.put(cs[2], c);
        c=new ArrayList();
        c.add(cs[5]);
        c.add(cs[6]);
        htColumns.put(cs[4], c);
        c=new ArrayList();
        c.add(cs[9]);
        htColumns.put(cs[8], c);
        ArrayList ret=convertToTable(al);
        for(int i=0;i< ret.size();i++){
            ArrayList t= (ArrayList)ret.get(i);
            for(int j=0;j< t.size();j++){
                System.out.print(","+ t.get(j));
            }
            System.out.print("\n");

        }
    }
    /**
    * Convert refereed colmn links to regular 2 dimension array
    * @param linkArray has elements linked like:
    *   ----c1----c2----c3
    *    |     |--c4----c5
    *    |           |--c6
    *    |--c7
    *    |--c8----c9
    * @return the regular 2 dimension array has elements like:
    *   ----c1----c2---c3
    *    |  NA --c4----c5
    *    |  NA   NA  --c6
    *    |--c7
    *    |--c8---c9
    *
    */
    private ArrayList convertToTable(ArrayList linkArray){
        ArrayList al=new ArrayList();
        for(int i=0;i< linkArray.size();i++){
            ArrayList row=new ArrayList();
            row.add((Column)linkArray.get(i));
            al.add(row);
            addToRegularTable(al, (Column)linkArray.get(i),0);
        }
        return al;
    }
    private void addToRegularTable(ArrayList rt, Column column, int col){
        ArrayList row=(ArrayList) rt.get(rt.size()-1);
        ArrayList al= (ArrayList)htColumns.get(column);
        if( al ==null) return;
        if( al.size()>0){
            row.add( (Column)al.get(0));
            addToRegularTable(rt, (Column)al.get(0), col+1);
        }
        for(int i=1;i< al.size();i++){
            // add a blank row, with elements befeore col are null
            row=new ArrayList();
            for(int j=0;j<col+1;j++){
                row.add(null);
            }
            row.add((Column)al.get(i));
            rt.add(row);
            addToRegularTable(rt, (Column)al.get(i), col+1);
        }
    }
    public static void main(String[] args) {
    //    System.out.println(java.net.URLEncoder.encode("ÖÐÎÄ"));
        if( true) return;
        LinkArrayTest linkArrayTest1 = new LinkArrayTest();
        linkArrayTest1.test();
    }
 class Column{
     private String name;
     private int hc;
     public Column(String n, int hashCode ){
         name=n;
         hc=hashCode;
     }
     public String toString(){ return name;}
     public int hashCode(){
         return hc;
     }
     public boolean equals(Object obj){
         if(( obj instanceof Column ) && ((Column)obj).name.equals(name)) return true;
         return false;
     }
 }
 }