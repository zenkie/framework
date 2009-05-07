/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.portal.auth;

import java.util.Date;

import com.liferay.portal.PortalException;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class InactiveUserException extends PortalException {
	private Date activeDate;
	public InactiveUserException() {
		super();
	}

	public InactiveUserException(String msg) {
		super(msg);
	}

	public InactiveUserException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public InactiveUserException(String msg, Date d) {
		super(msg);
		activeDate=d;
	}
	public InactiveUserException(Throwable cause) {
		super(cause);
	}
	public void setActiveDate(Date d){
		activeDate=d;
	}
	public Date getActiveDate(){
		return activeDate;
	}
}
