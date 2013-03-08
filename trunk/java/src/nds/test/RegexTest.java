package nds.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
	
	String regEx = "[\\u4e00-\\u9fa5]";
	
    public static void test1(){
        String pattern = "[a-z]+";
        String text = "Now is the time";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        while (m.find()) {
            System.out.print(text.substring(m.start(), m.end()) + "*");
        }

    }
    public static void test3(){
        Pattern p = Pattern.compile( "\\<td>([^\\<>]++)\\</td>" );
        Matcher m = p.matcher( "dolphin <td>orca</td> junk\n"
        + "<td></td> empty"
        + "<td>pilot whale</td> beluga\n"
        );
        while ( m.find () )
        {
        int gc = m.groupCount();
// group 0 is the whole pattern
// run from 1 to gc, not 0 to gc-1 as is traditional.
        for ( int i=1 ; i<=gc ; i++ )
        {
        System.out.println( m.group( i )) ;
        }
        }

    }
    public static void test2(){
        String s = "^IN(\\d{8})_(\\d{6})_(\\d{2})_[+-]??(\\d{1,})_(\\d{2}).txt";
        String str = "IN20021130_021531_00_+45409000931640979_00.txt";
        Pattern pattern = Pattern.compile( s , Pattern.UNICODE_CASE );
        Matcher m=pattern.matcher(str);
        if ( m.matches() ){
            System.out.println(m.group(1));
            System.out.println(m.group(2));
        }


    }
    
    public String chinaToUnicode(String str){  
        String result="";  
        for (int i = 0; i < str.length(); i++){  
             int chr1 = (char) str.charAt(i);  
             if(chr1>=19968&&chr1<=171941){//ºº×Ö·¶Î§ \u4e00-\u9fa5 (ÖÐÎÄ)  
                 result+="\\u" + Integer.toHexString(chr1);  
             }else{  
                 result+=str.charAt(i);  
             }  
        }  
        return result;  
   }
    
    
    public boolean vd(String str){
    	  
        char[] chars=str.toCharArray(); 
        boolean isGB2312=false; 
        for(int i=0;i<chars.length;i++){
                    byte[] bytes=(""+chars[i]).getBytes(); 
                    if(bytes.length==2){ 
                                int[] ints=new int[2]; 
                                ints[0]=bytes[0]& 0xff; 
                                ints[1]=bytes[1]& 0xff; 
                                if(ints[0]>=0x81 && ints[0]<=0xFE && ints[1]>=0x40 && ints[1]<=0xFE){ 
                                            isGB2312=true; 
                                            break; 
                                } 
                    } 
        } 
        return isGB2312; 
    }
    
    
    public static void print(int temp[]) {  
    	for (int i=0; i<temp.length; i++ ) {     
    		System.out.print(temp[i] + "\t");
    	}
    }
    
    public static int[]  expandArray(int[]  a,  int  size)  {  
        if  (size  <=  a.length)  {  
                return  a;  
        }  
        int[]  t  =  new  int[size];  
        System.arraycopy(a,  0,  t,  0,  a.length);  
        return  t;  
 }  
    
    public static void main(String args[]) {
    //    test3();
    	int i1[] = {1,2};
    	int i2[]={3,4};
    	//int i2[] = {25,48,68,96,47,36,54};
    	//System.out.println(i1);
    	 //int[] c=new int[i1.length -1];
         //System.arraycopy(i1, 0, c, 0, i1.length -1);
    	//System.arraycopy(i1,0,i2,1,i2.length-1); 
    	i1=expandArray(i1,i2.length);
    	System.arraycopy(i2, 0, i1, 1, i2.length);
    	print(i1);
    	//System.out.println(i2);
    	//System.out.println("²âÊÔ");
    }
}
