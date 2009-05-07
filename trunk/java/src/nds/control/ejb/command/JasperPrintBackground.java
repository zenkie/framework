/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.util.Tools;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;

/**
 * Print jasper report to file, so the ui will only get a message about success or not
 * 
 * @author yfzhu@agilecontrol.com
 */

public class JasperPrintBackground extends Command{
	/**
	 * @param event parameters:
	 * 		"reportobject"	- JasperReport
	 * 		"reportparam"   - HashMap 
	 * 		"filetype"	- file type supported: "pdf"  "excel" "csv" "xml" (embed image)
	 * 		"destfolder"	- file storage folder, the output file and report file will be stored in.
	 * 		"destfile"		- report file name, without folder information.
	 * This method will also return information about file in valueholder, as which stored in output file.
	 * And note this method will not be asynchronous, that should be handled in ClientControlerWebImpl.  
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	long startTime= System.currentTimeMillis();
    	String destFolder= (String)event.getParameterValue("destfolder");
    	String fileName= (String)event.getParameterValue("destfile");
    	String destFile	=destFolder + java.io.File.separator+ fileName  ;
    	String fileType= (String)event.getParameterValue("filetype");
    	JasperReport report= (JasperReport)event.getParameterValue("reportobject");
    	HashMap parameters=  (HashMap)event.getParameterValue("reportparam");
    	String message=null;
    	nds.security.User usr= helper.getOperator(event);
    	ValueHolder holder=new ValueHolder();
    	java.sql.Connection conn=QueryEngine.getInstance().getConnection();
    	try{
    		// catch all errors so write to destfolder
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, conn);
    		if( "pdf".equalsIgnoreCase(fileType)){
    			JasperExportManager.exportReportToPdfFile(jasperPrint, destFile);
    		}else if("xls".equalsIgnoreCase(fileType)){
				JRXlsExporter exporter = new JRXlsExporter();
				
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
				exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile);
				exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
				
				exporter.exportReport();
    		}else if("csv".equalsIgnoreCase(fileType)){
				JRCsvExporter exporter = new JRCsvExporter();
				exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
				exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile);
				
				exporter.exportReport();
    		}else if("xml".equalsIgnoreCase(fileType)){
    			JasperExportManager.exportReportToXmlFile(jasperPrint, destFile, true/*isEmbeddingImages*/);
    		}else
    			throw new NDSException("Unsupported file type:"+ fileType);
    		// write to file for success
    		int seconds= (int)((System.currentTimeMillis() - startTime)/1000);
    		message=  (new Date(startTime))+ ", "+ seconds +" seconds:" + fileName;
    		Tools.writeFile(destFolder+"/print.log", true,Tools.LINE_SEPARATOR+ message, "UTF-8");
	    	holder.put("code","0");
    	}catch(Throwable e){
    		logger.error("User "+ usr.getName() + "@" + usr.getClientDomain()+" fail to print background:"+ destFile, e);
    		message=  (new Date(startTime))+ ", fail to create print file:" + destFile+ ":"+ e;
    		try{
    			Tools.writeFile(destFolder+"/print.log", true, Tools.LINE_SEPARATOR+ message, "UTF-8");
    		}catch(Exception ex){
    			logger.error("Could not write file: "+ destFolder+"/print.log :" + e);
    		}
    		holder.put("code", "-1");
    	}finally{
    		try{
    			conn.close();
    		}catch(Exception e){}
    	}
		holder.put("message", message);
    	return holder;
    }
}
