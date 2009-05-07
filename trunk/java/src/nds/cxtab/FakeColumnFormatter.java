/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.cxtab;

import java.util.Locale;

import nds.util.ColumnInterpreter;

import nds.jcrosstab.MemberFormatter;

/**
 * 
 * This column will contain only one value: 1, but will return ""
 * 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class FakeColumnFormatter implements MemberFormatter {
	public static final FakeColumnFormatter INSTANCE= new FakeColumnFormatter("");
	private String name;
	public FakeColumnFormatter(String name){
		if(name==null)
			name="";
		else
			this.name=name;
	}
	public String format(String member) {
		return name;
	}
	
}
