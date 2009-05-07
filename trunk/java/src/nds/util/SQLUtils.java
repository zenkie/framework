package nds.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUtils {
    public SQLUtils() {
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
