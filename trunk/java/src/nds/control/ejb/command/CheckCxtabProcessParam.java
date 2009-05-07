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
 * Direct to /html/nds/cxtab/processparam.jsp to construct ui for accepting cxtab pre-process params.
 * 
 * Some report need program to prepare data first, this program and its parameters can be set in ad_cxtab
 * This command will redirect the ui to browser
 * 
 * This is for ajax request.	
 * @author yfzhu@agilecontrol.com 
 */
public class CheckCxtabProcessParam extends Command {
	 /** 
	  * @param event contains 
	 * 	jsonObject - 
			cxtab* - ad_cxtab.name 
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.	
	 * @return "data" will be jsonObject with following format:
	 * { 	code:0|!=0,
	 * 		message: message for error,
	 * 		dom: DOM created by special we page, may contain javascript segments
	 * }
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	User usr=helper.getOperator(event);
  	QueryEngine engine=QueryEngine.getInstance();
  	MessagesHolder mh= MessagesHolder.getInstance();
  	try{
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject",true);
	  	Object tag= jo.opt("tag");
	  	String cxtabName= jo.getString("cxtab");
	  	
	  	Table table= manager.getTable("ad_cxtab");
	  	if(table.getAlternateKey().isUpperCase())cxtabName= cxtabName.toUpperCase();
	  	int processId=Tools.getInt( engine.doQueryOne("select ad_process_id from ad_cxtab where ad_client_id="+ usr.adClientId+" and "+ 
	  			table.getAlternateKey().getName()+"="+ QueryUtils.TO_STRING(cxtabName)+" and isactive='Y'"), -1);	  	
	  	ValueHolder vh;
	  	
	  	JSONObject ro=new JSONObject();
	  	if(tag!=null)ro.put("tag", tag); //  return back unchanged.
	  	//direct to page no matter what pid is 
  		ro.put("code", 0);
		WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
		/**
		 * Please note param "compress=false" is to prohibit  com.liferay.filters.compression.CompressionFilter from compressing file content 
		 */
		String page=wc.forwardToString(WebKeys.NDS_URI+"/cxtab/processparam.jsp?compress=false&processid="+processId);
		//logger.debug("page content:"+ page);
		ro.put("pagecontent", page);

		ValueHolder holder= new ValueHolder();
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("data",ro );
		logger.debug(ro.toString());
		return holder;
  	}catch(Throwable t){
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  }
}