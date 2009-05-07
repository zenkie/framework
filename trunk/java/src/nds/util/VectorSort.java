// Decompiled by Jad v1.5.7f. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VectorSort.java

package nds.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class VectorSort
{
    static class Compare
        implements Comparator
    {

        public int compare(Object o1, Object o2)
        {
            if(isByToString)
            {
                if(o1.toString() != null && o2.toString() != null)
                    return o1.toString().compareTo(o2.toString());
                if(o1.toString() != null && o2.toString() == null)
                    return 1;
                return o1.toString() != null || o2.toString() == null ? 0 : -1;
            }
            try
            {
                Object r1 = method.invoke(o1, null);
                Object r2 = method.invoke(o2, null);
                if(r1 == null || r2 == null)
                    throw new Error("error--the field used to sort maybe null in some object");
                switch(dataType.charAt(0))
                {
                case 99: // 'c'
                    int j = ((Character)r1).compareTo((Character)r2);
                    return j;

                case 98: // 'b'
                    int k = ((Byte)r1).compareTo((Byte)r2);
                    return k;

                case 115: // 's'
                    int l = ((Short)r1).compareTo((Short)r2);
                    return l;

                case 105: // 'i'
                    int i1 = ((Integer)r1).compareTo((Integer)r2);
                    return i1;

                case 108: // 'l'
                    int j1 = ((Long)r1).compareTo((Long)r2);
                    return j1;

                case 100: // 'd'
                    int k1 = ((Double)r1).compareTo((Double)r2);
                    return k1;

                case 102: // 'f'
                    int l1 = ((Float)r1).compareTo((Float)r2);
                    return l1;

                case 106: // 'j'
                    if(dataType.equals("java.lang.String"))
                    {
                        int i2 = ((String)r1).compareTo((String)r2);
                        return i2;
                    }
                    if(dataType.equals("java.util.Date"))
                    {
                        int j2 = ((Date)r1).compareTo((Date)r2);
                        return j2;
                    }
                    // fall through

                case 101: // 'e'
                case 103: // 'g'
                case 104: // 'h'
                case 107: // 'k'
                case 109: // 'm'
                case 110: // 'n'
                case 111: // 'o'
                case 112: // 'p'
                case 113: // 'q'
                case 114: // 'r'
                default:
                    throw new Error("error-----what is the data type for your sorting field?");
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            int i = 0;
            return i;
        }

        private Method method;
        private String dataType;
        private boolean isByToString;

        Compare()
        {
            method = null;
            dataType = null;
            isByToString = false;
            isByToString = true;
        }

        Compare(Method m, String dt)
        {
            method = null;
            dataType = null;
            isByToString = false;
            method = m;
            dataType = dt;
        }
    }


    public VectorSort()
    {
    }

    public static void sort(Vector v)
    {
        if(v == null || v.size() == 0)
        {
            return;
        } else
        {
            Compare cmp = new Compare();
            Collections.sort(v, cmp);
            return;
        }
    }

    public static void sort(Vector v, String _orderBy)
    {
        sort(v, _orderBy, true);
    }

    public static void sort(Vector v, String orderBy, boolean isAscending)
    {
        if(v == null || v.size() == 0)
            return;
        if(orderBy != null)
        {
            Method method = null;
            String dataType = null;
            try
            {
                dataType = v.elementAt(0).getClass().getDeclaredField(orderBy).getType().getName();
            }
            catch(NoSuchFieldException e2)
            {
                throw new Error("error-----can not get the member of ".concat(String.valueOf(String.valueOf(orderBy))));
            }
            try
            {
                orderBy = String.valueOf(Character.toUpperCase(orderBy.charAt(0))) + String.valueOf(orderBy.substring(1));
                method = v.elementAt(0).getClass().getMethod("get".concat(String.valueOf(String.valueOf(orderBy))), null);
            }
            catch(NoSuchMethodException e3)
            {
                try
                {
                    orderBy = orderBy.toUpperCase();
                    method = v.elementAt(0).getClass().getMethod("get".concat(String.valueOf(String.valueOf(orderBy))), null);
                }
                catch(Exception e4)
                {
                    throw new Error(String.valueOf(String.valueOf((new StringBuffer("error-----can not get the method of get")).append(orderBy).append("(): ").append(e4.getMessage()))));
                }
            }
            Compare cmp = new Compare(method, dataType);
            Collections.sort(v, cmp);
        }
    }
}
