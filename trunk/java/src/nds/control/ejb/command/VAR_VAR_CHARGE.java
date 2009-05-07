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
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.Tools;

import org.directwebremoting.WebContext;
import org.json.JSONObject;

public class VAR_VAR_CHARGE extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
		
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	JSONObject params=null,params2=null;
	  	java.util.Locale locale= event.getLocale();
  	    QueryEngine engine=QueryEngine.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
        Boolean flag=false;
        HttpServletRequest request=null;
        String email="";
        String docno="";
		User usr = helper.getOperator(event);
		int userid = usr.getId();
        
        try {
	   		params2=jo.getJSONObject("params");
			String userValidCode= params2.getString("verifyCode");
			WebContext ctx=(WebContext) jo.opt("org.directwebremoting.WebContext");
	  		if(ctx!=null){
		  	request = ctx.getHttpServletRequest();
			String serverValidCode=(String)request .getSession().getAttribute("nds.control.web.ValidateMServlet");
	    	if(serverValidCode.equalsIgnoreCase(userValidCode)){
	    		   
	    	}else{
	    		throw new NDSEventException("@error-verify-code@");
	    	}
	  		}
        }catch(Throwable th){
		      if(th instanceof NDSException) throw (NDSException)th;
		       logger.error("exception",th);
		       throw new NDSException(th.getMessage(), th);
		}
	  		
	  	try{
	  		params=jo.getJSONObject("params");
	  		int e_orderid=QueryEngine.getInstance().getSequence("e_order", conn);
	  		int clientid=Tools.getInt(params.getString("clientid"),-1);
	  		List emaillist= QueryEngine.getInstance().doQueryList("select id,email,ad_client_id,ad_org_id from c_bpartner where id="+clientid); 
	  		email=(String)((List)emaillist.get(0)).get(1);
	  		List c_bpartnerlist=QueryEngine.getInstance().doQueryList("select name,email,mobile,contactor from c_bpartner where id="+clientid);
	   		pstmt= conn.prepareStatement("insert into e_order(id,ad_client_id, ad_org_id,docno,doctype,"+
	   				"amt,c_bpartner_id,state,creationdate,payer_id,pname,pemail,pmobile,ptruename, ownerid, modifierid, modifieddate,isactive) "+
	   				"values(?,?,?,Get_SequenceNo('VAR',?),'V',?,?,'N',sysdate,?,?,?,?,?,?,?,sysdate,'Y')");
	   		pstmt.setInt(1,e_orderid);
	   		pstmt.setInt(2,Tools.getInt(((List)emaillist.get(0)).get(2),-1));
	   		pstmt.setInt(3,Tools.getInt(((List)emaillist.get(0)).get(3),-1));
	   		
	   		pstmt.setInt(4, Tools.getInt(((List)emaillist.get(0)).get(2),-1)); // docno. client
	   		pstmt.setInt(5,Tools.getInt(params.getString("amt"),-1));
		    pstmt.setInt(6,clientid);
		    pstmt.setInt(7,clientid);
		    pstmt.setString(8,(String)((List)c_bpartnerlist.get(0)).get(0));
		    pstmt.setString(9,(String)((List)c_bpartnerlist.get(0)).get(1));
		    pstmt.setString(10,(String)((List)c_bpartnerlist.get(0)).get(2));
		    pstmt.setString(11,(String)((List)c_bpartnerlist.get(0)).get(3));
			pstmt.setInt(12,userid);
			pstmt.setInt(13,userid);
		    pstmt.executeUpdate();
		 
		    docno=(String)QueryEngine.getInstance().doQueryOne("select docno from e_order where id="+e_orderid);
		/*    ArrayList params1=new ArrayList();
   			params1.add(docno);
   			System.out.print(docno);
			QueryEngine.getInstance().executeStoredProcedure("E_ORDER_CHECK_BALANCE", params1, false,conn);
		*/
	   	  	ValueHolder holder= new ValueHolder();
	   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
	   		holder.put("code","0");
			JSONObject returnObj = new JSONObject();
			returnObj.put("url", "/var/order_pay.jsp?id=" + e_orderid+ "&isvar=true" );
			holder.put("data", returnObj);
	   		return holder;

	  	}catch(Throwable t){
   		    if(t instanceof NDSException) throw (NDSException)t;
	          logger.error("exception",t);
	        throw new NDSException(t.getMessage(), t);
   	  	}finally{
   	        try{pstmt.close();}catch(Exception ea){}
   	        try{conn.close();}catch(Exception e){}
   	  	} 
   	  }
   

}
