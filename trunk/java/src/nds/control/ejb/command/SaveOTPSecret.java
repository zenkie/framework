package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;

import com.liferay.util.Encryptor;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.Directory;
import nds.security.User;

/**
 * Save OTP secret, secret may not be equal to password stored in lportal.user_
 */

public class SaveOTPSecret extends Command {
    private final static String UPDATE_OTP_INFO="update users set otp_secret=? ,otp_counter=? where id=? and is_otp='Y'";
    private final static String GET_LPORTAL_PASSWORD="select PASSWORDENCRYPTED ,PASSWORD_ from lportal.user_ where userid=?";
    private final static DecimalFormat COUNTER_FORMAT= new DecimalFormat("###,###,###");
	/**
	 * @param event contains "userid" and "secret","secret2"
	 * 
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	//logger.debug(event.toDetailString());
  	User usr=helper.getOperator(event);
	ValueHolder holder= new ValueHolder();
	holder.put("code","1");
  	String lportal_userid= (String)event.getParameterValue("userid");
	long counter;

  	try{
	int userId=SecurityUtils.getUser(lportal_userid).getId().intValue();
	if( userId==-1){
		holder.put("message", "@user-not-found@");
	}else{
	if (userId!=usr.getId().intValue() && !SecurityUtils.hasObjectPermission(usr.getId().intValue(), usr.getName(), 
			"users", userId, Directory.WRITE,event.getQuerySession())){
		holder.put("message", "@no-permission@");
	}else{
		String secret= (String)event.getParameterValue("secret");
		String secret2= (String)event.getParameterValue("secret2");
		if(secret==null|| secret.length()<6){
			holder.put("message", "@secret-length-too-short@");
		}else{
			if(!secret.equals(secret2)){
				holder.put("message", "@two-secret-not-identical@");
			}else{
				// ok, let's check password should not be equal to password stored portal 
				Connection conn=QueryEngine.getInstance().getConnection();
				PreparedStatement pstmt= null;
				ResultSet rs=null;
				boolean fail=false;
				try{
					pstmt= conn.prepareStatement(GET_LPORTAL_PASSWORD);
					pstmt.setString(1, lportal_userid);
					rs= pstmt.executeQuery();
					if(rs.next()){
						boolean encrypted= (rs.getInt(1)==1);
						String pass= rs.getString(2);
						if((encrypted && Encryptor.digest(secret).equals(pass)) ||
								(!encrypted && secret.equals(pass))){
							//same
							holder.put("message", "@secret-should-be-different-to-password@");
							fail=true;
						}
					}else{
						holder.put("message", "@db-error@");
						fail=true;
					}
					rs.close();
					if(!fail){
						
						pstmt= conn.prepareStatement(UPDATE_OTP_INFO);
				    	pstmt.setString(1, Tools.encrypt(secret));
						// 将重新生成计数器，计数器长度为18位的随机数
				    	Random random= new Random(System.currentTimeMillis());
				    	counter=random.nextLong();
				    	if(counter<0) counter=-counter;
				    	pstmt.setLong(2, counter);
				    	pstmt.setInt(3, userId);
				    	int cnt= pstmt.executeUpdate();
				    	if(cnt==1){
				    		holder.put("code","0");
				    		holder.put("message", "@finish-and-counter-set-to@"+COUNTER_FORMAT.format(counter));
				    	}else
				    		holder.put("message","@user-not-allow-to-use-otp@");
					}
				}finally{
					try{if(rs!=null)rs.close();}catch(Exception e3){}
					try{if(pstmt!=null)pstmt.close();}catch(Exception e3){}
					try{if(conn!=null)conn.close();}catch(Exception e){}
				}		
				
				
			}
		}
	}
	}
  	}catch(Exception e){
  		logger.error("Found exception",e);
  		holder.put("message","@exception@:"+ e);
  	}
	return holder;
  }
  
  
  
}