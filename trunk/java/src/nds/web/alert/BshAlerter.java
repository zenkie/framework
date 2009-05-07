/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.alert;

import nds.query.*;
import nds.schema.Column;
import nds.schema.Legend;
import nds.util.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import bsh.EvalError;
import bsh.Interpreter;
/**
 * Using script to justify what class should be used for specified column
 * 脚本在 ad_script 中存储，脚本名称必须是 [table].[column].alerter
 * 这里根据 脚本运行的返回值判断，参数通过 "alert.queryresult"（QueryResult）, 
 * "alert.columnindex"（Integer）, "alert.column"（Column） 设置
 * 
 * Client 相关,需要为每家公司配置script
 * @author yfzhu@agilecontrol.com
 */

public class BshAlerter extends ColumnAlerterSupport{
	private Hashtable htScripts=new Hashtable();// key: ad_client_id+"."+column.id , value: script of that column(String)
	/**
	 * Clear cache
	 *
	 */
	public void clear(){htScripts.clear();}	
	/**
	 * How to describe this alerter
	 * @return Legend which contains items that description each style of alert
	 */
	public Legend getLegend(Column column){
		return null;
	}
	public String getRowCssClass(QueryResult rs, int column, Column col){
		//load adclient id from db
		QuerySession session;
		int adClientId=-1;
		session=rs.getQueryRequest().getSession();
		if(session !=null){
			adClientId= Tools.getInt(session.getAttribute("$AD_CLIENT_ID$"), -1);
		}
		if (adClientId==-1 ){
			logger.warning("Could not get ad_client id from query, no alerter then");
			return null;
		}
		String key=adClientId + "."+col.getId();
		String script=(String)htScripts.get(key);
		if(script==null){
			try{
				script=(String) QueryEngine.getInstance().doQueryOne(
					"select content from ad_script where ad_client_id="+adClientId+" and name='"+ col.toString()+".alerter'");
			}catch(Throwable e){
				logger.error("Could not load script from db for client="+ adClientId+", column="+ col, e);
				script ="";
			}
			//
			htScripts.put(key, script);
		}
		if(Validator.isNull(script)) return null;
		Object o =null;
		try{
			HashMap map=new HashMap();
			map.put("alert.queryresult", rs);
			map.put("alert.columnindex", new Integer(column));
			map.put("alert.column", col);
			o=BshScriptUtils.evalScript(script, new StringBuffer(), false, map);
		}catch(Throwable t){
			logger.error("Could not handle script for "+ col, t);
		}
		return ( o==null?null:o.toString());
	}
	
	private static  BshAlerter instance;
	public static BshAlerter getInstance(){
		if(instance==null){
			instance= new BshAlerter();
		}
		return instance;
	}


}
