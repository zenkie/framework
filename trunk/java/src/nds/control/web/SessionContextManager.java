/******************************************************************
*
*$RCSfile: SessionContextManager.java,v $ $Revision: 1.9 $ $Author: Administrator $ $Date: 2006/03/13 01:15:57 $
*
*$Log: SessionContextManager.java,v $
*Revision 1.9  2006/03/13 01:15:57  Administrator
*no message
*
*Revision 1.8  2006/01/07 11:45:53  Administrator
*no message
*
*Revision 1.7  2005/06/25 10:09:35  Administrator
*no message
*
*Revision 1.6  2005/06/16 10:19:18  Administrator
*no message
*
*Revision 1.5  2005/05/27 05:01:47  Administrator
*no message
*
*Revision 1.4  2005/05/16 07:34:12  Administrator
*no message
*
*Revision 1.3  2005/04/27 03:25:31  Administrator
*no message
*
*Revision 1.2  2005/04/18 03:28:16  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:15  Administrator
*init
*
*Revision 1.3  2003/09/29 07:37:21  yfzhu
*before removing entity beans
*
*Revision 1.2  2002/12/17 05:53:45  yfzhu
*no message
*
*Revision 1.3  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.2  2001/11/20 22:36:09  yfzhu
*no message
*
*Revision 1.1  2001/11/16 11:42:40  yfzhu
*no message
*
********************************************************************/

package nds.control.web;


import javax.servlet.http.HttpSession;

import nds.control.util.SecurityUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Director;
import nds.util.Manager;
import nds.util.SessionContextActor;
import nds.util.TimeLog;
import nds.util.*;
import java.sql.*;
import java.util.Locale;

import nds.query.*;
import nds.schema.TableManager;
import nds.security.User;

/**
Maintains session level context information
*/
public class SessionContextManager extends ModelUpdateNotifier
            implements  nds.util.SessionContextActor,
            java.io.Serializable
{
    private Logger logger= LoggerManager.getInstance().getLogger(SessionContextManager.class.getName());

    private Manager manager;
    public SessionContextManager() {
        manager=new Manager();
    }
    public void init(Director director) {
    }
    public String getDetailInfo(){
    		return nds.util.Tools.getDetailInfo(manager);
    }
    private Object create(String name, HttpSession session) {
        Object actor= manager.create(name, this.getClass().getClassLoader());
        if( actor instanceof SessionContextActor) {
            ((SessionContextActor) actor).init(session);
        }
        return actor;
    }
	 
    /**
     There will also have a check operation in nds.web.SessionAttributeController.
    */
    public void init(HttpSession session) {
        int tid=TimeLog.requestTimeLog("SessionContextManager.init");
        //Thread.dumpStack();
        try {
            UserWebImpl user= new UserWebImpl();
            user.init(manager);
            user.init(session);
            manager.setRole(WebKeys.USER, user);
            //logger.debug("session="+ session+", SessionContextManager="+ this);
						/**
						* yfzhu 2004-09-19 add support for portal auth
						* Liferay will add user name in session context with "j_username" if authenticated
						*/
            Object obj=session.getAttribute("USER_ID");
            // ip will be set by com\liferay\portal\action\LoginAction.java
            String ipAddr=(String)session.getAttribute("IP_ADDRESS");
			if(obj !=null && (obj instanceof String)){
				
  /**
  *  由于 lportal 的限制，名称一律设置为小写
  *  参见：com.liferay.portal.ejb.UserManagerImpl#_authenticate
  *  登录的时候由系统强行设置登录名为小写后验证
  *  // there's also a same handle method in nds.web.SessionAttributeController
  */
				String domainName=( (String )obj).toLowerCase(); //like email address
				
				String uName, adclientName ;
				int p=domainName.lastIndexOf("@");
				if ( p>0){
					 uName= domainName.substring(0,p );
					 adclientName= domainName.substring(p+1);
					 User usr= SecurityUtils.getUser(uName,adclientName);
					 
					 if(usr.getId().intValue() !=-1){
						 nds.control.util.ValueHolder holder=new nds.control.util.ValueHolder();
						 holder.put("user", usr);
						 holder.put("remote_address", ipAddr);
						 /*holder.put("id", usr.getId());
						 holder.put("active",new Boolean( usr.isActive()));
						 holder.put("name",uName);
						 holder.put("desc","");
						 holder.put("domain", adclientName);
						 holder.put("remote_address", "N/A");*/
						 user.performUpdate(holder,session);
					 }else logger.info("Could not found nds user(isactive) for " + domainName);
				}else logger.info("Looks like invalid: " + domainName);
			}else{
				logger.debug("Not found j_username in session!");
			}
			
			/*if(user.getUserId()==-1){
				//logout
				logger.debug("Logout user since invalid");
				session.invalidate();
			}*/
						
            //Object actor;
            //actor =create("nds.control.web.ClientControllerWebImpl",session);
            //manager.setRole(WebKeys.WEB_CONTROLLER,actor);

            //        actor =create("nds.control.web.UserWebImpl",session);
            //        manager.setRole(WebKeys.USER, actor);

            logger.debug("SesssionContextManager initialized, time out is "+session.getMaxInactiveInterval());
        } catch(Exception e) {
            logger.debug("Error initialize SesssionContextManager for session:"+ session,e);
        }finally{
            TimeLog.endTimeLog(tid);
        }
    }

    /**
    @roseuid 3BF2895B0318
    */
    public Object getActor(String role) {
        return manager.getActor(role);
    }

    /**
     Destroy all actors
    @roseuid 3BF30EAB0366
    */
    public void destroy() {
        manager.destroyAll();
        logger.debug("SesssionContextManager destroied");
    }
}
