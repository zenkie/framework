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

public class WhErrqty extends ColumnObtain {

  public WhErrqty() {
  }
  public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {
        String[] cheStorage = event.getParameterValues("cheStorage");
        int objectid  = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;
        logger.debug("the value of objectid is:"+objectid) ;
        String sql = "";
        logger.debug("the value of sql is:"+sql) ;
        int stockId = -1;
        BigDecimal[] storage = new BigDecimal[length];
        String[] itemidStr = event.getParameterValues("itemid");
        try{

                int productId = -1;
                BigDecimal sto = null;

                for(int i = 0;i<length;i++){

                    sql = "select productid,stockid,storage from whcheqtyerrshtitem where id = "+new Integer(itemidStr[i]).intValue();
                    logger.debug("the value of sql is:"+sql) ;
                    QueryEngine engine2 = QueryEngine.getInstance() ;
                    ResultSet result = engine2.doQuery(sql) ;
                    if(result.next() ){
                        productId = result.getInt(1) ;
                        stockId = result.getInt(2) ;
                        sto = result.getBigDecimal(3);
                        logger.debug("the value of productId is:"+productId) ;
                        logger.debug("The value of stockId is:"+stockId) ;
                        logger.debug("The value of sto is:"+sto.doubleValue());
                    }
                    storage[i] = new BigDecimal( Tools.getBigDecimal(cheStorage[i],true).doubleValue() - sto.doubleValue());

                   /* sql = "select stockstorage from stockstorage where stockid = "+stockId+" and productid = "+productId;
                    logger.debug("the value of sql is:"+sql) ;
                    QueryEngine engine3 = QueryEngine.getInstance() ;
                    ResultSet res = engine3.doQuery(sql);
                    if(res.next() ){
                       storage[i] = new BigDecimal(res.getBigDecimal(1).doubleValue()-Tools.getBigDecimal(cheStorage[i],true).doubleValue()) ;

                    }else{
                       storage[i] = new BigDecimal(0-Tools.getBigDecimal(cheStorage[i],true).doubleValue());
                    }
                    */
                }
            }catch(Exception e){
                throw new NDSEventException("error!");
            }
            Vector vec = new Vector();
            vec.add(storage);
            return vec;
  }
}