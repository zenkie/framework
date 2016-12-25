/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.*;

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
	private String iconURL;
	private List<TableCategory> tcs;
	private List<WebAction> actions;
	private List tcacts; // WebAction and TableCategory in order
	public SubSystem(){
		tcs=new ArrayList<TableCategory>();
		actions=new ArrayList<WebAction>();
		tcacts=new ArrayList();
	}

	/**
	 * 
	 * @return
	 * @since 4.1
	 */
	public List<TableCategory> getTableCategories(){
		return tcs;
	}
	/**
	 * 
	 * @return
	 * @since 4.1
	 */
	public List<WebAction> getWebActions(){
		return actions;
	}
	/**
	 * 
	 * @return not null,elements can be WebAction or TableCategory in order
	 */
	public List children(){
		return tcacts;
	}
	public void sortTableCategoryAndActions(){
		nds.util.ListSort.sort(tcs, "getOrder");
		nds.util.ListSort.sort(actions, "getOrder");
		//nds.util.ListSort.sort(tbacts, "getOrder");
		
		Collections.sort(tcacts, new Comparator(){
			/**
			 * a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
			 */
			public int compare(Object o1,Object o2){
				int o1Order,o2Order;
				if(o1 instanceof TableCategory) o1Order= ((TableCategory)o1).getOrder();
				else o1Order=  ((WebAction)o1).getOrder();
				
				if(o2 instanceof TableCategory) o2Order= ((TableCategory)o2).getOrder();
				else o2Order=  ((WebAction)o2).getOrder();
				
				return o1Order-o2Order;
				
			}
		});
	}
	public void addWebAction(WebAction action){
		if(action.getScript()==null) throw new nds.util.NDSRuntimeException("SubSystem webaction must have script set:"+action.getId());
		//TODO add jsonobject to webaction supply 
		/*
		if(!action.getScript().startsWith("<")){
			throw new nds.util.NDSRuntimeException("SubSystem webaction must be tree xml format(starts with '<' tag):"+action.getId());
		}*/
		actions.add(action);
		tcacts.add(action);
	}
	public void addTableCategory(TableCategory tc){
		tcs.add(tc);
		tcacts.add(tc);
	}
	
	public String getDescription(Locale locale){
		if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode())
			return name;
		return MessagesHolder.getInstance().getMessage3(locale, "subsystem_"+id,name);
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

	public String getIconURL() {
		return iconURL;
	}

	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	
}
