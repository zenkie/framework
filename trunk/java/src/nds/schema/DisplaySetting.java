/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.*;
/**
 * The holder class of screen layout setting of one column field
 * That's used wnen constructing single-object form, such as when
 * create or modify one object.
 * 
 * Allowed properties are 
 * text(column)[charsPerRow],textarea(column,row)[charsPerRow],select(column), radio(column),check(column), hr, blank(column,row)
 * hr will take the whole column
 * column is for columns (max to 2 currently), default to 1
 * row defaults to 1
 * 
 * hr is horizonal row
 * blank means blank that filed with text
 * 
 * @author yfzhu@agilecontrol.com
 * @since 2.0
 */

public class DisplaySetting {
	private static Logger logger= LoggerManager.getInstance().getLogger(DisplaySetting.class.getName());

	/**
	 * Input box, with only one row
	 */
	public final static int OBJ_TEXT=1;
	/**
	 * Textarea for multiple lines
	 */
	public final static int OBJ_TEXTAREA=2;
	/**
	 * Dropdown box
	 */
	public final static int OBJ_SELECT=3;
	/**
	 * Radio box
	 */
	public final static int OBJ_RAIDO=4;
	/**
	 * Check box, only for Y/N limit-value column
	 */
	public final static int OBJ_CHECK=5;
	/**
	 * <hr>
	 */
	public final static int OBJ_HR=6;
	/**
	 * Blank field, can set row and columns to be blank
	 */
	public final static int OBJ_BLANK=7;
	
	/**
	 * A url link in page, with icon set as attachment(allow browse)
	 */
	public final static int OBJ_FILE=8;
	
	/**
	 * A url link in page, an image of limit size, with icon set as attachment(allow browse)
	 */
	public final static int OBJ_IMAGE=9;
	/**
	 * Button type
	 * 在Button type 的情况下，Column.ValueInterpretor 需要设置显示button的控制类，或处理button事件的执行类
	 * 具体说明请参见 nds.web.ButtonCommandUI
	 */
	public final static int OBJ_BUTTON=10;
	/**
	 * Clob type
	 */
	public final static int OBJ_CLOB=11;
	/**
	 * xml type, 一般保存在数据库字段类型为CLOB的对象里，主要针对过滤器字段
	 * 过滤器字段，必须定义如下：
类型（coltype）：CLOB
建立索引（ISINDEXED）：否
赋值方式(obtainmanner): 界面输入
输入校验(正则) （REGEXPRESSION）: json 格式的表达式
字段翻译器（INTERPRETER）：nds.web.interpreter.FilterInterpreter
显示控件(DISPLAYTYPE): xml
长度和宽度DISPLAYROWS和DISPLAYCOLS: 与textarea/text 相同，可多列，多行
	 */
	public final static int OBJ_XML=12;
	
	private static  Hashtable methods;
	static{
		methods=new Hashtable();
		methods.put("text", new Integer(OBJ_TEXT));
		methods.put("textarea", new Integer(OBJ_TEXTAREA));
		methods.put("select", new Integer(OBJ_SELECT));
		methods.put("radio", new Integer(OBJ_RAIDO));
		methods.put("check", new Integer(OBJ_CHECK));
		methods.put("hr", new Integer(OBJ_HR));
		methods.put("blank", new Integer(OBJ_BLANK));
		methods.put("file", new Integer(OBJ_FILE));
		methods.put("image", new Integer(OBJ_IMAGE));
		methods.put("button", new Integer(OBJ_BUTTON));
		methods.put("clob", new Integer(OBJ_CLOB));
		methods.put("xml", new Integer(OBJ_XML));
	}
	public String getObjectTypeString(){

		String s="";
		switch(objType){
			case OBJ_TEXT: s="text";break;
			case OBJ_TEXTAREA: s="textarea";break;
			case OBJ_SELECT: s="select";break;
			case OBJ_RAIDO: s="radio";break;
			case OBJ_CHECK: s="check";break;
			case OBJ_HR: s="hr";break;
			case OBJ_BLANK: s="blank";break;
			case OBJ_FILE : s="file";break;
			case OBJ_IMAGE : s="image";break;
			case OBJ_BUTTON: s="button";break;
			case OBJ_CLOB: s="clob";break;
			case OBJ_XML: s="xml";break;
		}
		return s;
	}
		
	/**
	 * Object Type  such as  OBJ_HR, OBJ_TEXT
	 */
	private int objType;
	private int rows;
	private int columns;
	private int charsPerRow;
	
	public DisplaySetting(String type){
		this(type, null);
	}
	/**
	 * @param col the Column 
	 * 
	 */
	public DisplaySetting(Column col){
		//parse from column
		this(null, col);
	}
	 /** 
	 * @param type if null, use default one from column instead.
	 * format:
	 * text(column),textarea(column,row),select(column),
	 * radio(column),check(column), hr, blank(column,row)
	 * 
	 */
	public DisplaySetting(String type, Column col){
		try{
		if (type==null){
			checkOnColumn(col);
			
		}else{
			
			int ps, comma, se;
	        String p,s;

//			parse
	        type=type.trim().toLowerCase();
	        int q= type.indexOf('(');
	        String grossType=null;
	        if( q != -1){
	            grossType=type.substring(0,q);
		        
		        
		        ps= type.indexOf('(')+1;// precision start
		        comma= type.indexOf(',');// comman pos
		        se=type.indexOf(')');// scale end
		        if( comma != -1){
		            p= type.substring(ps,comma);
		            s= type.substring(comma+1,se);
		            columns=(new Integer(p)).intValue();
		            rows=(new Integer(s)).intValue();
		        }else{
		            p= type.substring(ps, se);
		            columns=(new Integer(p)).intValue();
		            rows=1;
		        }
	        }else{
		        	grossType=type;
		        	columns=1;
		        	rows=1;
	        }
        	// charsPerRow
        	ps=type.indexOf('[')+1;
        	se=type.indexOf(']');// scale end
        	if( ps != 0){
        		charsPerRow=(new Integer(type.substring(ps,se))).intValue();
        	}else{
        		if(rows ==1 && columns==1)charsPerRow =20;
        		if(rows==1 && columns>1) charsPerRow=80;
        		if(rows>1 && columns>1) charsPerRow=60;
        	}
	        
	        Integer  t=(Integer) methods.get(grossType);
	        if( t ==null) throw new NDSRuntimeException(" Type \""+type+"\" not recognized by DisplaySetting");
	        objType= t.intValue();		
	        //if(columns>3) throw new NDSRuntimeException(" Column \""+col+"\" error in setting display columns(greater than 2):"+columns);
		}
		}catch(Throwable t){
			if(t instanceof NDSRuntimeException) throw (NDSRuntimeException)t;
			logger.error(" Type \""+type+"\" not recognized by DisplaySetting", t);
			throw new NDSRuntimeException(" Type \""+type+"\" not recognized by DisplaySetting");
		}
        
        		  	
	}
	/**
	 * When display type is OBJ_BLANK or OBJ_HR, will return true
	 * @return
	 */
	public boolean isUIController(){
		return (objType==OBJ_BLANK || objType==OBJ_HR);
	}
	
	/**
	 * @return Returns the charsPerRow.
	 */
	public int getCharsPerRow() {
		return charsPerRow;
	}
	private void checkOnColumn(Column col){
		columns= 1;
		rows=1;
		objType= OBJ_TEXT;
		charsPerRow=20;
		PairTable pt=col.getValues(TableManager.getInstance().getDefaultLocale());
		if( pt !=null){
			int cnt= pt.size();
			if(cnt==2 &&  pt.containsKey("Y") &&
					pt.containsKey("N")){
				objType= OBJ_CHECK;
			}else{
				objType=OBJ_SELECT;
			}
			return ;
		}
		switch(col.getType()){
			case Column.STRING:
				if( col.getLength()> 256) {
					objType= OBJ_TEXTAREA;
					rows=6;
					columns= 2;
					charsPerRow=60;
				}else if(col.getLength()> 128){
					columns= 2;
					charsPerRow=80;
				}
				break;
		}
	}
	/**
	 * 
	 * @return OBJ_HR, OBJ_TEXT, etc
	 */
	public int getObjectType(){
		return objType;
	}
	public int getRows(){
		return rows;
	}
	public int getColumns(){
		return columns;
	}
	public String toString(){
		return  getObjectTypeString()+"("+ columns+","+rows+")[" + charsPerRow+"]";
	}
	public static void main(String[] args){
		DisplaySetting ds=new DisplaySetting("textarea(2,2)[34]");
		System.out.println(ds);
	}
}
