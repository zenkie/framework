package nds.control.ejb;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class MySQLObjectCreateImpl extends SqlGenerateSupport{
    private HashMap invalidRows=null;
    //key: row number (start from 0) values :String reason
    public void setInvalidRows(HashMap rows){
        invalidRows=rows;
    }

    // VectorÖÐ°üº¬sqlÓï¾ä
    public Vector getSqlArray(HashMap hashColValue,DefaultWebEvent event,Table table,int length) throws NDSException,RemoteException{
        /** -- yfzhu modified 2003-07-27 to support pos security update --*/
        //String tableName = table.getName() ;
        String tableName = table.getDispatchTableName() ;
        Column column;
        String columnName = null;

        //######### dispatch column id
        int dispatchColumnId= -1;
        if (table.getDispatchColumn() !=null) dispatchColumnId=table.getDispatchColumn().getId();

        ArrayList editColumnList = table.getAllColumns() ;
        Vector vec = new Vector();
        for(int i = 0;i<length;i++){
            if ( invalidRows !=null && invalidRows.containsKey(new Integer(i))) continue;
            Iterator colIte = editColumnList.iterator() ;
            Iterator colIte2 = editColumnList.iterator();
            String sql = "insert into "+tableName+" (";

            while(colIte.hasNext() ){
                column = (Column)colIte.next();
                columnName = column.getName();
                if(column.getObtainManner().equals("trigger") ){
                    continue;
                }
                sql+= columnName+",";
            }
           sql = Pub.removeLastString(sql,",");
           sql += ") values (";

           //########### dispath customer id ############
           int dispatchCustomerId= -1;

           while(colIte2.hasNext() ){
               column = (Column)colIte2.next();
               columnName = column.getName() ;
               if(column.getObtainManner().equals("trigger") )
                   continue;
               Vector value = (Vector)hashColValue.get(columnName) ;
               int colType = column.getType();

                   switch(colType){
                   case Column.NUMBER:
                   case Column.DATENUMBER:	
                       Object[] valBig = (Object[])value.get(0);
                       sql += valBig[i]+",";
                       // ########### dispatch column id
                       if ( column.getId() == dispatchColumnId) {
                           dispatchCustomerId=( new Integer(""+ valBig[i])).intValue() ;
                       }
                       break;
                   case Column.DATE:
                       Object[] valDate = (Object[])value.get(0) ;
                       if(valDate[i]==null)
                           sql +="null,";
                       else
                           // this is the only difference to Oracle
                           sql += "'"+valDate[i]+"',";
                       break;
                   case Column.STRING:
                       Object[] valStr = (Object[])value.get(0) ;
                       sql += "'"+Pub.getDoubleQuote((String) valStr[i])+"',";
                       break;
                   default:
                   		throw new NDSException("Unexpected column type:"+colType + " for column :"+column);
                   }
           }
           sql = Pub.removeLastString(sql,",");
           sql += ")";

           if ( table.getDispatchType()== table.DISPATCH_ALL  ){
               vec.addElement(Pub.getExpDataRecord(-1, sql));
           }else if ( table.getDispatchType()== table.DISPATCH_SPEC   ){
               vec.addElement(Pub.getExpDataRecord(dispatchCustomerId, sql));
           }
       }
       return vec;
    }
}