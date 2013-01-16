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
 * load page to client 
 * This is for ajax request.	
 *  
 */
public class LoadPage extends Command {
	/**
	 * @param event contains 
	 * 	jsonObject - 
			queryParams - JSONObject which contains query params for url
			url - page url 
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
	 * @return "data" will be jsonObject with following format:
	 * { 	pagecontent:page for that url,
	 * }
	 * 	
	 */
	
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}
	
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	User usr=helper.getOperator(event);
  	QueryEngine engine=QueryEngine.getInstance();
  	MessagesHolder mh= MessagesHolder.getInstance();
  	try{
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject queryParams= jo.optJSONObject("queryParams");
	  	WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
		/**
		 * Please note param "compress=false" is to prohibit  com.liferay.filters.compression.CompressionFilter from compressing file content 
		 */
	  	StringBuffer url= new StringBuffer(jo.getString("url"));
	  	url.append("?");
			/**
			 * Please note param "compress=false" is to prohibit  com.liferay.filters.compression.CompressionFilter from compressing file content 
			 */
	  	url.append("compress=f");
	  	if(queryParams!=null){
	  		for(Iterator it=queryParams.keys();it.hasNext();){
	  			String key=it.next().toString();
	  			String value=queryParams.getString(key);
	  			url.append("&").append(key).append("=").append(java.net.URLEncoder.encode(value,"UTF-8"));
	  		}
	  	}
	  	String page=url.toString();
	  	logger.debug(page);
		String pagecontent=wc.forwardToString(page);
		JSONObject ro=new JSONObject();
		ro.put("pagecontent", pagecontent);
	  	ValueHolder holder= new ValueHolder();
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");
		holder.put("data",ro );
		return holder;
  	}catch(Throwable t){
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  }
  
  private DefaultWebEvent createEvent(JSONArray row, ArrayList colNames, DefaultWebEvent template ) throws JSONException{
  	DefaultWebEvent e=(DefaultWebEvent)template.clone();
  	for(int i=0;i< colNames.size();i++){
  		e.put( (String)colNames.get(i), row.get(i));
  	}
  	return e;
  }
}