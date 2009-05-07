/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.log4j;

import java.sql.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

//import org.apache.log4j.jdbcplus.JDBCPoolConnectionHandler;
import javax.naming.Context;
import javax.naming.InitialContext;


/**
 * Support for datasource connection
 * @author yfzhu@agilecontrol.com
 */

public class DataSourceConnectionHandler {//implements JDBCPoolConnectionHandler {
	private DataSource datasource;
	
	public Connection getConnection()  throws SQLException{
        if   (datasource!=null) return datasource.getConnection();
        throw new IllegalArgumentException("Can not call this method when datasource not established.");
	}

	public Connection getConnection(String _url, String _username, String _password) throws SQLException{
//		 Get a context for the JNDI look up
		if(datasource==null){
	        try{
	        	Context ctx = new InitialContext();
		        datasource = (DataSource) ctx.lookup (_url);
	        }catch(Exception e){
	        	throw new SQLException("Could not find datasource from "+ _url);
	        }
		}
		return datasource.getConnection();
	}

	/**
	 * The connection is free again, and can be used elsewhere
	 * @param  con connection to be freed
	 * @exception Exception if any error occurs
	 */
	public void freeConnection(Connection con) throws Exception {
		con.close();
	}
}
