/******************************************************************
*
*$RCSfile: ConsoleLogger.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:18 $
*
*$Log: ConsoleLogger.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:18  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/

package nds.log;
import java.io.IOException;
import java.io.PrintWriter;
/**
* Write only to System.out
*/
public class ConsoleLogger extends TextStreamLogger implements Logger
{
    private int loglevel;
    private String sys;

    public ConsoleLogger()
    {
    }
    public ConsoleLogger(String system)
    {
        init( system, null, "128");
    }
    public void init(String system, String fileName, String level){
        this.sys=system;
        try{
            this.loglevel= Integer.parseInt(level);
        }catch(Exception e){
            System.out.println("Error reading '"+ level+"' as an integer");
            loglevel=128;
        }
        open();
    }
    private boolean isLog(int i)
    {
        return i >=loglevel;
    }

    private void println(int level, String s){
        try{
        if(isLog(level)){
            super.log(level, sys, System.currentTimeMillis(), s);
        }
        }catch(IOException e){
            System.err.println("Error in print:"+ e);
        }

    }
    private void println(int level,String s, Throwable throwable){
        try{
        if(isLog(level)){
            super.log(level, sys, System.currentTimeMillis(), s, throwable);
        }
        }catch(IOException e){
            System.err.println("Error in print:"+ e);
        }

    }
    public void debug(String s)
    {
        println(DEBUG, s);
    }

    public void debug(String s, Throwable throwable)
    {
        println(DEBUG, s, throwable);
    }

    public void error(String s)
    {
        println(ERROR, s);
    }

    public void error(String s, Throwable throwable)
    {
        println(ERROR, s, throwable);
    }

    public void info(String s)
    {
        println(INFO, s);
    }

    public void info(String s, Throwable throwable)
    {
        println(INFO, s, throwable);
    }

    public void warning(String s)
    {
        println(WARNING, s);
    }

    public void warning(String s, Throwable throwable)
    {
        println(WARNING, s, throwable);
    }
    public void open()
    {
        out = new PrintWriter(System.out, true);
    }
    public void close()
    {
        flush();
    }

}
