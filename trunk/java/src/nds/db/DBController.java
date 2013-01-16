package nds.db;

import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;
import java.util.List;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryException;
import nds.query.QueryRequestImpl;
import nds.query.*;
import nds.util.Loader;
import org.json.*;
/**
 * Work as proxy for respective database managers, the dbtype should be defined
 * as "dbms.type" in configuration file, and there should exists Class
 * nds.db.${dbms.type}.DatabaseManager which implements nds.db.DatabaseManager
 */
public class DBController implements java.io.Serializable, DBManager,
    nds.util.DestroyListener {
    private Logger logger= LoggerManager.getInstance().getLogger(DBController.class.getName());
//    private Director director;
    private DBManager manager;

    public DBController() {
    }

    public void init(Properties props){
        if (manager ==null){
            String dbType=  props.getProperty("dbms.type");
            if (dbType==null) throw new Error("Not found dbms.type in property file");
            String className="nds.db."+ dbType+".DatabaseManager";
            try{
                Class cl= Loader.loadClass(this.getClass(), className);
                manager=(DBManager) cl.newInstance();
                manager.init(props);
            }catch(Exception e){
                logger.error("Could not create object "+className );
            }
            logger.debug("DBController initialized.");
        }
    }
    public void destroy(){
        manager.destroy();
        manager=null;
        logger.debug("DBController destroied.");
    }
    public QueryRequestImpl createRequest(){
            return manager.createRequest();
    }
    public QueryRequestImpl createRequest(QuerySession session){
        return manager.createRequest(session);
    }    

   //--------------------------implements DBManager


    public Collection executeFunction ( String fncName, Collection params, Collection results,Connection conn) throws QueryException {
        return manager.executeFunction(fncName, params,results, conn);
    }
    public SPResult executeStoredProcedure(String spName, Collection params, boolean hasReturnValue) throws QueryException{
        return manager.executeStoredProcedure(spName,params, hasReturnValue);
    }
    public SPResult executeStoredProcedure(String spName, Collection params, boolean hasReturnValue,Connection conn) throws QueryException{
        return manager.executeStoredProcedure(spName,params, hasReturnValue,conn);
    }

    /**  调用函数GET_EMPLOYEEID(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getEmployeeId( int operateid) throws QueryException {
        return manager.getEmployeeId(operateid);
    }
    public int getSequence( String tableName, Connection conn) throws QueryException {
    	return manager.getSequence(tableName, conn);
    }

    public String getSheetNo(String tableName, int clientId) throws QueryException   {
        return manager.getSheetNo(tableName,clientId);
    }
    /**  @deprecated
     */
    public int getSheetStatus(String tableName,int tableId) throws QueryException {
        return manager.getSheetStatus(tableName, tableId);
    }

	public List doQueryList(String sql, Object[] paramArrayOfObject,
			int paramInt, Connection conn) throws QueryException {
		return manager.doQueryList(sql, paramArrayOfObject, paramInt, conn);
	}
	
	public JSONArray doQueryJSONArray(String sql,
			Object[] paramArrayOfObject, int paramInt,
			Connection conn) throws QueryException {
		return manager.doQueryJSONArray(sql, paramArrayOfObject,
				paramInt, conn);
	}

	public JSONArray doQueryObjectArray(String sql,
			Object[] paramArrayOfObject, Connection conn,
			boolean toUpper) throws QueryException {
		return manager.doQueryObjectArray(sql, paramArrayOfObject,
				conn, toUpper);
	}

	public int executeUpdate(String sql, Object[] paramArrayOfObject,
			Connection conn) throws QueryException {
		return manager.executeUpdate(sql, paramArrayOfObject,
				conn);
	}

	public JSONObject doQueryObject(String sql,
			Object[] paramArrayOfObject, Connection conn)
			throws QueryException {
		return manager.doQueryObject(sql, paramArrayOfObject,
				conn, true);
	}

	public JSONObject doQueryObject(String sql,
			Object[] paramArrayOfObject, Connection conn,
			boolean toUpper) throws QueryException {
		return manager.doQueryObject(sql, paramArrayOfObject,
				conn, toUpper);
	}


}