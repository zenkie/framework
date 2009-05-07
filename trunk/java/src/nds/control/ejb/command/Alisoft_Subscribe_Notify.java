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
import com.alisoft.sip.sdk.isv.*;
import nds.saasifc.alisoft.*;

import java.util.*;
import java.net.*;

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
import javax.servlet.http.*;
/**
 * Alisoft 的定购通知处理，参数将从event.params中获取
 * 
 * 按照 http://forum.alisoft.com/viewthread.php?tid=3055&extra=page%3D1  设计的定购通知页面
 * 
 * @author yfzhu
 *
 */
public class Alisoft_Subscribe_Notify extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		 Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
		 String appId= conf.getProperty("saas.alisoft.appkey");
		 String appsecret= conf.getProperty("saas.alisoft.appsecret");

		 
		  Map  map =(Map) event.getParameterValue("params");
		  String sig=(String)map.get(OrderConstants.PARAMETER_SIGNATURE);//由于加密时要去掉获得的signature本身，所以，先行保存
		  
		  String sign=SubscribeSignUtil.Signature(map, appsecret);//加密
		  
		  String subevent= (String)map.get(OrderConstants.PARAMETER_EVENT);//事件类型：subsc-新订；renewAhead-未到期续订；renew-到期续订；resource-购买资源；break-退订
		  String userId=(String)map.get(OrderConstants.PARAMETER_USERID);

		  String subscId=(String)map.get(OrderConstants.PARAMETER_SUBSCID); //新订单ID
		  String gmtStart=(String)map.get(OrderConstants.PARAMETER_GMTSTART);
		  String gmtEnd=(String)map.get(OrderConstants.PARAMETER_GMTEND);
		  
		  String ctrlParams=(String)map.get(OrderConstants.PARAMETER_CTRLPARAMS);
		  String totalAmount=(String)map.get(OrderConstants.PARAMETER_TOTALAMOUNT);
		  String amount=(String)map.get(OrderConstants.PARAMETER_AMOUNT); // what i need
		  String rentAmount=(String)map.get(OrderConstants.PARAMETER_RENTAMOUNT);
		  String resourceAmount=(String)map.get(OrderConstants.PARAMETER_RESOURCEAMOUNT);
		  String couponAmount=(String)map.get(OrderConstants.PARAMETER_COUPONAMOUTN);
		  
		  QueryEngine engine=QueryEngine.getInstance();
			PreparedStatement stmt=null;
//		    ResultSet rs=null;
			Connection conn= engine.getConnection();
		  
		  String message=null;
		  ValueHolder holder= new ValueHolder();
		  try{
			  /*
			   * 先验证应用ID及签名是否一致
			   * 
			   */
			  if(sig!=null && !sig.equals(sign)){
				  logger.warning(sig +"!="+ sign);
			  }
			  if(!(appId.equals(map.get(OrderConstants.PARAMETER_APPID))/*&& sig!=null && sig.equals(sign)*/)){
				  logger.error("appId:"+appId+"!="+OrderConstants.PARAMETER_APPID+":"+map.get(OrderConstants.PARAMETER_APPID));
				  message="定购参数错误，请返回<a href='http://www.alisoft.com'>阿里软件</a>重试";
			  }else{
				  // 根据用户，而不是定购状态来确用户采购方式，目前整个都是资源预定型
				  logger.debug("subevent="+ subevent);
				  if("resource".equals(subevent)){
					  boolean isNewClient= Tools.getInt(QueryEngine.getInstance().doQueryOne("select count(*) from users where saasvendor='alisoft' and saasuser='"+ userId+"'"), 0)==0;
					  if(isNewClient) subevent="subsc";
				  }
				  if("subsc".equals(subevent)){
					  int amt=(int)Double.parseDouble( totalAmount);
					  //new
					  // 尝试从 ctrlParams 里获取 email 和 domaindesc
					  PairTable pt= PairTable.parse(ctrlParams, null);
					  String email=(String) pt.get("email");
					  String domainDesc=(String) pt.get("domaindesc");
					  if(domainDesc!=null)domainDesc=  URLDecoder.decode(domainDesc, "GBK");
					  
					  logger.debug("new "+domainDesc+", email="+email+",desc="+domainDesc  +", amt="+ amt);
					  
					  if(nds.util.Validator.isNull(email)) throw new NDSException("您的Email信息未设置");
					  if(nds.util.Validator.isNull(domainDesc)) domainDesc="网店"; 
					  
					  String templateClient=conf.getProperty("newclient.template","demo");
					  
					  ArrayList params=new ArrayList();
					  params.add(email);
					  params.add(userId);//null 
					  params.add("alisoft");// 
					  params.add(new Integer(amt));//
					  params.add(templateClient);
					  params.add(domainDesc);//
					  params.add("test");
					  params.add("");	// domain is null
					  SPResult result=engine.executeStoredProcedure("ad_client_saas_new", params, true, conn);
					  message= result.getMessage();
					  int code= result.getCode();// this is ad_client_id that created
					  String domain =(String )engine.doQueryOne("select domain from ad_client where id="+ code, conn);
					  boolean isMultipleClientEnabled= "true".equals(conf.getProperty("webclient.multiple","false"));					
					  if(isMultipleClientEnabled){
						// clone web folder in /act/webroot/$domain
						String webRoot= conf.getProperty("client.webroot","/act/webroot");
						String srcClientFolder= webRoot+"/"+ templateClient;
						String destClientFolder=webRoot+"/"+ domain;

						logger.debug("copy dir "+ srcClientFolder+" to "+ destClientFolder);
						nds.util.FileUtils.delete(destClientFolder);
						nds.util.FileUtils.copyDirectory(srcClientFolder, destClientFolder);
						

						}
					  //message="定购完成，您可以登录了";
				  }else if("break".equals(subevent)){
					  //break
					  int clientId =Tools.getInt(engine.doQueryOne("select ad_client_id from users where saasvendor='alisoft' and saasuser="
							  + QueryUtils.TO_STRING(userId), conn), -1);
					  if(clientId<0){
						  throw new NDSException("未找到您作为系统管理员对应的帐套");
					  }
					  ArrayList params=new ArrayList();
					  params.add(new Integer(clientId));//
						
					  SPResult result=engine.executeStoredProcedure("ad_client_break", params, false, conn);
					  
					  message="退定完成";
				  }else{
					  //pay
					  int clientId =Tools.getInt(engine.doQueryOne("select ad_client_id from users where name='root' and saasvendor='alisoft' and saasuser="
							  + QueryUtils.TO_STRING(userId), conn), -1);
					  if(clientId<0){
						  throw new NDSException("未找到您作为系统管理员对应的帐套");
					  }
					  ArrayList params=new ArrayList();
					  int amt=(int)Double.parseDouble( totalAmount);
					  
					  params.add(new Integer(clientId));//
					  params.add(new Integer(amt));//
					  
						
					  SPResult result=engine.executeStoredProcedure("ad_client_renew", params, false, conn);
					  
					  message="续费成功，续费金额"+ amt +"元" ;
				  }
			  }
   		}catch(Throwable t){
   	  		logger.error("exception",t);
   	  		if(t instanceof NDSException) throw (NDSException)t;
   	  		throw new NDSException("操作遇到异常，请刷新页面重试。如您确认已经付款，请联系我们的客服人员协助解决 "+
   	  			conf.getProperty("company.contactor", ""));
   	  		//holder.put(name, value)
   	  	}finally{
	   	  	try{if(stmt!=null)stmt.close();}catch(Exception ea){}
	        try{conn.close();}catch(Exception e){}   	  		
   	  	}
   		holder.put("message",message);
   		holder.put("code","0");
   		return holder;
   	  }
	
}
