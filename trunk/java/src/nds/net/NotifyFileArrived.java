package nds.net;
import java.io.File;

import nds.util.CommandExecuter;
import nds.util.Tools;

/**
在posNet发送DBImport请求后，ndsJava发出此命令给ndsMgr，
  首先暂停请求后续文件（在结束后再开始）
  如果文件已经被处理过，则无须再行处理（防止有多次操作）
  然后下载文件到本地，进行CheckSum矫检（如果ServerManager和SyncManager装在同一台机器上，则不需要下载
  执行数据库写
  只有以上动作全部正确，才将此文件标志为已经处理过

  msg参数
  ClientName:客户端名称
  FileName:String
  FileCheckSum: String
  DownloadURL: String
  TotalFileLength: int
*/
public class NotifyFileArrived extends AbstractSessionListener{

    // UploadRootDir is for local file system
    private String  uploadRootDir,tmpDir , impCmd, ftpGetCmd;
    private boolean shareFileSystemWithRelay;
    private ServerManager server;

    public NotifyFileArrived() {
    }

    public void setController(SessionController controller){
        super.setController(controller);
        if (! (controller.getCommander() instanceof ServerManager))
            throw new Error("Internal Error: NotifyFileArrived should have ServerManager in controller.");
        server=(ServerManager) controller.getCommander();
        uploadRootDir =controller.getAttribute("PosDB.Upload.RootDir", "f:/act/posdb/upload");
        tmpDir =controller.getAttribute("PosDB.TmpDir", "f:/act/posdb/tmp");
        impCmd =controller.getAttribute("PosDB.Import.Command", "sh f:/impora");
        ftpGetCmd=controller.getAttribute("PosDB.FTPGet.Command", "sh f:/ftpget");
        try{
            shareFileSystemWithRelay= (new Boolean(controller.getAttribute("PosDB.ShareDir", "false"))).booleanValue();
        }catch(Exception e){
            logger.error("Could not read property 'PosDB.ShareDir' as boolean: " + controller.getAttribute("PosDB.ShareDir", "false"));
            shareFileSystemWithRelay=false;
        }
    }

    public synchronized void onMessage(SessionMsg msg){
        pauseRequestFileDownload();
try{
        int resultCode=0;
        String resultString="OK";

        String clientName= msg.getParam("ClientName");
        // relative to uploadRootDir+ "/" + clientName
        String fileName= msg.getParam("FileName");
        //如果这个文件与上次处理成功的文件名称一致，就不进行处理
        if(fileHandled(clientName, fileName)==true) return;

        String downloadURL= msg.getParam("DownloadURL");
        logger.debug("NotifyFileArrived request from " + clientName+", file:" + fileName );
        // command
        String outputFileName=tmpDir +"/" + clientName + "_"+ fileName+".out";

        // file like "c:/tmp/12.sql.gz
        String fullFileName= (uploadRootDir+ "/" + clientName+"/" +fileName);
        if(!shareFileSystemWithRelay){

            // download file
            if( download(clientName, fileName, downloadURL, fullFileName) != 0){
                return;
            }
            String checkSum= msg.getParam("FileCheckSum");
            if( checkSum==null || checkSum.length() !=32 ){
                logger.debug("FileCheckSum not found or invalid:"+ checkSum);
                return;
            }else{
                // compare checksum
                String fck=MD5SumUtil.getCheckSum(new File(fullFileName),"");
                //System.out.println("Checksum for file:"+ fileName + " is " + fck);
                if ( fck.length() == 32){
                    if ( !fck.equalsIgnoreCase(checkSum)){
                        // error file upload
                        logger.debug("MD5 Checksum error for file " + fullFileName + ", local=" + fck +", while remote=" +checkSum) ;
                        return;
                    }
                }
            }
        }// end  if !shareFileSystemWithRelay

        // start a command
        CommandExecuter exec= new CommandExecuter(outputFileName);
        String cmd= impCmd + " " + fullFileName;
        try{
            resultCode=exec.run(cmd);
            if ( resultCode != 0) {
                logger.error("Error when execute "+ cmd+ "  Internal Error[code=" + resultCode+"]");
            }else{
                //只有当数据库被执行后，此文件才会被标记处理成功
                markFileHandled(clientName, fileName);
                // yfzhu 2004-01-15 found data would still be error inside the sql file partly
                // you should check the error one
                /* not implemented yet! */
            }
            logger.debug("Result in executing " +cmd+":" + Tools.getFileContent(outputFileName ));

        }catch(Exception e){
            logger.error("Error executing " + cmd, e);
            resultCode = 1;
        }
}finally{
        resumeRequestFileDownload();
}
    }

    private void pauseRequestFileDownload(){
        server.pauseRequestFileDownload() ;
    }
    private void resumeRequestFileDownload(){
        server.resumeRequestFileDownload();
    }
    private void markFileHandled(String clientName, String fileName){
        server.markFileHandled(clientName, fileName);
    }
    private boolean fileHandled(String clientName, String fileName){
        return server.fileHandled(clientName, fileName);
    }
    /**
     * @param clientName remote path, such as "CSA023"
     * @param remoteFile remote file name, not including any path info
     * @param remoteURL the full url info, such like "ftp://yfzhu:abcer@mit/posdb/down.sql.gz"
     * @param local local file name, include all path
     * @return result code of executing, 0 for OK
     */
    private int download(String clientName,String remoteFile, String remoteURL, String local){
        // start a command
        String outputFileName=tmpDir +"/" + clientName + "_"+ remoteFile+".ftp.out";
        CommandExecuter exec= new CommandExecuter(outputFileName);
        int resultCode=0;
        try{
            java.net.URL url=new java.net.URL(remoteURL);
            String host= url.getHost();
            String remotePath= url.getPath(); // this include the filename, like /posdb/down.sql.gz
            createFolder(local);
            String userInfo= url.getUserInfo();// this include username and password
            String userName= userInfo.substring(0, userInfo.indexOf(":"));
            String passwd= userInfo.substring( userInfo.indexOf(":")+1);
            String cmd= this.ftpGetCmd  + " " + host + " "+ userName + " "+ passwd + " "+ remotePath+ " "+local;
            resultCode=exec.run(cmd);
            if ( resultCode != 0) {
                logger.error("Error when execute "+ cmd+ "  Internal Error[code=" + resultCode+"]");
            }
            logger.debug("Result in executing " +cmd+":" + Tools.getFileContent(outputFileName ));

        }catch(Exception e){
            logger.error("Error executing downlad (clientName=" +clientName+ ", remoteFile="+ remoteFile + ", remoteURL="+ remoteURL+", local="+ local, e);
            resultCode = 1;
        }
        return resultCode;

    }
    /**
     * @param path which is a file
     */
    private void createFolder(String path){
        try{
            File file= (new File(path)).getParentFile();
            if (!file.exists() ) file.mkdirs() ;
        }catch(Exception e){
           logger.debug("Can not create folder", e);
        }
    }

}