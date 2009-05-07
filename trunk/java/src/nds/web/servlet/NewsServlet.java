/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.model.Company;
import com.liferay.util.GetterUtil;
import com.liferay.util.ParamUtil;
import com.liferay.util.StringUtil;

/*import de.nava.informa.core.ChannelExporterIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.exporters.RSS_2_0_Exporter;
import de.nava.informa.impl.basic.ChannelBuilder;
*/

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.model.dao.UNewsDAO;
import nds.query.ColumnLink;
import nds.query.Expression;
import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
import nds.schema.TableManager;
import nds.util.Html;
import nds.util.Tools;
import nds.util.StringUtils;
import nds.util.Validator;


/**
 * Used for portal3
 * @deprecated
 * @author yfzhu@agilecontrol.com
 */

public class NewsServlet extends HttpServlet {
	private static Logger logger= LoggerManager.getInstance().getLogger(NewsServlet.class.getName());	
	//private final static java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");	
	private String _companyId;
	private Company _company;
	private String _ctxPath;

	public void init(ServletConfig sc) throws ServletException {
		synchronized (NewsServlet.class) {
			super.init(sc);

			ServletContext ctx = getServletContext();

			_companyId = ctx.getInitParameter("company_id");

			try {
				_company = CompanyLocalServiceUtil.getCompany(_companyId);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			String ctxPath =
				GetterUtil.get(sc.getInitParameter("ctx_path"), "/");

			_ctxPath = StringUtil.replace(ctxPath + "/c", "//", "/");
		}
	}
	public void service(HttpServletRequest req, HttpServletResponse res)
						throws IOException, ServletException {
		try {
			int categoryId =Tools.getInt( ParamUtil.getString(req, "categoryid"),-1);
			int clientId=Tools.getInt( ParamUtil.getString(req, "adclientid"),-1);
			String keywords = ParamUtil.getString(req, "keywords");
	
			String findEntryUrl =
				"http://" + _company.getPortalURL() + _ctxPath +
					"/ndsnews/find_entry?";
			String xml = getEntriesXML(clientId, categoryId, keywords, findEntryUrl, req);
			//logger.debug(xml);
			res.setContentType("text/xml;charset=UTF-8");
			
			res.getWriter().write(xml);
			res.getWriter().flush();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getEntriesXML(int clientId, int categoryId, String keywords,String findEntryUrl,HttpServletRequest req )
		throws Exception {
		
		
		java.sql.Connection conn= null;
		QueryEngine engine=QueryEngine.getInstance();
		try{
			conn=engine.getConnection();
		String channelName;
		
		if(clientId!=-1){
			channelName=(String)engine.doQueryOne("select name from ad_client where id="+clientId, conn);
		}else{
			channelName= _company.getName();
		}
		if( categoryId!=-1){
			channelName+="_"+(String)engine.doQueryOne("select name from u_newscategory where id="+categoryId, conn);
		}
		if(Validator.isNotNull(keywords)){
			channelName+= "_"+ keywords;
			keywords= StringUtils.replaceToken(keywords, ",;£¬£» ","|");
		}
		TableManager manager= TableManager.getInstance();
		UNewsDAO dao=new UNewsDAO();
		//Selection: id, subject, modifieddate, style, contenturl, ad_client_id
		QueryRequestImpl query= dao.createRequest(req, dao.getNDSUser(req) );
		query.addSelection(manager.getColumn("u_news", "PUBLISHER").getId());
		query.addSelection(manager.getColumn("u_news", "AUTHOR").getId());
		query.addSelection(manager.getColumn("u_news", "u_clob_id").getId());
		Expression expr=null;
		if( clientId !=-1 ){
			ColumnLink clink=new ColumnLink(new String[]{"u_news.AD_CLIENT_ID"});
			expr=new Expression(clink, "="+clientId, null );
			query.addParam(expr);
		}
		if( categoryId !=-1 ){
			ColumnLink clink=new ColumnLink(new String[]{"u_news.U_NEWSCATEGORY_ID"});
			expr=new Expression(clink, "="+categoryId, null );
			query.addParam(expr);
		}
		if(!Validator.isNull(keywords)){
			keywords=StringUtils.replaceToken(keywords, ",;£¬£» ","|");
			expr=new Expression(null, "CATSEARCH(U_NEWS.KEYWORDS, '"+ keywords +"', 'order by modifieddate desc') > 0", null );
			query.addParam(expr);
		}
		
		query.setRange( 0, 20);
		logger.debug("news rss:"+ query.toSQL());
		
		/*ChannelBuilder builder = new ChannelBuilder();

		ChannelIF channel = builder.createChannel(channelName);
		*/
		QueryResult rs=engine.doQuery( query);
		int id;
		int clobId;
		String subject, publisher, author, desc, contenturl;
		java.util.Date date ;
		URL url;
		while(rs.next()){
			// Selection: id, subject, modifieddate, style, contenturl, ad_client_id, publisher, author,u_clob_id
			id=((java.math.BigDecimal)rs.getObject(1)).intValue();
			subject= (String)rs.getObject(2);
			if(rs.getObject(3) !=null){ 
		 	 	date= (Timestamp)rs.getObject(3);
		 	 }else{
		 	 	date=null;
		 	 }
			contenturl= (String)rs.getObject(5);
			publisher= (String)rs.getObject(7);
			author= (String)rs.getObject(8);
			
			clobId=  Tools.getInt(rs.getObject(9),-1);
			if(Validator.isNotNull(contenturl )){
				if(!contenturl.startsWith("http://")) contenturl= "http://" + _company.getPortalURL() + contenturl;
				url=new URL(contenturl);
			}else{
				url= new URL(findEntryUrl+"id="+id);
			}

			if(clobId!=-1 &&  Validator.isNull( contenturl)){
				desc= (String)engine.doQueryOne("select SUBSTR(to_char(content),1,512) from u_clob where id="+clobId);
				if(Validator.isNotNull(desc)) {
					desc=StringUtils.shorten( Html.stripHtml(desc), 100, "...");
				}
			}
			else{
				desc = "<a href=\""+ url+"\">"+ url+"</a>";
			}
			/*ItemIF item = builder.createItem(
					channel, subject, desc, url);	
			item.setDate(date);*/
		}

		/*		StringWriter writer = new StringWriter();

			ChannelExporterIF exporter = null;

			String charEncoding = "UTF-8";
			exporter = new RSS_2_0_Exporter(writer, charEncoding);
			exporter.write(channel);
			return writer.getBuffer().toString();
			*/
			return "Drepcated method";
		}finally{
			if(conn!=null){try{conn.close();}catch(Throwable t){}}
		}

		
	}	
	
}
