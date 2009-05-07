package nds.net;

import java.util.Date;
import java.util.Properties;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.CronParser;
import nds.util.NDSException;

public class Scheduler  extends Thread{
  public final static boolean isDebug= false;
  private String cron=null;
  private String procName=null;
  private String procNameCN=null;
  private boolean run=false;
  private final int MAX_DAYS=365;
  private Properties props;
  private Logger logger= LoggerManager.getInstance().getLogger(Scheduler.class.getName());

  /**
  *the thread will accept the logic driver and the Cron rule
  *it does not contains any logic information and act only as a Schedulerr
  *according the rule
  **/
  public Scheduler(String procName,String procNameCN,String cron) throws NDSException{
    super();

    if(procName==null || cron==null)throw new NDSException(this.getClass().getName()+":process or cron is null");
    this.procName=procName;
    this.procNameCN=procNameCN;
    this.cron=cron;
    run=true;
  }
  public void init(Properties props){
      this.props= props;
  }
  //wait to next loop to stop
  public void terminate(){
    run=false;
    synchronized(this){
      this.notifyAll();
    }
    logger.debug("terminate "+procNameCN+":"+(new Date()));
  }

  //stop right now
  public void kill(){
    run=false;
    try{
      this.interrupt();
    }
    catch(Exception e){
      e.printStackTrace();
    }
    logger.debug("stop "+procNameCN+":"+(new Date()));
  }

  public void run(){
    //Util.log("start "+procName+":"+(new Date()));
      logger.debug("start "+procNameCN+":"+(new Date()));
    while(run==true){
      ThreadProcess proc=null;
      try{
        Class c= Class.forName(procName.trim());
        proc=(ThreadProcess) c.newInstance();
        proc.init(props);
      }
      catch (Exception e) {
        //new NDSException(this.getClass().getName()+":can not get process instance",e);
        logger.debug("can't find "+procNameCN+":"+(new Date()),e);
        return;
      }

      try{
        //get wait time
        Date lastDate=new Date();
        CronParser cp=new CronParser(cron);
        Date date=cp.getNextRunTime(lastDate,MAX_DAYS);
        long interval=date.getTime()-lastDate.getTime();

        //wait
        //2 possibility:1.wait until the rule 2.accept notify to stop
        //Util.log("prepare exec "+procName+":"+(new Date())+" and next start time :"+date);
        logger.debug("prepare exec "+procNameCN+":"+(new Date())+" and next start time :"+date + ", interval in seconds:" + (interval/1000));
        try{
            synchronized(this){
                //for debug
                if(!isDebug)  wait(interval);
            }
        }catch(InterruptedException ie){
            // do nothing
        }


        if(run==true){
            logger.debug("begin exec "+procNameCN+":"+(new Date())+ " "+ this.toString() );
            proc.execute();
        }else{
            logger.debug(procNameCN + " stopped.");
        }
        if(isDebug) {
            logger.debug("In debug mode, stop "+procNameCN);
            break;
        }
      }
      catch(Exception e){
        //Util.log("error in exec "+procName+":"+(new Date())+e);
        logger.debug("error in exec "+procNameCN+":"+(new Date())+ " "+ this.toString() ,e);
        //new NDSException(this.getClass().getName()+":process runnning error",e);
      }
    }// end while
  }

}
