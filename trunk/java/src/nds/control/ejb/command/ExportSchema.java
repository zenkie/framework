/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.util.Tools;
import nds.schema.*;
import nds.control.ejb.*;
import java.io.*;

/**
 * Export Schema definition from oracle to hsql file 
 * @author yfzhu@agilecontrol.com
 */

public class ExportSchema extends Command{
	/**
	 * @param event, contains:
	 * 		scriptPath* - hsql script file path, like '/act/upload'
	 * 		scriptFileName* - hsql script file, with no path infomation
	 * 		filter_sql*	- tables to be exported. in format like 'select id from ad_table where xxxx'
	 * 		include_relate_table - "Y"/"N", if N, will not include relate tables
	 */
	public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
		logger.debug(event.toDetailString());
		helper.checkOperatorIsAdmin(event);
        URL url=nds.control.web.ServletContextManager.class.getResource("/hibernate.hsql.cfg.xml");
        
        java.io.StringWriter writer=new java.io.StringWriter();

        String scriptPath= Tools.decrypt((String)event.getParameterValue("scriptPath"));
        String fileName=(String)event.getParameterValue("scriptFileName");
        String scriptFile= scriptPath+ java.io.File.separator+ fileName;
        logger.debug("write to file:"+ scriptFile);
		String sql= (String)event.getParameterValue("filter_sql");
        boolean include_relate_table= Tools.getYesNo(event.getParameterValue("include_relate_table"), false);
        writer.write("Include relate table:"+ include_relate_table + StringUtils.LINE_SEPARATOR);
        SchemaExport se=new SchemaExport();
        int code=0;
        try{
        	se.init(url, include_relate_table);
        	java.sql.ResultSet rs=nds.query.QueryEngine.getInstance().doQuery("select name from ad_table where id "+sql, true);
        	while(rs.next()){
        		String tableName= (rs.getString(1));
        		se.transferTable(tableName);
        		writer.write("Transfer table definition:"+ tableName + StringUtils.LINE_SEPARATOR);
        	}
    		se.commit();
    		java.io.File file=new File(scriptFile);
    		  if(file.exists()) file.delete();
    		  se.saveToFile(scriptFile);
    		  writer.write("Saved definitions to "+ fileName+ StringUtils.LINE_SEPARATOR);
        }catch(Throwable t){
    		logger.error("Fail to do export", t);
        	code= -1;
        	try{
        		writer.write("Fail to do export:"+ t.getMessage()+ StringUtils.LINE_SEPARATOR);
        	}catch(Exception e){
        	}
        	try{
        		se.rollback();
        	}catch(Throwable tt){
        		logger.error("Could not rollback:"+ tt.getMessage());
        	}
        }finally{
        	try{se.destroy();}catch(Throwable tx){
        		logger.error("Could not destory:"+ tx.getMessage());
        	}
        }
        ValueHolder vh=new ValueHolder();
        String msg= "<PRE>"+writer.toString()+"</PRE>";
        vh.put("message", msg);
        vh.put("code",""+ code);
        vh.put("next-screen", nds.util.WebKeys.NDS_URI+"/reports/index.jsp");
        
        return vh;
	}
}
