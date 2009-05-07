/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Converts a property string into a PropertyString and vice versa.
     *
     * <p> For example, <code>"Provider=MSOLAP; DataSource=LOCALHOST;"</code>
     * becomes the set of (key, value) pairs <code>{("Provider","MSOLAP"),
     * ("DataSource", "LOCALHOST")}</code>. Another example is
     * <code>Provider='sqloledb';Data Source='MySqlServer';Initial
     * Catalog='Pubs';Integrated Security='SSPI';</code>.
 * @author yfzhu@agilecontrol.com
 */

public class PropertyString {
    List list = new ArrayList(); // elements are String[2]
    
    public void putAll(PropertyString props){
		for(Iterator it= props.iterator();it.hasNext(); ){
			String[] key=(String[]) it.next();
			put(key[0], key[1]);
		}
    }
    /**
     * return null if not found
     * @param key
     * @return
     */
    public String get(String key) {
        for (int i = 0, n = list.size(); i < n; i++) {
            String[] pair = (String[]) list.get(i);
            if (pair[0].equalsIgnoreCase(key)) {
                return pair[1];
            }
        }
        return null;
    }
    public boolean getBoolean(String key, boolean defaultValue){
    	String v= get(key);
    	return Tools.getBoolean(v, defaultValue);
    }
    public int getInt(String key, int defaultValue){
    	String v= get(key);
    	return Tools.getInt(v, defaultValue);
    }
    
    /**
     * 
     * @param key
     * @param value
     * @return old value of the key, may be null
     */
    public String put(String key, String value) {
        for (int i = 0, n = list.size(); i < n; i++) {
            String[] pair = (String[]) list.get(i);
            if (pair[0].equalsIgnoreCase(key)) {
                String old = pair[1];
                pair[1] = value;
                return old;
            }
        }
        list.add(new String[] {key, value});
        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        for (int i = 0, n = list.size(); i < n; i++) {
            String[] pair = (String[]) list.get(i);
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(pair[0]);
            sb.append('=');

            /*
             * Quote a property value if is has a semi colon in it
             * 'xxx;yyy';
             */
            if (pair[1].indexOf(';') >= 0 && pair[1].charAt(0) != '\'') {
                sb.append("'");
            }

            sb.append(pair[1]);

            if (pair[1].indexOf(';') >= 0 && pair[1].charAt(pair[1].length() - 1) != '\'') {
                sb.append("'");
            }

        }
        return sb.toString();
    }

    public Iterator iterator() {
        return list.iterator();
    }	
  
	/**
     * Converts a property string into a {@link PropertyString}.
     *
     * <p> For example, <code>"Provider=MSOLAP; DataSource=LOCALHOST;"</code>
     * becomes the set of (key, value) pairs <code>{("Provider","MSOLAP"),
     * ("DataSource", "LOCALHOST")}</code>. Another example is
     * <code>Provider='sqloledb';Data Source='MySqlServer';Initial
     * Catalog='Pubs';Integrated Security='SSPI';</code>.
     *
     **/
    public static PropertyString parse(String s) {
        return new PropertyStringParser(s).parse();
    }

    private static class PropertyStringParser {
        private final String s;
        private final int n;
        private int i;
        private final StringBuffer nameBuf;
        private final StringBuffer valueBuf;

        private PropertyStringParser(String s) {
            this.s = s;
            this.i = 0;
            this.n = s.length();
            this.nameBuf = new StringBuffer(64);
            this.valueBuf = new StringBuffer(64);
        }

        PropertyString parse() {
            PropertyString list = new PropertyString();
            while (i < n) {
                parsePair(list);
            }
            return list;
        }
        /**
         * Reads "name=value;" or "name=value<EOF>".
         */
        void parsePair(PropertyString list) {
            String name = parseName();
            String value;
            if (i >= n) {
                value = "";
            } else if (s.charAt(i) == ';') {
                i++;
                value = "";
            } else {
                value = parseValue();
            }
            list.put(name, value);
        }
        /**
         * Reads "name=". Name can contain equals sign if equals sign is
         * doubled.
         */
        String parseName() {
            nameBuf.setLength(0);
            while (true) {
                char c = s.charAt(i);
                switch (c) {
                case '=':
                    i++;
                    if (i < n && (c = s.charAt(i)) == '=') {
                        // doubled equals sign; take one of them, and carry on
                        i++;
                        nameBuf.append(c);
                        break;
                    }
                    String name = nameBuf.toString();
                    name = name.trim();
                    return name;
                case ' ':
                    if (nameBuf.length() == 0) {
                        // ignore preceding spaces
                        i++;
                        break;
                    } else {
                        // fall through
                    }
                default:
                    nameBuf.append(c);
                    i++;
                    if (i >= n) {
                        return nameBuf.toString().trim();
                    }
                }
            }
        }
        /**
         * Reads "value;" or "value<EOF>"
         */
        String parseValue() {
            char c;
            // skip over leading white space
            while ((c = s.charAt(i)) == ' ') {
                i++;
                if (i >= n) {
                    return "";
                }
            }
            if (c == '"' || c == '\'') {
                String value = parseQuoted(c);
                // skip over trailing white space
                while (i < n && (c = s.charAt(i)) == ' ') {
                    i++;
                }
                if (i >= n) {
                    return value;
                } else if (s.charAt(i) == ';') {
                    i++;
                    return value;
                } else {
                    throw new RuntimeException(
                            "quoted value ended too soon, at position " + i +
                            " in '" + s + "'");
                }
            } else {
                String value;
                int semi = s.indexOf(';', i);
                if (semi >= 0) {
                    value = s.substring(i, semi);
                    i = semi + 1;
                } else {
                    value = s.substring(i);
                    i = n;
                }
                return value.trim();
            }
        }
        /**
         * Reads a string quoted by a given character. Occurrences of the
         * quoting character must be doubled. For example,
         * <code>parseQuoted('"')</code> reads <code>"a ""new"" string"</code>
         * and returns <code>a "new" string</code>.
         */
        String parseQuoted(char q) {
            char c = s.charAt(i++);
            if(c != q)throw new Error("assert failed");
            valueBuf.setLength(0);
            while (i < n) {
                c = s.charAt(i);
                if (c == q) {
                    i++;
                    if (i < n) {
                        c = s.charAt(i);
                        if (c == q) {
                            valueBuf.append(c);
                            i++;
                            continue;
                        }
                    }
                    return valueBuf.toString();
                } else {
                    valueBuf.append(c);
                    i++;
                }
            }
            throw new RuntimeException(
                    "Property string '" + s +
                    "' contains unterminated quoted value '" +
                    valueBuf.toString() + "'");
        }
    }
    
    public static  void main(String[] args){
    	String a="a=b;c=\"'d'\"";
    	PropertyString ps=new PropertyString();
    	ps.put("a", "b");
    	ps.put("c", "\"d\"");
    	System.out.println(ps.toString());
    	System.out.println(PropertyString.parse(a).toString());
    }
}
