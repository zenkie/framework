/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package com.liferay.portal.action;

import java.util.Date;

import com.liferay.portal.PortalException;
/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class InvalidIPException extends PortalException {
	public InvalidIPException() {
		super();
	}

	public InvalidIPException(String msg) {
		super(msg);
	}

	public InvalidIPException(String msg, Throwable cause) {
		super(msg, cause);
	}
	public InvalidIPException(Throwable cause) {
		super(cause);
	}

}
