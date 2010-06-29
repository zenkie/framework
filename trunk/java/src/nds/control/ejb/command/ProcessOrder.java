package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
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
 * for REST 
处理单据，如同时创建头和明细记录
命令参数：
	pre_actions:[action,...]//连续动作，在操作前的行为，action需要参考单据上的动作定义，常用的如:WebAction.对于网站订单，例如将取消付款，然后修改付款明细。pre_actions仅能用于修改单据的情况。
	masterobj:{ //头表记录对象
		table: 头表id
		id: 对应记录的ID, -1 表示新增，除非设置了“ak”或id_find内容。否则为修改
		<column-name>:<column-value> , 具体表的字段和值，参考ObjectCreate, ObjectModify
		...
	},
	detailobjs:{//明细表记录对象
		one-table-objs:[one-table-obj,...] //当明细有多个表时(如POS单据明细有商品明细和付款明细，网站零售单有商品明细和促销明细，将依次处理每个明细表）
		one-table-obj:{
			table: 明细表id
			addList:[add-obj,...]
				add-obj:{
					<column-name>:<column-value> , 具体表的字段和值，参考ObjectCreate
				}
			modifyList:[modify-obj,...]
				modify-obj:{
					<column-name>:<column-value> , 具体表的字段和值，参考ObjectModify
				}
			deleteList:	[delete-obj,...]
				delete-obj:{
					id: 记录ID，对于不知道ID的情况，可以设置 "ak" （alternate key）对应值来定位, 或通过 id_find 属性配置
				}
		}
	},
	post_actions:[action,...] //连续动作，在完成增删改调整后的行为，action需要参考单据上的动作定义，常用的如:ObjectSubmit.对于网站订单，则还需要进行配送分析（E_SHIPPING_ANALYZE）和付款(e_retail_pay)
						// 所有非常规动作都会作为WebAction(单对象存储过程)被检索和运行
返回补充数据：
	objectid:当主表为创建时，返回创建的主表记录的ID
	
ExecuteWebAction	执行网站单对象界面的定制按钮动作，如网络订单上的付款按钮
	objectid: 记录的ID，对于不知道ID的情况，可以设置 "ak" （alternate key）对应值来定位, 或通过 id_find 属性配置
	webaction:动作的ID
返回补充数据：
	data：JSON 格式的对象，具体看各个动作的返回内容，一般包括 code 和 message

 */
public class ProcessOrder extends Command {
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	return false;
    }
    /**
     * Single transaction
     */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
  	//WebContext ctx=(WebContext) jo.opt("org.directwebremoting.WebContext");
  	
  	JSONObject returnObj=new JSONObject();
  	JSONArray al, objectData, rtArrayResults=new JSONArray();
  	JSONObject rtRow;
  	DefaultWebEvent evt,template;
  	ValueHolder vh;
  	int rowIdx, objectId;
  	String errorMsg;
  	User usr=helper.getOperator(event);
  	int adClientId= usr.adClientId;
  	java.util.Locale locale= event.getLocale();
  	template=(DefaultWebEvent)event.clone();
  	template.getData().remove("jsonObject".toUpperCase());
  	//  original one will set this to false, when set to "N", the event command 
  	//  will manager its own transaction including commit and rollback operation
  	template.setParameter("nds.control.ejb.UserTransaction" , "Y"); 
  	
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
	java.sql.Connection conn=null;
  	try{
  		conn= QueryEngine.getInstance().getConnection();
  		
  		/*
  		 * Master object handling
  		 */
  		masterObj=jo.getJSONObject("masterobj");
  		masterTable=manager.findTable( masterObj.get("table"));

  		masterObjectId= event.getObjectIdByJSON(masterObj, masterTable, adClientId, conn);
		logger.debug("masterTable="+ masterTable+",masterObjectId="+masterObjectId);
		
		// preactions
		if(masterObjectId!=-1){
			JSONArray pre_actions=jo.optJSONArray("pre_actions");//id of webaction
			if(pre_actions!=null)for(int i=0;i< pre_actions.length();i++){
				evt=createWebActionEvent(jo,masterObjectId, pre_actions.getInt(i));
				vh=helper.handleEvent(evt);
			}
		}
		
		// this will be master record, may create or modify
		evt=createSingleObjEvent(masterObj,template);
		
		if(masterObjectId==-1){
			evt.setParameter("command", masterTable.getName()+"Create");
			masterObjectCreateAction=true;
			mainAction= Table.ADD;
		}else{
			
			//check table records exist and modifiable, object modify will check it first
	 	   	//helper.checkTableRowsModifiable(masterTable, new int[]{masterObjectId}, conn);
			evt.setParameter("id", String.valueOf(masterObjectId));// 强制设置id,减少检索ID的操作
			evt.setParameter("command", masterTable.getName()+"Modify");
			//partial update
			evt.setParameter("partial_update", "true");
		}
		vh=helper.handleEvent(evt); // single transaction
		
		masterObjectId= Tools.getInt( (Integer)vh.get("objectid"), masterObjectId);
		logger.debug("handled:masterObjectId="+masterObjectId);
		returnObj.put("objectid",masterObjectId );
		
//  		spr=(SPResult)vh.get("spresult");
//  		returnObj.put("spresult", spr);


  		/**
  		 * detailobjs, contains RefByTable#id
  		 */
  		JSONObject detailobjs= jo.optJSONObject("detailobjs");
  		if(detailobjs!=null){
  	  		JSONArray reftables= detailobjs.optJSONArray("reftables"); //elements are id of  RefByTable
  	  		JSONArray tables= detailobjs.optJSONArray("tables"); //elements are id/ak/alais of RefByTable table
  	  		
  	  		JSONArray rft;
  	  		if(reftables==null) rft=tables;
  	  		else rft= reftables;
  	  		if(rft==null) throw new NDSException("neither reftables nor tables is set");
  	  		
  	  		JSONArray refobjs= detailobjs.getJSONArray("refobjs"); //elements are object or list
  	  		ArrayList refbyTables=masterTable.getRefByTables();
  	  		RefByTable rbt=null;

	  		for(int dojIdx=0;dojIdx<rft.length();dojIdx++ ){
	  			if( reftables!=null){
	  				//load from reftables
		  			int refByTableId= reftables.getInt(dojIdx);
		  			boolean rbTFound=false;
		  			for(int rbTIdx=0;rbTIdx<refbyTables.size();rbTIdx++){
		  				rbt=(RefByTable) refbyTables.get(rbTIdx);
		  				if(rbt.getId()== refByTableId){
		  					rbTFound=true;
		  					break;
		  				}
		  			}
		  			if(!rbTFound)throw new NDSException("detailobjs:"+refByTableId+ " not found in master refby tables("+masterTable+")");
	  			}else{
	  				//load from tables
	  				Table rftt=manager.findTable(tables.opt(dojIdx));
	  				boolean rbTFound=false;
	  				for(int rbTIdx=0;rbTIdx<refbyTables.size();rbTIdx++){
		  				rbt=(RefByTable) refbyTables.get(rbTIdx);
		  				if(rbt.getTableId()== rftt.getId()){
		  					rbTFound=true;
		  					break;
		  				}
		  			}
		  			if(!rbTFound)throw new NDSException("detailobjs:"+rftt+ " not found in master refby tables("+masterTable+")");
		  			
	  			}
	  			if(rbt.getAssociationType()==RefByTable.ONE_TO_ONE){
	  				/*
	  				 *Inline single object (1:1) handling, for parent id, must using "ak" or "id_find" method
	  				 *,since web ui does not support such case
	  				 */
	  				JSONObject inlineObject=refobjs.getJSONObject(dojIdx);
	  				Table inlineTable=manager.findTable( inlineObject.get("table"));
	  				evt=createSingleObjEvent(inlineObject,template);
	  				
	  				int inlineObjectId=evt.getObjectId(inlineTable, usr.adClientId);
	  				
	  				if(inlineObjectId==-1){
	  					evt.setParameter("command", inlineTable.getName()+"Create");
	  				}else{
	  					evt.setParameter("id", String.valueOf(inlineObjectId));// 强制设置id,减少检索ID的操作
	  					evt.setParameter("command", inlineTable.getName()+"Modify");
	  					//partial update
	  					evt.setParameter("partial_update", "true");

	  				}
	  				vh=helper.handleEvent(evt);
	  				inlineObjectId= Tools.getInt( (Integer)vh.get("objectid"), masterObjectId);
	  				logger.debug("handled:inlineObject="+inlineObjectId);
	  			}else{
	  				/*
	  				 * List (1:m), since no fixed column handling, for parent id, we will
	  				 * compose fixedcolumns for handling 
	  				 */
	  				JSONObject listObject=refobjs.getJSONObject(dojIdx);
	  				int tableId=rbt.getTableId();
	  				Table table=manager.getTable(tableId);
	  				String tableName= table.getName();
	  				String fixedColumns=null;
	  		    	PairTable pt=new PairTable();
	  		    	Column pfk=manager.getParentFKColumn(table);
	  				if(pfk!=null){
	  					pt.put(String.valueOf( pfk.getId()), String.valueOf(masterObjectId));
	  	  				fixedColumns= pt.toParseString(null);
	  				}
	  		  	  	template.setParameter("table", String.valueOf(tableId));
	  		  	  	if(fixedColumns!=null)template.setParameter("fixedcolumns", fixedColumns);

	  		  	  	nds.control.util.EditableGridMetadata gm=new nds.control.util.EditableGridMetadata(table, locale,usr.getSecurityGrade(),
	  			  	  		nds.control.util.EditableGridMetadata.ITEM_COLUMN_MASKS);
	  		  	  	
	  		  	  	//add
	  		  	  	//ArrayList colNames;	
	  		  		al= listObject.optJSONArray("addList");
	  		  		if(al!=null){
	  		  			// it's low-performance to handle item one by one, since ObjectCreate support batch handling
	  		  			/*
	  		  			colNames= gm.getColumnsWhenCreate();
	  			  		for(int i=0;i< al.length();i++){
	  			  			// these proecesses will cause whole process rolls back if any error occur
	  			  			evt= createSingleObjEvent(al.getJSONObject(i),template);
	  			  			evt.setParameter("command",tableName+"Create");
	  			  			try{
	  			  				vh=helper.handleEvent(evt);
	  			  			}catch(Throwable t){
	  			  				logger.error(t.getMessage(),t);
	  			  				throw new NDSException("add item "+i+" of "+tableName+":"+ helper.getRootCauseMessage(t, locale));
	  			  			}
	  			  		}*/
	  		  			evt= createBatchCreateEvent(al,template,table);
	  		  			vh=helper.handleEvent(evt);
	  		  		}
	  		  		//modify
	  		  		al= listObject.optJSONArray("modifyList");
	  		  		if(al!=null){
		  		  		//colNames=gm.getColumnsWhenModify();
		  		  		for(int i=0;i< al.length();i++){
	  			  			evt= createSingleObjEvent(al.getJSONObject(i),template);
		  		  			evt.setParameter("command",tableName+"Modify");
		  					//partial update
		  					evt.setParameter("partial_update", "true");
		  					
		  		  			try{
		  		  				vh=helper.handleEvent(evt);
		  		  			}catch(Throwable t){
	  			  				logger.error(t.getMessage(),t);
	  			  				throw new NDSException("modify item "+i+" of "+tableName+":"+ helper.getRootCauseMessage(t, locale));
		  		  			}
		  		  		}
	  		  		}
	  		  		
	  		  		//delete
	  		  		al= listObject.optJSONArray("deleteList");
	  		  		if(al!=null){
		  		  		//colNames= gm.getColumnsWhenDelete();
		  		  		for(int i=0;i< al.length();i++){
	  			  			evt= createSingleObjEvent(al.getJSONObject(i),template);
		  		  			evt.setParameter("command",tableName+"Delete");
		  		  			try{
		  		  				vh=helper.handleEvent(evt);
		  		  				errorMsg=null;
		  		  			}catch(Throwable t){
	  			  				logger.error(t.getMessage(),t);
	  			  				throw new NDSException("delete item "+i+" of "+tableName+":"+ helper.getRootCauseMessage(t, locale));
		  		  			}
		  		  		}
	  		  		}
	  			}
	  		}
  		}// end detailobjs!=null

  		//postactions
		if(masterObjectId!=-1){
			
			JSONArray post_actions=jo.optJSONArray("post_actions");//id of webaction
			if(post_actions!=null)for(int i=0;i< post_actions.length();i++){
				evt=createWebActionEvent(jo,masterObjectId, post_actions.getInt(i));
				evt.getJSONObject().put("objectid", masterObjectId);
				vh=helper.handleEvent(evt);
			}
		}  		
		//submit
		boolean bSubmit=jo.optBoolean("submit",false);
  		if(bSubmit){
	    	DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
	    	dwe.setParameter("command", masterTable.getName()+"Submit" );
	    	dwe.setParameter("id",	String.valueOf(masterObjectId));
	    	ValueHolder vh2=helper.handleEvent(dwe); 
	    	if(Tools.getInt(vh2.get("code"), -1) !=0){
	    		throw new NDSEventException((String)vh2.get("message") );
	    	}else{
	    		returnMsg= (String)vh2.get("message");
	    	}
  		}		
  		
  	}catch(Throwable t){
  		logger.error("exception",t);
  		//throw new NDSException(t.getMessage(), t);
  		throw new NDSException(nds.util.StringUtils.getRootCause(t).getMessage(), t);
  	}finally{
  		try{conn.close();}catch(Throwable t){}
  	}
  	ValueHolder holder= new ValueHolder();
	holder.put("message",(returnMsg==null? mh.translateMessage("@complete@",locale):returnMsg) );
  	
	holder.put("code","0");

	holder.put("restResult",returnObj );
	return holder;
  }
  /**
   * For ObjectCreate
   * @param ja elements are jsonobject
   * @return
   * @throws Exception
   */
  private DefaultWebEvent createBatchCreateEvent(JSONArray ja, DefaultWebEvent template,Table table) throws Exception{
	  DefaultWebEvent e=(DefaultWebEvent)template.clone();
	  e.setParameter("command","ObjectCreate");
	  e.setParameter("best_effort","false"); //one transaction
	  ArrayList cols=table.getColumns(new int[]{1}, false,template.getSecurityGrade());
	  for(int i=0;i< cols.size();i++){
		  Column column=(Column)cols.get(i);
		  String colName= column.getName();
		  
		  if(column.getReferenceTable()!=null) colName+="__"+ column.getReferenceTable().getAlternateKey().getName();
		  
		  String[] row= new String[ja.length()];
		  for(int j=0;j< row.length;j++){
			  row[j]= ja.getJSONObject(j).optString(colName);
		  }
		  e.setParameter(colName, row);
	  }
	  return e;
  }
  /**
   * Create web action event via main jsonobject
   * @param jo
   * @return
   * @throws Exception
   */
  private DefaultWebEvent createWebActionEvent(JSONObject jo, int masterObjId, int actionId ) throws Exception{
	  DefaultWebEvent e=new DefaultWebEvent("CommandEvent");
	  e.setParameter("command","ExecuteWebAction");
	  JSONObject j=new JSONObject();

	  Object req=jo.opt("javax.servlet.http.HttpServletRequest");
	  if(req!=null ) j.put("javax.servlet.http.HttpServletRequest", req);
	  j.put("webaction",actionId);
	  j.put("objectid", masterObjId);
	  
	  e.put("JSONOBJECT", j);
	  
	  return e;
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