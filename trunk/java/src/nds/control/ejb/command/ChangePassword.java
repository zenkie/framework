package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import nds.control.web.*;

/**
 * Change user password
 * Liferay has its own transaction control
 * Add support for plain password storage in users table if admin specified 
 */

public class ChangePassword extends Command {
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	
    	return true;
    }	
	/**
	 * @param event contains "userid" and "password1" and "password2"
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	java.lang.String companyId="liferay.com";
  	User usr=helper.getOperator(event);
  	
  	event.setParameter("directory", "USERS_LIST");
  	
  	//helper.checkDirectoryWritePermission(event, usr);
  	
	int userid=Tools.getInt( event.getParameterValue("userid",true), -1);
	if( userid != usr.id.intValue()){
		if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, "USERS", 
				userid, nds.security.Directory.WRITE, event.getQuerySession()))
			throw new NDSException("@no-permission@");
	}
	java.lang.String password1= (String)event.getParameterValue("password1",true);
	java.lang.String password2= (String)event.getParameterValue("password2",true);
	boolean passwordReset=false;
	/**
	 * 写入Liferay 系统
	 */
	try{
		String userd= helper.getUserDomainName(userid);
		
		
		//logger.debug("beign update user " + userd + " with password:"+ password1);
		UserLocalServiceUtil.updatePassword(userd, password1,
				password2, passwordReset);

		//store password to users table in plain text
		Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
		if("true".equals(conf.getProperty("security.password.plain", "false")))
			storePlainPassword(userid,password1, usr.name);
		
		/*UserManager userManager = UserManagerFactory.getManager();
		
		userManager.updateUser(userd, password1,
			password2, passwordReset);*/
	}catch(Exception e){
		logger.debug("Error when update password:", e);
		String msg;
		if( e instanceof UserPasswordException){
			UserPasswordException ex= (UserPasswordException)e;
		
		if( ex.getType() ==UserPasswordException.PASSWORD_ALREADY_USED){
			msg= "密码已经被使用过了,请更换!";
		}else if( ex.getType()== UserPasswordException.PASSWORD_INVALID){
			msg= "密码无效，请更换!";
		}else if ( ex.getType()==UserPasswordException.PASSWORDS_DO_NOT_MATCH){
			msg= "两次密码不匹配,请重新输入!";
		}else msg=ex.getMessage();
		}else
			msg= e.getMessage();
		throw new NDSException("无法修改用户到门户安全系统!"+ msg);
	}
	ValueHolder holder= new ValueHolder();
	holder.put("message", "密码修改成功!");
	holder.put("code","0");
	return holder;
  }
  private void storePlainPassword(int userId, String passwd, String operator)throws Exception{
	  logger.debug("Change password of "+ userId +" by "+operator);
	  QueryEngine.getInstance().executeUpdate("UPDATE USERS SET PASSWORDHASH="+QueryUtils.TO_STRING(passwd)+" WHERE ID="+ userId);
  }
}