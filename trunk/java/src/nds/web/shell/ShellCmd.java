package nds.web.shell;

import org.json.*;

public interface ShellCmd {
	public void execute(String args, JSONObject envObj) throws Exception;
}
