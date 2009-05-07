package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.Vector;

import nds.control.check.ColumnCheckImpl;
import nds.control.event.DefaultWebEvent;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
import nds.util.StringUtils;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class PwdColumnObtain extends ColumnObtain{

  public PwdColumnObtain() {
  }

  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
      String colName = col.getName();
      int sqlType = col.getType()  ;              // 得到该字段的类型
      Vector vec = new Vector();
      String[] value = null;
      value = event.getParameterValues(colName);
      String[] result = new String[value.length ];
      ColumnCheckImpl checkImpl = new ColumnCheckImpl();
      for(int i= 0;i<value.length ;i++){
          try{
          checkImpl.isColumnValid(col,value[i]);
          /**
           * yfzhu 2005-04-07 to enable connection to Liferay Portal,
           * so we should get plain password and send to Liferay. 
           */
          result[i] =value[i];  //StringUtils.hash((String)value[i]);
          }catch(NDSException e){
              if ( this.isBestEffort){
                  this.setRowInvalid(i, col.getDescription(Locale.CHINA)+"错误："+e.getMessage() );
                  result[i]="";
              }else throw e;
          }
      }
      vec.add(result);
      return vec;
  }
}