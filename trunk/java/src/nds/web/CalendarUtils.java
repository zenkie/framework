/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web;

import nds.schema.CalendarTable;
import nds.schema.Column;
import nds.security.Directory;
import nds.util.*;
import java.util.*;
import nds.control.web.*;
import nds.query.*;
/**
 * Utilities to get calendar events
 * @author yfzhu@agilecontrol.com
 */

public class CalendarUtils {
	public static final long SECOND = 1000;

	public static final long MINUTE = SECOND * 60;

	public static final long HOUR = MINUTE * 60;

	public static final long DAY = HOUR * 24;

	public static final long WEEK = DAY * 7;
	
	/**
	 * Get events of specified table of specified date 
	 * @param table The table to be searched 
	 * @param filter the filter set on the filter column, must be single record filter
	 * @param user the currenty user who executed this query, shoud impose security filter
	 * @param tempCal the day to get the events
	 * @return CalendarEvent
	 */
	public static List getEventsByDay(CalendarTable table,  String filter, UserWebImpl user, Calendar tempCal) throws Exception{
		nds.schema.Column auditStateColumn= table.getColumn("audit_state");
		boolean isCheckAuditState=(auditStateColumn !=null);
		QueryEngine engine=QueryEngine.getInstance();
		QueryRequestImpl query=createRequest(table, filter, user, tempCal);
		ArrayList al=new ArrayList();
		query.addSelection(table.getColumn("id").getId());
		query.addSelection(table.getColumn("begindate").getId());
		query.addSelection(table.getColumn("begintime").getId());
		query.addSelection(table.getColumn("endtime").getId());
		query.addSelection(table.getColumn("shortdesc").getId());
		query.addSelection(table.getColumn("description").getId());
		int defaultAuditState=-1;
		if(isCheckAuditState) {
			query.addSelection(auditStateColumn.getId());
			defaultAuditState=nds.util.Tools.getInt( auditStateColumn.getDefaultValue(),3);
		}
		
		query.addOrderBy(new int[]{table.getColumn("id").getId()}, false);
		
		
		QueryResult rs= engine.doQuery(query);
		Calendar c= new GregorianCalendar();
		while(rs.next()){
			CalendarEvent ce=new CalendarEvent();
			ce.id= Tools.getInt(rs.getObject(1), -1);
			ce.beginTime =Tools.getInt(rs.getObject(3), 0);
			ce.beginDate =getDate( (Date)rs.getObject(2), ce.beginTime, c);
			ce.endTime= Tools.getInt(rs.getObject(4), 0);
			ce.shortDesc= (String)rs.getObject(5);
			ce.description= (String)rs.getObject(6);
			if(isCheckAuditState){
				ce.auditState=  Tools.getInt(rs.getObject(7),0);
				ce.initAuditState = defaultAuditState;
			}
			al.add(ce);
		}
		return al;
	}
	private static QueryRequestImpl createRequest(CalendarTable table,  String filter, UserWebImpl user, Calendar tempCal) throws Exception{
		Expression expr=user.getSecurityFilter(table.getName(), Directory.READ);
		// date filter
		if(expr !=null){
			expr= expr.combine( new Expression(ColumnLink.createLink( table.getColumn("begindate")), 
				"=" +((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format( tempCal.getTime()), null)
				,SQLCombination.SQL_AND, null);
		}else{
			expr=  new Expression(ColumnLink.createLink( table.getColumn("begindate")), 
					"=" +((java.text.SimpleDateFormat)QueryUtils.dateFormatter.get()).format( tempCal.getTime()), null);
		}
		if( !Validator.isNull(filter)){
			// user specified filter
			Column col= table.getFilterColumn();
			ColumnLink cl=null;
			if(col !=null){
				if( col.getReferenceColumn()!=null ) cl= new ColumnLink(new int[]{col.getId(), col.getReferenceColumn().getTable().getAlternateKey().getId()});
				else cl= new ColumnLink(new int[]{col.getId()});
				Expression e= new Expression(cl, filter, null);
				expr= e.combine(expr, SQLCombination.SQL_AND, null);
			}
		}
		QueryEngine engine=QueryEngine.getInstance();
		QueryRequestImpl query= engine.createRequest(user.getSession());
		query.setMainTable(table.getId());
		query.addParam(expr);
		return query;
	}
	/**
	 * Check there has at least one event
	 * @param table
	 * @param filter
	 * @param user
	 * @param tempCal
	 * @return
	 * @throws Exception
	 */
	public static boolean hasEvents(CalendarTable table,  String filter, UserWebImpl user, Calendar tempCal) throws Exception{
		QueryEngine engine=QueryEngine.getInstance();
		QueryRequestImpl query=createRequest(table, filter, user, tempCal);
		return Tools.getInt(engine.doQueryOne(query.toCountSQL()),-1)>0;
	}
	private static Date getDate(Date d, int hourMin, Calendar c){
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, (int)hourMin/100);
		c.set(Calendar.MINUTE, hourMin%100);
		return c.getTime();
	}
}
