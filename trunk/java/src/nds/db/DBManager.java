package nds.db;

import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;
import java.util.List;
import nds.query.QueryException;
import nds.query.QueryRequestImpl;
import nds.query.*;
import nds.util.DestroyListener;
import org.json.*;

public interface DBManager extends DestroyListener  {
    public void init(Properties props);
    public Collection executeFunction ( String fncName, Collection params, Collection results,Connection conn) throws QueryException ;
    public SPResult executeStoredProcedure(String spName, Collection params, boolean hasReturnValue) throws QueryException;
    public SPResult executeStoredProcedure(String spName, Collection params, boolean hasReturnValue, Connection con) throws QueryException;

    /**  调用函数GET_EMPLOYEEID(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getEmployeeId( int operateid) throws QueryException ;
    public int getSequence( String tableName, Connection conn) throws QueryException ;
    /**  调用函数GETMAXID(int):这是Oracle中的函数
         *   参数：operateid(User 表中的ID)
         *   返回: employee表中的id
         */
    public String getSheetNo(String tableName, int clientId) throws QueryException   ;
    /**  调用函数GETSheeetStatus(int):这是Oracle中的函数
     *   参数：operateid(User 表中的ID)
     *   返回: employee表中的id
     */
    public int getSheetStatus(String tableName,int tableId) throws QueryException ;

    public QueryRequestImpl createRequest();
    public QueryRequestImpl createRequest(QuerySession session);
    
    public abstract List doQueryList(String sql, Object[] paramArrayOfObject, int paramInt, Connection conn)
    	    throws QueryException;
    
	public abstract JSONArray doQueryJSONArray(String sql,Object[] paramArrayOfObject, int paramInt,Connection conn)
			throws QueryException;

	public abstract int executeUpdate(String sql,Object[] paramArrayOfObject, Connection conn)
			throws QueryException;

	public abstract JSONObject doQueryObject(String sql,Object[] paramArrayOfObject, Connection conn,boolean toUpper)
			throws QueryException;

	public abstract JSONArray doQueryObjectArray(String sql,Object[] paramArrayOfObject, Connection conn,boolean toUpper) 
			throws QueryException;
}