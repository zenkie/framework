package nds.control.web.reqhandler;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.web.SecurityManagerWebImpl;
import nds.control.web.ServletContextManager;
import nds.control.web.SessionInfo;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.excel.ImportExcel;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.*;
import nds.util.Configurations;
import nds.util.Tools;
import nds.util.WebKeys;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*; 
import org.json.JSONObject;



/**
 *  配合自定义上传文件客户端的处理。后端处理流程转向WebAction定义的脚本
 *  inited by yfzhu 2009-11-22
 *  @since 4.0
 */
public class FileUploadHandler extends RequestHandlerSupport {
    private static Logger logger=LoggerManager.getInstance().getLogger(FileUploadHandler.class.getName());

    public FileUploadHandler() {}

    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
    	//long begintime=System.currentTimeMillis() ;
    	
    	try {
        	Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
        	if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
        	DefaultWebEvent ei=new DefaultWebEvent("CommandEvent");
        	
        	ei.setParameter("command", "ExecuteWebAction");
    	  	JSONObject jo=new JSONObject();
    	  	
    	  	
            Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
            
            
//          Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

//             Set factory constraints
            factory.setSizeThreshold(1024*1024 * Tools.getInt( conf.getProperty("import.excel.maxsize", "1"), 1)); // 1MB
            factory.setRepository(new File(conf.getProperty("dir.tmp","/portal/act.nea/tmp")));

//             Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

//             Set overall request size constraint
            upload.setSizeMax(1024*1024 * Tools.getInt( conf.getProperty("import.excel.maxsize", "1"), 1));

//             Parse the request
            List /* FileItem */ fileItems = upload.parseRequest(request);
            
            Iterator iter = fileItems.iterator();
            
            String fileName=null;
            FileItem fileItem=null;
            while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (item.isFormField()) {
                        jo.put(item.getFieldName(), item.getString());
                        // add to request attributes for later usage
                        // since 2.0
                        request.setAttribute(item.getFieldName(),item.getString() );
                    } else {
                    	fileItem = item;
                    }
            }
            if(fileItem==null) throw new NDSEventException("No file uploaded");
            // 由于swfobject的flash的bug，session信息拿不到，我们在js里上传了jsessionid，在服务器端重新找到session
            UserWebImpl user=null;// ((UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER));
            
        	String jsessionId=jo.optString("JSESSIONID");
        	if(jsessionId!=null ){
        	    ServletContextManager manager= WebUtils.getServletContextManager();
        	    SecurityManagerWebImpl se=(SecurityManagerWebImpl)manager.getActor(nds.util.WebKeys.SECURITY_MANAGER);
        		SessionInfo si= se.getSession(jsessionId);
        		if(si!=null){
        			user= si.getUserWebImpl();
        			
        		}else{
            		user= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(WebKeys.USER));
        		}
                ei.setParameter("JSESSIONID", jsessionId);//later will check this for permission
        		
        	}else{
        		user= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(WebKeys.USER));        	
        	}
        	if(user==null || user.isGuest()){
        		throw new NDSEventException("Please login");
        	}
        	//save file
        	
            String svrPath = conf.getProperty("export.root.nds","/aic/home")+ 
            	File.separator +  user.getClientDomain()+File.separator+ user.getUserName();
        	
            String absFileName= svrPath+File.separator+ fileItem.getName();
            fileItem.write(new File(absFileName));
            
            jo.put("file", absFileName);
            jo.put("javax.servlet.http.HttpServletRequest",request);

            jo.put("objectid",0);// fake one, must set to 0, so webaction will take as can display(permission ok)

            //add file to query object so db procedure can handle
            Object q=jo.opt("query");
            logger.debug("q="+ q+", "+ (q==null?"null": q.getClass().getName()));
            JSONObject query=null;//jo.optJSONObject("query");
            if(q instanceof String){
            	try{
            		query=new JSONObject((String)q);
            	}catch(Throwable t){
            		logger.error("not a valid json:"+ q);
            	}
            }else query=new JSONObject();
            query.put("file",absFileName);
            jo.put("query", query);
            
            logger.debug("by user " + user.getUserName()+":"+jo.toString());
            ei.put("JSONOBJECT", jo);
            return ei;
        }
        catch (Exception ex) {
            logger.error("error handling import excel request", ex);
            if(ex instanceof NDSEventException) throw (NDSEventException)ex;
            else throw new NDSEventException("@eception@:"+ ex);
        }finally{
        	
        }
    }
    private void checkParentTableWritePermission(Properties props) throws Exception{
    	int objectId= Tools.getInt(props.getProperty("objectid"),-1);

    	if(objectId==-1) return;

       	int tableId= Tools.getInt(props.getProperty("table"),-1);
       	Table parentTable=TableManager.getInstance().getTable(tableId).getParentTable();
       	if(parentTable==null) return;
       	
       	java.sql.Connection conn=nds.query.QueryEngine.getInstance().getConnection(); 
       	try{
       		nds.control.ejb.DefaultWebEventHelper.checkTableRowsModifiable(parentTable,new int[]{objectId}, conn);
       	}finally{
       		if(conn!=null){try{conn.close();}catch(Throwable t){}}
       	}
    }
}
