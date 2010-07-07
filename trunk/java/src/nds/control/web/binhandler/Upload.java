/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import nds.log.*;
import nds.report.ReportUtils;
import nds.schema.*;
import nds.util.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import nds.control.event.NDSEventException;
import nds.control.web.*;

/**
 * Upload file to user web folder
 * 
 * web form should contain:
 * 	"onsuccess" - script when upload successfuly, default to window.parent.window.stopUpload(0);
 *  "onfail"	- script when upload failed, default to window.parent.window.stopUpload(1);
 * 
 * @author yfzhu@agilecontrol.com
 */

public class Upload implements BinaryHandler{
	private Logger logger= LoggerManager.getInstance().getLogger(Upload.class.getName());
	
	public void init(ServletContext context){}
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		boolean isOK=false;
		HashMap map=new HashMap();
		try{
			UserWebImpl userWeb =null;
			userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	
			if(userWeb==null || userWeb.isGuest()){
				throw new NDSException("guest not allowed");
			}
			DiskFileUpload  fu = new DiskFileUpload();
	        // maximum size before a FileUploadException will be thrown
	        fu.setSizeMax(1024*1024*1024); // 1GB
	        
	        List fileItems = fu.parseRequest(request);
	        Iterator iter = fileItems.iterator();
	        InputStream in=null;
	        String fileName="";
	        
	        while (iter.hasNext()) {
	                FileItem item = (FileItem) iter.next();
	                if (!item.isFormField()) {
	                    in=item.getInputStream();
	                    fileName= item.getName();
	                    //remove file path
	                    if(fileName!=null){
	                    	int pos= fileName.lastIndexOf("\\");
	                    	if(pos>0)
	                    		fileName= fileName.substring(pos+1 );
	                    	pos= fileName.lastIndexOf("/");
	                    	if(pos>0)
	                    		fileName= fileName.substring(pos+1 );
	                    }
	                }else{
	                	map.put(item.getFieldName(),item.getString() );
	                }
	        }
	        if(in !=null){
	        // save to file system
	    		// save to file
	    		ReportUtils ru = new ReportUtils(request);
	    	    String name = ru.getUserName();
	    	    String svrPath = ru.getExportRootPath() + File.separator  + ru.getUser().getClientDomain()+File.separator+ name;
	    		
	    	    String filePath = svrPath + File.separator + fileName;
	    	    File file = new File(filePath);
	        	
	        	OutputStream out = null;
	        	out = new FileOutputStream(file);
	            copyContents( in, out );
	            out.flush();
	            out.close();
	            isOK=true;
	            logger.debug("save to "+ file.getAbsolutePath()+" (" + Tools.formatSize(file.length())+")");
	        }
		}catch(Throwable t){
			logger.error("fail to do upload ",t);
		}
		String script;
		if(isOK){
			script= (String)map.get("onsuccess");
			if(Validator.isNull(script)){
				script="window.parent.window.stopUpload(0);";
			}
		}else{
			script= (String)map.get("onfail");
			if(Validator.isNull(script)){
				script="window.parent.window.stopUpload(1);";
			}
		}
		response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.print("<html><body><script language='javascript' type='text/javascript'>");
        out.print(script);
        out.println("</script></body></html>"); 
    }
	/**
	 *  Just copies all characters from <I>in</I> to <I>out</I>.
	 *
	 *  @since 1.9.31
	 */
	private void copyContents( InputStream in, OutputStream out )
	    throws IOException
	{
	    byte[] b = new byte[1024*16]; // 16k cache
	    int bInt;
	    while((bInt = in.read(b,0,b.length)) != -1)
	    {
	        out.write(b,0,bInt);
	    }
	    out.flush();
	}
}
