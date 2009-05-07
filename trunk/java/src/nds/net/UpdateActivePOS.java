package nds.net;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import nds.util.Tools;

import com.echomine.jabber.JabberChatMessage;
/**
 * Update Active POS application, incoming message should have following params set<p>
 * "CommandType"= "UpdateActivePOS"<br>
 * "ClientName"=m_controller.getAttribute("username")<br>
 * "LocalVersion"=controller.getAttribute("version", "001")<br>
 * <p>
 * While the outgoing message should have these params: <p>
 * "LatestVersion"=(Long, as "1021")<br>
 * "DownloadURL"=(String as "http://www.sina.com/file/path")<br>
 * "FileCheckSum" = 32 char( or "unknown" if not found)
 * "FileLength"
 *
 * 2003-07-01 add CVS support
 *  "CVSROOT"  like ":pserver:Teddy@anson:e:/cvsroot"
 *  "CVSPOSModule"  like "pos"
 *  "CVSRecentUpdateTime" the last time that cvs repository being changed, so
 *   vb client can compare the update time with local time to decide whether
 *   invoke cvs co or not. This value will be retrieved from database table
 *   "appsetting" periodically. format like "2003-06-01 11:33:59"
 *
 * 2003-08-15 add support for seperate pos updating
 *  if there's "true" for "LimitSpecialUpdateClients", then only clients in
 *  list of "SpecialUpdateClients" will be notified of new version, other will
 *  get 0 which is the lowerest version no.
 *  like:
 * "LimitSpecialUpdateClients"="true"
 * "SpecialUpdateClients"="CSA002,CGA394" (seperated by comma)
 *       if "*" used then all client will be updated except SpecialNoUpdateClients
 * "SpecialNoUpdateClients"= "CSJ005,CSJ007,CSJ029,COJ001,ZZZ001"
 *
 */
public class UpdateActivePOS extends AbstractSessionListener{
    private String lastestVersion, downloadURL;
    String checkSum, fileLength, cvsRoot, cvsPOSModule ,cvsPassword,sessionMsgPrefix;
    String cvsFileRoot; // where holding cvs root directory
    private CVSUpdateCheck checker=null;
    private boolean isLimitToSpecialUpdateClients= false;
    private String[] specialUpdateClients=null;
    private String[] specialNoUpdateClients=null;
    public final static SimpleDateFormat dateTimeSecondsFormatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public UpdateActivePOS() {
    }
    /**
     * Convert string to array, string should be seperated by specified char
     */
    private String[] toArray(String s, String delimiter){
        String[] a=null;
        StringTokenizer st=new StringTokenizer(s,delimiter);
        ArrayList al=new ArrayList();
        while(st.hasMoreTokens()){
            al.add(st.nextToken() );
        }
        a=new String[al.size() ];
        for(int i= 0;i< al.size();i++)a[i]=(String)al.get(i);
        return a;
    }
    public void setController( SessionController controller){
        super.setController(controller);
        lastestVersion= controller.getAttribute("ActivePOSVersion", "2");
        downloadURL =controller.getAttribute("ActivePOSURL", "ftp://anonymous:mail@sina.com@localhost/download/setup.exe");
        checkSum =controller.getAttribute("ActivePOSCheckSum", "unknown"); // "unknown" is keyword
        fileLength =controller.getAttribute("ActivePOSFileLength", "-1");
        cvsRoot= controller.getAttribute("CVSROOT", ":pserver:error@error:/error");
        cvsFileRoot=controller.getAttribute("CVSDir", "/cvs");
        cvsPassword= controller.getAttribute("CVSPassword", "abc123");
        cvsPOSModule =controller.getAttribute("CVSPOSModule", "pos");
        try{
            isLimitToSpecialUpdateClients=(new Boolean(controller.getAttribute("LimitSpecialUpdateClients", "false"))).booleanValue();
        }catch(Exception e){
            isLimitToSpecialUpdateClients=false;
        }
        if(isLimitToSpecialUpdateClients){
            specialUpdateClients= toArray(controller.getAttribute("SpecialUpdateClients", ""),",");
            logger.debug("Only these clients will be notified new version:"+ Tools.toString(specialUpdateClients));
            this.specialNoUpdateClients=toArray(controller.getAttribute("SpecialNoUpdateClients", ""),",");
            logger.debug("Only these clients will *NOT* be notified new version:"+ Tools.toString(specialNoUpdateClients));
        }
        sessionMsgPrefix= controller.getCommander().getSessionMsgPrefix();
    }
    /**
     *  if there's "true" for "LimitSpecialUpdateClients", then only clients in
     *  list of "SpecialUpdateClients" will be notified of new version, other will
     *  get 0 which is the lowerest version no.
     *  like:
     * "LimitSpecialUpdateClients"="true"
     * "SpecialUpdateClients"="CSA002,CGA394" (seperated by comma)
     *       if "*" used then all client will be updated except SpecialNoUpdateClients
     * "SpecialNoUpdateClients"= "CSJ005,CSJ007,CSJ029,COJ001,ZZZ001"
     *  If LimitSpecialUpdateClients="false", or not exists, then
     *   all clients will be notified version.
     *
     * @param clientName client name, if null, and LimitSpecialUpdateClients=true, then will return false
     */
    private boolean checkShouldNotifyLatestVersion(String clientName){
        if(isLimitToSpecialUpdateClients){
            if(clientName==null) return false;
            if(specialUpdateClients !=null){
                for(int i=0;i< specialUpdateClients.length; i++){
                    if(clientName.equalsIgnoreCase(specialUpdateClients[i])) return true;
                    if("*".equalsIgnoreCase(specialUpdateClients[i])){
                        // check for no update clients, if contained, return false;
                        if(this.specialNoUpdateClients !=null){
                            for(int j=0;j< specialNoUpdateClients.length;j++){
                                if(clientName.equalsIgnoreCase(specialNoUpdateClients[j])) return false;
                            }
                        }
                        return true; // all clients will be notified
                    }// end *
                }
            }
            return false;
        }else{
            return true;
        }
    }
    /** the following mesg param will be filled by client
    *   "ClientName" - Client name (shop no)
    *   "LocalVersion" - Version in mysql.appsetting."localversion"
    *   "AppVersion" - what client about dialog shows
    */
    public void onMessage(SessionMsg msg){
        String clientName=msg.getParam("ClientName");
        boolean shouldNotify= checkShouldNotifyLatestVersion(clientName);
        logger.debug("UpdateActivePOS request from " + clientName+", whose local version:" + msg.getParam("LocalVersion") );
        // yfzhu added 2003-11-09 to insert table poslog
        notifyClientStatus(clientName, "LocalVersion", msg.getParam("LocalVersion"));

        SessionMsg out = new SessionMsg(sessionMsgPrefix);
        JabberChatMessage jcm= msg.getOrigionalJabberMessage();

        out.setParentMsgID(msg.getID()  );
        out.setThreadID(msg.getThreadID()) ;
        if( shouldNotify){
            out.addParam("LatestVersion", lastestVersion);
            out.addParam("DownloadURL",downloadURL);
            out.addParam("FileCheckSum",checkSum);
            out.addParam("FileLength",fileLength);
            //        out.addParam("CVSROOT",cvsRoot);
            out.addParam("CVSPOSModule",cvsPOSModule);
            out.addParam("CVSPassword",cvsPassword);
            out.addParam("CVSRecentUpdateTime", getCVSRecentUpdateTime());
        }else{
            out.addParam("LatestVersion", "1");
            out.addParam("DownloadURL","");
            out.addParam("FileCheckSum","");
            out.addParam("FileLength","0");
            //        out.addParam("CVSROOT",cvsRoot);
            out.addParam("CVSPOSModule",cvsPOSModule);
            out.addParam("CVSPassword",cvsPassword);
            out.addParam("CVSRecentUpdateTime", getCVSRecentUpdateTime());
        }
        controller.sendMsg(out, jcm.getFrom() ,jcm.getThreadID());
    }
    /**
     * Override AbstractSessionListener, to stop internal check thread for CVSLastUpdateTime
     */
    public void kill(){
        super.kill() ;
        // stop cvs check thread
        checker.kill();
        checker=null;
    }
    public void start(){
        super.start() ;
        if (checker ==null){
            try{
                checker= new CVSUpdateCheck(cvsFileRoot);
                Thread t=new Thread(checker);
                t.setDaemon(true);
                t.start();
            }catch(Exception e){
                logger.error("Could not start cvs checker", e);
                // return cuurent time to let client check cvs every time
            }
        }

    }
    /**
     * @return string format "yyyy-MM-dd H24:MI:SS"
     */
    private String getCVSRecentUpdateTime(){
        if (checker ==null){
            try{
                checker= new CVSUpdateCheck(cvsFileRoot);
                Thread t=new Thread(checker);
                t.setDaemon(true);
                t.start();
            }catch(Exception e){
                logger.error("Could not start cvs checker", e);
                // return cuurent time to let client check cvs every time
                return dateTimeSecondsFormatter.format((new java.util.Date()));
            }
        }
        return checker.getRecentUpdateTime();
    }

}
