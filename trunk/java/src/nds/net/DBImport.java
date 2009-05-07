package nds.net;
import java.io.File;

import com.echomine.jabber.JabberChatMessage;

/**
 * Executing a seperate system process using util.CommandExecuter, logging the command output
 * to a temporary file, and check result.
 *
 * Client ask for import db, incoming message should have following params set<p>
 * "CommandType"= "DBImport"<br>
 * "ClientName"=m_controller.getAttribute("username")<br>
 * "File" (String) the file to be imported, file only has a name, without path<br>
 * "FileCheckSum" the file check sum string to make sure the file is expected
 * <p>
 * While the outgoing message should have these params: <p>
 * "ResultCode"=(Long, as "0") if code <> 0, then the process failed, else means successful<br>
 * "ResultString"=(String) Detail Information about the import result, for vb Client to display<br>
 */
public class DBImport extends AbstractSessionListener{

    // UploadRootDir is for local file system
    private String  uploadRootDir,tmpDir , impCmd,sessionMsgPrefix;
    private SyncManager manager;
    public DBImport() {
    }

    public void setController(SessionController controller){
        super.setController(controller);
        uploadRootDir =controller.getAttribute("PosDB.Upload.RootDir", "f:/act/posdb/upload");
        tmpDir =controller.getAttribute("PosDB.TmpDir", "f:/act/posdb/tmp");
        impCmd =controller.getAttribute("PosDB.Import.Command", "sh f:/impora");
        sessionMsgPrefix= controller.getCommander().getSessionMsgPrefix();
        manager= (SyncManager)controller.getCommander();
    }
    public void onMessage(SessionMsg msg){
        SessionMsg out = new SessionMsg(sessionMsgPrefix);
        JabberChatMessage jcm= msg.getOrigionalJabberMessage();
        out.setParentMsgID(msg.getID()  );
        out.setThreadID(msg.getThreadID()) ;

        int resultCode=0;
        String resultString="OK";

        String clientName= msg.getParam("ClientName");
        // relative to uploadRootDir+ "/" + clientName
        String fileName= msg.getParam("File");
        if (fileName.endsWith(".sql")) fileName += ".gz";
        logger.debug("DBImport request from " + clientName+", file:" + fileName );
        // command
        String outputFileName=tmpDir +"/" + clientName + "_"+ fileName+".out";

        // file like "c:/tmp/12.sql.gz
        String fullFileName= (uploadRootDir+ "/" + clientName+"/" +fileName);
        String checkSum= msg.getParam("FileCheckSum");
        if( checkSum==null || checkSum.length() !=32 ){
            logger.debug("FileCheckSum not found or invalid:"+ checkSum);
        }else{
            // compare checksum
            String fck=MD5SumUtil.getCheckSum(new File(fullFileName),"");
            //System.out.println("Checksum for file:"+ fullFileName + " is " + fck);
            if ( fck.length() == 32){
                if ( !fck.equalsIgnoreCase(checkSum)){
                    // error file upload
                    // return result
                    out.addParam("ResultCode","99" );
                    out.addParam("ResultString", "MD5 Checksum error for file "+  fileName + ", please check.");
                    logger.debug("MD5 Checksum error for file " + fileName + ", local=" + fck +", while remote=" +checkSum) ;
                    controller.sendMsg(out, jcm.getFrom() ,jcm.getThreadID());
                    return;
                }
            }
        }

        // start a command
/*        CommandExecuter exec= new CommandExecuter(outputFileName);
        String cmd= impCmd + " " + fileName;
        try{
            resultCode=exec.run(cmd);
            if ( resultCode != 0) resultString= "Internal Error[code=" + resultCode+"]";
            logger.debug("Result in executing " +cmd+":" + Tools.getFileContent(outputFileName ));
        }catch(Exception e){
            logger.error("Error executing " + cmd, e);
            resultString="Error in executing:" + cmd;
            resultCode = 1;
        }
*/

        manager.getRelayManager().removeFile(clientName, fileName,0 /* normal maintain*/);
        manager.getRelayManager().addFile(clientName, fileName, "P");

        // return result
        out.addParam("ResultCode","0" );
        out.addParam("ResultString", "File will be handled.");
        controller.sendMsg(out, jcm.getFrom() ,jcm.getThreadID());
    }

}