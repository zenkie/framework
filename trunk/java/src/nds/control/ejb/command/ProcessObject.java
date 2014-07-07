package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.jfree.util.Log;
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
 * Update exising object with grid data, this is for ajax request on Object page
 * for new object, use ProcessObjectNew instead. The main difference between both is 
 * that ProcessObject is multiple transaction process, while ProcessObjectNew is single transaction 
 *   
 */
public class ProcessObject extends Command {
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(DefaultWebEvent event){
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
				this.printAfterSubmit=false//default to false, if true, will print after submit successfully
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
	 *          this.spresult={code:1,message:'alert("hello")'} -- main object's ac/am execution result, 
	 *            for code spec, see DefaultWebEventHelper#doTrigger
	 * 		}
	 	row is the row index of this.data in GridControl
	 	
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
  	WebContext ctx=(WebContext) jo.opt("org.directwebremoting.WebContext");
  	
  	JSONObject returnObj=new JSONObject();
  	JSONArray al, objectData, rtArrayResults=new JSONArray();
  	JSONObject rtRow;
  	DefaultWebEvent evt,template;
  	ValueHolder vh;
  	int rowIdx, objectId;
  	String errorMsg=null;
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
	boolean masterObjectCreateAction=false; //what's on master object, add or modify?
	SPResult spr=null; // main object's ac/am procedure result 
	SPResult sbr=null;// submit result 
	logger.debug("jsonObject   is  ->"+jo.toString());
  	try{
  		/*
  		 * Master object handling
  		 */
  		masterObj=jo.getJSONObject("masterobj");
  		masterTable=manager.findTable( masterObj.get("table"));

  		masterObjectId= masterObj.getInt("id");
		logger.debug("masterTable="+ masterTable+",masterObjectId="+masterObjectId);
		
		// this will be master record, may create or modify
		evt=createSingleObjEvent(masterObj,template);
		
		masterObjectId=evt.getObjectId(masterTable, usr.adClientId);
		if(masterObjectId==-1){
			evt.setParameter("command", masterTable.getName()+"Create");
			masterObjectCreateAction=true;
			mainAction= Table.ADD;
		}else{
			//check table records exist and modifiable, object modify will check it first
	 	   	//helper.checkTableRowsModifiable(masterTable, new int[]{masterObjectId}, conn);
			evt.setParameter("id", String.valueOf(masterObjectId));// 强制设置id,减少检索ID的操作
			evt.setParameter("command", masterTable.getName()+"Modify");
		}
		vh=helper.handleEventWithNewTransaction(evt);
		
		masterObjectId= Tools.getInt( (Integer)vh.get("objectid"), masterObjectId);
		logger.debug("handled:masterObjectId="+masterObjectId);
  		spr=(SPResult)vh.get("spresult");
  		returnObj.put("spresult", spr);

  		/*
		 *Inline single object (1:1) handling  
		 */
		JSONObject inlineObject= jo.optJSONObject("inlineobj");
		if(inlineObject!=null){
			Table inlineTable=manager.findTable( inlineObject.opt("table"));
			evt=createSingleObjEvent(inlineObject,template);
			
			int inlineObjectId=evt.getObjectId(inlineTable, usr.adClientId);
			
			if(inlineObjectId==-1){
				evt.setParameter("command", inlineTable.getName()+"Create");
				// unless main object request special ui action, refresh inner tab
				if(spr!=null){
					if(spr.getCode()==0){
						spr.setCode(3);//refresh list
					}
				}
			}else{
				evt.setParameter("id", String.valueOf(inlineObjectId));// 强制设置id,减少检索ID的操作
				evt.setParameter("command", inlineTable.getName()+"Modify");
			}
			vh=helper.handleEventWithNewTransaction(evt);
			//内嵌对象若为新增，强制要求刷新整个界面，避免重复创建 2010-7-30 by yfzhu
			if(inlineObjectId==-1){
				SPResult spr2=new SPResult(1,null );
				returnObj.put("spresult", spr2);
			}	
			inlineObjectId= Tools.getInt( (Integer)vh.get("objectid"), masterObjectId);
			logger.debug("handled:inlineObject="+inlineObjectId);
		}
		String fixedColumns=null;
		/*
		 * inline multiple objects (1:m) handling
		 */
		boolean hasItems= jo.optBoolean("bestEffort", false);
		boolean hasItemAddList=false; // for item adding list checking
		if(hasItems){
	  		Table table= manager.findTable(jo.opt("table"));
	  		if(table==null) throw new NDSRuntimeException("Invalid item table: "+ jo.opt("table"));
			String tableName=table.getName();
	  		int tableId= table.getId();
	  		
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
	  	  	nds.control.util.EditableGridMetadata gm=new nds.control.util.EditableGridMetadata(table, locale,usr.getSecurityGrade(),
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
		  			rtRow=new JSONObject();
					rtRow.put("row", objectData.getInt(0));
					rtRow.put("objId", objectId); // -1 means error found
					rtRow.put("msg", errorMsg); // not null means error found
					rtRow.put("action","A");
					rtRow.put("jsoncreated", jsonObjectCreated?"T":"F");
					rtArrayResults.put(rtRow);
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
	  		}
	  		//按照要求进行修改 
	  		//如果全部都正确，并且明细表是nds.schema.AttributeDetailSupportTableImpl，并且支持当前界面做了新增动作
	  		// 则不返回明细而直接要求刷新界面 （因为这些表都会合并条码）
	  		// yfzhu 2009-12-1
	  		if(!errorFound && (table instanceof nds.schema.AttributeDetailSupportTableImpl) &&  
	  				(jo.optJSONArray("addList")!=null && jo.optJSONArray("addList").length()>0)){
	  			returnObj.put("refresh",true );
	  			
	  		}else{
	  			returnObj.put("results",rtArrayResults );
	  			//logger.debug("results:"+ rtArrayResults);
	  		}
		}// end hasItems
  		/**May submit immediately*/
  		boolean bSubmit=Tools.getYesNo( jo.optString("submitAfterSave"), false);
  		int bwebact=Tools.getInt(jo.optString("actionAfterSave"),0);
  		boolean bPrintAfterSubmit=Tools.getYesNo( jo.optString("printAfterSubmit"), false);
  		String printFileName=null;
  		if(bwebact!=0){
  			DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
  			dwe.setParameter("command", "ExecuteWebAction");
  			JSONObject ac=new JSONObject();
  			ac.put("webaction",bwebact);
  			ac.put("objectid", String.valueOf(masterObjectId));
  			HttpServletRequest request = ctx.getHttpServletRequest();
  			ac.put("javax.servlet.http.HttpServletRequest", request);
  			dwe.put("JSONOBJECT", ac);
  			ValueHolder vh2=helper.handleEventWithNewTransaction(dwe);
  			logger.debug("!!!!!!!test:"+(String)vh2.get("message"));
  			return vh2;
  		}
  		if(bSubmit && !errorFound){
	    	DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
	    	dwe.setParameter("nds.control.ejb.UserTransaction" , "Y"); // original one set this to "N"
	    	dwe.setParameter("command", masterTable.getName()+"Submit" );
	    	dwe.setParameter("id",	String.valueOf(masterObjectId));
	    	dwe.setParameter("printAfterSubmit", bPrintAfterSubmit?"Y":"N");
	    	if(ctx!=null){
		    	dwe.put("org.directwebremoting.WebContext", ctx);// need for PrintJasper
	    	}
	    	ValueHolder vh2=helper.handleEventWithNewTransaction(dwe); 
	    	  /**
             * 修改submit 方法支持rcode 返回
             * 101 刷新不关闭
             */
	  		sbr=(SPResult)vh2.get("sbresult");
	  		returnObj.put("sbresult", sbr);
	    	
	    	if(Tools.getInt(vh2.get("code"), -1) !=0&& Tools.getInt(vh2.get("code"), -1) !=101){
	    		throw new NDSEventException((String)vh2.get("message") );
	    	}else{
	    		returnMsg= (String)vh2.get("message");
	    		if(bPrintAfterSubmit){
	    			printFileName=  ( (JSONObject)vh2.get("data")).optString("printfile");
	    			if(nds.util.Validator.isNotNull(printFileName))returnObj.put("printfile", printFileName);
	    		}
	    	}
  		}else{// no submit
  		
	  		
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
	  		
	  		JSONObject q=jo.optJSONObject("queryRequest");
	  		JSONObject qr=new JSONObject();
	  		if(q!=null){
	  			
		  		Table qTable= TableManager.getInstance().findTable(q.get("table"));
		  		qr.put("table", q.get("table"));
		  		qr.put("column_masks", q.get("column_masks"));
		  		qr.put("column_include_uicontroller", q.optBoolean("column_include_uicontroller",false));
		  		qr.put("init_query", true);
		  		qr.put("dir_perm", q.optInt("dir_perm",1));
		  		qr.put("fixedColumns", q.optString("fixedColumns"));
		  		StringBuffer sb=new StringBuffer(" IN (");
		  		boolean isNoData=true;   
		  		for(int i=0;i< rtArrayResults.length();i++){
		  			rtRow= rtArrayResults.getJSONObject(i);
		  			String action= rtRow.getString("action");
		  			int oid= rtRow.getInt("objId");
		  			if (oid !=-1 && ("A".equals(action ) || "M".equals(action)) &&  Validator.isNull(rtRow.optString("msg"))){
		  				// this is successful added or modified row
		  				if(isNoData)sb.append(oid);
		  				else sb.append(",").append(oid);
		  				isNoData=false;
		  			}
		  		}
		  		if(isNoData)sb.append("-1");
		  		sb.append(")");
		  		QueryResult res=null;
		  		Expression expr=new Expression(new ColumnLink(new int[]{qTable.getPrimaryKey().getId()}), sb.toString(), null);
		  		qr.put("param_expr",expr );
		  		qr.put("start",0);
		  		qr.put("range", Integer.MAX_VALUE);
		  		logger.debug(qr.toString());
			  	QueryRequestImpl quest=	AjaxUtils.parseQuery(qr, event.getQuerySession(),usr.getId().intValue(),locale )	;		  		
		  		if(!isNoData){
			  		
			  		res=QueryEngine.getInstance().doQuery(quest);
		  		}else{
		  			res=QueryEngine.getInstance().doDummyQuery(quest, "no data");
		  		}
		  		// convert button
		  		if(ctx!=null){
			  		HttpServletRequest request = ctx.getHttpServletRequest();
			  		nds.control.util.AjaxUtils.convertButtonHtml((QueryResultImpl)res, request);
		  		}
		  		returnObj.put("qresult",res.toJSONObject(true));
	  		}
	  		
	  		// where to direct?
	  		// if table is no longer viewable, only send a message back to client, and notify closing
	  		// else show object content still
	
	  		// check object viewable for current use?
	  		if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.getId().intValue(),usr.getName(), 
	  				masterTable.getName(), masterObjectId,nds.security.Directory.WRITE ,event.getQuerySession())){
	  			returnObj.put("message",mh.translateMessage("@object-saved-but-status-changed@",locale));
	  			returnObj.put("closewindow",true);// issue close
	  		}else{
	  			String serverPath=WebKeys.NDS_URI;
	  			if(masterTable.getSysmodel()!=null&&masterTable.getSysmodel().getMlink()!=null){
	  			    serverPath=masterTable.getSysmodel().getMlink();
	  			}
		  		if(mainAction== Table.ADD && !errorFound && !hasItemAddList){
		  			String nextScreen=serverPath+"/object/object.jsp?table="+ masterTable.getId()+"&id="+ masterObjectId+
		  			(masterObj.optString("fixedcolumns")!=null?"&fixedcolumns="+ masterObj.optString("fixedcolumns"):"");
		  			returnObj.put("nextscreen", nextScreen);
		  		}else{
			  		/* 
			  		 * Send a modified page for master object
			  		 */
			  		returnObj.put("masterid", masterObjectId);
					WebContext wc=(WebContext) jo.get("org.directwebremoting.WebContext");
					/**
					 * Please note param "compress=false" is to prohibit  com.liferay.filters.compression.CompressionFilter from compressing file content 
					 */
					String page=wc.forwardToString(serverPath+"/object/ajax_object.jsp?compress=f&table="+masterObj.get("table")+"&id="+ masterObjectId);
					returnObj.put("masterpage", page);
					// and for  mainAction== Table.ADD, reconstruct object page url for refresh and fixedcolumns information
					if(mainAction== Table.ADD){
						returnObj.put("url",serverPath+"/object/object.jsp?table="+ masterObj.get("table")+"&id="+ masterObjectId);
						returnObj.put("fixecolumnstr",fixedColumns);
						returnObj.put("fixecolumns",PairTable.parse(fixedColumns, null).toJSONString());
					}
		  		}
	  		}
  		}//end not submit
  		
  	}catch(Throwable t){
  		logger.error("exception",t);
  		//throw new NDSException(t.getMessage(), t);
  		throw new NDSException(nds.util.StringUtils.getRootCause(t).getMessage(), t);
  	}
  	ValueHolder holder= new ValueHolder();
  	if(errorFound){
  		holder.put("code","-1");
  		if(errorMsg!=null){
  			holder.put("message", errorMsg);
  		}else{
  		holder.put("message", mh.translateMessage("@contains-error@",locale));
  		}
  	}else{
  		holder.put("code","0");
  		holder.put("message",(returnMsg==null? mh.translateMessage("@complete@",locale):returnMsg) );
  	}
	holder.put("data",returnObj );
	return holder;
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
	  try{
		  DefaultWebEvent e=(DefaultWebEvent)template.clone();
	  		for(int i=0;i< colNames.size();i++){
		  		Object o=  row.get(i+1);
		  		if(o!=null && o instanceof String) o= ((String)o).trim();
		  		e.put( (String)colNames.get(i),o); // since row(0) is always row index
	  		}
	  		e.put("JSONROW", row);// this could be used by some special command, such as B_V2_PRJ_TOKEModify
	  	return e;
	  }catch(org.json.JSONException t){
		  logger.error("Fail to create event :"+ t.getMessage()+
				  ", row:"+ row.join(",")+";colunm names:"+ Tools.toString(colNames,","), t);
		  
		  throw t;
	  }
  }
}