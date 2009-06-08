package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AjaxUtils;
import nds.control.util.ValueHolder;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryResult;
import nds.query.QueryResultImpl;
import nds.query.SPResult;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.PairTable;
import nds.util.Tools;
import nds.util.Validator;
import nds.util.WebKeys;

import org.directwebremoting.WebContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProcessStep extends Command {
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
				this.masterobj={...}     // master object information, will update this first if exists, and will stop processing if error found
				this.inlineobj={...}     // inline object information, when object has ref-table and mapping type is 1:1
				this.addList=new Array();
				this.modifyList=new Array();
				this.deleteList=new Array();
				this.queryRequest=QueryRequest(){
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
	 * 			this.masterpage="html page content for master object";
	 *          this.masterid=124; -- master object id, may newly created
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
  	template.setParameter("nds.control.ejb.UserTransaction" , "Y"); // original one will set this to false
  	MessagesHolder mh= MessagesHolder.getInstance();
  	boolean jsonObjectCreated=false;
	Boolean jc;
	int masterObjectId=-1;
	JSONObject masterObj=null;
	Table masterTable=null;
	int mainAction=Table.MODIFY; //  Table.ADD or Table.MODIFY
	boolean errorFound=false;
	String returnMsg=null;
	boolean masterObjectCreateAction=false;//what's on master object, add or modify?
    QueryEngine engine = QueryEngine.getInstance() ;
    ArrayList params=null;
    int p_nextstep=-1;//表示在第几步
    String r_message="N";//是否是最后一步
  	try{
  		/*
  		 * Master object handling
  		 */
  		p_nextstep = Tools.getInt(jo.getString("p_nextstep"),-1);
  		masterObj=jo.getJSONObject("masterobj");
  		masterTable=manager.getTable( masterObj.getInt("table"));
  		masterObjectId= masterObj.getInt("id");
		logger.debug("masterTable="+ masterTable+",masterObjectId="+masterObjectId);
		if("N".equals(jo.getString("inline"))){
		// this will be master record, may create or modify
		evt=createSingleObjEvent(masterObj,template);
		if(Tools.getInt(masterObj.get("id"),-1)==-1){
			evt.setParameter("command", masterTable.getName()+"Create");
			masterObjectCreateAction=true;
			mainAction= Table.ADD;
		}else{
			evt.setParameter("command", masterTable.getName()+"Modify");
		}
			vh=helper.handleEventWithNewTransaction(evt);
			masterObjectId= Tools.getInt( (Integer)vh.get("objectid"), masterObjectId);
		  	params=new ArrayList();
		  	params.add(new Integer(masterObjectId));
		  	params.add(usr.getId());
		  	params.add(0);
		  	SPResult result=engine.executeStoredProcedure(masterTable.getName()+"_step", params, true);
		  	p_nextstep=result.getCode();
		  	r_message=result.getMessage();
			logger.debug("handled:masterObjectId="+masterObjectId);
		}
		String fixedColumns=null;
		/*
		 * Detail object handling
		 */
		boolean hasItems= jo.optBoolean("bestEffort", false);
		boolean hasItemAddList=false; // for item adding list checking
		if(hasItems){
	  		Table table;
			String tableName= jo.optString("table"); //masterObj.optString("table");
	  		int tableId= Tools.getInt(tableName, -1);
	  		if(tableId==-1){
	  			table=manager.getTable(tableName);
			  	if(table!=null) tableId= table.getId();
	  		}else{
	  			table= manager.getTable(tableId);
	  			if(table!=null) tableName= table.getName();
	  		} 
	  		if(table==null) throw new NDSRuntimeException("Invalid item table: "+ tableName);
	  		
	  		fixedColumns=jo.optString("fixedColumns");
	  		/**
  			 * 需要额外处理新增界面下，订单明细与订单头同时保存的情况, 头的ID 将作为fixedcolumn 插入到明细
  			 */
  			//if(masterObjectCreateAction){
  				PairTable pt =PairTable.parse(fixedColumns, null);
  				Column pfk=manager.getParentFKColumn(table);
  				if(pfk!=null){
  					pt.put(String.valueOf( pfk.getId()), String.valueOf(masterObjectId));
  	  				fixedColumns= pt.toParseString(null);
  				}
  			//}
	  	  	nds.control.util.EditableGridMetadata gm=new nds.control.util.EditableGridMetadata(table, locale,null,
	  	  		nds.control.util.EditableGridMetadata.ITEM_COLUMN_MASKS);
	  	  	template.setParameter("table", String.valueOf(tableId));
	  	  	if(fixedColumns!=null)template.setParameter("fixedcolumns", fixedColumns);
	  	  	template.put("column_masks", jo.optJSONArray("column_masks"));
	  	  	//add
	  	  	ArrayList colNames;	
	  		al= jo.optJSONArray("addList");
	  		if(al!=null){
	  			hasItemAddList=true;
	  			colNames= gm.getColumnsWhenCreate();			
		  		for(int i=0;i< al.length();i++){
		  			// these proecesses will cause whole process rolls back if any error occur
		  			objectData=  al.getJSONArray(i);
		  			evt= createEvent(objectData,colNames, template);		  			
		  			evt.setParameter("command",tableName+"Create");
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
		  				errorMsg=helper.getRootCauseMessage(t, locale);
		  				errorFound=true;
		  			}
		  		}
	  		}
	  		//modify
	  		al= jo.optJSONArray("modifyList");
	  		if(al!=null){
	  		colNames=gm.getColumnsWhenModify();
	  		for(int i=0;i< al.length();i++){
	  			// these proecesses will cause whole process rolls back if any error occur
	  			objectData=  al.getJSONArray(i);
				//rowIdx= objectData.getInt(0);
	  			evt= createEvent(objectData,colNames, template);
	  			evt.setParameter("command",tableName+"Modify");
				objectId =Tools.getInt(evt.getParameterValue("id"),-1);
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
	  		}
	  		}
	  		
	  		//delete
	  		al= jo.optJSONArray("deleteList");
	  		if(al!=null){
	  		colNames= gm.getColumnsWhenDelete();
	  		for(int i=0;i< al.length();i++){
	  			// these proecesses will cause whole process rolls back if any error occur
	  			objectData=  al.getJSONArray(i);
				//rowIdx= objectData.getInt(0);
	  			evt= createEvent(objectData,colNames, template);
	  			evt.setParameter("command",tableName+"Delete");
				objectId =Tools.getInt(evt.getParameterValue("id"),-1);
	  			try{
	  				vh=helper.handleEventWithNewTransaction(evt);
	  				errorMsg=null;
	  			}catch(Throwable t){
	  				logger.debug(t.getMessage(),t);
	  				errorMsg= helper.getRootCauseMessage(t,locale);
	  				errorFound=true;
	  			}
	  		}
	  		}
		  	params=new ArrayList();
		  	params.add(new Integer(masterObjectId));
		  	params.add(usr.getId());
		  	params.add(p_nextstep);
		  	if(!"Y".equals(jo.getString("inlineflag"))){
			  	SPResult result=engine.executeStoredProcedure(masterTable.getName()+"_step", params, true);
			  	p_nextstep=result.getCode();
			  	r_message=result.getMessage();
		  	}
		}// end hasItems
  		/**May submit immediately*/
  		boolean bSubmit=Tools.getYesNo( jo.optString("submitAfterSave"), false);
  		
  		if(bSubmit && !errorFound){
	    	DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
	    	dwe.setParameter("nds.control.ejb.UserTransaction" , "Y"); // original one set this to "N"
	    	dwe.setParameter("command", masterTable.getName()+"Submit" );
	    	dwe.setParameter("id",	String.valueOf(masterObjectId));
	    	dwe.setParameter("parsejson","Y");
	    	ValueHolder vh2=helper.handleEventWithNewTransaction(dwe);
	    	if(Tools.getInt(vh2.get("code"), -1) !=0){
	    		throw new NDSEventException((String)vh2.get("message") );
	    	}else{
	    		returnMsg= (String)vh2.get("message");
	    		params=new ArrayList();
			  	params.add(new Integer(masterObjectId));
			  	params.add(usr.getId());
			  	params.add(p_nextstep);
			  	SPResult result=engine.executeStoredProcedure(masterTable.getName()+"_step", params, true);
				p_nextstep=result.getCode();
				r_message=result.getMessage();
	    	}
  		}
  		boolean bUnSubmit=Tools.getYesNo( jo.optString("unsubmit"), false);
  		if(bUnSubmit){
	    	DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
	    	dwe.setParameter("nds.control.ejb.UserTransaction" , "Y"); // original one set this to "N"
	    	dwe.setParameter("command", masterTable.getName()+"Unsubmit" );
	    	dwe.setParameter("id",	String.valueOf(masterObjectId));
	    	dwe.setParameter("parsejson","Y");
	    	ValueHolder vh2=helper.handleEventWithNewTransaction(dwe);
	    	if(Tools.getInt(vh2.get("code"), -1) !=0){
	    		throw new NDSEventException((String)vh2.get("message") );
	    	}else{
	    		returnMsg= (String)vh2.get("message");
	    		params=new ArrayList();
			  	params.add(new Integer(masterObjectId));
			  	params.add(usr.getId());
			  	params.add(p_nextstep);
			  	SPResult result=engine.executeStoredProcedure(masterTable.getName()+"_step", params, true);
				p_nextstep=result.getCode();
				r_message=result.getMessage();
	    	}
  		}
  		
	  	String nextScreen="";
	  	/*
	  	 * nextstep表示在第几步以便页面上一步和下一步的跳转
	  	 * 当不是最后一步的时候p_nextstep是表示第几步,当是最后一步的时候p_nextstep=-2表示是最后一步
	  	 * 
	  	 */
	  	if(r_message.equals("Y")){
			  nextScreen=WebKeys.NDS_URI+"/step/index.jsp?table="+ masterTable.getId()+"&id="+ masterObjectId+
			  (masterObj.optString("fixedcolumns")!=null?"&p_nextstep=-2&nextstep="+p_nextstep+"&fixedcolumns="+ masterObj.optString("fixedcolumns"):"");
	  	}else{
			  nextScreen=WebKeys.NDS_URI+"/step/index.jsp?table="+ masterTable.getId()+"&id="+ masterObjectId+
			  (masterObj.optString("fixedcolumns")!=null?"&p_nextstep="+p_nextstep+"&fixedcolumns="+ masterObj.optString("fixedcolumns"):"");
	  	}
		returnObj.put("nextscreen", nextScreen);	  	
  	}catch(Throwable t){
  		logger.error("exception",t);
  		throw new NDSException(nds.util.StringUtils.getRootCause(t).getMessage(), t);
  	}
  	ValueHolder holder= new ValueHolder();
  	if(errorFound)
  		holder.put("message", mh.translateMessage("@contains-error@",locale));
  	else{		
  		holder.put("message",(returnMsg==null? mh.translateMessage("@complete@",locale):returnMsg) );
  	}
	holder.put("code","0");
	holder.put("data",returnObj );
	return holder;
  }
  /**
   * Get table , try id first, then name
   * @param tableIdOrName
   * @return null if table not found
   */
  private Table findTable(Object tableIdOrName){
	  if(tableIdOrName==null) return null;
	  int id=Tools.getInt(tableIdOrName, -1);
	  if(id!=-1){
		  return TableManager.getInstance().getTable(id);
	  }else{
		  return TableManager.getInstance().getTable(tableIdOrName.toString());
	  }
  }
  private DefaultWebEvent createSingleObjEvent(JSONObject obj, DefaultWebEvent template  ) throws JSONException{
	  	DefaultWebEvent e=(DefaultWebEvent)template.clone();
	  	e.setParameter("command","ObjectCreate");
	  	for(Iterator it=obj.keys();it.hasNext();){
	  		String key= (String)it.next();
	  		Object o=  obj.get(key);
	  		if(o!=null && o instanceof String) o= ((String)o).trim();
	  		e.put(key, o); 
	  	}
		return e;
	  }
  private DefaultWebEvent createEvent(JSONArray row, ArrayList colNames, DefaultWebEvent template ) throws JSONException{
  	DefaultWebEvent e=(DefaultWebEvent)template.clone();
  	for(int i=0;i< colNames.size();i++){
  		Object o=  row.get(i+1);
  		if(o!=null && o instanceof String) o= ((String)o).trim();
  		e.put( (String)colNames.get(i),o); // since row(0) is always row index
  	}
	e.put("JSONROW", row);// this could be used by some special command, such as B_V2_PRJ_TOKEModify
  	return e;
  }
}