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
import java.net.URI;


import nds.control.web.*;

/**
 * IDoc is used for client application to handle specific tasks locally.
 * 
 * 在portal上建立相应的表，增加button类型的字段，nds.web.button.ButtonLocalProcess，
 * 此字段显示条件为：当前用户是否为单据的可修改权限用户。点击后将在i_doc表中创建/更新记录，
 * 然后重定向到返回下载文件的连接 /servlets/binserv/IDoc。该连接对应nds. control.web.binhandler.IDoc，
 * 首先判断当前用户是否具有i_doc对应的记录的源表的修改权限，如果是，将生成.nea文件，提供给客户端
 * 
 * @author yfzhu@agilecontrol.com
 * @since 4.0 
 */

public class IDoc implements BinaryHandler{
    /**
     * @param request, contains:
     * 	"docno" - doc no for idoc
     */
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		
		nds.export.IDoc  doc=new nds.export.IDoc();
		URI u= new URI(request.getRequestURI());
		String wsdl=new URI(u.getScheme(),
		        u.getUserInfo(), u.getHost(), u.getPort(),
		        "/services/DocService", null,null).toString();
			//((Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS)).getProperty("web.url","http://localhost");
		//String wsdl= webRoot +"/services/DocService"; 
		doc.setWsdl(wsdl);
		
		doc.setDocNo(request.getParameter("docno"));
		
		doc.setIpAddress(request.getRemoteAddr());
		
		doc.setSessionId(request.getSession().getId());
		
		UserWebImpl user=(UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER);
		doc.setUser(user.getUserName()+"@"+ user.getClientDomain());

		byte[] bytes = doc.getBytes() ;
		
		response.setContentType(doc.MIME_TYPE);
		response.setHeader("Content-Disposition","inline;filename=\""+doc.getDocNo()+"."+ doc.FILE_EXTENSION+"\"");		
		response.setContentLength(bytes.length);
		ServletOutputStream ouputStream = response.getOutputStream();
		ouputStream.write(bytes, 0, bytes.length);
		ouputStream.flush();
		ouputStream.close();

					
	}
	
}
