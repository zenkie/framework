package nds.web.config;
/**
 * ListPortlet ui configurator
 * @author yfzhu
 *
 */
public class ListUIConfig implements PortletConfig {
	private int id;
	private String name;
	private String style;
	private String cssClass;
	private boolean isShowTitle;
	private String moreStyle;
	private String titleCss;
	private String moreURL;
	private int columnCount;
	private int[] columnLength;
	private int pageSize;
	private String searchBox;
	
	
	/**
	 * How many columns, not including PK column, will be displayed on UI
	 */
	public int getColumnCount() {
		return columnCount;
	}
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}
	/**
	 * Array length should be equals to {@link #getColumnCount()}, note PK column 
	 * is always hidden to be the url of first displayed column
	 * @return
	 */
	public int[] getColumnLength() {
		return columnLength;
	}
	public void setColumnLength(int[] columnLength) {
		this.columnLength = columnLength;
	}
	public String getCssClass() {
		return cssClass;
	}
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isShowTitle() {
		return isShowTitle;
	}
	public void setShowTitle(boolean isShowTitle) {
		this.isShowTitle = isShowTitle;
	}
	public String getMoreStyle() {
		return moreStyle;
	}
	public void setMoreStyle(String moreStyle) {
		this.moreStyle = moreStyle;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getMoreURL() {
		return moreURL;
	}
	public void setMoreURL(String moreURL) {
		this.moreURL = moreURL;
	}
	public String getSearchBox() {
		return searchBox;
	}
	public void setSearchBox(String searchBox) {
		this.searchBox = searchBox;
	}
	public String getTitleCss() {
		return titleCss;
	}
	public void setTitleCss(String titleCss) {
		this.titleCss = titleCss;
	}

}
