/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    	// user must have write permission on this object
		if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.getId().intValue(), 
				usr.getName(),column.getTable().getName(), objectId,Directory.WRITE, event.getQuerySession() )){
			throw new NDSException("@no-permission@");
		}
		
		//replace @xxx@ with data of current row column value
		
    	
    	/*if(!usr.name.equals("root"))
    		if (!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, "AD_CXTAB", cxtabId, nds.security.Directory.WRITE, event.getQuerySession()))
    			throw new NDSException("@no-permission@");
    	*/
    	ValueHolder holder=new ValueHolder();
    	String message="@complete@";
    	String cmd=null;
    	try{
        	cmd= parseWildcardDefaultValue(column, objectId);
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
    /**
     * 类似于WildcardFilter,我们允许命令行里有Wildcard column 作为参数内容,这里将替换为实际的字段的内容.
     * 
     * 当前仅支持本表的字段作为Wildcard column
     * 
    */
    private String parseWildcardDefaultValue(Column column, int objectId) throws Exception{
		// 定位关联字段
		Pattern a= Pattern.compile("@(.*?)@");
		String cmd= column.getDefaultValue();
		Matcher m= a.matcher(cmd);
		TableManager manager=TableManager.getInstance();
		java.util.ArrayList cs=new ArrayList();  
		while(m.find()){
			String c= m.group(1); // this will be the ref column
			Column fc= (Column)manager.getColumn(c.toUpperCase());
			if(fc == null) fc= column.getTable().getColumn(c.toUpperCase());
			if(fc==null)throw new NDSRuntimeException("Wildcard defaultvalue error for column"+ column+": Could not find column '"+ c+"' defined in defaultvallue");
			if(column.getTable().getId() != fc.getTable().getId()){
				throw new NDSRuntimeException("Current only defaultvalue containing column of the same table is supported:"+ column +": defaultvalue="+column.getDefaultValue());
			}
			
			cs.add(fc);
		}
		if(cs.size()==0){
			return cmd;
		}
		// cmd format: /opt/build/bin/@NAME@ @ID@
		// sql: select 'opt/build/bin/' || NAME ||' ' || ID ||'' from b_project where id=1343
		String sql="select '" + cmd + "' from "+ column.getTable().getRealTableName()+" "+ column.getTable()
			+" where id="+ objectId;
		for(int i=0;i<cs.size();i++){
			Column col= (Column) cs.get(i);
			
			sql= StringUtils.replace(sql, "@"+ col.getName()+"@","'||"+ col.getName()+"||'"); 
		}
		logger.debug(sql);
		return (String)QueryEngine.getInstance().doQueryOne(sql);
    }        
	    
}
