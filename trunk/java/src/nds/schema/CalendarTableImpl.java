/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.*;
import java.util.Collections;
import nds.web.bean.*;
import nds.util.PairTable;
/**
 * 
 * Table which supports Calendar manipuplation
 * This kind of table must have following columns:
 * startdate, starttime, duration, content
 * 
 * 起始日期，起始时间，结束时间，内容（对于车辆，为借车人，事件简述，目标地点；对于会议室，
 * 为预定人，事件简述；对于员工日程，为事件简述；对于请假，为事件简述）
 */

public class CalendarTableImpl extends AuditTableImpl implements CalendarTable{
	private static class CalendarTableDef{
		public String filterColumn; // the column on CalendarTable used for filter
		public boolean isEventEditable; // is to show add icon on day? if set to true, should set addActionDestTable/addActionDestColumnTime
		public String addActionDestTable; // when isEventEditable=true, use can goto this table for adding records 
		public String addActionTimeDestColumn; // this can link to the column on addActionDestTable
		public CalendarTableDef(String fc, boolean iee, String aadt, String aadct ){
			filterColumn=fc;
			isEventEditable=iee;
			addActionDestTable= aadt;
			addActionTimeDestColumn=aadct;
		}
	}
	private static PairTable ctables=null;//key : tablename, value: CalendarTableDef
	static {
		if(ctables==null){
			ctables=new PairTable();
			ctables.put("OA_VEHICLE_USE", new CalendarTableImpl.CalendarTableDef("OA_VEHICLE_ID", true, "OA_VEHICLE_USE", "BEGINDATE"));
			ctables.put("OA_ROOM_USE", new CalendarTableImpl.CalendarTableDef("OA_ROOM_ID", true, "OA_ROOM_USE", "BEGINDATE"));
			ctables.put("S_COURSE", new CalendarTableImpl.CalendarTableDef("C_BPARTNER_ID", true, "S_COURSE", "BEGINDATE"));
		}
	}
	private Collection extendMenuItems=null; 
	private Collection extendButtons=null;
	/**
	 * Which column will be used as filter column on main table, currently only one supported   
	 * @return null if no filter column
	 */
	public Column getFilterColumn(){
		String tn= this.getName().toUpperCase();
		CalendarTableImpl.CalendarTableDef cdd=(CalendarTableImpl.CalendarTableDef) ctables.get(tn);
		Column col=null;
		if (cdd!=null){
			TableManager manager= TableManager.getInstance();
			col= manager.getColumn(tn, cdd.filterColumn);
		}
		return 	col;
	}
	/**
	 * if isEventEditable=true, 这个字段在getAddActionDestTable之上，存放时间
	 * @return
	 */
	public Column getAddActionTimeDestColumn(){
		String tn= this.getName().toUpperCase();
		CalendarTableImpl.CalendarTableDef cdd=(CalendarTableImpl.CalendarTableDef) ctables.get(tn);
		Column t=null; // default
		if (cdd!=null){
			TableManager manager= TableManager.getInstance();
			t=manager.getColumn( cdd.addActionDestTable,cdd.addActionTimeDestColumn );
		}
		return t;
	}
	/**
	 * if isEventEditable=true, 这张表作为新增记录所在的表，表里哪个字段存放时间通过getAddActionTimeDestColumn指明
	 * @return
	 */
	public Table getAddActionDestTable(){
		String tn= this.getName().toUpperCase();
		CalendarTableImpl.CalendarTableDef cdd=(CalendarTableImpl.CalendarTableDef) ctables.get(tn);
		Table t=null; // default
		if (cdd!=null){
			TableManager manager= TableManager.getInstance();
			t=manager.getTable( cdd.addActionDestTable);
		}
		return t;
	}
	/**
	 * 是否允许新增记录，新增的记录基于哪张表，在getAddActionDestTable指明
	 * 例如：会议室预定，查看记录在会议室预定记录(oa_roomftp)中找，而定会议室则在会议室请求(oa_roomreq)表中建立
	 * @return
	 */
	public boolean isEventEditable(){
		String tn= this.getName().toUpperCase();
		CalendarTableImpl.CalendarTableDef cdd=(CalendarTableImpl.CalendarTableDef) ctables.get(tn);
		boolean b= false; // default
		if (cdd!=null){
			b= cdd.isEventEditable;
		}
		return b;
	}
	/**
	* When adTable has className set to this class name, the DBClassLoader 
	* will instantiate this class instance. But table should have following
	* columns to make sure that the calendar system can run well on it:
	* 
	* id, begindate(date), begintime(number(4)), endtime(number(4)), shortdesc (varchar2(20)),description varchar2((400))
	*
	*/
	public boolean isSupportCalendar(){
		TableManager manager= TableManager.getInstance();
		return ((manager.getColumn(this.getName(), "Id")!=null) &&
		(manager.getColumn(this.getName(), "begindate")!=null) &&
		(manager.getColumn(this.getName(), "begintime")!=null) &&
		(manager.getColumn(this.getName(), "endtime")!=null) &&
		(manager.getColumn(this.getName(), "shortdesc")!=null) &&
		(manager.getColumn(this.getName(), "description")!=null));
	}
	/**
	 * Contains only on extend menu item;
	 */
	public Collection getExtendMenuItems(){
    	if(extendMenuItems==null ){
    		MenuItem mi=new MenuItem(true);
    		mi.function="calendarView()";
    		mi.image="/images/tb_calendar.gif";
    		mi.name="object.calendar";
    		extendMenuItems=Collections.singletonList(mi);
    	}
    	
    	return extendMenuItems;
    }

}
