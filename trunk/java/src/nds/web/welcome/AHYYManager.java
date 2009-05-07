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

public class AHYYManager extends DefaultManager{
	/**
	 * Welcome page
	 * 用户如果已经设置了u_user_quizz, 就返回空
	 * @return null if no welcome page needed
	 */
	public String getWelcomePageURL(UserWebImpl user){
		if(user==null|| user.isGuest()) return null;
		try{
			QueryEngine engine =QueryEngine.getInstance();
			int cnt=Tools.getInt(engine.doQueryOne("select count(*) from u_user_quizz where ownerid="+ user.getUserId()),0);
			if(cnt>0){
				return this.defaultWelcomeURL;
			}else{
				return "/html/nds/ahyy/setquizz.jsp";
			}
		}catch(Throwable t){
			logger.error("Fail to load welcome url for user:"+ user.getUserId(), t);
			return null;
		}
	
	}
	
}
