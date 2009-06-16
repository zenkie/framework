package nds.web.welcome;

import javax.servlet.ServletContext;

import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.util.Configurations;
import nds.util.Director;
import nds.util.ServletContextActor;
import nds.util.WebKeys;
import nds.query.*;
import nds.util.*;

public class BardManager extends DefaultManager{
	/**
	 * Welcome page
	 * 用户名是'tempcust', 就要求用户进入创建用户 /prj/bard/newuser.jsp
	 * 用户是users.usertype==2 (经销商)，且未建立申请单，就要求他们填写申请单
	 * @return null if no welcome page needed
	 */
	public String getWelcomePageURL(UserWebImpl user){
		if(user==null|| user.isGuest()) return null;
		try{
			if( user.getUserName().equalsIgnoreCase("tempcust") ) return "/prj/bard/newuser.jsp";
			
			QueryEngine engine =QueryEngine.getInstance();
			int cnt=Tools.getInt(engine.doQueryOne(
					"select users_checkcust("+ user.getUserId()+") from dual"),0);
			if(cnt>0){
				return "/prj/bard/newcust.jsp";
			}else{
				return null;
			}
		}catch(Throwable t){
			logger.error("Fail to load welcome url for user:"+ user.getUserId(), t);
			return null;
		}
	
	}
	
}
