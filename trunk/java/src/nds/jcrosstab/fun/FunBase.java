/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.jcrosstab.fun;
import java.text.*;
import java.util.*;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */
 
public abstract class FunBase extends FunUtil implements Fun{
	private static DecimalFormat DEFAULT_FORMATTER= new DecimalFormat("#0.00");
	protected DecimalFormat format=null; 
//	protected SetWrapper valueSet; 
	protected HashMap params; // key: name of param in upper case, value: param
	/**
	 * Used for function column display
	 */
	protected String description;

	public FunBase(){
		//valueSet=new SetWrapper();
		params=new HashMap();
	}
	
	/**
	 * Set function parameter for algorithm condition, some function will act in difference according to 
	 * different parameter
	 * @param param
	 * @param value
	 */
	public void addParameter(String name, String value){
		params.put(name.toUpperCase(), value);
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * 
	 * @return java.sql.Types.INTEGER or java.sql.Types.DOUBLE
	 */
	public int getReturnType(){
		return java.sql.Types.DOUBLE;
	}

	/**
	 * Set value formatter
	 * @param ft
	 */
	public void setValueFormatter(DecimalFormat ft){
		this.format=ft;
	}
	/**
	 * Format return value 
	 * @return
	 */ 
	public DecimalFormat getValueFormatter(){
		return format==null?DEFAULT_FORMATTER:format;
	}
	 
}
