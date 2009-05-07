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

import java.util.*;

import nds.security.User;

/**
 * Save print setting for one special table
 * This is for ajax request.	
 *  
 */
public class SavePrintSetting extends Command {
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject -
	 * 		template - template id in format like:
	 * 			format like: cx123 for ad_cxtab(id=123) definitions, t123 for ad_report(id=123) definitions
	 * 		format	-	pdf, xls, or csv 
	 * 		tableid - table id
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.	
	 * @return "data" will be jsonObject with following format:
	 * { 	code:0|!=0,
	 * 		message: message for error,
	 * }
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	User user=helper.getOperator(event);
  	QueryEngine engine=QueryEngine.getInstance();
  	MessagesHolder mh= MessagesHolder.getInstance();
  	StringBuffer sb=new StringBuffer();
  	try{
  		JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	Object tag= jo.opt("tag");
	  	int tableId= jo.getInt("tableid");
	  	String template = jo.getString("template");
	  	String format =jo.getString("format");
	  	String module= TableManager.getInstance().getTable(tableId).getName().toLowerCase()+".print";
	  	
	  	ArrayList al=new ArrayList();
	  	String s;
	  	
	  	
	  	s="merge into ad_user_pref p using(select "+ QueryUtils.TO_STRING( module)+ " module,"+QueryUtils.TO_STRING( "template")+ " name,"+
	  	 QueryUtils.TO_STRING(template)+" value from dual) a on (p.ad_user_id="+ user.id+" and p.module=a.module and p.name=a.name ) "+
	  	 "when matched then update set p.value=a.value, p.modifieddate=sysdate "+
	  	 "when not matched then insert (id, ad_user_id,module,name,value,creationdate,modifieddate) values"+
	  	 "(get_sequences('ad_user_pref'), "+user.id+",a.module, a.name, a.value,sysdate,sysdate)"; 
	  	
	  	logger.debug(s);
	  	al.add(s);
	  	
	  	s="merge into ad_user_pref p using(select "+ QueryUtils.TO_STRING( module)+ " module,"+QueryUtils.TO_STRING( "format")+ " name,"+
	  	 QueryUtils.TO_STRING(format)+" value from dual) a on (p.ad_user_id="+ user.id+" and p.module=a.module and p.name=a.name ) "+
	  	 "when matched then update set p.value=a.value, p.modifieddate=sysdate "+
	  	 "when not matched then insert (id, ad_user_id,module,name,value,creationdate,modifieddate) values"+
	  	 "(get_sequences('ad_user_pref'), "+user.id+",a.module, a.name, a.value,sysdate,sysdate)"; 
	  	
	  	logger.debug(s);
	  	al.add(s);
	  	
	  	engine.doUpdate(al);

	  	JSONObject ro=new JSONObject();
	  	ro.put("tag", tag); //  return back unchanged.
  		ro.put("code", 0);
  		ro.put("message", mh.getMessage(event.getLocale(), "complete"));
	  	
	  	ValueHolder holder= new ValueHolder();
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");
		holder.put("data",ro );
		
		return holder;
  	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  }
  
}