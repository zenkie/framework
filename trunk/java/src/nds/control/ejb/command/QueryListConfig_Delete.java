package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.Types;
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
import nds.web.config.*;
import java.util.*;

import nds.security.User;

/**
 * Delete QueryListConfig
 * This is for ajax request.	
 *  
 */
public class QueryListConfig_Delete extends Command {
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject - 
			qlfid* - id of QueryListConfig
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.
	 * @return "data" will be jsonObject with following format:
	 * { 	code:0|!=0,
	 * 		message: message for error,
	 * 		qdf: QueryListConfig.toJSON
	 * }
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	User user=helper.getOperator(event);
  	QueryEngine engine=QueryEngine.getInstance();
  	MessagesHolder mh= MessagesHolder.getInstance();
  	StringBuffer sb=new StringBuffer();
  	ValueHolder holder= new ValueHolder();
  	JSONObject ro=new JSONObject();
  	try{
  		JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	Object tag= jo.opt("tag");
	  	int qlfId= jo.getInt("qlfid");
	  	QueryListConfig qlc=QueryListConfigManager.getInstance().getQueryListConfig(qlfId);
	  	
	  	//permission check
	  	if(Tools.getInt(engine.doQueryOne("select isadmin from users where id="+user.id),-1)!=1 ||
	  			(nds.security.Directory.READ & nds.control.util.SecurityUtils.getPermission(
	  		  			manager.getTable(qlc.getTableId()).getSecurityDirectory(), user.id.intValue()))
	  		  			!=nds.security.Directory.READ){
	  		if(!user.getName().equals("root"))
	  			throw new NDSException("@no-permission@");
	  	}
	  	
	  	QueryListConfigManager.getInstance().deleteQueryListConfig(qlfId);
	  	
	  	logger.debug("Delete qlf id="+qlfId);
	  	
	  	ro.put("tag", tag); //  return back unchanged.
	  	ro.put("message", mh.getMessage(event.getLocale(), "complete"));
		
	  	holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("data",ro );
		holder.put("code","0");
		
		
		return holder;
  	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  }
  

}