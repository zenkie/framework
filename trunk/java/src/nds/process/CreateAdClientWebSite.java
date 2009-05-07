package nds.process;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import nds.control.event.NDSEventException;
import nds.control.web.WebUtils;
import nds.util.*;
import nds.query.*;
import nds.schema.*;
import nds.util.NDSException;
import nds.web.config.ListDataConfig;
import nds.web.config.ListUIConfig;
import nds.web.config.PortletConfigManager;

import java.util.*;
/**
 * 创建公司静态网站

内建一个WebConversation， 获取ad_client 和 ad_client 所选择的网站模板

依次读取每个指定的单模板网页，生成静态文件。单模板网页包括：index.vml, products.vml, services.vml, solutions.vml, aboutus.vml

读取每个多模板网页，根据对应数据表，生成网页内容。多模板网页包括:
news.vml ( 对应表 U_NEWS, 过滤条件doctype is null ), product.vml (对应表WEB_PRODUCT, 过滤调节无), solution.vml


 * @author yfzhu
 *
 */
public class CreateAdClientWebSite extends SvrProcess{
	private final static String DEFAULT_LANG= "UTF-8";
	/**
	 * At most how many records fetched for each kind of table
	 * Since this is for small web site, the value is not quite big.
	 */
	private final static int MAX_RECORDS_FETCHED=200;
	private int clientId;
	private WebConversation wc;
	private Connection conn;
 	private String webRoot ;       // server ip and port, such like http://www.demo.com:8001
 	private String templateFolder; // relative to /html/nds/website
	private String outputFolder; // output the static html pages, like "/act/websites/www.demo.com/"
	/**
	 * these pages will be generated only once
	 */
	private final static String[] OnePageTemplate=new String[]{
		"index.vml", "aboutus.vml"
	};
	/**
	 * these pages will be generated mutliple times as it's for list 
	 */ 
	private final static String[][] MultiplePageTemplate=new String[][]{
		{"solution", "solution.vml"},
		{"service","service.vml"},
		{"news","news.vml"},
		{"product","product.vml"}
	};
	
	
	/**
	 *  Parameters:
	 *    ad_client_id - id of ad_client
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			log.debug("name="+name+",param="+ para[i]);
			if (name.equals("ad_client_id"))
				clientId =Tools.getInt( para[i].getParameter(), -1);
		}
	}	
	/**
	 *  Perrform process. will create and execute immdiate preprocess first if nessisary
	 *  
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		if(clientId==-1) throw new NDSException("Client id not set");

		long startTime= System.currentTimeMillis();
	  	PreparedStatement pstmt=null;
		QueryEngine engine=  QueryEngine.getInstance();
		Configurations conf=((Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS));
		webRoot=conf.getProperty("server.url", "http://localhost");
	    try{
		    conn= engine.getConnection();
		    List al= (List)QueryEngine.getInstance().doQueryList("select w.domain, w.foldername from web_site_template w, web_client c where c.ad_client_id="+clientId+" and w.id=c.web_site_template_id", conn);
		    String domain=  (String)((List) al.get(0)).get(0);
		    templateFolder= (String)((List) al.get(0)).get(1);
			outputFolder=conf.getProperty("websites.root", "/act/sites")+"/"+domain+"/";
		    
		    // get web session ready, set client id to session context
		    prepareWebConversation();
		    
		    for(int i=0;i< OnePageTemplate.length;i++)
		    	createPage(OnePageTemplate[i]);
		    
		    for(int i=0;i<MultiplePageTemplate.length;i++ )
		    	createPages(MultiplePageTemplate[i][0],MultiplePageTemplate[i][1] );
		    
	    	notifyOwner("@website-created@",
	    			"@website-created@"+((System.currentTimeMillis() - startTime)/1000)+"s",
	    			null);
		    
		    return "@complete@";
	    }catch(Throwable e){
		 	log.error("", e);
		    	// this is asynchronous task, so should notifiy user (creator) of the completion
		    	notifyOwner("@website-creation-failed@",
		    			"@error-msg@:"+ nds.util.StringUtils.getRootCause(e).getMessage(),null);
		 	if(!(e instanceof NDSException ))throw new NDSEventException("@exception@", e);
		 	else throw (NDSException)e;
	    }finally{
	    	if(conn!=null){
	    		try{conn.close();}catch(Throwable t){}
	    	}
	    	try{
	    		closeWebConversation();
	    	}catch(Throwable t){
	    		log.error("Fail to close web conversion", t);
	    	}
	    	log.debug("website client="+ clientId+ " finished, total:"+ ((System.currentTimeMillis()-startTime)/1000.0)+" s");
	    }

	}
	/**
	 * 
	 * @param confName adListDataConf.name
	 * @param pageName handling file name 
	 * @throws Exception
	 */
	private void createPages(String confName, String pageName) throws Exception{
		List ids= getList(confName);
		for(int i=0;i< ids.size();i++){
			Object pk= ids.get(i);
			createPage(pageName+"?id="+ pk);
		}
	}
	/**
	 * 
	 * @param adListDataConfName name of ad_listdataconf table, note name is unique in ad_listdataconf
	 * @return List of Object( PK Integer)
	 */
	public List getList(String adListDataConfName )throws Exception{
		PortletConfigManager pcManager=(PortletConfigManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PORTLETCONFIG_MANAGER);
		ListDataConfig dataConfig= (ListDataConfig)pcManager.getPortletConfig(adListDataConfName,nds.web.config.PortletConfig.TYPE_LIST_DATA);
		
		TableManager manager=TableManager.getInstance();
		QueryEngine engine=QueryEngine.getInstance();
		int tableId=dataConfig.getTableId();
		Table table;
		table= manager.getTable(tableId);

		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);

	    query.setMainTable(tableId,true, dataConfig.getFilter());
	    		
	    query.addSelection(table.getPrimaryKey().getId());
		if( dataConfig.getOrderbyColumnId()!=-1){
			Column orderColumn= manager.getColumn(dataConfig.getOrderbyColumnId());
			if(orderColumn!=null && orderColumn.getTable().getId()== tableId)query.setOrderBy(new int[]{dataConfig.getOrderbyColumnId()}, dataConfig.isAscending());
		}else{
	    	query.setOrderBy(new int[]{ table.getPrimaryKey().getId()}, false);
	    }
	    query.setRange(0,MAX_RECORDS_FETCHED);
	    
	    Expression expr=null;
		if(table.isAcitveFilterEnabled()){
			expr=new Expression(new ColumnLink(new int[]{table.getColumn("ISACTIVE").getId()}), "=Y", null);
		}
		if(table.isAdClientIsolated()){
			if(expr!=null)expr=expr.combine(new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+clientId, null), SQLCombination.SQL_AND, null);
			else expr=new Expression(new ColumnLink(new int[]{table.getColumn("AD_CLIENT_ID").getId()}), "="+clientId, null);
		}
		query.addParam(expr);
		
		QueryResult result= QueryEngine.getInstance().doQuery(query,conn);
		
        ArrayList data=new ArrayList();
		while(result.next()){
	        data.add(result.getObject(1));
		}
		
	    
		return data;
	}
	/**
	 * Save page content to file, file name will be relative to outputFolder, in format like:
	 * "news.vml_id_133.html"
	 * 
	 * @param page relative to webRoot+"/html/nds/website/"+ templateFolder, in format like:
	 * 	"news.vml?id=133"
	 * 
	 * @throws Exception
	 */
	private void createPage(String page) throws Exception{
		WebResponse res= wc.getResponse(webRoot+"/html/nds/website/"+ templateFolder+"/"+page);
		if(res !=null){
			String content=res.getText();
			if( content!=null) content= content.replaceAll("\n","");
			
			String fileName= page.replace('?','_');
			fileName=fileName.replace('=','_');
			fileName= outputFolder+ fileName+".html";
			Tools.writeFile(fileName, content, "UTF-8");
		}
		
	}
	/**
	 * Create session and set client id
	 *
	 */
	private void prepareWebConversation( ) throws Exception{
		wc= new WebConversation();
		wc.setHeaderField("Accept-Language", DEFAULT_LANG);
		
		wc.getResponse(webRoot+"/html/nds/website/setclient.vml?client="+clientId);
	}
	private void closeWebConversation() throws Exception{
		wc.getResponse(webRoot+"/html/nds/website/closesession.jsp");
		wc=null;
	}
}
