
package nds.taglibs.input;

import nds.portlet.util.PortletUtils;
import nds.util.MessagesHolder;
import nds.util.WebKeys;

import java.util.Map;
import java.util.Calendar;
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
    
    private String showTime;
    
    public void release() {
        super.release();
        id = null;
        name = null;
        attributes = null;
        showDefaultRange=null;
        showTime=null;
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
            boolean ishowtime=nds.util.Tools.getYesNo(showTime, false);
            //System.out.print("ishowtime is "+ishowtime);
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
            	if (ishowtime) {
            		((Calendar) c).set(11, 0);
            		((Calendar) c).set(12, 0);
            		((Calendar) c).set(13, 0);
            		startDate =  ((java.text.SimpleDateFormat) QueryUtils.dateTimeSecondsFormatter.get()).format(((Calendar)c).getTime());
            		java.util.Calendar end = Calendar.getInstance();
            		end.setTimeInMillis(System.currentTimeMillis());
            		((Calendar) end).set(11, 23);
            		((Calendar) end).set(12, 59);
            		((Calendar) end).set(13, 59);
            		endDate = ((java.text.SimpleDateFormat) QueryUtils.dateTimeSecondsFormatter.get()).format(((Calendar)end).getTime());
            	} else {
            		startDate = ((java.text.SimpleDateFormat) QueryUtils.dateNumberFormatter
            				.get()).format(c.getTime());
            		endDate = ((java.text.SimpleDateFormat) QueryUtils.dateNumberFormatter
            				.get()).format(new java.util.Date());
            	}
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
            //out.print("<a onclick=\"event.cancelBubble=true;\" href=\"javascript:showCalendar('"+imageCalendar+"',false,'"+id+"_1',null,null,true);\"><img id='"+imageCalendar+"' width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></a>");
        	out.print("<a onclick=\"event.cancelBubble=true;\" href='javascript:WdatePicker({errDealMode:2,el:\"" + Util.quote(id) + "_1\"" + (ishowtime ? ",dateFmt:\"yyyy/MM/dd HH:mm:ss\"" : "") + "})'><img id='"+imageCalendar+"' width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></a>");
        	//localJspWriter.print("&nbsp;<span id='" + (String)localObject + "' onaction='javascript:WdatePicker({errDealMode:2,el:\"" + a.a(this.b) + "_1\"" + (bool2 ? ",dateFmt:\"yyyy/MM/dd HH:mm:ss\"" : "") + "})'><img width='16' height=16 src='/html/nds/images/" + (bool2 ? "datetime" : "datenum") + ".gif' border='0' align='absmiddle'></span>");

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
        	out.print("<a onclick=\"event.cancelBubble=true;\" href='javascript:WdatePicker({errDealMode:2,el:\"" + Util.quote(id) + "_2\"" + (ishowtime ? ",dateFmt:\"yyyy/MM/dd HH:mm:ss\"" : "") + "})'><img id='"+imageCalendar+"' width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></a>");

            //out.print("<a onclick=\"event.cancelBubble=true;\" href=\"javascript:showCalendar('"+imageCalendar+"',false,'"+id+"_2',null,null,true);\"><img id='"+imageCalendar+"' width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></a>");
            

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
	
	public String getShowTime() {
		return showTime;
	}

	public void setShowTime(String showtime) {
		this.showTime = showtime;
	}

}