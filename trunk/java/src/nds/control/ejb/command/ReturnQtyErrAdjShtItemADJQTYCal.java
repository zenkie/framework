package nds.control.ejb.command;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.NDSException;
import nds.util.Tools;
/**
 * <p>Title: NDS Project</p>
 * <p>Description: San gao shui yuan, mu xiang ren jia</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: aic</p>
 * @author yfzhu
 * @version 1.0
 */

public class ReturnQtyErrAdjShtItemADJQTYCal extends ColumnObtain {

  public ReturnQtyErrAdjShtItemADJQTYCal() {
  }
  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
     String[] itemid = event.getParameterValues("itemid");
     String[] freightLossQty = event.getParameterValues("freightLossQty");
     BigDecimal[] bigVal= new BigDecimal[length ] ;
      String colName = col.getName();
      int sqlType = col.getType()  ;              // 得到该字段的类型
      Vector vec = new Vector();
      try{
        for(int i = 0;i<length  ;i++){
          String sqlStr = "select ERRQTY  from returnqtyerradjshtitem where id= "+itemid[i];
          QueryEngine engine = QueryEngine.getInstance();
          ResultSet result = engine.doQuery(sqlStr);
          if(result.next()){
            int errQty = result.getInt(1);
            logger.debug("The value of the errqty is:"+errQty);
            int adjqty = errQty-new Integer(freightLossQty[i]).intValue() ;
            bigVal[i]=Tools.getBigDecimal(new Integer(adjqty).toString() ,true);
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
      /*
       Vector vec = new Vector();
      String[] miscitemId = event.getParameterValues("menuitem_no");
      String[] bigVal= new String[length ] ;
      try{
        for(int i = 0;i<length  ;i++){
              String sqlStr = "select miscitemid from menuitem where no='"+miscitemId[i]+"'";
              QueryEngine engine = QueryEngine.getInstance();
              ResultSet result = engine.doQuery(sqlStr);
              if(result.next() ){
                bigVal[i] = result.getString(1);
              }else{
                  throw new NDSEventException("The data in the menuitem is not right!");
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
      */
  }
}