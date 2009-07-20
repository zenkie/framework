/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.bean;

import java.util.*;
import java.net.*;

//import nds.portlet.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import nds.web.*;
/**
 * 
 * Get default buttons from factory, and construct special button
 * at free, do not put those button into factory.
 * @author yfzhu@agilecontrol.com
 */
public class ButtonFactory {
	/**
	 * Key: Locale, Value: ButtonFactory
	 */
	private static Hashtable instances=new Hashtable();
	/**
	 * Key: button name in upper case, value: Button
	 */
	private Hashtable buttons;
	
	private ButtonFactory(){
		buttons=new Hashtable();
	}
	
	public Button getButton(String name){
		return (Button)buttons.get(name.toUpperCase());
	}
	public void putButton(Button b){
		buttons.put( b.getName().toUpperCase(), b);
	}
	private void createButton(String name, String value, String action, String accessKey,String title){
		Button b=new Button();
		b.setName(name);
		b.setValue(value);
		HashMap map=new HashMap();
		map.put("onclick",action);
		if(title!=null)map.put("title",title);
		if(accessKey!=null) map.put("accessKey", accessKey);
		b.setAttributes(map);
		buttons.put(name.toUpperCase(), b);
	}
	private void createButton(String name, String value, String action, String accessKey){
		createButton(name, value, action, accessKey);
	}
	/**
	 * Will not cached
	 * @param name
	 * @param value
	 * @param action
	 * @return
	 */
	public Button newButtonInstance(String name, String value, String action){
		
		return newButtonInstance(name, value, action, null,null);
	}
	public Button newButtonInstance(String name, String value, String action, String accessKey){
		return newButtonInstance(name, value, action, accessKey,null);
	}
	public Button newButtonInstance(String name, String value, String action, String accessKey, String title){
		Button b=new Button();
		b.setName(name);
		b.setValue(value+(accessKey!=null?"("+accessKey+")": ""));
		HashMap map=new HashMap();
		map.put("onclick",action);
		if(title!=null)map.put("title",title);
		if(accessKey!=null) map.put("accessKey", accessKey);
		b.setAttributes(map);
		return b;
	}	
	/**
	 * Will not cached
	 * @param name
	 * @param context
	 * @return
	 */
	public Button newButtonInstance(String name,PageContext context){
		return newButtonInstance(name, LangUtil.get(context, "object."+name.toLowerCase()), 
				"javascript:do"+ name+"();", null);
	}	 
	public Button newButtonInstance(String name,PageContext context, String accessKey, String title){
		return newButtonInstance(name, LangUtil.get(context, "object."+name.toLowerCase())+"("+accessKey+")", 
				"javascript:do"+ name+"();", accessKey,title);
	}
	public Button newButtonInstance(String name,PageContext context, String accessKey){
		return newButtonInstance(name, LangUtil.get(context, "object."+name.toLowerCase())+"("+accessKey+")", 
				"javascript:do"+ name+"();", accessKey);
	}	
	/**
	 * If  name='print', value will be resouce of "object.print"
	 * @param name
	 * @param context
	 */
	private void createButton(String name,PageContext context, String accessKey){
		createButton(name, LangUtil.get(context, "object."+name.toLowerCase())+"("+accessKey+")", 
				"javascript:do"+ name+"();", accessKey, null);
	}
	private void createButton(String name,PageContext context, String accessKey,String title){
		createButton(name, LangUtil.get(context, "object."+name.toLowerCase())+"("+accessKey+")", 
				"javascript:do"+ name+"();", accessKey, LangUtil.get(context, title));
	}
	private void init(PageContext context){
		createButton("Add", context, "N");
		createButton("CreateAndAdd", context,"L");
		createButton("Create", context,"S");
		createButton("Modify", context, "S");
		createButton("Delete", context, "X");
		createButton("Submit", context, "G");
		createButton("SubmitPrint", context, "H");
		createButton("PrintSetting", context, "T");
		createButton("Print", context, "P");
		createButton("Refresh", context, "J");
		createButton("ListAdd", context, "C");
		createButton("ListDelete", context,"E");
		createButton("ListModify", context,"S");
		createButton("ListCreate", context,"S");
		createButton("ListSubmit", context, "G");
		createButton("ListPrint", context, "P");
		createButton("ListImport", context, "I");
		createButton("GoModifyPage", context, "M");
		createButton("GoViewPage", context, "V");
		createButton("NewObject", context, "N");
		createButton("CopyTo", context, "Y");
		createButton("CopyFrom", context,  "F");
		createButton("OtherViews", context, "W");
		createButton("Template", context, "T");
		createButton("SaveLine", context, "L", "object.saveline.title");
		createButton("Process", context, "O");
		createButton("DeleteLine", context, "E","object.deleteline.title");
		// audti
		createButton("Request", context, "U");
		createButton("Accept", context, "U");
		createButton("Reject", context, "R");
		createButton("Unsubmit", context, "U");
		// C will be used for Close
	}
	/**
	 * Get button factory according page context's locale
	 * Each locale has a button factory. 
	 * @param context
	 * @return
	 */
	public static ButtonFactory getInstance(PageContext context, Locale locale){
		ButtonFactory instance=(ButtonFactory) instances.get(locale);
		if(instance ==null) {
			instance=new ButtonFactory();
			instance.init(context);
			instances.put( locale, instance);
		}

		return instance;
	}
}
