package nds.control.ejb.command;

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
import nds.query.*;
import nds.util.*;

import nds.schedule.JobManager;
import nds.security.User;

 import org.apache.commons.httpclient.HttpClient;  
import org.apache.commons.httpclient.methods.GetMethod;
 
 
public class WebClient_CreateImage extends Command {
	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		User user=helper.getOperator(event);
	  	helper.checkDirectoryWritePermission(event, user);
		int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
		java.util.Locale locale= event.getLocale();
		String sql="select name,saying from web_client where id=?";
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String name,saying;
        try {
			pstmt= conn.prepareStatement(sql);
			pstmt.setInt(1,objectId);
			rs= pstmt.executeQuery();
			if(rs.next()){
				name=rs.getString(1);
				saying=rs.getString(2);	
				
				String template1="size 50,50;autoresize both;color;fill;color 0,0,0;font 'FZDHTJW.TTF',15,15;string '";
				String template2="size 100,100;autoresize both;color 255,255,255;font 'FZDHTJW.TTF',25,25;string '";;
				this.generateFontImage(template1, user,name, "companyname.gif");
				this.generateFontImage(template2, user,saying, "companysaying.gif");
			} 
        }catch (Exception exception) {
        	exception.printStackTrace();
        }
			finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}		
		ValueHolder holder= new ValueHolder();
   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
   		holder.put("code","0");
   		return holder;
	}
	
	/**
	 * 将web_client表中的公司名称和格言转化成图片。
	 * @param template
	 * @param str
	 * @param saveFile
	 * @throws Exception
	 */
	private void generateFontImage(String template,User user,String str, String saveFile) throws Exception{
		
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		String fgurl=conf.getProperty("webclient.fgurl");
		String clientWebRoot=conf.getProperty("client.webroot","/act/webroot");					     
		String u=java.net.URLEncoder.encode(template+str+"',10,10;", "UTF-8");
		URL  url = new URL(fgurl+ u);
		URLConnection connection = url.openConnection();
		InputStream is;
		FileOutputStream fs;
		try {
			is = connection.getInputStream();
			fs = new FileOutputStream(clientWebRoot+'/'+user.clientDomain+"/images/"+saveFile);
			byte[] b = new byte[8192];
			int bInt;
			while((bInt = is.read(b,0,b.length))!= -1)
			{   
			    fs.write(b,0,bInt);
			}
			fs.flush();
			fs.close();
	        is.close();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}
}

