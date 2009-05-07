
package nds.web.interpreter;
import java.util.Locale;

import nds.util.ColumnInterpretException;
import nds.util.*;
/**
 * Encrypt column content and show only password masks on web
 */
 
public class EncryptInterpreter implements ColumnInterpreter,java.io.Serializable {
    public EncryptInterpreter() {}
    /** 
     */
    public String parseValue(Object value,Locale locale) {
    	if(value==null) return "";
        return "******";
    }
    /**
    * Just the str
    */
    public Object getValue(String str,Locale locale) {
        return str;
    }
}
