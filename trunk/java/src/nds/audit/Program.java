/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.audit;

/**
 * Program to judge phase intance status
 * @author yfzhu@agilecontrol.com
 */

public interface Program {
	public String execute(int phaseInstanceId) throws Exception;
}
