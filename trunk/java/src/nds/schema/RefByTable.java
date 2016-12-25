/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;

import nds.log.*;
import nds.util.*;
/**
 * definition like:
 * 
   <ref-by-table>
       <name>table2</name>
       <ref-by-column>table2.column</ref-by-column> <!-- this column should reftable to table1.key1, but not nessessary-->
       <filter>iscustomer='Y'</filter>
       <!-- 1 or n , if not set, then judge by ref-by column, if the ref-by column is 
       primary key of the ref-by table, then take as 1, else take as n-->
       <association>1<association>
   </ref-by-table>
 * @author yfzhu@agilecontrol.com
 */

public class RefByTable implements Serializable {
	private static transient Logger logger=LoggerManager.getInstance().getLogger(RefByTable.class.getName()); 
	/**
	 * Like BParnter vs Vendor
	 */
	public final static int ONE_TO_ONE=1;
	/**
	 * Like Order vs Order Lines
	 */
	public final static int ONE_TO_MANY=2;
	private int masterTableId; // master table of the refby table 
	private int assocType;
	private int tableId; // ref-table
	private int columnId; // ref-table's column
	private String filter;
	private String description; // added 2005-11-16
	private int id;// id of the RefByTable, can be used to identify in main table which tab is selected
	private String descKey;
	private boolean isBundledWhenCopy;
	private String inlineMode;//if "Y", will has edit pane under list, for table that has many columns,
							   //set this param to "N" for clean ui, and "B" only display product_id and
							  // qty column (for burgeon only)
	/**
	 * If true, will allow refby table to popup from current object window.
	 * 
	 * By default, whether poupup or not is checked over refby table' isMenuObject property.
	 */
	private boolean allowPopup;
	
	/**
	 * Create a fake ref-by-table of the main table. This constructor
	 * is used for UI to construct universal tabs of the main table
	 * This ref-by-table is the main table itself, with id=-1; 
	 * @param mainTable the primary table
	 */
	public RefByTable(Table mainTable){
		masterTableId=mainTable.getId();
		tableId= mainTable.getId();
		columnId= mainTable.getPrimaryKey().getId();
		filter=null;
		
		descKey= mainTable.getName().toLowerCase();
		assocType=ONE_TO_ONE;
		id=-1;
		isBundledWhenCopy=false;
		inlineMode="N";
		allowPopup=false;
	}
	/**
	 * Will check for condition that refColumn is same type as main table's pk
	 * @param recordId id of the RefByTable
	 * @param table the main table 
	 * @param refTable the refby table
	 * @param refColumn the column in refby table, if null, will try to find in refTable
	 * 's all columns whose ref-table referres to the main table, and that column has only
	 * one.
	 * @param filter filter string such like "isvendor='y'"
	 * @param association only "1" or "n" allowed
	 * @param inlineMode 
	 * "Y", will has edit pane under list, for table that has many columns,
	 * "N" for clean ui, whether popup or not is decided by refby table' isMenuObject property
	 * "B" for special mode that only product_id and number will display
	 * "NP" for clean ui and popup, 
	 * "NS" for clean ui and not allow popup
	 * 	
	 * be displayed
	 */
	public RefByTable(int recordId, Table table, Table refTable, Column refColumn, String filter, 
			String association, String desc, String inlineMode){
		masterTableId=table.getId();
		int pkId= table.getPrimaryKey().getId();
		if(refColumn ==null){
			int cnt=0;
			for(Iterator it=refTable.getAllColumns().iterator();it.hasNext();){
				Column col =(Column )it.next();
				Column cr=col.getReferenceColumn();
				if((cr !=null) &&cr.getId()== pkId){
					if(cnt>0) throw new Error("Could at least 2 columns refer to main table " + table + " in ref-by-table " + refTable);
					refColumn=col;
				}
				
			}
			if ( refColumn == null) throw new Error(" ref-by-cloumn not set for main table " + table + " in ref-by-table " + refTable);
		}
		if ( refColumn.getType()!= table.getPrimaryKey().getType()){
			throw new Error(" ref-by-cloumn type error for main table " + table + " in ref-by-table " + refTable);
		}
		tableId= refTable.getId();
		columnId= refColumn.getId();
		this.filter=filter;
		this.description=desc;
		/**
		 * regulate mode setting, default 'N'
		 */
		this.inlineMode="N";
		this.allowPopup=false;
		if(inlineMode!=null){
			if(inlineMode.length()==2){
				this.inlineMode=inlineMode.substring(0,1);
				this.allowPopup= "P".equals(inlineMode.substring(1,2));
			}else{
				// 1, so should decide allowPopup
				this.inlineMode=inlineMode;
				if("N".equals(inlineMode))this.allowPopup=refTable.isMenuObject(); 
			}
		}else{
			this.allowPopup=refTable.isMenuObject();
		}
		
		/*if(!("Y".equals(inlineMode)|| "B".equals(inlineMode))) this.inlineMode="N";
		else this.inlineMode= inlineMode;*/
		
		if( Validator.isNull(desc) )
			descKey= refTable.getName();
		else
			descKey=  "refby_"+table.getName()+"_"+ refColumn.toString();
		descKey = descKey.toLowerCase();
		if ("1".equals(association)) assocType= ONE_TO_ONE;
		else if("n".equals(association))assocType=ONE_TO_MANY;
		else{
			// guess association from ref-by column, if it's primary key, take as ONE_TO_ONE,
			// else take as ONE_TO_MANY
			if ( columnId == refTable.getPrimaryKey().getId()) assocType= ONE_TO_ONE;
			else assocType=ONE_TO_MANY;
			//throw new Error(table.getName()+" association unknown for " + refColumn+" :" + association);
		}
		/**
		 * Id can not be -1, which will be taken as main table object(the first tab)
		 */
		id=recordId;//Sequences.getNextID("ref-by-table");
		isBundledWhenCopy= refTable.getName().equals(table.getName() +"ITEM")||
		refTable.getName().equals(table.getName() +"DT")||
		refTable.getName().equals(table.getName() +"LINE");
	}
	public boolean isInlineMode(){
		return !"N".equals(inlineMode); 
	}
	/**
	 * @return true if refby table allows for inline edit
	 */
	public String getInlineMode(){
		return inlineMode;
	}
	/**
	 * The identifier of this ref-by-table,
	 * can be used to identify in main table which tab is selected
	 * @return
	 */
	public int getId(){ return id;}
	/**
	 * 
	 * @return Master table's id
	 */
	public int getMasterTableId(){
		return masterTableId;
	}
	/**
	 * Ref-by-table, not the main table
	 * @return
	 */
	public int getTableId(){
		return tableId;
	}
	/**
	 * Column in Ref-by-table, which must refer to main table's PK
	 * @return
	 */
	public int getRefByColumnId(){
		return columnId;
	}
	/**
	 * Allow refby table records to popup from list table when doublc click or not
	 * @return
	 */
	public boolean allowPopup(){
		return this.allowPopup;
	}
	/**
	 * Filter string such like "isvendor='y'", only when the object in main 
	 * table suit to the filter, will the tab displayed.
	 * 
	 * For instance, a bpartner will not show "vendor" tab if that partner 
	 * has "isvendor" set to 'N'
	 * @return
	 */
	public String getFilter(){
		return filter;
	}
	/**
	 * Description on tab for the ref-table
	 * @return
	 */
	public String getDescription(Locale locale){
		if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode()){
			if( description!=null) return description;
			return TableManager.getInstance().getTable(tableId).getDescription(locale);
		}else{
			if( description!=null) return  MessagesHolder.getInstance().getMessage3(locale, descKey,description);
			return TableManager.getInstance().getTable(tableId).getDescription(locale);
		}
	}
	/**
	 * Each object in main table can has one or more records in ref-by-table
	 * @return ONE_TO_ONE or ONE_TO_MANY
	 */
	public int getAssociationType(){
		return assocType;
	}
	/**
	 * Check is refby table records should also be copied when main table record is been copied
	 * 例如，当单据表头被复制的时候，是否明细也应该被复制，并设置到表头对应的字段
	 * 当前的实现是通过判断从表是否以Item 结尾来进行，更为通用的方式是通过在ad_refbytable 上增加判断字段来允许用户配置
	 * @return true if rebytable is ended with "item" of main table name
	 */
	public boolean isBundledWhenCopy(){
		return isBundledWhenCopy;
	}
	
	public String toString(){
		TableManager tm= TableManager.getInstance();
		if(!tm.isInitialized()) return "table= "+ tableId+ ", column="+ columnId+ ", filter=\""+ filter+"\", association="+ this.assocType;
		return  "table= "+ tm.getTable(tableId)+ ", column="+ tm.getColumn(columnId)+ ", filter=\""+ filter+"\", association="+ this.assocType;
		
	}
}
