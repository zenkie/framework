package nds.util.test;
import java.util.Calendar;
import java.util.Date;

import nds.util.CronParser;
public class CronParserTest {

    public CronParserTest() throws Exception {
        Calendar c= Calendar.getInstance();
        c.set(2004,6,1,23,59,20);
        Date d=c.getTime();
        System.out.println(d);
        CronParser cp=new CronParser("* * * * *");
        System.out.println(cp.getNextRunTime(d, 1));
    }
    public static void main(String[] args)  throws Exception{
        CronParserTest cronParserTest1 = new CronParserTest();
    }
}