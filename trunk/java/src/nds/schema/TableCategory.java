/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.*;
import nds.util.FileUtils;
import nds.util.MessagesHolder;

/**
 * Tree structure category of table, current has only one level
 * @author yfzhu@agilecontrol.com
 */

public class TableCategory {
	private int id;
	private String name;
	private  SubSystem subsystem=null;
	private int orderno;
	private String pageURL;
	private String icoURL;

	private List<WebAction> actions;
	private List<Table> tables;
	private List tbacts; // WebAction and Table in order
	
	public TableCategory(){
		actions=new ArrayList<WebAction>();
		tables=new ArrayList<Table>();
		tbacts=new ArrayList();
	}
	public void sortTablesAndActions(){
		nds.util.ListSort.sort(tables, "getOrder");
		nds.util.ListSort.sort(actions, "getOrder");
		//nds.util.ListSort.sort(tbacts, "getOrder");
		
		Collections.sort(tbacts, new Comparator(){
			/**
			 * a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
			 */
			public int compare(Object o1,Object o2){
				int o1Order,o2Order;
				if(o1 instanceof Table) o1Order= ((Table)o1).getOrder();
				else o1Order=  ((WebAction)o1).getOrder();
				
				if(o2 instanceof Table) o2Order= ((Table)o2).getOrder();
				else o2Order=  ((WebAction)o2).getOrder();
				
				return o1Order-o2Order;
				
			}
		});
	}
	public String getDescription(Locale locale){
		if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode())
			return name;
		return MessagesHolder.getInstance().getMessage(locale, "tablecategory_"+id);
	}
	/**
	 *  
	 * @return elements are nds.schema.Table or nds.schema.WebAction, is not null
	 */
	public List children(){
		return tbacts;
	}
	/**
	 * 
	 * @return
	 * @since 4.1
	 */
	public List<Table> getTables(){
		return tables;
	}
	/**
	 * 
	 * @return
	 * @since 4.1
	 */
	public List<WebAction> getWebActions(){
		return actions;
	}
	public void addWebAction(WebAction action){
		actions.add(action);
		tbacts.add(action);
	}
	public void addTable(Table tb){
		tables.add(tb);
		tbacts.add(tb);
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
	public int getOrder() {
		return orderno;
	}
	/**
	 * @param orderno The orderno to set.
	 */
	public void setOrder(int orderno) {
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
	 * @return Returns the parent.
	 */
	public SubSystem getSubSystem() {
		return subsystem;
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
	/**
	 * @param parent The parent to set.
	 */
	public void setSubSystem(SubSystem ss) {
		this.subsystem= ss;
	}
	
	/**
	 * @param ico url
	 */
	public String getIcoURL() {
		return icoURL;
	}
	
	public void setIcoURL(String icoURL) {
		this.icoURL = icoURL;
	}
	/**
	 * @param ico back url
	 */
	public String getIcoURLback() {
		
		return icoURL==null?"":FileUtils.getFileNameNoEx(icoURL)+"_bk."
		+FileUtils.getExtension(icoURL);
	}
	
}
