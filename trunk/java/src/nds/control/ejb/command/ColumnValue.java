package nds.control.ejb.command;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import nds.control.event.DefaultWebEvent;
import nds.schema.Table;
import nds.util.NDSException;

/**
 * @author yfzhu
 * @version 1.0
 */

public interface ColumnValue extends Serializable{

    /**get the values of the column, every column is in the vector
     * @parameter: list  代表要操作的所有的列名
     * @length: the record to be operated
     * return: the key: column name, value: Vector
     */

    public HashMap getColumnHashMap(DefaultWebEvent event,Table table,ArrayList list,int length,Connection conn) throws NDSException,RemoteException;
//    public Vector getObjectId(String tableName);
    // 该函数主要是得到当前的操作行为类型:如新增、修改等等
    public void setActionType(String actionType);
    public String getActionType();

}