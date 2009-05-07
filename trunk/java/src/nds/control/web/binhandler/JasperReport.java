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
import nds.query.QueryEngine;
import nds.control.web.*;
import net.sf.jasperreports.engine.*;
import nds.report.*;

/**
 * Return JasperReport
 * @author yfzhu@agilecontrol.com
 */

public class JasperReport implements BinaryHandler{
    private Logger logger= LoggerManager.getInstance().getLogger(JasperReport.class.getName());
	
	/**
	*  return OutputStream of JasperReport object, this page could only be viewed from localhost
	*  for security concern.
	*  parameter can be (id), or (table and type)
	*  @param id - report id, or
	* 
	*  @param table - table name
	*  @param type  - reporttype "s","l","o", case insensitive
	*  
	*  @param client(*) - client domain
	*  @param version - version number, default to -1
	*
	*/
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		String clientName= request.getParameter("client");
		int objectId= ParamUtils.getIntAttributeOrParameter(request, "id", -1);
		if( objectId ==-1){
			// try using table and type
			objectId= getReportId(clientName, request.getParameter("table"), request.getParameter("type"));
		}
		if( objectId ==-1){
			logger.error("report not found, request is:"+ Tools.toString(request));
			throw new NDSException("report not found");
		}
		int version= ParamUtils.getIntAttributeOrParameter(request, "version", -1);
		File reportXMLFile=new File( ReportTools.getReportFile(objectId, clientName));
		if( reportXMLFile.exists()){
			// generate jasperreport if file not exists or not newer
			String reportName=reportXMLFile.getName().substring(0, reportXMLFile.getName().lastIndexOf("."));
			File reportJasperFile = new File(reportXMLFile.getParent(), reportName+ ".jasper");
			if( !reportJasperFile.exists()|| reportJasperFile.lastModified()<reportXMLFile.lastModified()){
				JasperCompileManager.compileReportToFile(reportXMLFile.getAbsolutePath(),reportJasperFile.getAbsolutePath() );
			}
			InputStream is=new FileInputStream(reportJasperFile);
	        response.setContentType("application/octetstream;");
	        response.setContentLength((int)reportJasperFile.length());
	        
	        //response.setHeader("Content-Disposition","inline;filename=\""+reportJasperFile.getName()+"\"");
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
		}else{
			throw new NDSException("Not found report template"); 
		}
					
	}
	/**
	 * 
	 * Find report with input, throw exception if could not found. When there's more than one, use
	 * id small one
	 * @param clientDomain 
	 * @param tableName 
	 * @param reportType
	 * @return
	 * @throws Exception
	 */
	private int getReportId(String clientDomain, String tableName, String reportType) throws Exception{
		reportType= reportType.toUpperCase();
		Table table= nds.schema.TableManager.getInstance().getTable(tableName);
		if( table ==null) throw new NDSException("table "+ tableName + " not found.");
		String sql="select r.id from ad_report r, ad_client c where c.domain='"+ clientDomain+"' and r.ad_table_id="+table.getId()+" and r.reporttype='"+ reportType+"' and c.id=r.ad_client_id order by id asc";
		return Tools.getInt(QueryEngine.getInstance().doQueryOne(sql), -1);
	}
}
