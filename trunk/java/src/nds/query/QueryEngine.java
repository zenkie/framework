
/******************************************************************
*
*$RCSfile: QueryEngine.java,v $ $Revision: 1.11 $ $Author: Administrator $ $Date: 2006/07/12 10:10:59 $
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryEngine.java

package nds.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nds.control.util.EJBUtils;
import nds.db.DBController;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NativeTools;
import nds.util.Tools;
import sun.jdbc.rowset.CachedRowSet;
/**
 * Singleton
 * 通过JDBC 执行查询，由于对数据库是只读操作，故不会影响EJB。很显然，这个类是性能枢纽
 */
public class QueryEngine {
    //private static Logger logger= LoggerManager.getInstance().getLogger(QueryEngine.class.getName());
	private static final Log logger = LogFactory.getLog(nds.query.QueryEngine.class);	
    private static QueryEngine instance=null;
    private DBController controller= new DBController();
    private DataSource datasource;

    private  int logDuration =10;// IF query time (seconds) exceeds this value, it will be logged
    /* if query result has more lines than this value, powerful range sql will be created
       If this value is none positive, then range sql will nerver be created
    */
    private int minRangeSQLCount= 1000;


    private QueryEngine() throws javax.naming.NamingException{
        // check file, if invalid, exit system
    	//if(logger.isDebugEnabled())logger.debug("You licese:"+ NativeTools.getCPUIDs());
    	nds.util.LicenseManager.validateLicense("Agile ERP","2.0",  EJBUtils.getApplicationConfigurations().getProperty("license","/license.xml") );
        /*if((!"ddd56e5dac8d3536b806c78f42145d73".equals(Tools.getFileCheckSum(this.getClass(), "nds.util.LicenseManager")))){
        	Thread t=new Thread(new Runnable(){
        		public void run(){
        			try{
        				Thread.sleep(1000);
        				System.err.println("Important file changed, will exit.");
        				System.exit(1099);
        			}catch(Throwable e){
        			}
        		}
        	});
        	t.start();
        }*/
    	
    	// Get a context for the JNDI look up
        Context ctx = new InitialContext();
        // Look up myDataSource
        String name= nds.control.util.EJBUtils.getApplicationConfigurations().getProperty("jndi.datasource", "/nds/jdbc/DataSource");
        logger.info("Using datasource:"+name);
        datasource = (DataSource) ctx.lookup (name);


    }
    private QueryEngine(DataSource ds){
        logger.debug("Creating QueryEngine using DataSource:"+ ds);
        datasource= ds;
    }
    protected QueryEngine(String name) {

    }
    public int executeUpdate(String sql)throws SQLException{
        Connection con=null;
        Statement stmt=null;
        try{
            con=getConnection();
        }catch(Exception e){
            throw new SQLException("Can not get connection:"+e.getLocalizedMessage());
        }
        try{
        	stmt = con.createStatement();
        	logger.debug(sql);
            return stmt.executeUpdate(sql);
        }finally{
            try{closeConnection(con);}catch(Exception e){}
        }
    }
    /*public QueryRequestImpl createRequest(){
        return controller.createRequest();
    }*/
    public QueryRequestImpl createRequest(QuerySession session){
        return controller.createRequest(session);
    }    
    public int doUpdate(List vec)throws UpdateException{
        Connection con=null;
        try{
            con=getConnection();
        }catch(Exception e){
            logger.error("Error", e) ;
            throw new UpdateException("@no-connection@："+e.getLocalizedMessage());
        }
        int count =0;
        try{
            count=doUpdate( vec,con);
        }finally{
            try{closeConnection(con);}catch(Exception e){}
        }
        return count;
    }
    /**
    * Update database according the SQL string in <code>vect</code>
    * @param vec elements: String( sql format)
    *
    */
    public int doUpdate(List vec, Connection con) throws UpdateException{
        Statement stmt=null;
        int count = 1; // start from 1 for user's convenience
        Iterator ite = vec.iterator() ;
        String sql = null;
        try{
            stmt = con.createStatement();
            while(ite.hasNext() ){
                sql = (String)ite.next() ;
                logger.debug(sql);
                stmt.executeUpdate(sql);
                count ++;
            }
        }catch(Exception e){
            logger.error(sql, e) ;
            throw new UpdateException("@error-at-line@ "+ count+":"+e.getLocalizedMessage());
        }finally{
            try{stmt.close();}catch(Exception ea){}
        }
        return count;
    }
    /**
     * @return totol count of query
     */
    public int getTotalResultRowCount(QueryRequest quest) throws QueryException{
        Connection con=null;
        Statement stmt=null;
        ResultSet rs=null;
        String sql=null;
        int count=-1;
        try{
        long startTime=System.currentTimeMillis();
        con=getConnection();
        stmt= con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY ) ;

        // count the total size of request
        sql= quest.toCountSQL();
        rs= stmt.executeQuery(sql);
        if( rs.next()){
            count= rs.getInt(1);
        }
        }catch(Exception e){
            logger.error("Error doing query :"+ sql, e);
            throw new QueryException("Error doing query:"+ sql, e);
        }finally{
            try{stmt.close();}catch(Exception ea){}
            try{rs.close();}catch(Exception e){}
            try{closeConnection(con);}catch(Exception eb){}
        }
        return count;
    }
    public QueryResult doQuery(QueryRequest quest)throws QueryException {
    	Connection con=null;
    	try{
    		con=getConnection();
    		return doQuery(quest, con);
    	}finally{
    		try{closeConnection(con);}catch(Exception eb){}
    	}
    }
    /**
     * do dummy query 
     * @param quest
     * @param err error info for why dummy result generated
     * @return
     * @throws QueryException
     */
    public QueryResult doDummyQuery(QueryRequest quest,String err){
   		QueryResultImpl qr=new DummyQueryResultImpl(quest,err);
   		return qr;
    }
    /**
     * @return number of records start from QueryRequest.getStartRowIndex(),
     *      and at maximum QueryRequest.getRange()
     *
     * @roseuid 3B822F570389
     */
    public QueryResult doQuery(QueryRequest quest, Connection con)throws QueryException {
        Statement stmt=null;
        ResultSet rs=null;
        String sql=null;
        try{
        long startTime=System.currentTimeMillis();
        stmt= con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY ) ;

        // count the total size of request
        sql= quest.toCountSQL();
        rs= stmt.executeQuery(sql);
        int count=-1;
        if( rs.next()){
            count= rs.getInt(1);
        }
//        logger.debug("(count="+count+","+(int)((System.currentTimeMillis()-startTime)/1000)+" ms) "+sql);
        rs.close();
        // get detailed result of quest
        boolean isRangedSQL=false;
       // Where there's more than 100 rows to be skipped, we will use the most powerful sql engine
       // to generate only needed data.
       // So why not 200 rows, or a expression based on range and count? OK, it's not so reasonable.
       /*-- yfzhu modified at 2003-07-16 to allow ranged sql when row count is bigger than 1000 */
       /*-- yfzhu modified at 2004-05-08 to support db which does not support range limit, such as hsql*/
//        if( quest.getStartRowIndex()>100){
        if(count > minRangeSQLCount && minRangeSQLCount > 0){
            sql=quest.toSQLWithRange();
            isRangedSQL=true;
        }else{
            sql= quest.toSQL();
        }
        rs= stmt.executeQuery(sql);

        QueryResultImpl qr=new QueryResultImpl(rs,quest, count, isRangedSQL);
        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
        if (duration >= logDuration ) logger.info("("+duration+" s) "+sql);
        else logger.debug("("+duration+" s) "+sql);
        return qr;
        }catch(Exception e){
            logger.error("Error doing query :"+ sql, e);
            throw new QueryException("Error doing query:"+ sql, e);
        }finally{
            try{stmt.close();}catch(Exception ea){}
            try{rs.close();}catch(Exception e){}
        }
    }
    // marked up 2007-5-10 for doQueryNoRange
    
    /*
    public QueryResult doQueryNoRange(QueryRequest quest)throws QueryException {
    	Connection con=null;
    	try{
    		 con=getConnection();
    		 return doQueryNoRange(quest, con);
    	}finally{
    		try{closeConnection(con);}catch(Exception eb){}
    	}
    }
   
    public QueryResult doQueryNoRange(QueryRequest quest, Connection con)throws QueryException {
        Statement stmt=null;
        ResultSet rs=null;
        String sql=null;
        try{
        long startTime=System.currentTimeMillis();
        stmt= con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY ) ;

        // count the total size of request
        sql= quest.toCountSQL();
        rs= stmt.executeQuery(sql);
        int count=-1;
        if( rs.next()){
            count= rs.getInt(1);
        }
        //        logger.debug("(count="+count+","+(int)((System.currentTimeMillis()-startTime)/1000)+" ms) "+sql);
        rs.close();
        // get detailed result of quest
        boolean isRangedSQL=false;
        // Where there's more than 100 rows to be skipped, we will use the most powerful sql engine
        // to generate only needed data.
        // So why not 200 rows, or a expression based on range and count? OK, it's not so reasonable.
        sql= quest.toSQL();
        rs= stmt.executeQuery(sql);

        QueryResultImpl qr=new QueryResultImpl(rs,quest, count);
        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
        if (duration > logDuration ) logger.info("("+duration+" s) "+sql);
        else logger.debug("("+duration+" s) "+sql);
        return qr;
        }catch(Exception e){
            logger.error("Error doing query :"+ sql, e);
            throw new QueryException("Error doing query:"+ sql, e);
        }finally{
            try{stmt.close();}catch(Exception ea){}
            try{rs.close();}catch(Exception e){}
            
        }
    }*/
    
    /**
     * Retriever first row first column of the query result
     * @param sql
     * @return null if not found
     * @throws QueryException
     */
    public Object doQueryOne(String sql, Connection con) throws QueryException{
    	Statement stmt=null;
        ResultSet rs=null;
        try{
	        long startTime=System.currentTimeMillis();
	        stmt= con.createStatement( );
	        rs= stmt.executeQuery(sql);
	        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
	        if (duration > logDuration) logger.info("("+duration+" s) "+sql);
			else logger.debug("("+duration+" s) "+sql);
	        if(rs.next()) return rs.getObject(1);
	        else return null;
        }catch(SQLException e){
            try{stmt.close();}catch(Exception ea){}
            try{rs.close();}catch(Exception e2){}
            logger.error("Error doing query::"+sql +"::"+ e);
            throw new QueryException("Error doing query:"+sql, e);
        }finally{
                try{stmt.close();}catch(Exception ea){}
                try{rs.close();}catch(Exception e){}
        }    	
    	
    }    
    /**
     * Retriever first row first column of the query result
     * @param sql
     * @return null if not found
     * @throws QueryException
     */
    public Object doQueryOne(String sql) throws QueryException{
        Connection con=getConnection();
        try{
        	return doQueryOne(sql, con);
        }finally{
            try{closeConnection(con);}catch(Exception eb){}
        }    	
    	
    }
    public List doQueryList(String sql)throws QueryException{
    	Connection con=getConnection();
        try{
        	return doQueryList(sql, con);
        }finally{
            try{closeConnection(con);}catch(Exception eb){}
        }    
    }
    /**
     * 
     * @param sql 
     * @return List that contain column of the resultset sql
     * If sql contains only one column, elements will be Object of that column type
     * If sql contains more than one columns, elements will be List also
     * @throws QueryException
     */
    public List doQueryList(String sql, Connection con)throws QueryException{
    	List al=new java.util.ArrayList();
        
        	Statement stmt=null;
            ResultSet rs=null;
            try{
    	        long startTime=System.currentTimeMillis();
    	        stmt= con.createStatement( );
    	        rs= stmt.executeQuery(sql);
    	        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
    	        if (duration > logDuration) logger.info("("+duration+" s) "+sql);
    			else logger.debug("("+duration+" s) "+sql);
    	        int colCount= rs.getMetaData().getColumnCount();
    	        int i;
    	        if( colCount==1){
	    	        while(rs.next()){
	    	        	al.add(rs.getObject(1));
	    	        }
    	        }else{
        	        while(rs.next()){
        	        	List rec=new java.util.ArrayList();
        	        	for(i=1;i<=colCount; i++){
        	        		rec.add(rs.getObject(i));
        	        	}
        	        	al.add(rec);
        	        }
    	        }
    	        return al;
            }catch(SQLException e){
                try{stmt.close();}catch(Exception ea){}
                try{rs.close();}catch(Exception e2){}
                logger.error("Error doing query::"+sql +"::"+ e);
                throw new QueryException("Error doing query:"+sql, e);
            }finally{
                    try{stmt.close();}catch(Exception ea){}
                    try{rs.close();}catch(Exception e){}
                    
            }    	
        	

    }

    /**Note, after retriving all data from result, the connection must be closed
     * using ResultSet.getStatement().getConnection()
     * @param cached, if true, the row set will be cached, and need no close explicitly, but will be slower
     * @return Read-only result set will be returned
     * @roseuid 3B86ED350240
     */
    public ResultSet doQuery(String sql, boolean cached) throws QueryException{
        Statement stmt=null;
        ResultSet rs=null;
        Connection con=null;
        try{
        long startTime=System.currentTimeMillis();
        con=getConnection();
        stmt= con.createStatement( );
        rs= stmt.executeQuery(sql);
        int duration=(int)((System.currentTimeMillis()-startTime)/1000);
        if (duration > logDuration) logger.info("("+duration+" s) "+sql);
		else logger.debug("("+duration+" s) "+sql);
        if( cached ){
            CachedRowSet set= new CachedRowSet();
            set.populate(rs);
            return set;
        }else return rs;
        }catch(SQLException e){
            try{stmt.close();}catch(Exception ea){}
            try{rs.close();}catch(Exception e2){}
            try{closeConnection(con);}catch(Exception eb){}
            logger.error("Error doing query::"+sql +"::"+ e);
            throw new QueryException("Error doing query:"+sql, e);
        }finally{
            if(cached){
                try{stmt.close();}catch(Exception ea){}
                try{rs.close();}catch(Exception e){}
                try{closeConnection(con);}catch(Exception eb){}
            }
        }
    }
    /**
     * Do query using cached rowset, so the client need no close explicitly, but will be slower.
     */
    public ResultSet doQuery(String sql) throws QueryException{
        return doQuery(sql, true);
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
        return controller.executeStoredProcedure(spName,params,hasReturnValue, con);

    }
    public int getSequence( String tableName, Connection conn) throws QueryException {
    	return controller.getSequence(tableName, conn);
    }
    public int getSequence( String tableName) throws QueryException {
        Connection conn= getConnection();
        try{
    	return controller.getSequence(tableName, conn);
        }finally{
        	try{conn.close();}catch(Exception e){}
        }

    }
    /**  调用函数GET_EMPLOYEEID(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getEmployeeId( int operateid) throws QueryException {
        return controller.getEmployeeId(operateid);
    }



   /**  调用函数GETMAXID(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public String getSheetNo(String tableName, int clientId) throws QueryException {
        return controller.getSheetNo(tableName, clientId);
    }

    /**  调用函数GETSheeetStatus(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getSheetStatus(String tableName,int tableId) throws QueryException {
        int status= Tools.getInt(this.doQueryOne("select status from "+ tableName+" where id="+ tableId), 0);
    	return status;
    }

    public Collection executeFunction ( String fncName, Collection params, Collection results) throws QueryException {
        Connection conn=null;
        try{
        	conn=getConnection();
            return controller.executeFunction(fncName,params, results,conn);
        }finally{
            try{closeConnection(conn);}catch(Exception eb){}
        }

    }
    public Collection executeFunction ( String fncName, Collection params, Collection results,Connection conn) throws QueryException {
        return controller.executeFunction(fncName,params, results,conn);
    }

    public Connection getConnection() throws QueryException {
        try{
        Connection dbConnection = null;
        if (datasource != null) dbConnection = datasource.getConnection();
        return dbConnection;
        }catch(SQLException e){
            throw new QueryException(e.getLocalizedMessage(), e);
        }
    }
    
    public void closeConnection(Connection dbConnection)throws SQLException  {
        if (dbConnection != null && !dbConnection.isClosed()) {
            dbConnection.close();
        }
    }
    /**
     * @param props should have following property
     * query.minlogduration (int)
     * query.rangesqlcount(int)
     * dbms.type( string supported values are "mysql","hsql", "oracle", which should be package name
     *            under nds.db)
     */
    public void init(Properties props){
        logDuration=Tools.getInt( props.getProperty("query.minlogduration", "10"), 10);
        minRangeSQLCount= Tools.getInt( props.getProperty("query.rangesqlcount", "1000"), 1000);
        controller.init(props);
    }
    public synchronized static  QueryEngine getInstance() throws QueryException{
        try{
            if( instance ==null) instance=new QueryEngine();
            return instance;
        }catch(Exception e){
            throw new QueryException("Error getting QueryEngine instance.", e);
        }
    }
    /**
     * @param ds will only be usable in the first time create QueryEngine instance.
     * After one instance created, next time time using this method will only return
     * last created instance, no matter ds changed or not.
     */
    public synchronized static  QueryEngine getInstance(DataSource ds) throws QueryException{
        try{
            if( instance ==null) {
                instance=new QueryEngine(ds);
            }
            return instance;
        }catch(Exception e){
            throw new QueryException("Error getting QueryEngine instance.", e);
        }
    }

}
