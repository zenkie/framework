package nds.control.ejb.command;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
/**
 * <p>Title: NDS Project</p>
 * <p>Description: San gao shui yuan, mu xiang ren jia</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: aic</p>
 * @author yfzhu
 * @version 1.0
 */

public class OutletDaySaleShtItemRETAILUPCal extends ColumnObtain {

  public OutletDaySaleShtItemRETAILUPCal() {
  }
  public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {
    String[] productNo = event.getParameterValues("Product_No");
    BigDecimal[] bigVal= new BigDecimal[length ] ;
    String colName = col.getName();
    int sqlType = col.getType()  ;              // 得到该字段的类型
    Vector vec = new Vector();
      try{
        for(int i = 0;i<length  ;i++){
          String sqlStr = "select retailUp  from product where No= "+"'"+productNo[i]+"'";
          QueryEngine engine = QueryEngine.getInstance();
          ResultSet result = engine.doQuery(sqlStr);
          if(result.next()){
            bigVal[i] = result.getBigDecimal(1);
            logger.debug("The value of the retailUp is:"+bigVal[i].doubleValue());

          }
        }
      }catch(Exception e){
         if(e instanceof NDSEventException)
           throw new NDSEventException(e.getMessage() );
         logger.debug("error found",e);
         throw new NDSEventException("Error found",e);
      }
      vec.add(bigVal) ;
      return vec;

  }
}