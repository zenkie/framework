/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.cxtab;

import java.util.*;
import java.io.*;
import java.text.*;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import nds.util.*;
import nds.velocity.VelocityUtils;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;

/**
 * Report variables used by Velocity template. 
 * 
 * Normally set as $v in template file
 * 
 * Singleton
 * @author yfzhu@agilecontrol.com
 */

public class ReportVariables  {
	private final static Logger logger=LoggerManager.getInstance().getLogger(ReportVariables.class.getName());
	private static ReportVariables instance=null;
	
	private VelocityContext context=null; 
	
	private ReportVariables(VelocityContext  c){
		this.context=c;
		c.put("v",this);
	}
	public static ReportVariables getInstance(){
		if(instance==null) {
			try{
				Velocity.init();
			}catch(Exception e){
				logger.error("fail to init velocity", e);
				throw new NDSRuntimeException("Fail to init velocity",e);
			}
			VelocityContext c= new VelocityContext();
			
			instance=new ReportVariables(c);
		}
		return instance;
	}
	/**
	 * Evaluate template containing velocity variables
	 * @param template 
	 * @return
	 */
	public String evaluate(String template) throws NDSException {

		StringWriter output = new StringWriter();

		try {
			
			Velocity.evaluate(context, output, VelocityUtils.class.getName(), template);
		} catch (Exception e) {
			logger.error("fail to eval "+ template+":"+ e.getMessage());
			throw new NDSException("Fail to parse:"+ e.getMessage(), e);
		}

		return output.toString();
	}
	/**
	 * Alias as day()
	 * @param daysFromNow
	 * @return
	 */
	public String dd(int daysFromNow){
		return day(daysFromNow);
	}
	
	/**
	 * 
	 * @return yyyymmdd format
	 */
	public String today(){
		return day(0);
	}
	public String yesterday(){
		return day(-1);
	}
	
	/**
	 * 
	 * @param daysFromNow -1 for yesterday and 1 for tomorrow, and so on
	 * @return
	 */
	public String day(int daysFromNow){
		return  ((SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(new java.util.Date(
				System.currentTimeMillis()+ daysFromNow* 1000* 3600*24));
	}
	
	public String monday(){
		return weekday(1,0);
	}
	public String sunday(){
		return weekday(7,0);
	}
	/**
	 * Find week day of current week
	 * @param num 1 for Monday and 7 for Sunday
	 * @return yyyymmdd format
	 */
	public String weekday(int num){
		return weekday(num,0);
	}
	public String wd(int num, int weeksFromNow){
		return weekday(num,weeksFromNow);
	}
	/**
	 * Find week day of week from now
	 * @param num 1 for Monday and 7 for Sunday
	 * @param weeksFromNow 0 for current week, -1 for last week, and so on
	 * @return yyyymmdd format
	 */
	public String weekday(int num, int weeksFromNow){
		if(num<1 || num>7) throw new java.lang.IllegalArgumentException("Num("+num+") should be 1-7");
		Calendar time=Calendar.getInstance();
		
		time.add(Calendar.WEEK_OF_YEAR, weeksFromNow); 
		time.set(Calendar.DAY_OF_WEEK, (num+1)%7);
		if(num==7) time.add(Calendar.DATE, 7);
		return ((SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(time.getTime());
	}
	/**
	 * Month day of current month, will never exceed the last day of this month. 
	 * For example, if in Feb, monthday(31) just returns the last day of Feb.
	 * @param num 1-31, if current month has no 31, just return the last day
	 * @return yyyymmdd format
	 */
	public String monthday(int num){
		return monthday(num, 0);
	}
	/**
	 * Month day of last month, will never exceed the last day of last month. 
	 * For example, if in Feb, monthday(31) just returns the last day of Feb.
	 * @param num 1-31, if current month has no 31, just return the last day
	 * @return yyyymmdd format
	 */
	public String lastmonthday(int num){
		return monthday(num, -1);
	}
	public String md(int num, int monthsFromNow){
		return monthday(num,monthsFromNow);
	}
	/**
	 * 
	 * @param num  1-31, if current month has no 31, just return the last day
	 * @param monthsFromNow 0 for this month, -1 for last month, 1 for next month, and so on 
	 * @return
	 */
	public String monthday(int num, int monthsFromNow){
		if(num<1 || num>31) throw new java.lang.IllegalArgumentException("Num("+num+") should be 1-31");
		Calendar time=Calendar.getInstance();
		time.add(Calendar.MONTH, monthsFromNow);
		int max= time.getActualMaximum(Calendar.DAY_OF_MONTH);
		if(num>max) num= max;
		time.set(Calendar.DAY_OF_MONTH, num);
		return ((SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(time.getTime());
	}
	/**
	 * 
	 * @param num  1-365, if current year has no 365, just return the last day of the year
	 * @return 
	 */
	public String yearday(int num){
		return yearday(num,0);
	}
	public String yd(int num, int yearsFromNow){
		return yearday(num, yearsFromNow);
	}
	/**
	 * 
	 * @param num  1-365, if the specified year has no 365, just return the last day of the year
	 * @param yearsFromNow 
	 *  if yearsFromNow<1000, 0 for this year, -1 for last year, 1 for next year, and so on
	 *  if yearsFromNow>1000, just specify the year number, 2006 is just year of 2006 
	 * @return 
	 */
	public String yearday(int num, int yearsFromNow){
		if(num<1 || num>365) throw new java.lang.IllegalArgumentException("Num("+num+") should be 1-31");
		Calendar time=Calendar.getInstance();
		if(yearsFromNow<1000)
			time.add(Calendar.YEAR, yearsFromNow);
		else
			time.set(Calendar.YEAR, yearsFromNow);
		int max= time.getActualMaximum(Calendar.DAY_OF_YEAR);
		if(num>max) num= max;
		time.set(Calendar.DAY_OF_YEAR, num);
		return ((SimpleDateFormat)QueryUtils.dateNumberFormatter.get()).format(time.getTime());
	}
	/**
	 * 
	 * @return hour 0-23
	 */
	public String hour(){
		Calendar time=Calendar.getInstance();
		return String.valueOf( time.get(Calendar.HOUR_OF_DAY));
	}
	/**
	 * 
	 * @return current minute 0-59
	 */
	public String minute(){
		Calendar time=Calendar.getInstance();
		return String.valueOf( time.get(Calendar.MINUTE));
	}
	/**
	 * 
	 * @return "AM" or "PM"
	 */
	public String ampm(){
		Calendar time=Calendar.getInstance();
		return  time.get(Calendar.AM_PM)==Calendar.AM?"AM":"PM";
	}
	/**
	 * Format current time 
	 * @param format any format that java format accepts
	 * @return 
	 */
	public String fmt(String pattern ){
		SimpleDateFormat sdf=new SimpleDateFormat(pattern);
		return sdf.format(new java.util.Date());
	}
	
}
