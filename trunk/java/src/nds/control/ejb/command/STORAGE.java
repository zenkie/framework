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

public class STORAGE extends ColumnObtain {

  public STORAGE() {
  }
  public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {

        int objectid  = Tools.getInt(event.getParameterValue("objectid"),-1 ) ;

        String sql = "select customerid from outletcheqtyerrsht where id = "+objectid;
        int customerId = -1;
        BigDecimal[] storage = new BigDecimal[length];
        String[] itemidStr = event.getParameterValues("itemid");
        QueryEngine engine = QueryEngine.getInstance() ;
        try{
                ResultSet set = engine.doQuery(sql);
                if(set.next() ){
                    customerId = set.getInt(1) ;

                }
                int productId = -1;

                for(int i = 0;i<length;i++){

                    sql = "select productid from outletcheqtyerrshtitem where id = "+new Integer(itemidStr[i]).intValue();
                    ResultSet result = engine.doQuery(sql) ;
                    if(result.next() ){
                        productId = result.getInt(1) ;
                        logger.debug("the value of productId is:"+productId) ;
                    }

                    sql = "select outletstorage from outletstorage where customerid = "+customerId+" and productid = "+productId;
                    logger.debug("the value of sql is:"+sql) ;
                    ResultSet res = engine.doQuery(sql);
                    if(res.next() ){
                       storage[i] = res.getBigDecimal(1) ;
                    }else{
                       storage[i] = new BigDecimal(0);
                    }
                }
            }catch(Exception e){
                throw new NDSEventException("error!");
            }
            Vector vec = new Vector();
            vec.add(storage);
            return vec;
  }
}