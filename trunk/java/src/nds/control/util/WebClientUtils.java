package nds.control.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.UserWebImpl;
import nds.control.web.WebClientUserFileServlet;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.util.*;

import nds.schedule.JobManager;
import nds.security.User;

 import org.apache.commons.httpclient.HttpClient;  
import org.apache.commons.httpclient.methods.GetMethod;

public class WebClientUtils {
	private static Logger logger= LoggerManager.getInstance().getLogger( WebClientUtils.class.getName());	
//	private final static String TEMPLATE_NAME="size 50,50;autoresize both;color;fill;color 0,0,0;font 'FZDHTJW.TTF',15,15;string '";
											  
//	private final static String TEMPLATE_SAYING="size 100,100;autoresize both;color 255,255,255;font 'FZDHTJW.TTF',25,25;string '";;
	
	private final static String TEMPLATE_NAME="size 50,50;autoresize both;color 0,0,10;padding 5,5,5,5;fill;color 0,0,0;font 'FZDHTJW.TTF',18,18;antialias 16; type gif,256,{color:0,0,10};string '";
	  
	private final static String TEMPLATE_SAYING="size 50,50;autoresize both;color 250,250,250;padding 5,5,5,5;fill;color 255,255,255;font 'FZDHTJW.TTF',24,24;antialias 16;type gif,256,{color:250,250,250};string '";
	
	/**
	 * Create images for client
	 * @param domain ad_client.domain
	 * @param name ad_client.name
	 * @param saying ad_client.saying
	 * @throws Exception
	 */
	public static void createImages(String domain, String name, String saying) throws NDSException{
		try{
		generateFontImage(TEMPLATE_NAME, domain,name, "companyname.gif");
		generateFontImage(TEMPLATE_SAYING, domain,saying, "companysaying.gif");
		}catch(Throwable t){
			logger.error("Fail on createImages("+domain+","+name+","+saying+")", t);
			throw new NDSException("@exception@", t);
		}
	}
	/**
	 * 将web_client表中的公司名称和格言转化成图片。
	 * @param template
	 * @param str
	 * @param saveFile
	 * @throws Exception
	 */
	private static void generateFontImage(String template,String domain,String str, String saveFile) throws Exception{
		
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		String fgurl=conf.getProperty("webclient.fgurl");
		String clientWebRoot=conf.getProperty("client.webroot","/act/webroot");					     
		String u=java.net.URLEncoder.encode(template+str+"',10,10;", "UTF-8");
		URL  url = new URL(fgurl+ u);
		URLConnection connection = url.openConnection();
		InputStream is;
		FileOutputStream fs;
		is = connection.getInputStream();
		fs = new FileOutputStream(clientWebRoot+'/'+domain+"/images/"+saveFile);
		byte[] b = new byte[8192];
		int bInt;
		while((bInt = is.read(b,0,b.length))!= -1)
		{   
		    fs.write(b,0,bInt);
		}
		fs.flush();
		fs.close();
        is.close();
	}	
}
