package nds.saasifc;

import nds.security.*;
import nds.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
/**
 * Helper class for saas user handling
 * @author yfzhu
 *
 */
public class UserUtils {
    private static final String GET_USER="select u.name, u.passwordhash, u.isactive, u.description, c.domain, u.ad_client_id, u.email from users u, ad_client c where u.saasvendor=? and u.saasuser=? and c.id=u.ad_client_id";
    private static final String GET_USER_BY_MAIL="select u.name, u.passwordhash, u.isactive, u.description, c.domain, u.ad_client_id, u.email from users u , ad_client c where u.email=? and c.id=u.ad_client_id";

    private static Logger logger= LoggerManager.getInstance().getLogger(UserUtils.class.getName());
    

    public static User getUser(String email) throws NDSException {
        User usr=null;
        Connection con=null;
        PreparedStatement pstmt= null;
        ResultSet rs= null;
        try {
            con= QueryEngine.getInstance().getConnection();
            pstmt= con.prepareStatement(GET_USER_BY_MAIL);
            pstmt.setString(1,email);
            rs= pstmt.executeQuery();
            if( rs.next()){
                usr= new User();
                usr.name=(rs.getString(1));
                usr.passwordHash=(rs.getString(2));
                usr.isActive= "Y".equals( rs.getString(3));
                usr.description=(rs.getString(4));
                usr.clientDomain=(rs.getString(5));
                usr.adClientId = rs.getInt(6);
                usr.email= rs.getString(7);
            }
        }
        catch(Exception e) {
            logger.debug("Errors found when trying to get operator from event.",e);
            throw new NDSException("@exception@", e);
            
        }finally{
            if( rs!=null){try{rs.close();}catch(Exception e){}}
            if( pstmt!=null){try{pstmt.close();}catch(Exception e){}}
            if( con!=null){try{con.close();}catch(Exception e){}}
        }
        return usr;
		
	} 
	/**
	 * Will check user information in "users" table
	 * @param vendor
	 * @param userId
	 * @return null if user not found, exception when error found (db error)
	 */
	public static User getUser(String vendor, String userId) throws NDSException {
        User usr=null;
        Connection con=null;
        PreparedStatement pstmt= null;
        ResultSet rs= null;
        try {
            con= QueryEngine.getInstance().getConnection();
            pstmt= con.prepareStatement(GET_USER);
            pstmt.setString(1,vendor);
            pstmt.setString(2,userId);
            rs= pstmt.executeQuery();
            if( rs.next()){
                usr= new User();
                usr.name=(rs.getString(1));
                usr.passwordHash=(rs.getString(2));
                usr.isActive= "Y".equals( rs.getString(3));
                usr.description=(rs.getString(4));
                usr.clientDomain=(rs.getString(5));
                usr.adClientId = rs.getInt(6);
                usr.email= rs.getString(7);
            }
        }
        catch(Exception e) {
            logger.debug("Errors found when trying to get operator from event.",e);
            throw new NDSException("@exception@", e);
        }finally{
            if( rs!=null){try{rs.close();}catch(Exception e){}}
            if( pstmt!=null){try{pstmt.close();}catch(Exception e){}}
            if( con!=null){try{con.close();}catch(Exception e){}}
        }
        return usr;
		
	}
}
