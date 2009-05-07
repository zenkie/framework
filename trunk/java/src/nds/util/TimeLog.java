package nds.util;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.*;

import nds.log.Logger;
import nds.log.LoggerManager;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

 /**
  * Log time elaps
  */
public class TimeLog {
	private static Logger logger=LoggerManager.getInstance().getLogger(TimeLog.class.getName());
	
    private static boolean doLog=true;

    class LogItem{
        String name;
        long beginTime, interval=-1;
        LogItem(String s){
            name=s;
            beginTime= System.currentTimeMillis();
        }
    }
    class ItemStat{
        long averTime=0, maxTime=0, minTime=0, count=0;
        ItemStat( long oneTime){
            averTime=oneTime;
            maxTime= oneTime;
            minTime=oneTime;
            count=1;
        }
        public String toString(){
            return "averTime="+ averTime+",maxTime="+maxTime+",minTime="+minTime+",count="+count;
        }
    }
    /**
     * Internal class to keep track of the current count of a unique id.
     */
    private final class Sequence {

        private int count;

        public Sequence(int currentCount) {
            count = currentCount;
        }

        public final synchronized int next() {
            return (++count);
        }
    }

    private Hashtable ht/*key:int, value: LogItem*/, hs/*key: String, value: ItemStat*/;
    private Sequence seq;
    private TimeLog() {
        ht=new Hashtable();
        seq=new Sequence(0);
        hs=new Hashtable();
    }
    private static TimeLog instance=null;
    private static synchronized TimeLog getInstance(){
        if( instance == null){
            instance=new TimeLog();
        }
        return instance;
    }
    /**
     * Generate a new time log item, log the begin time, when endTimeLog() executed, the
     * item will be retrieved using the item id, and total elapes time will be recorded
     * @return log item id
     */
    public int requestTimeLog0(String name){
       if(!doLog) return -1;
       int itemid= seq.next();
       ht.put( new Integer(itemid), new LogItem(name));
       return itemid;
    }
    /**
     * @param itemId id in log, was genenrated by requestTimeLog()
     */
    public void endTimeLog0(int itemId){
        if(!doLog) return ;
        LogItem item= (LogItem)ht.get(new Integer(itemId));
        if( item !=null){
            item.interval= System.currentTimeMillis() - item.beginTime;
            /*Calendar c=Calendar.getInstance(); 
            c.setTimeInMillis(item.beginTime);
            logger.debug(item.name + ":begin:" + (c.getTime())+", duration="+ (item.interval/1000));
            */
            ItemStat stat=(ItemStat)  hs.get( item.name);
            if( stat ==null){
                stat= new ItemStat(  item.interval);
                hs.put( item.name, stat);
            }else{
                stat.averTime = (stat.averTime * stat.count + item.interval)/(stat.count +1);
                if(stat.minTime > item.interval) stat.minTime= item.interval;
                if(stat.maxTime < item.interval) stat.maxTime= item.interval;
                stat.count ++;
            }
        }
    }

    /**
     * Generate a new time log item, log the begin time, when endTimeLog() executed, the
     * item will be retrieved using the item id, and total elapes time will be recorded
     */
    public static int requestTimeLog(String name){
        return getInstance().requestTimeLog0(name);
    }
    public static void endTimeLog(int id){
        getInstance().endTimeLog0(id);
    }
    public static void dumpLog(Writer  out) throws IOException{
        Hashtable table=getInstance().hs;
        Enumeration enu=table.keys();
        while( enu.hasMoreElements()){
            String s=(String)enu.nextElement();
            ItemStat st=(ItemStat) table.get(s);
            out.write(s+","+ st.minTime+","+st.maxTime+","+st.averTime+","+st.count+lineSeparator );
        }
    }
    /**
     * @return hashtable key: String tablename, value :static information(TimeLog.ItemStat),you can view as toString();
     */
    public static Hashtable getTableStats(){
        return getInstance().hs;
    }
    static String lineSeparator = System.getProperty("line.separator");

}