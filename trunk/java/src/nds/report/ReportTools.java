/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.report;

import nds.control.web.*;
import nds.util.Attachment;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.ParamUtils;
import nds.util.WebKeys;
import nds.query.*;
import java.util.*;
import java.io.File;
import java.sql.*;
import nds.schema.*;
import nds.security.User;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class ReportTools {
	private static String reportWebRoot=null;
	public static void setReportWebRoot(String r){
		reportWebRoot=r;
	}
	public static String getReportWebRoot(){
		if(reportWebRoot==null){
			reportWebRoot =((Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS)).getProperty("web.url", "http://localhost");
		}
		return reportWebRoot;
	}
	/**
	 * different client has different report template on the same table
	 * @param type
	 * @param tableName
	 * @param clientName
	 * @return
	 */
	public static String getReportURL(String type, String tableName, String clientName){
		return getReportWebRoot()+"/servlets/binserv/JasperReport?type="+ type +"&table="+ tableName+"&client="+ clientName;
	}
	public static String getReportURL(int adReportId, String clientName){
		return getReportWebRoot()+"/servlets/binserv/JasperReport?id="+ adReportId+"&client="+ clientName;
	}
	/**
	 * Create job id, so every report has identity.
	 * @return String 
	 */
	public static String createJobId(String userId){
		return "JobID="+System.currentTimeMillis()+(userId==null?"":userId);
	}
	/**
	 * 
	 * @param user
	 * @return available space size of user in bytes
	 */
	public static long getAvailableSpaceSize(User user){
		return getSpaceQuota()-getSpaceUsed(user);
	}
	/**
	 * 
	 * @return Space quota set in "export.quota" of portal.properties in bytes
	 */
	  public static long getSpaceQuota(){
	  	 Configurations	conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
	      String quota = conf.getProperty("export.quota","5M");
	      if(quota.substring(quota.length() - 1).equalsIgnoreCase("m"))
	          return Integer.valueOf(quota.substring(0,quota.length()-1)).longValue()*1024*1024;
	      else if(quota.substring(quota.length() - 1).equalsIgnoreCase("k"))
	          return Integer.valueOf(quota.substring(0,quota.length()-1)).longValue()*1024;
	      else
	          return Integer.valueOf(quota.substring(0,quota.length())).longValue();
	  }
	  /**
	   * 
	   * @return used space size of user in bytes
	   */
	  public static long getSpaceUsed(User user){
	  	 Configurations	conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
	  	  String exportRootPath= conf.getProperty("export.root.nds","/aic/home");
	      File dir = new File(exportRootPath+File.separator+ user.getClientDomain()+File.separator+  user.name);
	      
	      if(!dir.exists() || !dir.isDirectory())
	          return 0;
	      File[] files = dir.listFiles();
	      long spaceUsed = 0;
	      for(int i = 0;i < files.length;i++){
	          if(files[i].isFile())
	              spaceUsed += files[i].length();
	      }
	      return spaceUsed;
	  }	
	/**
	 * 
	 * @param reportId
	 * @param clientName
	 * @return report file in server directory
	 * @throws Exception
	 */
	public static String getReportFile(int reportId, String clientName) throws Exception{
		TableManager manager=TableManager.getInstance();
		Table table=manager.getTable("ad_report");
		int tableId= table.getId();
		int columnId =manager.getColumn("ad_report", "FILEURL").getId();
		int objectId= reportId;
		int version=-1;
		if( tableId == -1) {
	       	throw new NDSException("object type not set");
		}
		Column col=manager.getColumn(columnId);
		AttachmentManager attm=(AttachmentManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.ATTACHMENT_MANAGER);
		Attachment att= attm.getAttachmentInfo(clientName+"/" + table.getRealTableName()+"/"+col.getName(),  objectId+"" , version);
		String fileName= table.getName()+ "_"+ manager.getColumn(columnId).getName()+"_"+ objectId+"_"+att.getVersion()+"."+ att.getExtension();
		File reportXMLFile=attm.getAttachmentFile(att);
		return reportXMLFile.getAbsolutePath();
		
	}
	
}
