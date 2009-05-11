/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.*;
import nds.control.web.*;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;

/**
 * 能够执行操作系统命令的按钮，操作系统命令从column.defaultvalue获取, 命令的输出内容将返回给客户端
 * 
 * 由于此命令的功能特殊性，将记录到系统日志中
 * 
 * @author yfzhu@agilecontrol.com
 */

public class ExecShell extends Command{
	/**
	 * @param event parameters:
	 *    objectid - object id of table that button is working on
	 *    columnid - column id that is defined as button  		
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	User usr=helper.getOperator(event);
    	int objectId=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	int columnId=Tools.getInt( event.getParameterValue("columnid",true), -1);
    	Column column=TableManager.getInstance().getColumn(columnId);
    	String cmd= column.getDefaultValue();
    	/*if(!usr.name.equals("root"))
    		if (!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, "AD_CXTAB", cxtabId, nds.security.Directory.WRITE, event.getQuerySession()))
    			throw new NDSException("@no-permission@");
    	*/
    	ValueHolder holder=new ValueHolder();
    	String message="@complete@";
    	try{
    		if(nds.util.Validator.isNotNull(cmd)){
    			Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
    			
    			String logFile = conf.getProperty("dir.tmp","/tmp") + File.separator+ "ExecShell_"+ column.getTable()+"_"+ columnId+"_"+System.currentTimeMillis()+".log"; 
    			CommandExecuter cmdE= new CommandExecuter(logFile);
    			
    			int err=cmdE.run(cmd);
    			logger.info("User "+ usr.getName() + "@" + usr.getClientDomain()+" runs command :"+ cmd+" with return code:"+err);
        		SysLogger.getInstance().info("sys", "exec", usr.getName(), "", cmd+"("+ err+")", usr.adClientId);
    			message= Tools.readFile(logFile);
    			//delete log file
    			FileUtils.delete(logFile);
    		}
    		holder.put("code","0");
    	}catch(Throwable e){
    		logger.error("User "+ usr.getName() + "@" + usr.getClientDomain()+" fail to run command :"+ cmd, e);
    		message="@exception@:"+ cmd +":"+ e.getMessage();
    		throw new NDSException(message);
    	}
		holder.put("message", message);
		holder.put("code", new Integer(0));//no change for current page		
    	return holder;
    }
    
	    
}
