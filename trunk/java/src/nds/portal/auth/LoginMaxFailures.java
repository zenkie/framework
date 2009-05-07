/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.portal.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.liferay.portal.security.auth.*;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.model.User;


import java.util.Map;
/**
 * set user inactive, and log current date to lastlogindate,
 * PortalAuth will check is user inactive, if true, will check is user time inactive duration
 * expire
 * @author yfzhu@agilecontrol.com
 */

public class LoginMaxFailures implements AuthFailure {
	private static final Log logger = LogFactory.getLog(LoginMaxFailures.class);
	
	public void onFailureByEmailAddress(
			String companyId, String emailAddress, Map headerMap,
			Map parameterMap)
		throws AuthException {
//		 make sure the user exists in system
		try{
			User user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
			if(user!=null){
				UserManager.getInstance().putInactiveUser(emailAddress);
				user.setFailedLoginAttempts(0);
				com.liferay.portal.service.persistence.UserUtil.update(user);
			}
		}catch(Exception e){
			logger.error("Fail to find user for user email="+emailAddress, e);
		}		
	}

	public void onFailureByUserId(
			String companyId, String userId, Map headerMap, Map parameterMap)
		throws AuthException {
	
		// make sure the user exists in system
		try{
			User user = UserLocalServiceUtil.getUserById(companyId, userId);
			if(user!=null){
				UserManager.getInstance().putInactiveUser(userId);
				user.setFailedLoginAttempts(0);
				com.liferay.portal.service.persistence.UserUtil.update(user);
			}
		}catch(Exception e){
			logger.error("Fail to find user for userId="+userId, e);
		}
	}

}
