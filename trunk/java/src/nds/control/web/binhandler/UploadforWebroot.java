/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.*;



import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import nds.log.*;
import nds.report.ReportUtils;
import nds.schema.*;
import nds.util.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;

import nds.control.event.NDSEventException;
import nds.control.web.*;
import nds.weixin.ext.*;
import  nds.util.ImageUtils;
import nds.util.FileUtils;

/**
 * Upload file to user web folder
 * 
 * web form should contain:
 * 	"onsuccess" - script when upload successfuly, default to window.parent.window.stopUpload(0);
 *  "onfail"	- script when upload failed, default to window.parent.window.stopUpload(1);
 * 
 * @author yfzhu@agilecontrol.com
 */

public class UploadforWebroot implements BinaryHandler{
	private Logger logger= LoggerManager.getInstance().getLogger(UploadforWebroot.class.getName());
	public static final String PROP_STORAGEDIR = "webclient.upload";
	private String m_storageDir;
	public void init(ServletContext context){
	}
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		 Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
	     m_storageDir = conf.getProperty(PROP_STORAGEDIR, "/act.net/webhome");
		boolean isOK=false;
		HashMap map=new HashMap();
		String fileName="";
		String modname="";
		try{
			UserWebImpl userWeb =null;
			userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	
			if(userWeb==null || userWeb.isGuest()){
				throw new NDSException("guest not allowed");
			}
			WeUtilsManager Wemanage =WeUtilsManager.getInstance();
			WeUtils wu=Wemanage.getByAdClientId(userWeb.getAdClientId());
			//	          Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();

			//	             Set factory constraints
			factory.setSizeThreshold(1024*1024 * Tools.getInt( conf.getProperty("webclient.upload.maxsize", "1"), 1)); // 1MB
			factory.setRepository(new File(conf.getProperty("dir.tmp","/portal/act.nea/tmp")));

			//	             Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			//	             Set overall request size constraint
			upload.setSizeMax(1024*1024 * Tools.getInt( conf.getProperty("webclient.upload.maxsize", "1"), 4));

			//	             Parse the request
			List /* FileItem */ fileItems = upload.parseRequest(request);
			
			Iterator iter = fileItems.iterator();
			//DiskFileUpload  fu = new DiskFileUpload();
	        // maximum size before a FileUploadException will be thrown
	        //fu.setSizeMax(1024*1024*1024); // 1GB
	        
	       // List fileItems = fu.parseRequest(request);
	        //Iterator iter = fileItems.iterator();
	        InputStream in=null;
	        
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
	                	map.put(item.getFieldName(),item.getString("UTF-8") );
	                }
	        }
	        if(in !=null){
	        // save to file system
	    		// save to file
	    		//ReportUtils ru = new ReportUtils(request);
	    	    //String name = ru.getUserName();
	        	//System.out.print(m_storageDir);
	        	//System.out.print(wu.getDoMain());
	    	    String svrPath = m_storageDir+ File.separator  + wu.getDoMain();
	    	     modname= (String)map.get("modname");
	    	    if(modname!=null){
	    	    	svrPath+=File.separator+modname;
	    	    }
	    	    File uploadPath = new File(svrPath);//上传文件目录
	    	    if (!uploadPath.exists()) {
	    	       uploadPath.mkdirs();
	    	    }
	    	    
	    	    fileName=new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+"."+FileUtils.getExtension(fileName);
	    	    String filePath = svrPath + File.separator + fileName;
	    	    int insize=in.available();
	    	    BufferedImage bi=ImageIO.read(in);
	    	    Boolean isthum= nds.util.Tools.getBoolean(map.get("isThum"), false);//(Boolean)map.get("isThum")==null?false:true;
	    		if(isthum){
	    			//createThumbnailator("/Users/jackrain/Downloads/4.jpg","/Users/jackrain/Downloads/imgTest2.jpg",200,200);
	    			int width=Integer.valueOf((String)map.get("width"));
	    			int hight=Integer.valueOf((String)map.get("hight"));
	    			logger.debug("width ->"+width+"hight ->"+hight+"fileName ->"+fileName);
	    			ImageUtils.createThumbnailator(bi,filePath,width,hight,true,fileName);
	    		}else{
	    			logger.debug("fileName ->"+fileName);
	    			ImageUtils.createThumbnailator(bi,filePath,fileName);
	    		}
	    	    
	    		
	    	    //File file = new File(filePath);
	        	//OutputStream out = null;
	        	//out = new FileOutputStream(file);
	            //copyContents( in, out );
	            //out.flush();
	            //out.close();
	            isOK=true;
	            //"/servlets/userfolder/"+fileName
	            logger.debug("save to "+ filePath+" (" + Tools.formatSize(insize)+")");
	        }
		}catch(Throwable t){
			logger.error("fail to do upload ",t);
			//return;
		}
		String script;
		if(isOK){
			script= (String)map.get("onsuccess");
			if(Validator.isNull(script)){
				script="window.parent.window.stopUpload(0);";
			}else{
				script=replace(script,"$filepath$","/servlets/userfolder/"+modname+"/"+fileName);
			}
		}else{
			script= (String)map.get("onfail");
			if(Validator.isNull(script)){
				script="window.parent.window.stopUpload(1);";
			}else{
				script=replace(script,"$filepath$","/servlets/userfolder/"+modname+"/"+fileName);
			}
		}
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
		Map.Entry entry = (Map.Entry) iter.next();
		Object key = entry.getKey();
		Object val = entry.getValue();
		System.out.print("key =>"+key+"val =>"+val);
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
	
	
    /**
     * Efficient string replace function. Replaces instances of the substring
     * find with replace in the string subject. karl@xk72.com
     * 
     * @param subject
     *            The string to search for and replace in.
     * @param find
     *            The substring to search for.
     * @param replace
     *            The string to replace instances of the string find with.
     */
    public static String replace(String subject, String find, String replace) {
        StringBuffer buf = new StringBuffer();
        int l = find.length();
        int s = 0;
        int i = subject.indexOf(find);
        //System.out..print("find ->"+find+" pos->"+i);
        while (i != -1) {
            buf.append(subject.substring(s, i));
            buf.append(replace);
            s = i + l;
            i = subject.indexOf(find, s);
        }
        buf.append(subject.substring(s));
        return buf.toString();
    }
}
