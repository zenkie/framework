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

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class GetFile implements BinaryHandler{
	  private Logger logger= LoggerManager.getInstance().getLogger(GetFile.class.getName());	 
	
	  private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
	  private static final String PLAIN_CONTENT_TYPE = "text/plain; charset=UTF-8";
	  private static final String DOWNLOAD_TYPE = "application/octetstream; charset=GBK";
	  private static final String[] TEXT_TYPE=new String[]{"html","htm","csv","txt","log"};
	  
	  private static final String[] EXT=new String[]{
		  "xls","doc","pdf","zip","cub"
	  };
	  private static final String[] EXT_CONTENT_TYPE=new String[]{
		  "application/vnd.ms-excel","application/vnd.ms-word","application/pdf","application/zip",
		  "application/x-download"
	  };
	  
	  /**
	   *  
	   * @param fileName full file name
	   * @return 0 for html/htm, 1 from other text, -1 for none text
	   */
	  private int getFileType(String fileName){
    	String ext= fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();	
    	for(int i=0;i<TEXT_TYPE.length;i++ ){
    		if(ext.equalsIgnoreCase(TEXT_TYPE[i])) return (ext.contains("htm")? 0:1); 
    	}
    	return -1;
	  }
	  /**
	   * will read parameters and decide whether display data directly or request download dialog
	   *  	filename* - the file that should be returned, that file should be file name only and exist in user's web folder
	   *    show     - "Y"(default) or "N" when "Y" will try get file real type and set in Content-Disposition
	   *                so IE will display that file directly without save dialog
	   *    del    - "N"(default), will delete after downloaded
	   *    cd - content-disposition "inline"|"attachment" default to inline               
	   */
      public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
        String filePath = request.getParameter("filename");
        boolean deleteFile=nds.util.Tools.getYesNo(request.getParameter("del"), false);
        String cd= request.getParameter("cd");
        if(!"attachment".equalsIgnoreCase(cd)) cd="inline";
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
        boolean isShow= nds.util.Tools.getYesNo(request.getParameter("show"), true);
        if(filePath!=null && !filePath.trim().equals("")){
            filePath = filePath.trim();
            ReportUtils ru = new ReportUtils(request);
            String name = ru.getUserName();

            File file = new File(ru.getExportRootPath()+File.separator+ru.getUser().getClientDomain()+ File.separator+ name+File.separator+filePath);
            if(file.exists() && file.isFile()){
            	logger.debug("Downloading "+ file.getAbsolutePath());
            	int fileType=getFileType(file.getName());
            	if( fileType==0){
            		response.setContentType(CONTENT_TYPE);
                	response.setHeader("Content-Disposition",cd+";"+ 
                			WebUtils.getContentDispositionFileName(filePath, request));
            		
            		//response.setHeader("Content-Disposition","inline;filename=\""+URLEncoder.encode(filePath,"UTF-8")+"\"");
            	}else if(fileType==1){
            		response.setContentType(PLAIN_CONTENT_TYPE);
                	response.setHeader("Content-Disposition",cd+";"+ 
                			WebUtils.getContentDispositionFileName(filePath, request));
            	}else{
            		boolean fileExtFound=false;
            		if(isShow){
            			// get file extension
            			String fileExt=null;
            			int p=file.getName().lastIndexOf('.');
            			if(p>0) fileExt= file.getName().substring(p+1);
            			if(fileExt!=null){
	            			for(int i=0;i<EXT.length;i++ ){
	            				if(EXT[i].equals(fileExt)){
	            					response.setContentType(EXT_CONTENT_TYPE[i]);
	            					response.setHeader("Content-Disposition",cd+";"+
	                            			WebUtils.getContentDispositionFileName(filePath, request));
	                        		
	            					//response.setHeader("Content-Disposition","inline;filename=\""+URLEncoder.encode(filePath,"UTF-8")+"\"");
	            					fileExtFound=true;
	            					break;
	            				}
	            			}
            			}
            		}
            		if(!fileExtFound){
            			response.setContentType(DOWNLOAD_TYPE);
            			//response.setHeader("Content-Disposition","inline;filename=\""+URLEncoder.encode(filePath,"UTF-8")+"\"");
            			response.setHeader("Content-Disposition",cd+";"+ 
                    			WebUtils.getContentDispositionFileName(filePath, request));
                		
            		}
            	}
                
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
                	// 改成10分钟后删除, 借助外部检查程序
                	
                	/*
                	try{
                		if(!file.delete()){
                			logger.debug("Fail to delete file:"+ file.getAbsolutePath());
                		}
                	}catch(Throwable t){
                		logger.error("Fail to delete file "+ file.getAbsolutePath()+":"+ t);
                	}*/
                }
                return;
            }else
            	logger.warning("Could not load file:"+ file.getAbsolutePath());
        }
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>GetFile</title></head>");
        out.println("<body>");
        out.println("<p>文件不存在,或者文件不可读，或者没有指定文件名</p>");
        out.println("</body></html>");      	
      }
      public void init(ServletContext context){}
}
