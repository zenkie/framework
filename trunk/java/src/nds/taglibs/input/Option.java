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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * 
 * This class implements the &lt;input:option&gt; tag, which presents an
 * &lt;option&gt; form element.
 * 
 * @version 0.90
 * @author Karl von Randow
 */

public class Option extends BodyTagSupport {

    private String value; // value of option

    private Map attributes; // attributes of the <select> element

    private String attributesText; // attributes of the <option> element as text

    public void release() {
        super.release();
        value = null;
        attributes = null;
        attributesText = null;
    }

    public int doStartTag() throws JspException {
        // It seems to be necessary to clear the body as release doesn't do
        // this?
        if (getBodyContent() != null) {
            getBodyContent().clearBody();
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        try {
            String content = getBodyContent() != null ? getBodyContent()
                    .getString() : null;

            // get what we need from the page
            JspWriter out = pageContext.getOut();

            String value = this.value;
            if (value == null && content != null) {
                value = content.trim();
            }

            // start building up the tag
            out.print("<option ");
            if (value != null) {
                out.print("value=\"" + Util.quote(value) + "\" ");
            }

            // include any attributes we've got here
            Util.printAttributes(out, attributes);
            if (attributesText != null) {
                out.print(attributesText + " ");
            }

            /*
             * Print out our options, selecting one or more if appropriate. If
             * there are multiple selections but the page doesn't call for a
             * <select> that accepts them, ignore the selections. This is
             * preferable to throwing a JspException because the (end) user can
             * control input, and we don't want the user causing exceptions in
             * our application.
             */

            if (testAndRemoveChosen(value)) {
                out.print("selected=\"selected\" ");
            }

            // end the starting tag
            out.print(">");
            if (content != null) {
                out.print(content);
            }
            out.print("</option>");
        } catch (Exception ex) {
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_PAGE;
    }

    protected boolean testAndRemoveChosen(String value) throws JspException {
        Select selectTag = (Select) findAncestorWithClass(this, Select.class);
        if (selectTag != null) {
            Map chosen = selectTag.getChosen();
            if (value != null && chosen != null && chosen.containsKey(value)) {
                if (!selectTag.isMultiple()) {
                    chosen.remove(value);
                }
                return true;
            } else {
                return false;
            }
        } else {
            throw new JspTagException("option tag used outside a select tag");
        }
    }

    public void setValue(String x) {
        value = x;
    }

    /**
     * Getter for property value.
     * 
     * @return Value of property value.
     */
    public String getValue() {
        return value;
    }

    public void setAttributes(Map x) {
        attributes = x;
    }

    /**
     * Getter for property attributes.
     * 
     * @return Value of property attributes.
     */
    public Map getAttributes() {
        return attributes;
    }

    public void setAttributesText(String x) {
        attributesText = x;
    }

    /**
     * Getter for property attributesText.
     * 
     * @return Value of property attributesText.
     */
    public String getAttributesText() {
        return attributesText;
    }

}