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
import nds.util.AES;

/**
 * Holding native methods, to enable multiple classloading(
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4225434)
 * This class should be loaded by root classloader 
 * @author yfzhu@agilecontrol.com
 */

public final class NativeTools {
	private static final Log logger = LogFactory.getLog(NativeTools.class);
	private static ClassLoader classLoader;
	private NativeTools(){}
	/**
	 * Get local machine's cpus' id, seperated by comma if multiple cpu found
	 * If more than 4 cpu found, only first 4 cpu ids will be retrieved.
	 * @return codeToCheck if success,else null
	 */
	public static native String getCPUIDs(String codeToCheck);
	//public static native String decrypt(String s);
	public static native String encrypt(String s);
	public static String decrypt(String s){
		return s;
	};
	
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
			URL classUrl= (classLoader==null?NativeTools.class.getClassLoader():classLoader).getResource(file);
			
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
			String s="1xtl1vgz1vv31k8w1k5a1vu71vfv1xtx"/*nuto.dat*/;
	        byte[] b=new byte[s.length()/2];
	        int l=0;
	        for (int i=0;i<s.length();i+=4)
	        {
	            String x=s.substring(i,i+4);
	            int i0 = Integer.parseInt(x,36);
	            int i1=(i0/256);
	            int i2=(i0%256);
	            b[l++]=(byte)((i1+i2-254)/2);
	        }
	        /**
	         * Check OS Platform info, 
	         * for windows 32bit, use nuto.dat
	         * for windows amd64, use nuto.dat.a 
	         * for linux x86, use nuto.dat.x
	         * for linux 64, use nuto.dat.l
	         */
	        String f=new String(b,0,l);
	        String osName=System.getProperty("os.name"); //Windows XP, Linux, Windows 2003
	        String osArch=System.getProperty("os.arch");// i386(Linux),x86,amd64,ia64
	        String dataModel= System.getProperty("sun.arch.data.model");//32,64
	        if(osName.indexOf("Windows")> -1){
	        	if(dataModel.indexOf("64")>-1) f=f+".a";
	        	//others will be f
	        }else if(osName.indexOf("Linux")> -1){
	        	if(dataModel.indexOf("32")>-1) f=f+".x";
	        	else if(osArch.indexOf("64")>-1) f=f+".l";
	        	// others will be f
	        }
			URI uri= new URI(loader.getResource(f).toString());
			String a= (new File(uri)).getAbsolutePath();
			
			//System.out.println("file="+a);
			//System.out.println("sdfsdfsdfsdfsdf");
			System.load(a);
			//System.out.println("sdfsdfsdfsdfdsfsdf");
			//Class.forName("java.lang.System").getMethod("load",new Class[]{String.class}).invoke(null,new Object[]{a});
			
		} catch (Throwable ule) {
			logger.debug("Error loading library:"+ule.getClass()+":"+ ule.getMessage());//do not dump ule, as it will show the nuto.dat lib file path
			logger.error("\n\r"+
	                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\r"+
	                "     Library file missing or invalid, will exit.    \n\r"+
	                "                                                      \n\r"+
	                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			beginExit();
		}
	}
	private static void beginExit(){
		if(true) return;
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
