package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
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

public class OperatColumnObtain extends ColumnObtain{
//  int length;
  public OperatColumnObtain( ) {
//      this.length = length;
  }
  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
      int operateid = Tools.getInt(event.getParameterValue("operatorid") ,-1) ;
//      int length = this.getLength() ;
      Vector vec = new Vector();
      BigDecimal[] operateUser = new BigDecimal[length];
      for(int i = 0;i<length;i++){
          operateUser[i] = new BigDecimal(operateid);
      }
      vec.add(operateUser) ;
      return vec;
  }
/*  public int getLength(){
      return length;
  }

  */
}