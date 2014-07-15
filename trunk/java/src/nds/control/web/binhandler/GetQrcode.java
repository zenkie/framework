/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.web.binhandler;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.schema.*;
import nds.util.*;
import nds.weixin.ext.RestControl;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

import java.util.*;
import java.io.*;

import nds.control.web.*;

/**
 * 根据请求获取二维码图片
 * 
 * @author jackrain@hotmail.com
 */

public class GetQrcode implements BinaryHandler{
	private Logger logger= LoggerManager.getInstance().getLogger(GetQrcode.class.getName());	 
	public void init(ServletContext context){}
    /**
     * Things needed in this page:
     *  table* - (int) table id
     *  column* - (int) column id
     *  objectid* - (int) record id
     *  version - (int ) -1 if not found
     */
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		
		UserWebImpl userWeb =null;
		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	
		java.sql.Connection conn = null;
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);	    
		String clientWebRoot=conf.getProperty("webclient.upload","/act.net/webhome");
		try{
		WeUtilsManager Wemanage = WeUtilsManager.getInstance();
		WeUtils wu = Wemanage.getByAdClientId(userWeb.getAdClientId());
		
		TableManager tableManager=TableManager.getInstance();
		int tableId= ParamUtils.getIntAttributeOrParameter(request, "table", -1);
		int objectId =ParamUtils.getIntAttributeOrParameter(request, "objectid", -1);
		Table table;
		table= tableManager.getTable(tableId);
		QueryEngine engine= QueryEngine.getInstance();
		conn= engine.getConnection();
		 
		ArrayList list = new ArrayList();
		list.add(objectId);
		list.add(table.getName());
		ArrayList res = new ArrayList();
		res.add(String.class);
		java.util.Collection result = QueryEngine.getInstance()
				.executeFunction("GET_PATHURL", list, res,conn);
		String purl = (String) result.toArray()[0];
		
	   if(purl!=null){
		   
	        String ct= Tools.getContentType("png", "application/octetstream");
	        response.setContentType(ct+"; charset=GBK");
	        //if(ct.indexOf("text/")>-1|| ct.indexOf("image")>-1)
	        response.setHeader("Content-Disposition","inline;filename=\""+objectId+"_qrcode.png\"");
	      //add get thum image file
			qrcode handler = new qrcode();
			ServletOutputStream os = response.getOutputStream();
			
			if(nds.util.Tools.getInt(wu.getWXType(),0)==4){
				purl="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wu.getAppId()
						+"&redirect_uri="+purl+"&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
			}
			logger.debug("purl->"+purl);
			if(table.getName().equalsIgnoreCase("WX_V_APPENDGOODS")){
				String psql="select t.ITEMPHOTO from WX_V_APPENDGOODS t where t.id="+objectId;
				String imgPath=(String) engine.doQueryOne(psql,conn);
				String fName = imgPath.trim();  
			    String fileName = fName.substring(fName.lastIndexOf("/")+1);  
				String path= clientWebRoot+"/"+wu.getDoMain()+"/WX_APPENDGOODS/"+fileName;
				handler.encoderQRCode(purl,os,"png",18,path);
			}
			else{
				handler.encoderQRCode(purl, os, "png",18);
			}
			os.flush();
			os.close();

			return;
		}else{
			request.getRequestDispatcher(WebKeys.NDS_URI+"/images/noimg.png").forward(request, response);
		}
		
		}catch(Throwable t){
			logger.error("fail to get user file"+t);
			request.getRequestDispatcher(WebKeys.NDS_URI+"/images/noimg.png").forward(request, response);
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Throwable t) {
				}
			}
		}
					
	}
	private final static String NO_ATTACH="<html><head><title>Attachment</title></head><body><p> File not found!</body></html>";
}
