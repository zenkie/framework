/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import javax.servlet.http.HttpServletRequest;

import nds.schema.Column;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class ButtonCheckCronExpression {
	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "CheckCronExpression";
	}
	
}
