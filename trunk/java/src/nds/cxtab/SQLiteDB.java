package nds.cxtab;

import java.io.File;
import java.io.PrintStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletContext;

import nds.control.web.ServletContextManager;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.QueryUtils;
import nds.security.User;
import nds.util.*;

import org.json.JSONArray;



public class SQLiteDB {

	private static Logger logger = LoggerManager.getInstance().getLogger((SQLiteDB.class.getName()));
	private int piId;
	private int dimCount;
	private String filename;
	private String sql;
	private int cxtabId;
	private String filterDesc;
	private String cxtabName;
	private User user;
	private String dataSQL;
	private Connection oraConn;
	private String filePath;

	public SQLiteDB(int pid, int dimc, String sql_str, String fname, int cxid, String ctname,
			String expr, User us, Connection pcon) {
		piId = pid;
		filename = fname;
		sql = sql_str;
		oraConn = pcon;
		dimCount = dimc;
		filterDesc = expr;
		cxtabName = ctname;
		user = us;
		cxtabId = cxid;
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		String exportRootPath=conf.getProperty("export.root.nds","/act/home");
		filePath =exportRootPath + File.separator+user.getClientDomain()+File.separator+ user.getName();
		logger.debug(String.valueOf(piId));
		logger.debug(filename);
		logger.debug(sql);
		logger.debug(String.valueOf(dimCount));
		logger.debug(filterDesc);
		logger.debug(cxtabName);
		logger.debug(String.valueOf(cxtabId));

	}

	public static void main(String args[]) throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		Connection connection = null;
		try {
			Random random= new Random(System.currentTimeMillis());
			long counter=random.nextLong();
			System.out.print(counter);
			connection = DriverManager.getConnection("jdbc:sqlite:s.db");
			Statement stat = connection.createStatement();
			stat.setQueryTimeout(3600);
			stat.executeUpdate("insert into person values(1, '朱叶峰')");
			ResultSet rs = stat.executeQuery("select * from person");
			while (rs.next()) {
				System.out.println("name = " + (rs.getString("name")));
				System.out.println("id = " + rs.getInt("id"));
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ef) {
				System.err.println(ef);
			}
		} catch (SQLException ew) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(ew.getMessage());
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException et) {
				System.err.println(et);
			}
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int save() throws Exception { 

		Class.forName("org.sqlite.JDBC");
		Statement stmt = null;
		PreparedStatement ptmt=null;
		Connection conn = null;
		int datacount=0;
		Object connb;
		Statement cx_stmt;
		PreparedStatement cx_ptmt;
		JSONArray cx_dim_data;
		String user_conf_data;
		Object localObject2;
		File localFile;
		Random random= new Random(System.currentTimeMillis());
		long counter=random.nextLong();
		String filesdb=String.valueOf(counter);
		try { 
			// 由于rename 文件没有残留
			//ServletContext contex =nds.control.web.WebUtils.getServletContext();
			//System.out.print(nds.control.web.WebUtils.getServerUrl());
			conn = DriverManager.getConnection("jdbc:sqlite:"+filename+".sdb");
			stmt=conn.createStatement();
			//优化SQLITE
			stmt.executeUpdate("PRAGMA synchronous = OFF;");
			migrateFacts(conn);
			datacount = migrateData(conn);
			putCxtabpar(conn);
			logger.debug("datacount is"+String.valueOf(datacount) );
			connb = conn; 
			SQLiteDB localSQLiteDB = this; 
			cx_stmt = null; 
			cx_ptmt = null;
			try {
				cx_dim_data = QueryEngine
						.getInstance()
						.doQueryJSONArray(
								"select id, description, position_, hidehtml,orderno  from ad_cxtab_dimension where ad_cxtab_id=? and isactive='Y'",
								new Object[] { Integer.valueOf(cxtabId) },
								oraConn);
				user_conf_data = (String) QueryEngine
						.getInstance()
						.doQueryOne(
								"select value from ad_user_pref where ad_user_id=? and module='cxtabdim' and name=?",
								new Object[] {
										Integer.valueOf(user.id.intValue()),
										String.valueOf(cxtabId) }, oraConn);
				int j = 0;
				if (Validator.isNotNull((String) user_conf_data)) {
					user_conf_data = (String) user_conf_data + ",";
					j = 1;
				}
				//(localObject4 = ((Connection) localObject3).createStatement())
				cx_stmt=((Connection) connb).createStatement();
				cx_stmt.executeUpdate("create table ad_cxtab_dimension(id,description, position_, hidehtml,orderno,isactive)");
				cx_stmt.close();
				cx_ptmt = ((Connection) connb).prepareStatement("insert into ad_cxtab_dimension(id,description, position_, hidehtml,orderno,isactive)values(?,?,?,?,?,'Y')");

				for (int i = 0; i < cx_dim_data.length(); i++) {
					JSONArray dim_data_list=cx_dim_data.getJSONArray(i);
					int dim_id = dim_data_list.getInt(0);
					if (((j != 0) && (((String) user_conf_data).contains(dim_id + ",")))|| (j == 0)) {
						cx_ptmt.setInt(1,dim_data_list.getInt(0));
						if (dim_data_list.isNull(1))
							cx_ptmt.setNull(2, 12);
						else
							cx_ptmt.setString(2,dim_data_list.getString(1));
						if (dim_data_list.isNull(2))
							cx_ptmt.setNull(3, 12);
						else
							cx_ptmt.setString(3,dim_data_list.getString(2));
						if (dim_data_list.isNull(3))
							cx_ptmt.setNull(4, 12);
						else
							cx_ptmt.setString(4,dim_data_list.getString(3));
						cx_ptmt.setInt(5,dim_data_list.getInt(4));
						cx_ptmt.executeUpdate();
					}
				}
				if (cx_stmt != null)
					try {
						cx_stmt.close();
					} catch (Throwable e) {
					}
				if (cx_ptmt != null)
					try {
						cx_ptmt.close();
					} catch (Throwable ea) {
					}
			} finally {
				if (cx_stmt != null)
					try {
						cx_stmt.close();
					} catch (Throwable e) {
					}
				if (cx_ptmt != null)
					try {
						cx_ptmt.close();
					} catch (Throwable ea) {
					}
			}

			try {
				cx_stmt=((Connection) connb).createStatement();
				cx_stmt.executeUpdate("create table ad_cxtab(piid, name, filter, owner, creationdate,rowcnt,usr_sgrade, sql)");
				cx_ptmt = ((Connection) connb).prepareStatement("insert into ad_cxtab(piid, name, filter, owner, creationdate,rowcnt,usr_sgrade,sql)values(?,?,?,?,?,?,?,?)");
				cx_ptmt.setInt(1,piId);
				cx_ptmt.setString(2,
						cxtabName);
				cx_ptmt
				.setString(
						3,
						filterDesc);
				cx_ptmt
				.setString(
						4,
						user.name);
				cx_ptmt.setString(5,
						((SimpleDateFormat) QueryUtils.dateTimeSecondsFormatter
								.get()).format(new Date()));
				cx_ptmt.setInt(6, datacount);
				cx_ptmt
				.setInt(7,
						user
						.getSecurityGrade());
				cx_ptmt.setString(8,dataSQL);
				cx_ptmt.executeUpdate();
				if (cx_stmt != null)
					try {
						cx_stmt.close();
					} catch (Throwable e) {
					}
				if (cx_ptmt != null)
					try {
						cx_ptmt.close();
					} catch (Throwable ea) {
					}
			} finally {
				if (cx_stmt != null)
					try {
						cx_stmt.close();
					} catch (Throwable e) {
					}
				if (cx_ptmt != null)
					try {
						cx_ptmt.close();
					} catch (Throwable ea) {
					}

			}

			if (stmt != null)
				try {
					stmt.close();
				} catch (Throwable e) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Throwable ea) {
				}
		}catch(SQLException e){	
			 logger.error("Error doing sqlite save:"+e.getMessage(),e);
		}finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Throwable e) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Throwable ea) {
				}

		}

		//暂存文件路径文件名
		
		String destfile = filename + ".sdb";
		 
		File sdfile=new File(destfile);
 
		
		//Object tempfile;
		//((File) (tempfile = new File(filesdb))).renameTo(new File(destfile));
		//String pfile =String.valueOf(piId)+"_"+String.valueOf(counter)+".7z";
		String exec_cmd="7zr a "+filename+" "+destfile;
		logger.debug("cub.sdb is"+exec_cmd);
		String log="7zcub.log";

		//调用7z合并生成打印文件
		CommandExecuter cmd= new CommandExecuter(log);
		int err=cmd.run(exec_cmd);
		if (sdfile.exists())sdfile.delete();	
		
		return datacount;
	}


	/**
	 * 
	 * @param conn
	 * @throws Exception
	 */
	private void migrateFacts(Connection conn)
			throws Exception
			{
		Statement stmt =null;
		PreparedStatement ptmt=null;
		try{ 
			logger.debug("doQueryJSONArray "+String.valueOf(cxtabId));
			JSONArray jor_arry = QueryEngine.getInstance().doQueryJSONArray("select id, description, function_, userfact,valuename,valueformat,sgrade,orderno from ad_cxtab_fact where ad_cxtab_id=? and isactive='Y'", 
					new Object[] {Integer.valueOf(cxtabId)},oraConn);
			stmt = conn.createStatement();
			stmt.executeUpdate("create table ad_cxtab_fact(id, description, function_, userfact,valuename,valueformat,sgrade,orderno,isactive)");
			stmt.close();
			ptmt = conn.prepareStatement("insert into ad_cxtab_fact(id, description, function_, userfact,valuename,valueformat,sgrade,orderno,isactive)values(?,?,?,?,?,?,?,?,'Y')");
			for (int v = 0; v < jor_arry.length(); v++)
			{
				JSONArray jsonarray = jor_arry.getJSONArray(v);
				ptmt.setInt(1, jsonarray.getInt(0));
				if (jsonarray.isNull(1))
					ptmt.setNull(2, 12);
				else
					ptmt.setString(2, jsonarray.getString(1));
				if (jsonarray.isNull(2))
					ptmt.setNull(3, 12);
				else
					ptmt.setString(3, jsonarray.getString(2));
				if (jsonarray.isNull(3))
					ptmt.setNull(4, 12);
				else
					ptmt.setString(4, jsonarray.getString(3));
				if (jsonarray.isNull(4))
					ptmt.setNull(5, 12);
				else
					ptmt.setString(5, jsonarray.getString(4));
				if (jsonarray.isNull(5))
					ptmt.setNull(6, 12);
				else
					ptmt.setString(6, jsonarray.getString(5));
				ptmt.setInt(7, jsonarray.getInt(6));
				ptmt.setInt(8, jsonarray.getInt(7));
				ptmt.executeUpdate();
			}

			if (stmt != null) try { stmt.close(); } catch (Throwable e) {} 
			if (ptmt != null) try { ptmt.close(); }catch (Throwable e){}
		}finally{
			if (stmt != null) try { stmt.close(); } catch (Throwable e) {} 
			if (ptmt != null) try { ptmt.close(); }catch (Throwable e){}
		}
			}
	/**
     * @param conn
	 * @return
	 * @throws Exception  
	 * put server loaclstation insert to cxtab_param
	 */
	private void putCxtabpar(Connection conn) throws Exception {
		Statement stmt = null;
		PreparedStatement ptmt=null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create table ad_cxtab_param(name, description, value)");
			String url=nds.control.web.WebUtils.getServerUrl();
			ptmt = conn.prepareStatement("insert into ad_cxtab_param(name, description, value)values(?,?,?)");
			ptmt.setString(1,"serverurl");
			ptmt.setString(2,"服务器地址");
			ptmt.setString(3,url);
			ptmt.executeUpdate();
			//user
			if (stmt != null) try { stmt.close(); } catch (Throwable e) {} 
			if (ptmt != null) try { ptmt.close(); }catch (Throwable e){}
		} finally {
			if (stmt != null) try { stmt.close(); } catch (Throwable e) {} 
			if (ptmt != null) try { ptmt.close(); }catch (Throwable e){}
		}
	}
	/**
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private int migrateData(Connection conn)
			throws Exception
			{
		ResultSet rs=null;
		Statement stmt=null;
		Statement ora_stmt=null;
		PreparedStatement ptmt=null;
		ResultSetMetaData rsmetadata;
		try{
			//int j = (resultsetmetadata = (resultset = oraConn.createStatement().executeQuery(sql)).getMetaData()).getColumnCount();
			ora_stmt=oraConn.createStatement();
			//fast cursor
//			ora_stmt=oraConn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
//                    java.sql.ResultSet.CONCUR_READ_ONLY);
//			ora_stmt.setFetchSize(200);
			rs=ora_stmt.executeQuery(sql);
			rsmetadata =rs.getMetaData();
			int colCount = rs.getMetaData().getColumnCount();
			logger.debug("orasql is"+sql);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();

			StringBuilder creat_sql = new StringBuilder("create table ad_cxtab_data(");
			StringBuilder insert_sql = new StringBuilder("insert into ad_cxtab_data(");
			StringBuilder query_sql = new StringBuilder("select ");
			creat_sql.append(rsmetadata.getColumnName(1));
			insert_sql.append(rsmetadata.getColumnName(1));
			query_sql.append(rsmetadata.getColumnName(1));
			for (int m = 2; m <= colCount; m++) {

				creat_sql.append(",").append(rsmetadata.getColumnName(m));
				insert_sql.append(",").append(rsmetadata.getColumnName(m));
				query_sql.append(",").append(rsmetadata.getColumnName(m));
			}

			creat_sql.append(")");
			query_sql.append(" from ad_cxtab_data");
			dataSQL = query_sql.toString();
			logger.debug(dataSQL);
			insert_sql.append(") values(?");
			for (int k = 2; k <= colCount; k++)
				insert_sql.append(",?");
			insert_sql.append(")");
			logger.debug(creat_sql.toString());
			stmt.executeUpdate(creat_sql.toString());
			logger.debug(insert_sql.toString());
			ptmt = conn.prepareStatement(insert_sql.toString());
			int i = 0;
			int k;
			while (rs.next())
			{
				for (k = 1; k <= dimCount; k++) {
					String str = rs.getString(k);
					if (rs.wasNull()) ptmt.setString(k, ""); else {
						//logger.debug("setString "+str);
						ptmt.setString(k, str);
					}
				}
				for (k = dimCount + 1; k <= colCount; k++) {
					double d1 = rs.getDouble(k);
					if (rs.wasNull()) ptmt.setNull(k, 8); else {
						//logger.debug("d1 "+String.valueOf(d1));
						ptmt.setDouble(k, d1);
					}
				}
				ptmt.addBatch();

				i++;
				if (i % 5000 == 0) ptmt.executeBatch();
			}
			ptmt.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
			QueryEngine.getInstance().executeUpdate("update ad_pinstance_para set p_number=? where ad_pinstance_id=? and name='filename'", new Object[] {
					Integer.valueOf(i), Integer.valueOf(piId)
			}, oraConn);

			return i;
		} finally {
			if (rs != null) try { rs.close(); } catch (Throwable e) {} 
			if (stmt != null) try { stmt.close(); } catch (Throwable ea) {} 
			if (ptmt != null) try { ptmt.close(); } catch (Throwable ew) {}  
		}
			}
}