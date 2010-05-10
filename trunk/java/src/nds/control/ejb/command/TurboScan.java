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
import java.sql.*;
/**
 * Fash save item with pdt and asi columns
 * 
 * init 2010-1-9	
 * yfzhu@agilecontrol.com
 * @since 4.0
 *  
 */
public class TurboScan extends Command {
	private static final String BARCODE="select p.id, asi.id,p.name,p.value, asi.value1_code,asi.value2_code from m_product_alias a,m_product p, m_attributesetinstance asi where a.no=? and p.id=a.m_product_id and asi.id=a.m_attributesetinstance_id";
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
	 * A normal line with m_product_id and m_attributesetinstance_id columns 
	 * and m_product_id should only contains barcode as input
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	TableManager manager=TableManager.getInstance();
  	User usr=helper.getOperator(event);
  	QueryEngine engine=QueryEngine.getInstance();
  	MessagesHolder mh= MessagesHolder.getInstance();
  	Connection conn= QueryEngine.getInstance().getConnection();
  	PreparedStatement pstmt=null;
  	ResultSet rs=null;
  	
  	String msg;
  	try{
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	//load pdt directly from ui
	  	String barcode= jo.getString("M_PRODUCT_ID__NAME");
	  	pstmt=conn.prepareStatement(BARCODE);
	  	pstmt.setString(1,barcode);
	  	rs= pstmt.executeQuery();
	  	int pid, asiid;
	  	if(rs.next()){
	  		pid= rs.getInt(1);
	  		asiid=rs.getInt(2);
	  		msg= rs.getString(3)+","+rs.getString(4)+","+rs.getString(5)+","+rs.getString(6);
	  	}else{
	  		throw new NDSException("…Ã∆∑Œ¥’“µΩ"+ barcode);
	  	}
	  	rs.close();
	  	rs=null;
	  	pstmt.close();
	  	pstmt=null;
	  	
	  	Object tag= jo.opt("tag");
	  	int tableId= jo.getInt("table");
	  	Table table= manager.getTable(tableId);
	  	Table masterTable=manager.getTable( jo.getInt("masterTableId"));
	  	int masterObjectId=jo.getInt("masterObjId");

	  	Column pfk=manager.getParentFKColumn(table);
	  	PairTable pt =new PairTable();
		pt.put(String.valueOf( pfk.getId()), String.valueOf(masterObjectId));
		String fixedColumns= pt.toParseString(null);
		
		DefaultWebEvent evt,template;
		
	  	java.util.Locale locale= event.getLocale();
	  	template=(DefaultWebEvent)event.clone();
		template.setParameter("table", String.valueOf(tableId));
		template.setParameter("command",table.getName()+"Create");
	  	template.getData().remove("jsonObject".toUpperCase());
	  	template.setParameter("nds.control.ejb.UserTransaction" , "Y"); // original one will set this to false
	  	
	  	template.setParameter("M_PRODUCT_ID",String.valueOf(pid));
	  	template.setParameter("M_ATTRIBUTESETINSTANCE_ID",String.valueOf(asiid));
	  	
  	  	if(fixedColumns!=null)template.setParameter("fixedcolumns", fixedColumns);

  	  	JSONArray ja=new JSONArray();
  	  	ja.put(Column.MASK_CREATE_EDIT);
  	  	template.put("column_masks", ja);
  	  	
  	  	
  	  	
	  	List cols= table.getColumns(new int[]{Column.MASK_CREATE_EDIT}, false,usr.getSecurityGrade()) ;
  		String cname;
	  	for(int i=0;i< cols.size();i++){
	  		Column cl= (Column)cols.get(i);
	  		Table fk= cl.getReferenceTable();
	  		if( fk!=null){
	  			cname= cl.getName()+"__"+ cl.getReferenceTable().getAlternateKey().getName();
	  		}else{
	  			cname= cl.getName();
	  		}
	  		template.put(cname, jo.getString(cname) );
	  	}
	  	ValueHolder holder=helper.handleEventWithNewTransaction(template);
		if(Tools.getInt(holder.get("code"),0)==0){
			//load pdt info
			JSONObject jo2=new JSONObject();
	  		jo.put("isok",true);
	  		jo.put("row", msg );
	  		holder.put("data",jo);
		}else{
			throw new NDSException( (String)holder.get("message"));
		}
	  	
		
		return holder;
  	}catch(Throwable t){
  		logger.error("exception",t);
  		String error=nds.util.StringUtils.getRootCause(t).getMessage();
  		ValueHolder holder=new ValueHolder();
  		try{
	  		JSONObject jo=new JSONObject();
	  		jo.put("isok",false);
	  		jo.put("row", error );
	  		holder.put("data",jo);
  		}catch(Throwable t2){
  			logger.error("exception",t2);
  			throw new NDSException(t2.getMessage(),t2);  
  		}
  		return holder;
  	}finally{
  		if(rs!=null)try{rs.close();}catch(Throwable t){}
  		if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
  		if(conn!=null)try{conn.close();}catch(Throwable t){}
  	}
  }
  
  
}