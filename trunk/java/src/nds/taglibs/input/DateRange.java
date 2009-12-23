
package nds.taglibs.input;

import nds.portlet.util.PortletUtils;
import nds.util.MessagesHolder;
import nds.util.WebKeys;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import nds.query.*;
/**
 * 
Date range selection, contains two visible input box and one hidden input
 */

public class DateRange extends TagSupport {
	
    private String name; // name of the text field

    private Map attributes; // attributes of the <input> element

    private String id; // bean id to get default values from
    
    private String showDefaultRange; // "Y" or "N"(default) set default start and end date
    
    public void release() {
        super.release();
        id = null;
        name = null;
        attributes = null;
        showDefaultRange=null;
    }

    public int doStartTag() throws JspException {
        try {
        	
            // get what we need from the page
            ServletRequest req = new nds.control.web.NDSServletRequest( pageContext.getRequest());
            JspWriter out = pageContext.getOut();
            /**
             * If true, will set start date and end date to sysdate-7 and sysdate
             */
            boolean defaultRange=nds.util.Tools.getYesNo(showDefaultRange, false);
            String startDate=null, endDate=null;
            if(defaultRange){
            	/**
            	 * 
				 * default to one week range, using portal.properties#query.date.range
				 *  
            	 */
            	java.util.Calendar c= java.util.Calendar.getInstance();
            	c.setTimeInMillis(System.currentTimeMillis());
            	c.add(java.util.Calendar.DAY_OF_MONTH, - QueryUtils.DEFAULT_DATE_RANGE );
            	startDate=  ((java.text.SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(c.getTime());
            	endDate= ((java.text.SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(new java.util.Date());
            }
            // hidden input
            out.print("<input type='hidden' id='"+ Util.quote(id)+"' ");
            out.print("name=\"" + Util.quote(name) + "\" value='");
            if(defaultRange){
            	out.print(startDate+ "~"+ endDate);
            }
        	out.print("'>");
            // from input
            out.print("<span class='f'>"+PortletUtils.getMessage(pageContext,"datefrom")+"</span>");
            out.print("&nbsp;&nbsp;");
            out.print("<input type='text' id='"+ Util.quote(id)+"_1' ");
            out.print("name=\"" + Util.quote(name) + "_1\" value='");
            if(defaultRange){
            	out.print( startDate);
            }
            out.print("' onchange=\"javascript:dateRangeChanged('"+id+"')\"");
            Util.printAttributes(out, attributes);
            out.print(">");
        	String imageCalendar="ic_"+id+"_1";
            out.print("<a onclick=\"event.cancelBubble=true;\" href=\"javascript:showCalendar('"+imageCalendar+"',false,'"+id+"_1',null,null,true);\"><img id='"+imageCalendar+"' width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></a>");
            // to input
            out.print("<br>");
            out.print("<span class='f'>"+PortletUtils.getMessage(pageContext,"dateto")+"</span>");
            out.print("&nbsp;&nbsp;");
            out.print("<input type='text' id='"+ Util.quote(id)+"_2' ");
            out.print("name=\"" + Util.quote(name) + "_2\" value='");
            if(defaultRange){
            	out.print( endDate);
            }
            out.print("' onchange=\"javascript:dateRangeChanged('"+id+"')\"");
            Util.printAttributes(out, attributes);
            out.print(">");
        	imageCalendar="ic_"+id+"_2";
            out.print("<a onclick=\"event.cancelBubble=true;\" href=\"javascript:showCalendar('"+imageCalendar+"',false,'"+id+"_2',null,null,true);\"><img id='"+imageCalendar+"' width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></a>");
            

        } catch (Exception ex) {
            throw new JspTagException(ex.getMessage());
        }
        return SKIP_BODY;
    }

    public void setName(String x) {
        name = x;
    }
    public String getName() {
        return name;
    }    
    public Map getAttributes() {
        return attributes;
    }
    public void setAttributes(Map x) {
        attributes = x;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getShowDefaultRange() {
		return showDefaultRange;
	}

	public void setShowDefaultRange(String showDefaultRange) {
		this.showDefaultRange = showDefaultRange;
	}

    


}