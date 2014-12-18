package nds.velocity;

import nds.schema.*;
import nds.query.*;
import nds.web.config.*;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;
import nds.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.model.*;
import nds.control.web.*;

import java.sql.*;
import java.util.*;
import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.directwebremoting.util.SwallowingHttpServletResponse;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.wxap.*;
import com.wxap.client.TenpayHttpClient;
import com.wxap.util.Sha1Util;
import com.wxap.util.TenpayUtil;

/**
 * Helper class for loading information from db, commonly named "web" in vm file
 * Note here no permission check for records
 *  
 * @author yfzhu
 */
public class WebClient {
    private static Logger logger= LoggerManager.getInstance().getLogger(WebClient.class.getName());
	
	private final static String NEWS_CLOB_BY_DOCNO="select content from u_news where ad_client_id=? and no=?";
	private final static String NEWS_CLOB_BY_ID="select content from u_news where ad_client_id=? and id=?";
	private final static String STORE_CLOB_BY_ID="select REMARK from WX_STORE where ad_client_id=? and id=?";
	
	private final static String WEB_CLIENT="select id from web_client where ad_client_id=?";
	private final static String FRIENDURL="select name,url,web_target from web_friendurl where ad_client_id=?";
	private final static String WX_CATEGORYSETPDT="select count(*) from WX_ITEMCATEGORYSET t where t.wx_itemcategoryset_id=?";
	private final static String WX_CATEGORYSETTXT="select count(*) from WX_ARTICLECATEGORY t where t.WX_ARTICLECATEGORY_ID=?";
	private final static int MAX_COLUMNLENGTH_WHEN_TOO_LONG=3000;//QueryUtils.MAX_COLUMN_CHARS -3
	
	private final static String DEFAULT_TEMPLATE_FOLDER="001";
	private int adClientId; 
	/**
	 * serverRootURL when in static output, in format like "http://www.mycompany.com", when in
	 * 				preview action, in format like "http://server/html/nds/website/<themeid>"
	 * themeid can be retrieved from web_client.website_template_id.foldername 
	 */
	private String serverRootURL;
	
	private String webDomain;
	private boolean hasTemplateFolder;
	/**
	 * 
	 * @param adClientId client id
	 * @param serverRootURL when in static output, in format like "http://www.mycompany.com", when in
	 * 				preview action, in format like "http://server/html/nds/website"
	 * @param  serverWebDomain the webdomain that user request
	 * @param hasTemplateFolder if true, will append server root url with /html/nds/website
	 */
	public WebClient(int adClientId, String serverRootURL, String serverWebDomain, boolean hasTemplateFolder){
		this.adClientId=adClientId;
		//String folder=null;
		/*try{
			folder= (String)nds.query.QueryEngine.getInstance().doQueryOne("select t.foldername from ad_site_template t, web_client c where c.ad_client_id="+ adClientId+" and t.id=c.ad_site_template_id");
		}catch(Throwable t){
			logger.error("Fail to load web template folder for client:"+ adClientId+", using 001 as default",t );
			folder= DEFAULT_TEMPLATE_FOLDER;
		}*/
		this.webDomain= serverWebDomain;
		if(hasTemplateFolder){
			this.serverRootURL= serverRootURL+"html/nds/oto/website/";
		}else{
			this.serverRootURL= serverRootURL;
		}
		this.hasTemplateFolder=hasTemplateFolder;
		
	}	
	/**
	 * This url maybe changed when admin of that web changed tem
	 * @return
	 */
	public String getServerRootURL(){
		if(hasTemplateFolder)  {
			WeUtilsManager Wemanage =WeUtilsManager.getInstance();
		//WeUtils wu=Wemanage.getByDomain(webDomain);
			return serverRootURL+Wemanage.getAdClientTemplateFolder(webDomain);
			//nds.control.web.WebUtils.getAdClientTemplateFolder(webDomain);
		}else{
			return serverRootURL;
		}
	}
	/**
	 * All links made by hand on vml files should use this method.
	 * 
	 * When in preview, just return link with no change, when in output static pages, will 
	 * convert "&", "?", "=" to "_", and remove ".vml", for instance, support link:
	 * 	"news.vml?id=133", will convert to "news_id_133.html"
	 * @param link
	 * @return
	 */
	public String link(String link){
		/*if(!isPreview){
			link= link.replace("[?&=]","_");
			link=link.replaceAll(".vml","");
			link=  link+".html";
		}*/
		return link;
	}
	/**
	 * System image that relative to theme location 
	 * @param imageFileName
	 * @return
	 */
	public String sysFileRootURL(){
		return getServerRootURL();
	}
	
	/**
	 * Convert "20080912" to "2008年9月12日"  
	 * @param data
	 * @return "" when data is null
	 */
	public String getDate(Object data){
		if(data==null)return "";
		try{
			java.text.SimpleDateFormat df=new java.text.SimpleDateFormat("yyyyMMdd");
			java.util.Date d= df.parse(data.toString().trim());
			java.text.SimpleDateFormat df2=new java.text.SimpleDateFormat("yyyy年MM月dd日");
			return df2.format(d);
		}catch(Throwable t){
			return data.toString();
		}
	}
	/**
	 * Update news, increase news.readcnt 
	 * @param newsId
	 */
	public void updateNewsCounter(int newsId){
		try{
			QueryEngine.getInstance().executeUpdate("update WX_ISSUEARTICLE set BROWSENUM=nvl(BROWSENUM,0)+1 where id="+ newsId);
			//logger.debug("updateNewsCounter !");
		}catch(Throwable t){
			logger.error("Fail to update news id="+ newsId, t);
		}
	}
	
	/**
	 * Update news, increase news.readcnt 
	 * @param newsId
	 */
	public static void shareRecord(HttpServletRequest req,int ad_client_id,int targetid){
		try{
			//int vipid=nds.util.Tools.getInt(req.getParameter("fromvip"), -1);
			//String fromid=req.getParameter("fromid");
			//int objid=nds.util.Tools.getInt(req.getParameter("objid"),-1);
			String state=req.getParameter("state");
			logger.debug("state->"+state);
			if(state==null)return;
			String[] param = state.split("z");
			logger.debug("param len->"+param.length);
			if(param.length<1)return;
			int vipid=nds.util.Tools.getInt(param[0], -1);
			int fromid=nds.util.Tools.getInt(param[1], -1);
			int objid=nds.util.Tools.getInt(param[2], -1);
			StringBuffer url=req.getRequestURL();
			QueryEngine.getInstance().executeUpdate("insert into WX_SHARERECORD(id,AD_CLIENT_ID,AD_ORG_ID,WX_VIP_ID,FROMID,OBJID,targetid,URL,CREATIONDATE,ISACTIVE) values(get_sequences('WX_SHARERECORD'),?,27,?,?,?,?,?,sysdate,'Y')" 
		               ,new Object[] {ad_client_id,vipid,fromid,objid,targetid,String.valueOf(url)});
			//QueryEngine.getInstance().executeUpdate("update WX_ISSUEARTICLE set BROWSENUM=nvl(BROWSENUM,0)+1 where id="+ newsId);
			logger.debug("shareRecord !");
		}catch(Throwable t){
			logger.error("Fail to shareRecord url="+ req.getRequestURL(), t);
		}
	}
	/**
	 * Update ths visit quantity of wx_setinfo
	 * @param companyid
	 */
	public void updateWebsite() {
		try{
			QueryEngine.getInstance().executeUpdate("update wx_setinfo s set s.visitnumber=nvl(s.visitnumber,0)+1 where s.ad_client_id="+ this.getClientId());
			//logger.debug("updateNewsCounter !");
		}catch(Throwable t){
			logger.error("Fail to update setinfo id="+ this.getClientId(), t);
		}
	}
	/**
	 * User specific image location
	 * @return #nds.control.web.WebClientUserFileServlet.USER_FOLDER_PATH
	 */
	public String userFileRootURL(){
		 return nds.control.web.WebClientUserFileServlet.USER_FOLDER_PATH;
	}
	/**
	 * 检查输入的时间是否在一周内，如果在，显示“新”标志
	 * @param date
	 * @return "" or "/images/new.gif"
	 */
	public String newTag(Object date){
		if(date==null)return "";
		try{
			java.text.SimpleDateFormat df=new java.text.SimpleDateFormat("yyyyMMdd");
			java.util.Date d= df.parse(date.toString().trim());
			if(System.currentTimeMillis() - d.getTime() > 1000* 60* 60* 24 *7){
				return "";
			}else 
			return "<img src='/images/new.gif' border='0'>";
		}catch(Throwable t){
			logger.error("parse date error "+  date+ " need format: yyyyMMdd:"+t);
			return "";
		}
		
	}
	
	/**
	 * Include list html code
	 * @param listConfName will have ListDataConfig and ListUIConfig set with 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	/*public void includeList(String listConfName,HttpServletRequest request, HttpServletResponse response ) throws Exception{
		request.setAttribute("nds.portal.listconfig",listConfName );
		
		if(isPreview)
			request.getSession().getServletContext().getRequestDispatcher("/html/nds/portal/portletlist/view.jsp").include(request,response);
		else{
			// should forward to a page converted all images/css/js to folder related to root
			request.getSession().getServletContext().getRequestDispatcher("/html/nds/portal/portletlist/view.jsp").include(request,response);
		}
	}*/
	
	public ListDataConfig getListDataConfig(String adListDataConfName) throws Exception{
		PortletConfigManager pcManager=(PortletConfigManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PORTLETCONFIG_MANAGER);
		return (ListDataConfig)pcManager.getPortletConfig(adListDataConfName,nds.web.config.PortletConfig.TYPE_LIST_DATA);
	}
	public ListUIConfig getListUIConfig(String adListUIConfName)throws Exception{
		PortletConfigManager pcManager=(PortletConfigManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PORTLETCONFIG_MANAGER);
		return (ListUIConfig)pcManager.getPortletConfig(adListUIConfName,nds.web.config.PortletConfig.TYPE_LIST_UI);
	}
	/**
	 * 
	 * @param adListDataConfName name of ad_listdataconf table, note name is unique in ad_listdataconf
	 * @param adListUIConfName name of ad_listuiconf table, note name is unique in ad_listuiconf  
	 * @return contains following object 
	 * 	"query" - QueryRequestImpl
	 *  "result" - QueryResult	
	 *  "list" - List of List, first element will be pk link, others will be columns specified by dataConfig
	 *  "uiconf" - ListUIConfig
	 *  "dataconf" - ListDataConfig
	 */
	public HashMap getListWithQuery(String adListDataConfName, String adListUIConfName )throws Exception{
		HashMap map= new HashMap();
		PortletConfigManager pcManager=(PortletConfigManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PORTLETCONFIG_MANAGER);
		ListDataConfig dataConfig= (ListDataConfig)pcManager.getPortletConfig(adListDataConfName,nds.web.config.PortletConfig.TYPE_LIST_DATA);
		ListUIConfig uiConfig= (ListUIConfig)pcManager.getPortletConfig(adListUIConfName,nds.web.config.PortletConfig.TYPE_LIST_UI);
		
		
		TableManager manager=TableManager.getInstance();
		QueryEngine engine=QueryEngine.getInstance();
		int tableId=dataConfig.getTableId();
		Table table;
		table= manager.getTable(tableId);

		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);

	    query.setMainTable(tableId,true, dataConfig.getFilter());
	    		
	    query.addSelection(table.getPrimaryKey().getId());
	    query.addColumnsToSelection(dataConfig.getColumnMasks(),false, uiConfig.getColumnCount() );
		if( dataConfig.getOrderbyColumnId()!=-1){
			Column orderColumn= manager.getColumn(dataConfig.getOrderbyColumnId());
			if(orderColumn!=null && orderColumn.getTable().getId()== tableId)query.setOrderBy(new int[]{dataConfig.getOrderbyColumnId()}, dataConfig.isAscending());
		}else{
	    	query.setOrderBy(new int[]{ table.getPrimaryKey().getId()}, false);
	    }
	    if(uiConfig.getPageSize()>0)query.setRange(0,uiConfig.getPageSize());
	    
	    Expression expr=null;
		if(table.isAcitveFilterEnabled()){
			expr=new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null);
		}
		if(table.isAdClientIsolated()){
			if(expr!=null)expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
			else expr=new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null);
		}
		query.addParam(expr);
		
		QueryResult result= QueryEngine.getInstance().doQuery(query);
		QueryResultMetaData meta=result.getMetaData();
		
		//Hashtable urls=new Hashtable();
	    String mainTablePath=getServerRootURL()+ dataConfig.getMainURL(); // like "/news.vml", this is used in preview page
		//if(mainTablePath!=null)urls.put(new Integer(0), mainTablePath);
	    String staticMainTablePath= getServerRootURL()+ link(mainTablePath); // like "news", this is used in static page
	    
	    int pkValue;
	    String url=null;
        String columnDataShort,columnData;
        ArrayList data=new ArrayList();
		while(result.next()){
			ArrayList list =new ArrayList();
	        pkValue= Tools.getInt(result.getObject(1),-1);
	        url=  mainTablePath+"?id="+pkValue;
	        //if(this.isPreview)url=  mainTablePath+"?id="+pkValue;
            //else url=  staticMainTablePath +"_"+pkValue+".html";
	        
	        list.add(url);
			
	        for(int i=1;i< meta.getColumnCount();i++){ // first column should always be PK
	        	columnData=result.getString(i+1, true, true);
				
				/*int objId= result.getObjectID(i+1);
				if(objId!=-1){
	                String s=(String)urls.get(new Integer(i+1)) ;
	                if( s!=null) {
	                    if(this.isPreview)url= serverRootURL+ s+"?id="+objId;
	                    else url=  serverRootURL+ s+"_"+objId+".html";
	                }else{
	                    //url=queryPath+ colmn.getTable().getId() +"&id="+objId;
	                }
	            }*/
	           
	            //Tools.isHTMLAnchorTag(columnData)
	            if(uiConfig.getColumnLength()!=null && uiConfig.getColumnLength().length>=i){
					columnDataShort= StringUtils.shortenInBytes(columnData, uiConfig.getColumnLength()[i-1]);
	            }else{
	            	columnDataShort= StringUtils.shortenInBytes(columnData, MAX_COLUMNLENGTH_WHEN_TOO_LONG);
	            }
	            list.add(columnDataShort);
			}
	        data.add(list);
		}
		
	    map.put("uiconf",uiConfig );
	    map.put("dataconf",dataConfig );
	    map.put("query",query );
	    map.put("result",result );
	    map.put("list",data );
		return map;
	}	
	/* (non-Javadoc)
     * @see org.directwebremoting.WebContext#forwardToString(java.lang.String)
     */
    public String forwardToString(String url, HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        StringWriter sout = new StringWriter();
        StringBuffer buffer = sout.getBuffer();

        HttpServletResponse realResponse =response;
        HttpServletResponse fakeResponse = new SwallowingHttpServletResponse(realResponse, sout, realResponse.getCharacterEncoding());

        HttpServletRequest realRequest = request;
        //realRequest.setAttribute(WebContext.ATTRIBUTE_DWR, Boolean.TRUE);

        WebUtils.getServletContext().getRequestDispatcher(url).forward(realRequest, fakeResponse);

        return buffer.toString();
    }
    
    
    
    
    public void forwardTourl(String url, HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        //StringWriter sout = new StringWriter();
        //StringBuffer buffer = sout.getBuffer();

        //HttpServletResponse realResponse =response;
        //HttpServletResponse fakeResponse = new SwallowingHttpServletResponse(realResponse, sout, realResponse.getCharacterEncoding());

        //HttpServletRequest realRequest = request;
        //realRequest.setAttribute(WebContext.ATTRIBUTE_DWR, Boolean.TRUE);

        WebUtils.getServletContext().getRequestDispatcher(url).forward(request, response);
        return;
        //return buffer.toString();
    }
	
	
	public String toHTMLControlForm(QueryRequest req, Expression userExpr, String nameSpace) throws QueryException {
		return QueryUtils.toHTMLControlForm(req, userExpr,nameSpace);
	}
	
	
	public List getList(String adListDataConfName, String adListUIConfName)throws Exception{
		  return getList(adListDataConfName,adListUIConfName,null,null,0);
	}
	
	public List getList(String adListDataConfName, String adListUIConfName,String fixedcol)throws Exception{
		  return getList(adListDataConfName,adListUIConfName,fixedcol,null,0);
	}
	/**
	 * 
	 * @param adListDataConfName name of ad_listdataconf table, note name is unique in ad_listdataconf
	 * @param adListUIConfName name of ad_listuiconf table, note name is unique in ad_listuiconf  
	 * @return List of List, first element will be pk link, others will be columns specified by dataConfig
	 */
	public List getList(String adListDataConfName, String adListUIConfName,String fixedcol,String parm,int pos)throws Exception{
		logger.debug("adListDataConfName="+adListDataConfName+", adListUIConfName="+adListUIConfName);
		PortletConfigManager pcManager=(PortletConfigManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PORTLETCONFIG_MANAGER);
		ListDataConfig dataConfig= (ListDataConfig)pcManager.getPortletConfig(adListDataConfName,nds.web.config.PortletConfig.TYPE_LIST_DATA);
		ListUIConfig uiConfig= (ListUIConfig)pcManager.getPortletConfig(adListUIConfName,nds.web.config.PortletConfig.TYPE_LIST_UI);
		
		TableManager manager=TableManager.getInstance();
		QueryEngine engine=QueryEngine.getInstance();
		int tableId=dataConfig.getTableId();
		Table table;
		table= manager.getTable(tableId);

		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);

	    query.setMainTable(tableId,true, dataConfig.getFilter());
	    		
	    query.addSelection(table.getPrimaryKey().getId());
	    query.addColumnsToSelection(dataConfig.getColumnMasks(),true, uiConfig.getColumnCount() );
		if( dataConfig.getOrderbyColumnId()!=-1){
			Column orderColumn= manager.getColumn(dataConfig.getOrderbyColumnId());
			if(orderColumn!=null && orderColumn.getTable().getId()== tableId)query.setOrderBy(new int[]{dataConfig.getOrderbyColumnId()}, dataConfig.isAscending());
		}else{
	    	query.setOrderBy(new int[]{ table.getPrimaryKey().getId()}, false);
	    }
	    if(uiConfig.getPageSize()>0)query.setRange(0,uiConfig.getPageSize());
	    
	    Expression expr=null;
		if(table.isAcitveFilterEnabled()){
			expr=new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null);
		}
		if(table.isAdClientIsolated()){
			if(expr!=null)expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
			else expr=new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null);
		}
		
		PairTable fixedColumns=null;
		Expression fixedExpr=null;
		try{
				fixedColumns= PairTable.parseIntTable(fixedcol,null );
				for( Iterator it=fixedColumns.keys();it.hasNext();){
			        	Integer key=(Integer) it.next();
			            Column col=manager.getColumn( key.intValue());
			            ColumnLink cl=new ColumnLink( col.getTable().getName()+"."+ col.getName());
			            fixedExpr= new Expression(cl,"="+ fixedColumns.get(key),null);
			        }

		}catch(NumberFormatException  e){
		 		fixedColumns= PairTable.parse(fixedcol,null );
		 		fixedExpr=Expression.parsePairTable(fixedColumns);
		}
		if(fixedExpr!=null)expr=expr.combine(fixedExpr, SQLCombination.SQL_AND, null);
		query.addParam(expr);
		
		QueryResult result= QueryEngine.getInstance().doQuery(query);
		QueryResultMetaData meta=result.getMetaData();
		
		//Hashtable urls=new Hashtable();
		
	    String mainTablePath=(dataConfig.getMainURL().startsWith("/")?"":"/")+
	    				dataConfig.getMainURL(); // like "/news.vml", this is used in preview page
	    if(mainTablePath.indexOf("@ID@")==-1&&parm==null){
	    	mainTablePath=mainTablePath+"?id=@ID@";
	    }
	    //if(mainTablePath!=null)urls.put(new Integer(0), mainTablePath);
	    //String staticMainTablePath= getServerRootURL()+ link(mainTablePath); // like "news", this is used in static page
	    
	    int pkValue;
	    String url=null;
        String columnDataShort,columnData;
        ArrayList data=new ArrayList();
		while(result.next()){
			ArrayList list =new ArrayList();
	        pkValue= Tools.getInt(result.getObject(1),-1);
	        url=StringUtils.replace(mainTablePath, "@ID@", String.valueOf(pkValue));  //mainTablePath+"?id="+pkValue;
	        if(parm!=null&&pos>0){
	        url=StringUtils.replace(mainTablePath, parm, String.valueOf(result.getObject(pos)));  //mainTablePath+"?id="+pkValue;
	        }
	        //if(this.isPreview)url=  mainTablePath+"?id="+pkValue;
            //else url=  staticMainTablePath +"_"+pkValue+".html";
	        
	        list.add(url);
			
	        for(int i=1;i< meta.getColumnCount();i++){ // first column should always be PK
	        	columnData=result.getString(i+1, true, false);
				
				/*int objId= result.getObjectID(i+1);
				if(objId!=-1){
	                String s=(String)urls.get(new Integer(i+1)) ;
	                if( s!=null) {
	                    if(this.isPreview)url= serverRootURL+ s+"?id="+objId;
	                    else url=  serverRootURL+ s+"_"+objId+".html";
	                }else{
	                    //url=queryPath+ colmn.getTable().getId() +"&id="+objId;
	                }
	            }*/
	           
	            //Tools.isHTMLAnchorTag(columnData)
	            if(uiConfig.getColumnLength()!=null && uiConfig.getColumnLength().length>=i){
					columnDataShort= StringUtils.shortenInBytes(columnData, uiConfig.getColumnLength()[i-1]);
	            }else{
	            	columnDataShort= StringUtils.shortenInBytes(columnData, MAX_COLUMNLENGTH_WHEN_TOO_LONG);
	            }
	            list.add(columnDataShort);
			}
	        data.add(list);
		}
		
	    
		return data;
	}
	/**
	 * Return record column data in map of specified record, get columns which has any of the bit masks set in specified positions.
	 *  
	 * @param tableName in ad_table
	 * @param objId id of that record
	 * @param columnMasks masks in column.mask
	 * @return key: column's dbname in lower case, value: column data formatted according to ad_column definition   
	 */
	public Map getObject(String tableName, int objId, int[] columnMasks,String fixedcol) throws Exception{
		QueryEngine engine= QueryEngine.getInstance();
		TableManager manager= TableManager.getInstance();
		Table table=manager.getTable(tableName);
		
		QueryRequestImpl query= engine.createRequest(null);
		query.setMainTable(table.getId());
		logger.debug("getObject columnMasks->"+columnMasks.toString());
		query.addSelection(table.getPrimaryKey().getId());
		query.addColumnsToSelection(columnMasks, true);
		Expression expr=null;
		if(objId>0){
		 expr=new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}), "="+objId, null);
		if(table.isAcitveFilterEnabled()){
			expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null), SQLCombination.SQL_AND, null);
		}
		if(table.isAdClientIsolated()){
			expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
		}
		}else if(fixedcol!=null){
		PairTable fixedColumns=null;
		//Expression fixedExpr=null;
		try{
				fixedColumns= PairTable.parseIntTable(fixedcol,null );
				for( Iterator it=fixedColumns.keys();it.hasNext();){
			        	Integer key=(Integer) it.next();
			            Column col=manager.getColumn( key.intValue());
			            ColumnLink cl=new ColumnLink( col.getTable().getName()+"."+ col.getName());
			            expr= new Expression(cl,"="+ fixedColumns.get(key),null);
			        }

		}catch(NumberFormatException  e){
		 		fixedColumns= PairTable.parse(fixedcol,null );
		 		expr=Expression.parsePairTable(fixedColumns);
		}
		if(table.isAcitveFilterEnabled()){
			expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null), SQLCombination.SQL_AND, null);
		}
		if(table.isAdClientIsolated()){
			expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
		}
		}
		if(expr!=null)query.addParam(expr);
		
		QueryResult result= engine.doQuery(query);
		//QueryResultMetaData meta=result.getMetaData();
		HashMap map = new HashMap();
		
		if(result.getRowCount()>0){
			result.next();
			map.put("id", result.getObject(1));
			ArrayList columns= table.getColumns(columnMasks,true,0);
			for(int i=0;i<columns.size();i++){
				
				Column column=(Column) columns.get(i);
				logger.debug("colnam ->"+column.getName().toLowerCase());
				//columnData=result.getString(i+1, true, true);
				if(column.getDisplaySetting().getObjectType()==DisplaySetting.OBJ_CLOB){
					map.put(column.getName().toLowerCase(), result.getObject(i+2));
				}else{
					map.put(column.getName().toLowerCase(), result.getString(i+2,true,false));
				}
			}
		}
		return map;
	}	
	/**
	 * Return record column data in map of specified record, column is from Column.MASK_QUERY_LIST=1
	 * @param tableName in ad_table
	 * @param objId id of that record
	 * @return key: column name in lower case, value: column data formatted according to ad_column definition   
	 */
	public Map getObject(String tableName, int objId) throws Exception{
		return getObject(tableName, objId, new int[]{Column.MASK_PRINT_SUBLIST},null);
	}
	/**
	 * Return record column data in map of specified record, column is from Column.MASK_QUERY_LIST=1
	 * @param tableName in ad_table
	 * @param objId id of that record
	 * @return key: column name in lower case, value: column data formatted according to ad_column definition   
	 */
	public Map getObject(String tableName, int objId, int mask) throws Exception{
		return getObject(tableName, objId, new int[]{mask},null);
	}
	
	public Map getObject(String tableName, String fixcol) throws Exception{
		return getObject(tableName, -1, new int[]{Column.MASK_PRINT_SUBLIST},fixcol);
	}
	
	
	public int getTableId() throws Exception{

		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt= conn.prepareStatement(WEB_CLIENT);
			pstmt.setInt(1,this.adClientId);
			rs= pstmt.executeQuery();
			if(rs.next()) return rs.getInt(1);
			return 1;
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		    
	}
	
	
	
	/**
	 * 
	 * @param docno
	 * @return
	 * @throws Exception
	 */
	public String getNewsBody(String docno) throws Exception{
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt= conn.prepareStatement(NEWS_CLOB_BY_DOCNO);
			pstmt.setInt(1,this.adClientId);
			pstmt.setString(2, docno);
			rs= pstmt.executeQuery();
			if(rs.next()) return rs.getString(1);
			return "";
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		
	}
	/**
	 * @param id 
	 * @return
	 * @throws Exception
	 */
	public String getNewsBody(int id) throws Exception{
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt= conn.prepareStatement(NEWS_CLOB_BY_ID);
			pstmt.setInt(1,this.adClientId);
			pstmt.setInt(2, id);
			rs= pstmt.executeQuery();
			if(rs.next()) return rs.getString(1);
			return "";
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		
	}
	
	/**
	 * @param id 
	 * @return
	 * @throws Exception
	 */
	public String getStoreBody(int id) throws Exception{
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt= conn.prepareStatement(STORE_CLOB_BY_ID);
			pstmt.setInt(1,this.adClientId);
			pstmt.setInt(2, id);
			rs= pstmt.executeQuery();
			if(rs.next()) return rs.getString(1);
			return "";
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */	
	public String getListbyCategory(int id,String ptype) throws Exception{
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		String tbname=null;
		String listname=null;
		try{
			if(ptype.equals("pdt")){
			pstmt= conn.prepareStatement(WX_CATEGORYSETPDT);
			tbname="WEB_MAIL_TMP";
			listname="mlist_tmp";
			}else if(ptype.equals("text")){
				pstmt= conn.prepareStatement(WX_CATEGORYSETTXT);
				tbname="WEB_CLIENT_TMP";
				listname="list_tmp";
			}
			pstmt.setInt(1,id);
			rs= pstmt.executeQuery();
			logger.debug("webclient getListbyCategory"+id);
			if(rs.next()) {
				//logger.debug("webclient getListbyCategory"+rs.getInt(1));
				if(Integer.valueOf(rs.getInt(1))==0){
					String listfold="select e.foldername from "+tbname+" g,AD_SITE_TEMPLATE e"+
					" where g."+listname+"=e.id and g.ad_client_id="+this.adClientId;
					logger.debug("webclient getListbyCategory"+listfold);
					 return (String)QueryEngine.getInstance().doQueryOne(listfold, conn);
				}
			}
			return "cate";
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public UNews getNews(int id) throws Exception{
		nds.model.dao.UNewsDAO dao=new nds.model.dao.UNewsDAO();
		return dao.load(new Integer(id));
	}
	
	public  int getClientId()
	{
		return this.adClientId ;
	}
	
	
	public int getCoupons(int vipid) throws Exception{
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try{
			pstmt= conn.prepareStatement("select count(1) from WX_COUPONEMPLOY t where  t.ISRECEIVE='N' and t.wx_vip_id=? and t.wx_coupon_id in(select c.id from wx_coupon c where c.id=t.wx_coupon_id and DECODE(c.VALIDAY,NULL, to_char(c.ENDTIME,'yyyymmdd'),to_char(t.CREATIONDATE+c.VALIDAY,'yyyymmdd'))>=to_char(sysdate,'yyyymmdd'))");
			pstmt.setInt(1,vipid);
			rs= pstmt.executeQuery();
			if(rs.next()) return rs.getInt(1);
			return 0;
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
	}
	
	/**
	 * Get url list of friends
	 * @return
	 * @throws Exception
	 */
	public List getFriendURLList()throws Exception{
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		ArrayList data=new ArrayList();
		String name,url,web_target;
		try{
			pstmt= conn.prepareStatement(FRIENDURL);
			pstmt.setInt(1,this.adClientId);
			rs= pstmt.executeQuery();						
			while(rs.next()) {
				ArrayList list =new ArrayList();
				name=rs.getString(1);
				//System.out.print(name);
				url=rs.getString(2);
				web_target=rs.getString(3);
				list.add(name);
				list.add(url);
				list.add(web_target);
				data.add(list);
			}
			 return data;
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}

		
	}
	/**
	 * Truncate string to specified length, escape first 3 chars ("<p>")
	 * Only used for news object, for others, use shortString intead
	 * @param str
	 * @param len
	 * @return
	 */
	public String truString(String str,int len){
		str=str.substring(3); 
		str=StringUtils.shorten(str,len);	
	   return str;	
	}
	/**
	 * Truncate string to specified length, escape first 3 chars ("<p>")
	 * Only used for news object, for others, use shortString intead
	 * @param str
	 * @param len
	 * @return
	 */
	public String shortString(String str,int len){
		return StringUtils.shorten(str,len);	
	}
	
	
	public String payOrder(int orderid,HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		RequestHandler reqHandler = new RequestHandler(request, response);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		
		TreeMap<String, String> outParams = new TreeMap<String, String>();
		//初始化 
		WeUtils wu=null;
		List weixinpay=null;
		try {
			java.net.URL url = new java.net.URL(request.getRequestURL().toString());
			WeUtilsManager Wemanage =WeUtilsManager.getInstance();
			wu =Wemanage.getByDomain(url.getHost());
			
			weixinpay=QueryEngine.getInstance().doQueryList("select p.partner,p.app_id,p.app_secret,p.partner_key,p.app_key from wx_pay p where p.ad_client_id=? and p.pcode='weixin'", new Object[] {wu.getAd_client_id()});
		}catch(Exception t){
			logger.debug("get path error->"+t.getMessage());
			t.printStackTrace();
			return "{}";
		}
		
		if(weixinpay==null||weixinpay.size()<=0) {
			logger.debug("not find pay info->");
			return "{}";
		}
		
		//查询订单信息
		List orderinfo=null;
		try {
			orderinfo=QueryEngine.getInstance().doQueryList("select o.docno,to_char(wmsys.wm_concat(ag.itemname)),sum(nvl(o.tot_amt,0)*100) from wx_order o,wx_orderitem oi,wx_appendgoods ag where o.id=? and o.id=oi.wx_order_id and oi.wx_appendgoods_id=ag.id group by o.docno", new Object[] {orderid});
		}catch(Exception e) {
			logger.debug("not find order info->");
			return "{}";
		}
		if(orderinfo==null||orderinfo.size()<=0) {
			logger.debug("not find order info->");
			return "{}";
		}
	
		String out_trade_no = String.valueOf(((List)orderinfo.get(0)).get(0));
		String productname= String.valueOf(((List)orderinfo.get(0)).get(1));
		if(nds.util.Validator.isNotNull(productname)&&productname.length()>30) {
			productname=productname.substring(0,29)+"...";
		}
		
		
		String APP_ID, APP_SECRET, PARTNER,PARTNER_KEY, APP_KEY;
		String NOTIFY_URL=wu.getDoMain()+"/servlets/binserv/nds.weixin.ext.RestWeixinPayCallback";
		
		PARTNER=String.valueOf(((List) weixinpay.get(0)).get(0));
		APP_ID=String.valueOf(((List) weixinpay.get(0)).get(1));
		APP_SECRET=String.valueOf(((List) weixinpay.get(0)).get(2));
		PARTNER_KEY=String.valueOf(((List) weixinpay.get(0)).get(3));
		APP_KEY=String.valueOf(((List) weixinpay.get(0)).get(4));
		reqHandler.init();
		reqHandler.init(APP_ID, APP_SECRET,APP_KEY,PARTNER_KEY);
		
		
		
		//获取提交的商品价格
		String order_price = request.getParameter("order_price");
		//获取提交的商品名称
		String product_name = request.getParameter("product_name");
		
		//设置package订单参数
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("bank_type", "WX");  //支付类型   
		packageParams.put("body", productname); //商品描述   
		packageParams.put("fee_type","1"); 	  //银行币种
		packageParams.put("input_charset", "UTF-8"); //字符集    
		packageParams.put("notify_url", NOTIFY_URL); //通知地址  
		packageParams.put("out_trade_no", out_trade_no); //商户订单号  
		packageParams.put("partner", PARTNER); //设置商户号
		packageParams.put("spbill_create_ip",  request.getRemoteAddr()); //订单生成的机器IP，指用户浏览器端IP
		packageParams.put("total_fee",String.valueOf(((List)orderinfo.get(0)).get(2))); //商品总金额,以分为单位
		
		//获取package包
		String packageValue = reqHandler.genPackage(packageParams);
		logger.debug("packageValue -> "+packageValue);
		String noncestr = Sha1Util.getNonceStr();
		String timestamp = Sha1Util.getTimeStamp();
		
		//设置支付参数
		SortedMap<String, String> signParams = new TreeMap<String, String>();
		signParams.put("appid", APP_ID);
		signParams.put("appkey", APP_KEY);
		signParams.put("noncestr", noncestr);
		signParams.put("package", packageValue);
		signParams.put("timestamp", timestamp);
		//生成支付签名，要采用URLENCODER的原始值进行SHA1算法！
		String sign = Sha1Util.createSHA1Sign(signParams);
		
		//增加非参与签名的额外参数
		//signParams.put("paySign", sign);
		//signParams.put("signType", "SHA1");
		
		/*HashMap map = new HashMap();
		map.put("appid", APP_ID);
		map.put("timestamp", timestamp);
		map.put("noncestr", noncestr);
		map.put("package", packageValue);
		map.put("signtype", "SHA1");
		map.put("paysign", sign);*/
		
		JSONObject orderjo=new JSONObject();
		try {
			orderjo.put("appId", APP_ID);
			orderjo.put("timeStamp", timestamp);
			orderjo.put("nonceStr", noncestr);
			orderjo.put("package", packageValue);
			orderjo.put("signType", "SHA1");
			orderjo.put("paySign", sign);
		}catch(Exception e) {
			
		}
		logger.debug("weixin pay->"+orderjo.toString());
		return orderjo.toString();
	}
	
	public JSONObject getAlias(int pdtid) throws QueryException {
		
		String psql="select t.WX_SPECID,t.qty-nvl(t.lock_qty,0) from wx_alias t where t.WX_SPECID is not null and t.wx_appendgoods_id=?";
		Connection conn= QueryEngine.getInstance().getConnection();
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		JSONObject jor = new JSONObject();
		try{
			pstmt= conn.prepareStatement(psql);
			pstmt.setInt(1,pdtid);
			rs= pstmt.executeQuery();	
			while(rs.next()) {
				jor.put(rs.getString(1),  Tools.getInt(rs.getObject(2), 0));
			}
			logger.debug("jor ->"+jor.toString());
			 return jor;
		} catch (Exception e) {
			try {
				pstmt.close();
			} catch (Exception ea) {
			}
			try {
				rs.close();
			} catch (Exception ew) {
			}
			try {
					conn.close();
			} catch (Exception ec) {
			}
			logger.error("Error doing query:"+ psql + pdtid + ":" + e);
			throw new QueryException("Error doing query:" + psql,e);
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		
	}
	
}

