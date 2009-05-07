/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nds.query.QueryResult;
import nds.schema.Table;
import nds.util.*;


/**
 * For web ui, will output as <input> 
 * @author yfzhu@agilecontrol.com
 */

public class Button {
    protected String name;        // name of the text field
    protected String value;
    protected Map attributes;     // attributes of the <input> element
    protected String type;// default to "button", others can be "submit","cancle"
    protected String attributesText; // attributes of the <input> element as text
    public Button(){}
    /**
     * Create button with "onclik" set to action
     * @param name
     * @param value
     * @param action
     */
    public Button(String name, String value, String action, String title){
    	this.name=name;
    	this.value=value;
    	attributes=new HashMap();
    	attributes.put("onclick",action);
    	if(title!=null)attributes.put("title",title);
    }
    public String toHREF(String cssClass){
    	StringBuffer sb=new StringBuffer();
    	String onclick= (String)attributes.get("onclick");
    	if(onclick!=null) onclick="javascript:"+ onclick;
    	else onclick="#";
    	
    	sb.append("<a ");
    	if(cssClass!=null) sb.append(" class=\"").append(cssClass).append("\" ");
    	sb.append("href=\"" + onclick+"\">").append(value).append("</a>");
    	
    	return sb.toString();
    	
    }
    public String toHTML() throws IOException{
    	StringBuffer sb=new StringBuffer();
    	sb.append("<input class='cbutton' type=\"").append(getType()).append("\" ")
			.append("name=\"").append(StringUtils.escapeHTMLTags(name)).append("\" ");
    	Util.printAttributes(sb, attributes);
    	if (attributesText != null) {
            sb.append(attributesText + " ");
        }
    	if(value !=null )sb.append("value=\"").append(value).append("\" ");
    	sb.append(">");
    	
    	return sb.toString();
    }
    
	/**
	 * @return Returns the attributesText.
	 */
	public String getAttributesText() {
		return attributesText;
	}
	/**
	 * @param attributesText The attributesText to set.
	 */
	public void setAttributesText(String attributesText) {
		this.attributesText = attributesText;
	}
	/**
	 * @return Returns the attributes.
	 */
	public Map getAttributes() {
		return attributes;
	}
	/**
	 * @param attributes The attributes to set.
	 */
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type==null?"button":type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * Check whether to display this button on specified object or not
	 */
	public boolean isValid(int objectId, Table mainTable, HttpServletRequest req) throws Exception{
		return true;
	}
    
}
