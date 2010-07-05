package nds.io;

/**
 * Plugin that has alias names
 *  
 * @author yfzhu
 */
public interface AliasPlugin {
	/**
	 * Alias name may contains several alias separated by comma
	 * alias name should be case insensitive
	 * @return
	 */
	public String getAlias();
}
