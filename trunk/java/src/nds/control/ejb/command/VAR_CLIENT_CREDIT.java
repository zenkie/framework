package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;

import org.directwebremoting.WebContext;
import org.json.JSONObject;

public class VAR_CLIENT_CREDIT extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
		
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null;
	  	java.util.Locale locale= event.getLocale();
        String orderno="";
        int e_orderId=-1;
        Connection conn= QueryEngine.getInstance().getConnection();
   	  	try{
 	  		params=jo.getJSONObject("params");
   		    orderno=params.getString("orderno");
   		    User usr=helper.getOperator(event);
   		    e_orderId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from e_order where docno='"+orderno+"'"),-1);
   		    // 再做一次余额校验，并设置单据为授信支付
   			ArrayList params1=new ArrayList();
   			params1.add(orderno);
			QueryEngine.getInstance().executeStoredProcedure("E_ORDER_CHECK_BALANCE", params1, false,conn);
	

   		    params1=new ArrayList();
   			params1.add(e_orderId);
   			params1.add(usr.getId());
   			params1.add("");
   			params1.add("");
			QueryEngine.getInstance().executeStoredProcedure("E_ORDERSUBMIT", params1, false,conn);
	   	  	ValueHolder holder= new ValueHolder();
	   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
	   		holder.put("code","0");
			JSONObject returnObj = new JSONObject();
			returnObj.put("url", "/var/var_paycreditsuccess.jsp?id=" + e_orderId);
			holder.put("data", returnObj);
	   		return holder;
		} catch (Throwable th) {
				if(th instanceof NDSException) throw (NDSException)th;
		  		throw new NDSException(th.getMessage(), th);
		}
   	  }
   

}
