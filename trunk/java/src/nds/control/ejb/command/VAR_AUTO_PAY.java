package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
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

//import com.var.bean.VarResponse;

public class VAR_AUTO_PAY extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		
		TableManager manager=TableManager.getInstance();
	  	java.util.Locale locale= event.getLocale();
  	    QueryEngine engine=QueryEngine.getInstance();
        Connection conn= engine.getConnection();
        PreparedStatement pstmt=null;   
        String email;
        String docno="";
        String return_url="";
        String Bargainor_id=(String)event.getParameterValue("bargainor_id");
   	    String spbill_create_ip=(String)event.getParameterValue("spbill_create_ip");
    //	VarResponse vr=new VarResponse();
	  /*	vr.setPname((String)event.getParameterValue("pame"));
	   		vr.setPemail((String)event.getParameterValue("pemail"));
	   		vr.setParea((String)event.getParameterValue("parea"));
	   		vr.setPphone((String)event.getParameterValue("pphone"));
	   		vr.setPmobile((String)event.getParameterValue("pmoblile"));
	   		vr.setAttach((String)event.getParameterValue("attach"));
	   		vr.setBargainor_id((String)event.getParameterValue("bargainor_id"));
	   		vr.setTransaction_id((String)event.getParameterValue("transaction_id"));
	   		
	   		vr.setCmdno((String)event.getParameterValue("cmdno"));
	   	    vr.setDate((String)event.getParameterValue("date"));
	   	    vr.setSp_billno((String)event.getParameterValue("sp_billno"));
	   	    vr.setPay_info("fail");
	   	    vr.setTotal_fee(Long.parseLong((String)event.getParameterValue("total_fee")));   	
	   	
	   	    */
     try{
   		 if(event.getParameterValue("cmdno").equals("1")){
	   	    int c_bpartner_id=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from c_bpartner where serialno='"+Bargainor_id+"'"),-1);
	   	    int count=Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from e_client where agent_id="+c_bpartner_id+" and email='"+(String)event.getParameterValue("pemail")+"'" ),0);
	   	    if(count==0){
	   	     int e_orderid=QueryEngine.getInstance().getSequence("e_order", conn);
	   	     List c_bpartnerlist= QueryEngine.getInstance().doQueryList("select ad_client_id, ad_org_id, id,name,email,mobile,contactor from c_bpartner where serialno='"+Bargainor_id+"'" );	   		
	   		 pstmt= conn.prepareStatement("insert into e_order(id,ad_client_id, ad_org_id,docno,doctype,amt,c_bpartner_id,pname,pemail,pmobile,ptruename,state,parea,pphone) values(?,?,?,Get_SequenceNo('VAR',?),'M',?,?,?,?,?,?,'N',?,?)");
	   		 pstmt.setInt(1,e_orderid);
	   		 pstmt.setInt(2,Tools.getInt(((List)c_bpartnerlist.get(0)).get(0),-1));
	   		 pstmt.setInt(3,Tools.getInt(((List)c_bpartnerlist.get(0)).get(1),-1));
	   		 pstmt.setInt(4,Tools.getInt(((List)c_bpartnerlist.get(0)).get(0),-1));
	   		 pstmt.setInt(5,Tools.getInt((String)event.getParameterValue("total_fee"), 0));	
             pstmt.setInt(6,Tools.getInt(((List)c_bpartnerlist.get(0)).get(2),-1));
             pstmt.setString(7,(String)event.getParameterValue("pame"));
             pstmt.setString(8,(String)event.getParameterValue("pemail"));	
             pstmt.setString(9,(String)event.getParameterValue("pmoblile"));	
             pstmt.setString(10,(String)event.getParameterValue("ptruename"));
             pstmt.setString(11,(String)event.getParameterValue("parea"));
             pstmt.setString(12,(String)event.getParameterValue("pphone"));
 		     pstmt.executeUpdate();	
	  		docno=(String)QueryEngine.getInstance().doQueryOne("select docno from e_order where id="+e_orderid); 
			ArrayList params1=new ArrayList();
  			params1.add(docno);
			QueryEngine.getInstance().executeStoredProcedure("E_ORDER_CHECK_BALANCE", params1, false,conn);
			ArrayList params2 = new ArrayList();
			params2.add(e_orderid);
			params2.add(0);
			params2.add("");
			params2.add("");
			QueryEngine.getInstance().executeStoredProcedure("E_ORDERSUBMIT", params2, false,conn);
	   	 }
   		}else if(event.getParameterValue("cmdno").equals("2")){
	   	    int c_bpartner_id=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from c_bpartner where serialno='"+Bargainor_id+"'"),-1);
  	   	   int count=Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from e_client where agent_id="+c_bpartner_id+" and email='"+(String)event.getParameterValue("pemail")+"'" ),0);
  	   	   if(count!=0){
  	   		int e_orderid=QueryEngine.getInstance().getSequence("e_order", conn);
  	   		List c_bpartnerlist= QueryEngine.getInstance().doQueryList("select ad_client_id, ad_org_id,agent_id,name,email,mobile,truename,area,phone from e_client where email='"+(String)event.getParameterValue("pemail")+"'" );
	   		pstmt= conn.prepareStatement("insert into e_order(id,ad_client_id, ad_org_id,docno,doctype,amt,c_bpartner_id,pname,pemail,pmobile,ptruename,state,parea,pphone) values(?,?,?,Get_SequenceNo('VAR',?),'M',?,?,?,?,?,?,'N',?,?)");
	   		 pstmt.setInt(1,e_orderid);
	   		 pstmt.setInt(2,Tools.getInt(((List)c_bpartnerlist.get(0)).get(0),-1));
	   		 pstmt.setInt(3,Tools.getInt(((List)c_bpartnerlist.get(0)).get(1),-1));
	   		 pstmt.setInt(4,Tools.getInt(((List)c_bpartnerlist.get(0)).get(0),-1));
	   		 pstmt.setInt(5,Tools.getInt((String)event.getParameterValue("total_fee"), 0));	
             pstmt.setInt(6,Tools.getInt(((List)c_bpartnerlist.get(0)).get(2),-1));
             pstmt.setString(7,(String)((List)c_bpartnerlist.get(0)).get(3));
             pstmt.setString(8,(String)((List)c_bpartnerlist.get(0)).get(4));	
             pstmt.setString(9,(String)((List)c_bpartnerlist.get(0)).get(5));	
             pstmt.setString(10,(String)((List)c_bpartnerlist.get(0)).get(6));
             pstmt.setString(11,(String)((List)c_bpartnerlist.get(0)).get(7));
             pstmt.setString(12,(String)((List)c_bpartnerlist.get(0)).get(8));
 		     pstmt.executeUpdate();	
 		    docno=(String)QueryEngine.getInstance().doQueryOne("select docno from e_order where id="+e_orderid);
			ArrayList params1=new ArrayList();
  			params1.add(docno);
			QueryEngine.getInstance().executeStoredProcedure("E_ORDER_CHECK_BALANCE", params1, false,conn);

			ArrayList params3=new ArrayList();
			params3.add(e_orderid);
			params3.add(0);
			params3.add("");
			params3.add("");
			QueryEngine.getInstance().executeStoredProcedure("E_ORDERSUBMIT", params3, false,conn);
  	   	   }
  	   	  }else if(event.getParameterValue("cmdno").equals("3")){
  	   	   int c_bpartnerId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from c_bpartner where serialno='"+Bargainor_id+"'" ),-1);
  	   	   int count=Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from e_client where agent_id="+c_bpartnerId+" and email='"+(String)event.getParameterValue("pemail")+"'" ),0);
  	   	     if(count!=0){
    	    ArrayList params2=new ArrayList();
    	      int e_orderid=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from e_client where email='"+(String)event.getParameterValue("pemail")+"'" ),0);
 		       params2.add(e_orderid);
 			   params2.add(0);
 			   params2.add("");
 			   params2.add("");
 		       QueryEngine.getInstance().executeStoredProcedure("ad_client_pause", params2, false,conn);
  	   	      }else{
  	   		
  	   	      }
    	       
    	     }else if(event.getParameterValue("cmdno").equals("4")){
    	    	  int c_bpartnerId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from c_bpartner where serialno='"+Bargainor_id+"'" ),-1);
    	   	      int  count=Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from e_client where agent_id="+c_bpartnerId+" and email='"+(String)event.getParameterValue("pemail")+"'" ),0);
    	   	      if(count!=0){
    	    	   int e_orderid=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from e_client where email='"+(String)event.getParameterValue("pemail")+"'"),-1); 
     	           ArrayList params3=new ArrayList();
  		           params3.add(e_orderid);
  			       params3.add(0);
  			       params3.add("");
  			       params3.add("");
  		          QueryEngine.getInstance().executeStoredProcedure("ad_client_stop", params3, false,conn);	
    	         }else{
    	    	 
    	         }
    	   }
   		}catch(Throwable th){
   	  		if(th instanceof NDSException) throw (NDSException)th;
   	  		logger.error("exception",th);
   	  		throw new NDSException(th.getMessage(), th);
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
