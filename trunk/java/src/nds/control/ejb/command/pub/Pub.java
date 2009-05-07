package nds.control.ejb.command.pub;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.*;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.query.QueryEngine;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.*;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public final class Pub {
  private static Logger logger;
    static {
         logger = nds.control.util.EJBUtils.getLogger(Pub.class.getName());
    }
  public static String getTableName2(DefaultWebEvent event) throws NDSException{
      try{
      int tableId = Tools.getInt(event.getParameterValue("table"),-1 ) ;

      TableManager manager = TableManager.getInstance() ;
      Table table = manager.getTable(tableId) ;
      return table.getName();
      }catch(Exception e){
          throw new NDSEventException("Error occur when invoke getTableName"+e.getMessage() );
      }


  }

  public static Table getTable2(DefaultWebEvent event) throws NDSException{
      try{
          int tableId = Tools.getInt(event.getParameterValue("table"),-1 ) ;
          TableManager manager = TableManager.getInstance() ;
          Table table = manager.getTable(tableId) ;
          return table;
      }catch(Exception e){
          throw new NDSEventException("Error occur when invoke getTableName"+e.getMessage() );
      }


  }
      // 根据表明称返回返回表对象
    public static Table getTable2(String tableName){
        TableManager manager = TableManager.getInstance();
        Table table = manager.getTable(tableName);
        return table;
    }
    // 出去原来String中最后的字符窜
    public static String removeLastString(String sql,String s){
        int length = sql.lastIndexOf(s);
        //nmdemo find bug
        return length>-1? sql.substring(0,length) : sql;
    }
    // the column value from outletcheqtyerrsht
    public static BigDecimal[] getStorage(DefaultWebEvent event,int length) throws NDSException{
        String[] productNo = event.getParameterValues("product_no");
        int objectid  = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;
        String sheet = (String)event.getParameterValue("sheet");
        String sql = "select customerid from outletcheqtyerrsht where id = "+objectid;
        int customerId = -1;
        BigDecimal[] storage = new BigDecimal[length];
        try{
            QueryEngine engine = QueryEngine.getInstance() ;
            ResultSet set = engine.doQuery(sql);
            if(set.next() ){
                customerId = set.getInt(1) ;

            }
            int productId = -1;
            for(int i = 0;i<length;i++){
                sql = "select id from product where no = "+"'"+productNo[i]+"'";
                QueryEngine engine2 = QueryEngine.getInstance() ;
                ResultSet result = engine2.doQuery(sql) ;
                if(result.next() ){
                    productId = result.getInt(1) ;
                }

                sql = "select outletstorage from outletstorage where customerid = "+customerId+" and productid = "+productId;
                QueryEngine engine3 = QueryEngine.getInstance() ;
                ResultSet res = engine3.doQuery(sql);
                if(res.next() ){
                   storage[i] = res.getBigDecimal(1) ;
                }else{
                   storage[i] = new BigDecimal(0);
                }
            }
        }catch(Exception e){
            throw new NDSEventException("error!");
        }
        return storage;
  }

  public static BigDecimal[] getCheStorage(DefaultWebEvent event,int length) throws NDSException{
        String[] cheStorage = event.getParameterValues("cheStorage");
        BigDecimal[] cheValue = new BigDecimal[length];
        for(int i= 0;i<length;i++){
            cheValue[i] = Tools.getBigDecimal(cheStorage[i],true);
        }
        return cheValue;

  }

  public static BigDecimal[] getStorageModify(DefaultWebEvent event) throws NDSException{
        String[] itemStr = event.getParameterValues("itemid");
        int objectid  = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;
        String sql = "select customerid from outletcheqtyerrsht where id = "+objectid;
        int customerId = -1;
        BigDecimal[] storage = new BigDecimal[itemStr.length];
        try{
            QueryEngine engine = QueryEngine.getInstance() ;
            ResultSet set = engine.doQuery(sql);
            if(set.next() ){
                customerId = set.getInt(1) ;

            }
            int productId = -1;
            for(int i = 0;i<itemStr.length;i++){
                sql = "select productid from outletcheqtyerrshtitem where id = "+Tools.getInt(itemStr[i],-1) ;
                QueryEngine engine2 = QueryEngine.getInstance() ;
                ResultSet result = engine2.doQuery(sql) ;
                if(result.next() ){
                    productId = result.getInt(1) ;
                }
                sql = "select outletstorage from outletstorage where customerid = "+customerId+" and productid = "+productId;
                QueryEngine engine3 = QueryEngine.getInstance() ;
                ResultSet res = engine3.doQuery(sql);
                if(res.next() ){
                   storage[i] = res.getBigDecimal(1) ;
                }else{
                   storage[i] = new BigDecimal(0);
                }
            }
        }catch(Exception e){
            throw new NDSEventException("error!");
        }
        return storage;
    }

    public static String getDoubleQuote(String dd){
      String nn=null;
      if(dd==null||"".equals(dd) ){
          return "";
      }
      for(int i =0;i<dd.length() ;i++){
          if(dd.charAt(i)=='\'') {
              nn+="''";
          }else{
              nn+= new Character(dd.charAt(i));
          }
      }
      return nn.substring(4,nn.length() );
    }
    /**
     * Get sql insert string with sqlText set
     * @param customerID then customer id in sql, if <0 that means Table.DISPATCH_ALL
     *         else Table.DISPATCH_SPEC
     * @param sqlText the sql string( mysql grammar)
     * @return the complete string that can create ExpData record
     */
    public static String getExpDataRecord(int shopgroupID, String sqlText ){
        /* yfzhu modified at 2003-07-20 for only shop group supported now*/
        sqlText= sqlText +";";
        return "insert into posexpdata (id, shopID, shopgroupid, sqlText) values ( seq_expdata.nextval, null, " +
                    shopgroupID +",'" + StringUtils.replace(sqlText,"'","''")+"')";

        //sqlText= sqlText +";";
        // return "insert into posexpdata (id, shopgroupid, sqlText) values ( seq_expdata.nextval, " +
        //            shopgroupID +",'" + StringUtils.replace(sqlText,"'","''")+"')";
    }
/*    public static String getExpDataRecordForShopGroup(int shopgroupID,  String sqlText ){
        sqlText= sqlText +";";
        return "insert into posexpdata (id, shopID, shopgroupid, sqlText) values ( seq_expdata.nextval, null, " +
                    shopgroupID +",'" + StringUtils.replace(sqlText,"'","''")+"')";
    }*/

}