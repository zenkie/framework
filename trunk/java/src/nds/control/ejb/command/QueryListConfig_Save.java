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
 * Save QueryListConfig
 * This is for ajax request.	
 *  
 */
public class QueryListConfig_Save extends Command {
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject - 
			qdf* -  QueryListConfig.toJSON
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
	  	JSONObject qdf= jo.getJSONObject("qdf");
	  	
	  	QueryListConfig qlf=new QueryListConfig(qdf);
	  	
	  	int qid= qlf.getId();
	  	QueryListConfigManager.getInstance().saveQueryListConfig(qlf);
	  	if(qlf.getId()!=qid){
	  		// a new config record saved, so will notify client
		  	ro.put("id", qlf.getId());
	  	}
	  	
	  	ro.put("tag", tag); //  return back unchanged.
	  	ro.put("message", mh.getMessage(event.getLocale(), "complete"));
	  	ro.put("code", 0);
	  	ro.put("name",qlf.getName());
		
	  	holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("data",ro );
		holder.put("code","0");
		logger.debug(ro.toString());
		
		logger.info("Save "+ qlf.getName()+" by "+ user.name+"(id="+ user.id+")");
		
		return holder;
  	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  }
  

}