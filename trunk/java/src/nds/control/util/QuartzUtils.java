/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.quartz.*;
import nds.log.*;
/**
 * Looking up scheduler from JNDI name
 * @author yfzhu@agilecontrol.com
 */

public class QuartzUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger(QuartzUtils.class.getName());
	/**
	 * Looking up from JNDI
	 * @return
	 * @throws Exception
	 */
	public static Scheduler getScheduler() throws Exception{
        // Get a context for the JNDI look up
        Context ctx = new InitialContext();
        // Look up 
        String name= nds.control.util.EJBUtils.getApplicationConfigurations().getProperty("jndi.scheduler", "Quartz");
		return  (Scheduler)ctx.lookup (name);
	}
}
