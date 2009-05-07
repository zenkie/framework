/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interfact that output binary information to client
 * @author yfzhu@agilecontrol.com
 */

public interface  BinaryHandler {
	
	abstract void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception;
}
