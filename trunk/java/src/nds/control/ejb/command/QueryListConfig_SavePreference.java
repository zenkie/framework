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
import nds.control.web.*;
import nds.util.*;
import nds.web.config.*;

import java.util.*;

import nds.security.User;

/**
 * Save QueryListConfig preference for user
 * This is for ajax request.	
 *  
 */
public class QueryListConfig_SavePreference extends Command {
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject - 
			qlcid* -  id of QueryListConfig
			table - table id used when qlcid=-1
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.
	 * @return "data" will be jsonObject with following format:
	 * { 	code:0|!=0,
	 * 		message: message for error,
	 * 		id: if is new, will be the new id
	 * 		name: name of QueryListConfig.toJSON saved
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
	  	int qlcId= jo.getInt("qlcid");
	  	int tableId= jo.getInt("table");
	  	String tbName=manager.getTable(tableId).getName();
	  	String name;
	  	if(qlcId==-1)name= QueryListConfigManager.getInstance().getMetaDefault(tableId).getName();
	  	else name= QueryListConfigManager.getInstance().getQueryListConfig(qlcId).getName();
	  	Properties props=new Properties();
	  	props.setProperty( tbName,String.valueOf( qlcId));
	  	SavePreference.setPreferenceValues(user.id.intValue(), "qlc", props);
	  	
	  	ro.put("tag", tag); //  return back unchanged.
	  	ro.put("message", mh.translateMessage("@current-qlc@"+name, event.getLocale()));
	  	ro.put("code", 0);
	  	
	  	holder.put("data",ro );
	  	holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");

		/**
		 * clear user web cache for qlc 
		 */
		WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
		UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(wc.getSession()).getActor(nds.util.WebKeys.USER));
		userWeb.invalidatePreference("qlc", tbName);
		
		
		logger.info("Save qlc "+ (qlcId)+" as default config for "+  manager.getTable(tableId).getName() +" by "+ user.name+"(id="+ user.id+")");
		
		return holder;
  	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  }
  

}