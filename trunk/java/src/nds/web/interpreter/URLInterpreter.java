
package nds.web.interpreter;
import java.util.Locale;
import nds.schema.TableManager;
import nds.util.*;
/**
 * 内容是链接url, 在web上显示为 <a href='$url'>url</a> 的形式 
 */
 
public class URLInterpreter implements ColumnInterpreter,java.io.Serializable {
    public URLInterpreter() {}
    /** Add <a href=''></a> to the value
     *  Set max display length to 60 (yfzhu 2005-05-07)
     * @throws ColumnInterpretException if input value is not valid
     */
    private String value;
  	private Locale locale;
    
  	public String parseValue(Object value,Locale locale) {
    	if(value==null) return "";
    	        if (this.locale == null) {
    		          this.locale = TableManager.getInstance().getDefaultLocale();
    		          this.value = MessagesHolder.getInstance().getMessage(locale, "click-to-open-attach");
    		         }
    		        if (this.locale.equals(locale))
    		          this.value = this.value;
    		         else {
    		          this.value = MessagesHolder.getInstance().getMessage(locale, "click-to-open-attach");
    		         }
        //return "<a href=\""+ value+"\">"+ StringUtils.shorten(value.toString(), 60,"..") +"</a>";
    		        return "<a href=\"" + value + "\">" + this.value + "</a>";
    }
    /**
    * Just the str
    */
    public Object getValue(String str,Locale locale) {
        return str;
    }
	@Override
	public String changeValue(String str, Locale locale)
			throws ColumnInterpretException {
		// TODO Auto-generated method stub
		return null;
	}
}
