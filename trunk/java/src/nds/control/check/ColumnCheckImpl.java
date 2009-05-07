package nds.control.check;
import java.rmi.RemoteException;
import java.util.Locale;

import nds.control.event.NDSEventException;
import nds.schema.Column;
import nds.util.*;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class ColumnCheckImpl extends ColumnCheck{

  public ColumnCheckImpl() {
  }
  public void isColumnValid(Column col,String objStr) throws NDSException,RemoteException{
	  if(true)return;
	 /*
      boolean strBoolean = col.isNullable() ;
      String regExpression  = col.getRegExpression();
      String errorMessage = col.getErrorMessage() ;

      if(strBoolean==false){
          if(Validator.isNull(objStr) && Validator.isNull(col.getDefaultValue())){
              throw new NDSEventException("列'"+col.getDescription(Locale.CHINA)  +"'不能为空！" );
          }
          if(regExpression!=null){
                  ParseRegExpression parseReg = new ParseRegExpression();
                  parseReg.parseExpression(regExpression,objStr,errorMessage);
          }
      }else{
          if((objStr!=null)&&!"".equals(objStr)){
              if(regExpression!=null){
                  ParseRegExpression parseReg = new ParseRegExpression();
                  parseReg.parseExpression(regExpression,objStr,errorMessage);
              }

          }
      }*/
  }
}