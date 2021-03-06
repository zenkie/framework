package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AuditUtils;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.monitor.MonitorManager;
import nds.monitor.ObjectActionEvent;
import nds.monitor.ObjectActionEvent.ActionType;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.query.SPResult;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;

import java.sql.*;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

public class ObjectSubmit extends Command{
    public ObjectSubmit() {

    }
    /**
     * 在提交前保存提交人到修改人, 提交动作需要确认对象仍然在当前指定状态（在屏幕上点击两次将使得提交的单据走了两个节点）
     * 先保存后提交 ("nds.savebeforesubmit"="Y") 用户有保存权限，且用户在提交前保存的设置界面上。
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
        String spName = (String)event.getParameterValue("spName");
        if(nds.util.Validator.isNull(spName)){
        	spName=(String)event.getParameterValue("command");
        }
        String tableName=null;
        Table table=null;
        if(nds.util.Validator.isNotNull(spName) && !"ObjectSubmit".equals(spName)){ 
        	tableName=spName.substring(0,spName.indexOf("Submit") ) ;
        }else{
        	table = TableManager.getInstance().findTable(event.getParameterValue("table"));
        }
        logger.debug("table name="+ tableName +", table="+ table);
        String origTableName= tableName;
        if(table==null) {table=TableManager.getInstance().findTable(tableName);}else{
        	tableName = table.getName();}
        if(nds.util.Validator.isNotNull(tableName)){
        	tableName= table.getName();
        	origTableName= tableName;	
        } 
        logger.debug("table name="+ tableName +", origtable="+ origTableName);
        User usr= helper.getOperator(event);	
        int pid =event.getObjectId(table, usr.adClientId);
        //Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
        //先保存后提交 since 2008-1-9
        if(Tools.getYesNo((String) event.getParameterValue("nds.savebeforesubmit"),false)){
	    	DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
	    	dwe.setParameter("command", table.getName()+"Modify" );
	    	ValueHolder vh2=helper.handleEvent(dwe);
	    	if(Tools.getInt(vh2.get("code"), -1) !=0){
	    		throw new NDSEventException((String)vh2.get("message") );
	    	}
        }
    	
        tableName=table.getRealTableName();
        QueryEngine engine = QueryEngine.getInstance() ;
        Connection conn=null;
        ValueHolder v = null;
        User user=helper.getOperator(event);
        int userId=user.getId().intValue()  ;
        conn = this.helper.getConnection(event);
        try{
        QueryUtils.lockRecord(table, pid, conn);
        
        int status = engine.getSheetStatus(tableName,pid,conn );
        if(status!=1){
            throw new NDSEventException("@object-already-submitted@" );
        }
        // addtional check, is the object in the status of the table limited to.
        if(!helper.isObjectInTable(table, pid,conn)){
        	//object no longer in table , so abort
        	throw new NDSEventException("@object-status-error-may-submitted-or-deleted@" );
        }

        
        // security check
        boolean b=false;
        try{
        	b=SecurityUtils.hasObjectPermission(userId, user.getName(), origTableName, 
        			pid,Directory.SUBMIT, event.getQuerySession());
        }catch(Exception e){
        	//e.printStackTrace();
            throw new NDSEventException(e.getMessage() );
        }
        if (!b) throw new NDSEventException("@no-permission@!" );
        
        String state=null;
        SPResult s=null;
        //conn=engine.getConnection();
        //try{
	        /**
	         * add check before submit, this is procedure
	           write in auditOrSubmitObject
	        String onSubmit= null;
	        if(table.getJSONProps()!=null ){
	        	onSubmit= table.getJSONProps().optString("before_submit");
	        	if(Validator.isNotNull(onSubmit)){
	    			ArrayList params=new ArrayList();
	    			params.add(new Integer(pid));
	    			engine.executeStoredProcedure(onSubmit, params, false ,conn);
	        	}
	        }
	        */
	        Vector sqls= new Vector();
	        sqls.addElement("update "+tableName+" set modifierid="+ userId+ ", modifieddate=sysdate where id="+pid);
            engine.doUpdate(sqls,conn);
            
            s= helper.auditOrSubmitObject(table, pid, userId, event, conn);
	        //if(s.getCode()!=0)
            /**
             * 修改submit 方法支持rcode 返回
             * 101 刷新不关闭
             */
            if(s.getCode()!=0 && s.getCode()!=101 )
	        	throw new NDSEventException(s.getMessage());
        
	        v=new ValueHolder();
        
	        
			if ("check_submit".equals(s.getTag())) {
				JSONObject jop = new JSONObject();
				jop.put("check_submit",s.getMessage());
				v.put("data", jop);
				v.put("code",String.valueOf(s.getCode()));
				v.put("message",s.getMessage());
				return v;
			}
			
			if ("submitted".equals(s.getTag())) {
     		   //monitor plugin
     		   JSONObject cxt=new JSONObject();
     		   cxt.put("source", this);
     		   cxt.put("connection", conn);
     		   cxt.put("statemachine", this.helper.getStateMachine());
     		   cxt.put("javax.servlet.http.HttpServletRequest", 
     		   event.getParameterValue("javax.servlet.http.HttpServletRequest", true));
     		   ObjectActionEvent oae=new ObjectActionEvent(table.getId(),
     				  pid, usr.adClientId,ActionType.SUBMIT, usr, cxt);
     		   MonitorManager.getInstance().dispatchEvent(oae);
			}
	        
	        // will print now? a parameter decide that: printAfterSubmit( "Y" | "N" )
	        // and if print, will generate a file and return that file name to client in parameter "printfile"
	        boolean bPrintAfterSubmit=Tools.getYesNo( event.getParameterValue("printAfterSubmit"), false);
	        if(bPrintAfterSubmit){
		    	DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
		    	dwe.setParameter("command", "PrintJasper" );
		    	org.json.JSONObject jo= new org.json.JSONObject();
		    	
		    	org.json.JSONObject params=new org.json.JSONObject();
		    	params.put("table", table.getId());
		    	params.put("id", pid);
		    	
		    	jo.put("params", params);
		    	jo.put("org.directwebremoting.WebContext", 
		    			event.getParameterValue("org.directwebremoting.WebContext"));
		    	dwe.put("JSONOBJECT",jo);
		    	
		    	ValueHolder vh2=helper.handleEvent(dwe);
	            /**
	             * 修改 方法支持rcode 返回
	             * 101 刷新不关闭
	             */
		    	if(Tools.getInt(vh2.get("code"), -1) !=0 && Tools.getInt(vh2.get("code"), -1) !=101){
		    		throw new NDSEventException((String)vh2.get("message") );
		    	}
		    	org.json.JSONObject jo2= new org.json.JSONObject();
		    	jo2.put("printfile", ( (JSONObject)vh2.get("data")).optString("printfile") );
		    	v.put("data", jo2);
	
	        }
            logger.debug("submit code is"+String.valueOf(s.getCode()));
			if ("check_submit".equals(s.getTag())) {
				JSONObject jop = new JSONObject();
				jop.put("check_submit",s.getMessage());
				 v.put("data", jop);
			}
	        v.put("code", String.valueOf(s.getCode()));
	        v.put("message",s.getMessage() ) ;
	        v.put("next-screen", "/html/nds/info.jsp");
	        /**
	         * ajax 支持、sbresult
	         */
	        if(s.getCode()==101){
	        SPResult sbr=new SPResult(s.getCode(),s.getMessage());
	        v.put("sbresult", sbr);
	        logger.debug(sbr.toJSONString());
	        }
	        return v;
        }catch(Exception e){
            if( e instanceof NDSException) throw (NDSException)e;
        	else throw new NDSEventException(e.getMessage(), e );
	    }finally{
	    	if(conn!=null)try{conn.close();}catch(Throwable tx){}
	    }

    }
}