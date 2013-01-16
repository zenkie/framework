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
 * 修改USBKEY保持的密钥
 */

public class ChangeUsbKey extends Command {
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(DefaultWebEvent event){
    	
    	return true;
    }	
	/**
	 * @param event contains "userid" and "usbkeycode"
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	java.lang.String companyId="liferay.com";
  	User usr=helper.getOperator(event);
  	
  	event.setParameter("directory", "USERS_LIST");
  	
  	//helper.checkDirectoryWritePermission(event, usr);
  	
	int userid=Tools.getInt( event.getParameterValue("userid",true), -1);
	if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, "USERS", 
				userid, nds.security.Directory.WRITE, event.getQuerySession()))
			throw new NDSException("@no-permission@");
	java.lang.String usbkeycode= (String)event.getParameterValue("usbkeycode",true);

	try{
		String userd= helper.getUserDomainName(userid);
		QueryEngine.getInstance().executeUpdate("UPDATE USERS SET EMAILVERIFY="+QueryUtils.TO_STRING(usbkeycode)+" WHERE ID="+ userid);

	}catch(Exception e){
		logger.error("error",e);
		throw new NDSException("无法修改密钥信息:"+ e.getMessage());
	}
	ValueHolder holder= new ValueHolder();
	holder.put("message", "密钥修改成功! 注意USBKEY也要同步更新");
	holder.put("code","0");
	return holder;
  }
}