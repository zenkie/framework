/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jspwiki.plugins;

import org.apache.log4j.Logger;
import com.ecyrd.jspwiki.*;
import java.util.*;
import java.io.StringReader;
import java.io.IOException;
import com.ecyrd.jspwiki.plugin.*;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

/**
[{ActiveDictionary method='table|column|columns|reftable_list|category_list' table='order' column='id' table='order' mask='query'}]

 
 * @author yfzhu@agilecontrol.com
 */
public class ActiveDictionary  implements WikiPlugin{
	private static Logger log = Logger.getLogger( ActiveDictionary.class );
	private static String DEFAULT_LANG= "UTF-8";
	private static WebConversation wc=new WebConversation();
	private String ndsWeb=null;
	public ActiveDictionary(){
		//wc.setExceptionsThrownOnErrorStatus(false); 
	}
	public String execute( WikiContext context, Map params )throws PluginException{
		WikiEngine engine = context.getEngine();
		WikiPage   page   = context.getPage();
		
		
		StringBuffer sb = new StringBuffer();
		if(ndsWeb==null)ndsWeb= engine.getWikiProperties().getProperty(Keys.PROPERTY_NDSWEB , Keys.DEFAULT_NDSWEB);
		try{
			String weburl= ndsWeb+ "/help/dictionary.jsp?";
			for(Iterator it= params.keySet().iterator();it.hasNext();){
				String key=(String) it.next();
				weburl+= key+"="+ params.get(key)+"&";
			}
			String lang=null;
			if( context.getHttpRequest()!=null ){
				lang=context.getHttpRequest().getHeader("Accept-Language");
			}
			if(lang==null)lang=DEFAULT_LANG;// default lang
			wc.setHeaderField("Accept-Language", lang);
			WebResponse res= wc.getResponse(weburl);
			
			if(res !=null){
				String content=res.getText();
				if( content!=null) content= content.replaceAll("\r\n","");
				sb.append(content);
			}
		}catch(Exception e){
			 log.error("Could not get dictionary", e);
	         throw new PluginException("Unable to load dictionary description (see logs)");
		}
		return  engine.textToHTML(context,sb.toString());
		
	 }
}


/**
 * Utility class to peform common String manipulation algorithms.
 */
class StringUtils {
    // States used in property parsing
    private static final int NORMAL = 0;
    private static final int SEEN_DOLLAR = 1;
    private static final int IN_BRACKET = 2;

    public static final String LINE_SEPARATOR=
        System.getProperty("line.separator","\n");
    public static final String NBSP="&nbsp;";
    public static String ISO_8859_1;
    static
    {
        String iso=System.getProperty("ISO_8859_1");
        if (iso!=null)
            ISO_8859_1=iso;
        else
        {
            try{
                new String(new byte[]{(byte)20},"ISO-8859-1");
                ISO_8859_1="ISO-8859-1";
            }
            catch(java.io.UnsupportedEncodingException e)
            {
                ISO_8859_1="ISO8859_1";
            }
        }
    }

    /**
     * Initialization lock for the whole class. Init's only happen once per
     * class load so this shouldn't be a bottleneck.
     */
    private static Object initLock = new Object();
    /**Replaces all instances of oldString with newString in line.
     */
    public static final String replace( String line, String oldString, String newString ) {
        return replace(line, oldString,newString,-1);
    }
    /**
     * Replaces instances of oldString with newString in line.
     *
     * @param line the String to search to perform replacements on
     * @param oldString the String that should be replaced by newString
     * @param newString the String that will replace all instances of oldString
     * @param maxCount the replace count , if -1 specified, replace all
     * @return a String will all instances of oldString replaced by newString
     */
    public static final String replace( String line, String oldString, String newString, int maxCount ) {
        if (line == null) {
            return null;
        }
        int count=0;
        int i=0;
        if ( ( i=line.indexOf( oldString, i ) ) >= 0 ) {
            char [] line2 = line.toCharArray();
            char [] newString2 = newString.toCharArray();
            int oLength = oldString.length();
            StringBuffer buf = new StringBuffer(line2.length);
            buf.append(line2, 0, i).append(newString2);
            i += oLength;
            count ++;
            int j = i;
            while( ( i=line.indexOf( oldString, i ) ) > 0 && ( maxCount< 0 || count < maxCount)) {
                buf.append(line2, j, i-j).append(newString2);
                i += oLength;
                j = i;
                count ++;
            }
            buf.append(line2, j, line2.length - j);
            return buf.toString();
        }
        return line;
    }

    /**
     * Replaces all instances of oldString with newString in line with the
     * added feature that matches of newString in oldString ignore case.
     *
     * @param line the String to search to perform replacements on
     * @param oldString the String that should be replaced by newString
     * @param newString the String that will replace all instances of oldString
     *
     * @return a String will all instances of oldString replaced by newString
     */
    public static final String replaceIgnoreCase(String line, String oldString,
            String newString) {
        if (line == null) {
            return null;
        }
        String lcLine = line.toLowerCase();
        String lcOldString = oldString.toLowerCase();
        int i=0;
        if ( ( i=lcLine.indexOf( lcOldString, i ) ) >= 0 ) {
            char [] line2 = line.toCharArray();
            char [] newString2 = newString.toCharArray();
            int oLength = oldString.length();
            StringBuffer buf = new StringBuffer(line2.length);
            buf.append(line2, 0, i).append(newString2);
            i += oLength;
            int j = i;
            while( ( i=lcLine.indexOf( lcOldString, i ) ) > 0 ) {
                buf.append(line2, j, i-j).append(newString2);
                i += oLength;
                j = i;
            }
            buf.append(line2, j, line2.length - j);
            return buf.toString();
        }
        return line;
    }

    /**
     * Replaces all instances of oldString with newString in line.
     * The count Integer is updated with number of replaces.
     *
     * @param line the String to search to perform replacements on
     * @param oldString the String that should be replaced by newString
     * @param newString the String that will replace all instances of oldString
     *
     * @return a String will all instances of oldString replaced by newString
     */
    public static final String replace( String line, String oldString,
                                        String newString, int[] count) {
        if (line == null) {
            return null;
        }
        int i=0;
        if ( ( i=line.indexOf( oldString, i ) ) >= 0 ) {
            int counter = 0;
            counter++;
            char [] line2 = line.toCharArray();
            char [] newString2 = newString.toCharArray();
            int oLength = oldString.length();
            StringBuffer buf = new StringBuffer(line2.length);
            buf.append(line2, 0, i).append(newString2);
            i += oLength;
            int j = i;
            while( ( i=line.indexOf( oldString, i ) ) > 0 ) {
                counter++;
                buf.append(line2, j, i-j).append(newString2);
                i += oLength;
                j = i;
            }
            buf.append(line2, j, line2.length - j);
            count[0] = counter;
            return buf.toString();
        }
        return line;
    }

    /**
     * This method takes a string which may contain HTML tags (ie, &lt;b&gt;,
     * &lt;table&gt;, etc) and converts the '&lt'' and '&gt;' characters to
     * their HTML escape sequences.
     *
     * @param input the text to be converted.
     * @return the input string with the characters '&lt;' and '&gt;' replaced
     *  with their HTML escape sequences.
     *
     * If \n found, replaced by "<br>", to avoid this, call escapeHTMLTags(String, boolean)
     *
     */
    public static final String escapeHTMLTags( String input ) {
        return escapeHTMLTags(input, true);
    }
    /**
     * @param br if br, then "\n" will be replaced by "<br>"
     */
    public static final String escapeHTMLTags( String input , boolean br ) {
    	//Check if the string is null or zero length -- if so, return
        //what was sent in.
        if( input == null || input.length() == 0 ) {
            return input;
        }
        /* old method, marked up and replace with following method since 2.0
         * 
        //Use a StringBuffer in lieu of String concatenation -- it is
        //much more efficient this way.
        StringBuffer buf = new StringBuffer(input.length());
        char ch = ' ';
        for( int i=0; i<input.length(); i++ ) {
            ch = input.charAt(i);
            if( ch == '<' ) {
                buf.append("&lt;");
            } else if( ch == '>' ) {
                buf.append("&gt;");
            } else if( br && ch == '\n'){
                buf.append("<br>");
            } else {
                buf.append( ch );
            }
        }
        return buf.toString();
        */
        input = replace(input, "&", "&amp;");
        input = replace(input, "\"", "&quot;");
        input = replace(input, "<", "&lt;");
        input = replace(input, ">", "&gt;");
        input = replace(input, "\n", "<br>");
        return input;
    }
    /**
     * 
     * @param input
     * @return string that can be used in sql part
     */
    public static String escapeForSQL(String input){
    	if( input == null || input.length() == 0 ) {
            return "";
        }
    	input = replace(input, "'", "''");
    	input = replace(input, "\\", "\\\\");
    	return input;
	}    
    /**
     * Encode the specified string into html <input value=""> tags
     * Note if " appear in input string, it will be replace by &quot;
     * @param input the specified string
     */
    public static final String encodeIntoHTMLInputValue( String input) {
        //Check if the string is null or zero length -- if so, return
        //what was sent in.
        if( input == null || input.length() == 0 ) {
            return input;
        }
        //Use a StringBuffer in lieu of String concatenation -- it is
        //much more efficient this way.
        StringBuffer buf = new StringBuffer(input.length());
        char ch = ' ';
        for( int i=0; i<input.length(); i++ ) {
            ch = input.charAt(i);
            if( ch == '<' ) {
                buf.append("&lt;");
            } else if( ch == '>' ) {
                buf.append("&gt;");
            } else if( ch == '"'){
                buf.append("&quot;");
            } else {
                buf.append( ch );
            }
        }
        return buf.toString();
    }
    /**
     * Used by the hash method.
     */
    private static MessageDigest digest = null;

    /**
     * Hashes a String using the Md5 algorithm and returns the result as a
     * String of hexadecimal numbers. This method is synchronized to avoid
     * excessive MessageDigest object creation. If calling this method becomes
     * a bottleneck in your code, you may wish to maintain a pool of
     * MessageDigest objects instead of using this method.
     * <p>
     * A hash is a one-way function -- that is, given an
     * input, an output is easily computed. However, given the output, the
     * input is almost impossible to compute. This is useful for passwords
     * since we can store the hash and a hacker will then have a very hard time
     * determining the original password.
     * <p>
     * In Jive, every time a user logs in, we simply
     * take their plain text password, compute the hash, and compare the
     * generated hash to the stored hash. Since it is almost impossible that
     * two passwords will generate the same hash, we know if the user gave us
     * the correct password or not. The only negative to this system is that
     * password recovery is basically impossible. Therefore, a reset password
     * method is used instead.
     *
     * @param data the String to compute the hash of.
     * @return a hashed version of the passed-in String
     */
    public synchronized static final String hash(String data) {
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsae) {
                System.err.println("Failed to load the MD5 MessageDigest. " +
                                   "Jive will be unable to function normally.");
                nsae.printStackTrace();
            }
        }
        //Now, compute hash.
        digest.update(data.getBytes());
        return toHex(digest.digest());
    }

    /**
     * Turns an array of bytes into a String representing each byte as an
     * unsigned hex number.
     * <p>
     * Method by Santeri Paavolainen, Helsinki Finland 1996<br>
     * (c) Santeri Paavolainen, Helsinki Finland 1996<br>
     * Distributed under LGPL.
     *
     * @param hash an rray of bytes to convert to a hex-string
     * @return generated hex string
     */
    public static final String toHex (byte hash[]) {
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++) {
            if (((int) hash[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) hash[i] & 0xff, 16));
        }
        return buf.toString();
    }

    /**
     * Converts a line of text into an array of lower case words. Words are
     * delimited by the following characters: , .\r\n:/\+
     * <p>
     * In the future, this method should be changed to use a
     * BreakIterator.wordInstance(). That class offers much more fexibility.
     *
     * @param text a String of text to convert into an array of words
     * @return text broken up into an array of words.
     */
    public static final String [] toLowerCaseWordArray(String text) {
        if (text == null || text.length() == 0) {
            return new String[0];
        }
        StringTokenizer tokens = new StringTokenizer(text, " ,\r\n.:/\\+");
        String [] words = new String[tokens.countTokens()];
        for (int i=0; i<words.length; i++) {
            words[i] = tokens.nextToken().toLowerCase();
        }
        return words;
    }

    /**
     * A list of some of the most common words. For searching and indexing, we
     * often want to filter out these words since they just confuse searches.
     * The list was not created scientifically so may be incomplete :)
     */
    private static final String [] commonWords =  new String [] {
                "a", "and", "as", "at", "be", "do", "i", "if", "in", "is", "it", "so",
                "the", "to"
            };
    private static Map commonWordsMap = null;

    /**
     * Returns a new String array with some of the most common English words
     * removed. The specific words removed are: a, and, as, at, be, do, i, if,
     * in, is, it, so, the, to
     */
    public static final String [] removeCommonWords(String [] words) {
        //See if common words map has been initialized. We don't statically
        //initialize it to save some memory. Even though this a small savings,
        //it adds up with hundreds of classes being loaded.
        if (commonWordsMap == null) {
            synchronized(initLock) {
                if (commonWordsMap == null) {
                    commonWordsMap = new HashMap();
                    for (int i=0; i<commonWords.length; i++) {
                        commonWordsMap.put(commonWords[i], commonWords[i]);
                    }
                }
            }
        }
        //Now, add all words that aren't in the common map to results
        ArrayList results = new ArrayList(words.length);
        for (int i=0; i<words.length; i++) {
            if (!commonWordsMap.containsKey(words[i])) {
                results.add(words[i]);
            }
        }
        return (String[])results.toArray(new String[results.size()]);
    }

    /**
     * Pseudo-random number generator object for use with randomString().
     * The Random class is not considered to be cryptographically secure, so
     * only use these random Strings for low to medium security applications.
     */
    private static Random randGen = null;

    /**
     * Array of numbers and letters of mixed case. Numbers appear in the list
     * twice so that there is a more equal chance that a number will be picked.
     * We can use the array to get a random number or letter by picking a random
     * array index.
     */
    private static char[] numbersAndLetters = null;

    /**
     * Returns a random String of numbers and letters of the specified length.
     * The method uses the Random class that is built-in to Java which is
     * suitable for low to medium grade security uses. This means that the
     * output is only pseudo random, i.e., each number is mathematically
     * generated so is not truly random.<p>
     *
     * For every character in the returned String, there is an equal chance that
     * it will be a letter or number. If a letter, there is an equal chance
     * that it will be lower or upper case.<p>
     *
     * The specified length must be at least one. If not, the method will return
     * null.
     *
     * @param length the desired length of the random String to return.
     * @return a random String of numbers and letters of the specified length.
     */
    public static final String randomString(int length) {
        if (length < 1) {
            return null;
        }
        //Init of pseudo random number generator.
        if (randGen == null) {
            synchronized (initLock) {
                if (randGen == null) {
                    randGen = new Random();
                    //Also initialize the numbersAndLetters array
                    numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" +
                                         "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
                }
            }
        }
        //Create a char buffer to put random letters and numbers in.
        char [] randBuffer = new char[length];
        for (int i=0; i<randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
        }
        return new String(randBuffer);
    }

    /**
     * Intelligently chops a String at a word boundary (whitespace) that occurs
     * at the specified index in the argument or before. However, if there is a
     * newline character before <code>length</code>, the String will be chopped
     * there. If no newline or whitespace is found in <code>string</code> up to
     * the index <code>length</code>, the String will chopped at <code>length</code>.
     * <p>
     * For example, chopAtWord("This is a nice String", 10) will return
     * "This is a" which is the first word boundary less than or equal to 10
     * characters into the original String.
     *
     * @param string the String to chop.
     * @param length the index in <code>string</code> to start looking for a
     *       whitespace boundary at.
     * @return a substring of <code>string</code> whose length is less than or
     *       equal to <code>length</code>, and that is chopped at whitespace.
     */
    public static final String chopAtWord(String string, int length) {
        if (string == null) {
            return string;
        }

        char [] charArray = string.toCharArray();
        int sLength = string.length();
        if (length < sLength) {
            sLength = length;
        }

        //First check if there is a newline character before length; if so,
        //chop word there.
        for (int i=0; i<sLength-1; i++) {
            //Windows
            if (charArray[i] == '\r' && charArray[i+1] == '\n') {
                return string.substring(0, i);
            }
            //Unix
            else if (charArray[i] == '\n') {
                return string.substring(0, i);
            }
        }
        //Also check boundary case of Unix newline
        if (charArray[sLength-1] == '\n') {
            return string.substring(0, sLength-1);
        }

        //Done checking for newline, now see if the total string is less than
        //the specified chop point.
        if (string.length() < length) {
            return string;
        }

        //No newline, so chop at the first whitespace.
        for (int i = length-1; i > 0; i--) {
            if (charArray[i] == ' ') {
                return string.substring(0, i).trim();
            }
        }

        //Did not find word boundary so return original String chopped at
        //specified length.
        return string.substring(0, length);
    }

    /**
     * Highlights words in a string. Words matching ignores case. The actual
     * higlighting method is specified with the start and end higlight tags.
     * Those might be beginning and ending HTML bold tags, or anything else.
     *
     * @param string the String to highlight words in.
     * @param words an array of words that should be highlighted in the string.
     * @param startHighlight the tag that should be inserted to start highlighting.
     * @param endHighlight the tag that should be inserted to end highlighting.
     * @return a new String with the specified words highlighted.
     */
    public static final String highlightWords(String string, String[] words,
            String startHighlight, String endHighlight) {
        if (string == null || words == null ||
                startHighlight == null || endHighlight == null) {
            return null;
        }

        //Iterate through each word.
        for (int x=0; x<words.length; x++) {
            //we want to ignore case.
            String lcString = string.toLowerCase();
            //using a char [] is more efficient
            char [] string2 = string.toCharArray();
            String word = words[x].toLowerCase();

            //perform specialized replace logic
            int i=0;
            if ( ( i=lcString.indexOf( word, i ) ) >= 0 ) {
                int oLength = word.length();
                StringBuffer buf = new StringBuffer(string2.length);

                //we only want to highlight distinct words and not parts of
                //larger words. The method used below mostly solves this. There
                //are a few cases where it doesn't, but it's close enough.
                boolean startSpace = false;
                char startChar = ' ';
                if (i-1 > 0) {
                    startChar = string2[i-1];
                    if (!Character.isLetter(startChar)) {
                        startSpace = true;
                    }
                }
                boolean endSpace = false;
                char endChar = ' ';
                if (i+oLength<string2.length) {
                    endChar = string2[i+oLength];
                    if (!Character.isLetter(endChar))  {
                        endSpace = true;
                    }
                }
                if ((startSpace && endSpace) || (i==0 && endSpace)) {
                    buf.append(string2, 0, i);
                    if (startSpace && startChar==' ') {
                        buf.append(startChar);
                    }
                    buf.append(startHighlight);
                    buf.append(string2, i, oLength).append(endHighlight);
                    if (endSpace && endChar==' ') {
                        buf.append(endChar);
                    }
                } else {
                    buf.append(string2, 0, i);
                    buf.append(string2, i, oLength);
                }

                i += oLength;
                int j = i;
                while( ( i=lcString.indexOf( word, i ) ) > 0 ) {
                    startSpace = false;
                    startChar = string2[i-1];
                    if (!Character.isLetter(startChar)) {
                        startSpace = true;
                    }

                    endSpace = false;
                    if (i+oLength<string2.length) {
                        endChar = string2[i+oLength];
                        if (!Character.isLetter(endChar))  {
                            endSpace = true;
                        }
                    }
                    if ((startSpace && endSpace) || i+oLength==string2.length) {
                        buf.append(string2, j, i-j);
                        if (startSpace && startChar==' ') {
                            buf.append(startChar);
                        }
                        buf.append(startHighlight);
                        buf.append(string2, i, oLength).append(endHighlight);
                        if (endSpace && endChar==' ') {
                            buf.append(endChar);
                        }
                    } else {
                        buf.append(string2, j, i-j);
                        buf.append(string2, i, oLength);
                    }
                    i += oLength;
                    j = i;
                }
                buf.append(string2, j, string2.length - j);
                string = buf.toString();
            }
        }
        return string;
    }

    /**
     * Escapes all necessary characters in the String so that it can be used
     * in an XML doc.
     *
     * @param string the string to escape.
     * @return the string with appropriate characters escaped.
     */
    public static final String escapeForXML(String string) {
        //Check if the string is null or zero length -- if so, return
        //what was sent in.
        if (string == null || string.length() == 0 ) {
            return string;
        }
        char [] sArray = string.toCharArray();
        StringBuffer buf = new StringBuffer(sArray.length);
        char ch;
        for (int i=0; i<sArray.length; i++) {
            ch = sArray[i];
            if(ch == '<') {
                buf.append("&lt;");
            } else if(ch == '>') {
                buf.append("&gt;");
            } else if (ch == '"') {
                buf.append("&quot;");
            } else if (ch == '\'') {
               buf.append("&apos;");
            } else if (ch == '&') {
                buf.append("&amp;");
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    /**
     * Unescapes the String by converting XML escape sequences back into normal
     * characters.
     *
     * @param string the string to unescape.
     * @return the string with appropriate characters unescaped.
     */
    public static final String unescapeFromXML(String string) {
        string = replace(string, "&lt;", "<");
        string = replace(string, "&gt;", ">");
        string = replace(string, "&quot;", "\"");
        string = replace(string, "&apos;", "'");
        return replace(string, "&amp;", "&");
    }
    /**
     * Check whether <code>str</code> is tailed with <code>tail</code>
     * @param str the string to be searched
     * @param tail the substring that should be on the end of the whole string
     * @return false if not in the last end.
     */
    public static boolean isTailedWith(String str, String tail) {
        /* nmdemo following is a big bug,sample str="Employee", tail="_AUDITSHT"
        if (str.lastIndexOf(tail) ==( str.length()- tail.length()))
        */
        int li=str.lastIndexOf(tail);
        return ( li==( str.length()- tail.length()) && li>=0);
    }

    /**
    *	Get first subString of line, satisfied with begin string identical to begin, and end to end
    *	@param includeTag if true, the begin string and end string will be included in return value
    *   @return null if can not find
    */
    public static String getSubString(String line, String begin,String end, boolean includeTag) {
        if (line == null) {
            return null;
        }
        int i=0,j=0;
        if ( ( i=line.indexOf( begin, 0 ) ) >= 0 ) {
            if( (j=line.indexOf(end,i+ begin.length()))>=0) {
                if(includeTag)
                    return line.substring(i,j+ end.length() );
                else
                    return line.substring(i+begin.length(), j);
            }
        }
        return null;
    }
    /**
    * Remove all c from old
    *
    */
    public static String removeChar(String old, char c) {
        if ( old==null) return null;
        StringBuffer buf=new StringBuffer(old);
        for( int i=buf.length() -1;i>=0 ;i--) {
            if( buf.charAt(i) ==c)
                buf.deleteCharAt(i);
        }
        return buf.toString();
    }
    /**
    *	Replace old string, which has any elements listed in refTable's propertyNames,
    *   with the substitute string in refTable value list related to the property name.
    *
    *   We will search in the old string char by char for <code>mark</code>, which is the
    *   start char and end char of the to-be replaced string in refTable. and replace them
    *   with the substitute string.
    *
    *	@param old the string contains many items to be replaced
    *	@param refTable the string list signs the string items and their substitute ones
    *	@param mark each to-be replaced string item in refTable should have this mark char
    *	 		as both start and end
    *	@return the new replaced string
    */
    public static String replaceStrings(String old, Properties refTable, char mark) {
        //A simple one, will enhance when I have time
        Iterator it= refTable.keySet().iterator();
        String name,value;
        while(it.hasNext()) {
            name=(String)it.next();
            value= refTable.getProperty(name);
            old=StringUtils.replace(old, name, value);
        }
        return old;
    }
    /**
     * return the string of throwalbe stack trace
     */
    public static String toString(Throwable throwable) {
        if(throwable == null)
            throwable = new Throwable("[Null exception passed, creating stack trace for offending caller]");
        java.io.ByteArrayOutputStream bytearrayoutputstream = new java.io.ByteArrayOutputStream();
        throwable.printStackTrace(new java.io.PrintStream(bytearrayoutputstream));
        return bytearrayoutputstream.toString();
    }
    /**
     * 解析数字字符串，数字之间用分隔符分隔
     * 返回int型数组
     */
    public static int[] parseIntArray(String s,String chr) {
        try {
            ArrayList is= new ArrayList();
            StringTokenizer st=new StringTokenizer(s,chr);
            while(st.hasMoreTokens()) {
                Integer v=new Integer(st.nextToken());
                is.add(v);
            }
            int[] ret=new int[is.size()];
            for(int i=0;i<ret.length;i++) {
                ret[i]=( (Integer)is.get(i)).intValue();
            }
            return ret;
        } catch(Exception e) {
            //logger.debug("can not parse '"+s+"'as int[]");
            return null;
        }
    }

    public static String displayMoney(String s, String money){
        if(s == null || s.trim().equals(""))
            s = "0";
        double value = Double.valueOf(s).doubleValue();
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("#,#00.00#");
        String res = "<div align=\"right\">" + df.format(value) + "</div>";
        return res;
    }
    /** Go through the input string and replace any occurance of ${p} with
     *the System.getProperty(p) value. If there is no such property p defined, then
     * the ${p} reference will remain unchanged.
     *@return the input string with all property references replaced
     */
    public static String replaceProperties(final String string)
    {
       final char[] chars = string.toCharArray();
       StringBuffer buffer = new StringBuffer();
       boolean properties = false;
       int state = NORMAL;
       int start = 0;
       for (int i = 0; i < chars.length; ++i)
       {
          char c = chars[i];

          // Dollar sign outside brackets
          if (c == '$' && state != IN_BRACKET)
             state = SEEN_DOLLAR;

          // Open bracket immediatley after dollar
          else if (c == '{' && state == SEEN_DOLLAR)
          {
             buffer.append(string.substring(start, i-1));
             state = IN_BRACKET;
             start = i-1;
          }

          // No open bracket after dollar
          else if (state == SEEN_DOLLAR)
             state = NORMAL;

          // Closed bracket after open bracket
          else if (c == '}' && state == IN_BRACKET)
          {
             // No content
             if (start+2 == i)
                buffer.append("${}"); // REVIEW: Correct?

             // Collect the system property
             else
             {
                String value = System.getProperty(string.substring(start+2, i));
                if( value != null )
                {
                   properties = true;
                   buffer.append(value);
                }
             }
             start = i+1;
             state = NORMAL;
          }
       }

       // No properties
       if (properties == false)
          return string;

       // Collect the trailing characters
       if (start != chars.length)
          buffer.append(string.substring(start, chars.length));

       // Done
       return buffer.toString();
    }

	public static String[] split(String s) {
		return split(s, ",");
	}

	public static String[] split(String s, String delimiter) {
		if (s == null || delimiter == null) {
			return new String[0];
		}

		s = s.trim();

		if (!s.endsWith(delimiter)) {
			s += delimiter;
		}

		if (s.equals(delimiter)) {
			return new String[0];
		}

		List nodeValues = new ArrayList();

		if (delimiter.equals("\n") || delimiter.equals("\r")) {
			try {
				BufferedReader br = new BufferedReader(new StringReader(s));

				String line = null;

				while ((line = br.readLine()) != null) {
					nodeValues.add(line);
				}

				br.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		else {
			int offset = 0;
			int pos = s.indexOf(delimiter, offset);

			while (pos != -1) {
				nodeValues.add(s.substring(offset, pos));

				offset = pos + delimiter.length();
				pos = s.indexOf(delimiter, offset);
			}
		}

		return (String[])nodeValues.toArray(new String[0]);
	}

	public static boolean[] split(String s, String delimiter, boolean x) {
		String[] array = split(s, delimiter);
		boolean[] newArray = new boolean[array.length];

		for (int i = 0; i < array.length; i++) {
			boolean value = x;

			try {
				value = Boolean.valueOf(array[i]).booleanValue();
			}
			catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static double[] split(String s, String delimiter, double x) {
		String[] array = split(s, delimiter);
		double[] newArray = new double[array.length];

		for (int i = 0; i < array.length; i++) {
			double value = x;

			try {
				value = Double.parseDouble(array[i]);
			}
			catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static float[] split(String s, String delimiter, float x) {
		String[] array = split(s, delimiter);
		float[] newArray = new float[array.length];

		for (int i = 0; i < array.length; i++) {
			float value = x;

			try {
				value = Float.parseFloat(array[i]);
			}
			catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static int[] split(String s, String delimiter, int x) {
		String[] array = split(s, delimiter);
		int[] newArray = new int[array.length];

		for (int i = 0; i < array.length; i++) {
			int value = x;

			try {
				value = Integer.parseInt(array[i]);
			}
			catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static long[] split(String s, String delimiter, long x) {
		String[] array = split(s, delimiter);
		long[] newArray = new long[array.length];

		for (int i = 0; i < array.length; i++) {
			long value = x;

			try {
				value = Long.parseLong(array[i]);
			}
			catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static short[] split(String s, String delimiter, short x) {
		String[] array = split(s, delimiter);
		short[] newArray = new short[array.length];

		for (int i = 0; i < array.length; i++) {
			short value = x;

			try {
				value = Short.parseShort(array[i]);
			}
			catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}
	public static String shorten(String s) {
		return shorten(s, 20);
	}

	public static String shorten(String s, int length) {
		return shorten(s, length, "..");
	}

	public static String shorten(String s, String suffix) {
		return shorten(s, 20, suffix);
	}

	public static String shorten(String s, int length, String suffix) {
		if (s == null || suffix == null)  {
			return null;
		}

		if (s.length() > length) {
			s = s.substring(0, length) + suffix;
		}

		return s;
	}
	public static String shortenInBytes(String s, int bytes) {
		if (s == null || "".equals(s) )  {
			return "";
		}
		int len= s.length();
		int b= s.getBytes().length;
		return StringUtils.shorten(s,(int) ((bytes*len)/b), "...");
		
	}
	
	
}
