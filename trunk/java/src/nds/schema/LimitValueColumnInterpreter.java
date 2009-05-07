/******************************************************************
*
*$RCSfile: LimitValueColumnInterpreter.java,v $ $Revision: 1.3 $ $Author: Administrator $ $Date: 2005/12/18 14:06:17 $
*
*$Log: LimitValueColumnInterpreter.java,v $
*Revision 1.3  2005/12/18 14:06:17  Administrator
*no message
*
*Revision 1.2  2005/06/16 10:19:20  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
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

import java.util.Enumeration;
import java.util.*;

import nds.util.ColumnInterpretException;
import nds.util.*;
import nds.util.StringHashtable;
import nds.util.Tools;
/**
 * Parse limit value of column, the column should has method <br>
 * Column.isValueLimited()=true
 *
 */
public class LimitValueColumnInterpreter implements nds.util.ColumnInterpreter, java.io.Serializable {
    
	
	private Column column;
    public LimitValueColumnInterpreter(Column col)  {
        column=col;
    }
    /**parse specified value to string that can be easily interpreted by users
     * @param value must be Integer or String(representing a int value)
     * @throws ColumnInterpretException if input value is not valid
     */
    public String parseValue(Object value, Locale locale) throws ColumnInterpretException {
    	if( value !=null && value instanceof String) value=( (String)value).trim();
    	Object s=(Object)column.getValues(locale).get(value);
        if( s ==null)
            throw new ColumnInterpretException("Invalid value:"+value+", legal values are:"+ column.getValues(locale));
        return s.toString();
    }
    /**
    * parse input string to column accepted int value
    * @throws ColumnInterpretException if input string is not valid
    */
    public Object getValue(String str, Locale locale) throws ColumnInterpretException {
    	if(str !=null) str= str.trim();
    	try {
        	Object o=column.getValues(locale).getKey(str);
        	if( o==null) throw new ColumnInterpretException("Can not parse '"+str+
        			"' to a valid LimitValue, legal values are:"+column.getValues(locale));
            return o;
        } catch(Exception e) {
            throw new ColumnInterpretException("Can not parse '"+str+
            		"' to a valid LimitValue, legal values are:"+column.getValues(locale));
        }
    }

}
