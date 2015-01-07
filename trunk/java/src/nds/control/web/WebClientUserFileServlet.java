package nds.control.web;

import nds.util.*;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

import java.io.*;
import java.net.URLEncoder;

import java.util.Date;

import com.liferay.util.HttpHeaders;
import com.liferay.util.servlet.ServletResponseUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * This servlet is used for previewing client website. 

 * Servlet mapping all files in url "/servlets/userfolder" to 
 * client folder defined in Configuration "client.webroot"+"/"+<client domain>
 * 
 * For example, browser request downloading file named "/servlets/userfolder/images/myhome.jpg" with client domain named "www.56center.com"
 * and we set "client.webroot" to "/act/webroot", then this servlet will try loading file 
 * "/act/webroot/www.56center.com/images/myhome.jpg"
 * 
 * <<Important>>
 * This servlet is not used as fckeditor request user folder must be within servletcontext
 * @author yfzhu
 *
 */
public class WebClientUserFileServlet extends HttpServlet {
	private static Log logger = LogFactory.getLog(WebClientUserFileServlet.class);
	public  final static String USER_FOLDER_PATH="/servlets/userfolder"; 
	private String clientWebRoot;
	public void init(ServletConfig config) throws ServletException {
		synchronized (WebClientUserFileServlet.class) {
			super.init(config);

		    Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);	    
		    clientWebRoot=conf.getProperty("webclient.upload","/act.net/webhome");
		}
	}

	public void service(HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException {
		
		UserWebImpl userWeb =null;
		WeUtilsManager Wemanage =WeUtilsManager.getInstance();
    	try{
    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(req.getSession()).getActor(nds.util.WebKeys.USER));	
    	}catch(Throwable userWebException){
    		
    	}
    	try{
		String domain=null;
		logger.debug("user name=> "+userWeb.getUserName());
    	if(userWeb==null || userWeb.getUserId()==UserWebImpl.GUEST_ID ){
    		try{
    			java.net.URL url = new java.net.URL(req.getRequestURL().toString());
    			WeUtils wu=Wemanage.getByDomain(url.getHost().toLowerCase().trim());
    			domain=wu.getDoMain();
    			//domain=WebUtils.getAdClientDomain( url.getHost());
    			
    		}catch(Throwable t){
    			logger.error("fail to parse host from "+req.getRequestURL() +":"+t);
    		}		
    		if(domain==null){
    			//host not set in web_client
    			return;
    		}
    		
    	}else{
    		//WeUtils wu=nds.weixin.ext.WeUtilsManager.getByAdClientId(userWeb.getAdClientId());
    		WeUtils wu=Wemanage.getByAdClientId(userWeb.getAdClientId());
    		domain=wu.getDoMain();
    		logger.debug("user domain=> "+domain);
    	}
    	
    	String path= clientWebRoot+"/"+domain+req.getPathInfo();
    	//logger.debug(Tools.toString(req));
    	/*String requestPath=req.getPathInfo();
    	int pos= requestPath.indexOf(USER_FOLDER_PATH);
    	if(pos>-1){
    		String f= requestPath.substring(pos+ USER_FOLDER_PATH.length());
    		path= path+f;
    	}else{
    		logger.warn(USER_FOLDER_PATH+" not found in request path "+ requestPath);
    		return;
    	}*/
    	File file=new File(path);
    	if(!file.exists()){
    		logger.warn(path+" not found");
    		req.getRequestDispatcher(WebKeys.NDS_URI+"/images/noimg.png").forward(req, res);
    		return;
    	}
    	
    	/*
		long lastModified = file.lastModified();

		long ifModifiedSince =
				req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);

		if ((ifModifiedSince > 0) && (ifModifiedSince == lastModified)) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		res.addHeader(HttpHeaders.CACHE_CONTROL, "max-age=0");

		res.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModified);

		res.setContentType("application/octetstream");
		res.setContentLength((int)file.length());
		
		//shall we ask client to download instead of opening directly?
		//res.setHeader("Content-Disposition","attachment;filename=\""+URLEncoder.encode(file.getName(),"UTF-8")+"\"");
		

		//write file
        FileInputStream is=new FileInputStream(file);
        ServletOutputStream os = res.getOutputStream();
        byte[] b = new byte[8192];
        int bInt;
        while((bInt = is.read(b,0,b.length)) != -1)
        {
            os.write(b,0,bInt);
        }
        os.close();
        is.close();*/
    	//File f=new File(path+"/"+ dir+"/"+image);
		if(file.exists()){
			byte[] bytes= readFile(file.getAbsolutePath());
		    String ct= Tools.getContentType(FileUtils.getExtension(file.getName()), "application/octetstream");
		    res.setContentType(ct+"; charset=GBK");
		    res.setContentType("text/plain; charset=UTF-8");
			//res.setContentType("application/octetstream");
			res.setContentLength(bytes.length);
			res.setHeader("Content-Disposition","inline;filename=\""+file.getName()+"\"");
			ServletOutputStream ouputStream = res.getOutputStream();
			ouputStream.write(bytes, 0, bytes.length);
			ouputStream.flush();
			ouputStream.close();
		}
    	}catch(Throwable t){
			logger.error("fail to get user file"+t);
			req.getRequestDispatcher(WebKeys.NDS_URI+"/images/noimg.png").forward(req, res);
		}	
	}
	
	//private final static String NO_ATTACH="<html><head><title>nofile</title></head><body><p> File not found!</body></html>";
	/**
     * Read file content as whole, only suitable for small size
     */
    public static byte[] readFile(String fileName) throws IOException{
        FileInputStream is=new FileInputStream(fileName);
        ByteArrayOutputStream os= new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int bInt;
        while((bInt = is.read(b,0,b.length)) != -1)
        {
            os.write(b,0,bInt);
        }
        is.close();
        os.flush();
        return os.toByteArray();

    }
	
}
