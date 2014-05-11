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
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
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
	  	Connection conn=null;
	  	boolean vailed=false;
	  	String  checmak="null";
	  	String mac=null;
		Object sc=null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
	    int user_num=0;
	    int pos_num=0;
	    String company=null;
	    String expdate=null;
	  	try{
		   
	  		conn= nds.query.QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement("select mac from users where id=?");
			pstmt.setInt(1, 893);
			rs= pstmt.executeQuery();
			if(rs.next()){
				sc=rs.getObject(1);
				if(sc instanceof java.sql.Clob) {
					mac=((java.sql.Clob)sc).getSubString(1, (int) ((java.sql.Clob)sc).length());
	        	}else{
	        		mac=(String)sc;
	        	}	
			}
		
		  	try{
		  	// logger.debug("upload keyfile is"+mac);
		  	LicenseManager.validateLicense(WebKeys.PRDNAME,"5.0", mac,true);
		    vailed=true;
		    Iterator b=LicenseManager.getLicenses();
		    
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
			returnObj.put("url", checmak);
	  		ValueHolder holder= new ValueHolder();
			holder.put("message", mh.getMessage(event.getLocale(), "complete"));
			holder.put("code","0");
			holder.put("data", returnObj);
		  	return holder;
	  	}catch(Throwable t){
	  		logger.error("exception",t);
	  		if(t instanceof NDSException) throw (NDSException)t;
	  		else
	  			throw new NDSException(t.getMessage(), t);
	  	}finally{
			try{if(rs!=null) rs.close();}catch(Throwable t){}
			try{if(pstmt!=null) pstmt.close();}catch(Throwable t){}
			try{if(conn!=null) conn.close();}catch(Throwable t){}
	  	}
	  }
}
