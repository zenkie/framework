package nds.control.ejb.command;

import java.math.BigDecimal;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
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

public class DisRequestShtItemREALAMTCal extends ColumnObtain {

  public DisRequestShtItemREALAMTCal() {
  }
  public Vector getColumnValue(DefaultWebEvent event, Table table, Column col, int length) throws java.rmi.RemoteException, nds.util.NDSException {
     String[] qty = event.getParameterValues("requestDisQty");
     String[] realUp = event.getParameterValues("realUp");
     BigDecimal[] bigVal= new BigDecimal[length ] ;
      String colName = col.getName();
      int sqlType = col.getType()  ;              // 得到该字段的类型
      Vector vec = new Vector();
      double realAmt;
      for(int i = 0;i<length;i++){
          if( realUp[i]  == null || realUp[i].trim().equals("") ){
              // yfzhu modified here at 2003-03-03 since realUp may not be set on screen
              // take it to 0 acoording to lishuyan's comment
              realAmt=0;
          }else{
             realAmt = (new Integer(qty[i]).intValue())*(new Double(realUp[i]).doubleValue());
          }
          bigVal[i]=Tools.getBigDecimal(new Double(realAmt).toString() ,true);
      }
      vec.add(bigVal) ;
      return vec;
  }
}