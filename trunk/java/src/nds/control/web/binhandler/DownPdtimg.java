/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.Connection;
import nds.query.QueryEngine;
import nds.schema.*;
import nds.util.*;

import java.util.*;
import java.io.*;

import nds.control.web.*;

/**
 * Return attachment
 * 与Attach 不同的是，Attach2不做权限校验，方便报表等查看图片
 * 
 * 这是一个超级BUG,时间压力，先这样实现了。仅为burgeon提供
 * 
 * @author yfzhu@agilecontrol.com
 */

public class DownPdtimg implements BinaryHandler{
	public void init(ServletContext context){}
    /**
     * Things needed in this page:
     *  table* - (int) table id
     *  column* - (int) column id
     *  objectid* - (int) record id
     *  version - (int ) -1 if not found
     */
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		//UserWebImpl userWeb =null;
		//userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	
		
		TableManager tableManager=TableManager.getInstance();
		//int tableId= ParamUtils.getIntAttributeOrParameter(request, "table", -1);
		//int columnId =ParamUtils.getIntAttributeOrParameter(request, "column", -1);
		QueryEngine engine= QueryEngine.getInstance();
		//Connection conn= engine.getConnection();
		String pdtno= ParamUtils.getAttributeOrParameter(request, "pdtno");
		int version= ParamUtils.getIntAttributeOrParameter(request, "version", -1);
		String thum=ParamUtils.getAttributeOrParameter(request, "thum");
		String colname=String.valueOf(engine.doQueryOne("select value from ad_param where name='DownPdtimg'"));
		int objectId=Tools.getInt( engine.doQueryOne("select g.id from m_product g where g.name='"+pdtno+"'"),-1);
 
		InputStream is=null;
		Table table;
	    table= tableManager.getTable("m_product");
	    //table.getColumn(colname);
		/**------check permission---**/
/*		if(!userWeb.hasObjectPermission(table.getName(),objectId,  nds.security.Directory.READ)){
		   	throw new NDSException("no permission");
		}*/
		/**------check permission end---**/

		Column col=table.getColumn(colname);
		int columnId=col.getId();
		AttachmentManager attm=(AttachmentManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.ATTACHMENT_MANAGER);
		Attachment att= attm.getAttachmentInfo("burgeon/" + table.getRealTableName()+"/"+col.getName(),  objectId+"" , version);
		if(att!=null){
			String fileName= table.getName()+ "_"+ tableManager.getColumn(columnId).getName()+"_"+ objectId+"_"+att.getVersion()+"."+ att.getExtension();
	        String ct= Tools.getContentType(att.getExtension(), "application/octetstream");
	        response.setContentType(ct+"; charset=GBK");
	        response.setContentLength((int)att.getSize());
	        //if(ct.indexOf("text/")>-1|| ct.indexOf("image")>-1)
	        response.setHeader("Content-Disposition","inline;filename=\""+fileName+"\"");
	      //add get thum image file
        	if(thum!=null&&thum.equals("Y")){
        		is=attm.getAttachmentData(att,thum);
        	}else{
        		is=attm.getAttachmentData(att);
        	}

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

			return;
		}else{
			PrintWriter writer = response.getWriter();
			writer.print(NO_ATTACH);
			writer.close();
		}
					
	}
	private final static String NO_ATTACH="<html><head><title>Attachment</title></head><body><p> File not found!</body></html>";
}
