/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.interpreter;

import java.util.Locale;

import nds.util.*;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class CrossOrderStatusInterpreter implements ColumnInterpreter,java.io.Serializable {
    public CrossOrderStatusInterpreter() {}
    /** Valid part are 
     *  1 on hand  2 finish
     *  N notice, M confirm, P planning, F closed and not handle in E state
     * @throws ColumnInterpretException if input value is not valid
     */
    public String parseValue(Object value,Locale locale) {
    	if (value==null )return "";
    	char[] s=( (String)value).toCharArray();
    	String r="";
    	for (int i=0;i<s.length;i++){
    		r += "<img src='"+ WebKeys.NDS_URI+"/images/status/crossorder_"+ s[i]+".gif' width=16 height=16 align=absmiddle border=0>";
    	}
        return r;
    }
    /**
    * Just the str
    */
    public Object getValue(String str,Locale locale) {
        return str;
    }
	@Override
	public String changeValue(String str, Locale locale)
			throws ColumnInterpretException {
		// TODO Auto-generated method stub
		return null;
	}
}
