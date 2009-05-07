/******************************************************************
*
*$RCSfile: ProcedureHandler.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*
********************************************************************/
package nds.control.ejb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryException;
import nds.query.SPResult;
import nds.util.NDSException;
import nds.util.TimeLog;

/**
 * 注意，本实现仅保证在单机运行正常（对 禁止同名同参数的存储过程被同时执行）
 *
 * This class will be called when stored procedure is request to be executed.
 * The event sending will have following parameters set: <br>
 *        <ul>
 *        <li>EventName is "ProcedureEvent" </li>
 *        <li>"procedureName" - procedureName   </li>
 *        <li>"params"        - params ( if <code>params</code> is not serializable, the interal process will convert it
 *                          to a serializable one</li>
 *        <li>"hasReturnValue"- hasReturnValue(Boolean) </li>
 *        <li>"resultFile"   - absolute result file name(include path)that will store result information
 *                             the file's parent dose not nessisary exist, even its grandpa
 *          </li>
 *        <li>"operator"     - the commander of this calle
 *          <li>"singleton"    - whether allow simutaneous call or not</li>
 *        </ul>
 * This handler will write result to a file as
 * @see nds.control.ejb.DefaultWebEventHelper#executeStoredProcedure(String,Collection,boolean, boolean,boolean)
 */
public class ProcedureHandler extends StateHandlerSupport {

    private static Logger logger= LoggerManager.getInstance().getLogger(ProcedureHandler.class.getName());
    //private final static DateFormat dateFormatter =new SimpleDateFormat("yyyy 年MM 月dd 日 HH:mm:ss");
    private final static String LINE_SEPERATOR="\n\r";// windows platform

    public ValueHolder perform(NDSEvent ev) throws NDSException, RemoteException {
    	DateFormat dateFormatter =new SimpleDateFormat("yyyy 年MM 月dd 日 HH:mm:ss");
        SPResult spr=null;
        boolean hasError=false;
        boolean hasReturnValue=false;
        boolean singleton=true;
        String resultFileName=null;
        String procName=null;
        String rs=null;
        Collection params=null;
        String dateInfo = null;

        long curTime = System.currentTimeMillis();

        DefaultWebEvent event= (DefaultWebEvent) ev;
        procName=(String) event.getParameterValue("procedureName");
        params= (Collection)event.getParameterValue("params");
        hasReturnValue= ((Boolean)event.getParameterValue("hasReturnValue")).booleanValue();
        singleton= ((Boolean)event.getParameterValue("singleton")).booleanValue();
        resultFileName= (String)event.getParameterValue("resultFile");
        String oper= (String) event.getParameterValue("operator");
        // log time
        int timeLog= TimeLog.requestTimeLog("ProcedureHandler-"+procName);

        String tmpMsg = "";
        if (procName.toUpperCase().equals("MMCLOSEOFFDATESHTSUBMIT")) {
            tmpMsg = "月结第一部分";
        } else if (procName.toUpperCase().equals("OLTMMCOFBILLAMTSHTSUBMIT")) {
            tmpMsg = "月结第二部分";
        } else if (procName.toUpperCase().equals("DAYCLOSEOFFSTART")) {
            tmpMsg = "日结";
        }

        try {
            dateInfo = dateFormatter.format(new Date(curTime)) + LINE_SEPERATOR + "操作 ";
            rs = dateInfo + "执行" + LINE_SEPERATOR + "信息 " + tmpMsg + "开始！";
            writeToFile(resultFileName, LINE_SEPERATOR + "-----------------------------------------" +
                    LINE_SEPERATOR + rs, true);

            // disallow simutaneous execution of same proc if singleton is requested
            RunningProcedureInfo rpi = getRunningProcedureCache().getRunningProcedureInfo(procName, params);
            if ( singleton && rpi !=null) {
                hasError=true;
                Date startTime= rpi.startTime;
                String theUser= rpi.commander;
                rs="失败"+LINE_SEPERATOR+"信息 相同的请求正在执行中，请稍后再试（由用户"+theUser+"于"+dateFormatter.format(startTime)+"开始运行).";
            }else{
                if( singleton){
                    registerStoredProcedure(procName,params, oper);
                }
                spr=execProcedure(procName,params,hasReturnValue);
            }// end if
        } catch(Exception e) {
            logger.error("Errors found execute event:"+ev, e);
            hasError=true;
            rs = "失败"+LINE_SEPERATOR+"信息 在执行后台请求时发现异常，请确认系统安装和配置正确.";
        } finally {
            unregisterStoredProcedure(procName,params);
        }
        long endTime=System.currentTimeMillis();
        dateInfo =dateFormatter.format(new Date(curTime))+" - "+
                  dateFormatter.format(new Date(endTime))+
                  "  ("+  ((endTime - curTime)/1000)+" 秒) "+LINE_SEPERATOR+"操作 ";

        if( hasError){
            rs=dateInfo +rs;
        }else{
            if( hasReturnValue){
                if( spr.isSuccessful()){
                    rs= dateInfo+"成功"+LINE_SEPERATOR+"信息 "+ tmpMsg + spr.getMessage();
                }else{
                    rs= dateInfo+"失败"+LINE_SEPERATOR+"信息 "+ tmpMsg + spr.getDebugMessage();
                }
            }else{
                rs= dateInfo+"成功"+LINE_SEPERATOR+"信息 "+"无";
            }
        }
        try{
            writeToFile(resultFileName,  LINE_SEPERATOR+"-----------------------------------------"+
                  LINE_SEPERATOR+ rs, true/*append*/);
            File p= new File(resultFileName);
            String path= p.getParent();
            String name= p.getName();
            String descFileName= path+"/desc/"+ name;
            logger.debug("Describe file:"+ descFileName);
            writeToFile(descFileName,rs, false);
        }catch(IOException eo){
            logger.error("Error write to file"+resultFileName, eo );
        }
        // log the information
        logger.debug("Stored Procedure '"+procName+" called, time elapsed in effective operation:"+
            ((endTime - curTime)/1000)+" seconds");
        TimeLog.endTimeLog(timeLog);
        return  null;
    }
    /**
     * use protected just for testing
     */
    protected SPResult execProcedure(String procName,Collection params,boolean hasReturnValue) throws QueryException{
        logger.debug("@@ execProcedure(");
        DefaultWebEventHelper helper=new DefaultWebEventHelper();
        return helper.executeStoredProcedure(procName,params,hasReturnValue);
    }
    private void registerStoredProcedure(String procedureName, Collection params, String commander){
        RunningProcedureInfo info=getRunningProcedureCache().put(procedureName,params ,commander);
        logger.debug( "Registered "+info);
    }
    private void unregisterStoredProcedure(String procedureName, Collection params){
        RunningProcedureInfo info=getRunningProcedureCache().remove(procedureName,params );
        logger.debug( "Unregistered "+info);
    }
    private void writeToFile(String fileName, String content, boolean append) throws IOException{
        //first , ensure file path exist, if not exist, create one
        File parentFile= (new File(fileName)).getParentFile();
        if(! parentFile.exists()) parentFile.mkdirs();
        // write to file
        FileOutputStream  fos=new FileOutputStream(fileName,append/*append*/);
        fos.write(content.getBytes());
        fos.flush();
        fos.close();
    }
    private static RunningProcedureCache instance=null;
    private static synchronized RunningProcedureCache getRunningProcedureCache(){
            if( instance ==null){
                instance= new RunningProcedureCache();
            }
            return instance;
    }

}

class RunningProcedureCache {
    private Hashtable procs;//key object generated by gentKey(), value: RunningProcedureInfo
    RunningProcedureCache(){
        procs=new Hashtable();
    }
    /**
     * One thing must be confirmative: same procedureName with same parameters
     * should return same key
     */
    private Object genKey( String procedureName, Collection params){
        RunningProcedureInfoKey key= new RunningProcedureInfoKey(procedureName, params);
        return key;
    }
    /**
     * @return null if procedure not found in cache
     */
    public RunningProcedureInfo getRunningProcedureInfo( String procedureName, Collection params){
        Object key=genKey( procedureName, params);
        return (RunningProcedureInfo)procs.get(key);
    }
    public RunningProcedureInfo put(String procedureName, Collection params, String commander){
        Object key=genKey( procedureName, params);
        RunningProcedureInfo rpi=new RunningProcedureInfo();
        rpi.commander= commander;
        rpi.procedureName= procedureName;
        rpi.startTime= new Date();
        procs.put(key, rpi);
        return rpi;
    }
    public RunningProcedureInfo remove( String procedureName, Collection params){
        Object key=genKey( procedureName, params);
        RunningProcedureInfo rpi= (RunningProcedureInfo)procs.get(key);
        procs.remove(key);
        return rpi;
    }
}

class RunningProcedureInfoKey {
    private String proc;
    private Collection ps;
    private int hash;
    public RunningProcedureInfoKey(String procedureName, Collection params){
        proc= procedureName;
        ps= params;
        hash= procedureName.hashCode();
    }
    public int hashCode(){
        return hash;
    }
    public boolean equals(Object x){
        if (!(x instanceof RunningProcedureInfoKey))
            return false;
        RunningProcedureInfoKey rpi = (RunningProcedureInfoKey) x;
        return ( proc.equals(rpi.proc) && ps.equals( rpi.ps) );
    }
}

class RunningProcedureInfo {
    String procedureName, commander;
    Date startTime;
    public String toString(){
        return procedureName+" by "+ commander+" at "+ startTime;
    }
}
