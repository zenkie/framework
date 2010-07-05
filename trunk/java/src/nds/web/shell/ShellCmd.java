package nds.web.shell;

import org.json.*;
import java.util.Locale;
/**
 * 
 * Work as command line in portal
 * 
 * @author yfzhu
 *
 */
public interface ShellCmd extends nds.io.AliasPlugin{
	/**
	 * 
	 * @param args args excluding cmd name/alias
	 * @param envObj contains following known parameters
	 * 	
	 * @throws Exception
	 */
	public JSONObject execute(String args, JSONObject envObj) throws Exception;
	
	/**
	 * Get simple help message about command arguments, for details and samples,
	 * User should call command: man <command>
	 * @param locale
	 * @return simple message about arguments, just like usage in unix shell
	 */
	public String getHelp(Locale locale);
}
