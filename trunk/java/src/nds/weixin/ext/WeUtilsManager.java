package nds.weixin.ext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryException;

public class WeUtilsManager {
	private static WeUtilsManager instance=null;
	private static Hashtable<String,WeUtils> weUtiles ;
	private static Logger logger= LoggerManager.getInstance().getLogger(WeUtilsManager.class.getName());
	private final static String websql="select wxi.ad_client_id, wxc.DOMAIN, wxc.wxnum, wxc.wxtype, wxi.url,"+
       "wxi.appid, wxi.appsecret, wxi.wxparam,wxi.wxtoken, nvl(ast.foldername, ''),nvl(ast1.foldername,'')"+
       " from WEB_CLIENT wxc LEFT JOIN WX_INTERFACESET wxi ON wxi.ad_client_id = wxc.ad_client_id "+
       "LEFT JOIN WEB_CLIENT_TMP wct ON wct.ad_client_id = wxc.ad_client_id "+
       "LEFT JOIN AD_SITE_TEMPLATE ast ON ast.id = wct.home_tmp "+
       "LEFT JOIN WEB_MAIL_TMP wmt ON wmt.ad_client_id=wxc.ad_client_id LEFT JOIN "+
       "AD_SITE_TEMPLATE ast1 ON ast1.id=wmt.home_tmp "+
       "where wxc.DOMAIN=? and wxc.domain is not null";
	
	
	public WeUtilsManager(){
		logger.debug("WeUtilsManager is init!!!!");
		if(weUtiles==null){weUtiles=new Hashtable<String,WeUtils>();}
		try {
			List localList = QueryEngine.getInstance().doQueryList("select wxi.ad_client_id,wxc.DOMAIN,wxc.wxnum,wxc.wxtype,"+
		"wxi.url,wxi.appid,wxi.appsecret,wxi.wxparam,wxi.wxtoken,nvl(ast.foldername,''),nvl(ast1.foldername, '') "+
		"from WEB_CLIENT wxc LEFT JOIN WX_INTERFACESET wxi ON wxi.ad_client_id=wxc.ad_client_id "+
		"LEFT JOIN WEB_CLIENT_TMP wct ON wct.ad_client_id=wxc.ad_client_id LEFT JOIN"+
		" AD_SITE_TEMPLATE ast ON ast.id=wct.home_tmp "+
		"LEFT JOIN WEB_MAIL_TMP wmt ON wmt.ad_client_id=wxc.ad_client_id LEFT JOIN "+
	    "AD_SITE_TEMPLATE ast1 ON ast1.id=wmt.home_tmp where wxc.domain is not null"
					);
			WeUtils wu = null;
		      for (int i = 0; i < localList.size(); i++) {
		    	  wu = new WeUtils((List)localList.get(i));  
		    	  weUtiles.put(wu.getCustomId(), wu);
		    	  logger.debug("init WeUtils->"+i);
		      }
		} catch (QueryException e) {
			e.printStackTrace();
		}
	}
	
	public WeUtils getWeUtils(String customId){
		if(weUtiles.containsKey(customId)){return weUtiles.get(customId);}
		return null;
	}
	
	public static WeUtils addWeUtils(List l){
		WeUtils wu=null;
		if(l==null||l.size()<9) {return wu;}
		String customId=(String)l.get(7);
		if(weUtiles.containsKey(customId)) {
			wu=weUtiles.get(customId);
			return wu;
		}else {
			wu=new WeUtils(l);
			if(wu!=null){weUtiles.put(wu.getCustomId(), wu);}
		}
		
		return wu;
	}
	
	public static WeUtils getByAdClientId(int adClientId) {
		WeUtils wu=null;
		if(weUtiles!=null) {
			WeUtils w=null;
			for(Enumeration<WeUtils> wus=weUtiles.elements();wus.hasMoreElements();) {
				w=wus.nextElement();
				if(w.getAd_client_id()==adClientId) {
					wu=w;
					break;
				}
			}
		}
		return wu;
	}
	
	
	public static WeUtils getByDomain(String webDomain) {
		WeUtils wu=null;
		if(weUtiles!=null) {
			logger.debug("weUtiles size is"+weUtiles.size());
			WeUtils w=null;
			for(Enumeration<WeUtils> wus=weUtiles.elements();wus.hasMoreElements();) {
				w=wus.nextElement();
				logger.debug("weUtiles domain is->"+w.getDoMain());
				if(w.getDoMain().equals(webDomain)) {
					logger.debug("weUtiles domain is eauals");
					wu=w;
					break;
				}
			}
		}
		return wu;
	}
	
	public static synchronized WeUtilsManager getInstance(){
		if(instance==null){instance=new WeUtilsManager();}
		return instance;
	}
	
	
	

	/**
	 * Unloading adclient from weUtiles
	 * 
	 * @param adClientId
	 */
	public static void unloadAdClientId(Integer adClientId) {
		Object key=getByAdClientId(adClientId).getCustomId();
		weUtiles.remove(key);
	}
	
	
	/**
	 * get AD_SITE_TEMPLATE.FOLDERNAME
	 * 
	 * @param clientDomain
	 *            , web_client.DOMAIN, should be case insensitive
	 * @return null if not found
	 */
	
	public static String getAdClientTemplateFolder(String webDomain) {
		WeUtils dc = loadAdClient(webDomain);
		if (dc == null)
			return null; // default to 001
		return (String) dc.getFoldName();
	}
	
	
	public static String getAdClientmallTemplateFolder(String webDomain) {
		WeUtils dc = loadAdClient(webDomain);
		if (dc == null)
			return null; // default to 001
		return (String) dc.getFoldMall();
	}
	
	/**
	 * 
	 * @param webDomain
	 *            web_client.domain
	 * @return null if not found, or [ad_client.id (Integer), ad_client.domain
	 *         (String)]
	 */
	private static WeUtils loadAdClient(String webDomain) {
		if (webDomain == null)
			return null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String d = webDomain.toLowerCase();
		List al=new java.util.ArrayList();
		WeUtils dc = getByDomain(d);
		//if(weUtiles==null){weUtiles=new Hashtable<String,WeUtils>();}
		try {
			if (dc == null) {
				// loading from db
				conn = QueryEngine.getInstance().getConnection();
				List localList = QueryEngine.getInstance().doQueryList(websql,new Object[] { String.valueOf(webDomain)},conn);
				 for (int i = 0; i < localList.size(); i++) {
			    	  dc = new WeUtils((List)localList.get(i));  
			    	  logger.debug("weutils key is ->"+dc.getCustomId());
			    	  weUtiles.put(dc.getCustomId(), dc);
				 }
				} else {
					logger.debug("find client for " + webDomain);
				}
		} catch (Throwable t) {
			logger.error("Fail to get ad_client.id from " + webDomain, t);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Throwable t2) {
			}
		}
		return dc;
	}
	
	
}
