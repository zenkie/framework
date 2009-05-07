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
	/**
	 * 
	 * @param event contains 
	 * 	jsonObject - 
			cxtabId* - ad_cxtab.id
	 		axisH :  axis horizontal, this is an array object, elements has properties: {columnlink, description}
	 		axisV :  axis vertical, same as axisH
	 		axisP :  axis vertical, same as axisH
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
  	try{
	  	conn=engine.getConnection();
  		JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	Object tag= jo.opt("tag");
	  	int cxtabId= jo.getInt("cxtabId");
	  	int factTableId= Tools.getInt(engine.doQueryOne("select ad_table_id from ad_cxtab where id="+ cxtabId, conn),-1);
	  	Table factTable = manager.getTable(factTableId);
	  	
	  	JSONArray axisH=jo.optJSONArray("axisH");
	  	JSONArray axisV=jo.optJSONArray("axisV");
	  	JSONArray axisP=jo.optJSONArray("axisP");
	  	JSONArray measures=jo.optJSONArray("measures");
	  	
	  	pstmt=conn.prepareStatement(DELETE_DIMENSION);
	  	pstmt.setInt(1, cxtabId);
	  	pstmt.executeUpdate();
	  	try{pstmt.close();}catch(Throwable td){}
	  	
	  	
	  	pstmt=conn.prepareStatement(DELETE_MEASURE);
	  	pstmt.setInt(1, cxtabId);
	  	pstmt.executeUpdate();
	  	try{pstmt.close();}catch(Throwable td){}
	  	
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
	  			throw new NDSException( "@axis-h-line@ " + (i+1)+ " @is-invalid@:"+ clink+";");
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
	  	pos= axisV.length() + axisH.length();
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
	  		pstmt.setInt( 6, (i+1+pos)*10);
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

	  	JSONObject ro=new JSONObject();
	  	ro.put("tag", tag); //  return back unchanged.
  		ro.put("code", 0);
  		ro.put("message", mh.getMessage(event.getLocale(), "complete"));
	  	
	  	ValueHolder holder= new ValueHolder();
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");
		holder.put("data",ro );
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