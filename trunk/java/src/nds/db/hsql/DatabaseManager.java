package nds.db.hsql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import nds.db.DBManager;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.DummyQuerySession;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.QuerySession;
import nds.query.SPResult;
import nds.util.NDSRuntimeException;

public class DatabaseManager implements DBManager{


    private static Logger logger= LoggerManager.getInstance().getLogger("HSQLDBManager");
    private  int logDuration =10;// IF query time (seconds) exceeds this value, it will be logged
    /* if query result has more lines than this value, powerful range sql will be created
       If this value is none positive, then range sql will nerver be created
    */
    private int minRangeSQLCount= 1000;

    public DatabaseManager() {
    }

    public void destroy(){
        Tools.destroy();
    }
    public nds.query.QueryRequestImpl createRequest(){
    	return new QueryRequestImpl();
    }
    public nds.query.QueryRequestImpl createRequest(QuerySession session){
        return new QueryRequestImpl(session);
    }    
    
    /**
     * @param props should have following property
     * query.minlogduration (int)
     * query.rangesqlcount(int)
     */
    public void init(Properties props){
        logDuration=nds.util.Tools.getInt( props.getProperty("query.minlogduration", "10"), 10);
        this.minRangeSQLCount=nds.util.Tools.getInt( props.getProperty("query.rangesqlcount", "1000"), 1000);

        try{
            Tools.init(QueryEngine.getInstance().getConnection());
        }catch(Exception e){
            throw new NDSRuntimeException("Could not get connection", e);
        }
    }
    // not ok , will not support result, so must set results to Empty
    public Collection executeFunction ( String fncName, Collection params, Collection results,Connection conn) throws QueryException {
      CallableStatement stmt = null;
      ArrayList returns = null;
      try {
        long startTime = System.currentTimeMillis();
        conn = getConnection();
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
//        logger.debug( "execute function:"+ ("{(" + resultQ + ") call " + fncName + " (" + paramQ + ")}"));
        stmt = conn.prepareCall( "{ " + resultQ + " = call " + fncName + " (" + paramQ + ")}");
        int k = 0;
        for( Iterator itresults=results.iterator(); itresults.hasNext();){
            k ++;
            Object result = itresults.next();
            if( result == String.class){
                stmt.registerOutParameter(k, Types.VARCHAR);
            }else if( result == Integer.class){
                stmt.registerOutParameter(k, Types.INTEGER);
            }else if( result == Float.class){
                stmt.registerOutParameter(k, Types.FLOAT);
            }else{
                throw new QueryException("Intenal Error: unsupported type:"+ result.getClass()+",value="+result);
            }
        }
        for( Iterator itparams = params.iterator();itparams.hasNext();){
            k ++;
            Object param= itparams.next();
            if( param instanceof String){
                stmt.setString(k,(String)param);
            }else if( param instanceof Integer){
                stmt.setInt(k,((Integer)param).intValue());
            }else if( param instanceof Float){
                stmt.setFloat(k,((Float)param).floatValue());
            }else{
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
            }else{
                throw new QueryException("Intenal Error: unsupported type:"+ result.getClass()+",value="+result);
            }
        }
//        logger.debug("("+(int)((System.currentTimeMillis()-startTime)/1000)+" ms) " + fncName);
        return returns;
      }catch(SQLException e){
        throw new QueryException("Error doing funtion:"+fncName+":"+ e.getLocalizedMessage(), e);
      }finally{
        try{stmt.close();}catch(Exception ea){}
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
    /**
     * @param hasReturnValue, if true, result must be string, which should be in format like
     *    number+":"+ result string, normally none zero number means error found.
     * Such as:
     *    -1:no data found
     *
     */
    public SPResult executeStoredProcedure(String spName, Collection params, boolean hasReturnValue, Connection con) throws QueryException{

        CallableStatement stmt=null;
        try{
        long startTime=System.currentTimeMillis();

        String paramQ="";
        for( int i=0;i< params.size();i++){
            paramQ +=",?";
        }
//        if( hasReturnValue) paramQ += ",?,?";
        if( paramQ.length()>0) paramQ= paramQ.substring(1);

        stmt= con.prepareCall(( "{ call "+spName+" ("+paramQ+")}" )) ;
        int j=0;
        for( Iterator it= params.iterator();it.hasNext();){
            j ++;
            Object param= it.next();
            if( param instanceof String){
                stmt.setString(j, (String)param);
            }else if( param instanceof Integer){
                stmt.setInt(j,((Integer)param).intValue());
            }else if( param instanceof Float){
                stmt.setFloat(j,((Float)param).floatValue());
            }else{
                throw new QueryException("Intenal Error: unsupported type:"+ param.getClass()+",value="+param);
            }
//            logger.debug("--- " + param) ;
//            logger.debug(" set param "+j+": "+ param);
        }
/*        if( hasReturnValue) {
           stmt.registerOutParameter( j+1, java.sql.Types.INTEGER);
           stmt.registerOutParameter( j+2, java.sql.Types.VARCHAR);
        }*/
        ResultSet rs= stmt.executeQuery();

        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
        if (duration > logDuration ) logger.info("("+duration+" s) "+spName);
        if ( hasReturnValue) {
            rs.next();
            String rt= rs.getString(1);
            int pt= rt.indexOf(":");
            if(pt > 0){
                int code=nds.util.Tools.getInt( rt.substring(0, pt), 0);// default to OK
                String msg= rt.substring(pt+1);
                return new SPResult(code, msg);
            }else
                return new SPResult(rt);
        }else{
            SPResult spr= new SPResult();
            return spr;
        }

        }catch(SQLException e){
            throw new QueryException("Error doing Stored Procedure:"+spName, e);
        }finally{
            try{stmt.close();}catch(Exception ea){}
        }

    }
    /**  调用函数GET_EMPLOYEEID(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getEmployeeId( int operateid) throws QueryException {
      return Tools.getEmployeeId(operateid);
    }
    public int getSequence( String tableName, Connection conn) throws QueryException {
      return Tools.getSequence(tableName);

    }
    /**  调用函数GETMAXID(int):这是Oracle中的函数
      *   参数：operateid(User 表中的ID)
      *   返回: employee表中的id
      */
     public String getSheetNo(String tableName, int clientId) throws QueryException {
       return Tools.getSheetNO(tableName, clientId);
    }
    /**  调用函数GETSheeetStatus(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getSheetStatus(String tableName,int tableId) throws QueryException {
        return Tools.getSheetStatus(tableName, tableId);
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