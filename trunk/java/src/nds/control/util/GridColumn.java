/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;
import org.json.*;
import nds.schema.*;
import nds.query.*;
import nds.util.PairTable;

import java.util.Locale;
/**
 * GridColumn contains Grid column information
 * @author yfzhu@agilecontrol.com
 */

public class GridColumn implements JSONString{
	private String name;
	private String description;
	private boolean isVisible; // fk column will be invisible, while fk.ak will be visible.
	private Column col;			
	private ColumnLink columnLink; //v2 for GridColumn 
	private int type; // Column.Date|NUMBER|STRING	
	private int rTableId;
	private int objIdPos;
	private boolean isUploadWhenCreate;
	private boolean isUploadWhenModify;
	private String defaultValue;
	private Locale locale;
	private String fkQueryURL;// when column is fk (such as m_product_id), how to do search
	private boolean hideInEditMode;// controlled by embed_obj_hide of FK table
	private boolean defocus=false;//default focus set
	public String toJSONString()  {
		try{
			return toJSONObject().toString();
		}catch(Throwable t){
			return "";
		}
	}
	public JSONObject toJSONObject() throws JSONException{
		JSONObject jo=new JSONObject();
		jo.put("name",name);
		jo.put("description",description);
		jo.put("isVisible", isVisible);
		jo.put("columnId",col==null? -1: col.getId());
		if(columnLink!=null){
			jo.put("clink", columnLink.toHTMLString());
			jo.put("dsptype", columnLink.getLastColumn().getDisplaySetting().getObjectType());
		}
		if(col!=null){
			jo.put("dsptype", col.getDisplaySetting().getObjectType());
		}

		
		jo.put("isNullable", col==null?true:col.isNullable());
		if(	col!=null && col.isValueLimited()){
			jo.put("isValueLimited",true);
			jo.put("values",col.getValues(locale).toHashMap());
		}else{
			jo.put("isValueLimited",false);
		}
		
		jo.put("type",type);
		jo.put("rTableId",rTableId);
		jo.put("objIdPos",objIdPos);
		jo.put("isUploadWhenCreate",isUploadWhenCreate);
		jo.put("isUploadWhenModify",isUploadWhenModify);
		if(hideInEditMode)jo.put("hideInEditMode", hideInEditMode);
		jo.put("defaultValue",defaultValue);
		jo.put("summethod", col==null?null: col.getSubTotalMethod());
		jo.put("fkQueryURL", fkQueryURL);
		jo.put("defocus", defocus);
		return jo;
	}
	
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public boolean isUploadWhenCreate() {
		return isUploadWhenCreate;
	}
	public boolean isUploadWhenModify() {
		return isUploadWhenModify;
	}
	public void setUploadWhenCreate(boolean isUploadWhenCreate) {
		this.isUploadWhenCreate = isUploadWhenCreate;
	}
	public void setUploadWhenModify(boolean isUploadWhenModify) {
		this.isUploadWhenModify = isUploadWhenModify;
	}
	
	public int getObjIdPos() {
		return objIdPos;
	}
	
	public void setObjIdPos(int objIdPos) {
		this.objIdPos = objIdPos;
	}
	public Column getColumn() {
		return col;
	}
	public String getDescription() {
		return description;
	}
	public boolean isVisible() {
		return isVisible;
	}
	public String getName() {
		return name;
	}
	public int getReferenceTableId() {
		return rTableId;
	}
	public int getType() {
		return type;
	}
	public void setColumn(Column col) {
		this.col = col;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setReferenceTableId(int tableId) {
		rTableId = tableId;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getFkQueryURL() {
		return fkQueryURL;
	}
	public void setFkQueryURL(String fkQueryURL) {
		this.fkQueryURL = fkQueryURL;
	}
	public boolean isHideInEditMode() {
		return hideInEditMode;
	}
	public void setHideInEditMode(boolean hideInEditMode) {
		this.hideInEditMode = hideInEditMode;
	}
	public ColumnLink getColumnLink() {
		return columnLink;
	}
	public void setColumnLink(ColumnLink columnLink) {
		this.columnLink = columnLink;
	}
	public void setDefocus(boolean focus) {
		// TODO Auto-generated method stub
		this.defocus=focus;
	}
}
