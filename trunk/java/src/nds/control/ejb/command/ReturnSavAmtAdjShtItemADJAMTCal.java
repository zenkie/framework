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

public class ReturnSavAmtAdjShtItemADJAMTCal extends ColumnObtain {

  public ReturnSavAmtAdjShtItemADJAMTCal() {
  }
  public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {
     String[] itemid = event.getParameterValues("itemid");
     String[] realUp = event.getParameterValues("realUp");
     BigDecimal[] bigVal= new BigDecimal[length ] ;
      String colName = col.getName();
      int sqlType = col.getType()  ;              // 得到该字段的类型
      Vector vec = new Vector();
      try{
        for(int i = 0;i<length  ;i++){
          String sqlStr = "select RETURNLANDQTY  from ReturnSavAmtAdjShtItem where id= "+itemid[i];
          QueryEngine engine = QueryEngine.getInstance();
          ResultSet result = engine.doQuery(sqlStr);
          if(result.next()){
            int RETURNLANDQTY = result.getInt(1);
//            logger.debug("The value of the errqty is:"+RETURNLANDQTY);
            double adjAmt = RETURNLANDQTY*(new Double(realUp[i]).doubleValue());
            bigVal[i]=Tools.getBigDecimal(new Double(adjAmt).toString() ,true);
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