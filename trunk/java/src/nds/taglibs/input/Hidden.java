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

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 * This class implements the &lt;input:hidden&gt; tag, which presents an
 * &lt;input type="hidden" ... /&gt; form element.
 * 
 * @version 0.90
 * @author Shawn Bayern
 * @author Lance Lavandowska
 * @author Karl von Randow
 */

public class Hidden extends TagSupport {

    private String name; // name of the hidden field

    private String dVal; // default value if none is found

    private Map attributes; // attributes of the <input> element

    private String attributesText; // attributes of the <input> element as text

    private String beanId; // bean id to get default values from

    public void release() {
        super.release();
        name = null;
        dVal = null;
        attributes = null;
        attributesText = null;
        beanId = null;
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

            // get what we need from the page
            ServletRequest req = new nds.control.web.NDSServletRequest( pageContext.getRequest());
            JspWriter out = pageContext.getOut();

            // start building up the tag
            out.print("<input type=\"hidden\" ");
            out.print("name=\"" + Util.quote(name) + "\" ");

            // include any attributes we've got here
            Util.printAttributes(out, attributes);
            if (attributesText != null) {
                out.print(attributesText + " ");
            }
            if(attributes!=null && attributes.get("ai")!=null)
            {
                String[] attr = req.getParameterValues(name);
                try{
                    int index = Integer.parseInt((String)attributes.get("ai"));
                        /*
                         * print out the value from the request if it's there, or
                         * use the default value if it's not
                         */
                        
                    	if (index >-1 && index < attr.length && attr[index] != null)
                            out.print("value=\""
                                + Util.quote(attr[index]) + "\" ");
                        else if (dVal != null)
                            out.print("value=\"" + Util.quote(dVal) + "\" ");
                        else
                            out.print("value=\"\" ");

                        // end the tag
                        out.print("/>");
                    return SKIP_BODY;
                }catch(Exception e){
                	
                }
            }
            /*
             * print out the value from the bean if it's there, or from the
             * request if it's there, or use the default value if it's not
             */
            String beanValue = (beanId != null ? Util.beanPropertyValue(
                    pageContext.findAttribute(beanId), name) : null);
            if (beanValue != null) {
                out.print("value=\"" + Util.quote(beanValue) + "\" ");
            } else if (req.getParameter(name) != null) {
                out.print("value=\"" + Util.quote(req.getParameter(name))
                        + "\" ");
            } else {
                if (dVal != null)
                    out.print("value=\"" + Util.quote(dVal) + "\" ");
                else
                    out.print("value=\"\" ");
            }
            // end the tag
            out.print("/>");

        } catch (Exception ex) {
        	ex.printStackTrace();
            throw new JspTagException(ex.getMessage());
        }
        return SKIP_BODY;
    }

    public void setName(String x) {
        name = x;
    }

    public void setAttributes(Map x) {
        attributes = x;
    }

    public void setAttributesText(String x) {
        attributesText = x;
    }

    public void setBean(String x) {
        beanId = x;
    }

    public void setDefault(String x) {
        dVal = x;
    }

    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for property default.
     * 
     * @return Value of property default.
     */
    public String getDefault() {
        return dVal;
    }

    /**
     * Getter for property bean.
     * 
     * @return Value of property bean.
     */
    public String getBean() {
        return beanId;
    }

    /**
     * Getter for property attributesText.
     * 
     * @return Value of property attributesText.
     */
    public String getAttributesText() {
        return attributesText;
    }

    /**
     * Getter for property attributes.
     * 
     * @return Value of property attributes.
     */
    public Map getAttributes() {
        return attributes;
    }
}