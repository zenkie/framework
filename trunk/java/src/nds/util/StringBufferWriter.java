/******************************************************************
*
*$RCSfile: StringBufferWriter.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2006/03/13 01:03:54 $
*
*$Log: StringBufferWriter.java,v $
*Revision 1.2  2006/03/13 01:03:54  Administrator
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
//package org.apache.jasper.compiler.ServeltWriter

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * This is what is used to generate servlets.
 *
 * @author Anil K. Vijendran
 */
public class StringBufferWriter {
    public static int TAB_WIDTH = 4;
    public static String SPACES = "                              ";

	private String linesep;// system.getProeprty("line.seperator");
    // Current indent level:
    int indent = 0;

    // The sink buffer:
    StringBuffer buffer;

    public StringBufferWriter(StringBuffer buffer) {
	this.buffer = buffer;
	linesep= System.getProperty("line.separator");
    }

    public void pushIndent() {
	if ((indent += TAB_WIDTH) > SPACES.length())
	    indent = SPACES.length();
    }

    public void popIndent() {
	if ((indent -= TAB_WIDTH) <= 0 )
	    indent = 0;
    }


    /**
     * Quote the given string to make it appear in a chunk of java code.
     * @param s The string to quote.
     * @return The quoted string.
     */

    public String quoteString(String s) {
	// Turn null string into quoted empty strings:
	if ( s == null )
	    return "null";
	// Hard work:
	if ( s.indexOf('"') < 0 && s.indexOf('\\') < 0 && s.indexOf ('\n') < 0
	     && s.indexOf ('\r') < 0)
	    return "\""+s+"\"";
	StringBuffer sb  = new StringBuffer();
	int          len = s.length();
	sb.append('"');
	for (int i = 0 ; i < len ; i++) {
	    char ch = s.charAt(i);
	    if ( ch == '\\' && i+1 < len) {
		sb.append('\\');
		sb.append('\\');
		sb.append(s.charAt(++i));
	    } else if ( ch == '"' ) {
		sb.append('\\');
		sb.append('"');
	    } else if (ch == '\n') {
	        sb.append ("\\n");
	    }else if (ch == '\r') {
	   	sb.append ("\\r");
	    }else {
		sb.append(ch);
	    }
	}
	sb.append('"');
	return sb.toString();
    }

    public void println(String line) {
	buffer.append(SPACES.substring(0, indent)+line+linesep);
    }

    public void println() {
	 println("");
    }

    public void indent() {
	buffer.append(SPACES.substring(0, indent));
    }


    public void print(String s) {
	buffer.append(s);
    }

    public void printMultiLn(String multiline) {
	// Try to be smart (i.e. indent properly) at generating the code:
	BufferedReader reader =
            new BufferedReader(new StringReader(multiline));
	try {
    	    for (String line = null ; (line = reader.readLine()) != null ; )
		//		println(SPACES.substring(0, indent)+line);
		println(line);
	} catch (IOException ex) {
	    // Unlikely to happen, since we're acting on strings
	}
    }


}
