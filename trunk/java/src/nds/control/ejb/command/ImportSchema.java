/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.util.NDSException;
import nds.util.StringUtils;
import nds.schema.*;
import nds.control.ejb.*;
import java.io.*;

/**
 * Import Schema definition from hsql file to oracle
 * @author yfzhu@agilecontrol.com
 */

public class ImportSchema extends Command{
	/**
	 * @param event, contains:
	 * 		scriptFile* - hsql script file, like '/path/to/trans', not file extension
	 * 		ad_table*	- tables to be imported(name, String).
	 * 		destConnection - if set, will be used as destination connection 
	 */
	public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
        logger.debug(event.toDetailString());
		helper.checkOperatorIsAdmin(event);
        StringWriter out= new StringWriter();
        URL url=nds.control.web.ServletContextManager.class.getResource("/hibernate.hsql.cfg.xml");
         
        
        String scriptFile=nds.util.Tools.decrypt( (String)event.getParameterValue("scriptFile"));
        logger.debug("script file:"+ scriptFile);
        Connection dest= (Connection)event.getParameterValue("destConnection");
		String[] tables= event.getParameterValues("ad_table");
        String filter=(String)event.getParameterValue("filter_sql");
        SchemaImport si=new SchemaImport();
        java.sql.Connection conn=nds.query.QueryEngine.getInstance().getConnection();
        
        
        int code=0;
        try{
        	conn.setAutoCommit(false);
            si.init(url, scriptFile, out, conn); // user defined connection, not use internal one
            for(int i=0;i<tables.length;i++){
            	si.transferTable( tables[i] );
            }
            si.commit();
        }catch(Throwable t){
        	code= -1;
        	logger.error("Fail to import schema:", t);
        	try{
        		si.outputMessage("Fail to do import:"+ t.getMessage());
        	}catch(Exception e){}
        	try{
        		si.rollback();
        	}catch(Throwable tt){
        		logger.error("Could not rollback:"+ tt.getMessage());
        	}
        }finally{
        	try{si.destroy();}catch(Throwable tx){
        		logger.error("Could not destory:"+ tx.getMessage());
        	}
        	//try{if(rs!=null)rs.close();}catch(Throwable tx){}
        	try{if(conn!=null)conn.close();}catch(Throwable tx){}
        }
        ValueHolder vh=new ValueHolder();
        vh.put("message", "<PRE>"+out.toString()+"</PRE>");
        vh.put("code",""+ code);
        return vh;
	}
}
