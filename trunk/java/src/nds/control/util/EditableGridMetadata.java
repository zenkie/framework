/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.util;

import nds.control.web.UserWebImpl;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.*;
import nds.query.*;
import nds.util.JSONUtils;
import nds.util.MessagesHolder;
import nds.query.web.FKObjectQueryModel;
import java.util.*;

import org.json.*;
/**
 * Information for constructing editable grid. Grid columns list will be like:
 * 0        1       2       3           4   5       6           7       8
 * rowIdx	state	errmsg	jasonobj	id	column1	column1_id	column2	column3
 * 
 * @author yfzhu@agilecontrol.com
 */
public class EditableGridMetadata { 
	private static Logger logger= LoggerManager.getInstance().getLogger(EditableGridMetadata.class.getName());
	/**
	 * Columns which has any of the bit masks set in specified positions.
     * For instance, getColumns([0,3]) will return columns which
     * is showable when creation form <b>OR</b> modifiable in update form.
     * refer to Column.isMaskSet for mask information. Elements shoule be 0-9
     * 
     * 
	 */
	public final static int[] ITEM_COLUMN_MASKS=new int[]{Column.MASK_QUERY_SUBLIST,Column.MASK_CREATE_EDIT, Column.MASK_MODIFY_EDIT};
	private Table table;
	private ArrayList columns;// elements are GridColumn
	private int[] masks;  
	/**
	 * 
	 * @param table the main table whose records are shown in the grid
	 * @param locale used for construct column header
	 * @param userWeb, contains method to get default value for column, key: column name, value: 
	 * default value of that column, may be null for those not care about default value of columns
	 * @param masks Columns which has any of the bit masks set in specified positions.
     * For instance, getColumns([0,3]) will return columns which
     * is showable when creation form <b>OR</b> modifiable in update form.
     * refer to Column.isMaskSet for mask information. Elements shoule be 0-9
     * Note following columns will not be loaded:
     * 		displaytype in {'xml','file','image'}
	 */ 
	public EditableGridMetadata(Table table, Locale locale, UserWebImpl userWeb, int[] masks){
		this.table=table;
		this.columns=new ArrayList();
		this.masks= masks;
		ArrayList cls=table.getColumns(masks,false ); // nerver load displaytype in {'xml','file','image'}
		setup(locale,userWeb,cls);
	}
	/**
	 * 
	 * @param table
	 * @param locale
	 * @param userWeb
	 * @param cols column link to set in grid
	 */
	public EditableGridMetadata(Table table, Locale locale, UserWebImpl userWeb, List<ColumnLink> cols){
		this.table=table;
		this.columns=new ArrayList();
		
		setup(locale,userWeb,cols);
	}
	private void setup( Locale locale, UserWebImpl userWeb, List cls){
		Properties prefs=null;
		try{
			if(userWeb!=null)prefs=userWeb.getPreferenceValues("template."+table.getName().toLowerCase(),false,true);
		}catch(Throwable t){
			logger.error("Could not fetch user preference", t);
		}
		boolean canAdd=table.isActionEnabled(Table.ADD);
		boolean canModify=table.isActionEnabled(Table.MODIFY);
		
		columns.add(createGridColumn("rowIdx", MessagesHolder.getInstance().getMessage(locale,"rowindex"), true,null,null,Column.STRING,table.getId(),4,true,true, null,locale,null));
		columns.add(createGridColumn("state__", MessagesHolder.getInstance().getMessage(locale,"rowstate"), false,null,null,Column.STRING,-1,-1,false,false,null,locale,null));
		columns.add(createGridColumn("errmsg", MessagesHolder.getInstance().getMessage(locale,"errmsg"), true,null,null,Column.STRING,-1,-1,false,false,null,locale,null));
		columns.add(createGridColumn("jsonobj", MessagesHolder.getInstance().getMessage(locale,"jsonobj"), true,null,null,Column.STRING,-1,-1,true,true,null,locale,null));
		Column pk= table.getPrimaryKey();
		columns.add(createGridColumn(pk.getName(),"ID", false, pk,null,pk.getType(), -1, -1, false, true,null,locale,null));
		String defaultValue=null;
		FKObjectQueryModel fkQueryModel;
		String fkQueryURL;
		for(int i=0;i< cls.size();i++){
			Column col=null;
			ColumnLink clink=null;
			boolean isColumnLink=( cls.get(i) instanceof ColumnLink );
			if(isColumnLink){
				clink= ((ColumnLink)cls.get(i));
				col= clink.getLastColumn();
				isColumnLink=clink.length()>1; // should be real column link
			}else{
				col=(Column)cls.get(i);
				try{
					clink=( cls.get(i) instanceof ColumnLink )? 
						((ColumnLink)cls.get(i)): 
							new ColumnLink(new int[]{col.getId()});
				}catch(Throwable t){
					throw new nds.util.NDSRuntimeException("Fail to setup clink:"+ col);
				}
			}
			
			if(prefs!=null && userWeb!=null){
				defaultValue=userWeb.replaceVariables(prefs.getProperty(col.getName(), 
						userWeb.getUserOption(col.getName(),  col.getDefaultValue(true))));
			}
			if(isColumnLink){
				if( col.getReferenceTable() !=null) {
					Column col2=col.getReferenceTable().getAlternateKey();
					fkQueryURL=null;
					columns.add(createGridColumn(clink.toHTMLString()+"__"+ col2.getName(),clink.getDescription(locale),
							true, col,clink,col2.getType(),col.getReferenceTable().getId(), columns.size()+1,
							 false, false,defaultValue,locale,fkQueryURL));
					columns.add(createGridColumn(clink.toHTMLString()+"__ID" ,clink.getDescription(locale),
							false, col,clink, col.getType(),-1,-1, false,false,null,locale,null));
					
				}else{
					columns.add(createGridColumn(clink.toHTMLString(),clink.getDescription(locale),
						true, col,clink,col.getType(),-1, -1, 
						false, false,defaultValue,locale,null));
				}
			}else{
				if( col.getReferenceTable() !=null) {
					Column col2=col.getReferenceTable().getAlternateKey();
					if(col.isMaskSet(Column.MASK_CREATE_EDIT) && col.isMaskSet(Column.MASK_MODIFY_EDIT)){
					//@ACCEPTER@ will be replaced by specific row input id
						fkQueryModel=new FKObjectQueryModel(false,false,col.getReferenceTable(),"@ACCEPTER@",col,null);
						fkQueryModel.setQueryindex(-1);
						fkQueryURL=fkQueryModel.getButtonClickEventScript(true);
					}else{
						fkQueryURL=null;
					}
					columns.add(createGridColumn(col.getName()+"__"+ col2.getName(),col.getDescription(locale),
							true, col,clink,col2.getType(),col.getReferenceTable().getId(), columns.size()+1,
							 col.isMaskSet(Column.MASK_CREATE_EDIT), 
							 col.isMaskSet(Column.MASK_MODIFY_EDIT),defaultValue,locale,fkQueryURL));
					columns.add(createGridColumn(col.getName()+"__ID" ,col.getDescription(locale),
							false, col, clink,col.getType(),-1,-1, false,false,null,locale,null));
					
				}else{
					columns.add(createGridColumn(col.getName(),col.getDescription(locale),
						true, col,clink,col.getType(),-1, -1, 
						col.isMaskSet(Column.MASK_CREATE_EDIT), 
						col.isMaskSet(Column.MASK_MODIFY_EDIT ),defaultValue,locale,null));
				}
			}
		}
	}	
	public JSONObject toJSONObject() throws JSONException{
		JSONObject jo=new JSONObject();
		jo.put("table",table.getName());
		jo.put("ismenuobj",table.isMenuObject());
		jo.put("tableId", table.getId());
		jo.put("columns",  JSONUtils.toJSONArray(columns));
		if(masks!=null)jo.put("column_masks", JSONUtils.toJSONArrayPrimitive(masks));
		jo.put("popupitem", table.isMenuObject()); // identical to ismenuobj
		return jo;
	}
	/**
	 * 
	 * @param allowPopupItem if true, will set popupitem to true
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject(boolean allowPopupItem) throws JSONException{
		JSONObject jo=new JSONObject();
		jo.put("table",table.getName());
		jo.put("ismenuobj",table.isMenuObject());
		jo.put("tableId", table.getId());
		jo.put("columns",  JSONUtils.toJSONArray(columns));
		if(masks!=null)jo.put("column_masks", JSONUtils.toJSONArrayPrimitive(masks));
		jo.put("popupitem", allowPopupItem);
		return jo;
	}
	/**
	 * 
	 * @return elements are GridColumn
	 */
	public ArrayList getColumns(){
		return columns;
	}
	
	private GridColumn createGridColumn(String name, String desc, boolean isVisible, 
			Column col, ColumnLink clink,int type, int rTableId, int objIdPos, 
			boolean uploadWhenCreate, boolean uploadWhenModify, String defaultValue, Locale locale, String fkQueryURL){
		GridColumn c=new GridColumn();
		c.setName(name);
		c.setDescription(desc);
		c.setVisible(isVisible);
		c.setColumn(col);
		c.setColumnLink(clink);
		c.setType(type);
		c.setReferenceTableId(rTableId);
		c.setObjIdPos(objIdPos);
		c.setUploadWhenCreate(uploadWhenCreate);
		c.setUploadWhenModify(uploadWhenModify);
		c.setDefaultValue(defaultValue);
		c.setLocale(locale);
		c.setFkQueryURL(fkQueryURL);
		c.setHideInEditMode(col!=null && col.getReferenceTable()!=null && col.getReferenceTable().getJSONProps()!=null &&
					col.getReferenceTable().getJSONProps().optBoolean("embed_obj_hide",false)==true);
		return c;
	}
	/**
	 * 
	 * @return column name that is to send column data to server for creation
	 */
	public ArrayList getColumnsWhenCreate(){
		ArrayList al=new ArrayList();
		for(int i=0;i< columns.size();i++){
			GridColumn c=(GridColumn) columns.get(i);
			if(c.isUploadWhenCreate())al.add( c.getName());
		}
		return al;
	}
	/**
	 * 
	 * @return column name that is to send column data to server for modification
	 */
	public ArrayList getColumnsWhenModify(){
		ArrayList al=new ArrayList();
		for(int i=0;i< columns.size();i++){
			GridColumn c=(GridColumn) columns.get(i);
			if(c.isUploadWhenModify())al.add( c.getName());
		}
		return al;
	
	}
	
	/**
	 * 
	 * @return column name that is to send column data to server for modification
	 */
	public ArrayList getColumnsWhenDelete(){
		ArrayList al=new ArrayList();
//		al.add("rowIdx");
		al.add("ID");
		return al;
	}
}
