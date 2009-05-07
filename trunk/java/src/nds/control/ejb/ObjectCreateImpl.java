package nds.control.ejb;
import java.rmi.RemoteException;
import java.util.*;

import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
import nds.log.LoggerManager;
import nds.query.*;
import nds.log.Logger;
import nds.log.LoggerManager;
/**
 * Helper class for ObjectCreate for constructing sql statement
 * @author yfzhu
 * @version 1.0
 */

public class ObjectCreateImpl{
	private static Logger logger=LoggerManager.getInstance().getLogger(ObjectCreateImpl.class.getName());
    private HashMap invalidRows=null;
    private ArrayList sqlIndex=new ArrayList(); // key: Integer
    private HashMap hashColValue;
    private DefaultWebEvent event;
    private Table table;
    private int length;
    /**
     * @param hashColValue key: Column name in uppercase, value:List of values for that column to be inserted into db, element's type can be Decimal/String/Integer/Data
     * @param event
     * @param table
     * @param length how many records to be inserted, note hashColValue may contains invalid row, so the real rows to be inserted may be smaller than this
     */
    public ObjectCreateImpl(HashMap hashColValue,DefaultWebEvent event,Table table,int length){
    	this.hashColValue=hashColValue;
    	this.event= event;
    	this.table=table;
    	this.length = length;
    }
    public void setInvalidRows(HashMap rows){
        invalidRows=rows;
    }
    /**
     * Since some rows are invalid, the vector returned by getSqlArray
     * may have new index of each element in original sheet, such as:
     *  sheet has rows (0, 1,2,3,4)
     *  while (2, 3) row is invalid, so the vector returned by getSqlArray\
     *  is (0,1,4)
     */
    public ArrayList getSQLIndex(){
        return sqlIndex;
    }
    /**
     * Columns types for elements in {@link #getSQLData(HashMap, DefaultWebEvent, Table, int)} 
     * @param table
     * @return elements are Column.NUMBER, Column.DATENUMBER,column.STRING,column.DATE 
     */
    public int[] getColumnTypes(){
    	ArrayList cols = table.getAllColumns() ;
    	ArrayList types=new ArrayList();
    	for(int i=0;i<cols.size();i++){
    		Column column = (Column)cols.get(i);
            if(column.getObtainManner().equals("trigger") ){
                continue;
            }    		
            types.add( new Integer(column.getType()));
    	}
    	int[] ts= new int[types.size()];
    	for(int i=0;i< ts.length;i++) ts[i] = ((Integer)types.get(i)).intValue();
    	return ts;
    }
   
    /**
     * 
     * @return elements are List contains column value(type is set in repective position in #getColumnTypes)
     * @throws NDSException
     * @throws RemoteException
     */    
    public ArrayList getSQLData() throws NDSException,RemoteException{
        String tableName = table.getRealTableName() ;
        Column column;
        String columnName = null;
        ArrayList editColumnList = table.getAllColumns() ;
        ArrayList data = new ArrayList();
        for(int i = 0;i<length;i++){
            if( invalidRows!=null && invalidRows.containsKey(new Integer(i))){
                continue;
            }
            sqlIndex.add(new Integer(i));
            Iterator colIte2 = editColumnList.iterator();
            ArrayList row=new ArrayList();
          	for(int j=0;j<editColumnList.size();j++ ){
               column = (Column)editColumnList.get(j);
               columnName = column.getName() ;
               if(column.getObtainManner().equals("trigger") )continue;
               Vector value = (Vector)hashColValue.get(columnName) ;
               //logger.debug(columnName+", value="+ value.size()+ ", i="+i);
           	   row.add(((Object[])value.get(0))[i]);
           }
           data.add(row);
       }
       return data;
    } 
    public String getPreparedStatementSQL(){
    	StringBuffer sql = new StringBuffer("INSERT INTO "+table.getRealTableName()+" (");
    	ArrayList cols = table.getAllColumns() ;
    	StringBuffer value=new StringBuffer();
        for(int i=0;i<cols.size();i++){
            Column column = (Column)cols.get(i);
            String columnName = column.getName();
            if(column.getObtainManner().equals("trigger") ){
                continue;
            }
            sql.append(columnName).append(",");
            value.append("?,");
        }
       sql.deleteCharAt(sql.length()-1);
       sql.append(") values (").append(value);
       sql.deleteCharAt(sql.length()-1);
       sql.append(")");
       String psql= sql.toString();
       return psql;
    }    
    
}