package nds.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.MD5Sum;

/**
 * Wrapper MD5Sum to support cache checksum string in a seperate file
 */
class MD5SumUtil {
    private static Logger logger= LoggerManager.getInstance().getLogger(MD5SumUtil.class.getName());


    /**
     * Get file checksum string in MD5
     * This method will first check if file named ${filename}.sum exists in subdirectory
     * "checksum", if not exists, then generate that file containing string of MD5 result
     * and then return the MD5.
     */
    public String getFileCheckSum(File file, String cacheFile){
        String md5;

        File cf= new File(cacheFile).getParentFile() ;
        if ( ! cf.exists() ) {
            cf.mkdirs() ;
        }
        File f= new File(cacheFile);
        if ( ! f.exists()){
            // md5 file not exist, create one
            md5= createFileCheckSum(file);
            writeCheckSumFile(md5, f);
        }else{
            //JUST return the content as string
            try{
                md5= getFileContent(f);
            }catch(IOException e){
                md5= createFileCheckSum(file);
                writeCheckSumFile(md5, f);
            }
        }
        return md5;
    }
    /**
     * Create check sum string of file, if failed, return 'unknown'
     * @return "unknown" if failed, else a string of length 32
     */
    public String createFileCheckSum(File file){
        try {
            return MD5Sum.toCheckSum(file.getAbsolutePath());
        }
        catch (IOException ex) {
            return "unknown";
        }
    }
    /**
     * Write string to a new file, does not throw any exception
     */
    private void writeCheckSumFile(String md5, File file){
        try{
            FileOutputStream fos=new FileOutputStream(file);
            fos.write( md5.getBytes());
            fos.close();
        }catch(IOException e){
            logger.error("Could not write checksum "+md5+ " in " + file.getAbsolutePath(), e);
        }
    }
    /**
     * Read whole file content as string
     */
    private String getFileContent(File file) throws IOException{
        int filesize=(int) file.length();
        FileInputStream fis= new FileInputStream(file);
        byte[] data=new byte[filesize];
        fis.read(data);
        String tmp = new String(data);
        return tmp;
    }
    /**
     * if cache file exists, simply read content as checksum,
     * else caclulate that one and save to cache file
     * @param file the file to be obtained checksum
     * @param cacheFile the cache file name (full path),in which checksum string will
     *                  be set in, if is null, then no cache file will be generated
     * @return 32 char of checksum, or "unknown" if exception occcur
     */
    public static String getCheckSum(File file, String cacheFile) {
        MD5SumUtil util=new MD5SumUtil();
        if ( cacheFile ==null || cacheFile.length() ==0)
            return util.createFileCheckSum(file);
        else
            return util.getFileCheckSum(file, cacheFile);
    }

    public static void main(String[] args) {
        File f= new File("F:\\act\\posdb\\upload\\CSC001\\bb.sql.gz");
        System.out.println(getCheckSum(f,""));
    }
}