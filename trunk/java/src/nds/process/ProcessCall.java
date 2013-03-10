/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import java.util.Properties;

import nds.control.util.ValueHolder;


/**
 * Interface for user started processes.
 * @author yfzhu@agilecontrol.com
 */

public interface ProcessCall {
	/**
	 * Create a new instance. ProcessCall will be managed as plugin by nds.io.PluginManager,
	 * and since we do not allow client to newInstance, we delegate the new method to provider 
	 * @return a new instance
	 */
	//public ProcessCall newInstance();
	/**
	 *  Start the process.
	 *  Called when pressing the ... button in ...
	 *  It should only return false, if the function could not be performed
	 *  as this causes the process to abort.
	 *
	 *  @param ctx              Context
	 *  @param pi				Process Info
	 *  @return true if the next process should be performed
	 */
	public abstract ValueHolder startProcess (Properties ctx, ProcessInfo pi);

	public abstract boolean internalTransaction();

}
