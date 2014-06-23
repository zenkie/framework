package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;
import java.sql.*;

import nds.control.ejb.Command;
import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.web.*;
import nds.control.util.*;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;
/**
. 
 	  
*/
public class RegistrateUser  extends Command{
	private String  defaultRootId;  
	public  RegistrateUser(){
		defaultRootId=( EJBUtils.getApplicationConfigurations().getProperty("default.root.id","0"));
		
	}
	/*
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}
*/
    /**
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
    	ValueHolder vh=null;
    	try{
	    	long beginTime= System.currentTimeMillis();
	        logger.debug(event.toDetailString());
	
	        if(Tools.getYesNo(event.getParameterValue("vaildcode", false), false)){
	        	throw new NDSEventException("@error-verify-code@");
	        }
	        // call ObjectCreate event to insert data to db
	        event.setParameter("command","ObjectCreate");
	        event.setParameter("directory","WX_REGUSER_LIST");
	        event.setParameter("operatorid", defaultRootId);
	        String wxappid=(String) event.getParameterValue("WXAPPID");
	        logger.debug("WXAPPID ->"+wxappid);
	        helper.getOperator(event);
	        vh = helper.handleEvent(event);
	
	        if(Tools.getInt( vh.get("code"), 0) != 0 ){
	        	throw new NDSException((String)vh.get("message"));
	        }
	        
	        vh.put("message", "@registrate-success@");
	        vh.put("next-screen", "/register.jsp");
	        if(wxappid!=null){
	        	String domin=String.valueOf(QueryEngine.getInstance().doQueryOne("select t.domain from web_client t where t.wxnum='"+wxappid+"'"));
	        	WeUtilsManager Wemanage =WeUtilsManager.getInstance();
				Wemanage.getAdClientTemplateFolder(domin);
	        }
	        return vh;
    	}catch(Throwable t){
    		logger.error("Error", t);
    		vh=new ValueHolder();
			vh.put("code", "-1");
			vh.put("message",	WebUtils.getExceptionMessage(t, TableManager.getInstance().getDefaultLocale()));
    		try{
    			vh.put("next-screen", "/register.jsp?err="+ java.net.URLEncoder.encode(	WebUtils.getExceptionMessage(t, TableManager.getInstance().getDefaultLocale()), "UTF-8") );
    		}catch(Throwable t2){
    			
    		}
    	}
    	return vh;
    }
   
}