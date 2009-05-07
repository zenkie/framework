/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.*;
import java.util.Collections;
import nds.web.bean.*;
/**
 * 
 * Table which supports Calendar manipuplation
 * This kind of table must have following columns:
 * startdate, starttime, duration, content
 * 
 * 起始日期，起始时间，结束时间，内容（对于车辆，为借车人，事件简述，目标地点；对于会议室，
 * 为预定人，事件简述；对于员工日程，为事件简述；对于请假，为事件简述）
 */

public interface CalendarTable extends Table {
	/**
	 * Which column will be used as filter column on main table, currently only one supported   
	 * @return null if no filter column
	 */
	public Column getFilterColumn();
	/**
	 * if isEventEditable=true, 这个字段在getAddActionDestTable之上，存放时间
	 * @return
	 */
	public Column getAddActionTimeDestColumn();
	/**
	 * if isEventEditable=true, 这张表作为新增记录所在的表，表里哪个字段存放时间通过getAddActionTimeDestColumn指明
	 * @return
	 */
	public Table getAddActionDestTable();
	/**
	 * 是否允许新增记录，新增的记录基于哪张表，在getAddActionDestTable指明
	 * 例如：会议室预定，查看记录在会议室预定记录(oa_roomftp)中找，而定会议室则在会议室请求(oa_roomreq)表中建立
	 * @return
	 */
	public boolean isEventEditable();
	/**
	* When adTable has className set to this class name, the DBClassLoader 
	* will instantiate this class instance. But table should have following
	* columns to make sure that the calendar system can run well on it:
	* 
	* id, begindate(date), begintime(number(4)), endtime(number(4)), shortdesc (varchar2(20)),description varchar2((400))
	*
	*/
	public boolean isSupportCalendar();
	

}
