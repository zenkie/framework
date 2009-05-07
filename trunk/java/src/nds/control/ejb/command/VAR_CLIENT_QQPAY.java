package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;

import org.json.JSONObject;

public class VAR_CLIENT_QQPAY extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
		
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null;
	  	java.util.Locale locale= event.getLocale();
        String orderno="";
   	    Connection conn= QueryEngine.getInstance().getConnection();
 	    PreparedStatement pstmt=null;
   	  	try{
 	  		params=jo.getJSONObject("params");
   		    orderno=params.getString("orderno");
   		     pstmt= conn.prepareStatement("update e_order set state='W' where docno=?");
   		     pstmt.setString(1,orderno);	
   		     pstmt.executeUpdate();
   		   User usr=helper.getOperator(event);
   		   int e_orderId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from e_order where docno='"+orderno+"'"),-1);
			ArrayList params1=new ArrayList();
   			params1.add(e_orderId);
   			params1.add(usr.getId());
   			params1.add("");
   			params1.add("");
				QueryEngine.getInstance().executeStoredProcedure("E_ORDERSUBMIT", params1, false,conn);
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
