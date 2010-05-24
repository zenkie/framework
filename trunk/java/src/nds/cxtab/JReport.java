/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.cxtab;

import java.util.*;
import java.util.Date;
import java.sql.*;
import java.io.*;

import javax.servlet.ServletOutputStream;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.*;
import nds.report.ReportTools;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.control.web.*;
import nds.control.event.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.util.*;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.json.*;


/** 
 * Create jasperreport
 * 
 * @author yfzhu@agilecontrol.com
 */

public class JReport {
    private static Logger logger= LoggerManager.getInstance().getLogger((JReport.class.getName()));
	
	private String cxtabName;
	private int cxtabId=-1;
	private String filterExpr;
	private String queryStr;
	private String fileName;
	private int userId;
	private int adClientId;
	private String fileType;
	private int reportInstanceId=-1; // check ad_cxtab.AD_COLUMN_CXTABINST_ID , if set, will used this as instance id
									  // of query, and data will be limited to the range specified
	private int processInstanceId; // ad_pinstance.ID
	private String filterDesc;
	private Properties props;
	private long startTime;
	private Table factTable;
	private User user;
	private String sql;	
	private String whereClause; // filter setting from ui
	private HashMap params; // currently used for print one object
	/**
	 * Create report and save to file
	 * @return file name
	 */
	public String create(Connection conn) throws Exception{
        startTime=System.currentTimeMillis();

			String file;	
			// prepare file
	        Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			String exportRootPath=conf.getProperty("export.root.nds","/act/home");
			boolean isSystemInDebug=false;// "develope".equals( conf.getProperty("schema.mode", "production"));
			
			user= SecurityUtils.getUser( userId); 
			// check user output folder available size
			if ( ReportTools.getAvailableSpaceSize(user)<0){
				throw new NDSException("@no-free-space-for-user@:"+user.name);
			}
			
			QueryRequestImpl query=null;
			
			List ql= QueryEngine.getInstance().doQueryList(
					"select id,ad_table_id, attr2,name from ad_cxtab where name="+QueryUtils.TO_STRING(cxtabName)+
					" and ad_client_id=(select ad_client_id from users where id="+userId+")", conn);
			
			cxtabId= Tools.getInt( ((List)ql.get(0)).get(0),-1);
			int tableId= Tools.getInt( ((List)ql.get(0)).get(1),-1);
			String jasperFilePath= (String) ((List)ql.get(0)).get(2);
			
			JasperReport jreport= this.getJasperReport(jasperFilePath);

			HashMap parameters=new HashMap();
			/**
			 * from 2009.05.19, we do not allow jreport without table defined
			 */
			if( tableId==-1 ){
				if(true)throw new NDSException("table must be defined");
				// no ad_table, all parameters are read from ad_cxtab_jpara
				JSONObject jo= new JSONObject(queryStr);
				JRParameter[] params=jreport.getParameters();
				//List params=QueryEngine.getInstance().doQueryList("select name,paratype,ad_column_id from ad_cxtab_jpara where ad_cxtab_id="+ cxtabId, conn);
				for(int i=0;i<params.length;i++){
					JRParameter param= params[i];
					if(param.isSystemDefined() || !param.isForPrompting())continue;
					String pn= (String)param.getName();
					Object value= getValue(jo,pn,param.getDescription(),param.getValueClass());
					parameters.put(pn, value);
				}
			}else{
				if(params!=null){
					// when params set (this is used when print one object), will not create sql or where param
					for(Iterator it=params.keySet().iterator();it.hasNext();){
						String p= (String)it.next();
						parameters.put(p, params.get(p));
					}
				}else{
					query=this.prepareReport(conn);
					/* set following parameters in jasperreport
					 * $p(sql) - full sql generated from ui, according to ad_cxtab
					 * $p(where) - only where clause
					 */
					parameters.put("sql",sql);
					logger.debug("$p!{sql}:"+ sql);
					if(whereClause==null)
						parameters.put("where", "1=1");
					else {
						parameters.put("where", whereClause);
						logger.debug("$p!{where}:"+ whereClause);
					}
				}
			}
			parameters.put("ad_client_id", String.valueOf(user.adClientId));
			parameters.put("user", user.name);
	    	parameters.put("jobid", ReportTools.createJobId(user.id+""));
	    	parameters.put("userid", String.valueOf(user.id));
	    	parameters.put("ad_pi_id", String.valueOf(processInstanceId));
	    	
			JasperPrint jasperPrint = JasperFillManager.fillReport(jreport, parameters, conn);

   	        File destFolder = new File(exportRootPath+File.separator+ user.getClientDomain()+File.separator+  user.name);
			
   	        if(!destFolder.exists()){
   	        	if(!destFolder.mkdirs()){
   	        		throw new NDSException("@no-permission@( create folder:"+exportRootPath+File.separator+ user.getClientDomain()+File.separator+  user.name+")" );
   	        	}
   	        }
   	        
	    	String destFile	=destFolder + java.io.File.separator+ this.fileName+"."+ this.fileType ;
			
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
    		}else if("htm".equalsIgnoreCase(fileType)){
    			JRHtmlExporter exporter = new JRHtmlExporter();

    			Map imagesMap = new HashMap();
    			//session.setAttribute("IMAGES_MAP", imagesMap);
    			exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
    			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
    			FileOutputStream  ouputStream =new FileOutputStream(destFile);
    			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, ouputStream);
    			
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
    		// write to file for success
    		int seconds= (int)((System.currentTimeMillis() - startTime)/1000);
    		logger.debug( cxtabName+":"+  (new Date(startTime))+ ", "+ seconds +" seconds:" + this.fileName);
    		
//    		 create description file for report
		    createDescriptionFile(destFolder+File.separator+ this.fileName+"."+ this.fileType );
	  		//log running time
	  		nds.cxtab.CxtabReport.logRunningTime(cxtabId, userId, (int)(System.currentTimeMillis()-startTime), 0,conn);
		    
		    return  this.fileName+"."+ this.fileType;
	    		    
	}	
	/**
	 * Create description file in folder of user web folder, contents is from filterDesc
	 * @param file
	 */
	private void createDescriptionFile(String file){
		try{
			File f=new File(file);
			File descFolder=new File(f.getParent() + File.separator+ "desc");
			if(!descFolder.exists()) descFolder.mkdirs();
			String descFile= f.getParent() + File.separator+ "desc"+  File.separator+  f.getName();
			String p;
			if(nds.util.Validator.isNull(filterDesc)) p="";
			else p= ":"+filterDesc;
			nds.util.Tools.writeFile(descFile, "["+cxtabName+"]"+p, "UTF-8");
			 
		}catch(Throwable t){
			logger.error("Fail to write desc file for "+file+":"+ t);
		}
	}		
	/**
     * Parse java class name to param type:
     * 	 "N" - Number
     * 	 "S" - String
     *   "D" - Date 
     * @param javaclassName 
     * @return
     */
    private Object getValue(JSONObject jo,String name,String desc, Class clazz) throws Exception{
    	try{
	    	Object o= jo.opt(name);
	    	if(o==null) return null;
	    	if(clazz == java.lang.String.class){
	    		 return o.toString();
	    	}else if(clazz== java.lang.Integer.class){
	    		return new Integer(jo.getInt(name));   
	    	}else if(clazz==  java.lang.Double.class){
	    		return new Double(jo.getDouble(name));
	    	}else if(clazz== java.util.Date.class){
	    		java.text.SimpleDateFormat sf=(java.text.SimpleDateFormat) QueryUtils.dateNumberFormatter.get();
	    		return  sf.parse(o.toString());
	    	}
    	}catch(Throwable t){
    		logger.debug("error for "+ name, t);
    		throw new NDSException("@condition-error@:"+ desc);
    	}
    	throw new NDSException("@unsupported-class@:"+ clazz.getName() );
    }	
    /**
	 * 
	 * @param fileXMLPath ad_cxtab.attr2, xml file path
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static JasperReport getJasperReport(int cxtabId) throws Exception{
		String filePath=(String)QueryEngine.getInstance().doQueryOne("select attr2 from ad_cxtab where id="+cxtabId);
		if(Validator.isNull(filePath)){
			throw new NDSException("@jreport-file-not-found@");
		}
		return getJasperReport(filePath);   		
		
	}    
	/**
	 * 
	 * @param fileXMLPath ad_cxtab.attr2, xml file path, if relative(not started from "/"), 
	 * will relative to portal.properties#dir.jreport
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static JasperReport getJasperReport(String fileXMLPath) throws Exception{
		JasperReport jasperReport=null;
		
		if(!fileXMLPath.startsWith("/") ){
			//relative to portal.properties#dir.jreport
	        Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			String jreportDir=conf.getProperty("dir.jreport","/act.nea/jreport");
			fileXMLPath=jreportDir+File.separator+fileXMLPath;
			logger.debug("jreport file path:"+fileXMLPath);
		}
		File reportXMLFile=new File(fileXMLPath);
		
		if( reportXMLFile.exists()){    		

			String reportName=reportXMLFile.getName().substring(0, reportXMLFile.getName().lastIndexOf("."));
			File reportJasperFile = new File(reportXMLFile.getParent(), reportName+ ".jasper");
			if( !reportJasperFile.exists()|| reportJasperFile.lastModified()<reportXMLFile.lastModified()){
				JasperCompileManager.compileReportToFile(reportXMLFile.getAbsolutePath(),reportJasperFile.getAbsolutePath() );
			}
			jasperReport = (JasperReport)JRLoader.loadObject(reportJasperFile);
		}
		if(jasperReport==null) throw new NDSException("@jreport-file-not-found@");
		return jasperReport;
	}
	/**
	 * Create QueryRequest, if not on ad_table, return null;
	 * @param conn
	 * @throws Exception
	 */
	private QueryRequestImpl prepareReport(Connection conn) throws Exception{
        boolean isOnHTML=false;//isOnHTML when for html report, dimension that set "hidehtml"="N" will not queried
		QueryEngine engine=QueryEngine.getInstance();
		TableManager manager=TableManager.getInstance();
		
		List ed= engine.doQueryList("select ad_table_id,name from ad_cxtab where id="+ cxtabId, conn);
		int factTableId= Tools.getInt(((List)ed.get(0)).get(0),-1);
		
		if(factTableId==-1) return null;
		
		String cxtabDesc=(String) ((List)ed.get(0)).get(1);
		/*int factTableId= Tools.getInt(engine.doQueryOne(
				"select ad_table_id from ad_cxtab where id="+ cxtabId, conn), -1);*/
		factTable= manager.getTable(factTableId);
		
		List dimensionsH= engine.doQueryList("select columnlink, description, measure_order, hidehtml from ad_cxtab_dimension where ad_cxtab_id="+
				cxtabId+" and isactive='Y' and position_='H' "+ (isOnHTML?"and hidehtml='N'":"") +" order by orderno asc", conn);
		List dimensionsV= engine.doQueryList("select columnlink, description, measure_order,hidehtml from ad_cxtab_dimension where ad_cxtab_id="+
				cxtabId+" and isactive='Y' and position_='V' "+ (isOnHTML?"and hidehtml='N'":"") +" order by orderno asc", conn);
		
		// filter will be added to where clause directly
		String cxtabFilter= (String)engine.doQueryOne("select filter from ad_cxtab where id="+cxtabId, conn);
		
		
		
		Locale locale= user.locale;
		logger.debug("Locale for "+ user.getNameWithDomain()+"(id="+ userId+") is "+ locale);
		QuerySession qsession= QueryUtils.createQuerySession(userId,user.getSecurityGrade(), "", user.locale);
		QueryRequestImpl query=engine.createRequest(qsession);
		query.setMainTable(factTableId,true, cxtabFilter);

		//select
		if(dimensionsH!=null && dimensionsH.size()>0)for(int i=0;i< dimensionsH.size();i++){
			List dim= (List)dimensionsH.get(i);
			ColumnLink cl=new ColumnLink((String) dim.get(0));
			query.addSelection(cl.getColumnIDs(), false, (String)dim.get(1));
		}
		if(dimensionsV!=null && dimensionsV.size()>0)for(int i=0;i< dimensionsV.size();i++){
			List dim= (List)dimensionsV.get(i);
			ColumnLink cl=new ColumnLink((String) dim.get(0));
			query.addSelection(cl.getColumnIDs(), false, (String)dim.get(1));
		}
		logger.debug("filterExpr="+ filterExpr);
		logger.debug("query="+ query);
		// where
		Expression expr=null;
        // user-defined filter
        if(Validator.isNotNull(filterExpr)){
        	expr=new Expression(filterExpr);
            logger.debug("expr="+expr);
        }
		//security filter
        Expression sexpr= SecurityUtils.getSecurityFilter(factTable.getName(), Directory.READ,userId,qsession);
        logger.debug("sexpr="+sexpr);
        if(sexpr!=null && !sexpr.isEmpty() ){
        	if ((expr!=null && !expr.isEmpty()))
        		sexpr= expr.combine(sexpr,SQLCombination.SQL_AND,null);
        }else{
        	sexpr= expr;
        }
        // add ad_client filter again since whereClause need 
        if(factTable.isAdClientIsolated() ){
        	sexpr=( new Expression(new ColumnLink( factTable.getName()+".AD_CLIENT_ID"),
        			"="+ this.adClientId,null)).combine(sexpr,SQLCombination.SQL_AND,null);
        }
		
        logger.debug("sexpr="+sexpr);
        whereClause=this.parseVariable(QueryUtils.replaceVariables(query.addParam(sexpr), qsession));

		List measures=  engine.doQueryList("select ad_column_id, function_, userfact, description, VALUEFORMAT,valuename, param1,param2,param2 from ad_cxtab_fact where ad_cxtab_id="+
				cxtabId+" and isactive='Y' order by orderno asc",conn);
        //和平均有关的函数，包括avg, var,stdev，都不能让数据库进行group by 操作
        //而计数，最大，最小，累计等，可以先使用数据库完成有关group by运算
		//注意计算列 (以等号开头)的将不参与前期运算
        ArrayList facts=new ArrayList();
        ArrayList factDescs=new ArrayList();
        
        boolean isDBGroupByEnabled=true;
        boolean mustBeDBGroupBy=false;
        for(int i=0;i< measures.size();i++){
        	List mea= (List)measures.get(i);
        	String userFact= (String)mea.get(2);
        	if(Validator.isNotNull(userFact)){
        		if( userFact.startsWith("=")) continue;
        		// user fact 用于构造group by 语句，user fact 一般是一个汇总函数,如 count(distinct id)
        		facts.add(userFact);
        		factDescs.add(mea.get(3));
        		mustBeDBGroupBy=true;
        	}else{
            	String function= (String)mea.get(1);
        		int colId= Tools.getInt(mea.get(0),-1);
        		Column col= TableManager.getInstance().getColumn(colId);
        		
        		if(nds.jcrosstab.fun.FunUtil.isValidGroupByFunction(function)){
            		facts.add( function+"("+ factTable.getName()+"."+col.getName() + ")");
            		factDescs.add(mea.get(3));

        		}else{
        			isDBGroupByEnabled=false;
        		}
        	}
        }
        if(isDBGroupByEnabled){
        	sql= query.toGroupBySQL(facts );
        }else{
        	if(mustBeDBGroupBy) throw new NDSException("Cxtab configuration error, found user fact(db group by function) and invalid db group by function (e.g. avg) in the same time");
        	for(int i=0;i< measures.size();i++){
            	List mea= (List)measures.get(i);
            	// may not have user fact 
           		int colId= Tools.getInt(mea.get(0),-1);
           		Column col= TableManager.getInstance().getColumn(colId);
           		if(col!=null)query.addSelection( colId );
            }
        	sql= query.toSQL();
        }
        sql=this.parseVariable(sql);
		return query;
	}
	
	/**
	 * This will be needed when doing cube exporting
	 * @param d
	 */
	public void setAD_PInstance_ID(int d){
		this.processInstanceId=d;
	}
	public int getReportInstanceId() {
		return this.reportInstanceId;
	}
	public void setReportInstanceId(int id) {
		this.reportInstanceId = id;
	}
	public int getUserId() {
		return userId;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getCxtabName() {
		return cxtabName;
	}
	public String getFileName() {
		return fileName;
	}
	public String getFilterExpr() {
		return filterExpr;
	}
	public String getQuery() {
		return queryStr;
	}
	public void setCxtabName(String CxtabName) {
		this.cxtabName = CxtabName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setFilterExpr(String filterExpr) {
		this.filterExpr = filterExpr;
	}
	public void setQuery(String q) {
		this.queryStr = q;
	}
	public int getAdClientId() {
		return adClientId;
	}
	public void setAdClientId(int adClientId) {
		this.adClientId = adClientId;
	}

	public void setReportParameters(HashMap p){
		this.params=p;
	}
	public void setFilterDesc(String s){
		this.filterDesc=s;
	}
	/**
	 * support for $v as Velocity template containing variables
	 * @param sql may containing $v means for velocity variables
	 * @return sql that can be executed in db
	 * @throws NDSException
	 */
	private String parseVariable(String sql) throws NDSException{
		//
    	if(sql.indexOf('$')>0){
    		sql= ReportVariables.getInstance().evaluate(sql); 
    	}
    	return sql;
	}	
}
