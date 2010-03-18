package nds.security;
import nds.query.*;
import nds.schema.*;
import nds.util.*;
import java.util.*;
/**
 * Class for table sec_fkfilter
 * @author yfzhu
 *
 */
public class FkFilter {
	private int id;
	private int groupId;
	private int tableId;
	private String description;
	private Expression expression;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Expression getExpression() {
		return expression;
	}
	
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
}
