package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;

import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AjaxUtils;
import nds.control.util.EditableGridMetadata;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;

/**
 * Update grid data, this is for ajax request
 *   
 */
public class UpdateGridData extends Command {
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	return true;
    }
	/**
	 * @param event contains 
	 * 	jsonObject - in javascript format:
		 	function CommandEvent{
				this.command="UpdateGridData";
				this.table="c_order"
				this.fixeColumns= "113=33&133=2131"
				this.addList=new Array();
				this.modifyList=new Array();
				this.deleteList=new Array();
				this.queryRequest=QueryRequest(){
					this.table="C_OderItem";
					this.column_masks=[1,5];//Column.MASK_CREATE_EDIT "or" (union) Column.MASK_QUERY_SUBLIST
					this.column_include_uicontroller=false; // optional, default false
					this.init_query=true;// opt, default to true
										 // if true, will use dir_perm and fixedColumns for where clause construction,
										 // else these params will be ignored since param_expr contains these filters
										 
					this.dir_perm=1; //opt, default to 1, Directory.READ, Directory.WRITE will be used in where clause construction 
					this.fixedColumns="fixed_columns_a=b&fixed_columns_c=d" // opt, will be used in where clause construction
					this.param_expr=Expression.toString() // optional
					this.start=0;
					this.range=10;
					this.order_column="";
					this.order_asc=false;	
					this.quick_search_column=""; //opt
					this.quick_search_data=""; // opt
					this.accepter_id="" // opt
				}
			} 
	 * @return "data" will be jsonObject with following format:
	 * 		function UdateGridDataResult{
	 * 			this.results= []  //array, elements are 
	 * 				[{row: 1, objId:11, msg:null,action:"M", jsoncreated:"T"}, // if jsoncreated set to "T"(true), that row should be disabled for update  
	 * 				 {row: 3, objId:-1, msg:"creation failed",action:"A"}];
	 * 			this.qresult={
	 * 				 totalRowCount:13,
	 * 				 rowCount: 2,
	 * 				 rows:[][]
	 * 			};
	 *    		this.message="some information";
	 *          this.refresh=false;// when submit, set this to true will make the ui refresh list immediately
	 * 		}
	 	row is the row index of this.data in GridControl
	 	
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
  	JSONObject returnObj=new JSONObject();
  	JSONArray al, objectData, rtArrayResults=new JSONArray();
  	JSONObject rtRow;
  	DefaultWebEvent evt,template;
  	ValueHolder vh;
  	int rowIdx, objectId;
  	String errorMsg;
  	User usr=helper.getOperator(event);
  	java.util.Locale locale= event.getLocale();
  	template=(DefaultWebEvent)event.clone();
  	template.getData().remove("jsonObject".toUpperCase());
  	// every line has its own transaction, one line rollback will not interfere with the others
  	template.setParameter("nds.control.ejb.UserTransaction" , "Y"); // original one will set this to false
  	
  	MessagesHolder mh= MessagesHolder.getInstance();
  	boolean jsonObjectCreated=false;
	Boolean jc;

  	try{
  		String tableName= jo.getString("table");
	  	Table table= manager.getTable(tableName);
	  	int[] masks=null;
	  	JSONArray column_masks=jo.optJSONArray("column_masks");
	  	if(column_masks==null)masks=nds.control.util.EditableGridMetadata.ITEM_COLUMN_MASKS;
	  	else{
	  		masks=new int[column_masks.length()];
	  		for(int i=0;i< masks.length;i++)masks[i]= column_masks.getInt(i);
	  	}
	  	logger.debug("masks:"+ nds.util.Tools.toString(masks));
	  	nds.control.util.EditableGridMetadata gm=new nds.control.util.EditableGridMetadata(table, locale,null,
  	  		masks);
  	  	template.setParameter("table", String.valueOf(table.getId()));
  	  	template.setParameter("fixedcolumns", jo.optString("fixedColumns"));
  	  	template.put("column_masks", column_masks);
  	  	
  	  	boolean errorFound=false; // when error found in add/modify/delete, will not try submit
  	  	boolean submitListFound=false;// when submit list found, and submit is done, will not load query result, and request ui to refreh dirty list immediately
  	  	//add
  		al= jo.getJSONArray("addList");
  		ArrayList colNames= gm.getColumnsWhenCreate();
  		for(int i=0;i< al.length();i++){
  			// these proecesses will cause whole process rolls back if any error occur
  			objectData=  al.getJSONArray(i);
  			evt= createEvent(objectData,colNames, template);
  			evt.setParameter("command", tableName+"Create");
  			jsonObjectCreated=false;
  			try{
  				vh=helper.handleEventWithNewTransaction(evt);
  				objectId= Tools.getInt( (Integer)vh.get("objectid"), -1);
  				
  				jc=(Boolean)vh.get("jsonObjectCreated");
  				if(jc!=null && jc.booleanValue() ==true)jsonObjectCreated=true;
  				errorMsg=null;
  			}catch(Throwable t){
  				logger.debug(t.getMessage(),t);
  				objectId=-1;
  				errorMsg=helper.getRootCauseMessage(t,locale);
  				errorFound=true;
  			}
  			rtRow=new JSONObject();
			rtRow.put("row", objectData.getInt(0));
			rtRow.put("objId", objectId); // -1 means error found
			rtRow.put("msg", errorMsg); // not null means error found
			rtRow.put("action","A");
			rtRow.put("jsoncreated", jsonObjectCreated?"T":"F");
			rtArrayResults.put(rtRow);
  		}
  		
  		//modify
  		al= jo.getJSONArray("modifyList");
  		colNames=gm.getColumnsWhenModify();
  		for(int i=0;i< al.length();i++){
  			// these proecesses will cause whole process rolls back if any error occur
  			objectData=  al.getJSONArray(i);
			//rowIdx= objectData.getInt(0);
  			evt= createEvent(objectData,colNames, template);
  			evt.setParameter("command",tableName+"Modify");
			objectId =Tools.getInt(evt.getParameterValue("id"),-1);;
  			jsonObjectCreated=false;
			
  			try{
  				vh=helper.handleEventWithNewTransaction(evt);
  				jc=(Boolean)vh.get("jsonObjectCreated");
  				if(jc!=null && jc.booleanValue() ==true)jsonObjectCreated=true;
  				errorMsg=null;
  			}catch(Throwable t){
  				logger.debug(t.getMessage(),t);
  				errorMsg=helper.getRootCauseMessage(t,locale);
  				errorFound=true;
  			}
  			rtRow=new JSONObject();
			rtRow.put("row", objectData.getInt(0));
			rtRow.put("objId", objectId);
			rtRow.put("msg", errorMsg); // not null means error found
			rtRow.put("action","M");
			rtRow.put("jsoncreated", jsonObjectCreated?"T":"F");
			
			rtArrayResults.put(rtRow);
  		}
  		
  		//delete
  		al= jo.getJSONArray("deleteList");
  		colNames= gm.getColumnsWhenDelete();
  		for(int i=0;i< al.length();i++){
  			// these proecesses will cause whole process rolls back if any error occur
  			objectData=  al.getJSONArray(i);
			//rowIdx= objectData.getInt(0);
  			evt= createEvent(objectData,colNames, template);
  			evt.setParameter("command",tableName+"Delete");
			objectId =Tools.getInt(evt.getParameterValue("id"),-1);;
  			try{
  				vh=helper.handleEventWithNewTransaction(evt);
  				errorMsg=null;
  			}catch(Throwable t){
  				logger.debug(t.getMessage(),t);
  				errorMsg= helper.getRootCauseMessage(t,locale);
  				errorFound=true;
  			}
  			rtRow=new JSONObject();
			rtRow.put("row", objectData.getInt(0));
			rtRow.put("objId", objectId);
			rtRow.put("msg", errorMsg); // not null means error found
			rtRow.put("action","D");
			rtArrayResults.put(rtRow);
  		}
  		returnObj.put("results",rtArrayResults );
  		logger.debug("results:"+ rtArrayResults);
  		
		if(errorFound==false){
			//try submit list
			al= jo.optJSONArray("submitList");
			if(al!=null && al.length()>0){
				submitListFound=true;
				//do submit on each line
				evt=(DefaultWebEvent)template.clone();
	  			evt.setParameter("command","ListSubmit"); 
	  			evt.put("itemid",nds.util.JSONUtils.toStringArray(al));
	  			try{
	  				vh=helper.handleEventWithNewTransaction(evt);
	  				returnObj.put("message", mh.translateMessage( (String)vh.get("message"),locale));
	  			}catch(Throwable t){
	  				logger.debug(t.getMessage(),t);
	  				returnObj.put("message", helper.getRootCauseMessage(t,locale));
	  			}
	  			// always refresh ui list
	  			returnObj.put("refresh",true);
			}
		}else{
			// no line submitted as error found
			al= jo.optJSONArray("submitList");
			if(al!=null && al.length()>0){
				returnObj.put("message", mh.translateMessage( "@no-submit-as-update-failed@",locale));
			}
		}
		// Only when no submit, will try update each line of front UI
  		if(!submitListFound){
  			returnObj.put("refresh",false);
	  		// for all those created and modified rows, reconstruct query result object
	  		// note some rows may no longer showable as status change 
	  		/*function QueryRequest(){
	  			this.table="C_OderItem";
	  			this.column_masks=[1,5];//Column.MASK_CREATE_EDIT,Column.MASK_QUERY_SUBLIST
	  			this.column_include_uicontroller=false; // optional, default false
	  			this.init_query=true;// opt, default to true
	  								 // if true, will use dir_perm and fixedColumns for where clause construction,
	  								 // else these params will be ignored since param_expr contains these filters
	  								 
	  			this.dir_perm=1; //opt, default to 1, Directory.READ, Directory.WRITE will be used in where clause construction 
	  			this.fixedColumns="fixed_columns_a=b&fixed_columns_c=d" // opt, will be used in where clause construction
	  			this.param_expr=Expression.toString() // optional
	  			this.start=0;
	  			this.range=10;
	  			this.order_column="";
	  			this.order_asc=false;	
	  			this.quick_search_column=""; //opt
	  			this.quick_search_data=""; // opt
	  			this.accepter_id="" // opt
	  		}*/
	  		
	  		JSONObject q=jo.getJSONObject("queryRequest");
	  		JSONObject qr=new JSONObject();
	  		qr.put("table", q.getString("table"));
	  		qr.put("column_masks", q.get("column_masks"));
	  		qr.put("column_include_uicontroller", q.optBoolean("column_include_uicontroller",false));
	  		qr.put("init_query", true);
	  		qr.put("dir_perm", q.optInt("dir_perm",1));
	  		qr.put("fixedColumns", q.optString("fixedColumns"));
	  		StringBuffer sb=new StringBuffer(" IN (-1");
	  		for(int i=0;i< rtArrayResults.length();i++){
	  			rtRow= rtArrayResults.getJSONObject(i);
	  			String action= rtRow.getString("action");
	  			int oid= rtRow.getInt("objId");
	  			if (oid !=-1 && ("A".equals(action ) || "M".equals(action)) &&  Validator.isNull(rtRow.optString("msg"))){
	  				// this is successful added or modified row
	  				sb.append(",").append(oid);
	  			}
	  		}
	  		sb.append(")");
	  		
	  		Expression expr=new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}), sb.toString(), null);
	  		qr.put("param_expr",expr );
	  		qr.put("start",0);
	  		qr.put("range", Integer.MAX_VALUE);
	  		
	  		logger.debug(qr.toString());
	  		QueryResult res=QueryEngine.getInstance().doQuery(AjaxUtils.parseQuery(qr, event.getQuerySession(),usr.getId().intValue(),locale ));
	  		
	  		returnObj.put("qresult", res);
  		}
  		
  		
  		
  	}catch(Throwable t){ 
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}
  	ValueHolder holder= new ValueHolder();
	holder.put("message", mh.translateMessage("@complete@",locale));
	holder.put("code","0");
	holder.put("data",returnObj );
	return holder;
  }
  
  private DefaultWebEvent createEvent(JSONArray row, ArrayList colNames, DefaultWebEvent template ) throws JSONException{
  	DefaultWebEvent e=(DefaultWebEvent)template.clone();
  	Object value;
  	for(int i=0;i< colNames.size();i++){
  		value=     row.get(i+1);
  		if(JSONObject.NULL.equals(value)) value=null;
  		e.put( (String)colNames.get(i),value); // since row(0) is always row index 
  	}
	e.put("JSONROW", row);// this could be used by some special command, such as B_V2_PRJ_TOKEModify
  	return e;
  }
}