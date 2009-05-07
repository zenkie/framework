package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.*;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class ShtNoColumnObtain extends ColumnObtain{
//  int length ;
  public ShtNoColumnObtain() {
//      super();
//      this.length = length;
  }
  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
  	  //logger.debug("==============="+ event.toDetailString());
      String tableName = table.getName();
//      int length = this.getLength();
      Vector vec = new Vector();
      QueryEngine engine = QueryEngine.getInstance();
      
      //logger.debug("########"+ event.getStringWithNoVariables("$AD_CLIENT_ID$"));
      int clientId= Tools.getInt(event.getStringWithNoVariables("$AD_CLIENT_ID$"), -1);
      logger.debug("clientId="+ clientId);

      String sheetNo = null;


      String[] sheetNoSeq = new String[length];

      for(int i = 0;i<length;i++){
          if( this.isInvalidRow(i) ){
              sheetNoSeq[i]="Invalid";
          }else{
              try{
              	/**
              	 * Change to get sequence according to sequence head(name)
              	 * @since 2.0
              	 */
	              String resultCol =engine.getSheetNo(col.getSequenceHead(), clientId);
	              sheetNoSeq[i] = resultCol;
	              logger.debug(col+":"+resultCol);
              }catch(NDSException e){
                  if( this.isBestEffort ){
                      this.setRowInvalid(i, "在获取编号时出错："+ e);
                      sheetNoSeq[i] = "Invalid";
                  }else throw e;
              }
          }
      }
      vec.add(sheetNoSeq) ;
      return vec;
  }
/*  public int getLength(){
      return length;
  }
  */
}