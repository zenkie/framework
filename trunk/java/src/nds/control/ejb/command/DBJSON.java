package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import java.util.*;

import nds.security.User;

/**
 * 用于完成Javascript 发来的数据库json 服务请求，将event 内部定义的json 不进行解析而直接发往数据库
 *  
 */
public class DBJSON extends Command {
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject -
			param* - String, 无须解析为JSON 对象, JSON object 将作为调用存储过程的参数
	 *      table*  - String, 主要工作的表，例如对于POS业务，table为M_RETAIL
	 * 		action* - String, 动作名称
	 *      permission* - "R" 表示只读, "W"表示写入（包含读权限，对主表的增删改，和提交相应单据的授权）
	 系统根据上述table,action,permission 构造存储函数名，规则：
	       <table>_$<permission>_<action>(userId,jsonRequest)
	       
	       例如，m_retail_$w_insertline
				
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.	
	 * @return "data" will be jsonObject with following format:
	 * { 	
	 * 		tag: from request
	 		jsonResult* -string JSON object returned from database
	 * }
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	User usr=helper.getOperator(event);
  	QueryEngine engine=QueryEngine.getInstance();
  	MessagesHolder mh= MessagesHolder.getInstance();
  	try{
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	Object tag= jo.opt("tag");
	  	String table= jo.getString("table");
	  	String action= jo.getString("action");
	  	String permission= jo.getString("permission");
	  	String param= jo.getString("param");
	  	
	  	// check permission
	  	Table t= manager.getTable(table);
	  	if (!"root".equals(usr.getName())){
		  	int perm= helper.getPermissions(t.getSecurityDirectory(),usr.id.intValue());
		  	int minPerm=1;// read
		  	if("W".equalsIgnoreCase(permission)){
		  		if(t.isActionEnabled(Table.ADD) || t.isActionEnabled(Table.MODIFY)|| t.isActionEnabled(Table.DELETE))
		  			minPerm |=3;
		  		if( t.isActionEnabled(Table.SUBMIT))
		  			minPerm |= 5; 
		  	}
		  	if( (perm & minPerm) !=minPerm){
		  		logger.debug("perm="+ perm+",minPerm="+minPerm);
		  		throw new NDSException("@no-permission@");
		  	}
	  	}
	  	// construct procedure name
	  	String func= table + "_$"+ permission+"_"+ action;
	  	logger.debug("Call "+ func+"("+usr.getId()+","+param+")");
	  	ArrayList params= new ArrayList();
	  	params.add(usr.getId());
	  	params.add(param);
	  	
	  	ArrayList res= new ArrayList();
	  	res.add( String.class);// string
	  	
	  	Collection list=QueryEngine.getInstance().executeFunction(func, params, res);
	  	String result= (String)list.iterator().next();
	  	logger.debug("result:"+ result);
	  	
	  	JSONObject data=new JSONObject();
	  	data.put("jsonResult",result);
	  	if(tag!=null)data.put("tag", tag); //  return back unchanged.
	  	
  		ValueHolder holder= new ValueHolder();
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");
		holder.put("data",data );
	  	

	  	return holder;
  	}catch(Throwable t){
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  }
 
}