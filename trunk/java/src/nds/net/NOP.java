package nds.net;
import nds.log.Logger;
import nds.log.LoggerManager;

import com.echomine.jabber.JabberCode;
import com.echomine.jabber.JabberSession;
import com.echomine.jabber.JabberStringMessage;
/**
 *  Do nothing but keep session alive
 */
public class NOP implements Runnable{
    private Logger logger= LoggerManager.getInstance().getLogger(NOP.class.getName());
    private final static int SLEEP_INTERVAL= 1000* 60;// one minute
    private JabberSession session ;
    private boolean bStop;
    public NOP() {
        bStop=false;
    }
    public void kill(){
        bStop=true ;
    }

    public void setSession(JabberSession session ){
        this.session= session;
    }
    public void run(){
        logger.debug("NOP started to keep session alive, interval is "+ (SLEEP_INTERVAL/1000)+ " seconds.");
        while( ! bStop){
            try{
                Thread.sleep(SLEEP_INTERVAL);
                if(session !=null && session.getConnection() !=null && session.getConnection().isConnected() ){
                    JabberStringMessage msg=new JabberStringMessage(JabberCode.MSG_CHAT ,"\n");
                    session.sendMessage(msg);
                }
            }catch(Exception e){
                logger.debug("NOP Exception:" + e);
            }

        }
        logger.debug("NOP stopped.");
    }

}