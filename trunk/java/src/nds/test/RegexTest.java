package nds.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
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
    
    public static void main(String args[]) {
    //    test3();
    	System.out.println("²âÊÔ");
    }
}
