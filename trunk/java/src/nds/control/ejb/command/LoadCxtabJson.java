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
 * Load Cxtab, including axis and measures
 * This is for ajax request.	
 *  
 */
public class LoadCxtabJson extends Command {
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject - 
			cxtabId* - ad_cxtab.id
			uk.ltd.getahead.dwr.WebContext - this is for convenience to request jsp result
			tag - this is used by client to remember locale status, such as for row information,
				  it will be sent back unchanged.	
	 * @return "data" will be jsonObject with following format:
	 * { 	code:0|!=0,
	 * 		message: message for error,
	 		axisH :  axis horizontal, this is an array object, elements has properties: {columnlink, description}
	 		axisV :  axis vertical, same as axisH
	 		axisP :  axis page, same as axisP
	 		measures:array object, elements {description, column, userfact, valueformat,function_}
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
	  	int cxtabId= jo.getInt("cxtabId");
	  	int factTableId= Tools.getInt(engine.doQueryOne("select ad_table_id from ad_cxtab where id="+ cxtabId),-1);
	  	Table factTable = manager.getTable(factTableId);
	  	JSONArray axisH=new JSONArray();
	  	JSONArray axisP=new JSONArray();
	  	JSONArray axisV=new JSONArray();
	  	JSONArray measures=new JSONArray();
	  	List al=engine.doQueryList("select columnlink,description, position_, hidehtml from ad_cxtab_dimension where ad_cxtab_id="+ cxtabId +" order by orderno asc");
	  	for(int i=0;i< al.size();i++){
	  		List d= (List) al.get(i);
	  		String pos= (String) d.get(2);
	  		String hidehtml= (String) d.get(3);
	  		String clink=(String) d.get(0);
	  		try{
	  			new ColumnLink(clink);
	  		}catch(Throwable t){
	  			logger.debug("clink "+ clink +" not valid:"+ t);
	  			continue;
	  		}
	  		JSONObject dim=new JSONObject();
  			dim.put("columnlink", clink);
  			dim.put("hidehtml", hidehtml);
  			String desc=(String) d.get(1);
  			if(Validator.isNull(desc)){
  				ColumnLink cl= new ColumnLink(clink);
  				desc ="["+ cl.getDescription(event.getLocale())+"]"; 	  		
  			}
  			
  			dim.put("description", desc);
	  		if("H".equals(pos)){
	  			axisH.put(axisH.length(), dim);
	  		}else if("V".equals(pos)){
	  			axisV.put(axisV.length(), dim);
	  		}else{
	  			axisP.put(axisP.length(),dim);
	  		}
	  	}
  		String colName;
	  	al= engine.doQueryList("select ad_column_id, description, function_, userfact, VALUEFORMAT, valuename from ad_cxtab_fact where ad_cxtab_id="+ cxtabId+" order by orderno asc");
	  	for(int i=0;i< al.size();i++){
	  		List d= (List) al.get(i);
	  		int colId = Tools.getInt( d.get(0), -1);
	  		if(colId !=-1) {
	  			if(manager.getColumn(colId)==null) continue;
	  			colName=factTable.getName()+"."+ manager.getColumn(colId).getName();
	  		}else{
	  			colName="";
	  		}
	  		JSONObject m=new JSONObject();
  			m.put("column",  colName);
  			
  			String desc=(String) d.get(1);
  			if(Validator.isNull(desc)){
  				//ColumnLink cl= new ColumnLink(new int[]{colId});
  				desc ="["+ manager.getColumn(colId).getDescription(event.getLocale())+"]"; 	  		
  			}  		
  			
  			m.put("description", desc);
  			m.put("function_",   (String) d.get(2));
  			m.put("userfact", (String) d.get(3));
  			m.put("valueformat", (String) d.get(4));
  			m.put("valuename", (String) d.get(5));
  			measures.put(measures.length(), m);
	  	}
	  	JSONObject ro=new JSONObject();
	  	ro.put("tag", tag); //  return back unchanged.
  		ro.put("code", 0);
  		ro.put("message", mh.getMessage(event.getLocale(), "cxtab-loaded"));
  		ro.put("axisH", axisH);
  		ro.put("axisV", axisV);
  		ro.put("axisP", axisP);
  		ro.put("measures", measures);
	  	
	  	ValueHolder holder= new ValueHolder();
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");
		holder.put("data",ro );
		logger.debug(ro.toString());
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