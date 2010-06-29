package nds.control.ejb.command;

import java.math.BigDecimal;
import java.sql.ResultSet;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.log.Logger;
import nds.query.QueryEngine;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.Tools;
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
      TableManager manager = TableManager.getInstance() ;
      Table table = manager.findTable(event.getParameterValue("table")) ;
      return table.getName();
      }catch(Exception e){
          throw new NDSEventException("Error occur when invoke getTableName"+e.getMessage() );
      }


  }

  public static Table getTable2(DefaultWebEvent event) throws NDSException{
      return  TableManager.getInstance().findTable(event.getParameterValue("table"));
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
        return sql.substring(0,length) ;
    }
    // the column value from outletcheqtyerrsht
    public static BigDecimal[] getStorage(DefaultWebEvent event,int length) throws NDSException{
        String[] productNo = event.getParameterValues("product_no");
        int objectid  = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;

        String sql = "select customerid from outletcheqtyerrsht where id = "+objectid;
        int customerId = -1;
        BigDecimal[] storage = new BigDecimal[length];
        String[] itemidStr = event.getParameterValues("itemid");
        try{
                QueryEngine engine = QueryEngine.getInstance() ;
                ResultSet set = engine.doQuery(sql);
                if(set.next() ){
                    customerId = set.getInt(1) ;

                }
                int productId = -1;

                for(int i = 0;i<length;i++){
                    if(productNo!=null)
                      sql = "select id from product where no = "+"'"+productNo[i]+"'";
                    else
                      sql = "select productid from outletcheqtyerrshtitem where id = "+new Integer(itemidStr[i]).intValue();
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
}