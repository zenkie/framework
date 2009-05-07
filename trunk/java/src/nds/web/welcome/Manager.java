package nds.web.welcome;

import nds.control.web.UserWebImpl;
/**
 * 显示用户首次登录时的欢迎页面(dialog)
 * @author yfzhu
 *
 */
public interface Manager {
	/**
	 * Welcome page
	 * @return null if no welcome page needed
	 */
	public String getWelcomePageURL(UserWebImpl user);
}
