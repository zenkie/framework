package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.control.web.ServletContextManager;
import nds.control.web.WebUtils;
import nds.control.web.reqhandler.UploadKeyHandler;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.SQLTypes;
import nds.security.User;
import nds.util.Configurations;
import nds.util.JSONUtils;
import nds.util.License;
import nds.util.LicenseMake;
import nds.util.LicenseWrapper;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.WebKeys;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.LicenseManager;

public class CheckmacKey extends Command {
	/**
	 * @param event contains 
	 *  check mackey is vaild
	 *  
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	  	
	 
	  	MessagesHolder mh= MessagesHolder.getInstance();
	  	boolean vailed=false;
	  	String  checmak="null";
	  	String mac=null;
		Object sc=null;
	    int user_num=0;
	    int pos_num=0;
	    String company=null;
	    String expdate=null;
	  	try{
		   
		    ServletContextManager scm= WebUtils.getServletContextManager();
		    
		    LicenseMake licmark=(LicenseMake)scm.getActor(nds.util.WebKeys.LIC_MANAGER);
		    
		    licmark.validateLicense(nds.util.WebKeys.getPrdname(),"5.0","");
		    vailed=true;
		    Iterator b=licmark.getLicenses();
		    
		    while (b.hasNext()) {
		    	LicenseWrapper o = (LicenseWrapper)b.next();
		    	user_num=o.getNumUsers();
		    	pos_num=o.getNumPOS();
		    	company=o.getName();
		    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		    	expdate = df.format(o.getExpiresDate());
		    }
		    
		   } catch (Exception e) {
			   logger.debug("check mackey invaild",e);
		   }
	  	
		    if(vailed){
		    	checmak="/html/prg/regSuccess.jsp?cp="+company+"&user="+user_num+"&pos="+pos_num+"&exp="+expdate;
		    }else{
		    	checmak="/html/prg/regFail.jsp";
		    }


		    JSONObject returnObj = new JSONObject();
			try {
				returnObj.put("url", checmak);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  		ValueHolder holder= new ValueHolder();
			holder.put("message", mh.getMessage(event.getLocale(), "complete"));
			holder.put("code","0");
			holder.put("data", returnObj);
		  	return holder;
	  	
	  }
}
