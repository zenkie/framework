
package nds.taglibs.input;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.schema.TableManager;
import nds.util.IntHashtable;
import nds.util.ParamUtils;
import nds.util.StringHashtable;
import nds.util.WebKeys;


/**
 *
 */

public class PreferSelect extends TagSupport {

    private String name;        // name of the select element
    private String dVal;        // default value if none is found
    private Map attributes;     // attributes of the <select> element
    private int tableID=-1;
    String form;
    public void setName(String x) {
        name = x;
    }
    public void setTable(String tbId){
        try{
        tableID= (new Integer(tbId)).intValue();
        }catch(Exception e){
            tableID=-1;
        }
    }
    public void setForm(String formId){
        form=formId;
    }
    public void setAttributes(Map x) {
        attributes = x;
    }

    public int doStartTag() throws JspException {
        StringHashtable options;        // what are our options? :)
        try {
            // sanity check
            if (name == null || name.equals(""))
                throw new JspTagException("invalid null or empty 'name'");

            // get what we need from the page
            ServletRequest req = new nds.control.web.NDSServletRequest( pageContext.getRequest());
            JspWriter out = pageContext.getOut();

            // get currrent page's tableId and session
            if (tableID==-1) throw new JspTagException("table not set or is not integer value");



            SessionContextManager manager= WebUtils.getSessionContextManager(pageContext.getSession());
            UserWebImpl usr=(UserWebImpl)manager.getActor(WebKeys.USER);


            // start building up the tag
            out.println("<select class='PreferSelect' size='1' name=\"" + Util.quote(name) + "\" onchange='javascript:changePrefer(this.value,"+ form + ", this)'> ");


            /*
             * Print out our options, selecting one or more if appropriate.
             * If there are multiple selections but the page doesn't call
             * for a <select> that accepts them, ignore the selections.
             * This is preferable to throwing a JspException because the
             * (end) user can control input, and we don't want the user
             * causing exceptions in our application.
             */

            // get the current selection
             int sv=ParamUtils.getIntAttribute(req, "preferId", 0);// req.getParameter(name);
            String selected=sv+"";
            //System.out.println(" selected = "+selected );

            options= new StringHashtable();

            options.put("--空过滤器--", "4"); // 当选中的时候，将使得页面被刷新，过滤器设置为0
            if ( sv== 8){
                options.put("--保存--", "1");  // 当选中的时候，如果当前设置为0，则调用addPreference,>9则调用copyPreferenceAs
                options.put("--自定义--", "8");
            }else if (sv > 9){
                options.put("--删除--", "2");
                options.put("--作为默认项--", "3");
            }
            
            IntHashtable ih=new IntHashtable();//usr.getPreferences(TableManager.getInstance().getTable(tableID).getName().toUpperCase());
            Enumeration  eih= ih.keys(); int iKey;
            while (eih.hasMoreElements()) {
                iKey=((Integer) eih.nextElement()).intValue() ;
                options.put( (String)ih.get(iKey),iKey+"");
            }

            // actually print the <option> tags
            if (options != null) {
                Enumeration i = options.keys();
                while (i.hasMoreElements()) {
                    Object oKey = i.nextElement();
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
                    if( value.equals( selected))
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
            throw new JspTagException(ex.getMessage());
        }
        return SKIP_BODY;
    }
}
