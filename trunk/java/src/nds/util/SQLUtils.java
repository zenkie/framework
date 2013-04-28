package nds.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class SQLUtils {
    public SQLUtils() {
    }
    
    /**

    2      * 获得PreparedStatement向数据库提交的SQL语句

    3      * @param sql

    4      * @param params

    5      * @return

    6      */

    public static String getPreparedSQL(String sql, Object[] params) {

    	//1 如果没有参数，说明是不是动态SQL语句

    	int paramNum = 0;

    	if (null != params)  paramNum = params.length;

    	if (1 > paramNum) return sql;

    	//2 如果有参数，则是动态SQL语句

    	StringBuffer returnSQL = new StringBuffer();

    	String[] subSQL = sql.split("\\?");

    	for (int i = 0; i < paramNum; i++) {

    		if (params[i] instanceof Date) {

    			returnSQL.append(subSQL[i]).append(" '").append(new Timestamp(
						((Date) params[i]).getTime())).append("' ");

    		} else {

    			returnSQL.append(subSQL[i]).append(" '").append(params[i]).append("' ");

    		}

    	}



    	if (subSQL.length > params.length) {

    		returnSQL.append(subSQL[subSQL.length - 1]);

    	}

    	return returnSQL.toString();

    }
    /**
     * Value stored in db is number
     * Return true when field value is not 0, false if is 0, if null , return defaultValue
     * If result is null, return defaultValue
     * @param rs ResultSet
     * @param columnIdx int
     * @param defaultValue boolean
     * @return boolean
     */
    public static boolean getBoolean(ResultSet rs, int columnIdx, boolean defaultValue) throws SQLException{
        int i= rs.getInt(columnIdx);
        if ( rs.wasNull() ) return defaultValue;
        return i==0;
    }
    /**
     * @param rs ResultSet
     * @param columnIdx int
     * @param defaultValue int
     * @throws SQLException
     * @return int return defaultValue if field is null, not 0 ( which is sql specified)
     */
    public static int getInt(ResultSet rs, int columnIdx, int defaultValue) throws SQLException{
    int i= rs.getInt(columnIdx);
    if ( rs.wasNull() ) return defaultValue;
    return i;
}

}
