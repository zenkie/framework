/******************************************************************
*
*$RCSfile: Dictionary.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/12/18 14:06:17 $
*
*$Log: Dictionary.java,v $
*Revision 1.3  2005/12/18 14:06:17  Administrator
*no message
*
*Revision 1.2  2005/11/16 02:57:21  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.2  2003/03/30 08:11:36  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.4  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.3  2001/11/14 23:31:20  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema;

import java.util.*;
import java.util.Iterator;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.ColumnInterpretException;
import nds.util.ColumnInterpreter;
import nds.util.NDSRuntimeException;
import nds.util.Validator;
/**
 * Converting column value to meaningful words for display, and vice versa
 */
class Dictionary implements java.io.Serializable {
    private Logger logger= LoggerManager.getInstance().getLogger(Dictionary.class.getName());

    Hashtable interpreters;//key :columnId(Integer) value: Interpreter

    Dictionary() {
        interpreters=new Hashtable();
    }
    public ColumnInterpreter getColumnInterpreter(int columnId){
    	return (ColumnInterpreter)interpreters.get(new Integer(columnId));
    }
    /**
     * Get column value by check data in sequence of value first, desc second
     * @param columnId
     * @param valueOrDesc
     * @param locale
     * @return null if input is neither value nor description of specified column
     * @since 3.0
     */
    public String getColumnValueByValueOrDesc(int columnId, String valueOrDesc, Locale locale) {
    	if(Validator.isNull( valueOrDesc)) return null;
    	ColumnInterpreter ci=(ColumnInterpreter)interpreters.get(new Integer(columnId));
    	if(ci==null) return valueOrDesc;
    	try{
    		ci.parseValue(valueOrDesc,locale);
    		return valueOrDesc;
    	}catch(ColumnInterpretException e) {
    		try{
    			return (ci.getValue(valueOrDesc,locale)).toString() ;
    		}catch(ColumnInterpretException e2) {
    			return null;
    		}
    	}
    }
    /**
     * @return description of value if column.getValue()!=null || column.getValueInterpeter() !=nll
     * else return ""+value;
     */
    public String getDescription(Column column, Object value, Locale locale) {
        return getDescription(column.getId(),value,locale);
    }
    
    /**
     * @return description of value if column.getValue()!=null || column.getValueInterpeter() !=nll
     * else return ""+value;
     */
    public String getDescription(int columnId, Object value, Locale locale) {
        if(value==null || value.toString().equals("")) return "";
        ColumnInterpreter ci=(ColumnInterpreter)interpreters.get(new Integer(columnId));
        if(ci!=null) {
            try {
                return ci.parseValue(value,locale);
            } catch(ColumnInterpretException e) {
            	logger.debug("error for "+ TableManager.getInstance().getColumn(columnId)+":"+ e.getMessage());
                return "N/A";
            }
        } else {
        	Column col=TableManager.getInstance().getColumn(columnId);
            if( col.getValues(locale)!=null) {
                logger.error("Error found in Dictionary, column "+
                             TableManager.getInstance().getColumn(columnId)+ " should has value interpreter");
            }
            // not consider button type, should not let button type column printable
            // no need to show error, just display empty string
            if( col.getDisplaySetting().getObjectType()== DisplaySetting.OBJ_BUTTON){
            	//throw new NDSRuntimeException("Button type column should not be taken as ColumnInterpreter, check mask setting of "+col);
            	return "";
            }
            return ""+value;
        }

    }
    /**
     * Get columns's db valid value ( int ) according to description, is useful when
     * user import data in batch from file (Excel)
     */
    public String getValue( Column col, String desc, Locale locale){
        ColumnInterpreter ci=(ColumnInterpreter)interpreters.get(new Integer(col.getId()));
        if( ci !=null){
            try{
                return (ci.getValue(desc,locale)).toString() ;
            }catch(Exception e){
            	logger.error("Could not parse " + desc + " of " + col+":" + e);
                return null;
            }
        }else{
            logger.error("Column "+ col + " should not be treated as limit value column to parse "+ desc);
            return null;
        }
    }
    public void init(TableManager manager) {
        interpreters.clear();
        for( Iterator it=manager.getAllTables().iterator();it.hasNext();) {
            for( Iterator cs=( (Table)it.next()).getAllColumns().iterator();cs.hasNext();) {
                Column c=(Column) cs.next();
                // skip button type column, 2005-10-26
                if( c.getDisplaySetting().getObjectType()== DisplaySetting.OBJ_BUTTON)continue;
                /**
                 * prepare intepreter for column
                 * two types of column should have interpreter:
                 * 1. column.isValueLimited() --> LimitValueColumnInterpreter
                 * 2. column.getValueInterpeter() --> ColumnInterpreter
                 */
                if( c.getValues(manager.getDefaultLocale()) !=null) {
                    //                    logger.debug("ColumnId:"+ c.getId()+", values:"+ c.getValues());
                    interpreters.put(new Integer(c.getId()), new LimitValueColumnInterpreter(c) );
                } else if( c.getValueInterpeter() !=null) {
                    try {
                        Object pre= Class.forName( c.getValueInterpeter() ).newInstance();
                        // interpreter can be ButtonUIController/ColumnInterpreter/ColumnAlerter
                        // and here we only handle ColumnInterpreter
                        if(pre instanceof ColumnInterpreter )interpreters.put( new Integer(c.getId()), pre);
                    } catch(Exception e) {
                        logger.error("Internal Error: Could not instantiate interpreter "+
                                     c.getValueInterpeter()+" of column "+ c, e);
                    }

                }
            }
        }
    }
}
