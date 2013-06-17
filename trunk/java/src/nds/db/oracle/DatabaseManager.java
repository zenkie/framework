package nds.db.oracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.math.BigDecimal;

import java.sql.Clob;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import oracle.sql.CLOB;
import java.io.Writer;
import java.util.List;
import nds.db.DBManager;
import nds.log.Logger;
import nds.log.LoggerManager;
import org.json.*;
import nds.query.*;
import nds.util.Tools;
import nds.schema.*;
import oracle.jdbc.OracleConnection;
import org.jboss.resource.adapter.jdbc.WrappedConnection;

public class DatabaseManager implements DBManager{
    private static Logger logger= LoggerManager.getInstance().getLogger(DatabaseManager.class.getName());
    private  int logDuration =10;// IF query time (seconds) exceeds this value, it will be logged
    /* if query result has more lines than this value, powerful range sql will be created
       If this value is none positive, then range sql will nerver be created
    */
    private int minRangeSQLCount= 1000;
    private boolean isDebug=false;
    public DatabaseManager() {
    }

    public void destroy(){
    }
    public nds.query.QueryRequestImpl createRequest(){
        return new nds.db.oracle.QueryRequestImpl();
    }
    public nds.query.QueryRequestImpl createRequest(QuerySession session){
        return new nds.db.oracle.QueryRequestImpl(session);
    }    
    /**
     * @param props should have following property
     * query.minlogduration (int)
     * query.rangesqlcount(int)
     * schema.mode (String)
     */
    public void init(Properties props){
        logDuration=Tools.getInt( props.getProperty("query.minlogduration", "10"), 10);
        this.minRangeSQLCount= Tools.getInt( props.getProperty("query.rangesqlcount", "1000"), 1000);
        isDebug= "develope".equals(props.getProperty("schema.mode", "production"));
    }
    /*
     * This method uses temporary clob to create the CLOB object.
     */
	public static CLOB getCLOB(StringBuffer clobData,
			oracle.jdbc.OracleConnection conn) throws SQLException {
		CLOB tempClob = null;

		try {
			// create a new temporary CLOB
			tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);

			// Open the temporary CLOB in readwrite mode to enable writing
			tempClob.open(CLOB.MODE_READWRITE);

			// Get the output stream to write

			Writer tempClobWriter = tempClob.setCharacterStream(0L);

			// Write the data into the temporary CLOB
			tempClobWriter.write(clobData.toString());

			// Flush and close the stream
			tempClobWriter.flush();
			tempClobWriter.close();

			// Close the temporary CLOB
			tempClob.close();

		} catch (Exception exp) {
			logger.error("fail to getClob", exp);
			// Free CLOB object
			tempClob.freeTemporary();
			// do something
		}
		return tempClob;

	}

	public static CLOB getCLOB(String clobstr,
			oracle.jdbc.OracleConnection conn) throws SQLException {
		CLOB tempClob = null;
		try {
			// create a new temporary CLOB
			tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);

			// Open the temporary CLOB in readwrite mode to enable writing
			tempClob.open(CLOB.MODE_READWRITE);

			Writer tempClobWriter = tempClob.setCharacterStream(0L);
			tempClobWriter.write(clobstr);
			// Flush and close the stream
			tempClobWriter.flush();
			tempClobWriter.close();

			tempClob.close();
		} catch (Exception exp) {
			logger.error("fail to getClob", exp);

			tempClob.freeTemporary();
		}

		return tempClob;
	}
	
	public static CLOB getCLOB(StringBuilder clobstr,
			oracle.jdbc.OracleConnection conn) throws SQLException {
		CLOB tempClob = null;
		try {
			// create a new temporary CLOB
			tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);

			// Open the temporary CLOB in readwrite mode to enable writing
			tempClob.open(CLOB.MODE_READWRITE);

			Writer tempClobWriter = tempClob.setCharacterStream(0L);
			tempClobWriter.write(clobstr.toString());
			// Flush and close the stream
			tempClobWriter.flush();
			tempClobWriter.close();

			tempClob.close();
		} catch (Exception exp) {
			logger.error("fail to getClob", exp);

			tempClob.freeTemporary();
		}

		return tempClob;
	}
	/**
	 * 
	 * @param sql
	 * @param paramArrayOfObject
	 * @param conn
	 * @param toUpper
	 * @return
	 * @throws QueryException
	 */
	public JSONArray doQueryObjectArray(String sql,Object[] paramArrayOfObject, Connection conn,
			boolean toUpper) throws QueryException {
		JSONArray jor_Array  = new JSONArray();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			int m = 0;
			long startTime = System.currentTimeMillis();
			pstmt = createStatement(sql, paramArrayOfObject, conn);
			rs = pstmt.executeQuery();
			ResultSetMetaData rs_metadata=rs.getMetaData();
			int colCount = rs.getMetaData().getColumnCount();
			logger.debug("colCount is "+colCount);

			while (rs.next()) {
				JSONObject jor = new JSONObject();
				for (int k = 1; k <= colCount; k++) {
					Object localObject = rs.getObject(k);
					String str = toUpper ? rs_metadata
							.getColumnName(k).toUpperCase()
							: rs_metadata.getColumnName(k)
									.toLowerCase();
					if (rs.wasNull()) {
						jor.put(str, JSONObject.NULL);
					} else {
						if ((localObject instanceof Clob)) {
							localObject = ((Clob) localObject).getSubString(1L,
									(int) ((Clob) localObject).length());
						}
						jor.put(str, localObject);
					}
				}

				jor_Array.put(jor);
				m++;
			}
			double duration = (int) ((System.currentTimeMillis() - startTime) / 1000.0);
 
			StringBuilder param = new StringBuilder("[");
			if ((paramArrayOfObject != null) && (paramArrayOfObject.length > 0)) {
				param.append(paramArrayOfObject[0]);
				for (int v=1; v < paramArrayOfObject.length; v++)
					param.append(",").append(paramArrayOfObject[v]);
			}
			param.append("]");

			if (duration > logDuration)
				logger.info("(" + duration + " s, cnt " + m+ ") " + sql + param);
			else
				logger.debug("(" + duration + " s, cnt " + m+ ") " + sql + param);

			return jor_Array;
			
		} catch (Exception e) {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception ew) {
			}
			StringBuilder param = new StringBuilder("[");
			if ((paramArrayOfObject != null) && (paramArrayOfObject.length > 0)) {
				param.append(paramArrayOfObject[0]);
				for (int i = 1; i < paramArrayOfObject.length; i++)
					param.append(",")
							.append(paramArrayOfObject[i]);
			}
			param.append("]");

			logger.error("Error doing query:"+ sql + param + ":" + e);
			throw new QueryException("Error doing query:" + sql,e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception ew) {
			}
		}
	}
	/**
	 * 
	 * @param sql
	 * @param paramArrayOfObject
	 * @param conn
	 * @param toUpper
	 * @return
	 * @throws QueryException
	 */
	public JSONObject doQueryObject(String sql,
			Object[] paramArrayOfObject, Connection conn,
			boolean toUpper) throws QueryException {
		JSONObject jor = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			int m = 0;
			long startTime = System.currentTimeMillis();
			pstmt = createStatement(sql, paramArrayOfObject, conn);
			rs = pstmt.executeQuery();
			ResultSetMetaData rs_metadata=rs.getMetaData();
			int colCount = rs.getMetaData().getColumnCount();

			while (rs.next()) {
				jor = new JSONObject();
				for (int k = 1; k <= colCount; k++) {
					Object localObject = rs.getObject(k);
					String str = toUpper ? rs_metadata
							.getColumnName(k).toUpperCase()
							: rs_metadata.getColumnName(k)
									.toLowerCase();
					if (rs.wasNull()) {
						jor.put(str, JSONObject.NULL);
					} else {
						if ((localObject instanceof Clob)) {
							localObject = ((Clob) localObject).getSubString(1L,
									(int) ((Clob) localObject).length());
						}
						jor.put(str, localObject);
					}
				}
				m++;
			}
			

			double duration = (int) ((System.currentTimeMillis() - startTime) / 1000.0);
			 
			StringBuilder param = new StringBuilder("[");
			if ((paramArrayOfObject != null) && (paramArrayOfObject.length > 0)) {
				param.append(paramArrayOfObject[0]);
				for (int v=1; v < paramArrayOfObject.length; v++)
					param.append(",").append(paramArrayOfObject[v]);
			}
			param.append("]");

			if (duration > logDuration)
				logger.info("(" + duration + " s, cnt " + m+ ") " + sql + param);
			else
				logger.debug("(" + duration + " s, cnt " + m+ ") " + sql + param);

			return jor;
			
		} catch (Exception e) {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception ew) {
			}
			StringBuilder param = new StringBuilder("[");
			if ((paramArrayOfObject != null) && (paramArrayOfObject.length > 0)) {
				param.append(paramArrayOfObject[0]);
				for (int i = 1; i < paramArrayOfObject.length; i++)
					param.append(",")
							.append(paramArrayOfObject[i]);
			}
			param.append("]");

			logger.error("Error doing query:"+ sql + param + ":" + e);
			throw new QueryException("Error doing query:" + sql,e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception ew) {
			}
		}
	}
/**
 * 
 * @param sql
 * @param paramArrayOfObject
 * @param paramInt
 * @param conn
 * @return
 * @throws QueryException
 */
	public JSONArray doQueryJSONArray(String sql, Object[] paramArrayOfObject,
			int paramInt, Connection conn) throws QueryException {
		JSONArray jor_arry = new JSONArray();

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			long startTime = System.currentTimeMillis();
			//Connection pcon=getConnection();
			pstmt = createStatement(sql, paramArrayOfObject, conn);
			//rs = pstmt.executeQuery();
			//int p=1741;
			//pstmt= conn.prepareStatement(sql);
			//pstmt.setInt(1, p);
			rs = pstmt.executeQuery();
			ResultSetMetaData rs_metadata = rs.getMetaData();
			int colCount = rs.getMetaData().getColumnCount();
			int i = 0;
			Object local;
			logger.debug("colCount is "+colCount);
			logger.debug("paramInt is "+paramInt);
			if (colCount == 1) {
				while (i != paramInt) {
					i++;
					while (rs.next()) {
						local = rs.getObject(1);
						if (rs.wasNull())
							jor_arry.put(JSONObject.NULL);
						else {
							jor_arry.put(local);
						}
					}
					if (paramInt <= i)
						break;
				}
			} else {
				logger.debug("do while");
				while (i != paramInt) {
					i++;
					while (rs.next()) {
						// logger.debug("get rs");
						JSONArray rs_jor = new JSONArray();
						for (int m = 1; m <= colCount; m++) {
							Object localObject = rs.getObject(m);
							// logger.debug("get g");
							// logger.debug(String.valueOf(rs.getObject(m)));
							if (rs.wasNull()) {
								// logger.debug("get null");
								rs_jor.put(JSONObject.NULL);
							} else {
								// logger.debug("get object");
								rs_jor.put(rs.getObject(m));
							}
						}
						jor_arry.put(rs_jor);
					}
					if (paramInt <= i)
						break;
				}
			}
			logger.debug("jor_arry "+jor_arry.toString());
			
			double duration = (int) ((System.currentTimeMillis() - startTime) / 1000.0);

			StringBuilder param = new StringBuilder("[");
			if ((paramArrayOfObject != null) && (paramArrayOfObject.length > 0)) {
				param.append(paramArrayOfObject[0]);
				for (int v = 1; v < paramArrayOfObject.length; v++)
					param.append(",").append(paramArrayOfObject[v]);
			}
			param.append("]");

			if (duration > logDuration)
				logger.info("(" + duration + " s, cnt " + i + ") " + sql
						+ param);
			else
				logger.debug("(" + duration + " s, cnt " + i + ") " + sql
						+ param);

			return jor_arry;

		} catch (Exception e) {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception ew) {
			}
			StringBuilder param = new StringBuilder("[");
			if ((paramArrayOfObject != null) && (paramArrayOfObject.length > 0)) {
				param.append(paramArrayOfObject[0]);
				for (int i = 1; i < paramArrayOfObject.length; i++)
					param.append(",").append(paramArrayOfObject[i]);
			}
			param.append("]");

			logger.error("Error doing query:" + sql + param + ":" + e);
			throw new QueryException("Error doing query:" + sql, e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception ew) {
			}
		}
	}
	/**
	 * 
	 * @param sql
	 * @param paramArrayOfObject
	 * @param conn
	 * @return
	 * @throws SQLException
	 * @throws QueryException
	 */
	private static PreparedStatement createStatement(String sql,
			Object[] paramArrayOfObject, Connection conn) throws SQLException,
			QueryException {
		PreparedStatement pstmt = conn.prepareStatement(sql);
		//logger.debug("paramArrayOfObject is"+ String.valueOf(paramArrayOfObject));
		if (paramArrayOfObject != null){
			logger.debug("createStatement now!");
			for (int i = 0; i < paramArrayOfObject.length; i++) {
				Object parm = paramArrayOfObject[i];
				logger.debug("localObject is "+String.valueOf(parm));
				if (paramArrayOfObject == null) {
					throw new QueryException(
							"Intenal Error: unsupport null as parameter");
				}
				if ((parm instanceof String)) {
					logger.debug("createStatement String!");
					pstmt.setString(i + 1, (String) parm);
				} else if ((parm instanceof Integer)) {
					int val=((Integer) parm).intValue();
					logger.debug("createStatement Integer! val is"+String.valueOf(val));
					pstmt.setInt(i + 1, val);
				} else if ((parm instanceof Float)) {
					logger.debug("createStatement Float!");
					pstmt.setFloat(i + 1, ((Float) parm).floatValue());
				} else if ((parm instanceof Double)) {
					logger.debug("createStatement Double!");
					pstmt.setDouble(i + 1, ((Double) parm).doubleValue());
				} else if ((parm instanceof Date)) {
					logger.debug("createStatement Date!");
					pstmt.setTimestamp(i + 1, new Timestamp(
							((Date) parm).getTime()));
				} else if (((parm instanceof StringBuilder))
						|| ((parm instanceof StringBuffer))) {
					logger.debug("createStatement StringBuffer!");
					// converting conn to oracle.jdbc.OracleConnection
					oracle.jdbc.OracleConnection oc = null;
					if ((conn instanceof WrappedConnection)) {
						oc = (OracleConnection) ((WrappedConnection) conn)
								.getUnderlyingConnection();
					} else if ((conn instanceof OracleConnection))
						oc = (OracleConnection) conn;
					else {
						throw new SQLException(
								"Not supported connection class:"
										+ oc.getClass().getName()
										+ " (only oracle and jboss connection supported)");
					}
					pstmt.setClob(i + 1, getCLOB(parm.toString(), oc));
				} else if ((parm instanceof Clob)) {
					logger.debug("createStatement Clob!");
					pstmt.setClob(i + 1, (Clob) parm);
				} else if ((parm instanceof Class)) {
					logger.debug("instanceof Class!");
					if (parm.equals(String.class))
						pstmt.setNull(i + 1, 12);
					else if (parm.equals(Integer.class))
						pstmt.setNull(i + 1, 4);
					else if (parm.equals(Float.class))
						pstmt.setNull(i + 1, 6);
					else if (parm.equals(Double.class))
						pstmt.setNull(i + 1, 8);
					else if (parm.equals(Date.class))
						pstmt.setNull(i + 1, 93);
					else if (parm.equals(StringBuilder.class))
						pstmt.setNull(i + 1, 2005);
					else if (parm.equals(StringBuffer.class))
						pstmt.setNull(i + 1, 2005);
					else if (parm.equals(Clob.class))
						pstmt.setNull(i + 1, 2005);
					else
						throw new QueryException(
								"Intenal Error: unsupported type:"
										+ parm);
				} else {
					throw new QueryException("Intenal Error: unsupported type:"
							+ parm.getClass() + ",value=" + parm);
				}
			}
		}
		return pstmt;
	}

 /**
  * 
  */
	public List doQueryList(String sql, Object[] paramArrayOfObject,
			int paramInt, Connection conn) throws QueryException {
		ArrayList al = new ArrayList();

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			long startTime = System.currentTimeMillis();
			pstmt = createStatement(sql, paramArrayOfObject, conn);
			rs = pstmt.executeQuery();

			double duration = (int) ((System.currentTimeMillis() - startTime) / 1000.0);
	
			int colCount = rs.getMetaData().getColumnCount();
			int i=0;
			logger.debug("colCount is:"+String.valueOf(colCount));
			if (colCount == 1) {
				while (i != paramInt) {
					i++;
					while (rs.next()) {
						al.add(rs.getObject(1));
					}
					if (paramInt <= i)
						break;
					
				}
			} else {
				while (i != paramInt) {
					i++;
					while (rs.next()) {
						// logger.debug("get rs");
						List rec = new java.util.ArrayList();
						for (int m = 1; m <= colCount; m++) {
							rec.add(rs.getObject(m));
						}
						al.add(rec);
					}
					if (paramInt <= i)
						break;
					
				}
			}
			if (duration > logDuration)
				logger.info("(" + duration + " s, cnt " + i + ") " + sql);
			else
				logger.debug("(" + duration + " s, cnt " + i + ") " + sql);
			
			return al;
		} catch (SQLException e) {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception e2) {
			}
			logger.error("Error doing query::" + sql + "::" + e);
			throw new QueryException("Error doing query:" + sql, e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception e) {
			}

		}
	}
    /**
     * 
     * @param sql
     * @param paramArrayOfObject
     * @param conn
     * @return
     * @throws QueryException
     */
    
	public int executeUpdate(String sql, Object[] paramArrayOfObject,
			Connection conn) throws QueryException {
		PreparedStatement pstmt = null;
		try {
			long startTime = System.currentTimeMillis();
			pstmt = createStatement(sql, paramArrayOfObject, conn);
			
			double duration = (int) ((System.currentTimeMillis() - startTime) / 1000.0);
			if (duration > logDuration)
				logger.info("(" + duration + " s) " + sql);
			else
				logger.debug("(" + duration + " s) " + sql);
			return  pstmt.executeUpdate();
			
		} catch (SQLException e) {
			try {
				pstmt.close();
			} catch (Exception ew) {
			}
			try {
				conn.close();
			} catch (Exception ea) {
			}
			StringBuilder param = new StringBuilder();
			for (int i = 0; i < paramArrayOfObject.length - 1; i++)
				param.append(paramArrayOfObject[i]).append(", ");
			param.append(paramArrayOfObject[(paramArrayOfObject.length - 1)]);
			logger.error("Error doing update:"+ sql + " (" + param + "):"+ e);
			throw new QueryException("Error doing query:" + sql,e);
		} finally {
			try {
				pstmt.close();
			} catch (Exception ew) {
			}
		}
	}

    /**
     * @param params should contain:
     * 
     */
    public Collection executeFunction ( String fncName, Collection params, Collection results, Connection conn) throws QueryException {
      CallableStatement stmt = null;
      ArrayList returns = new ArrayList();
      try {
      	//logger.debug("Begin call function:"+ fncName);
        long startTime = System.currentTimeMillis();
        String resultQ = "";
        for( int i=0; i<results.size(); i++) {
          resultQ += ",?";
        }
        if( resultQ.length()>0) resultQ = resultQ.substring(1);
        String paramQ = "";
        for( int j=0; j<params.size(); j++) {
          paramQ += ",?";
        }
        if( paramQ.length()>0) paramQ = paramQ.substring(1);
        logger.debug( "execute function:"+ ("{ " + resultQ + " = call " + fncName + " (" + paramQ + ")}"));
        stmt = conn.prepareCall( "{ " + resultQ + " = call " + fncName + " (" + paramQ + ")}");
        int k = 0;
        for( Iterator itresults=results.iterator(); itresults.hasNext();){
            k ++;
            Object result = itresults.next();
            if( result.equals( String.class)){
                stmt.registerOutParameter(k, Types.VARCHAR);
            }else if( result.equals( Integer.class)){
                stmt.registerOutParameter(k, Types.INTEGER);
            }else if( result.equals(Float.class)){
                stmt.registerOutParameter(k, Types.FLOAT);
            }else if( result.equals(java.sql.Clob.class)){
                stmt.registerOutParameter(k, Types.CLOB);
            }else{
            	logger.error("Found unsupported result type:"+ result+", pos="+ k);
                throw new QueryException("Intenal Error: unsupported type:"+ result.getClass()+",value="+result);
            }
        }
        for( Iterator itparams = params.iterator();itparams.hasNext();){
            k ++;
            Object param= itparams.next();
            if( param instanceof String){
            	// found oracle trick, callable statement does not support 32k string converting to clob
            	// and setClob must be called (yfzhu 2009-12-19) if function has clob as argument type
                stmt.setString(k,(String)param);
            }else if( param instanceof Integer){
                stmt.setInt(k,((Integer)param).intValue());
            }else if( param instanceof Float){
                stmt.setFloat(k,((Float)param).floatValue());
            }else if( param instanceof StringBuffer){// clob type convertion
//            	 converting conn to oracle.jdbc.OracleConnection
            	oracle.jdbc.OracleConnection oc=null;
            	if(conn instanceof org.jboss.resource.adapter.jdbc.WrappedConnection){
            		oc=(oracle.jdbc.OracleConnection)( (org.jboss.resource.adapter.jdbc.WrappedConnection)conn).getUnderlyingConnection();
            		
            	}else if(conn instanceof oracle.jdbc.OracleConnection){
            		oc=(oracle.jdbc.OracleConnection)conn;
            	}else{
            		throw new SQLException("Not supported connection class:"+ conn.getClass().getName()+" (only oracle and jboss connection supported)");
            	}
            	stmt.setClob(k, getCLOB((StringBuffer)param,oc));
            	logger.debug("oracle clob used for stmt");
            	
            }else{
            	logger.error("Found unsupported type:"+ param+", pos="+ k);
                throw new QueryException("Intenal Error: unsupported type:"+ param.getClass()+",value="+param);
            }
        }
        stmt.executeUpdate();
        int l = 0;
        for( Iterator itresults=results.iterator(); itresults.hasNext();){
            l ++;
            Object result = itresults.next();
            if( result == String.class){
                returns.add( stmt.getString( l));
            }else if( result ==Integer.class){
                returns.add( new Integer(stmt.getInt( l)));
            }else if( result ==Float.class){
                returns.add( new java.math.BigDecimal(stmt.getFloat( l)));
            }else if( result ==java.sql.Clob.class){
            	Object s= stmt.getObject(l);
            	// always return String
                returns.add( ((java.sql.Clob)s).getSubString(1, (int) ((java.sql.Clob)s).length()));
            }else{
                throw new QueryException("Intenal Error: unsupported type:"+ result.getClass()+",value="+result);
            }
        }
//        logger.debug("("+(int)((System.currentTimeMillis()-startTime)/1000)+" ms) " + fncName);
        return returns;
      }catch(SQLException e){
        logger.error("Error doing Stored Procedure:"+fncName+":"+ e.getMessage(),e);
    	//throw new QueryException("Error doing Stored Procedure:"+spName, e);
        throw new QueryException(parseMainMessage(e));
      }finally{
        try{stmt.close();}catch(Exception ea){}
        //logger.debug("end call function:"+ fncName);
      }
    }
    public SPResult executeStoredProcedure(String spName, Collection params, boolean hasReturnValue) throws QueryException{
        Connection conn=null;
        try{
            conn=getConnection();
            return executeStoredProcedure(spName, params,hasReturnValue,conn);
        }finally{
            try{closeConnection(conn);}catch(Exception eb){}
        }
    }

    public SPResult executeStoredProcedure(String spName, Collection params, boolean hasReturnValue, Connection con) throws QueryException{

        CallableStatement stmt=null;


        try{
        long startTime=System.currentTimeMillis();

        String paramQ="";
        for( int i=0;i< params.size();i++){
            paramQ +=",?";
        }
        if( hasReturnValue) paramQ += ",?,?";
        if( paramQ.length()>0) paramQ= paramQ.substring(1);

        stmt= con.prepareCall(( "{ call "+spName+" ("+paramQ+")}" )) ;
//        logger.debug("--- " + stmt) ;
        int j=0;
        String desc= "{ call "+spName+" (";
        for( Iterator it= params.iterator();it.hasNext();){
            j ++;
            Object param= it.next();
            desc +=param+",";
            if( param instanceof String){
                stmt.setString(j, (String)param);
                
            }else if( param instanceof Integer){
                stmt.setInt(j,((Integer)param).intValue());
            }else if( param instanceof Float){
                stmt.setFloat(j,((Float)param).floatValue());
            }else if( param instanceof StringBuffer){// clob type convertion
//            	 converting conn to oracle.jdbc.OracleConnection
            	oracle.jdbc.OracleConnection oc=null;
            	if(con instanceof org.jboss.resource.adapter.jdbc.WrappedConnection){
            		oc=(oracle.jdbc.OracleConnection)( (org.jboss.resource.adapter.jdbc.WrappedConnection)con).getUnderlyingConnection();
            		
            	}else if(con instanceof oracle.jdbc.OracleConnection){
            		oc=(oracle.jdbc.OracleConnection)con;
            	}else{
            		throw new SQLException("Not supported connection class:"+ con.getClass().getName()+" (only oracle and jboss connection supported)");
            	}
            	stmt.setClob(j, getCLOB((StringBuffer)param,oc));
            	logger.debug("oracle clob used for stmt");
            	
            }else{
                throw new QueryException("Intenal Error: unsupported type:"+ param.getClass()+",value="+param);
            }
//            logger.debug("--- " + param) ;
//            logger.debug(" set param "+j+": "+ param);
        }
        logger.debug("execute stored procedure:"+ desc +")}");
        
        if( hasReturnValue) {
           stmt.registerOutParameter( j+1, java.sql.Types.INTEGER);
           stmt.registerOutParameter( j+2, java.sql.Types.VARCHAR);
        }
        stmt.executeUpdate();
        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
        if (duration > logDuration ) logger.info("("+duration+" s) "+spName);
        if ( hasReturnValue) {
            SPResult spr= new SPResult(stmt, j+1);
            return spr;
        }else{
            SPResult spr= new SPResult();
            return spr;
        }

        }catch(SQLException e){
            logger.error("Error doing Stored Procedure:"+spName+":"+ e.getMessage());
        	//throw new QueryException("Error doing Stored Procedure:"+spName, e);
            throw new QueryException(parseMainMessage(e));
        }finally{
            try{stmt.close();}catch(Exception ea){}
        }

    }
    /**
     * Oracle error message example:
     * ORA-20201: 明细对应的应收付款单有和收付款单不一致的业务伙伴！
     ORA-06512: at "NDS3.C_ALLOCATION_CHECK_BPARTNER", line 17
     ORA-06512: at "NDS3.C_ALLOCATION_AM", line 26
     ORA-06512: at line 1
     
     The return message will be " 明细对应的应收付款单有和收付款单不一致的业务伙伴！" only when in production mode
     * 
     * @param e
     * @return
     */
    private String parseMainMessage(SQLException e){
    	String s= e.getMessage();
    	if(isDebug) return s;
    	int p= s.indexOf("ORA-");
    	int q= s.indexOf("ORA-",p+1);
    	String r;
    	if(p>=0){
    		if(q>0)r=s.substring(p+11, q-1);
    		else r= s.substring(p+11);
    	}else
    		r=s;
    	return r;
    }
    /**  调用函数GET_EMPLOYEEID(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getEmployeeId( int operateid) throws QueryException {
      CallableStatement stmt = null;
      Connection conn = null;
      try{
          conn = getConnection();
          stmt = conn.prepareCall("{? = call GETEMPID(?)}");
          stmt.registerOutParameter(1,Types.INTEGER);
          stmt.setInt(2,operateid);
          stmt.executeUpdate();
          int result = stmt.getInt(1);
          if(result==-1){
              throw new QueryException("The user is not a employee!");
          }
          return result;
      }catch(SQLException e){
          throw new QueryException("Error doing Stored Procedure:", e);
      }finally{
          try{stmt.close();}catch(Exception ea){}
          try{closeConnection(conn);}catch(Exception eb){}
      }
    }
    /**
     * get sequence of specified name
     */
    public int getSequence( String tableName, Connection conn) throws QueryException {
      CallableStatement stmt = null;
      try{
      stmt = conn.prepareCall("{? = call get_Sequences(?)}");
      stmt.registerOutParameter(1,Types.INTEGER );
      /**
       * Begin from 2.0, one db table may has multiple (updatable )views.
       * So the specified <param>tableName</param> may be the same one.
       * 
       * We will try first figure out the real table name, and use that to
       * get sequence.
       * @since 2.0
       */
      Table tb= TableManager.getInstance().getTable(tableName);
      if(tb!=null) tableName= tb.getRealTableName();
      stmt.setString(2,tableName);
      
      stmt.executeUpdate();
      int result = stmt.getInt(1);
      return result;
      }catch(SQLException e){
          throw new QueryException("Error doing Stored Procedure:", e);
      }finally{
          try{stmt.close();}catch(Exception ea){}
      }

    }
    /**  Find table's 
      */
     public String getSheetNo(String sequenceHead, int clientId) throws QueryException {
       CallableStatement stmt = null;
       Connection conn = null;
       try{
           conn = getConnection();
           stmt = conn.prepareCall("{? = call get_SequenceNo(?,?)}");
           stmt.registerOutParameter(1,Types.VARCHAR);
           stmt.setString(2,sequenceHead );
           stmt.setInt(3,clientId);
           stmt.executeUpdate();
           String result = stmt.getString(1);
           return result;
       }catch(SQLException e){
           throw new QueryException(e.getMessage(), e);
       }finally{
           try{stmt.close();}catch(Exception ea){}
           try{closeConnection(conn);}catch(Exception eb){}
       }
    }
    /**  调用函数GETSheeetStatus(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getSheetStatus(String tableName,int tableId) throws QueryException {
      CallableStatement stmt = null;
      Connection conn = null;
      try{
          conn = getConnection();
          stmt = conn.prepareCall("{? = call GetStatus(?,?)}");
          stmt.registerOutParameter(1,Types.INTEGER);
          stmt.setString(2,tableName);
          stmt.setInt(3,tableId);
          stmt.executeUpdate();
          int result = stmt.getInt(1);
          return result;
      }catch(SQLException e){
          throw new QueryException("Error doing Stored Procedure:", e);
      }finally{
          try{stmt.close();}catch(Exception ea){}
          try{closeConnection(conn);}catch(Exception eb){}
      }
    }

    private Connection getConnection() throws QueryException {
        return QueryEngine.getInstance().getConnection();
    }

    private void closeConnection(Connection dbConnection)throws SQLException  {
        if (dbConnection != null && !dbConnection.isClosed()) {
            dbConnection.close();
        }
    }

}