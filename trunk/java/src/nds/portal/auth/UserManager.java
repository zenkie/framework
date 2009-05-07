/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.portal.auth;

import java.util.*;

import com.liferay.portal.util.PropsUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class UserManager {
	private static final Log logger = LogFactory.getLog(UserManager.class);
	
	private static UserManager instance=null;
	private Hashtable ht=new Hashtable();//key: userId(String), value: Date
	private long sleepTime; // in milliseconds
	private UserManager(){
		try{
			sleepTime=1000*60* (Integer.parseInt((String) PropsUtil.get("auth.max.failures.sleeptime")));
		}catch(Exception e){
			sleepTime=1000*60*20; // 20 minutes
			logger.error("set sleep time for inactive user: 20 minutes" );
		}
	}
	public String dump(){
		return nds.util.Tools.toString(ht);
	}
	public void putInactiveUser(String userId){
		Date d= new Date(System.currentTimeMillis()+sleepTime );
		ht.put(userId, d);
		logger.debug("Put "+ userId+", activate time will be "+ d);
	}
	public Date getActiveDate(String userId){
		
		return (Date) ht.get(userId);
	}
	public void removeUser(String userId){
		ht.remove(userId);
		logger.debug( userId+" removed from inactive list.");
	}
	/**
	 * If user not inactive and during frozen time, return false
	 * @param userId
	 * @return
	 */
	public boolean isActive(String userId){
		Date date=(Date) ht.get(userId);
		if(date !=null){
			if(date.getTime()>  System.currentTimeMillis()){
				return false;
			}
		}
		return true;
	}
	public static UserManager getInstance(){
		if(instance==null){
			instance=new UserManager();
		}
		return instance;
	}
}
