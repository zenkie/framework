package nds.web.welcome;

import javax.servlet.ServletContext;

import nds.control.web.ClientControllerWebImpl;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Configurations;
import nds.util.Director;
import nds.util.ServletContextActor;
import nds.util.WebKeys;

public class DefaultManager implements Manager,ServletContextActor{
	protected Logger logger= LoggerManager.getInstance().getLogger(this.getClass().getName());

	protected String defaultWelcomeURL=null;
	/**
	 * Welcome page
	 * @return null if no welcome page needed
	 */
	public String getWelcomePageURL(UserWebImpl user){
		return defaultWelcomeURL;
	}
    public void init(Director director) {
    }
    public void init(ServletContext context) {
        Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
        defaultWelcomeURL= conf.getProperty("portal.welcome.url");
    }
    public void destroy() {
    }
	
}
