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
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * 
 * This class implements the &lt;input:textarea&gt; tag, which presents a
 * &lt;textarea&gt; form element.
 * 
 * @version 0.90
 * @author Shawn Bayern
 * @author Lance Lavandowska
 * @author Karl von Randow
 */

public class TextArea extends BodyTagSupport {

    private String name; // name of the textarea

    private String dVal; // default value if none is found

    private Map attributes; // attributes of the <textarea> element

    private String attributesText; // attributes of the <input> element as text

    private String beanId; // bean id to get default values from

    private String cols, rows;

    public void release() {
        super.release();
        name = null;
        dVal = null;
        attributes = null;
        attributesText = null;
        beanId = null;
        cols = null;
        rows = null;
    }

    public int doEndTag() throws JspException {
        try {
            // sanity check
            if (name == null || name.equals(""))
                throw new JspTagException("invalid null or empty 'name'");

            // Store beanId in a local variable because we change it
            String beanId = this.beanId;

            // Get default beanId
            if (beanId == null) {
                beanId = Util.defaultFormBeanId(this);
            } else if (beanId.length() == 0) {
                // An empty beanId means, do not use any bean - not even default
                beanId = null;
            }

            // get what we need from the page
            ServletRequest req =new nds.control.web.NDSServletRequest( pageContext.getRequest());
            JspWriter out = pageContext.getOut();

            // start building up the tag
            out.print("<textarea name=\"" + Util.quote(name) + "\" ");

            // include any attributes we've got here
            Util.printAttributes(out, attributes);
            if (attributesText != null) {
                out.print(attributesText + " ");
            }

            if (cols != null) {
                out.print("cols=\"" + Util.quote(cols) + "\" ");
            }
            if (rows != null) {
                out.print("rows=\"" + Util.quote(rows) + "\" ");
            }

            // end the starting tag
            out.print(">");

            String defaultValue = dVal;
            if (getBodyContent() != null
                    && getBodyContent().getString() != null) {
                defaultValue = getBodyContent().getString();
            }

            /*
             * print out the value from the bean if it's there, or from the
             * request if it's there, or use the default value if it's not
             */
            String beanValue = (beanId != null ? Util.beanPropertyValue(
                    pageContext.findAttribute(beanId), name) : null);
            if (beanValue != null) {
                out.print(Util.quote(beanValue));
            } else if (req.getParameter(name) != null) {
                out.print(Util.quote(req.getParameter(name)));
            } else {
                if (defaultValue != null)
                    out.print(Util.quote(defaultValue));
            }

            // end the textarea
            out.print("</textarea>");

        } catch (Exception ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_PAGE;
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

    public void setCols(String cols) {
        this.cols = cols;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }
}