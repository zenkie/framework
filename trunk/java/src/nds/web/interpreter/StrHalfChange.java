package nds.web.interpreter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.ColumnInterpretException;
import nds.util.ColumnInterpreter;
import nds.util.StringUtils;

/**
 *  È«½Ç×ª°ë½Ç·­ÒëÆ÷
 */
public class StrHalfChange implements ColumnInterpreter,java.io.Serializable {
	private static Logger logger= LoggerManager.getInstance().getLogger(StrHalfChange.class.getName());

  //  private String value;
  //	private Locale locale;
  	
	
    public StrHalfChange() {}
  	
	public String parseValue(Object val,Locale locale) {
		return (String) val;
	}
	
    public Object getValue(String str,Locale locale) {
     
    	
		//return value;
    	 throw new Error("Not supported");
    }

	@Override
	public String changeValue(String str, Locale locale)
			throws ColumnInterpretException {
		// TODO Auto-generated method stub
		String value = StringUtils.full2HalfChange(str);
		return value;
	}
}
