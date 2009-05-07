package nds.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
/**
 Check file such as CSJ005_EXP1004213836.sql.gz.out, that is the result of pl/sql script
 * if found error/ora- words in it, copy that file to /home/nds/var/errlog directory
*/
public class LogCheck {
    public final static String DEFAULT_PROPERTY_FILE="/logcheck.properties";
    private final static String TABLE_EXTENSION="sql.gz.out";
    private transient Logger logger= LoggerManager.getInstance().getLogger(LogCheck.class.getName());
    Configurations conf;
    private String errDir="/home/nds/var/errlog";
    private String checkDir="/home/nds/posdb/tmp";
    public LogCheck(){}
    public LogCheck(Configurations conf) {
        this.conf= conf;
    }
    public void start() throws Exception{
        String directory=checkDir ;
        File dir= new File(directory);
        // list only file with specified extension
        FilenameFilter filter=new FileSeacher(dir,TABLE_EXTENSION);
        File[] files=dir.listFiles(filter);
        if( files ==null || files.length==0) {
            logger.debug("Could not find any tables in directory:"+ dir);
            return;
        }
        //files=sort( files); // no need anymore, SchemaStrcuture will do that
        for( int i=0;i< files.length;i++) {
            try{
                checkLog( files[i]);
            }catch(Exception e){
                logger.error("Error found when init file:"+ files[i], e);
            }
        }

    }

    public void checkLog(File file) throws Exception {
        int filesize=(int) file.length();
        FileInputStream fis= new FileInputStream(file);
        byte[] data=new byte[filesize];
        fis.read(data);
        String content =   new String(data);
        if ( content.indexOf("ERROR ")> 0 || content.indexOf("ORA-")>0){
            logger.debug("Found file erro "+ file.getName());
            org.apache.tools.ant.util.FileUtils.newFileUtils().copyFile(file, new File(errDir+"/"+ file.getName()));
        }
    }
    private static void usage() {
        System.err.println("Usage:\n  java nds.util.LogCheck [-p properties]");
        System.err.println("\nOptions:");
        System.err.println("  -p : indicates the property file");
        System.exit(1);
    }

    public static void main(String[] argument) throws Exception{
        LogCheck m=null;
        try{
          String propfile = DEFAULT_PROPERTY_FILE;

          if(argument !=null)for (int i = 0; i < argument.length; i++) {
            if (argument[i].equals("-p")) {
              if (i + 1 < argument.length){
                  propfile = argument[i+1];
                  break;
              }
            }
          }
          //if(propfile==null) propfile=DEFAULT_PROPERTY_FILE;
          System.setProperty("applicationPropertyFile",propfile);
          InputStream is= new FileInputStream(propfile);
          Configurations confs = new Configurations(is);
          LoggerManager.getInstance().init(confs.getProperties(),true);
          m= new LogCheck(confs);

      }catch(Exception e){
          e.printStackTrace();
          usage();
          return;
       }
       m.start();
    }
    private static final String LINE_SEPERATOR=System.getProperty("line.separator");
    /**
     * Filter files with only specified extension
     */
    private class FileSeacher implements java.io.FilenameFilter {
        private File dir;
        private String ext;
        public FileSeacher(File dir, String extension) {
            this.dir=dir;
            this.ext=extension;
        }
        public boolean accept(File directory, String name) {
            if( dir.equals(directory) && name.toLowerCase().endsWith(ext))
                return true;
            return false;
        }
    }

}