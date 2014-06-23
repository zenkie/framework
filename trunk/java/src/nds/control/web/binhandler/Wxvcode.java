/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.http.*;

import com.kin.weixin.WeixinBind;

import nds.schema.*;
import nds.util.*;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;

import nds.log.Logger;
import nds.log.LoggerManager;
//import nds.portlet.util.*; 
import nds.control.web.*;
//import net.sf.jasperreports.engine.*;
import nds.report.*;
import java.net.URLEncoder;


/**
 * Return JasperReport
 * @author yfzhu@agilecontrol.com
 */

public class Wxvcode implements BinaryHandler{
	private static Logger logger= LoggerManager.getInstance().getLogger(Wxvcode.class.getName());
	public void init(ServletContext context){}
	private static Wxvcode instance=null;
	private static Hashtable<String,String> wxbind=new Hashtable<String,String>();
	
	
	public static synchronized Wxvcode getInstance(){
		if(instance==null){instance=new Wxvcode();}
		return instance;
	}
	
	public String getcookei(String user){
		return wxbind.get(user);
	}

	/**
	*  @param image - image name 
	*  @param dir - optional, if exists, will search file in that directory
	*/
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		String user=  request.getParameter("user");
	//	String dir= request.getParameter("dir");
		//if(image.equals("apt")) image="test";
		Wxvcode wxv=Wxvcode.getInstance();
		//String	wbind=wxv.wxbind.get(user);
		//ByteArrayOutputStream out=null;
		WeixinBind wx = new WeixinBind("","","");
		InputStream is=wx.code();
		wxv.wxbind.put(user, wx.getCookiestr());
		//codejpg
		response.setContentType("application/octetstream");
		ServletOutputStream os = response.getOutputStream();
        byte[] b = new byte[8192];
        int bInt;
        while((bInt = is.read(b,0,b.length)) != -1)
        {
            os.write(b,0,bInt);
        }
        is.close();
        os.flush();
        os.close();
	}
	
}
