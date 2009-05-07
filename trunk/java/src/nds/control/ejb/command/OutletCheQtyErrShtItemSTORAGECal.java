package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class OutletCheQtyErrShtItemSTORAGECal extends ColumnObtain{

  public OutletCheQtyErrShtItemSTORAGECal() {
  }

  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{

      Vector vec = new Vector();
      BigDecimal[] storage = Pub.getStorage(event,length);
      vec.add(storage) ;
      return vec;
  }
}