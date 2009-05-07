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
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;
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
    	Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
        String spName = (String)event.getParameterValue("spName");
        if(nds.util.Validator.isNull(spName)){
        	spName=(String)event.getParameterValue("command");
        }
        String tableName=null;
        Table table=null;
        if(nds.util.Validator.isNotNull(spName)){ 
        	tableName=spName.substring(0,spName.indexOf("Submit") ) ;
        }else{
        	Object to=event.getParameterValue("table");
        	if(to!=null){
        		logger.debug("table="+ to);
        		table= TableManager.getInstance().getTable(to.toString());
        		if(table!=null) tableName= table.getName();
        		else{
        			table=TableManager.getInstance().getTable(Tools.getInt(to, -1));
        			if(table!=null) tableName= table.getName();
        		}
        		
        	}else{
        		throw new NDSException("Could not fetch table information");
        	}
        	
        }
        logger.debug("table name="+ tableName +", table="+ table);
        String origTableName= tableName;
        if(table==null) table=TableManager.getInstance().getTable(tableName);
        
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
        
        int status = engine.getSheetStatus(tableName,pid.intValue() );
        if(status!=1){
            throw new NDSEventException("@object-already-submitted@" );
        }
        // addtional check, is the object in the status of the table limited to.
        if(!helper.isObjectInTable(table, pid)){
        	//object no longer in table , so abort
        	throw new NDSEventException("@object-status-error-may-submitted-or-deleted@" );
        }
        User user=helper.getOperator(event);
        int userId=user.getId().intValue()  ;
        
        // security check
        boolean b=false;
        try{
        	b=SecurityUtils.hasObjectPermission(userId, user.getName(), origTableName, 
        			pid.intValue(),Directory.SUBMIT, event.getQuerySession());
        }catch(Exception e){
            throw new NDSEventException(e.getMessage() );
        }
        if (!b) throw new NDSEventException("@no-permission@!" );
        
        Vector sqls= new Vector();
        sqls.addElement("update "+tableName+" set modifierid="+ userId+ ", modifieddate=sysdate where id="+pid);
        try{
            engine.doUpdate(sqls);
        }catch(Exception e){
            throw new NDSEventException(e.getMessage() );
        }
        ValueHolder v = null;
        String state=null;
        SPResult s=null;
        try{

            s= helper.auditOrSubmitObject(table, pid.intValue(), userId, event);
        }catch(Exception e){
        	     	
            if( e instanceof NDSException) throw (NDSException)e;
        	else throw new NDSEventException(e.getMessage(), e );
        }
        if(s.getCode()!=0){
        	throw new NDSEventException(s.getMessage());
        }
        v=new ValueHolder();
        
        // will print now? a parameter decide that: printAfterSubmit( "Y" | "N" )
        // and if print, will generate a file and return that file name to client in parameter "printfile"
        try{
	        boolean bPrintAfterSubmit=Tools.getYesNo( event.getParameterValue("printAfterSubmit"), false);
	        if(bPrintAfterSubmit){
		    	DefaultWebEvent dwe= (DefaultWebEvent)event.clone();
		    	dwe.setParameter("command", "PrintJasper" );
		    	org.json.JSONObject jo= new org.json.JSONObject();
		    	
		    	org.json.JSONObject params=new org.json.JSONObject();
		    	params.put("table", table.getId());
		    	params.put("id", pid.intValue());
		    	
		    	jo.put("params", params);
		    	jo.put("org.directwebremoting.WebContext", 
		    			event.getParameterValue("org.directwebremoting.WebContext"));
		    	dwe.put("JSONOBJECT",jo);
		    	
		    	ValueHolder vh2=helper.handleEvent(dwe);
		    	if(Tools.getInt(vh2.get("code"), -1) !=0){
		    		throw new NDSEventException((String)vh2.get("message") );
		    	}
		    	org.json.JSONObject jo2= new org.json.JSONObject();
		    	jo2.put("printfile", ( (JSONObject)vh2.get("data")).optString("printfile") );
		    	v.put("data", jo2);
	
	        }
        }catch(org.json.JSONException e){
        	throw new NDSException(e.getLocalizedMessage(),e);
        }
        v.put("code", String.valueOf(s.getCode()));
        v.put("message",s.getMessage() ) ;
        v.put("next-screen", "/html/nds/info.jsp");
        
        return v;

    }
}