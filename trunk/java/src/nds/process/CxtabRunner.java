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
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.cxtab.CxtabReport;
import nds.cxtab.EmptyRecordException;
import nds.schema.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;

/**
 * Create cxtab report 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class CxtabRunner extends SvrProcess
{
	private String filterExpr;
	private String filterSQL;
	private String cxtabName;
	private String fileName;
	private String fileType;
	private QueryEngine engine;
	private String filter;
	private String folder;
	private static HashSet<String> prclist;
	
	private static Logger logger= LoggerManager.getInstance().getLogger((CxtabRunner.class.getName()));

	public static void updateNoDeleteProcedures(String prcname) {
		prclist.clear();
		if (com.liferay.util.Validator.isNotNull(prcname)) {
			String[] b = prcname.split(",");
			for (int i = 0; i < b.length; i++)
				prclist.add(b[i]);
		}
	}
	
	public boolean internalTransaction()
	{
		return true;
	}
	/**
	 *  Parameters:
	 *    filter_expr, filter_sql (if set, will take privilege over filter_expr), 
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
			else if (name.equals("filter_sql"))
				filterSQL= ((String)para[i].getInfo());
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
	 * Return preprocess instance id if found preprocess, this process will be put in the same same with same recordno of
	 * parent process
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private int createPreProcessInstance(Connection conn) throws Exception{
		ArrayList list =new ArrayList();
        list.add(new Integer(this.getAD_PInstance_ID()));
        ArrayList res=new ArrayList();
        res.add(Integer.class);
        Collection result=QueryEngine.getInstance().executeFunction("ad_cxtab_create_prepi", list, res, conn );
        // sp returned value contains create pinstance id
        int pId=Tools.getInt(result.toArray()[0],-1);
		return pId;
	}
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
		boolean iscus = "cus".equalsIgnoreCase(this.fileType);
		
	    try{
		    conn= engine.getReadConnection();
		    // check PRE_PROCEDURE for cxtab
		    if (iscus) conn.setAutoCommit(false);
		    
		    List cxInfo=(List) engine.doQueryList(
		    		"select PRE_PROCEDURE, AD_PI_COLUMN_ID from ad_cxtab where ad_client_id="+ this.getAD_Client_ID()+
		    		" and name="+ QueryUtils.TO_STRING(cxtabName), conn);
		    String preProcedure=(String) ((List)cxInfo.get(0)).get(0);
		    int piColumnId= Tools.getInt( ((List)cxInfo.get(0)).get(1), -1);
		    
		    if( nds.util.Validator.isNotNull(preProcedure)&&(!"cub".equalsIgnoreCase(this.fileType))){
		    	ArrayList al=new ArrayList();
		    	al.add(new Integer(this.getAD_PInstance_ID()));
		    	engine.executeStoredProcedure(preProcedure, al, false,conn);
		    	
		    }
		    /*int preProcessInstanceId= createPreProcessInstance(conn);
		    if(preProcessInstanceId!=-1){
		    	ValueHolder holder=ProcessUtils.executeImmediateADProcessInstance(preProcessInstanceId , this.getAD_User_ID(), false);
	    	  	if(!holder.isOK()){
	    	  		throw new NDSException((String)holder.get("message"));
	    	  	}
		    }*/
			log.debug("filterExpr="+ filterExpr);
			log.debug("filterSQL="+ filterSQL);
			isBackground=Tools.getYesNo( engine.doQueryOne("select c.isbackground from ad_cxtab c where c.ad_client_id="+ this.getAD_Client_ID() +" and c.name="+ QueryUtils.TO_STRING(cxtabName),conn), false);
		    // create group query
		    nds.cxtab.CxtabReport cr=new nds.cxtab.CxtabReport();
		    cr.setCxtabName(cxtabName);
		    cr.setFileName(fileName);
		    cr.setFilterSQL(filterSQL);
		    cr.setFilterExpr(filterExpr);
		    cr.setFileType(fileType);
		    cr.setFilterDesc(filter);
		    cr.setFolder(folder);
		    cr.setUserId( this.getAD_User_ID());
		    //cr.setReportInstanceId(preProcessInstanceId); // this may be used as specical filter for sql statement
		    cr.setAD_PInstance_ID(this.getAD_PInstance_ID()); // this will be needed when doing cube exporting
		    String finalFile;
		    try{  
		    finalFile= cr.create(conn);
		    if( isBackground){
		    	// this is asynchronous task, so should notifiy user (creator) of the completion
		    	notifyOwner("["+cxtabName+"]@report-created@"+((System.currentTimeMillis() - startTime)/1000),
		    			"@click-attach-url-to-download@:"+ filter,
		    			"/servlets/binserv/GetFile?filename="+ finalFile);
		    			//+",@file-name@:"+finalFile,"/servlets/binserv/GetFile?filename="+ finalFile);
		    }
		    this.addLog("File created:" +finalFile);
		    finalFile = "File created:" + finalFile;
		    logger.debug(finalFile);
			} catch (EmptyRecordException e) {
				if (isBackground) {
					notifyOwner("[" + cxtabName + "]@empty-record@",
							"@empty-record@", null);
				}
				addLog("empty record");
				finalFile = "empty record";
			}
		    if (iscus) conn.commit();logger.debug("is cus");
		    // if cxtab has pre_procedure and ad_pi_column_id set, then will try eraise report data in fact table
		    // this task is asynchronized
		    logger.debug("preProcedure is "+preProcedure+" piColumnId is "+String.valueOf(piColumnId));
		    if(nds.util.Validator.isNotNull(preProcedure) && piColumnId!=-1&&(!prclist.contains(preProcedure.toUpperCase()))){
		    	ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
		    	DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
		    	event.setParameter("operatorid", String.valueOf(this.getAD_User_ID()));
		    	event.setParameter("command", "RemoveCxtabTmpData");
		    	event.setParameter("cxtab", cxtabName);
		    	event.setParameter("ad_pi_id", String.valueOf(this.getAD_PInstance_ID()));
		    	controller.handleEventBackground(event);
		    }		    
		    
		    return finalFile;
			
	    }catch(Throwable e){
	         try {
		   if (iscus)
					conn.rollback();
			} catch (Throwable e2) {
				this.log.error("fail to rollback conn:" + e2.getMessage());
			}
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

	
	static {
		if ((CxtabRunner.prclist = null) == null) {
			prclist = new HashSet();
			String localObject = WebUtils.getConfigurations().getProperty(
					"cxtab.preprocedure.nodelete", "");
			updateNoDeleteProcedures(localObject);
		}
	}
	
}
