package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

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

public class SysDateColumnObtain extends ColumnObtain{
    //private final static DateFormat timeFormatter =new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  public SysDateColumnObtain() {
//      this.length = length;
  }
  /**
   * @return elements are java.sql.Date
   */
  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
	  java.sql.Date date = new java.sql.Date(System.currentTimeMillis() );
      Vector vec = new Vector();
      java.sql.Date[] sysDate = new java.sql.Date[length];
      for(int i = 0;i<length;i++){
           sysDate[i] = date;
      }
      vec.addElement(sysDate) ;
      return vec;
  }
/*
  public int getLength(){
      return length;
  }

  */
}