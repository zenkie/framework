package nds.control.ejb.command;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ShtNoVariables;
import nds.query.QueryEngine;
import nds.schema.Column;
import nds.schema.Table;
import nds.util.*;
import nds.velocity.VelocityUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
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
  /**
   * yfzhu 2009-11-10 if no is already set from event, will use that one just like DirectColumnObtain (byPage)
   * this specialy for REST
   */
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
      
     
      sheetNo=(String)engine.doQueryOne("select vformat from ad_sequence where ad_client_id=? and name=?", new Object[] { Integer.valueOf(clientId), col.getSequenceHead() }, getConnection());
   
      //系统识别当前格式是SQL的依据是：当前格式中含有“$nextval”字符串。否则都将作为VTL 格式来处理。
      int VTL=(sheetNo==null)||(sheetNo.contains("$nextval"))? 1 : 0;
      logger.debug("sheetNo="+sheetNo+" VTL="+ VTL);

      String[] value = event.getParameterValues(col.getName());

      String[] sheetNoSeq = new String[length];

      for(int i = 0;i<length;i++){
          if( this.isInvalidRow(i) ){
              sheetNoSeq[i]="Invalid";
          }else{
              try{
            	//check event data first, if found, retrieve directly yfzhu 2009-11-10
            	//this occurs when using rest interface (order no is input from ecshop  
            	if(value!=null && value.length>i && Validator.isNotNull(value[i])){
            		sheetNoSeq[i] = value[i];
            	}else{
            		if(VTL==1){
            			String resultCol =engine.getSheetNo(col.getSequenceHead(), clientId);
            			sheetNoSeq[i] = resultCol;
            		}else{
                		StringWriter localStringWriter = new StringWriter(); 
                		ShtNoVariables dd = new ShtNoVariables(col, event ,i,getConnection()); 
                		VelocityContext context = VelocityUtils.createContext();
                		context.put("obj", dd);
                		//vlelocity 模版解析输出
                		Velocity.evaluate(context, localStringWriter, ShtNoVariables.class.getName(), sheetNo);
                		sheetNo  = localStringWriter.getBuffer().toString(); 
                		logger.debug("evaluate sheetNo="+sheetNo);
                		sheetNo  = dd.replaceFlowNO(sheetNo );
            			sheetNoSeq[i] = sheetNo ;
            			}
		         
            	}
				} catch (Throwable e) {
					if (this.isBestEffort) {
						this.setRowInvalid(i, "在获取编号时出错：" + e);
						sheetNoSeq[i] = "Invalid";
					} else {
						if (e instanceof NDSException)
							throw ((NDSException) e);
						throw new NDSException(e.getMessage(), e);
					}
				}
          }
      }
      vec.add(sheetNoSeq) ;
      return vec;
  }

}