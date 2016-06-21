package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;
import nds.schema.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.util.Configurations;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.control.util.*;
import nds.security.*;
import nds.util.Tools;
import org.json.*;

/**
 * Delete files in folder of user for specific ad_cxtab, this is file is started with
 * "CXR_"+ ad_cxtab.id
 * 
 * 
 */
public class DeleteCxtabFiles  extends Command{
	class TableFilter implements FileFilter{
		private String prefix;
		public TableFilter(String pre){
			prefix=pre;
		}
		public boolean accept(File pathname){
    		return pathname.getName().startsWith( prefix);
    	}
	}

    /**
     * @param event - special parameters:
     *  files - filenames
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
		User usr =helper.getOperator(event);
		int clientId= usr.adClientId;
		int orgId= usr.adOrgId;
		int uId= usr.id.intValue();

	    
	    Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);	    
	    Boolean byuserid= nds.util.Tools.getBoolean(conf.getProperty("report_savepathbyuserid","false"),false);

	    String svrPath = conf.getProperty("export.root.nds","/aic/home")
	    	+ File.separator + usr.getClientDomain()+File.separator+ (byuserid?usr.getId():usr.getName());
	    try{
		    int cxtabId=Tools.getInt( event.getParameterValue("cxtabid",true),-1);
		    
		    String msg = "";
		    nds.control.util.ValueHolder vh = new nds.control.util.ValueHolder();
		    JSONObject jo=new JSONObject();
		    
	    	TableFilter tf=new TableFilter("CXR_"+cxtabId+"_");//new TableFilter("CXR_"+cxtabId);
	    	File dir = new File(svrPath);
	    	File[] files = dir.listFiles(tf);
	    	
		    
		    for(int i = 0;i < files.length;i++){
	            File file = files[i];
	            File desc = new File(svrPath+File.separator+"desc"+File.separator+file.getName());
	            if(file.delete()){
	                desc.delete();
	            }
		    }
			msg= MessagesHolder.getInstance().translateMessage("complete", event.getLocale());
		    jo.put("message", msg);
			ValueHolder v = new ValueHolder();
	        v.put("data", jo ) ;
	        return v;
	    }catch(Throwable e){
		 	logger.error("", e);
		 	if(!(e instanceof NDSException ))throw new NDSEventException("Òì³£", e);
		 	else throw (NDSException)e;
	    }	  	
    }
}