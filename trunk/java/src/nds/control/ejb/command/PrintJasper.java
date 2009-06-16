/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.json.*;
import nds.schema.*;
import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryRequest;
import nds.query.QueryRequestImpl;
import nds.query.QueryResultImpl;
import nds.report.ReportFactory;
import nds.report.ReportTools;
import nds.security.Directory;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.Sequences;
import nds.util.Tools;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Print jasper report to file, so the ui will only get a message about success or not
 * and then direct user to that page or print immediately
 * 
 * @author yfzhu@agilecontrol.com
 */

public class PrintJasper extends Command{
	/**
	 * @param event parameters:
	 * 		"tag"	- Return to client directly
	 * 		"params"   - HashMap contains everything set in print option page
	 * 			"table*" int
	 * 			"reporttype" String "L" for list, "O" for Object,  default to "O"
	 * 			"template" optional, if not exists,will read from user setting and then system default
	 * 			"fmt" optional, if not exists,will read from user setting and then system default
	 * 			"destfile" optional, if not exists, will using date to create new one
	 * 			
	 * 			"id" if for Object report, this is a must exist one
	 * 			"expr" this is optional in List report, must not exist either.
	 * @return 
	 * 		"tag" 
	 * 		"printfile" url of the file created, client will print it directly or show in (if tag="Preview") 
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	
    	ValueHolder holder=new ValueHolder();
    	java.sql.Connection conn=null;
    	try{
	    	nds.security.User user= helper.getOperator(event);
	    	JSONObject jo=event.getJSONObject();
	    	JSONObject params=jo.getJSONObject("params");
	    	String tag=jo.optString("tag");

	    	int tableId= params.getInt("table");
	    	Table table= TableManager.getInstance().getTable(tableId);


	    	//fetch default params: template and fmt
	    	WebContext ctx=(WebContext) jo.opt("org.directwebremoting.WebContext");
	    	String defaultTemplate=null, defaultFormat=null;
	    	UserWebImpl userWeb=null;
	  		if(ctx!=null){
		  		HttpServletRequest request = ctx.getHttpServletRequest();
		  		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(ctx.getSession()).getActor(nds.util.WebKeys.USER));			  		
		    	Properties props=userWeb.getPreferenceValues(table.getName().toLowerCase()+".print", false,false);
		    	defaultTemplate= props.getProperty("template"); // format like: cx123 for ad_cxtab(id=123) definitions, t123 for ad_report(id=123) definitions
		    	defaultFormat=props.getProperty("format", "pdf"); //default to htm format as output
	  		}
	    	  
	    	String template= params.optString("template",defaultTemplate); //format like: cx123 for ad_cxtab(id=123) definitions, t123 for ad_report(id=123) definitions
	    	if(nds.util.Validator.isNull(template)){
	    		template= getPrintTemplate(userWeb,table);
	    	}
	    	
	    	boolean isCxtabJReport= template.substring(0,2).equals("cx");
	    	int reportId=-1;
	    	if(isCxtabJReport )reportId= Tools.getInt(template.substring(2), -1);
	    	else
	    		reportId= Tools.getInt(template.substring(1), -1);
	    	if(reportId==-1) throw new NDSException("report template not correct:"+ template);
	    	
	    	String fileType = params.optString("fmt",defaultFormat);
	    	
	    	String fileName= params.optString("destfile");
	    	if(nds.util.Validator.isNull(fileName)){
	    		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMddHHmm");
	    		fileName= "rpt"+sdf.format(new Date());
	    	}
	    	Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
	    	String destFolder = conf.getProperty("export.root.nds","/aic/home") + File.separator +  user.getClientDomain()+File.separator+  user.name;
	    	File fold= new File(destFolder);
	    	fold.mkdirs();
	    	String destFile	=destFolder + java.io.File.separator+ fileName+"."+ fileType  ;
	    	
	    	String reportType= params.optString("reporttype","O"); // L for List , O for Object
	    	
	    	int objectId= params.optInt("id",-1);
	    	String expr=params.optString("expr");
	    	
	    	Map parameters = new HashMap();
	    	// common parameters
	    	parameters.put("user", user.name);
	    	parameters.put("jobid", ReportTools.createJobId(user.id+""));
	    	// for special handle
	    	parameters.put("userid", String.valueOf(user.id));
	    	parameters.put("ad_client_id", String.valueOf(user.adClientId));   
	    	
	    	if("L".equals(reportType)){
	    		if(! ((nds.control.util.SecurityUtils.getPermission(table.getSecurityDirectory(), user.id.intValue())
	    				& Directory.EXPORT)==Directory.EXPORT) ){
	    			throw new NDSException("@no-permission@");
	    		}
	    		QueryRequestImpl q= QueryEngine.getInstance().createRequest(event.getQuerySession());
	    		q.setMainTable(table.getId());
	    		q.addAllShowableColumnsToSelection(Column.PRINT_LIST, false);
	    		String where=null;
	    		if(nds.util.Validator.isNotNull(expr))
	    			where=q.addParam( new Expression(expr));
	    		
	    		q.setOrderBy( new ColumnLink(params.getString("order_by")).getColumnIDs(), "true".equals(params.getString("order_asc")));
	    		q.setRange(0,  Integer.MAX_VALUE);
	    		String sql= q.toSQL();
	    		logger.debug(sql);
	    		params.put("sql", sql);
	    		if(where!=null) params.put("where",where);
	    		else params.put("where","1=1");
	
	    		
	    	}else{
	        	parameters.put("objectid", new Integer(objectId));
	    		if(!isCxtabJReport){
	    			// will create several sql for internal usage
	    			addSQLParam(parameters,table.getId(),objectId,userWeb);
	    		}
	    	}
	    	JasperReport report=null;
	    	if(isCxtabJReport){
	    		report = nds.cxtab.JReport.getJasperReport(reportId);    		
	    	}else{
	    		report = (JasperReport)JRLoader.loadObject(new java.net.URL(ReportTools.getReportURL(reportId, user.getClientDomain())));	    	
	    	}
	    	
	    	
	    	
	    	String message=null;
	    	nds.security.User usr= helper.getOperator(event);
	    	
	    	JSONObject retData=new JSONObject();
	    	retData.put("tag", tag);
	    	
	    	conn=QueryEngine.getInstance().getConnection();
    	
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
    		}else if("htm".equalsIgnoreCase(fileType)){

    			JRHtmlExporter exporter = new JRHtmlExporter();

    			Map imagesMap = new HashMap();
    			//session.setAttribute("IMAGES_MAP", imagesMap);
    			exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
    			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);

    			exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile);
    			
    			exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, imagesMap);
    			exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, new Boolean(true));
    			// some report will generate image file, such as barcode, so this param specifies where to store the image files
    			String dir= "dir"+Sequences.getNextID("nds.control.web.ViewHTML");
    			String path= ((Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS)).getProperty("dir.tmp","/act/tmp");
    			exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,  path+"/"+ dir);
    			exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/servlets/binserv/Image?dir="+ dir+"&image=");
    			
    			exporter.exportReport();
    			
    		}else
    			throw new NDSException("Unsupported file type:"+ fileType);
	    	holder.put("code","0");
	    	retData.put("printfile", fileName+"."+fileType);
	    	holder.put("data",retData);
    	}catch(Throwable e){
    		logger.error("Fail to print background:", e);
    		if( e instanceof NDSException) throw (NDSException)e;
    		else throw new NDSException(e.getMessage(), e);
    		//holder.put("code", "-1");
    	}finally{
    		try{
    			conn.close();
    		}catch(Exception e){}
    	}
		
    	return holder;
    }
    /**
     * Get template format like: cx123 for ad_cxtab(id=123) definitions, t123 for ad_report(id=123) definitions
     * @param table
     * @return
     * @throws Exception if not find any print template
     */
    private String getPrintTemplate(UserWebImpl userWeb, Table table) throws Exception{
    	if(userWeb!=null){
	    	int id=Tools.getInt(QueryEngine.getInstance().doQueryOne(
	    			"select id from ad_cxtab where ad_client_id=" +userWeb.getAdClientId() +" and ad_table_id="+table.getId() +
	    			" and reporttype='P' and rownum< 2 order by orderno, id"),-1);
	    	if(id!=-1) return "cx"+id;
	    	
	    	
	    	nds.query.QueryResult rt=userWeb.getReportTemplates(table, "O");
	    	if(rt.getRowCount()>0){
	    		rt.next();
	    		return "t"+rt.getObject(1);
	    	}
    	}
    	throw new NDSException("@print-template-not-set@");
    	
    }
	/**This is for object report
	 * Add "sql_main" and all related table's sql to param
	 * @param reportId
	 * @param params
	 * @param tableId
	 * @param objectId
	 * @param user
	 */
	private void addSQLParam( Map params, int tableId, int objectId, UserWebImpl user) throws Exception{
		/**
		 * ("org.directwebremoting.WebContext") not set in event will cause to this one
		 */
		if( user==null) throw new NDSException("Internal error: user not found in printing");
		QueryRequestImpl q=  QueryEngine.getInstance().createRequest(user.getSession());
		TableManager manager= TableManager.getInstance();
		Table table= manager.getTable(tableId);
		q.setMainTable(table.getId());
		q.addAllShowableColumnsToSelection(Column.PRINT_OBJECT, true);
		q.addParam( new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}), "="+ objectId, null ));
		String sql= q.toSQL();
		logger.debug("sql_main:"+sql);
		params.put("sql_main", sql);
		// tabs
		// 由于模板是按照table.getRefByTables() 来生成的，而用户的tab 可能由于权限的原因被去除一些tab
		// 故这里应该为去除的tab 生成一个查询结果为空的sql
		ArrayList defTabs= table.getRefByTables();
		ArrayList rfts= user.constructTabs(table, objectId); // 还包括了一个id=-1 的rft （主表本身），应该过滤掉
		boolean found=false;
		for(int i=0;i< defTabs.size();i++){
			RefByTable rft= (RefByTable)defTabs.get(i);
			if( rft.getId()==-1 ) continue ;// the main table itself
			found =false;
			for(int j=0;j< rfts.size();j++){
				if(rft.getId()==((RefByTable)rfts.get(j)).getId()){
					found=true;
					break;
				}
			}
	    	Table tb= manager.getColumn(rft.getRefByColumnId()).getTable();
			q =  QueryEngine.getInstance().createRequest(user.getSession());
			q.setMainTable(tb.getId() );
			q.addAllShowableColumnsToSelection(Column.PRINT_SUBLIST, false);
			if(found){
				// so generate sql with user's permission
				q.addParam( new Expression(new ColumnLink(new int[]{rft.getRefByColumnId()}), "="+ objectId, null ));
			}else{
				// a blank sql with no record return
				q.addParam( new Expression(null, "1=-1", null ));
			}
			sql= q.toSQL();
			String sqlParamName= ReportFactory.getSubReportSQLParamName(tb, rft.getId());
			logger.debug(sqlParamName+":"+ sql);
			params.put(sqlParamName, sql);
		}
		
	}    
}
