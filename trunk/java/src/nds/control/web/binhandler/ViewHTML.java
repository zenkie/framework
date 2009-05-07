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
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.util.JRLoader;
import nds.report.*;

/**
 * Return HTML
 * @author yfzhu@agilecontrol.com
 */

public class ViewHTML implements BinaryHandler{
    /**
     */
	public void process(HttpServletRequest request,HttpServletResponse  response)  throws Exception{
		ReportPrinter printer=new ReportPrinter();
		JasperPrint jasperPrint= printer.printForeground(request);

		JRHtmlExporter exporter = new JRHtmlExporter();

		Map imagesMap = new HashMap();
		//session.setAttribute("IMAGES_MAP", imagesMap);
		exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		/**
		 * yfzhu 2005-08-25 find bug using writer, can not handle UTF fonts
		 */
/*		PrintWriter out = response.getWriter();
		exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, out);
*/		
		ServletOutputStream ouputStream = response.getOutputStream();
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, ouputStream);
		
		exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, imagesMap);
		exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, new Boolean(true));
		// some report will generate image file, such as barcode, so this param specifies where to store the image files
		String dir= "dir"+Sequences.getNextID("nds.control.web.ViewHTML");
		String path= ((Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS)).getProperty("dir.tmp","/act/tmp");
		exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,  path+"/"+ dir);
		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/servlets/binserv/Image?dir="+ dir+"&image=");
		
		exporter.exportReport();


		
	}
}
