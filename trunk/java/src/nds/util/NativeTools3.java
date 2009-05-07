/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holding native methods, to enable multiple classloading(
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4225434)
 * This class should be loaded by root classloader 
 * @author yfzhu@agilecontrol.com
 */

public final class NativeTools3 {
	private static final Log logger = LogFactory.getLog(NativeTools3.class);
	private static ClassLoader classLoader;
	private NativeTools3(){}
	/**
	 * Get local machine's cpus' id, seperated by comma if multiple cpu found
	 * If more than 4 cpu found, only first 4 cpu ids will be retrieved.
	 * @return
	 */
	public static String getCPUIDs(){return "";}
	public static String decrypt(String s){ throw new Error("Deprecated");}
	public static String encrypt(String s){ throw new Error("Deprecated");}
	/**
	 * This method will be called by native library onLoad method
	 * to check some classes (loaded by <param>classLoader</param> set before)
	 * @param file should exists in pathes that classLoader searched
	 *   file format sample:
	 * 		nds/util/Tools.class
	 *      portal-ejb.jar
	 *   using relative path 
	 * @return null if file not found by classLoader 
	 */
	public static String getFileCheckSum(String file){
    	String sum=null;
    	try {
			URL classUrl= (classLoader==null?NativeTools3.class.getClassLoader():classLoader).getResource(file);
			
			if (classUrl != null){
				InputStream is= classUrl.openStream();
				try{
					sum=MD5Sum.toCheckSum(is);
				}finally{
					if(is!=null){
						try{is.close();}catch(Exception e){}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not get file checksum of "+ file, e);
		}
		//logger.debug("find sum for "+ file +"="+sum);
		return sum;
		
	}
	/**
	 * 
	 *
	 */
	public static void unload(){
		classLoader=null;
	}
	public static void load(ClassLoader loader){
		classLoader=loader;
		try {
			//java.lang.System.load("/path/to/nuto.dat"); normally in the same path as portal-ejb.jar 
			URI uri= new URI(loader.getResource("nuto.dat").toString());
			String a= (new File(uri)).getAbsolutePath();
			
			//System.out.println("file="+a);
			System.load(a);
			//Class.forName("java.lang.System").getMethod("load",new Class[]{String.class}).invoke(null,new Object[]{a});
			
		} catch (Throwable ule) {
			logger.debug("Error loading library:"+ule.getClass(),ule);
			logger.error("\n\r"+
	                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\r"+
	                "     Library file missing or invalid, will exit.    \n\r"+
	                "                                                      \n\r"+
	                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			beginExit();
		}
	}
	private static void beginExit(){
    	Thread t=new Thread(new Runnable(){
    		public void run(){
    			try{
    				Thread.sleep(1000);
    				System.exit(1099);
    				
    			}catch(Throwable e){
    				e.printStackTrace();
    			}
    		}
    	});
    	t.start();
    }

}
