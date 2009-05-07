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
 * load attribute setting according to table and record specified. 
 * This is for ajax request.	
 *  
 */
public class LoadProductAttribute extends Command {
	/**
	 * If we can find product by name, attribute set will be checked, if found, will direct
	 * front to attribute set filling page.
	 * 
	 * If we find product alias by name as barcode, attribute set will not be checked if attribute
	 * set instance id is set in m_product_alias table.
	 * 
	 * 如果m_product_alias 表里对应的行未设置 物料属性实例id，将根据物料的物料属性集仍然要求界面进行配置。
	 * 
	 * @param event contains 
	 * 	jsonObject - 
			tableId* - table
			recordId - id of that table records
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.	
	 * @return "data" will be jsonObject with following format:
	 * { 	code:0|!=0,
	 * 		message: message for error,
	 * 		dom: DOM created by special we page, may contain javascript segments
	 * 		showDialog: true|false
	 * 		jsondetail: json array,elements like ["P1323",13] "P1323" is for input id, 13 for qty
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
	  	int tableId= jo.getInt("tableId");
	  	int recordId= jo.getInt("recordId");
	  	ValueHolder vh;
	  	
	  	JSONObject ro=new JSONObject();
	  	ro.put("tag", tag); //  return back unchanged.
  		ro.put("code", 0);
  		ro.put("showDialog",false);
	  	Object jsondetail=null;
	  	int realTableId=manager.getTable(manager.getTable(tableId).getRealTableName()).getId();
	  	List al=engine.doQueryList("select 'P' ||M_ATTRIBUTESETINSTANCE_ID, qty from m_attributedetail where ad_table_id="+ realTableId+" and record_id="+ recordId);
	  	if(al.size()>0){
	  		jsondetail= JSONUtils.toJSONArray(al);
	  		ro.put("jsondetail", jsondetail);
	  		
	  		List v= engine.doQueryList("select i.m_product_id, p.M_ATTRIBUTESET_ID from "+ manager.getTable(tableId).getRealTableName()+
	  				" i, m_product p where p.id=i.m_product_id and i.id="+ recordId);
	  		int productId = Tools.getInt( ((List) v.get(0)).get(0),-1);
	  		int asId = Tools.getInt( ((List) v.get(0)).get(1),-1);
	  		if(asId!=-1 && productId!=-1 ){
  				//
  				WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
  				/**
  				 * Please note param "compress=false" is to prohibit  com.liferay.filters.compression.CompressionFilter from compressing file content 
  				 */
  				String page=wc.forwardToString(WebKeys.NDS_URI+"/pdt/itemdetail.jsp?compress=f&table="+tableId+"&pdtid="+productId+"&asid="+asId);
  				ro.put("pagecontent", page);
  		  		ro.put("showDialog",true);
  			}	  		
	  	}else{
	  		ro.put("jsondetail", "");// this will make client clear jsonobj column (null will remain it unchanged)
	  	}
	  	
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