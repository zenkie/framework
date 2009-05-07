package nds.control.ejb.command;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.Tools;
/**
 * <p>Title: NDS Project</p>
 * <p>Description: San gao shui yuan, mu xiang ren jia</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: aic</p>
 * @author yfzhu
 * @version 1.0
 */

public class WhCheQtyErrShtItemSTORAGECal extends ColumnObtain {

  public WhCheQtyErrShtItemSTORAGECal() {
   // super();
  }
  public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {
        Vector vec = new Vector();
        String[] productNo = event.getParameterValues("product_no");
        String[] stockNo = event.getParameterValues("stock_no");
        int objectid  = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;
        logger.debug("the value of objectid is:"+objectid) ;

        String sql = "";
        String sql2 = "";
        int productId = -1;
        int stockId = -1;
        BigDecimal[] storage = new BigDecimal[length];
        String[] itemidStr = event.getParameterValues("itemid");
        try{
                for(int i = 0;i<length;i++){
                    sql2 = "select id from stock where no =  "+"'"+stockNo[i]+"'";
                    logger.debug("The value of sql2 is:"+sql2) ;
                    QueryEngine engine = QueryEngine.getInstance() ;
                    ResultSet set = engine.doQuery(sql2);
                    if(set.next() ){
                          stockId = set.getInt(1) ;

                    }
                    logger.debug("the value of stockid is:"+stockId) ;
                    logger.debug("Are u going to test?");
                    sql = "select id from product where no = "+"'"+productNo[i]+"'";

                    logger.debug("the value of sql is:"+sql) ;
                    QueryEngine engine2 = QueryEngine.getInstance() ;
                    ResultSet result = engine2.doQuery(sql) ;
                    if(result.next() ){
                        productId = result.getInt(1) ;
                        logger.debug("the value of productId is:"+productId) ;
                    }

                    sql = "select stockstorage from stockstorage where stockId = "+stockId+" and productid = "+productId;
                    logger.debug("the value of sql is:"+sql) ;
                    QueryEngine engine3 = QueryEngine.getInstance() ;
                    ResultSet res = engine3.doQuery(sql);
                    if(res.next() ){
                       storage[i] = res.getBigDecimal(1) ;
                    }else{
                       storage[i] = new BigDecimal(0);
                    }
                }
            }catch(Exception e){
                throw new NDSEventException("Error from WhCheQtyErrShtItemSTORAGECal "+e.getMessage());
            }
      vec.add(storage) ;
      return vec;
  }
}