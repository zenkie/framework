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
public class CustRecQtyErrAdjShtItemADJQTYCal extends ColumnObtain{
    public CustRecQtyErrAdjShtItemADJQTYCal(){

    }
    public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {
        String[] freightLossQty = event.getParameterValues("freightLossQty");
        int objectid  = Tools.getInt(event.getParameterValue("objectid",true),-1 ) ;
        logger.debug("the value of objectid is:"+objectid) ;
        String sql = "";
        logger.debug("the value of sql is:"+sql) ;
        int errQty = 0;
        BigDecimal[] storage = new BigDecimal[length];
        String[] itemidStr = event.getParameterValues("itemid");
        try{
              BigDecimal sto = null;
              for(int i = 0;i<length;i++){

                    sql = "select errqty from CustRecQtyErrAdjShtItem where id = "+new Integer(itemidStr[i]).intValue();
                    logger.debug("the value of sql is:"+sql) ;
                    QueryEngine engine2 = QueryEngine.getInstance() ;
                    ResultSet result = engine2.doQuery(sql) ;
                    if(result.next() ){
                        errQty = result.getInt(1) ;
                        logger.debug("The value of sto is:"+errQty);
                    }
                    storage[i] = new BigDecimal( new Integer(errQty - Tools.getBigDecimal(freightLossQty[i],true).intValue()).toString());
                }
            }catch(Exception e){
                throw new NDSEventException("error!");
            }
            Vector vec = new Vector();
            vec.add(storage);
            return vec;
  }
}