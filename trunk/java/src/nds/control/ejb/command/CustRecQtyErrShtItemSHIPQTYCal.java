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
 * <p>Description: DRP System</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Agile Control Technologies</p>
 * @author unascribed
 * @version 1.0
 */

public class CustRecQtyErrShtItemSHIPQTYCal extends ColumnObtain{

    public CustRecQtyErrShtItemSHIPQTYCal() {
    }
    public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {
        Vector vec = new Vector();
        String[] productNo = event.getParameterValues("product_no");

        int objectid  = Tools.getInt(event.getParameterValue("objectid",true),-1 ) ;
        logger.debug("the value of objectid is:"+objectid) ;

        String sql = "";

        int productId = -1;
        int shipQty = -1;
        BigDecimal[] storage = new BigDecimal[length];


        try{
                for(int i = 0;i<length;i++){
                    sql = "select shipqty from disshipshtitem where disshipshtid = "
                          +"(select disshipshtid from custrecqtyerrsht where id = "+objectid
                          +" ) and productid=(select id from product where no ="
                          +" '"+productNo[i]+"')";
                    logger.debug("The value of sql is:"+sql) ;
                    QueryEngine engine = QueryEngine.getInstance() ;
                    ResultSet set = engine.doQuery(sql);
                    if(set.next() ){
                          shipQty = set.getInt(1) ;

                    }else{
                          shipQty = 0;
                    }
                    logger.debug("the value of stockid is:"+shipQty) ;

                       storage[i] = new BigDecimal(new Integer(shipQty).toString()) ;

                }
            }catch(Exception e){
                throw new NDSEventException("Error from CustRecQtyErrShtItemSHIPQTYCal "+e.getMessage());
            }
      vec.add(storage) ;
      return vec;
  }
}