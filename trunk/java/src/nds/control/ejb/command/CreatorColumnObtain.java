package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
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

public class CreatorColumnObtain extends ColumnObtain{

  public CreatorColumnObtain() {
  }
   public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
      int operateid = Tools.getInt(event.getParameterValue("operatorid",true) ,-1) ;
      QueryEngine engine = QueryEngine.getInstance() ;
      int employeeid = engine.getEmployeeId(operateid) ;
      Vector vec = new Vector();
      BigDecimal[] operateUser = new BigDecimal[length];
      for(int i = 0;i<length;i++){
          operateUser[i] = new BigDecimal(employeeid);
      }
      vec.add(operateUser) ;
      return vec;
  }
}