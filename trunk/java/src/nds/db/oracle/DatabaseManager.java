package nds.db.oracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import oracle.sql.CLOB;
import java.io.Writer;

import nds.db.DBManager;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.*;
import nds.query.SPResult;
import nds.util.Tools;
import nds.schema.*;
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
    public static CLOB getCLOB( StringBuffer clobData,oracle.jdbc.OracleConnection conn )
                    throws SQLException {
      CLOB tempClob = null;

      try {
        //  create a new temporary CLOB
        tempClob = CLOB.createTemporary( conn, true, CLOB.DURATION_SESSION );

        // Open the temporary CLOB in readwrite mode to enable writing
        tempClob.open( CLOB.MODE_READWRITE );

        // Get the output stream to write

        Writer tempClobWriter = tempClob.setCharacterStream( 0L );

        // Write the data into the temporary CLOB
        tempClobWriter.write( clobData.toString() );

        // Flush and close the stream
        tempClobWriter.flush(  );
        tempClobWriter.close(  );

        // Close the temporary CLOB
        tempClob.close(  );

      } catch ( Exception exp ) {
    	  logger.error("fail to getClob",exp);
	         // Free CLOB object
          tempClob.freeTemporary( );
          // do something
      }
      return tempClob;      

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