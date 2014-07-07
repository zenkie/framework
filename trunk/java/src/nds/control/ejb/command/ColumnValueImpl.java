package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.*;
import java.util.*;

import nds.control.ejb.ColumnCalFactory;
import nds.control.ejb.DefaultWebEventHelper;
import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryUtils;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.*;
import java.math.*;

import nds.schema.*;
/**
 * @author yfzhu
 * @version 1.0
 */

public class ColumnValueImpl implements ColumnValue{
    private Logger logger;
    private String actionType;
    private HashMap invalidRows;
    private boolean isBestEffort;
    public ColumnValueImpl(){

        logger=LoggerManager.getInstance().getLogger(getClass().getName());
    }
    public void enableBestEffort(boolean b){
        isBestEffort=b;
        if(b)invalidRows = new HashMap();
    }
    /**
    *  if isBestEffort is not enabled, return null;
     * @return key: row number( start from 0) Integer
     *       value: String msg ( why that row is error)
     */
    public HashMap getInvalidRows(){
        return invalidRows;
    }
    /**
     * �� ColumnHashMap ��д�� event ���������Щwfc�ֶεĻ�ȡ�������Ҫ�����ֶε�ֵ��Ϊ�ο�����ʱ���Ե�event���ȡ��
     * ���úõ��������ֶε�ֵ
     * 
     * @param event
     * @param table
     * @param colList normally table.getAllColumns()
     * @param length �����ļ�¼�ĸ�����һ���������1����sheetitem��ʱ�Ǹ��ݾ����������
     * @return Key: Column.Name(column is table.getAllColumns() filtered columns
     *              whose obtain-manner is 'trigger' ),
     *         Value: Vector that contains only one element, which is String[],or BigDecimal[], or  java.sql.Date[]
     * @throws NDSException
     * @throws RemoteException
     */
  public HashMap getColumnHashMap(DefaultWebEvent event,Table table,ArrayList colList,int length, Connection conn) throws NDSException,RemoteException{
  	 TableManager manager=TableManager.getInstance(); 
     HashMap hasMsp = new HashMap();
     /*
      * �� ColumnHashMap ��д�� event ���������Щwfc�ֶεĻ�ȡ�������Ҫ�����ֶε�ֵ��Ϊ�ο�����ʱ���Ե�event���ȡ��
     * ���úõ��������ֶε�ֵ 2008-07-22
      */
     event.put("ColumnValueHashMap",hasMsp);
     logger.debug("ColumnValueHashMap  "+hasMsp);
     Boolean unionfk=(Boolean) (event.getParameterValue("unionfk")==null?event.getParameterValue("unionfk"):false);
     String tableName = table.getName() ;
     Vector value = new Vector();
     //֧��sheetNo�ֶΰ��������ֶε�������Ϊһ���֣����磺��Ʒ�����Ҫ�������࣬С����Ϣ����д��Ϊ��
     //@DIM1_ID;CODE@-@DIM2_ID;CODE@-$serial4
     //move sheetNo column to last position, so can fetch other columns' value as parameter
     //ArrayList ite=new ArrayList();
     Iterator ite = colList.iterator();
     PairTable fixedColumns=DefaultWebEventHelper.getFixedColumns(event);
     logger.debug("fixed columns=" + fixedColumns);
     int maxRecordCount=length;
     
     try{
         // handle fixed columns first before normal columns (���������, wfc�ֶ�Ҫ���ø�����ֶ�����������������id��ͨ��fixedcolumn�����ݣ� 
         for(Iterator it= fixedColumns.keys();it.hasNext();){
         	Integer key=((Integer)it.next()); 
         	
         	Column col=manager.getColumn(key.intValue());
         	int colType= col.getType();
         	Object fv =  fixedColumns.get(key);
         	Object v=null; Object[] ar;
            switch(colType){
    	    	case Column.STRING:
    	            v= fv.toString();
    	            ar=new String[maxRecordCount];
    	            break;
            	case  Column.NUMBER:
            		v= new BigDecimal(fv.toString());
            		ar=new BigDecimal[maxRecordCount];
    /*	        	if( col.getScale()==0){
    	        		v=new Integer(fv.toString());
    	        		ar=new Integer[maxRecordCount];
    	        	}else{
    	        		v= new BigDecimal(fv.toString());
    	        		ar=new BigDecimal[maxRecordCount];
    	        	}*/
    	        	break;
            	case Column.DATENUMBER:
    	        	v=QueryUtils.paseInputDateNumber(fv.toString(), col.isNullable());
    	        	ar=new BigDecimal[maxRecordCount];
    	        	break;	
            	case Column.DATE:
    	        	v= QueryUtils.parseInputDate(fv.toString(), col.isNullable(), col.getSQLType());
    	        	ar=new java.sql.Date[maxRecordCount];
    	        	break;
            	default:
            		throw new NDSEventException("Column type not supported:" + colType);    
            }
         	value= new Vector();
         	
         	for(int i=0;i<maxRecordCount;i++ ) 
         		ar[i]= v;
         	value.addElement(ar);
         	hasMsp.put(col.getName(),value);
         } 
         }catch(Exception e){
         	logger.error("�ڲ�����:������Fixed columns ʱ�����쳣:"+ e.getMessage(), e);
         	throw new NDSEventException("�ڲ�����:������Fixed columns ʱ�����쳣:"+ e.getMessage());
         }     
     /**
      * ����ȡ��ֵ
      */
     
             ArrayList ShtNolist = new ArrayList();
             Object localObject1 = null;
               Object localObject3;   
               
               
     while(ite.hasNext() ){
         Column column = (Column)ite.next();
         if( fixedColumns.get( new Integer(column.getId()))!=null){
         	logger.debug("skip fixed column:" + column+"" );
         	// will handle in the last part of the method, see below
         	continue;
         }
         String obtainManner = column.getObtainManner();
         String columnName = column.getName() ;
         ColumnObtain co=null;
         try{
         if(obtainManner.equals("trigger") ){
             continue;
         }else if(obtainManner.equals("pk") ){
             co = new PKColumnObtain();
             co.setConnection(conn);
             if(isBestEffort){
                co.enableBestEffort(isBestEffort);
                co.setInvalidRows(invalidRows) ;
             }
             value = co.getColumnValue(event,table,column,length);
             hasMsp.put(columnName,value);
             //localObject1 = column;
         }else if(obtainManner.equals("sheetNo") ){
            // co = new ShtNoColumnObtain();
        	 ShtNolist.add(column);
         }else{
               if(obtainManner.equals("byPage") || obtainManner.equals("select") ){
             co = new DirectColumnObtain();
         }else if(obtainManner.equals("password") ){
             co = new PwdColumnObtain();
         }else if(obtainManner.equals("object") ){
             co = new ObjectColumnObtain();
         }else if(obtainManner.equals("sysdate") ){
             co = new SysDateColumnObtain();
         }else if(obtainManner.equals("operate") ){
             co = new OperatColumnObtain();
         }else{
               throw new NDSEventException("�ڲ�����:ObtainManner ���ò���ȷ!");
               /*
                	else if(obtainManner.equals("creator") ){
                		co= new CreatorColumnObtain(); // deprecated one, it will call getempid() in db
                		}
                         */
         }
         
         co.setConnection(conn);
         if(isBestEffort){
            co.enableBestEffort(isBestEffort);
            co.setInvalidRows(invalidRows) ;
         }
         //suppot fk�������ʱ֧�ֶ�Ӧ��ak��id
         value = co.getColumnValue(event,table,column,length);
         
         hasMsp.put(columnName,value);
         /*if(value!=null && value.size()> 0){
         	if(maxRecordCount<((Object[])value.elementAt(0)).length)
         		maxRecordCount=((Object[])value.elementAt(0)).length;
         }*/
         }
         }catch(Exception e2){
             logger.error("�ڲ�����:������"+ column+ "("+obtainManner+")ʱ�����쳣:", e2);
             throw new NDSEventException("�ڲ�����:������"+ column.getDescription(event.getLocale())+ "ʱ�����쳣:"+ e2.getMessage());
         }
     }
     
     
     
     
		for (Iterator shtno = ShtNolist.iterator(); shtno.hasNext();) {
			localObject3 = (Column) shtno.next();
			try {
				ColumnObtain co = null;
				co = new ShtNoColumnObtain();
				co.setConnection(conn);

				if (isBestEffort) {
					co.enableBestEffort(isBestEffort);
					co.setInvalidRows(invalidRows);
				}
				value = co.getColumnValue(event, table, (Column) localObject3,
						length);
				hasMsp.put(((Column) localObject3).getName(), value);

			} catch (Exception e3) {
				logger.error("�ڲ�����:������" + localObject3 + "ʱ�����쳣:", e3);
				throw new NDSEventException("�ڲ�����:������"
						+ ((Column) localObject3).getDescription(event
								.getLocale()) + "ʱ�����쳣:" + e3.getMessage());
			}

		}
     
      
     
     
   return hasMsp;
  }
  

  public void setActionType(String actionType){
      this.actionType = actionType;

  }
  public String getActionType(){
      return this.actionType ;
  }
  private void debug(String s){
      if (1 !=1 )logger.debug(s);
  }

}