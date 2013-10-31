/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import javax.servlet.*;
import javax.servlet.http.*;
import nds.schema.*;
import nds.util.*;

import java.util.*;
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

public class Image implements BinaryHandler{
	private static Logger logger= LoggerManager.getInstance().getLogger(Image.class.getName());
	public void init(ServletContext context){}
	/**
	*  @param image - image name 
	*  @param dir - optional, if exists, will search file in that directory
	*/
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		String image=  request.getParameter("image");
		String dir= request.getParameter("dir");
		if(image.equals("apt")) image="test";
		if(Validator.isNotNull(dir)){
			String path= ((Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS)).getProperty("dir.tmp","/act/tmp");
			File f=new File(path+"/"+ dir+"/"+image);
			if(f.exists()){
				byte[] bytes= readFile(f.getAbsolutePath());
				response.setContentType("application/octetstream");
				response.setContentLength(bytes.length);
				ServletOutputStream ouputStream = response.getOutputStream();
				ouputStream.write(bytes, 0, bytes.length);
				ouputStream.flush();
				ouputStream.close();
			}else{
				logger.debug("Image file not found:"+ f.getAbsolutePath());
				request.getRequestDispatcher(WebKeys.NDS_URI+"/images/"+ image).forward(request, response);
			}
		}else
		request.getRequestDispatcher(WebKeys.NDS_URI+"/images/"+ image).forward(request, response);
	}
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
