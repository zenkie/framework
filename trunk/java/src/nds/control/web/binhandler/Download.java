/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.report.ReportUtils;
import nds.util.Configurations;

/**
 * Different with GetFile, thie handler always request client to download the file instead of dispalying directly 
 * @author yfzhu@agilecontrol.com
 */

public class Download implements BinaryHandler{
	  private Logger logger= LoggerManager.getInstance().getLogger(GetFile.class.getName());	 
	
	  private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";
	  private static final String DOWNLOAD_TYPE = "application/octetstream; charset=GBK";
	  private static final String[] TEXT_TYPE=new String[]{"html","htm","csv","txt","log"};
	  
	  private static final String[] EXT=new String[]{
		  "xls","doc","pdf","zip","cub"
	  };
	  private static final String[] EXT_CONTENT_TYPE=new String[]{
		  "application/vnd.ms-excel","application/vnd.ms-word","application/pdf","application/zip","application/x-download"
	  };
	  
	  /**
	   *  
	   * @param fileName full file name
	   * @return
	   */
	  private boolean isTextType(String fileName){
    	String ext= fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();	
    	for(int i=0;i<TEXT_TYPE.length;i++ ){
    		if(ext.equalsIgnoreCase(TEXT_TYPE[i])) return true; 
    	}
    	return false;
	  }
	  /**
	   * will read parameters and decide whether display data directly or request download dialog
	   *  	filename* - the file that should be returned, that file should be file name only and exist in user's web folder
	   *  				for normal user, for root, it can be absolute path
	   */
      public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
    	Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
    	Boolean pathname=nds.util.Tools.getBoolean(conf.getProperty("report_savepathbyuserid","false"),false);
    	
        String filePath = request.getParameter("filename");
        boolean deleteFile=false;
        if(filePath==null){
        	// http://support.microsoft.com/kb/831929, since this bug, we set file name to PathInfo
        	/* this file should be deleted since such usage method is used for crosstab reports only
        	 	as soon user request such kind download, file will be deleted immediately*/
        	String pathInfo= request.getPathInfo();
        	int p=pathInfo.indexOf('/',1);
    		if(p>0){
    			filePath = pathInfo.substring(p+1);
    			//deleteFile=true;// do not delete since we allow download and print now 
    		}
        	
        }
        if(filePath!=null && !filePath.trim().equals("")){
            filePath = filePath.trim();
            ReportUtils ru = new ReportUtils(request);
            String name = pathname?String.valueOf(ru.getUser().getUserId()):ru.getUserName();
            nds.control.web.UserWebImpl userWeb=ru.getUser();
            boolean isRoot=userWeb.isPermissionEnabled("WEBSQL_LIST", nds.security.Directory.WRITE);
            
            File file = new File(filePath);
    	    if(file.isAbsolute()){
    	    	if(!isRoot){
    	        	file=null; // invalid file
    	    	}
    	    }else{
    	    	file=new File( ru.getExportRootPath()+File.separator+ru.getUser().getClientDomain()+ File.separator+ name+File.separator+filePath);
    	    }
    	    if(!isRoot){
    	    	//make sure that file exists in user web folder
    	    	File parent= new File(ru.getExportRootPath()+File.separator+ru.getUser().getClientDomain()+ File.separator+ name);
    	    	if(!file.getParentFile().equals(parent) ){
    	    		file=null; // invalid file
    	    	}
    	    }
            if(file!=null && file.exists() && file.isFile()){
            	logger.debug("Downloading "+ file.getAbsolutePath());
            	
           		response.setContentType(DOWNLOAD_TYPE);
           		response.setHeader("Content-Disposition","attachment;"+ 
            			WebUtils.getContentDispositionFileName(file.getName(), request));
           		//response.setHeader("Content-Disposition","attachment;filename=\""+URLEncoder.encode(filePath,"UTF-8")+"\"");

                
                FileInputStream is=new FileInputStream(file);
                ServletOutputStream os = response.getOutputStream();
                byte[] b = new byte[8192];
                int bInt;
                while((bInt = is.read(b,0,b.length)) != -1)
                {
                    os.write(b,0,bInt);
                }
                //out.write(new Byte(file.toString()));
                os.close();
                is.close();
                if(deleteFile){
                	try{
                		if(!file.delete()){
                			logger.debug("Fail to delete file:"+ file.getAbsolutePath());
                		}
                	}catch(Throwable t){
                		logger.error("Fail to delete file "+ file.getAbsolutePath()+":"+ t);
                	}
                }
                return;
            }else
            	logger.warning("Could not load file:"+ file.getAbsolutePath());
        }
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("文件不存在,或者文件不可读，或者没有指定文件名");
              	
      }
      public void init(ServletContext context){}
}
