package nds.net;
import java.io.File;

import com.echomine.jabber.JabberChatMessage;


/**
 * Client ask for upload db, incoming message should have following params set<p>
 * Normally this one is ahead of RequestDBDownload and UpdateActivePOS
 * "CommandType"= "RequestDBUpload"<br>
 * "ClientName"=m_controller.getAttribute("username")<br>
 * <p>
 * While the outgoing message should have these params: <p>
 * "UploadURL"=(String as "http://www.sina.com/file/path") the upload file path <br>
 */
public class RequestDBUpload extends AbstractSessionListener{
    // uploadRootURL is the ftp addr of the uploadRootDir
    private String  uploadRootURL, uploadRootDir,sessionMsgPrefix;
    private SyncManager manager ;
    public RequestDBUpload() {
    }

    public void setController(SessionController controller){
        super.setController(controller);
        uploadRootURL=controller.getAttribute("PosDB.Upload.RootURL", "ftp://anonymous:aaa@localhost/posdb/upload");
        uploadRootDir=controller.getAttribute("PosDB.Upload.RootDir", "f:/act/posdb/upload");

        sessionMsgPrefix= controller.getCommander().getSessionMsgPrefix();
        manager= (SyncManager)controller.getCommander();
    }
    public void onMessage(SessionMsg msg){
        String clientName= msg.getParam("ClientName");
        String uploadFile= msg.getParam("UploadFile");
        logger.debug("RequestDBUpload request from " + clientName+", File to be uploaded:" + uploadFile );

        SessionMsg out = new SessionMsg(sessionMsgPrefix);
        JabberChatMessage jcm= msg.getOrigionalJabberMessage();
        out.setParentMsgID(msg.getID()  );
        out.setThreadID(msg.getThreadID()) ;
        // get upload file url
        String uploadURL;
        uploadURL = uploadRootURL +  "/" + clientName +"/" + uploadFile;
        createFolder( uploadRootDir+ "/" + clientName);

        out.addParam("UploadURL",uploadURL );
        controller.sendMsg(out, jcm.getFrom() ,jcm.getThreadID());

        // mark file for deny
        manager.getRelayManager().addFile(clientName, uploadFile, "D");

    }
    private void createFolder(String path){
        try{
            File file= new File(path);
            if (!file.exists() ) file.mkdirs() ;
        }catch(Exception e){
           logger.debug("Can not create folder", e);
        }
    }


}