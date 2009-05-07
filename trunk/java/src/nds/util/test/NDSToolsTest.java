/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util.test;

import nds.util.*;
import java.util.*;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class NDSToolsTest {
	public static void printNDSDefine(){
		System.out.println("#define NDS_UTIL_LICENSEMANAGER_MD5 \""+ Tools.getFileCheckSum("nds.util.LicenseManager")+"\"");
		System.out.println("#define NDS_UTIL_TOOLS_MD5 \""+ Tools.getFileCheckSum("nds.util.Tools")+"\"");
		System.out.println("#define NDS_UTIL_NATIVETOOLS_MD5 \""+ Tools.getFileCheckSum("nds.util.NativeTools")+"\"");
		System.out.println("#define NDS_CONTROL_STARTUPENGINE_MD5 \""+ Tools.getFileCheckSum("nds.control.StartupEngine")+"\"");
		System.out.println("#define NDS_CONTROL_WEB_MAINSERVLET_MD5 \""+ Tools.getFileCheckSum("nds.control.web.MainServlet")+"\"");
	}
	public static void main(String[] args){
		printNDSDefine();
	}
}
