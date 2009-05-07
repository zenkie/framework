package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.query.QueryUtils;
import nds.schema.Column;
import nds.schema.SQLTypes;
import nds.schema.Table;
import nds.util.NDSException;
import nds.util.Tools;

/**
 * 
 * @deprecated
 * @author yfzhu@agilecontrol.com
 */
public class DefaultColumnObtain extends ColumnObtain{
//  public int length;
  public DefaultColumnObtain() {
//      this.length = length;
  }
  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
//      int length = this.getLength();
      String value = col.getDefaultValue();
      Object returnValue = null;
      Vector vec = new Vector();
      switch (col.getType()){
      case Column.NUMBER:
      		returnValue = new BigDecimal(Tools.getInt(value,-1)) ;
      		break;
      case Column.STRING:
	        returnValue = value.trim();
	      	break;
      case Column.DATENUMBER:
      		returnValue =QueryUtils.paseInputDateNumber(value,col.isNullable());
      		break;
      case Column.DATE:
	      	returnValue =QueryUtils.parseInputDate(value,col.isNullable(), col.getSQLType() );
	      	break;
      default:
          throw new NDSEventException("数据类型错误, 对应的列名为 "+col.getName());
      }
      Object[] returnArray = new Object[length];
      for(int i = 0;i<length;i++){
          returnArray[i] = returnValue;
      }

      vec.addElement(returnArray) ;

      return vec;
  }
/*
  public int getLength(){
      return length;
  }

  */
}