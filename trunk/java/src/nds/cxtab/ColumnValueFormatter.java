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
 * @author yfzhu@agilecontrol.com
 */

public class ColumnValueFormatter implements MemberFormatter {
	ColumnInterpreter lvci;
	Locale locale;
	public ColumnValueFormatter(ColumnInterpreter c, Locale locale){
		lvci=c;
		this.locale= locale;
	}
	public String format(String member) {
		try{
			return lvci.parseValue(member, locale);
		}catch(nds.util.ColumnInterpretException e){
			return member;
		}
	}
}
