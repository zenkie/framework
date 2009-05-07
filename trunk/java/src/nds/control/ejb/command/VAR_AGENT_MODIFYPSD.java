package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.Configurations;
import nds.util.NDSException;
import nds.util.Tools;

import org.json.JSONObject;

import com.liferay.portal.UserPasswordException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.tenpay.util.MD5Util;
import com.tenpay.util.TenpayUtil;

public class VAR_AGENT_MODIFYPSD extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
		
		logger.debug(event.toDetailString());
		Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		String key= (String)conf.getProperty("var.passwordcord");
		int user_id=Tools.getInt(event.getParameterValue("user_id"),-1);
		String password=(String)event.getParameterValue("password");
		String encode=(String)event.getParameterValue("encode");
	    StringBuffer buf = new StringBuffer(); 
	    TenpayUtil.addBusParameter(buf, "user_id", user_id);
	    TenpayUtil.addParameter(buf, "password", password);
	    String requestParameters = buf.toString();
	    TenpayUtil.addParameter(buf, "key", key);
	    String checkencode=MD5Util.MD5Encode(buf.toString()).toUpperCase();
		//System.out.print(checkencode);
		//System.out.print(encode);
	    if(checkencode.equals(encode)){
   		try{  			
			String userd= helper.getUserDomainName(user_id);
			UserLocalServiceUtil.updatePassword(userd, password,password, false);
			} catch (Exception e) {
				logger.debug("Error when update password:", e);
				String msg;
				if( e instanceof UserPasswordException){
					UserPasswordException ex= (UserPasswordException)e;
				
				if( ex.getType() ==UserPasswordException.PASSWORD_ALREADY_USED){
					msg= "密码已经被使用过了，请更换";
				}else if( ex.getType()== UserPasswordException.PASSWORD_INVALID){
					msg= "密码无效，请更换";
				}else if ( ex.getType()==UserPasswordException.PASSWORDS_DO_NOT_MATCH){
					msg= "两次密码不匹配，请重新输入";
				}else msg=ex.getMessage();
				}else
					msg= e.getMessage();
				throw new NDSException("无法修改用户到门户安全系统:"+ msg);
			}
   	  }
	    ValueHolder holder= new ValueHolder();
		holder.put("message", "密码修改成功！");
	    holder.put("code","0");
	    return holder;
	}
	
}

