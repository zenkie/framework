/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import java.util.*;
import java.sql.*;

import nds.control.event.*;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.schema.*;
import nds.query.*;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.security.User;
import nds.util.Configurations;
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
	private String filter;
	private String folder;
	
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
			else if (name.equals("filter"))
				filter= ((String)para[i].getParameter());
			else if (name.equals("folder"))
				folder= ((String)para[i].getParameter());
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
		boolean isBackground=true;
	    try{
		    conn= engine.getReadConnection();
		    // check PRE_PROCEDURE for cxtab add tmp table
		    conn.setAutoCommit(false);
		    // check PRE_PROCEDURE for cxtab add tmp table
		    //System.out.print("tmp cmit!!!");
		    // check pre-process for cxtab
			log.debug("filterExpr="+ filterExpr);
			log.debug("query="+ query);

			//check PRE_PROCEDURE for cxtab
		    
		    List cxInfo=(List) engine.doQueryList(
		    		"select PRE_PROCEDURE, AD_PI_COLUMN_ID,attr2 from ad_cxtab where ad_client_id="+ this.getAD_Client_ID()+
		    		" and name="+ QueryUtils.TO_STRING(cxtabName), conn);
		    String preProcedure=(String) ((List)cxInfo.get(0)).get(0);
		    int piColumnId= Tools.getInt( ((List)cxInfo.get(0)).get(1), -1);
		    String template=(String) ((List)cxInfo.get(0)).get(2);
		    if( nds.util.Validator.isNotNull(preProcedure)){
		    	ArrayList al=new ArrayList();
		    	al.add(new Integer(this.getAD_PInstance_ID()));
		    	engine.executeStoredProcedure(preProcedure, al, false,conn);
		    	
		    }	
		    isBackground=Tools.getYesNo( engine.doQueryOne("select c.isbackground from ad_cxtab c where c.ad_client_id="+ this.getAD_Client_ID() +" and c.name="+ QueryUtils.TO_STRING(cxtabName),conn), false);		    
		    String finalFile=null;
		    /**
		     * According to template
		     * There's 3 types extension for special report generation:
		     * jrxml:, java:, and python:
		     * jrxml is the default one  
		     */
		    if(template==null)template="";
		    String[] tp=template.split(":");
		    
		    if(tp.length==1 || tp[0].equals("jrxml")){
		    	// jrxml
			    // create group query
			    nds.cxtab.JReport cr=new nds.cxtab.JReport();
			    cr.setCxtabName(cxtabName);
			    cr.setFileName(fileName);
			    cr.setQuery(query);
			    cr.setFilterExpr(filterExpr);
			    cr.setFileName(fileName);
			    cr.setFileType(fileType);
			    cr.setFilterDesc(filter);
			    cr.setUserId( this.getAD_User_ID());
			    cr.setAdClientId(this.getAD_Client_ID());
			    cr.setAD_PInstance_ID(this.getAD_PInstance_ID()); // this will be needed when doing cube exporting
			    cr.setFolder(folder);
			    finalFile= cr.create(conn);
			    log.debug("is jrxml");
		    }else if(tp[0].equals("python")){
		    	finalFile=nds.control.util.PythonScriptUtils.runProcess(tp[1],this.getAD_PInstance_ID());
		    }else if(tp[0].equals("java")){
		    	Class c;
		    	try{
            		c= Class.forName(tp[1]);
            	}catch(ClassNotFoundException c2){
            		throw new NDSException("class not found for "+ tp[1]); 
            	}
            	ProcessRunner pr=(ProcessRunner)c.newInstance();
            	finalFile=pr.execute(this.getAD_PInstance_ID());
		    }
		    
		    conn.commit();
		    //System.out.print("is pdf");
//		  	if cxtab has pre_procedure and ad_pi_column_id set, then will try eraise report data in fact table
		    // this task is asynchronized
		    if(nds.util.Validator.isNotNull(preProcedure) && piColumnId!=-1){
		    	ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
		    	DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
		    	event.setParameter("operatorid", String.valueOf(this.getAD_User_ID()));
		    	event.setParameter("command", "RemoveCxtabTmpData");
		    	event.setParameter("cxtab", cxtabName);
		    	event.setParameter("ad_pi_id", String.valueOf(this.getAD_PInstance_ID()));
		    	controller.handleEventBackground(event);
		    }
		    
		    if( isBackground){
		    	// this is asynchronous task, so should notifiy user (creator) of the completion
		    	notifyOwner("["+cxtabName+"]@report-created@"+((System.currentTimeMillis() - startTime)/1000),
		    			"@click-attach-url-to-download@:"+ filter,
		    			"/servlets/binserv/GetFile?filename="+ finalFile);
		    			//+",@file-name@:"+finalFile,"/servlets/binserv/GetFile?filename="+ finalFile);
		    }
		    this.addLog("File created:" +finalFile); 
		    return "File created:" +finalFile;
		    
			
	    }catch(Throwable e){
		 	log.error("", e);
		 	if( isBackground){
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
