/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.report.ReportTools;
import nds.security.User;
import nds.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * 刷新报表参数
 * JReport 利用JasperReport 来构造统计报表，AD_JREPORT 表，用于描述报表，指明报表文件位置，上有一按钮，识别参数。即对应当前类，
 * 将报表中定义的参数识别出来。构造到 AD_JREPORT_PARA 中，PARA 定义：
Name, type, defaultValue, description, ad_column_id, selectiontype
Type: NUMBER | STRING | DATE 
SELECTIONTYPE: M | S
在点击识别参数按钮时，系统读取报表对应文件，对每个设置为 “USE AS PROMPT”的报表参数进行解析。如果定义已在参数中定义，则进行更新。对于不再存在的报表参数，还需执行删除。当然，所有的参数也允许用户直接在列表中调整。

报表放置在/act/jreport 目录，通过 portal.properties 的 JREPORT_HOME进行修改
 
 * @author yfzhu@agilecontrol.com
 */

public class JReportRefreshParams extends Command{
	/**
	 * @param event parameters:
	 *    objectid - object id of ad_jreport table		
	 * */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	User usr=helper.getOperator(event);
    	int reportId=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	
    	if (!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, "AD_CXTAB", reportId, nds.security.Directory.WRITE, event.getQuerySession()))
    			throw new NDSException("@no-permission@");
    	
    	ValueHolder holder=new ValueHolder();
    	String message="@complete@";
    	java.sql.Connection conn=QueryEngine.getInstance().getConnection();
    	QueryEngine engine=QueryEngine.getInstance();
    	PreparedStatement pstmt=null;
    	try{
    		String filePath=(String)engine.doQueryOne("select attr2 from ad_cxtab where id="+ reportId, conn);
    		
    		File reportXMLFile=new File(filePath);
    		if( reportXMLFile.exists()){    		

    			String reportName=reportXMLFile.getName().substring(0, reportXMLFile.getName().lastIndexOf("."));
				File reportJasperFile = new File(reportXMLFile.getParent(), reportName+ ".jasper");
				if( !reportJasperFile.exists()|| reportJasperFile.lastModified()<reportXMLFile.lastModified()){
					JasperCompileManager.compileReportToFile(reportXMLFile.getAbsolutePath(),reportJasperFile.getAbsolutePath() );
				}
				JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reportJasperFile);
				JRParameter[] params=jasperReport.getParameters();
				engine.executeUpdate("delete from ad_cxtab_jpara where ad_cxtab_id="+ reportId); 
				pstmt= conn.prepareStatement("insert into ad_cxtab_jpara(id, ad_cxtab_id, orderno,name, paratype, description, ad_column_id, selectiontype, creationdate,modifieddate, ownerid, modifierid, isactive,ad_client_id) values ("+
						"get_sequences('ad_cxtab_jpara'),?,?,?,?,?,?,?,sysdate,sysdate,?,?,'Y',?)");
				for(int i=0;i< params.length;i++){
					JRParameter param= params[i];
					if(!param.isForPrompting() || param.isSystemDefined())continue;
					pstmt.setInt(1,reportId);
					pstmt.setInt(2,(i+1)*10);
					pstmt.setString(3, param.getName());

					pstmt.setString(4, parseType(param.getValueClass()));
					String desc=param.getDescription();
					if(Validator.isNull(desc)) desc= param.getName();
					pstmt.setString(5,desc);
					JRPropertiesMap map =param.getPropertiesMap();
					if(map!=null){
						String column= map.getProperty("column");
						String selectionType= map.getProperty("selectiontype");
						if(column!=null){
							Column col= ( new ColumnLink(column).getLastColumn());
							pstmt.setInt(6, col.getId() );
						}else{
							pstmt.setNull(6,java.sql.Types.NUMERIC);
						}
						if( Validator.isNotNull(selectionType) && ("S".equalsIgnoreCase(selectionType)) ){
							pstmt.setString(7, "S");
						}else{
							pstmt.setString(7,"M");
						}
					}else{
						pstmt.setNull(6,java.sql.Types.NUMERIC);
						pstmt.setNull(7,java.sql.Types.VARCHAR);
					}
					pstmt.setInt(8, usr.id.intValue());
					pstmt.setInt(9, usr.id.intValue());
					pstmt.setInt(10, usr.adClientId);
					pstmt.executeUpdate();
				}
    		}else{
    			// file not fount
    			message="@jreport-file-not-found@";
    		}
			
    		holder.put("code","0");
    	}catch(Throwable e){
    		logger.error("User "+ usr.getName() + "@" + usr.getClientDomain()+" fail to refresh jreport:"+ reportId, e);
    		message="@jreport-found-error@:"+ e.getMessage();
    		throw new NDSException(message);
    	}finally{
    		try{
    			pstmt.close();
    		}catch(Exception e){}
    		try{
    			conn.close();
    		}catch(Exception e){}
    	}
		holder.put("message", message);
		holder.put("code", new Integer(1));// refresh current page		
    	return holder;
    }
    /**
     * Parse java class name to param type:
     * 	 "N" - Number
     * 	 "S" - String
     *   "D" - Date 
     * @param javaclassName 
     * @return
     */
    private String parseType(Class clazz) throws NDSException{
    	if(clazz== java.lang.Integer.class || clazz== java.lang.Float.class || clazz==  java.lang.Double.class)
    		return "N";
    	else if(clazz == java.lang.String.class)
    		return "S";
    	else if(clazz== java.util.Date.class){
    		return "D";
    	}
    	throw new NDSException("@unsupported-class@:"+ clazz.getName() );
    }
}
