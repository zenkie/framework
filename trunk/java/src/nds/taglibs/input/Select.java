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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 * This class implements the &lt;input:select&gt; tag, which presents a
 * &lt;select&gt; form element.
 * 
 * @version 0.90
 * @author Shawn Bayern
 * @author Lance Lavandowska
 */

public class Select extends TagSupport {

    private String name; // name of the select element

    private String dVal; // default value if none is found

    private String[] dValArray; // our multiple default values

    private Map attributes; // attributes of the <select> element

    private Map options; // what are our options? :)

    private String attributesText; // attributes of the <input> element as text

    private String beanId; // bean id to get default values from

    private boolean multiple; // select multiple

    private String size; // select size

    private List optionLabels; // a list of option labels

    private List optionValues; // a list of option values

    private HashMap chosen; // chosen options (created in doStartTag)
    
    private boolean chosingDefault=true; // using default value if exists? when chosen is set from httpRequest, will not use default value then  
    public void release() {
        super.release();
        name = null;
        dVal = null;
        dValArray = null;
        attributes = null;
        options = null;
        attributesText = null;
        beanId = null;
        multiple = false;
        size = null;
        optionLabels = null;
        optionValues = null;
        chosen = null;
    }

    public int doStartTag() throws JspException {
        try {
            // sanity check
            if (name == null || name.equals(""))
                throw new JspTagException("invalid null or empty 'name'");

            // get what we need from the page
            ServletRequest req = new nds.control.web.NDSServletRequest( pageContext.getRequest());
            JspWriter out = pageContext.getOut();

            // start building up the tag
            out.print("<select name=\"" + Util.quote(name) + "\" ");

            // include any attributes we've got here
            Util.printAttributes(out, attributes);
            if (attributesText != null) {
                out.print(attributesText + " ");
            }

            if (multiple) {
                out.print("multiple=\"multiple\" ");
            }
            if (size != null) {
                out.print("size=\"" + Util.quote(size) + "\" ");
            }

            // end the starting tag
            out.println(">");

//          get the current selection
            String[] selected = req.getParameterValues(name);
            String ai = (String)attributes.get("ai");
            HashMap chosen = new HashMap();
            if (selected != null && ai == null)
                for (int i = 0; i < selected.length; i++)
                    chosen.put(selected[i], null);
            else if(selected != null && ai != null){
                try{
                    chosen.put(selected[Integer.parseInt(ai)], null);
                }catch(Exception e){}
            }
            if (options != null) {
                for(Iterator i = options.keySet().iterator();i.hasNext();){
                    Object oKey = i.next();
                    Object oVal = options.get((String)oKey);

                    /* If the option contains non-Strings, give the user
                     * a more meaningful message than what he or she would get
                     * if we just propagated a ClassCastException back.
                     * (This'll get caught below).
                     */
                    if (!(oKey instanceof String) ||
                            (oVal != null && !(oVal instanceof String)))
                        throw new JspException(
                            "all members in options Map must be Strings");
                    String key = (String) oKey;
                    String value = (String) oVal;
                    if (value == null)
                        value = key;        // use key if value is null

                    out.print("<option");
                    if (!value.equals(key))
                        out.print(" value=\"" + Util.quote(value) + "\"");
                    /*
                     * This may look confusing: we match the VALUE of
                     * this option pair with the KEY of the 'chosen' Map
                     * (We want to match <option>s on values, not keys.)
                     */
                    if ((selected != null && chosen.containsKey(value))
                            || (selected == null && dVal != null &&
                            value.equals(dVal)))
                        out.print(" selected=\"selected\"");
                    out.print(">");
                    out.print(Util.quote(key));
                    out.println("</option>");
                }
            } else
                throw new JspTagException("invalid select: no options");

            // close off the surrounding select
            out.print("</select>");

        } catch (Exception ex) {
        	ex.printStackTrace();
            throw new JspTagException(ex.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    private void outputOption(JspWriter out, Object oLabel, Object oVal)
            throws java.io.IOException {
        String label = oLabel.toString();
        /*
         * Convert the value to a String if it is not already.
         */
        String value = (oVal != null ? oVal.toString() : null);

        /* Output the option tag */
        out.print("<option");
        
        /* Output the value if there is one specified separate from the label */
        if ( value != null ) {
            out.print(" value=\"" + Util.quote(value) + "\"");
        }
        
        /* If there is no value specified then use the label as the value,
         * this is for checking if this option is selected based on value.
         */
        if (value == null)
            value = label; // use label if value is null
        
        /*
         * This may look confusing: we match the VALUE of this option pair with
         * the KEY of the 'chosen' Map (We want to match <option>s on values,
         * not keys.)
         */
        if (chosen.containsKey(value)) {
            if (!multiple) {
                chosen.remove(value);
            }
            out.print(" selected=\"selected\"");
        }
        System.out.println(" value=" + value +"(hash="+ value.hashCode());
        if( chosingDefault ){
        	if(dVal != null && value.equals(dVal)){
        		out.print(" selected=\"selected\"");
        	}else{
        		System.out.println(" default value:"+ dVal +" !=" + value);
        	}
        }
        	
        out.print(">");
        out.print(Util.quote(label));
        out.println("</option>");
    }

    public int doEndTag() throws JspException {
        /*try {
            JspWriter out = pageContext.getOut();
            out.print("</select>");
        } catch (Exception ex) {
            throw new JspTagException(ex.getMessage());
        }*/
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

    public void setMultiple(boolean x) {
        multiple = x;
    }

    public void setSize(String x) {
        size = x;
    }

    public void setDefault(String x) {
        dVal = x;
    }

    public void setDefaults(String[] x) {
        dValArray = x;
    }

    public void setDefaults(Map x) {
        dValArray = new String[x.size()];
        Iterator it = x.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            dValArray[i++] = it.next().toString();
        }
    }

    public void setDefaults(Collection c) {
        dValArray = new String[c.size()];
        Iterator it = c.iterator();
        int i = 0;
        while (it.hasNext()) {
            dValArray[i++] = it.next().toString();
        }
    }

    public void setOptions(Map x) {
        options = x;
    }

    public void setOptionLabels(List x) {
        optionLabels = x;
    }

    public void setOptionValues(List x) {
        optionValues = x;
    }

    public HashMap getChosen() {
        return chosen;
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

    /**
     * Getter for property defaults.
     * 
     * @return Value of property defaults.
     */
    public String[] getDefaults() {
        return dValArray;
    }

    /**
     * Getter for property multiple.
     * 
     * @return Value of property multiple.
     */
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * Getter for property optionLabels.
     * 
     * @return Value of property optionLabels.
     */
    public List getOptionLabels() {
        return optionLabels;
    }

    /**
     * Getter for property optionValues.
     * 
     * @return Value of property optionValues.
     */
    public List getOptionValues() {
        return optionValues;
    }

    /**
     * Getter for property options.
     * 
     * @return Value of property options.
     */
    public Map getOptions() {
        return options;
    }

    /**
     * Getter for property size.
     * 
     * @return Value of property size.
     */
    public String getSize() {
        return size;
    }

}