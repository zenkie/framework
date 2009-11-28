package nds.control.ejb;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.math.BigDecimal;
import org.json.JSONArray;

import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.Tools;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
/**
 * Helper class for ObjectModify for constructing sql statement
 * @author yfzhu
 * @version 1.0
 */
public class ObjectModifyImpl{
	private static Logger logger=LoggerManager.getInstance().getLogger(ObjectModifyImpl.class.getName());
	//private HashMap hashColValue;
    private DefaultWebEvent event;
    private Table table;
    private int length;
    private  ArrayList modifiableColumns;
    /**
     * @param event
     * @param table
     * @param length how many records to be inserted, note hashColValue may contains invalid row, so the real rows to be inserted may be smaller than this
     */
    public ObjectModifyImpl(DefaultWebEvent event,Table table,int length) throws NDSException{
    	//this.hashColValue=hashColValue;
    	this.event= event;
    	this.table=table;
    	this.length = length;
    	prepareModifiableColumns();
    }
	public ArrayList getModifiableColumns(){
		return modifiableColumns;
	}
	/**
	 * Copied from ObjectModify
	    * If event contains column_masks, then will first get all columns that modifiable,
	    * then filter those not has any bit set in typer "column_masks".
	    *  
	    * So the columns is less or equal than Column.MASK_MODIFY_EDIT
	    * @param event
	    * @param table
	    * @return
	    * @throws Exception
	    */
	private void prepareModifiableColumns() throws NDSException{
		boolean isPartialUpdate=false;
		Object pu= event.getParameterValue("partial_update");
		try{
			if(pu!=null)
				isPartialUpdate=Boolean.parseBoolean(String.valueOf(pu));
		}catch(Throwable t){
			logger.error("fail to parse boolean:"+pu+":"+t);
		}
		if(isPartialUpdate){
			/**
			 * Only columns set in event will get updated, just like sql update "set" clause
			 * This used mainly for REST request
			 */
			modifiableColumns= table.getColumns(new int[]{Column.MASK_MODIFY_EDIT}, false);
			for(int i=modifiableColumns.size()-1;i>=0;i--){
				Column col= (Column)modifiableColumns.get(i);
				String colName=col.getName();
				if(col.getReferenceTable()!=null) colName+="__"+col.getReferenceTable().getAlternateKey().getName();
				boolean isInEvent=event.hasParameter(colName);
				
				if(!isInEvent) modifiableColumns.remove(i);
			}
		}else{
			Object masks= event.getParameterValue("column_masks"); // JSONArray
			if(masks!=null && masks instanceof JSONArray){
				logger.debug(masks.toString());
				
				int[] mas= new int[((JSONArray)masks).length()];
				for(int k=0;k< mas.length;k++){
					try{
						mas[k]=Tools.getInt(((JSONArray)masks).get(k) ,-1);
					}catch(Throwable t){
						throw new NDSException("Fail to parse array of "+ masks.toString(), t);
					}
				}
				
				modifiableColumns= table.getColumns(new int[]{Column.MASK_MODIFY_EDIT}, false);
				for(int i=modifiableColumns.size()-1;i>=0;i--){
					Column col= (Column)modifiableColumns.get(i);
					boolean isInMasks=false;
					for(int j=0;j<mas.length;j++){
						if( col.isMaskSet(mas[j])){
							isInMasks=true;
							break;
						}
					}
					if(!isInMasks) modifiableColumns.remove(i);
				}
			}else{
				modifiableColumns= table.getModifiableColumns(
		       		(event.getParameterValue("arrayItemSelecter")!=null? nds.schema.Column.QUERY_SUBLIST:Column.MODIFY));
				
			}
		}
		addCommonModifiableColumns();
	}	
	/**
     * Columns types for elements in {@link #getSQLData()}, the last column must be PK 
     * @return elements are Column.NUMBER, Column.DATENUMBER,column.STRING,column.DATE 
     */
    public int[] getColumnTypes(){
    	ArrayList cols =modifiableColumns;
    	ArrayList types=new ArrayList();
    	for(int i=0;i<cols.size();i++){
    		Column column = (Column)cols.get(i);
            types.add( new Integer(column.getType()));
    	}
    	// last one must be pk
    	types.add(new Integer(Column.NUMBER));
    	int[] ts= new int[types.size()];
    	for(int i=0;i< ts.length;i++) ts[i] = ((Integer)types.get(i)).intValue();
    	return ts;
    }		
    /**
     * 
     * @param hashColValue key: Column name in uppercase, value:List of values for that column to be inserted into db, element's type can be Decimal/String/Integer/Data
     * @return elements are List contains column value(type is set in repective position in #getColumnTypes, note 
     * the last column data is PK)
     * @throws NDSException
     * @throws RemoteException
     */    
    public ArrayList getSQLData(HashMap hashColValue, int objectId) throws NDSException,RemoteException{
    	//BigDecimal objectid =new BigDecimal( Tools.getInt(event.getParameterValue("id"),-1 ) );
        String[] itemidStr = event.getParameterValues("itemid");

        BigDecimal[] itemId = new BigDecimal[length];
        if((itemidStr!=null)&&(!"".equals("itemidStr") )){
            if(itemidStr.length !=length){
                throw new NDSEventException("the length of the paramter is wrong");
            }
            for(int i = 0;i<itemidStr.length ;i++){
                itemId[i] =new BigDecimal( Tools.getInt(itemidStr[i],-1) );
            }
        }
        String tableName = table.getRealTableName() ;
        Column column=null;
        String columnName = null;
        
        boolean bCheckStatus=( table.getColumn("status")!=null);
        ArrayList data=new ArrayList();
        ArrayList row;
        for(int i = 0;i<this.length;i++){
        	row=new ArrayList();
            for(int j=0;j< modifiableColumns.size();j++){
               column = (Column)modifiableColumns.get(j);
               columnName = column.getName();
               Vector value=null;Object[] valBig=null;
               try{
               	value= (Vector)hashColValue.get(columnName) ;
                valBig= (Object[])value.get(0);
               }catch(NullPointerException e){
               		logger.error("Error found for column " + column+":" + e);
					throw e;
               }
               row.add(valBig[i]);
        	}	
            // and PK
            if(itemidStr!=null)row.add(itemId[i]);
            else row.add(new BigDecimal(objectId));
            
        	data.add(row);
        }
        return data;
    }
    public String getPreparedStatementSQL(){
    	StringBuffer sql = new StringBuffer("UPDATE "+table.getRealTableName()+" SET ");
        for(int i=0;i<this.modifiableColumns.size();i++){
            Column column = (Column)modifiableColumns.get(i);
            String columnName = column.getName();
            sql.append(columnName).append("=");
            sql.append("?,");
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
	
  
  /**
   * Add columns "modifierid", "modifieddate" if arrayList not has them
   * and table has them
   *
   */
   private void addCommonModifiableColumns(){
   		boolean shouldAdd_ModifierId=true ;
   		boolean shouldAdd_ModifiedDate=true ;
   		Column col;
   		for(int i=0;i<this.modifiableColumns.size();i++){
   			col=(Column) modifiableColumns.get(i);
   			String name=col.getName().toUpperCase();
   			if ( "MODIFIERID".equals(name)) shouldAdd_ModifierId=false;
   			if ( "MODIFIEDDATE".equals(name)) shouldAdd_ModifiedDate=false;
   		}
   		if (shouldAdd_ModifierId ){
   			col= table.getColumn("MODIFIERID");
   			if (col!=null) modifiableColumns.add(col);
   		}
   		if (shouldAdd_ModifiedDate ){
   			col= table.getColumn("MODIFIEDDATE");
   			if (col!=null) modifiableColumns.add(col);
   		}
   }  
   /**
    * Add columns "modifierid", "modifieddate" if arrayList not has them
    * and table has them
    *@deprecated
    */
    public static void addCommonModifiableColumns(ArrayList arrayList, Table table){
    		boolean shouldAdd_ModifierId=true ;
    		boolean shouldAdd_ModifiedDate=true ;
    		Column col;
    		for(int i=0;i<arrayList.size();i++){
    			col=(Column) arrayList.get(i);
    			String name=col.getName().toUpperCase();
    			if ( "MODIFIERID".equals(name)) shouldAdd_ModifierId=false;
    			if ( "MODIFIEDDATE".equals(name)) shouldAdd_ModifiedDate=false;
    		}
    		if (shouldAdd_ModifierId ){
    			col= table.getColumn("MODIFIERID");
    			if (col!=null) arrayList.add(col);
    		}
    		if (shouldAdd_ModifiedDate ){
    			col= table.getColumn("MODIFIEDDATE");
    			if (col!=null) arrayList.add(col);
    		}
    }  
}