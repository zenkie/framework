/******************************************************************
*$RCSfile: ImportExcelFileHandler.java,v $ $Revision: 1.7 $ $Author: Administrator $
* $Date: 2006/01/31 02:59:27 $
********************************************************************/
package nds.control.web.reqhandler;

import java.io.InputStream;
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
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.excel.ImportExcel;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.TableManager;
import nds.util.Configurations;
import nds.util.Tools;
import nds.util.WebKeys;

import org.apache.commons.fileupload.*;
//import org.apache.commons.fileupload.servlet.*; 



/**
 * Handle login information from web, will append remote address to the Event, so
 * server side can log more detailed information about client
 */
public class ImportExcelFileHandler extends RequestHandlerSupport {
    private static Logger logger=LoggerManager.getInstance().getLogger(ImportExcelFileHandler.class.getName());
    //private final static DateFormat dateFormatter =new SimpleDateFormat("MMddHHmmss");

    public ImportExcelFileHandler() {}

    public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
    	DateFormat dateFormatter =new SimpleDateFormat("MMddHHmmss");
    	try {
        	Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
        	if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
        	ImportExcel ei=new ImportExcel(locale);
            // log begin date to compute total time consumption
            ei.setParameter("begintime", ""+ System.currentTimeMillis() );

            HttpSession session=request.getSession(true);
            WebUtils.getSessionContextManager(session);
            Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
            UserWebImpl user= ((UserWebImpl)WebUtils.getSessionContextManager(session).getActor(WebKeys.USER));
            String operatorName= user.getUserName() ; // this will be the sub directory name

            DiskFileUpload  fu = new DiskFileUpload();
            // maximum size before a FileUploadException will be thrown
            fu.setSizeMax(1024*1024 * Tools.getInt( conf.getProperty("import.excel.maxsize", "1"), 1)); // 1MB
            // maximum size that will be stored in memory
//            fu.setSizeThreshold(40960);
            // the location for saving data that is larger than getSizeThreshold()
//            fu.setRepositoryPath(conf.getProperty("export.root.nds","/aic/home"));
            
            List fileItems = fu.parseRequest(request);
            Iterator iter = fileItems.iterator();
            Properties props=new Properties();

            while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (item.isFormField()) {
                        props.setProperty(item.getFieldName(), item.getString());
                        // add to request attributes for later usage
                        // since 2.0
                        request.setAttribute(item.getFieldName(),item.getString() );
                    } else {
                        InputStream excelFileStream=item.getInputStream();
                        ei.setSourceInputStream(excelFileStream);
                    }
            }

            int tableId= Tools.getInt(props.getProperty("table"), -1);
            //logger.debug("table id="+ tableId) ;
            ei.setMainTable(tableId );
            ei.setStartRow(Tools.getInt(props.getProperty("startRow"), 1)) ;
            boolean bgRun="true".equalsIgnoreCase(""+props.getProperty("bgrun"));
            
            boolean update_on_unique_constraints="true".equalsIgnoreCase(""+props.getProperty("update_on_unique_constraints"));
            
            String df= dateFormatter.format(new Date());
            if(bgRun) ei.setParameter("outputfile", conf.getProperty("export.root.nds","/aic/home") +"/"+user.getClientDomain()+ "/"+ operatorName+"/OutExcel"+ df +".txt");
            Enumeration enu= props.keys() ;
            while(enu.hasMoreElements()) {
                String name=(String) enu.nextElement();
                String value= props.getProperty(name);
                ei.setParameter(name, value);
            }

            DefaultWebEvent event=ei.createEvent();
            event.setParameter("update_on_unique_constraints",  update_on_unique_constraints?"Y":"N");

            if(user !=null && user.getSession()!=null)
            	event.put("nds.query.querysession",user.getSession());
            return event;
        }
        catch (Exception ex) {
            logger.error("error handling import excel request", ex);
            if(ex instanceof NDSEventException) throw (NDSEventException)ex;
            else throw new NDSEventException("@eception@:"+ ex);
        }
    }
}
