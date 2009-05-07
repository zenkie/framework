package nds.control.ejb.command.test;

import java.net.URLEncoder;

public class TestURLEncoder {
    public static final String DEFAULT_CHARACTER_SET = "iso-8859-1";
    public TestURLEncoder() {
    }
    /**
     * Returns a URL-encoded version of the string, including all eight bits, unlike URLEncoder, which strips the high bit.
     **/
    public String encode( String source, String characterSet ) {
        try {
            if (characterSet.equalsIgnoreCase( DEFAULT_CHARACTER_SET )) {
                return URLEncoder.encode( source, characterSet );
            } else {
                byte[] rawBytes = source.getBytes( characterSet );
                StringBuffer result = new StringBuffer( 3*rawBytes.length );
                for (int i = 0; i < rawBytes.length; i++) {
                    int candidate = rawBytes[i] & 0xff;
                    if (candidate == ' ') {
                        result.append( '+' );
                    } else if ((candidate >= 'A' && candidate <= 'Z') ||
                               (candidate >= 'a' && candidate <= 'z') ||
                               (candidate == '.') || (candidate == '-' ) ||
                               (candidate == '*') || (candidate == '_') ||
                               (candidate >= '0' && candidate <= '9')) {
                        result.append( (char) rawBytes[i] );
                    } else if (candidate < 16) {
                        result.append( "%0" ).append( Integer.toHexString( candidate ).toUpperCase() );
                    } else {
                        result.append( '%' ).append( Integer.toHexString( candidate ).toUpperCase() );
                    }
                }
                return result.toString();
            }
            } catch (java.io.UnsupportedEncodingException e) {
                return "???";    // XXX should pass the exception through as IOException ultimately
            }
        
    }

    public static void main(String[] args) {
        TestURLEncoder a = new TestURLEncoder();
        System.out.println(a.encode("import java.util.*;","iso-8859-1"));

    }
}