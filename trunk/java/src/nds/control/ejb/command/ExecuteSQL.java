package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.SQLTypes;
import nds.security.User;
import nds.util.Configurations;
import nds.util.JSONUtils;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.Tools;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 执行SQL语句，通过REST调用
 *  
 */
public class ExecuteSQL extends Command {
	/**
	 * 
	 * @param  
	 * 	jsonObject -
			name* - name of ad_sql
	       	values*  - [],绑定变量的值，注意顺序必须与SQL语句中的变量顺序一直。另外，变量类型与SQL变量的类型也必须相符。
		目前支持的变量类型包括：String，Integer，Double，Date。Date 也是字符串，在设置变量时，
		必须加前缀$d来表示。例如， sql=”select id from c_store where creationdate=?” 前端可以配置
		 values=[“$d2010-11-12 14:21:00“ ]. 目前支持的date 格式只有一种：yyyy-mm-dd hh:mi:ss。 
		 对于Null 内容，由于sql必须指明其类型，故使用 $null_int, $null_str, $null_double, $null_date来表示，
		 直接设置null 将等同于 $null_str	    

	 * @return "result" will be jsonObject of ResultSet
	 * 
	 * 	
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	MessagesHolder mh= MessagesHolder.getInstance();
  	Connection conn=null;
  	PreparedStatement pstmt=null;
  	ResultSet result=null;
  	try{
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	String name= jo.optString("name");
	  	if(name==null) throw new NDSException("@object-not-found@:AdSQL("+name+")");

	  	conn=QueryEngine.getInstance().getConnection();
	  	
	  	List v=QueryEngine.getInstance().doQueryList("select id, value from ad_sql where ad_client_id="+
	  				usr.adClientId+" and name="+ QueryUtils.TO_STRING(name), conn);
	  	if(v.size()==0)  throw new NDSException("@object-not-found@:AdSQL("+name+")");
	  	int sqlId=Tools.getInt( ((List)v.get(0)).get(0),-1);
	  	String sql=(String)((List)v.get(0)).get(1);
	  	
	  	if(!SecurityUtils.hasObjectPermission(conn, usr.id.intValue(), usr.name, "AD_SQL", sqlId, 1, event.getQuerySession()))
	  		throw new NDSException("@no-permission@"); 

	  	//replace variables
	  	sql= QueryUtils.replaceVariables(sql.trim(), event.getQuerySession());
	  	logger.debug(sql);
	  	pstmt= conn.prepareStatement(sql);
	  	JSONArray values=jo.optJSONArray("values");
	  	if(values!=null){
	  		for(int i=0;i<values.length();i++){
	  			Object vi=values.get(i);
	  			if(vi==null){//
	  				pstmt.setNull(i+1, SQLTypes.VARCHAR);
	  			}else if( vi instanceof String){
	  				if(((String) vi).startsWith("$d")){
	  					String d= ((String) vi).substring(2);
	  					try{
	  						pstmt.setTimestamp(i+1, new java.sql.Timestamp(QueryUtils.dateTimeSecondsFormatter.get().parse(d).getTime()));
	  					}catch(Throwable t){
	  						throw new  NDSException("Failt to parse "+ d+" as date"); 
	  					}
	  				}else if( ((String) vi).startsWith("$null_")){
	  					// null
	  					String d= ((String) vi).substring(6);
	  					if("int".equals(d)){
	  						pstmt.setNull(i+1, SQLTypes.INT);
	  					}else if("double".equals(d)){
	  						pstmt.setNull(i+1, SQLTypes.DOUBLE);
	  					}else if("date".equals(d)){
	  						pstmt.setNull(i+1, SQLTypes.DATE);
	  					}else if("long".equals(d)){
	  						pstmt.setNull(i+1, SQLTypes.BIGINT);
	  					}else
	  						pstmt.setNull(i+1, SQLTypes.VARCHAR);
	  				}else{
	  					// take as normal string
	  					pstmt.setString(i+1, (String)vi);
	  				}
	  			}else if(vi instanceof Integer){
	  				pstmt.setInt(i+1, ((Integer) vi).intValue());
	  			}else if(vi instanceof Long){
	  				pstmt.setLong(i+1,((Long) vi).longValue());
	  			}else if(vi instanceof Double){
	  				pstmt.setDouble(i+1,((Double) vi).doubleValue());
	  			}else{
	  				throw new NDSException("Unsupported type "+ vi.getClass()+":"+ vi);
	  			}
	  		}
	  	}
	  	JSONObject res=new JSONObject();
	  	if(sql.toLowerCase().startsWith("select")){
	  		result= pstmt.executeQuery();
			
	  		//no exceed than specified length of rows fetched
	  		Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
			int range=Tools.getInt(conf.getProperty("rest.query.max.range"),-1);
			if(range==-1) range=QueryUtils.MAXIMUM_RANGE;
	  		
	  		res.put("result", JSONUtils.toJSONArray(result, range));
	  	}else{
	  		int count=pstmt.executeUpdate();
	  		res.put("count", count);
	  	}
	  	
  		ValueHolder holder= new ValueHolder();
		holder.put("message", mh.getMessage(event.getLocale(), "complete"));
		holder.put("code","0");
		holder.put("restResult", res);
	  	
		logger.debug("execute sql name="+name+" by "+ usr.name+" of id "+ usr.id);
	  	return holder;
  	}catch(Throwable t){
  		logger.error("exception",t);
  		if(t instanceof NDSException) throw (NDSException)t;
  		else
  			throw new NDSException(t.getMessage(), t);
  	}finally{
  		try{if(result!=null)conn.close();}catch(Throwable e){}
  		try{if(pstmt!=null)conn.close();}catch(Throwable e){}
  		try{if(conn!=null)conn.close();}catch(Throwable e){}
  	}
  }
 
}