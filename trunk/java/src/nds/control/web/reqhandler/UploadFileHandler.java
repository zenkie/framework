/******************************************************************
*$RCSfile: UploadFileHandler.java,v $ $Revision: 1.2 $ $Author: Administrator $
* $Date: 2005/08/28 00:27:03 $
********************************************************************/
package nds.control.web.reqhandler;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.web.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.Configurations;
import nds.util.*;
import nds.util.WebKeys;

import org.apache.commons.fileupload.*;



/**
目标：管理用户的各种文档
功能：文档列表，分类说明，文档上传，下载，权限管理，文档版本管理，集成业务系统，如产品资料，订单产品配置表

思路：建立新的obtainmanner=attach, 为url+button 的方式，url 为下载地址，button 为上传按钮，
点击上传按钮打开上传页面，可上传文件，系统将文件放在固定目录中
upload.root/$clientdomain/tablename/columnname/objectId-dir, 
目录中存放对应的附件，按版本号依次为1.ext, 2.ext, 附件描述文件attachment.properties

另外，也应当允许指定系统外的一个地址 (upload=false 时，取 fileurl 指定的值作为url)。

在字段中保存的是链接信息，如果是上传的文件，自动记录为
/attach?table=1032&column=10039&objectid=9290
如果是链接信息，由用户自行指定

如果指明参数:copytoroot - 表示需要将文件拷贝到upload.root/$clientdomain/tablename/columnname
目录，这种情况仅适用于文件上传。这种方式可用于上传文件互相需要协助的情况，例如：报表
 */
public class UploadFileHandler extends RequestHandlerSupport {
    private static Logger logger=LoggerManager.getInstance().getLogger(UploadFileHandler.class.getName());

    public UploadFileHandler() {}

    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
        try {
        	DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
        	event.setParameter("command", "SaveAttachmentURL");
        	HttpSession session=request.getSession(true);
            WebUtils.getSessionContextManager(session);
            Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
            AttachmentManager attm=(AttachmentManager)WebUtils.getServletContextManager().getActor(WebKeys.ATTACHMENT_MANAGER);
            UserWebImpl user= ((UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER));
            
            String operatorName= user.getUserName() ; // this will be the sub directory name
            
            DiskFileUpload  fu = new DiskFileUpload();
            // maximum size before a FileUploadException will be thrown
            fu.setSizeMax(1024*1024*1024); // 1GB
            
            List fileItems = fu.parseRequest(request);
            Iterator iter = fileItems.iterator();
            InputStream in=null;
            String fileName="";
            while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (item.isFormField()) {
                        event.setParameter(item.getFieldName(), item.getString());
                        // add to request attributes for later usage
                        // since 2.0
                        request.setAttribute(item.getFieldName(),item.getString() );
                    } else {
                        in=item.getInputStream();
                        fileName= item.getName();
                    }
            }
            if(in !=null &&  Tools.getBoolean(event.getParameterValue("upload"), false)==true){
            // save to file system
        		TableManager manager= TableManager.getInstance();
        		Table table= manager.getTable( Tools.getInt(event.getParameterValue("table"), -1));
        		Column col= manager.getColumn( Tools.getInt(event.getParameterValue("column"), -1));
            	int objectId= Tools.getInt( event.getParameterValue("objectid"),-1);
                if(!user.hasObjectPermission(table.getName(),objectId,  nds.security.Directory.WRITE)){
                	throw new NDSException("权限不足！");
                }
            	
            	Attachment att= attm.getAttachmentInfo(user.getClientDomain()+"/" + table.getRealTableName()+"/"+col.getName(),  ""+objectId, -1);
            	if (att==null){
            		// create it
            		att= new Attachment( user.getClientDomain()+"/" + table.getRealTableName()+"/"+col.getName(),  objectId+"" );
            		att.setAuthor(user.getUserName());
            		att.setVersion(0);
            		att.setExtension(attm.getFileExtension(fileName));
            		att.setOrigFileName(fileName);
            	}else{
            		//add att new file name
            		att.setOrigFileName(fileName);
            	}
            	File f=attm.putAttachmentData(att, in);
            	
            	//if copytoroot, will copy file to 
            	//upload.root/$clientdomain/tablename/columnname
            	if( Tools.getBoolean(event.getParameterValue("copytoroot"),false)==true){
            		File ftemp= new File(fileName);
            		File f2=new File(attm.getRootPath(), user.getClientDomain()+"/" + table.getRealTableName()+"/"+col.getName()+"/"+ftemp.getName());
            		Tools.copyFile(f, f2, true, true);
            	}
            }
            if(user !=null && user.getSession()!=null)
            	event.put("nds.query.querysession",user.getSession());
            return event;
        }
        catch (Exception ex) {
            logger.error("error handling upload file request", ex);
            if(ex instanceof NDSEventException) throw (NDSEventException)ex;
            else throw new NDSEventException("无法处理上传请求:"+ ex);
        }
    }
}
