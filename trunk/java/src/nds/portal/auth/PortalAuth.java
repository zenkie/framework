/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.portal.auth;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.security.auth.*;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.model.User;

import com.liferay.util.Encryptor;
import com.liferay.portal.util.PropsUtil;

import java.util.*;
import java.sql.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Do portal authentication, if the user is set using OTP method,
 * which is defined in "nds2.users" table, then first n chars will be 
 * eliminated to compare with portal database. the first n chars
 * contains OTP.
 * 
 * Also, system administrator can specify whether to allow user not to
 * enter system password during login when using OTP.
 * 
 * @author yfzhu@agilecontrol.com
 */

public class PortalAuth implements Authenticator {
	private static final Log logger = LogFactory.getLog(Authenticator.class);
	private DataSource datasource;
	private final static String GET_OTP_PASS_BY_NAME="select u.is_otp,u.otp_length, u.is_otp_only from users u, ad_client c where u.name=? and u.ad_client_id=c.id and c.domain=?";
	private final static String GET_OTP_PASS_BY_EMAIL="select u.is_otp,u.otp_length, u.is_otp_only from users u, ad_client c where u.email=? and u.ad_client_id=c.id and c.domain=?";
	
	public int authenticateByEmailAddress(
			String companyId, String emailAddress, String password,
			Map headerMap, Map parameterMap)throws AuthException{
		
		try {

			User user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
			
			return authenticateInPortal(password, user,false);

		}
		catch (Exception e) {
			logger.debug("Could not auth",e);
			throw new AuthException(e);
		}		
	}
	/**
	 * Check if user is OTP specified
	 * @password may contains OTP token
	 */
	public int authenticateByUserId(
			String companyId, String userId, String password, Map headerMap,
			Map parameterMap)throws AuthException{
		//logger.debug("PortalAuth:authenticate company:"+ companyId+",userid="+ userId+",password="+ password);
		try {

			User user = UserLocalServiceUtil.getUserById(companyId, userId);
			
			return authenticateInPortal(password, user,true);

		}
		catch (Exception e) {
			logger.debug("Could not auth",e);
			throw new AuthException(e);
		}
	}
	/**
	 * Get only portal password from input, which may contain both portal password and 
	 * OTP. It should be checked from "nds2.users" table to make sure whether user password 
	 * contains OTP or not, and also the length of OTP in pass.
	 * 
	 *  Currently we do not support OTP check on single company mode (using email address to authenticate)
	 * @param pass 
	 * @param userId
	 * @param otpCheck if true, will check otp, else, not
	 * @return
	 * @throws Exception 
	 */
	private int authenticateInPortal(String pass, User user, boolean otpCheck) throws Exception{
		  /**
		  *  由于 lportal 的限制，名称一律设置为小写
		  *  参见：com.liferay.portal.ejb.UserManagerImpl#_authenticate
		  *  登录的时候由系统强行设置登录名为小写后验证
		  */
		String domainName=user.getUserId();
		boolean checkPortalPassword=true;
		String portalPassword=pass;
		if(otpCheck){			
			String uName, adclientName ;
			int p=domainName.lastIndexOf("@");
			if ( p<=0){
				 
				// not a nds user, but may be portal super suer "liferay.com.1"
				//throw new AuthException("user id is invalid:"+user.getUserId());
			}else{
				uName= domainName.substring(0,p );
				adclientName= domainName.substring(p+1);
					 
				// connection to "nds2.users" table
				Connection conn= getConnection();
				PreparedStatement pstmt= null;
				ResultSet rs=null;
				try{
					pstmt= conn.prepareStatement(GET_OTP_PASS_BY_NAME);
			    	pstmt.setString(1, uName);
			    	pstmt.setString(2, adclientName);
			    	rs=pstmt.executeQuery();
			    	if(rs.next()){
			    		boolean isOTP="Y".equals(rs.getString(1));
			    		int len= rs.getInt(2);
			    		if(isOTP){
			    			portalPassword= pass.substring(len);
			    			// if not is_otp_only, then should check portal password
			    			checkPortalPassword = !"Y".equals( rs.getString(3));
			    		}
			    	}else{
			    		// user not exists in users table
			    		//portalPassword=pass;
			    	}
		    	
				}finally{
					try{rs.close();}catch(Exception e2){}
					try{pstmt.close();}catch(Exception e3){}
					try{conn.close();}catch(Exception e){}
				}
			}
		}
		int authResult = Authenticator.SUCCESS;
		//logger.debug("check system password ="+checkPortalPassword );
		if(checkPortalPassword){
			
			String encPwd = Encryptor.digest(portalPassword);
	
			if (user.getPassword().equals(encPwd)) {
				authResult = Authenticator.SUCCESS;
			}
			else {
				authResult = Authenticator.FAILURE;
			}
		}
		return authResult;	

	}

	/**
	 * Get connection to nds2 schema
	 * @return
	 * @throws Exception
	 */
	private Connection getConnection() throws Exception{
        // Get a context for the JNDI look up
		if(datasource==null){
	        Context ctx = new InitialContext();
	        // Look up myDataSource
	        String name= PropsUtil.get("nds.datasource");
	        datasource = (DataSource) ctx.lookup (name);
		}
		return datasource.getConnection();
	}
}
