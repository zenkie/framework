package nds.web.config;

public interface PortletConfig extends java.io.Serializable{
	/**
	 * ListDataConfig, when trying to modify int value, look at table_list.jsp first
	 */
	public final static int TYPE_LIST_DATA=1;
	/**
	 * ListUIConfig
	 */
	public final static int TYPE_LIST_UI=2;
	/**
	 * ObjectUIConfig
	 */
	public final static int TYPE_OBJECT_UI=3;
	
	public int getId();
	public String getName();
}
