package nds.net;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import com.echomine.jabber.JabberChatMessage;


/**
 * Client ask for download db, incoming message should have following params set<p>
 * "CommandType"= "RequestDBDownload"<br>
 * "ClientName"=m_controller.getAttribute("username")<br>
 * "LastDownloadFileID" (int) <br>
 * "GetTotalFileLength" (optional) if specified "true", then "TotalFileLength" will return
 * <p>
 * While the outgoing message should have these params: <p>
 * "RemoteFileID"=(Long, as "11") the next file id to be downloaded after $LastDownloadFileID<br>
 * "DownloadURL"=(String as "http://www.sina.com/file/path") the download file path <br>
 * "HasMoreFiles" = "String "TRUE" | "FALSE" means whether has more files to be downloaded or not<br>
 * "FileCheckSum" = 32 char MD5 String
 * "FileCount" =  files to be downloaded, at least is one when HasMoreFiles= true
 * "TotalFileLength" only when "GetTotalFileLength" (optional) specified in incoming message
 */
public class RequestDBDownload extends AbstractSessionListener{
    /**
     * Extension of file containing table info
     */
    public final static String DOWNLOADFILE_EXTENSION=".gz";
    // downloadRootDir is for local file system, while downloadRootURL is the ftp addr of the downloadRootDir
    private String  downloadRootDir, downloadRootURL ,sessionMsgPrefix;
    /**
     * Filter files with only specified extension, and name "bigger" that specified one
     */
    private class FileSeacher implements java.io.FilenameFilter {
        private File dir;
        private String ext;
        private String baseName;//the file name that all satisfied files' name should be "bigger" than this
        public FileSeacher(File dir, String extension, String baseName) {
            this.dir=dir;
            this.ext=extension;
            this.baseName= baseName;
        }
        public boolean accept(File directory, String name) {
            if( dir.equals(directory) && name.toLowerCase().endsWith(ext) && name.compareTo(baseName)>0){
//                logger.debug(name + " is accepted.");
                return true;
            }else{
//                logger.debug(name + " is rejected.");
                return false;
            }
        }
    }
    public RequestDBDownload() {
    }

    public void setController(SessionController controller){
        super.setController(controller);
        downloadRootDir =controller.getAttribute("PosDB.Download.RootDir", "f:/act/posdb/download");
        downloadRootURL=controller.getAttribute("PosDB.Download.RootURL", "ftp://anonymous:aaa@localhost/posdb/download");
        sessionMsgPrefix= controller.getCommander().getSessionMsgPrefix();
    }
    public void onMessage(SessionMsg msg){
        String clientName= msg.getParam("ClientName");
        String lastDownloadFileID= msg.getParam("LastDownloadFileID");
//        logger.debug("RequestDBDownload request from " + clientName+", LastDownloadFileID:" + lastDownloadFileID );

        // yfzhu added 2003-11-23 to insert table poslog
        notifyClientStatus(clientName, "LastDownloadFile", lastDownloadFileID);

        SessionMsg out = new SessionMsg(sessionMsgPrefix);
        JabberChatMessage jcm= msg.getOrigionalJabberMessage();
        out.setParentMsgID(msg.getID()  );
        out.setThreadID(msg.getThreadID()) ;
        // get download file url
        String downloadURL,remoteFileID,hasMoreFiles, fileCount, checkSum ;

        // list only file with specified extension
        File dir= new File(downloadRootDir+ "/" + clientName);

        FilenameFilter filter=new FileSeacher(dir,DOWNLOADFILE_EXTENSION,lastDownloadFileID );
        File[] files=dir.listFiles(filter);
        if( files ==null || files.length==0) {
//            logger.debug("Could not find any files bigger than " + lastDownloadFileID+ " in directory:"+ dir);
            out.addParam("RemoteFileID", ""); // this will tell client that no more files to be downloaded
            out.addParam("FileCount", "0");
            controller.sendMsg(out, jcm.getFrom() ,jcm.getThreadID());
            return;
        }
        files= sort(files);
        hasMoreFiles= files.length > 1 ? "TRUE": "FALSE";
        fileCount= files.length +""; // file to be downloaded, added at 2003-06-11 by yfzhu
        remoteFileID= files[0].getName() ;
        checkSum= MD5SumUtil.getCheckSum(files[0], files[0].getParentFile().getAbsolutePath() + "/checksum/"+ files[0].getName() +".sum");
        downloadURL = downloadRootURL +  "/" + clientName+ "/" + remoteFileID;
        out.addParam("DownloadURL",downloadURL );
        out.addParam("RemoteFileID", remoteFileID);
        out.addParam("FileCheckSum", checkSum);// file MD5 checksum, added at 2003-06-11 by yfzhu);
        out.addParam("HasMoreFiles", hasMoreFiles);
        out.addParam("FileLength", "" + files[0].length());
        out.addParam("FileCount", fileCount); // file to be downloaded, added at 2003-06-11 by yfzhu);
        if(  "true".equalsIgnoreCase(msg.getParam("GetTotalFileLength")))
            out.addParam("TotalFileLength", ""+getTotalLength( files));
        controller.sendMsg(out, jcm.getFrom() ,jcm.getThreadID());
    }
    /**
     * Get total file length
     */
    private long getTotalLength(File[] f){
        long l=0;
        for(int i=0;i< f.length;i++) l += f[i].length();
        return l;
    }
    private File[] sort(File[] files) {
        Vector v=new Vector();
        for( int i=0;i< files.length;i++) {
            v.addElement(files[i]);
        }
        nds.util.ListSort.sort(v);
        File[] fs=new File[ v.size()];
        for( int i=0;i< fs.length;i++) {
            fs[i]= (File) v.elementAt(i);
        }
        return fs;
    }


}