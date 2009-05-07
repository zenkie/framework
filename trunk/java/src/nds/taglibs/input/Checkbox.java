/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nds.taglibs.input;

import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import nds.util.StringUtils;
import nds.util.Tools;

/**
 * 
 * This class implements the &lt;input:checkbox&gt; tag, which presents an
 * &lt;input type="checkbox" ... /&gt; form element.
 * 
 * @version 0.90
 * @author Shawn Bayern
 * @author Lance Lavandowska
 * @author Karl von Randow
 */

public class Checkbox extends TagSupport {

    private String name; // name of the checkbox group

    private String value; // value of this particular button

    private String dVal; // our single default value

    private String[] dValArray; // our multiple default values

    private Map attributes; // attributes of the <input> element

    private String attributesText; // attributes of the <input> element as text

    private String beanId; // bean id to get default values from

    public void release() {
        super.release();
        name = null;
        dVal = null;
        dValArray = null;
        attributes = null;
        attributesText = null;
        beanId = null;
    }
    private void log(String s){
    	System.out.println("[Checkbox] " +s);
    }
    
    public int doStartTag() throws JspException {
        try {
            // sanity check
            if (name == null || name.equals(""))
                throw new JspTagException("invalid null or empty 'name'");

            // Store beanId in a local variable as we change it
            String beanId = this.beanId;

            // Get default beanId
            if (beanId == null) {
                beanId = Util.defaultFormBeanId(this);
            } else if (beanId.length() == 0) {
                // An empty beanId means, do not use any bean - not even default
                beanId = null;
            }

            // replace null value with "on"
            if (value == null)
                value = "on";

            // get what we need from the page
            ServletRequest req =new nds.control.web.NDSServletRequest( pageContext.getRequest());
            JspWriter out = pageContext.getOut();

            // construct a vector of default values
            Vector dVals = new Vector();
            if (dVal != null)
                dVals.add(dVal);
            if (dValArray != null)
                for (int i = 0; i < dValArray.length; i++)
                    if (dValArray[i] != null)
                        dVals.add(dValArray[i]);

            // start building up the tag
            out.print("<input type=\"checkbox\" ");
            out.print("name=\"" + Util.quote(name) + "\" ");
            out.print("value=\"" + Util.quote(value) + "\" ");

            // include any attributes we've got here
            Util.printAttributes(out, attributes);
            if (attributesText != null) {
                out.print(attributesText + " ");
            }
            //log("dVals:"+ Tools.toString( dVals.toArray()));
            /*
             * Check this box (no pun intended) against potentially multiple
             * selections. (No need for a hash table as in <select> because
             * we're doing this exactly once per tag. We COULD cache stuff
             * between tags, but I'm not sure that kind of extra performance
             * would ever be called for.) We first check if there is a bean
             * value associated with the checkbox, if there is we use it. Then
             * note that we only use the "defaults" if the request is ENTIRELY
             * empty; this is different from what we do with the other input
             * types, checking "defaults" when there's no value for the specific
             * field. This difference is the result of the underlying
             * inconsistency between checkboxes and everything else. To achieve
             * the default concept nicely with a bean, put the default as the
             * initial value of the property in the bean when it is constructed
             * rather than using the "default" attribute of the checkbox tag.
             */

            // First check beanValue, if available - then we don't worry about
            // the defaults or request values etc
            String[] beanValues = (beanId != null ? Util.beanPropertyValues(
                    pageContext.findAttribute(beanId), name) : null);
            if (beanValues != null) {
                for (int i = 0; i < beanValues.length; i++) {
                    if (beanValues[i] != null && beanValues[i].equals(value)) {
                        out.print("checked=\"checked\" ");
                        break;
                    }
                }
            }
            // No bean value, so check if the request is empty - and use
            // defaults if it is
            
            // yfzhu 2005-10-27 这里的原始想法是：只有当页面是初始构造的时候，才比较default值，其他时候应当比较
            // 从request里获取的输入，如果输入不存在，就表示 没有选中。
            // 原来的设计者认为，当request 不包含 parameter 的时候，就是页面初始构造，而如果是提交后返回的页面，上面
            // 就会有parameter。然而，在nds中，即使页面初始构造，就含有了parameter，例如 明细界面
            // 所以，在nds中无法判断何时是系统初始构造页面，而非出错返回。故需要在request 中增加 attribute "iserror"（boolean)
            // 当赋值为true的时候，确认为出错状态。具体的设置位置在 nds.control.web.MainServlet
            // 对于其他非mainservlet 输出的界面，如果发生错误，也应当设置此attribute
            else if (!req.getParameterNames().hasMoreElements() || 
            		(req.getParameterValues(name)==null && !Boolean.TRUE.equals(req.getAttribute("iserror"))) ) {
            	 
                if (dVals != null) {
                    for (int i = 0; i < dVals.size(); i++) {
                        if (dVals.get(i) == null
                                || !(dVals.get(i) instanceof String))
                            throw new JspTagException(
                                    "'default' array must only contain non-null "
                                            + "Strings");
                        if ((dVals.get(i)).equals(value)) {
                            out.print("checked=\"checked\" ");
                            break; // why go on?
                        }
                    }
                }
            } else {
                // Use values from the request
                String[] checked = req.getParameterValues(name);
                if (checked != null) {
                	//log(" checked !=null for , name="+  name + ", value="+ value + "checked="+ Tools.toString(checked));
                	/*if(value!=null){
                		int index =-1;
                		try{
                			index=Integer.parseInt((String)value);
                		}catch(Exception e){}
                		if (index >-1 && index < checked.length 
                				&& checked[index] != null && checked[index].equals(value)){
                			// value is index, so it 's checked, this is only valid
                			// for checkbox as multiple objects list selector
                			out.print("checked=\"checked\" ");
                		}
                	}*/
                    // use the request if it says anything
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i].equals(value)) {
                            out.print("checked=\"checked\" ");
                            break; // why go on?
                        }
                    }
                }
            }

            // end the tag
            out.print("/>");

        } catch (Exception ex) {
        	log(StringUtils.toString(ex));
            throw new JspTagException(ex.getMessage());
        }
        return SKIP_BODY;
    }

    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
    public String getName() {
        return name;
    }

    public void setName(String x) {
        name = x;
    }

    /**
     * Getter for property value.
     * 
     * @return Value of property value.
     */
    public String getValue() {
        return value;
    }

    public void setValue(String x) {
        value = x;
    }

    /**
     * Getter for property defaults.
     * 
     * @return Value of property defaults.
     */
    public String[] getDefaults() {
        return dValArray;
    }

    public void setDefaults(String[] x) {
        dValArray = x;
    }

    /**
     * Getter for property default.
     * 
     * @return Value of property default.
     */
    public String getDefault() {
        return dVal;
    }

    public void setDefault(String x) {
        dVal = x;
    }

    /**
     * Getter for property bean.
     * 
     * @return Value of property bean.
     */
    public String getBean() {
        return beanId;
    }

    public void setBean(String x) {
        beanId = x;
    }

    /**
     * Getter for property attributesText.
     * 
     * @return Value of property attributesText.
     */
    public String getAttributesText() {
        return attributesText;
    }

    public void setAttributesText(String x) {
        attributesText = x;
    }

    /**
     * Getter for property attributes.
     * 
     * @return Value of property attributes.
     */
    public Map getAttributes() {
        return attributes;
    }

    public void setAttributes(Map x) {
        attributes = x;
    }

}