/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command.test;

import java.util.ArrayList;
import java.util.Locale;

import nds.schema.Column;
import nds.util.NDSException;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class BatchUpdateTest {
	public static void main(String[] args) throws Exception{
		if(!parseValue("OLD.NAME+OLD.VALUE").equals("NAME+VALUE") || 
				!parseValue("OLD+OLD.VALUE").equals("COLUMN+VALUE") ||
				!parseValue("3*OLD").equals("3*COLUMN") ||
				!parseValue("OLD.NAME1+OLD.VALUE").equals("OLD.NAME1+VALUE") ||
				!parseValue("OLD.VALUE").equals("VALUE") ||
				!parseValue("OLD.VALUE+3").equals("VALUE+3") ||
				!parseValue("upper(OLD.VALUE)").equals("upper(VALUE)"))
			System.out.println("Failed");
		else
			System.out.println("OK");
		parseValue("upper(OLD.VALUE)").equals("upper(VALUE)");
	}
	private static boolean appendColumnName(String colName, StringBuffer sb){
		if(colName.equalsIgnoreCase("NAME") || colName.equalsIgnoreCase("VALUE")){
			sb.append(colName);
			return true;
		}
		return false;
	}
	/**
     * Parse value to db acceptable format
     * @param value starts with "${" and ends with "}", may contains OLD/OLD.<column> type 
     * @param column
     * @return value that will be used for update clause construction
     */
    private static String parseValue(String value) throws NDSException{
    	String old= value;
    	StringBuffer sb=new StringBuffer();
    	int p= value.indexOf("OLD");
    	String str;
    	int pre=0,i;
    	boolean found;
    	
    	while(p> -1){
    		sb.append(value.substring(pre, p));
    		// handle old replacement
    		if( (value.length()> p+3) && value.charAt(p+3)=='.'){
    			// maybe column definition
    			for(i=p+4;i< value.length();i++){
    				if("+-*/ ,;()[]{}'\"|\\<>:".indexOf(value.charAt(i))>-1){
    					break;
    				}
    			}
    				str= value.substring(p+4, i);
    				found= appendColumnName(str, sb);
        			if(found){
        				p=i; 
	    			}else{
	    				//there's no column matches the name, take old. as normal string
	    				sb.append("OLD.");
	    				p+=4;
	    			}
    		}else{
    			// just current column
    			sb.append( "COLUMN");
    			p+=3;
    		}
    		pre= p;
			p= value.indexOf("OLD", p);
    	}
    	if(pre< value.length())sb.append(value.substring(pre,  value.length()));
    	System.out.println(old+"  convert to "+ sb.toString());
    	return sb.toString();
    }
}
