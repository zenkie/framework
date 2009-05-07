package nds.net;
import java.util.Properties;

import nds.log.Logger;
import nds.log.LoggerManager;
/**
 *  Send download request every 5 minutes, can pause and resume
 */
public class SendDownloadRequest implements Runnable{
    public static final String XMLNS_ACT_POS = "act:pos";
    private int sleepTime; // in seconds

    private Logger logger= LoggerManager.getInstance().getLogger(SendDownloadRequest.class.getName());
    private ServerManager server;
    private int status; // 1 stop, 0 go, 2 pause

    public SendDownloadRequest() {
        status=0;
    }
    public void kill(){
        status= 1;
    }
    public void pause(){
        status= 2;
    }
    public void resume(){
        status=0;
        /* send request immdiately */
        try{
            server.sendRequestDownloadMsg();
        }catch(Exception e){
            logger.debug("SendDownloadRequest Exception:" + e);
        }

    }
    public void init(Properties props){
        try{
            sleepTime= new Integer(props.getProperty("SendDownloadRequest.interval", "300")).intValue() ;
        }catch(Exception e){
            sleepTime= 300;
        }

    }
    public void setServerManager(ServerManager server){
        this.server= server;
    }
    public void run(){
        logger.debug("SendDownloadRequest started, sleep interval "+ sleepTime +" seconds interval.");
        while( status !=1){
            try{
                if (status !=2)server.sendRequestDownloadMsg();
            }catch(Exception e){
                logger.debug("SendDownloadRequest Exception:" + e);
            }
            try{
                Thread.sleep(sleepTime * 1000);
            }catch(Exception e){
                logger.debug(""+ e);
            }

        }
        logger.debug("SendDownloadRequest stopped.");
    }
}