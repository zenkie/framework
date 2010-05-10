package nds.velocity;

import nds.schema.*;
import nds.query.*;
import nds.web.config.*;
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
	private final static String WEB_CLIENT="select id from web_client where ad_client_id=?";
	private final static String FRIENDURL="select name,url,web_target from web_friendurl where ad_client_id=?";
	private final static int MAX_COLUMNLENGTH_WHEN_TOO_LONG=30;//QueryUtils.MAX_COLUMN_CHARS -3
	
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
			this.serverRootURL= serverRootURL+"html/nds/website/";
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
		if(hasTemplateFolder)  
			return serverRootURL+nds.control.web.WebUtils.getAdClientTemplateFolder(webDomain);
		else
			return serverRootURL;
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
			QueryEngine.getInstance().executeUpdate("update u_news set readcnt=readcnt+1 where id="+ newsId);
		}catch(Throwable t){
			logger.error("Fail to update news id="+ newsId, t);
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
	
	
	public String toHTMLControlForm(QueryRequest req, Expression userExpr, String nameSpace) throws QueryException {
		return QueryUtils.toHTMLControlForm(req, userExpr,nameSpace);
	}
	/**
	 * 
	 * @param adListDataConfName name of ad_listdataconf table, note name is unique in ad_listdataconf
	 * @param adListUIConfName name of ad_listuiconf table, note name is unique in ad_listuiconf  
	 * @return List of List, first element will be pk link, others will be columns specified by dataConfig
	 */
	public List getList(String adListDataConfName, String adListUIConfName )throws Exception{
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
		
	    String mainTablePath=getServerRootURL()+
	    			(dataConfig.getMainURL().startsWith("/")?"":"/")+
	    				dataConfig.getMainURL(); // like "/news.vml", this is used in preview page
	    if(mainTablePath.indexOf("@ID@")==-1){
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
	public Map getObject(String tableName, int objId, int[] columnMasks) throws Exception{
		QueryEngine engine= QueryEngine.getInstance();
		TableManager manager= TableManager.getInstance();
		Table table=manager.getTable(tableName);
		
		QueryRequestImpl query= engine.createRequest(null);
		query.setMainTable(table.getId());
		
		query.addColumnsToSelection(columnMasks, false);
	
		Expression expr=new Expression(new ColumnLink(new int[]{table.getPrimaryKey().getId()}), "="+objId, null);
		if(table.isAcitveFilterEnabled()){
			expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null), SQLCombination.SQL_AND, null);
		}
		if(table.isAdClientIsolated()){
			expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+adClientId, null), SQLCombination.SQL_AND, null);
		}
		query.addParam(expr);
		
		QueryResult result= engine.doQuery(query);
		HashMap map = new HashMap();
		
		if(result.getRowCount()>0){
			result.next();
			ArrayList columns= table.getColumns(columnMasks,false,0);
			for(int i=0;i<columns.size();i++){
				Column column=(Column) columns.get(i);
				map.put(column.getName().toLowerCase(), result.getObject(i+1));
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
		return getObject(tableName, objId, new int[]{Column.MASK_QUERY_LIST});
	}
	/**
	 * Return record column data in map of specified record, column is from Column.MASK_QUERY_LIST=1
	 * @param tableName in ad_table
	 * @param objId id of that record
	 * @return key: column name in lower case, value: column data formatted according to ad_column definition   
	 */
	public Map getObject(String tableName, int objId, int mask) throws Exception{
		return getObject(tableName, objId, new int[]{mask});
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

}

