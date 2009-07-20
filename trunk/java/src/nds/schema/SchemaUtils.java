/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import nds.util.*;
import java.util.*;
import java.io.*;
import java.sql.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.model.*;

import nds.web.action.*;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class SchemaUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger(SchemaUtils.class.getName());	
	public static void createHSQLPropertyFile(String dir, String dbName) throws IOException{
		StringWriter out=new StringWriter();
		for(int i=0;i< HSQL_1_7_2_PROPERTIES.length;i++) out.write(HSQL_1_7_2_PROPERTIES[i]+ StringUtils.LINE_SEPARATOR);
		
		Tools.writeFile(dir+File.separator+dbName+ ".properties", false, out.toString(), "ISO-8859-1");
	}
	
	
	/**
	 * Get records in ad_table_transfer 
	 * @param file no ".script" extension
	 * @return List elements are AdTable
	 * @throws Exception
	 */
	public static  List getTransferTableFromHSQLFile(String file) throws Exception{
		  Connection conn=null;
		  Statement stmt= null;
		  ResultSet rs=null;
		  ArrayList list=new ArrayList();
		  try{ 
		  	 logger.debug("getTransferTableFromHSQLFile:jdbc:hsqldb:file:"+ file);
		  	 conn=DriverManager.getConnection("jdbc:hsqldb:file:"+ file ,"sa","");
		  	 stmt= conn.createStatement();
		  	 rs=stmt.executeQuery("select ad_table_id, name, description, comments from ad_table_transfer");
		  	 while( rs.next()){
		  	 	AdTable ad= new AdTable();
		  	 	ad.setId(new Integer(rs.getInt(1)));
		  	 	ad.setName(rs.getString(2));
		  	 	ad.setDescription(rs.getString(3));
		  	 	ad.setComments(rs.getString(4));
		  	 	list.add(ad);
		  	 }
		  	 return list;
		  }finally{
		  	try{if(rs!=null) rs.close();}catch(Exception ee){}
		  	try{
		  		if(conn!=null && !conn.isClosed()){
		  			// shutdown the connection
		  			conn.createStatement().executeUpdate("shutdown");
		  		}
		  	}catch(Exception e){
		  		logger.error("Fail to shutdown connection to hsqldb "+ file);
		  	}
		  	try{if(conn!=null) conn.close();}catch(Exception ee){}
		  }
		
	}
	private final static String[] HSQL_1_7_2_PROPERTIES=new String[]{
			"hsqldb.cache_file_scale=1",
			"runtime.gc_interval=0",
			"hsqldb.first_identity=0",
			"version=1.7.2",
			"modified=no",
			"hsqldb.script_format=0",
			"sql.enforce_size=false",
			"hsqldb.cache_size_scale=10",
			"hsqldb.cache_scale=14",
			"hsqldb.version=1.7.2",
			"hsqldb.log_size=200",
			"sql.enforce_strict_size=false",
			"readonly=false",
			"hsqldb.compatible_version=1.7.2",
			"hsqldb.original_version=1.7.2",
			"sql.compare_in_locale=false",
			"hsqldb.nio_data_file=true",
			"hsqldb.cache_version=1.7.0"			
	};
}
