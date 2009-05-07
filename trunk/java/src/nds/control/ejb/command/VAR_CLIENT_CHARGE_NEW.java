package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.TableManager;
import nds.util.Configurations;
import nds.util.MessagesHolder;
import nds.util.NDSException;
import nds.util.Tools;

import org.directwebremoting.WebContext;
import org.json.JSONException;
import org.json.JSONObject;
import nds.security.User;

public class VAR_CLIENT_CHARGE_NEW extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
		JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	java.util.Locale locale= event.getLocale();
  	    QueryEngine engine=QueryEngine.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;
 	    JSONObject params=null,params3=null;
        int productid;
        int c_bpartnerid;
        String email;
        //Boolean flag=false;
        //String docno="";
        int e_orderid;
        HttpServletRequest request=null;
        User usr=helper.getOperator(event);
        int userid=usr.getId();
        String state="D";
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
   			try {
				String userValidCode;
				params3=jo.getJSONObject("params");
				userValidCode = params3.getString("verifyCode");
				WebContext ctx=(WebContext) jo.opt("org.directwebremoting.WebContext");
				if(ctx!=null){
				request = ctx.getHttpServletRequest();
				String serverValidCode=(String)request.getSession().getAttribute("nds.control.web.ValidateMServlet");
				
				if(serverValidCode.equalsIgnoreCase(userValidCode)){
					   
				     }else{
					      throw new NDSEventException("@error-verify-code@");
				     }
				}
				email=params3.getString("email");
	   			ArrayList params1=new ArrayList();
	   			params1.add(email);
				QueryEngine.getInstance().executeStoredProcedure("E_ORDER_CHECK_EMAIL", params1, false,conn);
			}catch(Throwable th){
				try{conn.close();}catch(Exception e){}
	  		    if(th instanceof NDSException) throw (NDSException)th;
	  		       logger.error("exception",th);
	  		    throw new NDSException(th.getMessage(), th);
	  		    
			}
			
	  	try{
	  		params=jo.getJSONObject("params");
	  		email=params.getString("email");

	  	   	if(userid!=nds.control.web.UserWebImpl.GUEST_ID)
	    	{
	  	   	       state="M";
	    	}
				productid=Tools.getInt(params.getString("pdtid"),-1);
				if(Tools.getInt(params.getString("varid"),-1)==-1){
					   c_bpartnerid=Tools.getInt(conf.getProperty("var.default.var"),-1);
				}else{
					c_bpartnerid=Tools.getInt(params.getString("varid"),-1);
				}
				String  telphone=params.getString("area")+params.getString("phone");
				List c_bpartner= QueryEngine.getInstance().doQueryList("select ad_client_id,ad_org_id from c_bpartner where id="+c_bpartnerid);
			    e_orderid=QueryEngine.getInstance().getSequence("e_order", conn);
				pstmt= conn.prepareStatement("insert into e_order(id,ad_client_id,ad_org_id,docno,doctype,amt,c_bpartner_id,pname,pemail,parea,pphone,pmobile,ptruename,state,creationdate, ownerid, modifierid, modifieddate, isactive) values(?,?,?,Get_SequenceNo('VAR',1),?,?,?,?,?,?,?,?,?,'N',sysdate,?,?,sysdate,'Y')");
				pstmt.setInt(1,e_orderid);
				pstmt.setInt(2,Tools.getInt(((List)c_bpartner.get(0)).get(0),-1));
				pstmt.setInt(3,Tools.getInt(((List)c_bpartner.get(0)).get(1),-1));
				pstmt.setString(4,state);	
				pstmt.setString(5,params.getString("amt"));	
				pstmt.setInt(6,c_bpartnerid);
				pstmt.setString(7,params.getString("companyname"));	
				pstmt.setString(8,params.getString("email"));	
				pstmt.setString(9,params.getString("area"));	
				pstmt.setString(10,params.getString("phone"));	
				pstmt.setString(11,params.getString("mobile"));	
				pstmt.setString(12,params.getString("truename"));
				pstmt.setInt(13,userid);
				pstmt.setInt(14,userid);
				pstmt.executeUpdate();
				/*docno=(String)QueryEngine.getInstance().doQueryOne("select docno from e_order where id="+e_orderid); 
    	 	    if(userid!=nds.control.web.UserWebImpl.GUEST_ID){
	         	    int c_bpartner_id=Tools.getInt(QueryEngine.getInstance().doQueryOne("select c_bpartner_id from users where id="+userid),-1); 
	    	        ArrayList params2=new ArrayList();
	    	        params2.add(docno);
	//    	        System.out.print(docno);
	    	        QueryEngine.getInstance().executeStoredProcedure("E_ORDER_CHECK_BALANCE", params2, false,conn);    
    	 	    }*/
    	 	    
	  	       //request.getSession().setAttribute("flag", flag);
	  	       //request.getSession().setAttribute("docno", docno);
				ValueHolder holder= new ValueHolder();
				holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
				holder.put("code","0");
				JSONObject returnObj=new JSONObject();
				returnObj.put("url", "/var/order_pay.jsp?id="+ e_orderid+
							"&isvar="+ (params.optString("isvar")));
				holder.put("data", returnObj);
				return holder;
    	 	 
   		}catch(Throwable th){
  		    if(th instanceof NDSException) throw (NDSException)th;
  		       logger.error("exception",th);
  		    throw new NDSException(th.getMessage(), th);
  		    
		}finally{
             try{pstmt.close();}catch(Exception ea){}
             try{conn.close();}catch(Exception e){}
            
	  	} 
   	  }

}
