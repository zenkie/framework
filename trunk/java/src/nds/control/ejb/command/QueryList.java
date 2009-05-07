package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

import org.directwebremoting.WebContext;
import org.json.*;

import javax.servlet.http.HttpServletRequest;
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

public class QueryList  extends Command {

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
        String sql="";
        ResultSet rs=null;
    //    ResultSet js=null;
   		try{
	   		params=jo.getJSONObject("params");
	   		String str=params.getString("query");
	   		str=str.replace(',', '\n');
	   		String [] str1=str.split("\n");
	   		System.out.println(str1.length);
	   		sql="select docno,flowno,state,position,cash,contactorfrom,contactorto,targetdate,recivetdate,descfrom from l_order where docno=?";
	   		for (int i=1;i<str1.length;i++){
	   	      sql=sql+" or docno=?";
	   		}
	   		pstmt=conn.prepareStatement(sql);
	   		for (int j=0; j<str1.length;j++){
	   			pstmt.setString(j+1, str1[j]);
	   		}
	   		rs=pstmt.executeQuery();
	     	while(rs.next()){
	     		
	  		System.out.println(rs.getString(1));
	  		System.out.println(rs.getString(2));
	   		System.out.println(rs.getString(3));
	   		System.out.println(rs.getString(4));
	   		System.out.println(rs.getString(5));
	   		System.out.println(rs.getString(6));
	   		System.out.println(rs.getString(7));
	   		System.out.println(rs.getString(8));
	   		System.out.println(rs.getString(9));
	   		System.out.println(rs.getString(10));
	   	}
	   		
   		}catch(Throwable t){
   	  		if(t instanceof NDSException) throw (NDSException)t;
   	  		logger.error("exception",t);
   	  		throw new NDSException(t.getMessage(), t);
   	  	}finally{
         //   if( rs !=null){try{ rs.close();}catch(Exception e){}}
   	        try{pstmt.close();}catch(Exception ea){}
   	        try{conn.close();}catch(Exception e){}
   	  	} 
   	  	ValueHolder holder= new ValueHolder();
   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
   		holder.put("code","0");
   		return holder;
   	  }

}
