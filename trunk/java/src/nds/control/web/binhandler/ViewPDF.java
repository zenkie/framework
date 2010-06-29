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
//import nds.portlet.util.*; 
import nds.control.web.*;
import net.sf.jasperreports.engine.*;
import nds.report.*;

/**
 * Return pdf
 * @author yfzhu@agilecontrol.com
 */

public class ViewPDF implements BinaryHandler{
	public void init(ServletContext context){}
    /**
     */
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		ReportPrinter printer=new ReportPrinter();
		JasperPrint jasperPrint= printer.printForeground(request);

		byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);
		
		response.setContentType("application/pdf");
		response.setContentLength(bytes.length);
		ServletOutputStream ouputStream = response.getOutputStream();
		ouputStream.write(bytes, 0, bytes.length);
		ouputStream.flush();
		ouputStream.close();

					
	}
}
