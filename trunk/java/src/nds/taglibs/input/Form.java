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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 * This class implements the &lt;input:form&gt; tag, which presents an &lt;form
 * ... /&gt; element.
 * 
 * @version 0.90
 * @author Shawn Bayern
 * @author Lance Lavandowska
 * @author Karl von Randow
 */

public class Form extends TagSupport {

    private String name; // name of the form

    private String action; // form action

    private Map attributes; // attributes of the <input> element

    private String attributesText; // attributes of the <input> element as text

    private String beanId; // bean id to get default values from

    private String method; // form method

    private String encType; // form encType

    public void release() {
        super.release();
        name = null;
        action = null;
        attributes = null;
        attributesText = null;
        beanId = null;
        method = null;
        encType = null;
    }

    public int doStartTag() throws JspException {
        try {
            // get what we need from the page
            JspWriter out = pageContext.getOut();

            // start building up the tag
            out.print("<form method=\"");
            out.print(method != null ? method : "get");
            out.print("\" action=\"");
            if (action != null) {
                if (action.length() > 0
                        && pageContext.getResponse() instanceof HttpServletResponse) {
                    out.print(((HttpServletResponse) pageContext.getResponse())
                            .encodeURL(action));
                } else {
                    out.print(action);
                }
            }
            out.print("\" ");
            if (name != null) {
                out.print("name=\"" + Util.quote(name) + "\" ");
            }
            if (encType != null) {
                out.print("enctype=\"" + encType + "\" ");
            }

            // include any attributes we've got here
            Util.printAttributes(out, attributes);
            if (attributesText != null) {
                out.print(attributesText + " ");
            }

            // end the tag
            out.print(">");

        } catch (Exception ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();
            out.print("</form>");
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

    public String getBean() {
        return beanId;
    }

    public void setAction(String x) {
        action = x;
    }

    public void setMethod(String x) {
        method = x;
    }

    public void setEncType(String x) {
        encType = x;
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
     * Getter for property method.
     * 
     * @return Value of property method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Getter for property encType.
     * 
     * @return Value of property encType.
     */
    public String getEncType() {
        return encType;
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

    /**
     * Getter for property action.
     * 
     * @return Value of property action.
     */
    public String getAction() {
        return action;
    }

}