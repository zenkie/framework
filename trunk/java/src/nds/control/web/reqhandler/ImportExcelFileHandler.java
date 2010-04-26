/******************************************************************
*$RCSfile: ImportExcelFileHandler.java,v $ $Revision: 1.7 $ $Author: Administrator $
* $Date: 2006/01/31 02:59:27 $
********************************************************************/
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



/**
 * Handle login information from web, will append remote address to the Event, so
 * server side can log more detailed information about client
 * 
 * Supporting xls file and txt, csv type since portal 4.0
 */
public class ImportExcelFileHandler extends RequestHandlerSupport {
    private static Logger logger=LoggerManager.getInstance().getLogger(ImportExcelFileHandler.class.getName());
    //private final static DateFormat dateFormatter =new SimpleDateFormat("MMddHHmmss");

    public ImportExcelFileHandler() {}

    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
    	DateFormat dateFormatter =new SimpleDateFormat("MMddHHmmss");
    	InputStream excelFileStream=null;
        Properties props=new Properties();
    	
    	try {
        	Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
        	if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
        	ImportExcel ei=new ImportExcel(locale);
            // log begin date to compute total time consumption
            ei.setParameter("begintime", ""+ System.currentTimeMillis() );
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

            while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (item.isFormField()) {
                        props.setProperty(item.getFieldName(), item.getString());
                        // add to request attributes for later usage
                        // since 2.0
                        request.setAttribute(item.getFieldName(),item.getString() );
                    } else {
                    	String fileName = item.getName();
                    	
                        excelFileStream=item.getInputStream();
                        ei.setSourceInputStream(excelFileStream);
                        ei.setSourceFile(fileName);
                    }
            }

            // 由于swfobject的flash的bug，session信息拿不到，我们在js里上传了jsessionid，在服务器端重新找到session
            UserWebImpl user=null;// ((UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER));
            
        	String jsessionId=props.getProperty("JSESSIONID");
        	if(jsessionId!=null ){
        	    ServletContextManager manager= WebUtils.getServletContextManager();
        	    SecurityManagerWebImpl se=(SecurityManagerWebImpl)manager.getActor(nds.util.WebKeys.SECURITY_MANAGER);
        		SessionInfo si= se.getSession(jsessionId);
        		if(si!=null){
        			user= si.getUserWebImpl();
        		}else{
            		user= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(WebKeys.USER));
        		}
        	}else{
        		user= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(WebKeys.USER));        	
        	}
        	if(user==null || user.isGuest()){
        		throw new NDSEventException("Please login");
        	}
            // 如果存在table, objectid, 并且 table 是明细表，则认为objectid就是父表的ID，将确认父表必须为允许修改的状态
        	checkParentTableWritePermission(props);
        	
        	logger.debug(Tools.toString(props));
        	
            //if request contains formRequest, direct to that page if error found
            //see MainServlet for details of this variable usage
            String formRequest=props.getProperty("formRequest");
            if(formRequest!=null) request.setAttribute("formRequest", formRequest);
            
            int tableId= Tools.getInt(props.getProperty("table"), -1);

            /*logger.debug(Tools.toString(props));
            if("true".equals(props.getProperty("partial_update"))){
            	nds.control.ejb.command.SavePreference.setPreferenceValues(user.getUserId(),
            			"imp.upd."+TableManager.getInstance().getTable(tableId).getName().toLowerCase(),props);
            	
            }else{
            	nds.control.ejb.command.SavePreference.setPreferenceValues(user.getUserId(),
            			"imp.ins."+TableManager.getInstance().getTable(tableId).getName().toLowerCase(),props);
            	
            }*/

            //logger.debug("table id="+ tableId) ;
            ei.setMainTable(tableId );
            ei.setStartRow(Tools.getInt(props.getProperty("startRow"), 1)) ;
            ei.setStartColumn(Tools.getInt(props.getProperty("startColumn"), 1)) ;
            ei.setStartSkip(Tools.getInt(props.getProperty("startSkip"), 0)) ;
            boolean bgRun="true".equalsIgnoreCase(""+props.getProperty("bgrun"));
            
            boolean update_on_unique_constraints="true".equalsIgnoreCase(""+props.getProperty("update_on_unique_constraints"));
            
            String df= dateFormatter.format(new Date());
            if(bgRun) ei.setParameter("outputfile", conf.getProperty("export.root.nds","/aic/home") +"/"+user.getClientDomain()+ "/"+ user.getUserName()+"/OutExcel"+ df +".txt");
            Enumeration enu= props.keys() ;
            while(enu.hasMoreElements()) {
                String name=(String) enu.nextElement();
                String value= props.getProperty(name);
                ei.setParameter(name, value);
            }
             
            DefaultWebEvent event=ei.createEvent(user);
            event.setParameter("update_on_unique_constraints",  update_on_unique_constraints?"Y":"N");

            if(user !=null && user.getSession()!=null)
            	event.put("nds.query.querysession",user.getSession());
            return event;
        }
        catch (Exception ex) {
            logger.error("error handling import excel request", ex);
            if(ex instanceof NDSEventException) throw (NDSEventException)ex;
            else throw new NDSEventException("@eception@:"+ ex);
        }finally{
        	try{
        		if(excelFileStream!=null ) excelFileStream.close();
        	}catch(Throwable t){}
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
