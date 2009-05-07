/******************************************************************
*
*$RCSfile: ListSort.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/01/31 03:01:57 $
*
*$Log: ListSort.java,v $
*Revision 1.2  2006/01/31 03:01:57  Administrator
*no message
*
*Revision 1.1  2005/06/16 10:19:42  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.2  2003/03/30 08:11:40  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/


package nds.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.*;

/**the common quick sort utility
 * @author Cai Hailong
 * @version 1.1
 */
public class ListSort
{
    /**内嵌静态类,提供compare()方法
     *
     */
    static class Compare implements Comparator
    {
        private Method method = null;
        private String dataType = null;
        private boolean isByToString = false;
        private int ascending=1; // 1 for ascending, -1 for descending
        
        Compare()                   //for sort by toString()
        {
            isByToString = true;
        }
        Compare(Method m,String dt, boolean asc) //for normal sort
        {
            method = m;
            dataType = dt;
            if (dt ==null){
                GuessDateType(m.getReturnType());
            }
            ascending=asc? 1:-1;
        }
        Compare(Method m,String dt) //for normal sort
        {
            this(m,dt, true);
        }
        private void GuessDateType(Class c){
            if( c.getName().equals("java.lang.String"))dataType="s";
            else if( c.getName().equals("int"))dataType="i";
            else if( c.getName().equals("long"))dataType="l";
            else if ( c.getName().equals("java.lang.Integer"))dataType="i";
            else if ( c.getName().equals("java.lang.Long"))dataType="l";
            else if ( c.getName().equals("java.lang.Byte"))dataType="b";
            else if ( c.getName().equals("java.lang.Char"))dataType="c";
            else if ( c.getName().equals("java.lang.Double"))dataType="d";
            else if ( c.getName().equals("java.lang.Float"))dataType="f";
            else if ( c.getName().equals("java.lang.Double"))dataType="d";
            else if ( c.getName().equals("java.lang.Char"))dataType="c";
            else if ( c.getName().equals("java.util.Date"))dataType="java.util.Date";
            else throw new Error("Could not parse " +  c.getName() + " to specified date type.");
        }

        /**methos used to compare two object in a vector
         * @param o1 the first object to compare
         * @param o2 the second object to compare
         * @return an integer indicating the comparing result
         */
        public int compare(Object o1,Object o2)
        {
            if (isByToString)
            {
                if (o1.toString() != null && o2.toString() != null) return o1.toString().compareTo(o2.toString());
                else if (o1.toString() != null && o2.toString() == null) return 1;
                else if (o1.toString() == null && o2.toString() != null) return -1;
                else return 0;
            }
            else try
            {
                Object r1 = method.invoke(o1,null);
                Object r2 = method.invoke(o2,null);
                if((r1==null)) return -1 * ascending;
                if((r2==null)) return 1* ascending;
                /*if ((r1 == null)||(r2 == null))
                    throw new Error("error--the field used to sort maybe null in some object");
                */
                switch (dataType.charAt(0))
                {
                    case 'c':   return ((Character)r1).compareTo((Character)r2) *ascending;    //char
                    case 'b':   return ((Byte)r1).compareTo((Byte)r2)*ascending;              //byte
                    case 's':   return ((Short)r1).compareTo((Short)r2)*ascending;            //short
                    case 'i':   return ((Integer)r1).compareTo((Integer)r2)*ascending;        //int
                    case 'l':   return ((Long)r1).compareTo((Long)r2)*ascending;              //long
                    case 'd':   return ((Double)r1).compareTo((Double)r2)*ascending;          //double
                    case 'f':   return ((Float)r1).compareTo((Float)r2)*ascending;            //float
                    case 'j':   if (dataType.equals("java.lang.String"))
                                    return ((String)r1).compareTo((String)r2) *ascending;          //String
                                else if (dataType.equals("java.util.Date"))
                                    return ((Date)r1).compareTo((Date)r2) *ascending;
                    default:  throw new Error("error-----what is the data type for your sorting field?");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return 0;
            }
        }
    }

    /**the default sort method provided for use,sort by toString() method of Object
     * @param v the vector to sort by ascending order
     * @return the sorted vector according to toString()
     */
    public static void sort(List v)
    {
        if ((v == null)||(v.size() == 0)) return;           //vector may have no elements and not be null
        Compare cmp = new Compare();
        Collections.sort(v,cmp);
        //quickSortA(0, v.size() - 1, v,cmp);  //ascending order
    }


    /**the sort method provided for use
     * @param v the vector to sort by ascending order
     * @param _orderBy name of the property to be used as the order criteria
     * @return the sorted vector
     */
    public static void sort(List v, String _orderBy)
    {
        sort(v, _orderBy, true);
    }

    /**the sort method provided for use
     * @param v the vector to sort
     * @param _orderBy name of the property to be used as the order criteria
     * @param isAscending indicator of ascending or descending order
     */
    public static void sort(List v, String orderBy, boolean isAscending)
    {
        if ((v == null)||(v.size() == 0)) return;           //vector may have no elements and not be null
        boolean found= true;
         
        if (orderBy != null)   //by specified orderBy
        {
            Method method = null;   String dataType = null;
            try
            {                   //get the method of retrieving the field
                dataType = (v.get(0)).getClass().getDeclaredField(orderBy).getType().getName();
            }
            catch (NoSuchFieldException e2)
            {
                found=false;
                //throw new Error("error-----can not get the member of " + orderBy);
            }
            if(!found){
            try
            {
                method = (v.get(0)).getClass().getMethod("get"+ Character.toUpperCase(orderBy.charAt(0)) + orderBy.substring(1),null);
            }
            catch (NoSuchMethodException e3)
            {
                /**-----(3423432)modified by yfzhu for nds project----------**/
                    try{
                        method = (v.get(0)).getClass().getMethod("get"+orderBy.toUpperCase(),null);
                    }catch(NoSuchMethodException e4){
                    	try{
                            method = (v.get(0)).getClass().getMethod(orderBy,null);
                        }catch(Exception e5){
                                throw new Error("error-----can not get the method of for "+orderBy+"(): " + e4.getMessage());
                        }
                         
                    }
                /**---------(3423432)end-------------*/
            }
            }
            Compare cmp = new Compare(method,dataType,isAscending);
            Collections.sort(v,cmp);
            //if (isAscending) quickSortA(0, v.size() - 1, v,cmp);  //ascending order
            //else quickSortD(0, v.size() - 1, v,cmp);            //descending order
        }
    }
/*
    private static int rand(int left,int right)
    {
        return (left+right)/2;  //faster than using random number in test
    }
*/
    /**the recursive method to sort a vector by asending order
     * @param left the left index for the current sort process
     * @param right the right index for the current sort process
     * @param v the vector being sorted
     * @param cmp a Compare class object used to compare two object in v
     */
    //chl,2000/12/23, modify the algorithm to improve performance
/*    private static void quickSortA(int left, int right, Vector v, Compare cmp)
    {
        if (left >= right) return;
        swap(left,rand(left,right),v);
        int last = left;
        for (int i = left+1; i<=right; i++)
            if (cmp.compare(v.elementAt(i), v.elementAt(left)) < 0)
                swap(++last,i,v);
        swap(left,last,v);
        quickSortA(left,last - 1,v,cmp);
        quickSortA(last + 1,right,v,cmp);
    }*/
    /*
    private static void quickSortA(int left, int right, Vector v)
    {
        if (right > left)
        {
            Object o1 = v.elementAt(right);
            int i = left - 1;
            int j = right;
            while (true)
            {
                while (compare(v.elementAt(++i),o1) < 0);
                while (j > 0)
                    if (compare(v.elementAt(--j),o1) <= 0) break;
                if (i >= j) break;
                swap(i,j,v);
            }
            swap(i,right,v);
            quickSortA(left, i - 1, v);
            quickSortA(i + 1, right, v);
        }
    }
    */

    /**the recursive method to sort a vector by desending order
     * @param left the left index for the current sort process
     * @param right the right index for the current sort process
     * @param v the vector being sorted
     */
/*    private static void quickSortD(int left, int right, Vector v, Compare cmp)
    {
        if (left >= right) return;
        swap(left,rand(left,right),v);
        int last = left;
        for (int i = left+1; i<=right; i++)
            if (cmp.compare(v.elementAt(i), v.elementAt(left)) > 0)
                swap(++last,i,v);
        swap(left,last,v);
        quickSortD(left,last - 1,v,cmp);
        quickSortD(last + 1,right,v,cmp);
    }*/
    /*
    private static void quickSortD(int left, int right, Vector v)
    {
        if (right > left)
        {
            Object o1 = v.elementAt(left);
            int i = left;
            int j = right + 1;
            while (true)
            {
                while (compare(v.elementAt(--j),o1) < 0);
                while (i < v.size() - 1)
                    if (compare(v.elementAt(++i),o1) <= 0) break;
                if (i >= j) break;
                swap(i,j,v);
            }
            swap(left,j,v);
            quickSortD(left, j - 1, v);
            quickSortD(j + 1, right, v);
        }
    }
    */


    /**the method used to swap two object in a vector
     * @param i the index of the first object to swap
     * @param j the index of the second object to swap
     * @param v the vector to swap objects
     */
     /*
    private static void swap(int i, int j, Vector v)
    {
        Object tmp = v.elementAt(i);
        v.setElementAt(v.elementAt(j), i);
        v.setElementAt(tmp, j);
    }*/

}