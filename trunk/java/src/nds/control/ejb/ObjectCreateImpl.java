package nds.control.ejb;
import java.rmi.RemoteException;
import java.util.*;

import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.schema.*;

import nds.util.JNDINames;
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
     * UPDATE TABLE SET COL1=?, COL2=?,MODIFERID=?,MODIFIEDDATE=SYSDATE WHERE ID=?
     * AND 用户允许修改
     * 
     * 更新的字段必须是对象新增可以填写，且修改时也可以填写的字段，ID 总是在最后，如果有MODIFERID，在倒数第2
     */
   public String getPreparedStatementSQLForUpdate(){
	   ArrayList modifiableColumns=new ArrayList();
	   ArrayList al=table.getAllColumns();
	   for(int i=0;i<al.size();i++){
		   Column col=(Column)al.get(i);
		   if(col.isMaskSet(Column.MASK_CREATE_EDIT ) && col.isMaskSet(Column.MASK_MODIFY_EDIT)){
			   modifiableColumns.add(col);
		   }
	   }
	   
	   StringBuffer sql = new StringBuffer("UPDATE "+table.getRealTableName()+" SET ");
       for(int i=0;i<modifiableColumns.size();i++){
           Column column = (Column)modifiableColumns.get(i);
           String columnName = column.getName();
           sql.append(columnName).append("=");
           sql.append("?,");
       }
       if(shouldAddModifierIdToUpdateStatement()){
           sql.append("MODIFIERID=?,");
       }
       Column col= table.getColumn("MODIFIEDDATE");
       if(col!=null && !( col.isMaskSet(Column.MASK_CREATE_EDIT ) && col.isMaskSet(Column.MASK_MODIFY_EDIT))){
           String columnName = col.getName();
           sql.append(columnName).append("=");
           sql.append("sysdate,");
       }
       
       sql.deleteCharAt(sql.length()-1);
      sql.append(" WHERE ").append(table.getPrimaryKey().getName()).append("=?");
      boolean bCheckStatus=( table.getColumn("status")!=null);
      if(bCheckStatus){
   	   sql.append(" AND STATUS<>").append(JNDINames.STATUS_SUBMIT);
      }
      String psql= sql.toString();
      return psql;
   }    
   public boolean shouldAddModifierIdToUpdateStatement(){
	   Column col= table.getColumn("MODIFIERID");
       return (col!=null && !( col.isMaskSet(Column.MASK_CREATE_EDIT ) && col.isMaskSet(Column.MASK_MODIFY_EDIT)));
   }
   /**
    * Not include MODIFIERID AND ID
    * @return
    */
   public int[] getSQLDataColumnTypesForUpdate(){
	   ArrayList types=new ArrayList();
	   ArrayList al=table.getAllColumns();
	   for(int i=0;i<al.size();i++){
		   Column col=(Column)al.get(i);
		   if(col.isMaskSet(Column.MASK_CREATE_EDIT ) && col.isMaskSet(Column.MASK_MODIFY_EDIT)){
			   types.add( new Integer(col.getType()));
		   }
	   }
       int[] ts= new int[types.size()];
  		for(int i=0;i< ts.length;i++) ts[i] = ((Integer)types.get(i)).intValue();
  		return ts;
   }
   /**
    * UPDATE TABLE SET COL1=?, COL2=?,MODIFERID=?,MODIFIEDDATE=SYSDATE WHERE ID=?
     * AND 用户允许修改, 
     * Not include MODIFIERID AND ID
    * @return
    */
   public int[] getSQLDataIndexForUpdate(){
	   ArrayList modifiableColumns=new ArrayList();
	   ArrayList al=table.getAllColumns();
	   for(int i=0;i<al.size();i++){
		   Column col=(Column)al.get(i);
		   if(col.isMaskSet(Column.MASK_CREATE_EDIT ) && col.isMaskSet(Column.MASK_MODIFY_EDIT)){
			   modifiableColumns.add(col);
		   }
	   }

       Column column;
       ArrayList editColumnList = table.getAllColumns() ;
       ArrayList idxs=new ArrayList();
       
       for(int i=0;i< modifiableColumns.size();i++){
    	   Column udxCol= (Column) modifiableColumns.get(i);
    	   int idx=0;
	       for(int j=0;j<editColumnList.size();j++ ){
	          column = (Column)editColumnList.get(j);
	          if(column.getObtainManner().equals("trigger") )continue;
	          if(column.equals(udxCol)) break;
	          idx++;
	      }
	      idxs.add(new Integer(idx)); 
       }
       int[] ts= new int[idxs.size()];
       for(int i=0;i< idxs.size();i++) ts[i] = ((Integer)idxs.get(i)).intValue();
       return ts;
   }   
    /**
     * 在 SELECT ID FROM TABLE WHERE UDX_COLUMN1=? AND UDX_COLUMN2=?
     */
   public String getPreparedStatementSQLForUdx(){
	   List cols= TableManager.getInstance().getUniqueIndexColumns(table);
	   StringBuffer sb=new StringBuffer("SELECT ");
	   sb.append(table.getPrimaryKey().getName()).append(" FROM ").append(table.getRealTableName())
	   .append(" ").append(table.getName()).append(" WHERE ");
	   
	   Column col=(Column)cols.get(0);
	   sb.append( col.getName() ).append("=?");
	   for(int i=1;i< cols.size();i++){
		   col=(Column)cols.get(i);
		   sb.append(" AND ").append( col.getName() ).append("=?");
	   }
	   return sb.toString();
   }
   
   /**
    * Columns types for elements in {@link #getSQLData(HashMap, DefaultWebEvent, Table, int)} 
    * @param table
    * @return elements are Column.NUMBER, Column.DATENUMBER,column.STRING,column.DATE 
    */
   public int[] getSQLDataColumnTypesForUdx(){
	   	List cols= TableManager.getInstance().getUniqueIndexColumns(table);
   		ArrayList types=new ArrayList();
   		for(int i=0;i<cols.size();i++){
   			Column column = (Column)cols.get(i);
   			types.add( new Integer(column.getType()));
   		}
   		int[] ts= new int[types.size()];
   		for(int i=0;i< ts.length;i++) ts[i] = ((Integer)types.get(i)).intValue();
   		return ts;
   }
   /**
    * 在 SELECT ID FROM TABLE WHERE UDX_COLUMN1=? AND UDX_COLUMN2=?
    * 中，要输入的？在sqlData 的哪个位置
    * @return
    */
   public int[] getSQLDataIndexForUdx(){
       List udxCols= TableManager.getInstance().getUniqueIndexColumns(table);
       Column column;
       ArrayList editColumnList = table.getAllColumns() ;
       ArrayList idxs=new ArrayList();
       
       for(int i=0;i< udxCols.size();i++){
    	   Column udxCol= (Column) udxCols.get(i);
    	   int idx=0;
	       for(int j=0;j<editColumnList.size();j++ ){
	          column = (Column)editColumnList.get(j);
	          if(column.getObtainManner().equals("trigger") )continue;
	          if(column.equals(udxCol)) break;
	          idx++;
	      }
	      idxs.add(new Integer(idx)); 
       }
       int[] ts= new int[idxs.size()];
       for(int i=0;i< idxs.size();i++) ts[i] = ((Integer)idxs.get(i)).intValue();
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
    /**
     * 
     * @return array with 3 elements, 
     * 0 - ID column position in PreparedStatement of getPreparedStatementSQL
     * 1 - M_ATTRBIUTESETINSTANCE_ID column
     * 2 - *QTY* column
     */
    public int[] getASIRelateColumnsPosInStatement(){
    	int[] positions=new int[]{-1,-1,-1};
    	int j=1; // setXXX in PreparedStatement using column index starting form 1
    	ArrayList cols = table.getAllColumns() ;
        for(int i=0;i<cols.size();i++){
            Column column = (Column)cols.get(i);
            String columnName = column.getName();
            if(column.getObtainManner().equals("trigger") ){
                continue;
            }
            if(columnName.equalsIgnoreCase("id")){
            	positions[0]=j;
            }else if(columnName.equalsIgnoreCase("m_attributesetinstance_id")){
            	positions[1]=j;
            }else if(column.getType()== Column.NUMBER &&  column.isMaskSet(Column.MASK_CREATE_EDIT) 
            		&& columnName.indexOf("QTY")>-1){
            	positions[2]=j;
            }
            j++;
        }
        return positions;
    }
    
}