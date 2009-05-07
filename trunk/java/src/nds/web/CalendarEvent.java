/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web;
import java.util.*;
/**
 * Value holder for calendar event
 * @author yfzhu@agilecontrol.com
 */

public class CalendarEvent {
	public Date beginDate; // include date and beginTime
	public int id;
	public int beginTime;
	public Date endDate;
	public int endTime;
	public String shortDesc;
	public String description;
	public int auditState=0 ;// for check is it accepted (0=accepted)
	public int initAuditState=-1;
	public Date getStartDate(){ return beginDate;}
	private final static Calendar c= new GregorianCalendar();
	public Date getEndTime(){
		c.setTime(beginDate);
		c.set(Calendar.HOUR_OF_DAY, (int)(endTime/100) );
		c.set(Calendar.MINUTE, (endTime)%100);
		return c.getTime();
	}
	public String getCssClass(){
		String s;
		if(auditState==0) s="gamma";
		else if(auditState%2==0) s="auditing-row";
		else if(auditState==initAuditState) s="request-row";
		else s="reject-row";
		return s;
	}
}
