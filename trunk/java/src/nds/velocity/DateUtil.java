package nds.velocity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryUtils;

public class DateUtil {
	private static DateUtil a = null;

	public static DateUtil getInstance() {
		if (a == null) {
			a = new DateUtil();
		}
		return a;
	}

	public String dd(int daysFromNow) {
		return day(daysFromNow);
	}

	public String today() {
		return day(0);
	}

	public Date now() {
		return new Date();
	}

	public String yesterday() {
		return day(-1);
	}

	public String day(int daysFromNow) {

		return ((SimpleDateFormat) QueryUtils.dateNumberFormatter.get())
				.format(new Date(System.currentTimeMillis() + daysFromNow
						* 1000 * 3600 * 24));
	}

	public String monday() {
		return weekday(1, 0);
	}

	public String sunday() {
		return weekday(7, 0);
	}

	public String weekday(int paramInt) {
		return weekday(paramInt, 0);
	}

	public String wd(int paramInt1, int paramInt2) {
		return weekday(paramInt1, paramInt2);
	}

	/**
	 * Find week day of week from now
	 * 
	 * @param num
	 *            1 for Monday and 7 for Sunday
	 * @param weeksFromNow
	 *            0 for current week, -1 for last week, and so on
	 * @return yyyymmdd format
	 */
	public String weekday(int num, int weeksFromNow) {
		if (num < 1 || num > 7)
			throw new java.lang.IllegalArgumentException("Num(" + num
					+ ") should be 1-7");
		Calendar time = Calendar.getInstance();

		time.add(Calendar.WEEK_OF_YEAR, weeksFromNow);
		time.set(Calendar.DAY_OF_WEEK, (num + 1) % 7);
		if (num == 7)
			time.add(Calendar.DATE, 7);
		return ((SimpleDateFormat) QueryUtils.dateNumberFormatter.get())
				.format(time.getTime());
	}

	/**
	 * Month day of current month, will never exceed the last day of this month.
	 * For example, if in Feb, monthday(31) just returns the last day of Feb.
	 * 
	 * @param num
	 *            1-31, if current month has no 31, just return the last day
	 * @return yyyymmdd format
	 */
	public String monthday(int num) {
		return monthday(num, 0);
	}

	/**
	 * Month day of last month, will never exceed the last day of last month.
	 * For example, if in Feb, monthday(31) just returns the last day of Feb.
	 * 
	 * @param num
	 *            1-31, if current month has no 31, just return the last day
	 * @return yyyymmdd format
	 */
	public String lastmonthday(int num) {
		return monthday(num, -1);
	}

	public String md(int num, int monthsFromNow) {
		return monthday(num, monthsFromNow);
	}

	/**
	 * 
	 * @param num
	 *            1-31, if current month has no 31, just return the last day
	 * @param monthsFromNow
	 *            0 for this month, -1 for last month, 1 for next month, and so
	 *            on
	 * @return
	 */
	public String monthday(int num, int monthsFromNow) {
		if (num < 1 || num > 31)
			throw new java.lang.IllegalArgumentException("Num(" + num
					+ ") should be 1-31");
		Calendar time = Calendar.getInstance();
		time.add(Calendar.MONTH, monthsFromNow);
		int max = time.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (num > max)
			num = max;
		time.set(Calendar.DAY_OF_MONTH, num);
		return ((SimpleDateFormat) QueryUtils.dateNumberFormatter.get())
				.format(time.getTime());
	}

	/**
	 * 
	 * @param num
	 *            1-365, if current year has no 365, just return the last day of
	 *            the year
	 * @return
	 */
	public String yearday(int num) {
		return yearday(num, 0);
	}

	public String yd(int num, int yearsFromNow) {
		return yearday(num, yearsFromNow);
	}

	/**
	 * 
	 * @param num
	 *            1-365, if the specified year has no 365, just return the last
	 *            day of the year
	 * @param yearsFromNow
	 *            if yearsFromNow<1000, 0 for this year, -1 for last year, 1 for
	 *            next year, and so on if yearsFromNow>1000, just specify the
	 *            year number, 2006 is just year of 2006
	 * @return
	 */
	public String yearday(int num, int yearsFromNow) {
		if (num < 1 || num > 365)
			throw new java.lang.IllegalArgumentException("Num(" + num
					+ ") should be 1-31");
		Calendar time = Calendar.getInstance();
		if (yearsFromNow < 1000)
			time.add(Calendar.YEAR, yearsFromNow);
		else
			time.set(Calendar.YEAR, yearsFromNow);
		int max = time.getActualMaximum(Calendar.DAY_OF_YEAR);
		if (num > max)
			num = max;
		time.set(Calendar.DAY_OF_YEAR, num);
		return ((SimpleDateFormat) QueryUtils.dateNumberFormatter.get())
				.format(time.getTime());
	}

	/**
	 * 
	 * @return hour 0-23
	 */
	public String hour() {
		Calendar time = Calendar.getInstance();
		return String.valueOf(time.get(Calendar.HOUR_OF_DAY));
	}

	/**
	 * 
	 * @return current minute 0-59
	 */
	public String minute() {
		Calendar time = Calendar.getInstance();
		return String.valueOf(time.get(Calendar.MINUTE));
	}

	public String offset(int paramInt) {
		Calendar time = Calendar.getInstance();
		time.add(Calendar.MINUTE, paramInt);
		return ((SimpleDateFormat) QueryUtils.dateNumberFormatter.get())
				.format(time.getTime());
	}

	public String offset(Date paramDate, int paramInt) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(paramDate.getTime());
		time.add(Calendar.MINUTE, paramInt);
		return ((SimpleDateFormat) QueryUtils.dateNumberFormatter.get())
				.format(time.getTime());
	}

	public Date offsetDate(Date paramDate, int paramInt) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(paramDate.getTime());
		time.add(Calendar.MINUTE, paramInt);
		return time.getTime();
	}

	/**
	 * 
	 * @return "AM" or "PM"
	 */
	public String ampm() {
		Calendar time = Calendar.getInstance();
		return time.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
	}

	/**
	 * Format current time
	 * 
	 * @param format
	 *            any format that java format accepts
	 * @return
	 */
	public String fmt(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(new java.util.Date());
	}

	public Date parse(String paramString) {
		if (paramString == null)
			return null;
		if (paramString.contains("sysdate")) {
			try {

				Date isdate = (Date) QueryEngine.getInstance().doQueryOne(
						"select " + paramString + " from dual");
				if ((Date) isdate instanceof Date) {
					return isdate;
				}
			} catch (Throwable localThrowable) {
			}
			return null;
		}

		try {
			return ((SimpleDateFormat) QueryUtils.dateNumberFormatter.get())
					.parse(paramString);
		} catch (Exception localException1) {
			try {
				return ((SimpleDateFormat) QueryUtils.dateTimeSecondsFormatter
						.get()).parse(paramString);
			} catch (Exception localException2) {
			}
		}
		return null;
	}
	
	public int compareTo(Object tm1,Object tm2){
		
		java.text.DateFormat df=new java.text.SimpleDateFormat("yyyyMMdd");
		java.util.Calendar c1=java.util.Calendar.getInstance();   
		java.util.Calendar c2=java.util.Calendar.getInstance();  
		try  
		{   
		c1.setTime(df.parse((String)tm1));   
		c2.setTime(df.parse((String)tm2));   
		}catch(java.text.ParseException e){   
		System.err.println("格式不正确");   
		}
		int result=c1.compareTo(c2);   
		if(result==0)   
		//System.out.println("c1相等c2");   
			return 0;
		else if(result<0)   
		//System.out.println("c1小于c2");   
			return 2;
		else  
		//System.out.println("c1大于c2");
			return 1;
	}
	

	public String fmt(Object paramObject, String paramString) {
		Date localDate = null;
		if ((paramObject instanceof String))
			localDate = parse((String) paramObject);
		else if ((paramObject instanceof Date))
			localDate = (Date) paramObject;

		if (localDate != null)
			return new SimpleDateFormat(paramString).format(localDate);
		return "";
	}

	static {
		LoggerManager.getInstance().getLogger(DateUtil.class.getName());
	}
}

/*
 * Location: E:\portal5\portal422\server\default\deploy\nds.war\WEB-INF\classes\
 * Qualified Name: nds.velocity.DateUtil JD-Core Version: 0.6.2
 */