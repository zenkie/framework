package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import org.directwebremoting.WebContext;
import org.json.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import java.util.*;

import nds.security.User;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

import JOscarLib.Request.Request;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import nds.control.web.*;
import nds.velocity.*;

/**
 * For Yunbao
 * @author yfzhu
 *
 */
public class FeedBack extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
		
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null;
	  	java.util.Locale locale= event.getLocale();
  	    QueryEngine engine=QueryEngine.getInstance();
        boolean hasError=false;
        MessagesHolder mh= MessagesHolder.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
   		try{
	   		params=jo.getJSONObject("params");
            pstmt= conn.prepareStatement("insert into feedback(id,ad_client_id,subject,content,companyname,address,postcode,linkman,tel,phonenum,fax,email,creationdate,modifieddate) values( get_sequences('feedback'),?,?,?,?,?,?,?,?,?,?,?,sysdate,sysdate)");
            pstmt.setInt(1,params.getInt("clientId"));
            pstmt.setString(2,params.getString("subject"));		      
		    pstmt.setString(3,params.getString("content"));
		    pstmt.setString(4,params.getString("companyname"));
		    pstmt.setString(5,params.getString("address"));
		    pstmt.setString(6,params.getString("postcode"));
		    pstmt.setString(7,params.getString("linkman"));
		    pstmt.setString(8,params.getString("tel"));
		    pstmt.setString(9,params.getString("phonenum"));
		    pstmt.setString(10,params.getString("fax"));
		    pstmt.setString(11,params.getString("email"));
		    pstmt.executeUpdate();
   		}catch(Throwable t){
   	  		if(t instanceof NDSException) throw (NDSException)t;
   	  		logger.error("exception",t);
   	  		throw new NDSException(t.getMessage(), t);
   	  	}finally{
   	        try{pstmt.close();}catch(Exception ea){}
   	        try{conn.close();}catch(Exception e){}
   	  	} 
   	  	ValueHolder holder= new ValueHolder();
   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
   		holder.put("code","0");
   		return holder;
   	  }
   

}
