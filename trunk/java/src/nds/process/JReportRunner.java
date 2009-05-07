/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import java.util.*;
import java.sql.*;

import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.schema.*;
import nds.query.*;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;

/**
 * Create jasperreport. 2 types: with table, without table 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class JReportRunner extends SvrProcess
{
	private String filterExpr;
	private String query;
	private String cxtabName;
	private String fileName;
	private String fileType;
	private QueryEngine engine;
	/**
	 *  Parameters:
	 *    filter_expr(with table), query(without table) 
	 * 	  cxtab, filename
	 *  and preps_xxx for cxtab preprocess
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			log.debug("name="+name+",param="+ para[i]);
			if (name.equals("filter_expr"))
				filterExpr = ((String)para[i].getInfo());
			else if (name.equals("query"))
				query= ((String)para[i].getInfo());
			else if (name.equals("cxtab"))
				cxtabName= ((String)para[i].getParameter());
			else if (name.equals("filename"))
				fileName= ((String)para[i].getParameter());
			else if (name.equals("filetype"))
				fileType= ((String)para[i].getParameter());
		}
	}	//	prepare
	
	
	/**
	 *  Perrform process. will create and execute immdiate preprocess first if nessisary
	 *  
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		long startTime= System.currentTimeMillis();
	  	Connection conn=null;
	  	PreparedStatement pstmt=null;
		engine=  QueryEngine.getInstance();
	  	
	    try{
		    conn= engine.getConnection();
		    // check pre-process for cxtab
			log.debug("filterExpr="+ filterExpr);
			log.debug("query="+ query);
		    
		    // create group query
		    nds.cxtab.JReport cr=new nds.cxtab.JReport();
		    cr.setCxtabName(cxtabName);
		    cr.setFileName(fileName);
		    cr.setQuery(query);
		    cr.setFilterExpr(filterExpr);
		    cr.setFileName(fileName);
		    cr.setFileType(fileType);
		    cr.setUserId( this.getAD_User_ID());
		    cr.setAdClientId(this.getAD_Client_ID());
		    String finalFile= cr.create(conn);
		    if( "xls".equalsIgnoreCase(fileType)){
		    	// this is asynchronous task, so should notifiy user (creator) of the completion
		    	notifyOwner("["+cxtabName+"]@report-created@"+((System.currentTimeMillis() - startTime)/1000),
		    			"@click-attach-url-to-download@",
		    			"/servlets/binserv/GetFile?filename="+ finalFile);
		    			//+",@file-name@:"+finalFile,"/servlets/binserv/GetFile?filename="+ finalFile);
		    }
		    return "File created:" +finalFile;
		    
			
	    }catch(Throwable e){
		 	log.error("", e);
		 	if( "xls".equalsIgnoreCase(fileType)){
		    	// this is asynchronous task, so should notifiy user (creator) of the completion
		    	notifyOwner("["+cxtabName+"]@report-creation-failed-for-cxtab@",
		    			"@error-msg@:"+ nds.util.StringUtils.getRootCause(e).getMessage(),null);
		    }
		 	if(!(e instanceof NDSException ))throw new NDSEventException("@exception@", e);
		 	else throw (NDSException)e;
	    }finally{
	    	if(conn!=null){
	    		try{conn.close();}catch(Throwable t){}
	    	}
	    }

	}
	
}
