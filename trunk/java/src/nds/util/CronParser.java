/******************************************************************
*
*$RCSfile: CronParser.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:26 $
*
*$Log: CronParser.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:26  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.3  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.util;

import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
* Parse a cron job's time string, thus to compute when should a job
* be executed.
* @author yfzhu originally created for eforum
*
*/
public class CronParser{
    /**
     * Contains plaining meaning of each item
     */
	private String[] plainMeaning;
    private String cron;
	/**
	* Contains range of each field
	*/
	private BitSet[] range;
	/**
	* @param cron this string in format very similiar to Unix syntax of
	* "cronfile", contains 5 items, in order as:
	*	Minute(0-59) Hour(0-23) Date(1-31) Month(1-12) Day(1-7, Monday,...,Sunday order)
	* each item is seperated by token "\t\f\n\r", reserved chars and their meaning
	* is as following:
	*	'-': all values between the min and max(include)
	*	',': more sub items
	*	'*': all values
	* Sample:
	*	min		hour	date	month			day
	*	0		1,23	*		1-6,8,10-12		3-6
	*  	0		0		*		*				4
	*
	* @throw Excpetion if cron can not be correctly parsed
	*/
	public CronParser(String cron) throws NumberFormatException{
		this.cron=cron;
        init();
		StringTokenizer st=new StringTokenizer(cron);
		int pos=0;
		while( st.hasMoreTokens()){
			String part= st.nextToken();
			fillRange(pos, part);
			pos++;
		}
        checkRangeBound();
	}
	/**
     * All input param may have space char in it.
	* @param min minute 0-59
	* @param hour 0-23
	* @param monthdate 1-31
	* @param month 1-12
	* @param weekday 1-7 ( 1 for monday 7 for sunday)
	*/
	public CronParser(String min,String hour,String monthdate,String month,String weekday) throws NumberFormatException{
		min=StringUtils.removeChar(min,' ');
        hour=StringUtils.removeChar(hour,' ');
        monthdate=StringUtils.removeChar(monthdate,' ');
        month=StringUtils.removeChar(month,' ');
        weekday=StringUtils.removeChar(weekday,' ');

        cron=min+" "+hour+" "+monthdate+" "+month+" "+weekday;

        init();
		fillRange(0, min);
		fillRange(1,hour);
		fillRange(2,monthdate);
		fillRange(3,month);
		fillRange(4,weekday);
        checkRangeBound();
	}
    private void init(){
		range=new BitSet[5];
		range[MINUTE]=new BitSet(60);
		range[HOUR]=new BitSet(24);
		range[MONTHDAY]=new BitSet(31);
		range[MONTH]=new BitSet(12);
		range[WEEKDAY]=new BitSet(7);
        plainMeaning=new String[5];
        for(int i=0;i< 5;i++) plainMeaning[i]="";

    }
    private void checkRangeBound()throws IndexOutOfBoundsException{
        for(int i=0;i< 5;i++){
            if(range[i].length()> MAX_VALUES[i]-MIN_VALUES[i]+1){
                String[] items=new String[]{
                    "minute","hour","monthday","month","weekday"
                };
                throw new IndexOutOfBoundsException("Item "+items[i]+" contains "+(range[i].length()-1 +MIN_VALUES[i])+ ",out of bound("+MIN_VALUES[i]+"~"+MAX_VALUES[i]+")");
            }
        }

    }
	/**
	* Fill range of BitSet[pos] according to str, str is like:
	*	"0-89", "5,6-4,*"
	* @throw NumberFormatException if str is not valid
	*/
	private void fillRange(int pos, String str) throws NumberFormatException{
		StringTokenizer st=new StringTokenizer(str,",");
		while( st.hasMoreTokens()){
			String s=st.nextToken();
			int i= s.indexOf("-");
			if( i>0){
				int begin= Integer.parseInt( s.substring(0,i));
				int end= Integer.parseInt(s.substring(i+1));
                if( begin- MIN_VALUES[pos]>= range[pos].size() ||
                        end - MIN_VALUES[pos]>=range[pos].size()){
                    throw new IndexOutOfBoundsException("Out of bound");
                }
				for(int j=begin;j<= end;j++){
					range[pos].set(j-MIN_VALUES[pos]);
				}
                appendPlainMeaning(pos, begin+"-"+end);
				continue;
			}
			i=s.indexOf("*");
			if( i >=0){
				for( int j=0;j< MAX_VALUES[pos]-MIN_VALUES[pos]+1;j++)
					range[pos].set(j);
                plainMeaning[pos]="every";
				return;
			}
            // only one integer in it
			int j= Integer.parseInt(s);
            if( j- MIN_VALUES[pos]>= range[pos].size() ){
                    throw new IndexOutOfBoundsException(j+ "out of bound("+MIN_VALUES[pos]+"~"+MAX_VALUES[pos]+")");
            }
			range[pos].set(j-MIN_VALUES[pos]);
            appendPlainMeaning(pos,j+"");
		}
	}
    private void appendPlainMeaning(int pos, String item){
        if( !plainMeaning[pos].equals(""))
            plainMeaning[pos] +="、";
        plainMeaning[pos] +=item;
    }
	/**
	* Return next valid run time after the date(exclude). Given a cron string as
	*	"0 0 0 * * 4", and the requested date is Wendesday, then the return
	*	date will be the start second of tomorrow
	* @para date the date that search begins.
	* @param dayRange	cron may contains a requirement which has no valid day in a quite
	* 		long range. This parameter specifies afer how many day tries, we
	* 		will give up.
	* @throws Exception if could not find a day after <code>dayRange</code> days.
	*/
	public Date	getNextRunTime(Date date,int dayRange) throws Exception{
		Calendar c= new GregorianCalendar();
		c.setTime(date);
		rollToNextValidTime(c);
		rollToNextValidDate(c, dayRange);// may throw Exception
		return c.getTime();
	}
	/**
	* check if specified calendar's day is valid according to cron
	*/
	private boolean isValidDay(final Calendar c){
        /** dayweek is somewhat complicate, we want to Monday be 1, and Sunday be 7,
         *  while in array, range[WEEKDAY].(0) will be Monday,and (6) will be Sunday
         */
        int day_week= c.get(Calendar.DAY_OF_WEEK) -Calendar.SUNDAY -1;
        if( day_week ==-1)  day_week=6;

		return  range[MONTHDAY].get( c.get(Calendar.DAY_OF_MONTH) -1) &&
		range[MONTH].get( c.get(Calendar.MONTH)-Calendar.JANUARY) &&
		range[WEEKDAY].get( day_week ) ;
	}
	/**
	* Roll time part of calendar forward to valid one, may change date
	* part of calendar if current day has no longer had satisfactory time
	*
	*/
	private void rollToNextValidTime(Calendar c){
		int om,oh,nm,nh;// old second,minute,hour and new ones
        Calendar co=  new GregorianCalendar();
        co.setTime(c.getTime());

		om= c.get(Calendar.MINUTE);
        // clear second and add one minute
        c.set(Calendar.SECOND,0);
        c.add(Calendar.MINUTE,1);

		nm= getNextValidValue(MINUTE, c.get(Calendar.MINUTE));
		c.set(Calendar.MINUTE,nm);

		if( nm <= om  && co.get(Calendar.HOUR_OF_DAY)== c.get(Calendar.HOUR_OF_DAY)) c.add(Calendar.HOUR_OF_DAY, 1);

		oh=c.get(Calendar.HOUR_OF_DAY);

		if( range[HOUR].get(oh) ) return;// oh is valid

        c.set(Calendar.MINUTE, getFirstValidValue(MINUTE));
		nh= getNextValidValue(HOUR, oh);
		c.set(Calendar.HOUR_OF_DAY,nh);
        // &&  co.get(Calendar.DATE)== c.get(Calendar.DATE)
		if( nh <= oh ) {
            c.add(Calendar.DATE,1);
        }

	}
	/**
	* to a readable string, such as "1月或8月的9号到12号，并且是星期六或星期天的00:00
	*
	*/
	public String getPlainMeaning(){
        String s="";
        boolean isEveryMonth=plainMeaning[MONTH].equals("every");
        boolean isEveryDay=plainMeaning[MONTHDAY].equals("every");
        boolean isEveryWeekDay=plainMeaning[WEEKDAY].equals("every");
        boolean isEveryHour=plainMeaning[HOUR].equals("every");
        boolean isEveryMinute=plainMeaning[MINUTE].equals("every");
        if(isEveryMonth){
            if(isEveryDay){
                if( isEveryWeekDay){
                    s +="每天的";
                }else{
                    s +="每周的星期"+plainMeaning[WEEKDAY]+"，";
                }
            }else{
                s +="每月的第"+plainMeaning[MONTHDAY]+"天";
                if( !isEveryWeekDay){
                    s +="（而且是星期"+plainMeaning[WEEKDAY]+"）的";
                }else{
                    s +="的";
                }
            }
        }else{
            s +=plainMeaning[MONTH]+"月的";
            if(isEveryDay){
                if( isEveryWeekDay){
                    s +="每天的";
                }else{
                    s +="每个星期"+plainMeaning[WEEKDAY]+"，";
                }
            }else{
                s +="第"+plainMeaning[MONTHDAY]+"天";
                if( !isEveryWeekDay){
                    s +="（而且是星期"+plainMeaning[WEEKDAY]+"）的";
                }
            }
        }
        if(  isEveryHour ){
            s +="每小时";
        }else{
            s +=plainMeaning[HOUR]+"时";
        }
        if( isEveryMinute){
            s +="，每分钟";
        }else{
            s +=plainMeaning[MINUTE]+"分";
        }
		return s;
	}
	/**
	* Roll the calendar to valid date. Will roll day of month forward per 1
	* step until calendar comes in a valid day.
	*
	* @param c caledar to be rolled, if c is alreay a valid day, just return.
	* @throw Exception if after searching <code>dayRange</dayRange> days
	*		after c.getTime(), no valid	date found.
	*/
	private void rollToNextValidDate(Calendar c, int dayRange) throws Exception{
		int count=0;
        boolean dayChanged= !isValidDay( c);
		while(! isValidDay( c) && count <= dayRange){
			c.add(Calendar.DATE,1);
			count ++;
		}
		if( count > dayRange) throw new Exception("Could not find a suitable day in" +
				dayRange+" days");

        if( dayChanged) setFirstValidTime(c);
	}

    private void setFirstValidTime(Calendar c){
        c.set(Calendar.MINUTE, getFirstValidValue(MINUTE));
        c.set(Calendar.HOUR_OF_DAY, getFirstValidValue(HOUR));
    }
	/**
	* get next value of specified field, input value is excluded until next loop
	*/
	private int getNextValidValue(int field, int value){

		for( int i=value;i< range[field].size();i++){
			if( range[field].get(i)) return i;
		}
		for( int i=0;i <= value;i++){
			if( range[field].get(i)) return i;
		}
		return value;
	}
    private int getFirstValidValue(int field){
        int ret=-1;
        for( int i=0;i< range[field].size();i++){
			if( range[field].get(i)){
                ret=i;
                break;
            }
		}
        return ret;
    }
	public String toString(){
 		return cron;
	}
	private final static int MINUTE=0;
	private final static int HOUR=1;
	private final static int MONTHDAY=2;
	private final static int MONTH=3;
	private final static int WEEKDAY=4;
	private final static int[] MIN_VALUES={
			0,0,1,1,1
	};
	private final static int[] MAX_VALUES={
			59,23,31,12,7
	};
    /***** test ***/
    public static void main(String[] args) {
    String s2="nds.control.event.NDSEventException: Unable to resolve nds.ejb.basicinfo.Department Resolved: nds.ejb Unresolved:basicinfo" ;


    String s="nds.util.NDSException: \n"+
"Start server side stack trace:\n"+
"java.lang.NullPointerException\n"+
"	at nds.control.ejb.command.OrderBgtShtItemModify.execute(OrderBgtShtItemModify.java:53)\n"+
"	at nds.control.ejb.CommandHandler.perform(CommandHandler.java:30)\n"+
"End  server side stack trace";
//    System.out.println(CronParser.retrieveMessage(s2));
        String[] cron=new String[]{
            // min hour date month day
               "0   0    5   6      1-3",
               "0   0    *   *      1,5",
               "0-2,44  16  2-7 6,7,9-12   1",
               "0  0    *   *      6",
        };
        Date date=new Date();
        System.out.println("current time is:"+ date);
            for( int i=0;i< cron.length;i++){
/*       try{
                CronParser cp=new CronParser(cron[i]);
                System.out.println(cp.getPlainMeaning());
                for( int j=0;j< 7;j++){
                    date= cp.getNextRunTime(date,365);
                    System.out.println("next run time for '"+ cron[i]+ "' is:"+date);
                }
       }catch(Exception e){
        e.printStackTrace();
            }*/
       }
       System.exit(1);

    }

}