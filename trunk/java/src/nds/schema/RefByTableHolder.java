/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

/**
 * definition like:
 * 
   <ref-by-table>
       <name>table2</name>
       <ref-by-column>table2.column</ref-by-column> <!-- this column should reftable to table1.key1, but not nessessary-->
       <filter>iscustomer='Y'</filter>
       <!-- 1 or n -->
       <association>1<association>
   </ref-by-table>
 * 
 * @author yfzhu@agilecontrol.com
 * @deprecated
 */

public class RefByTableHolder {
	private String tableName;
	private String columnName;
	private String filter;
	private String association;
	
	/**
	 * @return Returns the association. "1" or "n"
	 */
	public String getAssociation() {
		return association;
	}
	/**
	 * @param association The association to set.
	 */
	public void setAssociation(String association) {
		this.association = association;
	}
	/**
	 * @return Returns the columnName.
	 */
	public String getColumnName() {
		return columnName;
	}
	/**
	 * @param columnName The columnName to set.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	/**
	 * @return Returns the filter.
	 */
	public String getFilter() {
		return filter;
	}
	/**
	 * @param filter The filter to set.
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}
	/**
	 * @return Returns the tableName.
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName The tableName to set.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String toString(){
		StringBuffer sb=new StringBuffer();
		sb.append("table=").append(tableName).append(", column=").append(columnName)
		.append(", filter=\"").append(filter ).append("\", association=").append( association);
		return sb.toString();
	}
}
