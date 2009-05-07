/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.portal.auth;

import nds.otp.*;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.security.auth.*;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.model.User;

import com.liferay.util.Encryptor;
import com.liferay.portal.util.PropsUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
/**
 * Will try to load first n chars in the input password,and check using OTP method 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class OTPAuth implements Authenticator {
	private static final Log logger = LogFactory.getLog(OTPAuth.class);
	private DataSource datasource;

	private final static String GET_OTP_INFO="select u.is_otp,u.otp_length,u.otp_secret,u.otp_counter, u.id from users u, ad_client a where u.name=? and u.ad_client_id=a.id and a.domain=?";	
	private final static String UPDATE_OTP_INFO="update users set otp_counter=? where id=?";
	
	/**
	 * do not check if using email address
	 */
	public int authenticateByEmailAddress(
			String companyId, String emailAddress, String password,
			Map headerMap, Map parameterMap)throws AuthException{
		return SUCCESS;
	}

	public int authenticateByUserId(
			String companyId, String userId, String password, Map headerMap,
			Map parameterMap)throws AuthException{
		logger.debug("OTPAuth:pass:"+ password+", user="+userId);
		try {

			OTPInfo otp= getOTPInfo(userId);
			if (otp==null){
				//  direct return true, escape otp check
				return SUCCESS;
			}
			/**
			 * 算法：根据当前系统时间+用户的secret+counter 计算密码，不一致的时候，将counter+1继续计算
			 * 当counter的计数达到 portal.properties 设定的auth.impl.otp.counter.window的值时，将
			 * 停止匹配
			 * 如果匹配到了password, 将设置 系统记录的counter=匹配时的counter
			 */
			String pass=  password.substring(0,otp.getTokenLength());
			logger.debug("pass:"+ pass+ ", otp:"+ otp);
			String cp;
			cp=  otp.computPassword();
			if(cp.equalsIgnoreCase(pass)){
				return SUCCESS;
			}else{
				logger.debug("should be "+ cp+", "+ otp.toString());
			}
			// still not found
			return FAILURE;
		}
		catch (Exception e) {
			logger.debug("Could not auth",e);
			throw new AuthException(e);
		}
		
	}
	/**
	 * Save count to user (specified in otpinfo.tag)
	 * @param otp
	 * @throws Exception
	 * @deprecated
	 */
	private void saveOTPInfo(OTPInfo otp) throws Exception{
		int uid=Integer.valueOf( otp.getTag()).intValue();
		Connection conn= getConnection();
		PreparedStatement pstmt= null;
		try{
			pstmt= conn.prepareStatement(UPDATE_OTP_INFO);
			
	    	pstmt.setLong(1, otp.getCounter());
	    	pstmt.setInt(2, uid);
	    	pstmt.executeUpdate();

		}finally{
			try{pstmt.close();}catch(Exception e3){}
			try{conn.close();}catch(Exception e){}
		}		
	}
	/**
	 * Get only otp password from input, which may contain both portal password and 
	 * OTP. It should be checked from "nds2.users" table to make sure whether user password 
	 * contains OTP or not, and also the length of OTP in pass. 
	 * @param pass 
	 * @param userId
	 * @return null if not is otp
	 * @throws Exception
	 */
	private OTPInfo getOTPInfo(String userId) throws Exception{
		OTPInfo otp=new OTPInfo();
		/**
		  *  由于 lportal 的限制，名称一律设置为小写
		  *  参见：com.liferay.portal.ejb.UserManagerImpl#_authenticate
		  *  登录的时候由系统强行设置登录名为小写后验证
		  */
		String domainName=userId;
		String uName, adclientName ;
		int p=domainName.indexOf("@");
		if ( p<=0) {
			return null;
			//throw new AuthException("user id is invalid:"+userId);
		}
		uName= domainName.substring(0,p );
		adclientName= domainName.substring(p+1);
			 
		String portalPassword=null;
		// connection to "nds2.users" table
		Connection conn= getConnection();
		PreparedStatement pstmt= null;
		ResultSet rs=null;
		try{
			pstmt= conn.prepareStatement(GET_OTP_INFO);
			
	    	pstmt.setString(1, uName);
	    	pstmt.setString(2, adclientName);
	    	rs=pstmt.executeQuery();
	    	if(rs.next()){
	    		boolean isOTP="Y".equals(rs.getString(1));
	    		if(isOTP){
		    		otp.setTokenLength(rs.getInt(2));
		    		otp.setSecret(deobfuscate(rs.getString(3)));
		    		otp.setCounter(rs.getLong(4));
		    		// user id is for later usage when save otp info to db
		    		otp.setTag(String.valueOf(rs.getInt(5)));
		    		otp.setCheckNo(CheckNO.getInstance().currentValue());
		    		return otp;
	    		}else{
	    			//no need to load infor
	    			return null;
	    		}
	    	}
	    	// user not found
	    	return null;
		}finally{
			try{rs.close();}catch(Exception e2){}
			try{pstmt.close();}catch(Exception e3){}
			try{conn.close();}catch(Exception e){}
		}
		
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
	/**
	 * Read from portal.properties#auth.impl.otp.counter.window
	 *
	 */
	private int getOTPCounterWindow(){
		int i;
		try{
			i=Integer.parseInt((String) PropsUtil.get("auth.impl.otp.counter.window"));
			
			if(i<1) i=1;//  minimum to 1 time
		}catch(Exception e){
			i= 5;// default to 5 retry time
		}
		return i;
	}

    
    /* Same as in nds.util.StringUtils#deobfuscate */
    private String deobfuscate(String s)
    {
       
        byte[] b=new byte[s.length()/2];
        int l=0;
        for (int i=0;i<s.length();i+=4)
        {
            String x=s.substring(i,i+4);
            int i0 = Integer.parseInt(x,36);
            int i1=(i0/256);
            int i2=(i0%256);
            b[l++]=(byte)((i1+i2-254)/2);
        }

        return new String(b,0,l);
    }	
	
}