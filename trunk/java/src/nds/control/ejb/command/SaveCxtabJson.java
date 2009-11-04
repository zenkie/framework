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
 * Save Cxtab, including axis and measures
 * This is for ajax request.	
 *  
 */
public class SaveCxtabJson extends Command {
	private final static String INSERT_MEASURE="insert into ad_cxtab_fact (id, ad_client_id,ad_cxtab_id,ad_column_id, description, function_,userfact, valueformat,valuename, orderno, ownerid, modifierid,creationdate,modifieddate,isactive) values (get_sequences('ad_cxtab_fact'),?,?,?,?,?,?,?,?,?,?,?,sysdate,sysdate, 'Y')";
	private final static String INSERT_DIMENSION="insert into ad_cxtab_dimension (id, ad_client_id,ad_cxtab_id,columnlink, description, position_,orderno, ownerid, modifierid,creationdate,modifieddate,isactive,hidehtml) values " +
		"(get_sequences('ad_cxtab_dimension'),?,?,?,?,?,?,?,?,sysdate,sysdate, 'Y',?)";
	private final static String DELETE_DIMENSION="delete from ad_cxtab_dimension where ad_cxtab_id=?";
	private final static String DELETE_MEASURE="delete from ad_cxtab_fact where ad_cxtab_id=?"; 
	//private final static String INSERT_CXTAB="insert into ad_cxtab(id,ad_client_id,ad_org_id,name,description,ad_table_id,filter,ad_process_id,ad_column_cxtabinst_id,sampleurl,attr1,attr2,attr3,ownerid,modifierid,creationdate,modifieddate,isactive,isbackground,ad_processqueue_id,ad_cxtab_category_id,reporttype,orderno,pre_procedure,ad_pi_column_id,ispublic) values(get_sequences('ad_cxtab'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate,sysdate,'Y',?,?,?,?,?,?,?,?)";
	private final static String INSERT_CXTAB="insert into ad_cxtab(id,ad_client_id,ad_org_id,name,description,ad_table_id,filter,ad_column_cxtabinst_id,sampleurl,attr1,attr2,attr3,ownerid,modifierid,creationdate,modifieddate,isactive,isbackground,ad_processqueue_id,ad_cxtab_category_id,reporttype,orderno,pre_procedure,ad_pi_column_id,ispublic) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,sysdate,sysdate,'Y',?,?,?,?,?,?,?,?)";
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject - 
			cxtabId* - ad_cxtab.id
	 		axisH :  axis horizontal, this is an array object, elements has properties: {columnlink, description}
	 		axisV :  axis vertical, same as axisH
	 		measures:array object, elements {description, column, userfact, valueformat,function_}
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
  	java.sql.Connection conn=null;
  	PreparedStatement pstmt=null;
  	StringBuffer sb=new StringBuffer();
  	boolean flag=false;
  	ValueHolder holder= new ValueHolder();
  	JSONObject ro=new JSONObject();
  	try{
	  	conn=engine.getConnection();
  		JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	Object tag= jo.opt("tag");
	  	int cxtabId= jo.getInt("cxtabId");
	  	String savetype=(String)jo.opt("savetype");
		String name=(String)jo.opt("name");
	  	
	  	JSONArray axisH=jo.optJSONArray("axisH");
	  	JSONArray axisV=jo.optJSONArray("axisV");
		JSONArray axisP=jo.optJSONArray("axisP");
	  	JSONArray measures=jo.optJSONArray("measures");
	  	if(savetype.equals("M")){
		  	
		  	pstmt=conn.prepareStatement(DELETE_DIMENSION);
		  	pstmt.setInt(1, cxtabId);
		  	pstmt.executeUpdate();
		  	try{pstmt.close();}catch(Throwable td){}
		  	
		  	pstmt=conn.prepareStatement(DELETE_MEASURE);
		  	pstmt.setInt(1, cxtabId);
		  	pstmt.executeUpdate();
		  	try{pstmt.close();}catch(Throwable td){}
		  	flag=true;
	  	}else{
	  		int count=Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from ad_cxtab where name='"+name+"'"), -1);
	  		if(count>=1){
	  	  		ro.put("code", 2);
	  		}else{
		  		List replist=QueryEngine.getInstance().doQueryList("select ad_org_id,description,ad_table_id,filter,ad_process_id,ad_column_cxtabinst_id,sampleurl,attr1,attr2,attr3,isbackground,ad_processqueue_id,ad_cxtab_category_id,reporttype,orderno,pre_procedure,ad_pi_column_id from ad_cxtab where id="+ cxtabId);
		  		int oldCxtabId=cxtabId;
		  		cxtabId=QueryEngine.getInstance().getSequence("ad_cxtab");
		  		if(replist.size()>=0){
		  			pstmt= conn.prepareStatement(INSERT_CXTAB);
		  			pstmt.setInt(1, cxtabId);
		  			pstmt.setInt(2, user.adClientId);
		  			pstmt.setInt(3, Tools.getInt(((List)replist.get(0)).get(0),0));
		  			pstmt.setString(4, name);
		  			pstmt.setString(5, (String)((List)replist.get(0)).get(1));
		  			pstmt.setInt(6, Tools.getInt(((List)replist.get(0)).get(2),0));
		  			pstmt.setString(7, (String)((List)replist.get(0)).get(3));
		  			pstmt.setInt(8, Tools.getInt(((List)replist.get(0)).get(5),0));
		  			pstmt.setString(9, (String)((List)replist.get(0)).get(6));
		  			pstmt.setString(10, (String)((List)replist.get(0)).get(7));
		  			pstmt.setString(11, (String)((List)replist.get(0)).get(8));
		  			pstmt.setString(12, (String)((List)replist.get(0)).get(9));
		  			pstmt.setInt(13, user.getId());
		  			pstmt.setInt(14, user.getId());
		  			pstmt.setString(15, (String)((List)replist.get(0)).get(10));
		  			pstmt.setInt(16, Tools.getInt(((List)replist.get(0)).get(11),0));
		  			pstmt.setInt(17, Tools.getInt(((List)replist.get(0)).get(12),0));
		  			pstmt.setString(18, (String)((List)replist.get(0)).get(13));
		  			pstmt.setInt(19, Tools.getInt(((List)replist.get(0)).get(14),0));
		  			pstmt.setString(20, (String)((List)replist.get(0)).get(15));
		  			pstmt.setInt(21, Tools.getInt(((List)replist.get(0)).get(16),0));
		  			pstmt.setString(22,"N");
		  			pstmt.executeUpdate();
				  	try{pstmt.close();}catch(Throwable td){}
				  	//clone AD_CXTAB_JPARA
				  	engine.executeUpdate(
				  	"insert into AD_CXTAB_JPARA(id,ad_client_id,ad_cxtab_id,name,description,paratype,defaultvalue,ad_column_id,selectiontype,ownerid,modifierid,creationdate,modifieddate,isactive,orderno,nullable) select get_sequences('AD_CXTAB_JPARA'),ad_client_id,"+
				  	cxtabId+",name,description,paratype,defaultvalue,ad_column_id,selectiontype,modifierid,modifierid,sysdate,sysdate,isactive,orderno,nullable from AD_CXTAB_JPARA where ad_cxtab_id="+oldCxtabId
				  	);
		  		}
		  		flag=true;
	  		}
	  	}
	  	if(flag){
	  		int factTableId= Tools.getInt(engine.doQueryOne("select ad_table_id from ad_cxtab where id="+ cxtabId, conn),-1);
		  	Table factTable = manager.getTable(factTableId);
		  	pstmt= conn.prepareStatement(INSERT_DIMENSION);
		  	for(int i=0;i< axisH.length();i++){
		  		JSONObject dim=axisH.getJSONObject(i);
		  		pstmt.setInt(1, user.adClientId);
		  		pstmt.setInt(2, cxtabId);
		  		String clink=dim.getString("columnlink");
		  		try{
		  			ColumnLink cl= new ColumnLink(clink);
		  		}catch(Throwable t){
		  			logger.error("fail to create clink:"+ clink, t);
		  			throw new NDSException( "@axis-h-line@" + (i+1)+ " @is-invalid@:"+ clink+";");
		  		}
		  		pstmt.setString(3, clink);
		  		String desc=  dim.getString("description");
		  		if(Validator.isNull(desc )|| desc.startsWith("[")){
			  		pstmt.setNull(4, Types.VARCHAR);
		  		}else{
		  			pstmt.setString(4,desc);
		  		}
		  		pstmt.setString(5, "H");
		  		pstmt.setInt( 6, (i+1)*10);
		  		pstmt.setInt(7, user.id.intValue());
		  		pstmt.setInt(8, user.id.intValue());
		  		pstmt.setString(9,dim.getString("hidehtml"));
		  		pstmt.executeUpdate();
		  	}
		  	int pos= axisH.length();
		  	for(int i=0;i< axisV.length();i++){
		  		JSONObject dim=axisV.getJSONObject(i);
		  		pstmt.setInt(1, user.adClientId);
		  		pstmt.setInt(2, cxtabId);
		  		String clink=dim.getString("columnlink");
		  		try{
		  			ColumnLink cl= new ColumnLink(clink);
		  		}catch(Throwable t){
		  			logger.error("fail to create clink:"+ clink, t);
		  			throw new NDSException( "@axis-v-line@ " + (i+1)+ " @is-invalid@:"+ clink+";");
		  		}
		  		pstmt.setString(3, clink);
		  		String desc=  dim.getString("description");
		  		if(Validator.isNull(desc )|| desc.startsWith("[")){
			  		pstmt.setNull(4, Types.VARCHAR);
		  		}else{
		  			pstmt.setString(4,desc);
		  		}
		  		pstmt.setString(5, "V");
		  		pstmt.setInt( 6, (i+1+pos)*10);
		  		pstmt.setInt(7, user.id.intValue());
		  		pstmt.setInt(8, user.id.intValue());
		  		pstmt.setString(9,dim.getString("hidehtml"));
		  		pstmt.executeUpdate();
		  	}
		  	int ppos= axisH.length()+axisV.length();
		  	for(int i=0;i< axisP.length();i++){
		  		JSONObject dim=axisP.getJSONObject(i);
		  		pstmt.setInt(1, user.adClientId);
		  		pstmt.setInt(2, cxtabId);
		  		String clink=dim.getString("columnlink");
		  		try{
		  			ColumnLink cl= new ColumnLink(clink);
		  		}catch(Throwable t){
		  			logger.error("fail to create clink:"+ clink, t);
		  			throw new NDSException( "@axis-p-line@ " + (i+1)+ " @is-invalid@:"+ clink+";");
		  		}
		  		pstmt.setString(3, clink);
		  		String desc=  dim.getString("description");
		  		if(Validator.isNull(desc )|| desc.startsWith("[")){
			  		pstmt.setNull(4, Types.VARCHAR);
		  		}else{
		  			pstmt.setString(4,desc);
		  		}
		  		pstmt.setString(5, "P");
		  		pstmt.setInt( 6, (i+1+ppos)*10);
		  		pstmt.setInt(7, user.id.intValue());
		  		pstmt.setInt(8, user.id.intValue());
		  		pstmt.setString(9,dim.getString("hidehtml"));
		  		pstmt.executeUpdate();
		  	}
		  	try{pstmt.close();}catch(Throwable td){}
		  	
		  	pstmt= conn.prepareStatement(INSERT_MEASURE);
		  	for(int i=0;i< measures.length();i++){
		  		JSONObject mea=measures.getJSONObject(i);
		  		pstmt.setInt(1, user.adClientId);
		  		pstmt.setInt(2, cxtabId);
		  		String clink=mea.optString("column");
		  		ColumnLink cl=null;
		  		Column column=null;
		  		if(Validator.isNotNull(clink)){
		  		try{
		  			cl= new ColumnLink(clink);
		  			column=cl.getLastColumn();
		  			if(cl.length()!=1 || column.getTable().getId() != factTable.getId()) throw new NDSException("fact column must be from main table:"+ factTable);
			  		pstmt.setInt(3, column.getId());
		  		}catch(Throwable t){
		  			logger.error("fail to create clink:"+ clink, t);
		  			throw new NDSException( "@fact-desc-line@ " + (i+1)+ " @is-invalid@:"+ clink+";");
		  		}
		  		}else{
		  			pstmt.setNull(3, Types.INTEGER);
		  		}
		  		String desc=  mea.getString("description");
		  		if(Validator.isNull(desc )|| desc.startsWith("[")){
			  		pstmt.setNull(4, Types.VARCHAR);
		  		}else{
		  			pstmt.setString(4,desc);
		  		}	  		
		  		pstmt.setString(5, mea.getString("function_"));
		  		pstmt.setString(6, mea.optString("userfact"));
		  		pstmt.setString(7, mea.getString("valueformat"));
	
		  		String valueName=  mea.optString("valuename");
		  		if(Validator.isNull(valueName )){
			  		pstmt.setNull(8, Types.VARCHAR);
		  		}else{
		  			pstmt.setString(8,valueName);
		  		}	
		  		
		  		pstmt.setInt( 9, (i+1)*10);
		  		pstmt.setInt(10, user.id.intValue());
		  		pstmt.setInt(11, user.id.intValue());
		  		pstmt.executeUpdate();
		  	}
	  		ro.put("code", 0);
	  	}

	  	ro.put("tag", tag); //  return back unchanged.
	  	ro.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("data",ro );
		holder.put("code","0");
		logger.debug(ro.toString());
		return holder;
  	}catch(Throwable t){
  		if(t instanceof NDSException) throw (NDSException)t;
  		logger.error("exception",t);
  		throw new NDSException(t.getMessage(), t);
  	}finally{
  		try{if(pstmt!=null)pstmt.close();}catch(Throwable t){}
  		try{conn.close();}catch(Throwable t){}
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