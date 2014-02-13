package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import nds.control.check.ColumnCheckImpl;
import nds.control.event.DefaultWebEvent;
import nds.schema.Column;
import nds.schema.DisplaySetting;
import nds.schema.SQLTypes;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.*;
import nds.query.*;
/**
 * Add default-value support in ver 2.0, and default value
 * can contains Session variables such as $C_BPartner_ID$
 */

public class DirectColumnObtain extends ColumnObtain{

   
public DirectColumnObtain() {
  }

  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
      String colName = col.getName();
      int sqlType = col.getType()  ;              // 得到该字段的类型
      Vector vec = new Vector();
      String[] value = null;
      String errorMessage = "";
      ColumnCheckImpl checkImpl = new ColumnCheckImpl();
      value = event.getParameterValues(colName);
      String valueOne;
   
      
      Object defaultValue=getDefaultValue(col, event);
      //Object[] result = null;
      switch (sqlType) {
         case Column.STRING:          // 字符型
          String[] result =new String[length ];
          // yfzhu 2006-7-1 when checkbox is modifiable on action page, checkbox's default value should be 'N', not default value set in column
          // as long as the input got, that should be "Y", no matter what char it is. ( the char from
          // web only means the previous value in db)
          if(col.getDisplaySetting().getObjectType()== DisplaySetting.OBJ_CHECK){
          		String cmd=(String)event.getParameterValue("command",true);
          		if(cmd!=null && (cmd.endsWith("Create") && col.isModifiable(Column.ADD) ||
          				(cmd.endsWith("Modify") && col.isModifiable(Column.MODIFY))))
         		defaultValue= "N";
          }
          // yfzhu 2007-05-17 the ui may allow user input description into the LimitValue column as data 
          if(col.isValueLimited() && value !=null){
          		TableManager manager=TableManager.getInstance();
          		int colId= col.getId();
          		Locale locale= event.getLocale();
          		for(int i= 0;i<length ;i++){
          			value[i]= manager.getColumnValueByValueOrDesc(colId, value[i], locale); // may be null if not valid
          		}
          }
          for(int i= 0;i<length ;i++){
            valueOne=( value==null?(String)defaultValue: Validator.isNull(value[i])?(String)defaultValue:value[i]);
            result[i] = this.getString(i, checkImpl,col,valueOne);
            if(col.isUpperCase() && result[i]!=null) result[i]= result[i].toUpperCase();
          }
          
          vec.add(result);
          break;
      case  Column.NUMBER:            // 数字型
          value = event.getParameterValues(colName);
          BigDecimal[] bigVal= new BigDecimal[length ] ;

          for(int i = 0;i<length  ;i++){
          	valueOne=( value==null?(String)defaultValue: Validator.isNull(value[i])?(String)defaultValue:value[i]);
              bigVal[i] = this.getBigDecimal(i, checkImpl,col,valueOne);
          }
          vec.add(bigVal) ;
          break;
      case  Column.DATENUMBER:            // 日期数字型
        value = event.getParameterValues(colName);
        BigDecimal[] dn= new BigDecimal[length ] ;

        for(int i = 0;i<length  ;i++){
        	valueOne=( value==null?(String)defaultValue: Validator.isNull(value[i])?(String)defaultValue:value[i]);
            dn[i] = this.getDateNumber(i, checkImpl,col,valueOne);
        }
        vec.add(dn) ;
        break;          
      case Column.DATE :             // 日期型
          value = event.getParameterValues(colName);
          /** nmdemo value may be null */
          //if( value==null) return vec;
          //--- above added by yfzhu 
          java.sql.Date[] date = new java.sql.Date[length ];

          for(int i = 0;i<length  ;i++){
          	valueOne=( value==null?(String)defaultValue: Validator.isNull(value[i])?(String)defaultValue:value[i]);  
          	//valueOne=( value==null?(String)null: value[i]);
                date[i] = this.getDate(i, checkImpl,col,valueOne);
          }
          vec.add(date) ;
          break;
       default:
       		logger.error("Unexpected column type:"+ sqlType+" when handling column:"+ col);
      }
      return vec;
  }
  private String getDefaultValue(Column col,DefaultWebEvent event){
 	String s=col.getDefaultValue();
  	if (s==null) return null;
  	 s=event.getStringWithNoVariables(s);
  	return s;
  	
  }
  /**
   * Fill default value to the array if exists the default value
   * @param os
   * @param col
   */
  /*private void fillDefault(Object[] os, Column col,DefaultWebEvent event){
  	String s=col.getDefaultValue();
  	if (s==null) return;
  	 s=event.getStringWithNoVariables(s);
  	Object df=null;
  	switch(col.getType()){
  		case Column.DATE :
  			try{
  				df=new java.sql.Date( ((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).parse(s).getTime());
  			}catch(Exception e){
  			}
  			break;
  		case Column.STRING:
  			df= s;
  			break;
  		case Column.NUMBER:
  			try{
  			df=new BigDecimal(s);
  			}catch(NumberFormatException  e){}
  			break;
  	}
  	if( df!=null){
  		for(int i=0;i<os.length;i++) os[i]=df;
  	}
	
  }*/
  
  /**
   * when col is LimitValue type, will check value for for description if it's not in value list
   * 增加字段翻译器支持
   */
  private String getString(int row, ColumnCheckImpl checkImpl, Column col, String valueOne)
   throws  NDSException,RemoteException{
      try{
      	checkImpl.isColumnValid(col,valueOne);
      	TableManager tm= TableManager.getInstance();
      	ColumnInterpreter ci=null;
      	String clsname=col.getValueInterpeter();
      	if(clsname!=null){
    	try{
    	 ci=(ColumnInterpreter) Class.forName(clsname).newInstance();
    	 if(ci!=null)valueOne=ci.changeValue(valueOne,tm.getDefaultLocale())==null?valueOne:ci.changeValue(valueOne,tm.getDefaultLocale());
    	} catch (Exception e) {
    		logger.debug("@column-not-support-changeValue@:"+ col.getValueInterpeter());
    	}
      	//tm.getColumnValueDescription(col.getId(), valueOne,tm.getDefaultLocale());
      	}
        return valueOne;
      }catch(NDSException e){
          if( this.isBestEffort ){
              this.setRowInvalid(row,col.getDescription(Locale.CHINA)+"错误："+ e.getMessage() );
              return valueOne;
          }else throw e;
      }
  }
  private BigDecimal getDateNumber(int row, ColumnCheckImpl checkImpl, Column col, String valueOne)
  throws  NDSException,RemoteException{
    try{
        checkImpl.isColumnValid(col,valueOne);
        BigDecimal b;
        // for datenumber type will check date
       	// input date could be yyyymmdd or yyyy/mm/dd
       	b= QueryUtils.paseInputDateNumber(valueOne, col.isNullable());
        return  b;
    }catch(NDSException e){
        if( this.isBestEffort ){
            this.setRowInvalid(row,col.getDescription(Locale.CHINA)+"错误："+e.getMessage() );
            return new BigDecimal( 0);
        }else throw e;
    }
}
  private BigDecimal getBigDecimal(int row, ColumnCheckImpl checkImpl, Column col, String valueOne)
     throws  NDSException,RemoteException{
        try{
            checkImpl.isColumnValid(col,valueOne);
            BigDecimal b;
            // for datenumber type will check date
           	b= Tools.getBigDecimal(valueOne,!col.isNullable());
            return  b;
        }catch(NDSException e){
            if( this.isBestEffort ){
                this.setRowInvalid(row,col.getDescription(Locale.CHINA)+"错误："+e.getMessage() );
                return new BigDecimal( 0);
            }else throw e;
        }
  }
  private java.sql.Date  getDate(int row, ColumnCheckImpl checkImpl, Column col, String valueOne)
      throws  NDSException,RemoteException{
         try{
         checkImpl.isColumnValid(col,valueOne);
         return QueryUtils.parseInputDate(valueOne,col.isNullable(), col.getSQLType() );
         }catch(NDSException e){
             if( this.isBestEffort ){
                 this.setRowInvalid(row,col.getDescription(Locale.CHINA)+"错误："+e.getMessage() );
                 return new java.sql.Date(System.currentTimeMillis());
             }else throw e;
         }
  }

}