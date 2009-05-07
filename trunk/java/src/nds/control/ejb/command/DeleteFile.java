/******************************************************************
*
*$RCSfile: CalculatePrice.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/06/24 00:32:31 $
*

********************************************************************/
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
 * Delete file in folder of user
 * 
 * @author yfzhu@agilecontrol.com
 */
public class DeleteFile  extends Command{

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
	    String svrPath = conf.getProperty("export.root.nds","/aic/home")
	    	+ File.separator + usr.getClientDomain()+File.separator+ usr.getName();
	    try{
	    JSONArray fileNames= event.getJSONObject().getJSONArray("files");
	    
	    
	    String msg = "";
	    nds.control.util.ValueHolder vh = new nds.control.util.ValueHolder();
	    JSONObject jo=new JSONObject();
	    if(fileNames !=null){
	    for(int i = 0;i < fileNames.length();i++){
	        String filePath = fileNames.getString(i);
	        if(filePath!=null && !filePath.trim().equals("")){
	            filePath = filePath.trim();
	            File file = new File(svrPath+File.separator+filePath);

	            if(file.exists() && file.isFile()){
	                File desc = new File(svrPath+File.separator+"desc"+File.separator+filePath);
	                if(file.delete())
	                {
	                    desc.delete();
	                    msg +=  "@file@:"+file.getName()+" @deleted@<br>";
	                }else{
	                    msg += "@file@:"+file.getName()+" @fail-to-delete@<br>"; 
	                }
	            }else
	                msg +="@file@:"+file.getName()+" @not-exists@<br>" ; 
	        }//end if
	    }//end for
		msg= MessagesHolder.getInstance().translateMessage(msg, event.getLocale());

	    jo.put("message", msg);
	    }
		ValueHolder v = new ValueHolder();
        v.put("data", jo ) ;
        return v;
	    }catch(Throwable e){
		 	logger.error("", e);
		 	if(!(e instanceof NDSException ))throw new NDSEventException("“Ï≥£", e);
		 	else throw (NDSException)e;
	    }	  	
    }
}