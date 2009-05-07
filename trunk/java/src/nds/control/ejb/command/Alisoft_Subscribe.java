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
 * Alisoft 的定购处理，参数将从session 中获取，而金额将从form中获取
 * 
 *  返回 url 作为重定向页面
 *  参照http://forum.alisoft.com/viewthread.php?tid=2407&extra=page%3D3
 * 
 * @author yfzhu
 *
 */
public class Alisoft_Subscribe extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		 Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
		 String appId= conf.getProperty("saas.alisoft.appkey");
		 String appsecret= conf.getProperty("saas.alisoft.appsecret");
		
		TableManager manager=TableManager.getInstance();
		
	  	JSONObject jo=(JSONObject)event.getParameterValue("jsonObject");
	  	
	  	java.util.Locale locale= event.getLocale();
  	    QueryEngine engine=QueryEngine.getInstance();
        boolean hasError=false;
        MessagesHolder mh= MessagesHolder.getInstance();
        
        String postData;//平台要求的原样回传的参数
        String returnUrl;//回传url
        String subscType;//订购类型
        double  amount;//金额
        double  rentAmount;
        double  resourceAmount;
        String ctrlParams;//控制参数
        String signature;//签名
   	  	ValueHolder holder= new ValueHolder();

   		try{
   		   JSONObject params=jo.getJSONObject("params");
   			/*
   		   * 从servletcontext中读取需要的参数
   		   */
   			
   		  subscType=params.getString("subscType");
   		  postData=params.getString("postData");
   		  returnUrl=params.getString("returnUrl");
   		  String gmtStart=params.getString("gmtStart");
   		  
   		  logger.debug("subscType"+ subscType+",gmtStart="+ gmtStart+",postData="+postData+",returnUrl="+returnUrl);

   		  
   		  String gmtEnd=addMon(gmtStart,1);//计算订单结束时间，即订单开始时间加上订购时间，此处写死为一year
   		  /*
   		   * 订购类型不同时，传给平台的参数也是不同的。所以，根据订购类型，分别进行参数的组织
   		   */
   		  Map<String, Object> map=new HashMap<String, Object>();
  		   rentAmount=0;//
  		   
  		   
   		   resourceAmount= params.getInt("amtpay"); // from form
   		   
   		   map.put("rentAmount", rentAmount);
   		   map.put("resourceAmount", resourceAmount);
   		   amount=rentAmount+resourceAmount;
   		   map.put("amount", amount);
   		   ctrlParams="amount="+ amount+"&rent="+rentAmount;
   		   
   		   if(amount <=0) throw new NDSException("支付金额不正确:"+ amount);
   		   
   		   map.put("description", "您的本次支付总额为"+amount+"元");

   		   map.put("postData", postData);
			String email= params.optString("email");
			String domainDesc= params.optString("domaindesc");
			if(nds.util.Validator.isNotNull(email)) ctrlParams+="&email="+ email;
			if(nds.util.Validator.isNotNull(domainDesc)) ctrlParams+="&domaindesc="+encodeURL(domainDesc);

			if(subscType.equals("0")){//新订
	   		   map.put("gmtStart",gmtStart);
	   		   map.put("gmtEnd", gmtEnd);
	   		   /**
	   		    * 校验email 是否在系统中已经存在，若已经存在需要报错
	   		    */
	   		   if(isEmailExists(email)){
	   			   throw new NDSException("Email:"+ email+"在系统中已经被使用，请修改");
	   		   }
   		   }else if(subscType.equals("1")){//未到期续订，不能修改订购开始时间，及控制参数
	   		   map.put("gmtEnd", gmtEnd);
   		  }else if(subscType.equals("2")){//到期续订
	   		   map.put("gmtStart",gmtStart);
	   		   map.put("gmtEnd", gmtEnd);
   		  }else {//订购资源，其中月租部分为零
   		  }
  		   map.put("ctrlParams", ctrlParams);
   		  signature=SubscribeSignUtil.Signature(map, appsecret);//签名
   		  map.put("signature", signature);
   		  /*
   		   * 组织参数
   		   */ 
   		  StringBuffer buffer = new StringBuffer();
   		  boolean notFirst = false;
   		  for (Map.Entry<String, ?> entry : map.entrySet()) {
   		   if (notFirst) {
   		    buffer.append("&");
   		   } else {
   		    notFirst = true;
   		   }
   		   Object value = entry.getValue();
   		   buffer.append(entry.getKey()).append("=").append(
   		     encodeURL(value) );
   		  }
   		  String queryString=buffer.toString();
   		  
   		  /*
   		   * 跳转回平台，并带上相关的订购参数
   		   */
   		  String url=(returnUrl+"?"+queryString);
   		  logger.debug("url:"+ url);
   		  
   		  holder.put("data", url);
   		  
   		}catch(Throwable t){
   	  		if(t instanceof NDSException) throw (NDSException)t;
   	  		logger.error("exception",t);
   	  		throw new NDSException("操作遇到异常，请刷新页面重试");
   	  		//holder.put(name, value)
   	  	}
   		holder.put("message", nds.util.MessagesHolder.getInstance().translateMessage("@complete@",locale));
   		holder.put("code","0");
   		return holder;
   	  }
	
	private boolean isEmailExists(String email) throws Exception{
		return Tools.getInt( 
				QueryEngine.getInstance().doQueryOne(
				"select count(*) from users where email="+ QueryUtils.TO_STRING(email)), -1)>0;
	}
		
	/*
	  * 编码
	  */
	private String encodeURL(Object target) throws Exception {
	  String result = (target != null) ? target.toString() : "";
	  result = URLEncoder.encode(result, "GBK");
	  return result;
	}

	/*
	  * 日期计算
	  */
	private static String addMon(String s, int n) {    
	   Calendar cd=null;
	  java.text.SimpleDateFormat TIME_FORMATER = new java.text.SimpleDateFormat("yyyy-MM-dd");//时间格式
	  try {    
	              
	   
             cd = Calendar.getInstance();    
            cd.setTime(TIME_FORMATER.parse(s));    
            cd.add(Calendar.MONTH, n);//增加一月    
            cd.add(Calendar.DATE, -1);
	   
        } catch (Exception e) {    
           e.printStackTrace();
        }    
	        
	        return TIME_FORMATER.format(cd.getTime());    
	}    

	
}
