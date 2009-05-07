package nds.net;

import java.io.File;
import java.util.Date;

import nds.log.Logger;
import nds.log.LoggerManager;

/**
 * check directory of cvs root every 10 minutes
 *
 */
class CVSUpdateCheck implements Runnable{
    private Logger logger= LoggerManager.getInstance().getLogger(this.getClass().getName());
    private final static int SLEEP_INTERVAL=60*1000*10;
    private String recentUpdateTime;
    private boolean pleaseStop=false;
    private String cvsRoot;
    public  CVSUpdateCheck(String cvsRoot){
        // this is default
        this.cvsRoot = cvsRoot;
        recentUpdateTime=UpdateActivePOS.dateTimeSecondsFormatter.format((new java.util.Date()));
    }
    public String getRecentUpdateTime(){
        if (pleaseStop==true ){
            recentUpdateTime= UpdateActivePOS.dateTimeSecondsFormatter.format((new java.util.Date()));
        }
        return recentUpdateTime;
    }    public void kill(){
        pleaseStop=true;
    }
    public void run(){
        while ( !pleaseStop){
        try{
            recentUpdateTime=UpdateActivePOS.dateTimeSecondsFormatter.format(
                    new Date(getDirectoryLastModificationTime(new File(cvsRoot))));

            logger.debug("Check cvs update time at " + UpdateActivePOS.dateTimeSecondsFormatter.format((new java.util.Date()))+
                           ", result is " + recentUpdateTime);
        }catch(Exception e){
            logger.error("During check cvs update" ,e);
        }finally{
            try{
                //sleep 5 minutes
                Thread.sleep(SLEEP_INTERVAL);
            }catch(Exception e3){}
        }
        }
        logger.debug("CVS Checker stopped.");
    }
    /**
     * Find the last modified time of all files in directory
     */
    private long getDirectoryLastModificationTime(File f){
        long max=0, d;
        if ( f.isDirectory() ){
            // check each sub dir and file
            File[] fs=f.listFiles();
            for(int i=0;i< fs.length;i++){
                d= getDirectoryLastModificationTime(fs[i]);
                if( max < d) max=d;
            }
        }else if( f.isFile() ){
            max=f.lastModified() ;
        }
        return max;

    }
}
