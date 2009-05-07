/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.report;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;
import nds.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.control.web.*;
import nds.control.util.*;
import nds.control.event.DefaultWebEvent;

/**
 * 负责根据界面内容，生成打印请求，并置于打印队列，打印队列通过nds.control.web.ClientControllerWebImpl 实现
 * 打印分为前台打印和后台打印两种方式，前台打印将返回JasperPrint,后台打印将打印结果信息置于用户目录中
 * @author yfzhu@agilecontrol.com
 */ 
 
public class ReportPrinter {
	private static Logger logger= LoggerManager.getInstance().getLogger(ReportPrinter.class.getName());
	
	public ReportPrinter(){
		
	}
	/**
	 * This is for list report
	 * Add "sql" to param 
	 * @param reportId
	 * @param params
	 * @param query
	 */
	private void addSQLParam(int reportId, Map params, QueryRequest query, UserWebImpl user) throws Exception{
		QueryRequestImpl q= QueryEngine.getInstance().createRequest(user.getSession());
		Table table=query.getMainTable(); 
		q.setMainTable(table.getId());
		q.addAllShowableColumnsToSelection(Column.PRINT_LIST, false);
		String where=q.addParam( query.getParamExpression());
		//fixme: should be modified, since query support more than one order by column (@see QueryRequestImpl#addOrderBy)
		//follwing setOrder only retrieve the first order by clause in query object
		q.setOrderBy(query.getOrderColumnLink(), query.isAscendingOrder());
		q.setRange(0,  Integer.MAX_VALUE);
		String sql= q.toSQL();
		logger.debug(sql);
		params.put("sql", sql);
		if(where!=null) params.put("where",where);
		else params.put("where","1=1");
		
	}
	/**This is for object report
	 * Add "sql_main" and all related table's sql to param
	 * @param reportId
	 * @param params
	 * @param tableId
	 * @param objectId
	 * @param user
	 */
	private void addSQLParam(int reportId, Map params, int tableId, int objectId, UserWebImpl user) throws Exception{
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
	private DefaultWebEvent createPrintEvent(HttpServletRequest request, boolean background) throws Exception{
		//logger.debug(Tools.toString(request));
		
		// is on ad_cxtab report or not
		boolean isJReport=Tools.getYesNo( request.getParameter("isjreport"), false);
		
		UserWebImpl userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	
    	int reportId= ParamUtils.getIntAttributeOrParameter(request, "reportid", -1);
    	QueryRequestImpl query =(QueryRequestImpl) request.getAttribute("query");
        int tableId= ParamUtils.getIntParameter(request,"table", -1);
        int objectId=ParamUtils.getIntParameter(request,"id", -1);
    	Table table=null;
        // check table epxort permission, bugs here, only batch export will be checked, one object export
    	// is not needed.
        if(query!=null ){
        	table= query.getMainTable();
        }
        
    	Map parameters = new HashMap();
    	// common parameters
    	parameters.put("user", userWeb.getUserName());
    	parameters.put("jobid", ReportTools.createJobId(userWeb.getUserId()+""));
    	// for special handle
    	parameters.put("userid", String.valueOf(userWeb.getUserId()));
    	parameters.put("objectid", new Integer(objectId));
    	parameters.put("ad_client_id", String.valueOf(userWeb.getAdClientId()));    	
    	// special parameters
    	String reportType= request.getParameter("reporttype");
    	
    	if("L".equals(reportType)){
        	userWeb.checkPermission(table.getSecurityDirectory(), Directory.EXPORT);
    		addSQLParam(reportId, parameters, query, userWeb);
    	}else{
    		addSQLParam(reportId, parameters, tableId, objectId, userWeb);
    	}    	
    	
    	JasperReport jasperReport=null;
    	if(isJReport){
    		jasperReport = nds.cxtab.JReport.getJasperReport(reportId);    		
    	}else{
	    	jasperReport = (JasperReport)JRLoader.loadObject(new java.net.URL(ReportTools.getReportURL(reportId, userWeb.getClientDomain())));	    	
    	}
    	

    	DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
        event.setParameter("operatorid", userWeb.getUserId()+"");
    	event.put("reportobject" ,jasperReport );
        event.put("reportparam",parameters);
        
        if(background){
        	event.setParameter("command", "JasperPrintBackground");
            event.setParameter("filetype", request.getParameter("filetype"));
            ReportUtils ru = new ReportUtils(request);
            String destFolder = ru.getExportRootPath() + File.separator +  ru.getUser().getClientDomain()+File.separator+  ru.getUserName();
            event.setParameter("destfolder", destFolder);
            event.setParameter("destfile", request.getParameter("destfile")+"." +  request.getParameter("filetype"));
            
        }else{
        	event.setParameter("command", "JasperPrintForeground");
        }
        return event;
	}
	public JasperPrint printForeground(HttpServletRequest request) throws Exception{
		ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.WEB_CONTROLLER);
		DefaultWebEvent event=createPrintEvent(request, false);

		ValueHolder holder = (ValueHolder)controller.handleEvent(event);
		return (JasperPrint)holder.get("print");
	}
	public void printBackground(HttpServletRequest request) throws Exception{
		ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(WebKeys.WEB_CONTROLLER);
		
		DefaultWebEvent event=createPrintEvent(request,true);

		// let client return immediately, so run in background
		controller.handleEventBackground(event);
	}
	
}
