package nds.control.web.binhandler;

import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.io.AliasPlugin;
import nds.log.Logger;
import nds.log.LoggerManager;

import org.json.*;
import nds.web.shell.*;
import nds.control.web.*;
import nds.schema.TableManager;
import nds.util.*;

/**
 * Handle command line request
 * 
 * @author yfzhu
 *
 */
public class Shell implements BinaryHandler{
	private static Logger logger= LoggerManager.getInstance().getLogger(Shell.class.getName());
	
	private static Hashtable<String,ShellCmd> commands=new Hashtable<String,ShellCmd>();//key: command name(String), value: ShellCmd
	/**
	 * Scan package of nds.web.shell and load all Command into memory to construct valid command list
	 */
	public void init(ServletContext context){
		
	}
	/**
	 * 
	 * @param name should be lower case and length>0
	 * @return
	 * @throws NDSException
	 */
	private static ShellCmd getInnerCommand(String name) throws NDSException{
		ShellCmd command=(ShellCmd) commands.get(name);
        if( command == null) {
        	Class c=null;
        	try{
        		c= Class.forName("nds.web.shell."+ name.substring(0,1).toUpperCase()+name.substring(1));
        		
        		command=(ShellCmd)c.newInstance();
				String[] alias=command.getAlias().split(",");
				for(int i=0;i<alias.length;i++){
					commands.put(alias[i].toLowerCase(),command);
				}
					
        	}catch(Exception c2){
        		logger.error("Fail to load "+ name+":"+ c2);
        	}
        }
        return command;
    }
	/**
	 * Find command according to cmdmain
	 * @param cmdmain  alias name of command
	 * @return
	 * @throws Exception
	 */
	public static ShellCmd getCommand(String cmdmain)throws Exception{
        nds.io.PluginController pc=(nds.io.PluginController) WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PLUGIN_CONTROLLER);
        ShellCmd cmd= pc.findPluginShellCmd(cmdmain);
        if(cmd==null){
        	cmd= getInnerCommand(cmdmain);
        }
        return cmd;
		
	}
	public static Collection<ShellCmd> listCommands() throws Exception{
        nds.io.PluginController pc=(nds.io.PluginController) WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PLUGIN_CONTROLLER);
        ArrayList<ShellCmd> cmds=new ArrayList(commands.values());
        for( Iterator<ShellCmd> it=pc.listShellCmds();it.hasNext();){
        	cmds.add(it.next());
        }
        return cmds;
	}
	private void showHelpPage(HttpServletRequest request,
			HttpServletResponse  response, String error, ShellCmd cmd, Locale locale) throws Exception{
		JSONObject  ret=new JSONObject();
		ret.put("message",MessagesHolder.getInstance().translateMessage(error,locale));
		ret.put("code", -1);
		ret.put("help", cmd==null?"":cmd.getHelp(locale));
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.print(ret.toString());
	}
	
	/**
	 * 
	 * 
	 */
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		String cmdline=request.getParameter("cmdline");
		
		SessionContextManager scmanager= WebUtils.getSessionContextManager(request.getSession());
		UserWebImpl userWeb=(UserWebImpl)scmanager.getActor(WebKeys.USER);
		if(userWeb==null || userWeb.isGuest()){
			showHelpPage(request,response,"<a href='/login.jsp'>Login first</a>", getCommand("shell"), TableManager.getInstance().getDefaultLocale());
			return;
		}
		Locale locale= userWeb.getLocale();
		ShellCmd cmd=null;
		try{
			if(Validator.isNull(cmdline)){
				showHelpPage(request,response,"@no-command@",  getCommand("help"),locale);
				return;
			}
			cmdline= cmdline.trim();
			//logger.debug(cmdline);
			
			String[] cmdparams=cmdline.split("[\t ]",2);
			cmd= getCommand( cmdparams[0].toLowerCase());
			String args;
			if(cmd==null){
				//try list as default command
				cmd= getCommand("list");
				args= cmdline;// whole line as args for list command
			}else{
				args=cmdparams.length>1?cmdparams[1]:"";
			}
			
			String env= request.getParameter("env");
			JSONObject envObj;
			if(nds.util.Validator.isNotNull(env)){
				envObj= new JSONObject(env);
			}else envObj=new JSONObject();
			envObj.put("request", request);
			envObj.put("response", response);
			if(userWeb!=null)envObj.put("userweb",userWeb);
			
			JSONObject ret=cmd.execute(args, envObj);
			//logger.debug(cmdline+":"+ ret);
			
			//for some command, reponse will be forward to binary download response, so do not print 
			//json here
			//sample: download
			//if(ret.optBoolean("output.json",true)){
				response.setContentType("application/json; charset=UTF-8");
				PrintWriter out = response.getWriter();
				String s =MessagesHolder.getInstance().translateMessage(ret.toString(), userWeb.getLocale()) ;
				out.print(s);
			//}*/
			//log to syslog and we can use this to fetch user command history
			/*SysLogger.getInstance().debug("SH", cmd.getAlias().split(",", 2)[0], 
					userWeb.getUserName(), userWeb.getHostIP(), cmdline, userWeb.getAdClientId() );
			*/
			userWeb.registerShellCmd(cmdline);
		}catch(Throwable t){
			logger.error("Fail to load "+ cmdline, t);
			showHelpPage(request,response,t.getMessage(),cmd, locale);
		}
		
	}
	
	
}
