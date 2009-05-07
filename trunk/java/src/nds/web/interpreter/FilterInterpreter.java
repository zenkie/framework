
package nds.web.interpreter;
import java.util.Locale;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Filter;
import nds.util.*;
/**
 * 过滤器对象解析器，
 * @see nds.schema.Filter
 */
public class FilterInterpreter implements ColumnInterpreter,java.io.Serializable {
    private static Logger logger= LoggerManager.getInstance().getLogger(FilterInterpreter.class.getName());
	
    public FilterInterpreter() {}
    /** 
     * Show Filter object's description
     * @aram value String to be converted to filter
     * @throws ColumnInterpretException if input value is not valid
     */
    public String parseValue(Object value,Locale locale) {
    	try{
    		nds.schema.Filter f=new nds.schema.Filter((String)value);
    		return f.getDescription();
    	}catch(Throwable t){
    		logger.error("Fail to parse "+ value, t);
    		return "N/A";
    	}
    }
    /**
    * throw error
    */
    public Object getValue(String str,Locale locale) {
        throw new Error("Not supported");
    }
}
