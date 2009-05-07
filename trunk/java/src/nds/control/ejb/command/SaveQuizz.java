package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;

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

/**
 * 
 * 保存用户设置的提问和密码
 * 
 *
 */
public class SaveQuizz  extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	
	  	JSONObject params=null;
	  	java.util.Locale locale= event.getLocale();
//	  	JSONObject returnObj=new JSONObject();
	  	User usr=helper.getOperator(event);
    	int userid=usr.getId();
    	if(userid==nds.control.web.UserWebImpl.GUEST_ID) throw new NDSException("@no-permission@");
    	//helper.checkDirectoryWritePermission(event, usr);
  	    QueryEngine engine=QueryEngine.getInstance();
        boolean hasError=false;
        MessagesHolder mh= MessagesHolder.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
   		try{
	   		String question= jo.getString("question");
	   		String answer= jo.getString("answer");
   			pstmt= conn.prepareStatement("update u_user_quizz set question=?, answer=?, modifieddate=sysdate where ownerid=?");
   			pstmt.setString(1,question);
   			pstmt.setString(2,answer);
   			pstmt.setInt(3,userid);
            int cnt=pstmt.executeUpdate();
            if(cnt ==0){
            	pstmt.close();
            	pstmt= conn.prepareStatement("insert into u_user_quizz(id,ad_client_id,ownerid,question,answer) values(?,?,?,?,?)");
            	pstmt.setInt(1, userid);
            	pstmt.setInt(2,usr.adClientId);
            	pstmt.setInt(3,userid);
            	pstmt.setString(4, question);
            	pstmt.setString(5,answer);
            	pstmt.executeUpdate();
            }
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