package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
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

public class OutletCheQtyErrShtItemERRQTYCal extends ColumnObtain{
  public OutletCheQtyErrShtItemERRQTYCal() {
  }
  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
       // µ√µΩ≈Ãµ„ø‚¥Ê
       String[] cheStorage = event.getParameterValues("cheStorage");
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
                logger.debug("the value of sql is:"+sql) ;
                QueryEngine engine3 = QueryEngine.getInstance() ;
                ResultSet res = engine3.doQuery(sql);
                if(res.next() ){
                   storage[i] = new BigDecimal(Tools.getBigDecimal(cheStorage[i],true).doubleValue()-res.getBigDecimal(1).doubleValue());
                }else{
                   storage[i] = new BigDecimal(Tools.getBigDecimal(cheStorage[i],true).doubleValue());
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