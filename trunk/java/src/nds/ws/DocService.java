/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package  nds.ws;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public interface DocService {
	public Doc prepare(String docNO);
	public Doc finish(String docNO);
	public Doc abort(String docNO);
}
