/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.Locale;

import nds.util.MessagesHolder;

/**
 * Sub system of table, current has only one level
 * @author yfzhu@agilecontrol.com
 */

public class SubSystem {
	private int id;
	private String name;
	private Integer orderno;
	private String pageURL;
	public String getDescription(Locale locale){
		if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode())
			return name;
		return MessagesHolder.getInstance().getMessage(locale, "subsystem_"+id);
	}
	
	public String getPageURL() {
		return pageURL;
	}

	public void setPageURL(String pageURL) {
		this.pageURL = pageURL;
	}

	/**
	 * @return Returns the orderno.
	 */
	public Integer getOrderno() {
		return orderno;
	}
	/**
	 * @param orderno The orderno to set.
	 */
	public void setOrderno(Integer orderno) {
		this.orderno = orderno;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
