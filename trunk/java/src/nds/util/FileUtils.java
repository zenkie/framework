package nds.util;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class manages operation done many times by JOnAS on files, like copying
 * them.
 * Original one is: org.objectweb.jonas_lib.files.FileUtils
 * @author Florent Benoit
 */
public class FileUtils {


    /**
     * Size of the buffer.
     */
    private static final int BUFFER_SIZE = 8192;


    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(FileUtils.class);

    /**
     * Utility class, no public constructor
     */
    private FileUtils() {

    }

    /**
     * Unpack the source archive in a given directory and returns directory
     * directory created.
     * @param packedJar source JarFile to be unpacked
     * @param dest the destination folder
     * @throws NDSException When unpack fails
     */
    public static void unpack(JarFile packedJar, File dest) throws NDSException {

        JarEntry entry = null;

        // get entries of the jar file
        Enumeration entries = packedJar.entries();
        while (entries.hasMoreElements()) {
            entry = (JarEntry) entries.nextElement();

            // File entry
            File entryFile = new File(dest, entry.getName());

            // Create directory
            if (entry.isDirectory()) {
                if (!entryFile.exists()) {
                    // create parent directories (with mkdirs)
                    if (!entryFile.mkdirs()) {
                        String err = "Can not create directory " + entryFile + ", Check the write access.";
                        throw new NDSException(err);
                    }
                }
                continue;
            }

            // If it's a file, we must extract the file
            // Ensure that the directory exists.
            entryFile.getParentFile().mkdirs();

            InputStream is = null;
            // get the input stream
            try {
                is = packedJar.getInputStream(entry);
                // Dump to the file
                dump(is, entryFile);
            } catch (IOException ioe) {
                throw new NDSException("Cannot get inputstream of entry '" + entry + "' of file '" + packedJar
                        + "'.");
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                        logger.debug("Cannot close input stream", ioe);
                }
            }

        }
    }

    /**
     * Write the given input stream in the given file.
     * @param in the inputStream to copy.
     * @param entryFile the file where the inputStream must be dumped.
     * @throws NDSException if the dump failed.
     */
    private static void dump(InputStream in, File entryFile) throws NDSException {

        try {
            //File output
            FileOutputStream out = new FileOutputStream(entryFile);
            int n = 0;
            try {
                //buffer
                byte[] buffer = new byte[BUFFER_SIZE];
                n = in.read(buffer);
                while (n > 0) {
                    out.write(buffer, 0, n);
                    n = in.read(buffer);
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            String err = "Error while unpacking entry " + entryFile + " : ";
            throw new NDSException(err, e);
        }
    }
    
 
    
    /**
	 * 读取文件所有数据<br>
	 * 文件的长度不能超过Integer.MAX_VALUE
	 * @param file 文件
	 * @return 字节码
	 * @throws IOException
	 */
	public static byte[] readBytes(File file) throws IOException {
		//check
		if (! file.exists()) {
			throw new FileNotFoundException("File not exist: " + file);
		}
		if (! file.isFile()) {
			throw new IOException("Not a file:" + file);
		}
		
		long len = file.length();
		if (len >= Integer.MAX_VALUE) {
			throw new IOException("File is larger then max array size");
		}

		byte[] bytes = new byte[(int) len];
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			in.read(bytes);
		}finally {
			in.close();
		}

		return bytes;
	}
    
	/**
	 * 读取文件所有数据<br>
	 * @param 文件名
	 * @return 字节码
	 * @throws IOException
	 */
    public static byte[] getFileContent(String src) throws IOException {
    	 
    	    byte[] b =  readBytes(new File(src));
    	 
    	    return b;
    	  }
    /**
     * Get file extension, such as "pdf", "doc" 
     * @param fileName
     * @return extension of that file, return null if no extesion found
     */
    public static String getExtension(String fileName){
    	if(fileName==null) return null; 
    	int p= fileName.lastIndexOf('.');
    	if(p > 0 ) return fileName.substring(p+1);
    	return null;
    }
    /**
     * Copy a file
     * @param src source file
     * @param dest dest file
     * @throws NDSException if the copy of the file failed
     */
    public static void copyFile(String src, String dest) throws NDSException {
        copyFile(new File(src), new File(dest));
    }

    /**
     * Copy a file
     * @param src source file
     * @param dest dest file
     * @throws NDSException if the copy of the file failed
     */
    public static void copyFile(File src, File dest) throws NDSException {

        if (dest.isDirectory()) {
        	logger.debug("Copy a file to a directory, append source filename to directory.");
            dest = new File(dest, src.getName());
        }

        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        FileChannel fcIn = null;
        FileChannel fcOut = null;
        try {

            // InputStream
            fIn = new FileInputStream(src);
            fOut = new FileOutputStream(dest);

            // nio channel
            FileChannel sourceFC = fIn.getChannel();
            FileChannel targetFC = fOut.getChannel();

            targetFC.transferFrom(sourceFC, 0, sourceFC.size());
        } catch (Exception e) {
            throw new NDSException("Error during copy file : " + src + " -> " + dest, e);
        } finally {
            try {
                fOut.close();
                fIn.close();
               // fcOut.close();
               // fcIn.close();
            } catch (Exception e) {
                logger.debug( "Cannot close some i/o which are open.", e);
            }

        }
    }
    /**
     * Delete files, no error even if file not deleted
     * @param daysBack
     * @param dirWay main directory
     * @param includeSub will loop over sub dir (recursively) or not 
     * @return count of files delete
     */
    public static int deleteFilesOlderThanNdays(int daysBack, String dirWay, boolean includeSub) {
    	final long purgeTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000);
    	FileFilter filter = new FileFilter() { 
    		public boolean accept(File f) { 
    			return f.isFile() && f.lastModified() < purgeTime; 
    		} 
    	}; 
    	
    	FileFilter dirFilter = new FileFilter() { 
    		public boolean accept(File file) { 
    			return file.isDirectory(); 
    		} 
    	};
    	
    	File directory = new File(dirWay);  
        
    	return deleteFiles(directory,filter,dirFilter,includeSub);
    }
    private static int deleteFiles(File directory,FileFilter fileFilter ,FileFilter dirFilter, boolean inlcudeSub){
    	int cnt=0;
    	if(directory.exists()){  
            File[] listFiles = directory.listFiles(fileFilter);              
            for(File listFile : listFiles) {  
            	cnt+= (listFile.delete()? 1:0);
            }
            if(inlcudeSub){
	            File[] dirs= directory.listFiles(dirFilter);
	            for(File dir : dirs) {  
	            	cnt+=deleteFiles(dir,fileFilter,dirFilter,inlcudeSub);
	            }
            }
        } else {  
            logger.warn("Files were not deleted, directory " + directory.getPath() + " does'nt exist!");  
        }  
    	return cnt;
    }
    /**
     * @param path file/directory to be deleted
     * @return true if deletion was OK
     */
    public static boolean delete(String path) {
        return delete(new File(path));
    }

    /**
     * @param f file/directory to be deleted
     * @return true if deletion was OK
     */
    public static boolean delete(File f) {
        if (f.isFile()) {
            return f.delete();
        } else {
            File[] childs = f.listFiles();
            if (childs == null) {
                // no childs
                return f.delete();
            } else {
                // childs
                boolean result = true;
                for (int i = 0; i < childs.length; i++) {
                    result &= delete(childs[i]);
                }
                return result && f.delete();
            }
        }

    }

    /**
     * Copy a directory recursively
     * @param src source directory
     * @param dest dest directory
     * @throws NDSException if the copy of the directory failed
     */
    public static void copyDirectory(String src, String dest) throws NDSException {
        copyDirectory(new File(src), new File(dest));
    }

    /**
     * Copy a directory recursively
     * @param src source directory
     * @param dest dest directory
     * @throws NDSException if the copy of the directory failed
     */
    public static void copyDirectory(File src, File dest) throws NDSException {

        if (!src.isDirectory()) {
            // We don not accept file arguments !
            throw new IllegalArgumentException("Source '" + src + "' must be a directory");
        }

        // create the destination directory if it is inexistant
        if (!dest.exists()) {
            dest.mkdirs();
        }

        // copy the files of the source directory
        File[] childs = src.listFiles();
        if (childs != null) {
            // childs
            for (int i = 0; i < childs.length; i++) {
                File child = childs[i];
                if (child.isFile()) {
                    // file
                    copyFile(child, dest);
                } else {
                    // directory
                    copyDirectory(child, new File(dest, child.getName()));
                }
            }
        }
    }
    
    /**
     * return a list of files with this suffix in directory dstr
     */
    public static String [] getFileList(File dir, String prefix, String suffix) {
        return dir.list(new DirFilter(prefix, suffix));
    }
    /**
     * return a list of files with this suffix in directory dstr
     */
    public static File[] getFiles(File dir, String prefix, String suffix) {
        return dir.listFiles(new DirFilter(prefix, suffix));
    }
}
/**
 * Filter used to seek directories
 * @author durieuxp
 */
class DirFilter implements FilenameFilter {

    String prefix = null;
    String suffix = null;

    public DirFilter(String p1, String s1) {
        prefix = p1;
        suffix = s1;
    }

    public boolean accept(File dir, String name) {
        if (name.endsWith("~")) {
            return false;
        }
        if (suffix != null && ! name.endsWith(suffix)) {
            return false;
        }
        if (prefix != null && ! name.startsWith(prefix)) {
            return false;
        }
        return true;
    }
    
}

