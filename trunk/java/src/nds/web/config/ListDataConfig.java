package nds.web.config;
/**
 * ListPortlet data configurator
 * @author yfzhu
 *
 */
public class ListDataConfig implements PortletConfig {
	private int id;
	private String name;
	private int tableId;
	private String mainURL;
	private String mainTarget;
	private String filter;
	private int[] columnMasks;
	private boolean isPublic;
	private int orderbyColumnId; 
	private boolean isAscending;
	
	public int[] getColumnMasks() {
		return columnMasks;
	}
	public void setColumnMasks(String columnMasks) {
		this.columnMasks =nds.util.StringUtils.parseIntArray(columnMasks,",");
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public String getMainTarget() {
		return mainTarget;
	}
	public void setMainTarget(String mainTarget) {
		this.mainTarget = mainTarget;
	}
	public String getMainURL() {
		return mainURL;
	}
	public void setMainURL(String mainURL) {
		this.mainURL = mainURL;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	public boolean isAscending() {
		return isAscending;
	}
	public void setAscending(boolean isAscending) {
		this.isAscending = isAscending;
	}
	public int getOrderbyColumnId() {
		return orderbyColumnId;
	}
	public void setOrderbyColumnId(int orderbyColumns) {
		this.orderbyColumnId = orderbyColumns;
	}
	
}
