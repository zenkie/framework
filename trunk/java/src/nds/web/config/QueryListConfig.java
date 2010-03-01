package nds.web.config;

import nds.schema.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.util.*;

import org.json.*;
import java.util.*;


/**
 * 
 * Contains query list configuration, including selections, condition columns and order by
 * columns
 * 
 * @author yfzhu@agilecontrol.com
 *
 */
public class QueryListConfig  implements JSONString{
	private static Logger logger= LoggerManager.getInstance().getLogger(QueryListConfig.class.getName());
	
	private int id;
	private String name;
	private boolean isDefault;
	private int tableId;
	private List<ColumnLink> selections;
	private List<ColumnLink> conditions;
	private List<ColumnLink> orderBys;
	private PairTable lengends;
	
	public QueryListConfig(){}
	public QueryListConfig(String json)throws JSONException,QueryException {
		this(new JSONObject(json));
	}
	public QueryListConfig(JSONObject jo)throws JSONException,QueryException {
		this.id= jo.getInt("id");
		this.name= jo.getString("name");
		this.isDefault= jo.getBoolean("default");
		this.tableId= jo.getInt("table");
		selections=new ArrayList<ColumnLink>();
		JSONArray ja=jo.getJSONArray("selections");
		for(int i=0;i< ja.length();i++){
			try{	
				ColumnLink cl=ColumnLink.parseJSONObject(ja.getJSONObject(i));
				selections.add(cl);
			}catch(Throwable t){
				logger.error("fail to load column link as selection:"+ ja.getJSONObject(i), t);
			}
		}
		conditions=new ArrayList<ColumnLink>();
		ja=jo.getJSONArray("conditions");
		for(int i=0;i< ja.length();i++){
			try{	
				ColumnLink cl=ColumnLink.parseJSONObject(ja.getJSONObject(i));
				conditions.add(cl);
			}catch(Throwable t){
				logger.error("fail to load column link as condition:"+ ja.getJSONObject(i), t);
			}
		}
		orderBys=new ArrayList<ColumnLink>();
		ja=jo.getJSONArray("orders");
		for(int i=0;i< ja.length();i++){
			try{	
				ColumnLink cl=ColumnLink.parseJSONObject(ja.getJSONObject(i));
				orderBys.add(cl);
			}catch(Throwable t){
				logger.error("fail to load column link as orderby:"+ ja.getJSONObject(i),t);
			}
		}
		
	}
	/**
     * Legend to mark table records style. 
     * For each column that has UIAlerter, will construct a legend. The returned 
     * PairTable will contain all these columns and their legends
     * @return PairTable may be null, key: ColumnLink, value: Legend
     */
    public PairTable getLegends(){
    	
    	if(lengends==null){
	    	
	    	for(int i=0;i<selections.size();i++){
	    		ColumnLink clink= selections.get(i);;
	    		Column col=clink.getLastColumn();
	    		nds.web.alert.ColumnAlerter ca=(nds.web.alert.ColumnAlerter)col.getUIAlerter();
	    		if(ca!=null){
	    			if(lengends==null)lengends=new PairTable();
	    			lengends.put(clink, ca.getLegend(col));
	    		}
	    	}
	    	if(lengends==null) lengends=PairTable.EMPTY_PAIRTABLE;
    	}
    	return lengends;
    }
	public JSONObject toJSON() throws JSONException{
		JSONObject jo=new JSONObject();
		jo.put("id",id);
		jo.put("name", name);
		jo.put("default", isDefault);
		jo.put("table", tableId);
		jo.put("selections", JSONUtils.toJSONArray(selections));
		jo.put("conditions", JSONUtils.toJSONArray(conditions));
		jo.put("orders", JSONUtils.toJSONArray(orderBys));
		return jo;	
	}
	public String toJSONString(){
		try{
			return toJSON().toString();
		}catch(Throwable t){
			return "";
		}
	}	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isDefault() {
		return isDefault;
	}
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	public List<ColumnLink> getSelections() {
		return selections;
	}
	public void setSelections(List<ColumnLink> selections) {
		this.selections = selections;
	}
	public List<ColumnLink> getConditions() {
		return conditions;
	}
	public void setConditions(List<ColumnLink> conditions) {
		this.conditions = conditions;
	}
	public List<ColumnLink> getOrderBys() {
		return orderBys;
	}
	public void setOrderBys(List<ColumnLink> orderBys) {
		this.orderBys = orderBys;
	}

	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
}
