/******************************************************************
*
*$RCSfile: TableImpl.java,v $ $Revision: 1.11 $ $Author: Administrator $ $Date: 2006/06/24 00:35:37 $
*
*$Log: TableImpl.java,v $
*Revision 1.11  2006/06/24 00:35:37  Administrator
*no message
*
*Revision 1.10  2006/03/13 01:13:32  Administrator
*no message
*
*Revision 1.9  2005/12/18 14:06:17  Administrator
*no message
*
*Revision 1.8  2005/11/16 02:57:22  Administrator
*no message
*
*Revision 1.7  2005/08/28 00:27:05  Administrator
*no message
*
*Revision 1.6  2005/05/20 23:09:12  Administrator
*no message
*
*Revision 1.5  2005/05/16 07:34:19  Administrator
*no message
*
*Revision 1.4  2005/04/27 03:25:34  Administrator
*no message
*
*Revision 1.3  2005/04/18 03:28:20  Administrator
*no message
*
*Revision 1.2  2005/03/16 09:05:13  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.7  2003/08/17 14:25:04  yfzhu
*before adv security
*
*Revision 1.6  2003/05/29 19:40:00  yfzhu
*<No Comment Entered>
*
*Revision 1.5  2003/04/03 09:28:15  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/03/30 08:11:36  yfzhu
*Updated before subtotal added
*
*Revision 1.3  2002/12/17 09:09:16  yfzhu
*no message
*
*Revision 1.2  2002/12/17 05:54:17  yfzhu
*no message
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.4  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.3  2001/11/22 06:28:18  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\TableImpl.java

package nds.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import nds.util.*;
/**
 * Note this class is not thread-safe 
 */
public class TableImpl implements Table {
    private final static String PRIMARYKEY = "id";// primary key name of table, we simplify it to universal name
    private static PairTable sysColumns;
    static{
    	sysColumns=new PairTable();
    	sysColumns.put("AD_CLIENT_ID","AD_CLIENT_ID" );
    	sysColumns.put("OWNERID","OWNERID");
    	sysColumns.put("MODIFIERID","MODIFIERID");
    	sysColumns.put("U_CLOB_ID","U_CLOB_ID");
    }
    //warning to programmer: Any time a new variable added, remind add it to clone() method
    private int id=-1;
    private String name;
    private String description;
/*    private ArrayList showableColumnsADD=new ArrayList();
    private ArrayList showableColumnsMODIFY=new ArrayList();
    private ArrayList showableColumnsPRINT=new ArrayList();
    private ArrayList showableColumnsQUERY=new ArrayList();
    private ArrayList showableColumnsOBJECTVIEW=new ArrayList();
*/    
    private ArrayList sumFields=new ArrayList();
    //private ArrayList modifiableColumns = new ArrayList();
    private ArrayList fLinks=new ArrayList();
    private ArrayList aliasTables=new ArrayList();
    private String itemTable = null;
    private String prefetchColumn = null;
    private String prefetchSql = null;

    private Column alternateKey=null;
    private Column alternateKey2=null;
    private Column dispatchColumn=null;
    private Column displayKey=null;
    private boolean shouldDispatch= false;
    private boolean hasSubTotal=false;
    private Column primaryKey=null;
    private String rowURL;
    private String rowURLTarget;//"_blank" for seperate window
    private String rowClass;
    private TableCategory category;
    private int tableOrder;
    private boolean[] actionMask;// 8 elements, means QADMSPGU by order
    private ArrayList columns=new ArrayList();
    private TriggerHolder triggers;
    //warning to programmer: Any time a new variable added, remind add it to clone() method
    private String comment;
    
    private String realTableName;
    private String filter;
    private boolean isView=false;
    private ArrayList refByTables= new ArrayList();// elements are RefByTable
    private String securityDirectory;
    private String submitProc;
    private boolean isClientIsolated=false;
    
    private int parentNodeColumnId=-1; // column contains tree parent node 
    private int summaryColumnId=-1; // column contains sign whether node is leaf 
    private boolean isActiveFilterEnabled=false; // has column "active" or not
    private Date modifiedDate=null;
    private boolean isMenuObj, isSMS,isDropdown, isBig;
    private int rowCount;
    private String maskString;
    private int uiconfigId=-1; //id of nds.web.config.ObjectUIConfig
    private int parentTableId=-1;// parent table id
    private PairTable lengendQueryList=null;
    /**
     * 当导入Excel时，最有可能导致导入错误的Unique Index，如果一张表上有多个Unique Index，
     * 将首选 索引中的字段都是可在新增时修改的 那个
     * 赋值是通过 TableManager来做到的
     */
    private String uniqueIndexName =null;
    private java.util.List uniqueIndexColumns=null; //elements are Column
    
    private List<WebAction>[] actions=null;
    private JSONObject jsonProps=null;

    public TableImpl(int id,int order,String tableName,String desc,String rowURL,String rowClass,TableCategory category, boolean[] mask, TriggerHolder trigs,String comment) {
        this.id=id;
        this.tableOrder=order;
        name=tableName;
        description=desc;
        this.rowURL=rowURL;
        this.rowClass=rowClass;
        this.category=category;
        actionMask=mask;
        this.triggers= trigs;
        this.comment=comment;
        if( actionMask==null || actionMask.length!=8) throw new IllegalArgumentException("Action mask of table "+tableName+" is not valid.");
        
    }
    public TableImpl(){
    	actionMask=new boolean[8];
    }
    public TableImpl(int id){
    	this.id=id;
    	actionMask=new boolean[8];
    }
    public void addWebAction(WebAction action){
    	int idx=action.getDisplayType().getIndex();

    	if(actions==null) actions=new ArrayList[5]; // treenode should not be here
    	if(actions[idx]==null) actions[idx]=new ArrayList<WebAction>();
    	
    	actions[idx].add(action);
    	
    }
    /**
     * Get WebAction from ad_action
     * @param dte actions of which display type
     * @return List nerver be null
     * @since 4.1
     */
    public List<WebAction> getWebActions(WebAction.DisplayTypeEnum dte){
    	if(actions ==null || actions.length< dte.getIndex() || actions[dte.getIndex()]==null)
    		return Collections.EMPTY_LIST;
    	else 
    		return actions[dte.getIndex()];
    }
    public void setTriggers(TriggerHolder th){
        triggers= th;
    }
    void setId(int id){
    	this.id=id;
    }
    /**
     * 当导入Excel时，最有可能导致导入错误的Unique Index，如果一张表上有多个Unique Index，
     * 将首选 索引中的字段都是可在新增时修改的 那个
     * 赋值是通过 TableManager来做到的
     * @param name
     * @param columns
     */
    void setUniqueIndex(String name, List columns){
    	uniqueIndexName=name;
    	uniqueIndexColumns=columns;
    }
    
    String getUniqueIndexName(){
    	return uniqueIndexName;
    }
    List getUniqueIndexColumns(){
    	return uniqueIndexColumns;
    }
    /**
     * Legend to mark table records style. 
     * For each column that has UIAlerter, will construct a legend. The returned 
     * PairTable will contain all these columns and their legends
     * @param columnMask Column.MASK_QUERY_LIST, or Column.MASK_QUERY_SUBLIST, 
     * currently only Column.MASK_QUERY_LIST implemented
     * @return PairTable may be null, key: Column, value: Legend
     */
    public PairTable getLegends(int columnMask){
    	if(columnMask != Column.MASK_QUERY_LIST) return null;
    	
    	if(lengendQueryList==null){
	    	ArrayList cols= this.getColumns(new int[]{columnMask}, false);
	    	for(int i=0;i<cols.size();i++){
	    		Column col=(Column ) cols.get(i);
	    		nds.web.alert.ColumnAlerter ca=(nds.web.alert.ColumnAlerter)col.getUIAlerter();
	    		if(ca!=null){
	    			if(lengendQueryList==null)lengendQueryList=new PairTable();
	    			lengendQueryList.put(col, ca.getLegend(col));
	    		}
	    	}
	    	if(lengendQueryList==null) lengendQueryList=PairTable.EMPTY_PAIRTABLE;
    	}
    	return lengendQueryList;
    }
    void setIsMenuObject(boolean b){ this.isMenuObj=b;}
    void setIsSMS(boolean b){ this.isSMS=b;}
    /**
     * @param mask can be combination of following chars
     *   "A" - Add
     *   "D" - Delete
     *   "M" - Modify
     *   "S" - Submit
     *   "Q" - Query
     *   "P" - UNSUBMIT
     *   "G" - Group Submit( all selected id whill be concated by comma, and send to ObjectSubmit('id1,id2')
     *   "U" - Unsubmit (@since 4.0) DEPRECATED 
     *   if action mask not found, the permission on the table will be denied
     */
    public void setMask(String maskString) {
    	for(int i=0;i< 8;i++) actionMask[i]=false;
        if(maskString ==null || "".equals(maskString.trim()))return;
        this.maskString=maskString;
        char[] c= maskString.toUpperCase().toCharArray();
        for( int i=0;i<c.length;i++){
        	
            switch(c[i]){//QADMSP
                case 'Q': actionMask[0]=true;break;
                case 'A': actionMask[1]=true;break;
                case 'D': actionMask[2]=true;break;
                case 'M': actionMask[3]=true;break;
                case 'S': actionMask[4]=true;break;
                case 'P': actionMask[5]=true;break;
                case 'G': actionMask[6]=true;break;
                case 'U': actionMask[5]=true;break;// EQUAL TO P
                default: throw new Error("Unsupported action mask:"+ maskString+"(table="+ name+")");
            }
        }
    }
	
	/**
	 * @param tableOrder The tableOrder to set.
	 */
	public void setOrder(int tableOrder) {
		this.tableOrder = tableOrder;
	}
	/**
	 * @param category The category to set.
	 */
	public void setCategory(TableCategory category) {
		this.category = category;
	}
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @param rowClass The rowClass to set.
	 */
	public void setRowClass(String rowClass) {
		this.rowClass = rowClass;
	}
	/**
	 * @param rowURL The rowURL to set. in format like:
	 * 	url:<target>
	 *  target can be "_blank" or null, if "_blank:, will show in sepereate window, else
	 *  will be inner dialog
	 */
	public void setRowURL(String r) {
		if(r!=null){
			String[] s=Pattern.compile("[:]").split(r);
			if(s.length>1){
				this.rowURL = s[0];
				this.rowURLTarget=s[1];
			}else{
				this.rowURL = r;
				this.rowURLTarget=null;
			}
		}else{
			this.rowURL = null;
			this.rowURLTarget=null;
		}
	}
	public String getRowURLTarget(){
		return rowURLTarget;
	}
	/**
	*别名表。在Column 中，有些字段为virtual, 表示该column是计算列，
	* 一般来说，计算列是当前表某字段的值和其他表（alias table）的结合产生的值
	*@return elements are nds.schema.AliasTable
	*/
	public ArrayList getAliasTables(){
		return aliasTables;
	}
	public void addAliasTable(AliasTable at){
		aliasTables.add(at);
	}
    public void addSumField(Column column){
        sumFields.add(column);
    }
    public Iterator getSumFields(){
        return sumFields.iterator();
    }
    public void addFlink(Column column){
        fLinks.add(column);
    }
    public Collection getFlinks(){
        return fLinks;
    }
    /**
     * 
     * @param column could not be PK
     * @throws NDSException
     */
    public void removeColumn(Column column) throws NDSException{
    	if( column.getId()== primaryKey.getId())
    		throw new NDSException("Could not remove PK of table ");
    	if(column.getId() == displayKey.getId()){
    		displayKey=alternateKey;
    	}
    	if(column.getId()==alternateKey.getId() ){
    		alternateKey= primaryKey;
    	}
    	columns.remove(column);
/*    	showableColumnsADD.remove(column);
    	showableColumnsMODIFY.remove(column);
    	this.showableColumnsOBJECTVIEW.remove(column);
    	this.showableColumnsPRINT.remove(column);
    	this.showableColumnsQUERY.remove(column);
*/    	
    }
    /**
     *
     * @roseuid 3B8678AF0252
     */
    public void addColumn(Column column) {
        // will judge this column's property and decide where to place
        // PK test
        if( column.getName().equalsIgnoreCase(PRIMARYKEY)) {
            if( primaryKey  !=null)
                throw new Error("Found two primary key for Table \""+name+"\":"+
                                primaryKey+"(hashcode="+System.identityHashCode(primaryKey)+"),"+column+
                                "(hashcode="+System.identityHashCode(primaryKey)+")");
            primaryKey=column;

        }
        // AK test
        if( column.isAlternateKey()) {
            if( alternateKey !=null)
                throw new Error("Found two alternate key for Table \""+name+"\":"+
                                alternateKey+","+column);
            alternateKey=column;
        }

        if( column.isAlternateKey2()) {//by Hawkins
            if( alternateKey2 !=null)
                throw new Error("Found two alternate key 2 for Table \""+name+"\":"+
                                alternateKey2+","+column);
            alternateKey2 = column;
        }

        if( column.isDisplayKey()) {
            if( displayKey !=null)
                throw new Error("Found two display key for Table \""+name+"\":"+
                		displayKey+","+column);
            displayKey=column;
        }
        // add to columns
        columns.add(column);
        // showableColumns test

        /*
         * 
               ///////////////////////////////////  ADD //////////////////////
        if( column.isShowable(Column.ADD)) {
            // make order
            int order= column.getDisplayOrder();
            boolean b=false;
            for( int i=0;i< showableColumnsADD.size();i++) {
                if(order <  ((Column)showableColumnsADD.get(i)).getDisplayOrder()) {
                    showableColumnsADD.add(i,column);
                    b=true;
                    break;
                }
            }
            if( !b)
                showableColumnsADD.add(column);
        }
        ///////////////////////////////////  MODIFY //////////////////////
        if( column.isShowable(Column.MODIFY)) {
            // make order
            int order= column.getDisplayOrder();
            boolean b=false;
            for( int i=0;i< showableColumnsMODIFY.size();i++) {
                if(order <  ((Column)showableColumnsMODIFY.get(i)).getDisplayOrder()) {
                    showableColumnsMODIFY.add(i,column);
                    b=true;
                    break;
                }
            }
            if( !b)
                showableColumnsMODIFY.add(column);
        }
        ///////////////////////////////////  QUERY //////////////////////
        if( column.isShowable(Column.QUERY_LIST)) {
            // make order
            int order= column.getDisplayOrder();
            boolean b=false;
            for( int i=0;i< showableColumnsQUERY.size();i++) {
                if(order <  ((Column)showableColumnsQUERY.get(i)).getDisplayOrder()) {
                    showableColumnsQUERY.add(i,column);
                    b=true;
                    break;
                }
            }
            if( !b)
                showableColumnsQUERY.add(column);
        }
        ///////////////////////////////////  print //////////////////////
        if( column.isShowable(Column.PRINT_LIST)) {
            // make order
            int order= column.getDisplayOrder();
            boolean b=false;
            for( int i=0;i< showableColumnsPRINT.size();i++) {
                if(order <  ((Column)showableColumnsPRINT.get(i)).getDisplayOrder()) {
                    showableColumnsPRINT.add(i,column);
                    b=true;
                    break;
                }
            }
            if( !b)
                showableColumnsPRINT.add(column);
        }
        
        if( column.isShowable(Column.QUERY_OBJECT)) {
            // make order
            int order= column.getDisplayOrder();
            boolean b=false;
            for( int i=0;i< showableColumnsOBJECTVIEW.size();i++) {
                if(order <  ((Column)showableColumnsOBJECTVIEW.get(i)).getDisplayOrder()) {
                    showableColumnsOBJECTVIEW.add(i,column);
                    b=true;
                    break;
                }
            }
            if( !b)
            	showableColumnsOBJECTVIEW.add(column);
        }
*/
        //System.out.println("now table "+ name+" has columns count "+ columns.size()+", and showable columns "+showableColumns.size());

        /*if(column.getModifiable()){
             modifiableColumns.add(column) ;
        }*/
        // enable subtotal flag if any one column has subtotal method
        if( hasSubTotal==false && column.getSubTotalMethod() !=null && column.getSubTotalMethod().trim().length() > 0) this.hasSubTotal =true;
        // check is table isolated by clients, since 2.0
        // check if ad_client is viewable, then table is viewable to all clients, since 3.0b
        if("AD_CLIENT_ID".equalsIgnoreCase(column.getName()) && !column.isShowable(Column.QUERY_LIST) ) isClientIsolated=true;
        if("ISACTIVE".equalsIgnoreCase(column.getName()) && ( column.isModifiable(Column.ADD) || column.isModifiable(Column.MODIFY)) ) this.isActiveFilterEnabled=true;
    }
    /**
     * 含义注释
     * @return String of column meaning
     */
    public String getComment(){
        return comment;
    }
    public void setComment(String s){
        comment=s;
    }
    /**
     * @roseuid 3B8AFCFC021F
     */
    public ArrayList getAllColumns() {
        return columns;
    }
    /**
     * UI Configuration, that is id of nds.web.config.ObjectUIConfig. ObjectUIConfig can be constructed 
     * using {@link nds.web.config.PortletConfigManager#getPortletConfig(int, int)}
     * @return 
     */
    public int getUIConfigId(){
    	return this.uiconfigId;
    }
    public void setUIConfigId(int uid){
    	this.uiconfigId= uid;
    }
    /**
     * @roseuid 3B8AFCFC0247
     */
    public Column getColumn(int columnID) {
        for( int i=0;i< columns.size();i++) {
            Column c=(Column) columns.get(i);
            if( c.getId() == columnID)
                return c;
        }
        return null;
    }

    public Column getColumn(String columnName) {
        for( int i=0;i< columns.size();i++) {
            Column c=(Column) columns.get(i);
            if( c.getName().equalsIgnoreCase(columnName))
                return c;
        }
        return null;
    }
    /**
     * 
     * @param action
     * @param includeUIController if false, will not add column that getDisplaySetting().isUIController()==true
     * @return
     */
    public ArrayList getShowableColumns(int action, boolean includeUIController) {
    	ArrayList c= getShowableColumns(action);
    	if(includeUIController) return c;
    	ArrayList al=new ArrayList();
    	for(int i=0;i< c.size();i++){
    		if( ((Column) c.get(i)).getDisplaySetting().isUIController()) continue;
    		al.add(c.get(i) );
    	}
    	return al;
    }
    /**
     * equals to getShowableColumns(action, true);
     */
    public ArrayList getShowableColumns(int action) {
        ArrayList c=new ArrayList();
        for(int i=0;i<columns.size();i++ ){
        	Column col=(Column)columns.get(i);
        	if( col.isShowable(action)) c.add(col);
        }
/*        switch(action) {
            case Column.ADD:
                c= showableColumnsADD;
                break;
            case Column.MODIFY:
                c=showableColumnsMODIFY;
                break;
            case Column.QUERY_LIST:
                c=showableColumnsQUERY;
                break;
            case Column.PRINT_LIST:
                c=showableColumnsPRINT;
                break;
            case Column.QUERY_OBJECT:
            	c= showableColumnsOBJECTVIEW;
            	break;
            default:
                throw new IllegalArgumentException("action is not valid:"+action);
        }*/
        return c;
    }

    /**
     * @roseuid 3B8AFCFC02BF
     */
    public Column getAlternateKey() {
        if( alternateKey==null) return primaryKey;
        return alternateKey;
    }
    
    public Column getAlternateKey2() {//by Hawkins
        return alternateKey2;
    }

    public Column getPrimaryKey() {
        return primaryKey;
    }
    public Column getDisplayKey(){
    	return displayKey==null?alternateKey:displayKey;
    }
    public int getOrder(){
        return tableOrder;
    }
    /**
     * @param showAction either Column.QUERY_SUBLIST or Column.MODIFY or Column.ADD
     */
    public ArrayList getModifiableColumns(int showAction){
    	ArrayList modifiableColumns=new ArrayList();
    	for(int i=0;i<columns.size();i++ ){
        	Column col=(Column)columns.get(i);
        	//if(  col.getModifiable() && col.isShowable(showAction)) modifiableColumns.add(col);
        	if(  col.isModifiable(showAction) && col.isShowable(showAction)) modifiableColumns.add(col);
        }
    	return modifiableColumns;
    }
    /**
     * Get columns which has any of the bit masks set in specified positions.
     * For instance, getColumns([0,3]) will return columns which
     * is showable when creation form <b>OR</b> modifiable in update form.
     * refer to Column.isMaskSet for mask information. 
     * @param columnMasks elements shoule be 0-9
     * @param includeUIControllerAndSpecialDisplayType if false, will not add column that getDisplaySetting().isUIController()==true
     *  and displaytype in {'xml','file','image'}
     * @return elements are array
     * @since 3.0
     */
    public ArrayList getColumns(int[] columnMasks ,boolean includeUIControllerAndSpecialDisplayType){
    	ArrayList cls=new ArrayList();
    	int j;
    	for(int i=0;i<columns.size();i++ ){
        	Column col=(Column)columns.get(i);
        	if(!includeUIControllerAndSpecialDisplayType){
        		if(col.getDisplaySetting().isUIController() ) continue;
        		int t=col.getDisplaySetting().getObjectType();
        		if(/*t==DisplaySetting.OBJ_CLOB || clob must be taken as common control(u_news.content)*/t==DisplaySetting.OBJ_FILE 
        				|| t==DisplaySetting.OBJ_IMAGE /*|| t==DisplaySetting.OBJ_XML*/)continue;
        	}
        	for(j=0;j< columnMasks.length;j++){
        		if(  col.isMaskSet(columnMasks[j]) ) {
        			cls.add(col);
        			break;
        		}
        	}
        }
    	return cls;    	
    }
    
    /**
     * @roseuid 3B8AFCFC02E7
     */
    public int getId() {
        return id;
    }

    /**
     * @roseuid 3B8AFCFC0306
     */
    public String getName() {
        return name;
    }

    /**
     * @roseuid 3B8AFCFC0342
     */
    public String getDescription(Locale locale) {
        if(TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode())
        	return description;
        return MessagesHolder.getInstance().getMessage(locale,
        		name.toLowerCase() );
    }

    /**
     * @roseuid 3B8AFCFC0360
     */
    public String getRowURL() {
        return rowURL;
    }

    /**
     * @roseuid 3B8AFCFC0388
     */
    public String getRowClass() {
        return rowClass;
    }

    public TableCategory getCategory() {
    	return category;
    }
    public boolean isActionEnabled(int action){
        return actionMask[action];
    }
    /**
     * 是否在新增或修改记录的时候，关联表(FK)所引用的记录必须是可用的。
     * 例如，如果表depart isAcitveEnabled=true,则在创建 employee的时候，
     * 只能选择 isactive='Y' 的记录
     * @return true if table has column "isactive"
     * @since 2.0
     */
    public boolean isAcitveFilterEnabled(){
    	return isActiveFilterEnabled;
    	
    }
    /**
     * Get trigger of specified event, the trigger is a kind of procedure
     * which will be called during execution
     * @param event currently support "AC","AM","BD" only
     * @return null or VersionedTrigger
     */
    public TriggerHolder.VersionedTrigger getTrigger(String event){
    	return triggers.getTrigger(event);
    }
    
    /**
     * Get trigger name of specified condition, the trigger is a kind of procedure
     * which will be called during execution
     * @param condition such as "AM","BM", for more, see Schema.TriggerHolder
     * @return trigger name specified in <table> <triggers><condition>
     *   <before-modify/> mapping to $Table.Name + "_BM"
     *   <after-modify/> mapping to $Table.Name + "_AM"
     * <before-delete/> mapping to $Table.Name + "_BD"
     * if trigger has its specified name, that name will be used instead.
     */
    /*public String getTriggerName(String condition){
        if ( triggers==null ) return null;
        String trigname= this.triggers.getProperty(condition);
        if( "".equals(trigname)){
            // use default trigger name, rule is: TableName+ "_" + condition
            return this.name + "_"+ condition;
        }else return trigname;
    }*/

    //////////////////////////////////////////////////
    /// override Object method
    public int hashCode() {
        return id;
    }
    public boolean equals(Object c) {
    	if (c ==null) return false;
        if( (c instanceof Table)&& ((Table)c).getId()== id)
            return true;
        return false;
    }

    public String toString() {
        return name;
    }

    public void clone(Object obj) {
        if(! (obj instanceof TableImpl))
            throw new IllegalArgumentException("Not a TableImpl");
        TableImpl ci=(TableImpl)obj;
        alternateKey=ci.alternateKey;
        alternateKey2=ci.alternateKey2;
        category=ci.category;
        columns=ci.columns;
        description=ci.description;
        id=ci.id;
        name=ci.name;
        primaryKey=ci.primaryKey;
        rowClass=ci.rowClass;
        rowURL=ci.rowURL;
        //modifiableColumns = ci.modifiableColumns ;
    }
    private void readObject(java.io.ObjectInputStream stream)throws IOException, ClassNotFoundException {
        int tid= stream.readInt();
        clone(TableManager.getInstance().getTable(tid));
    }
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeInt(id);
    }

    /**
     * by Hawke
     * getItemTable(),setItemTable(String name)
     */
    public String getItemTable(){
        return itemTable;
    }
    public void setItemTable(String name){
        this.itemTable = name;
    }
    public String getPrefetchColumn(){
        return prefetchColumn;
    }
    public void setPrefetchColumn(String columnName){
        this.prefetchColumn = columnName;
    }
    public String getPrefetchSql(){
        return prefetchSql;
    }
    public void setPrefetchSql(String sql){
        this.prefetchSql = sql;
    }

   /* @return DISPATCH_NONE 表示不需要下发
    DISPATCH_ALL 表示下发给所有的门店（也就是在expdata.customerID字段为空)
    DISPATCH_SPEC 表示下发给指定门店，门店组ID字段通过getDispatchColumn()指明的字段的当前记录的值来获得
    */
    public int getDispatchType() {
        return this.shouldDispatch? (this.dispatchColumn==null?DISPATCH_ALL:DISPATCH_SPEC):DISPATCH_NONE;
    }
    /* @return null 如果不需要下发，或下发给所有门店
    否则对应门店组ID字段，系统将根据此字段的值插入到ExpData.shopGroupID字段
    */
    public Column getDispatchColumn(){
        return dispatchColumn;
    }
    public void setDispatchColumn(Column col){
        this.shouldDispatch= true;
        dispatchColumn= col;
    }
    /**
     * 规则：如果表的名称起头是"POS"（不区分大小写），将起头的三个字母去除，取剩余部分
     * @return 下面的pos表的名称。
     */
    public String getDispatchTableName(){
        if (name.toLowerCase().startsWith("pos")){
            return name.substring(3);
        }else
            return name;
    }
    
    /**
      * if at least one column has sub-method set, return ture
      * when true, the UI will display subtotal items in page result, and inform the
      * user that full range subtotal can be viewed on the current query request.
      * @return
      */
    public boolean isSubTotalEnabled(){
        return hasSubTotal;
    }
    /**
     * Is this table's data isolated by client
     * 本系统从2.0开始支持多公司结构，如果表中有ad_client_id字段，则每家公司的用户应当
     * 被限制在该公司数据，而不能看到其他公司的数据
     * @return true if table contains column "ad_client_id"
     * @since 2.0
     */
    public boolean isAdClientIsolated(){
    	return isClientIsolated;
    }
    /**
     * The real table name, if isView()==false, that will be equal to table name
     * @since 2.0
     */
    public String getRealTableName(){
    	return realTableName;
    }
    public void setRealTableName(String rt){
    	realTableName=rt;
    	isView= !name.equalsIgnoreCase(realTableName);
    }
    /**
     * Return parent table. Parent table is for item-master style table, for instance, PO, SO.
     * When item table records are changed, system tries to update parent table record.
     * 
     * If child table does not define security directory, it will be set the same as parent table
     * 
     * Parent table are not cascaded currently.
     *  
     * @return parent table or null if not defined.
     * @since 4.0
     */
    public Table getParentTable(){
    	if(parentTableId!=-1) return TableManager.getInstance().getTable(parentTableId);
    	return null;
    }
    public int getParentTableId(){
    	return parentTableId;
    }
    public void setParentTableId(int ptId){
    	this.parentTableId= ptId;
    }
    /**
     * Check table is view or not, if is view, then the real table name can be 
     * retrieved by getRealTableName()
     * 
     * There's no nessisary that view must has filter 
     * @return true if this is view.
     * @since 2.0
     */
    public boolean isView(){
    	return isView;
    	
    }
    /**
     * @since 2.0
     */
    public boolean isTree(){
    	return parentNodeColumnId !=-1; 
    }
    /**
     * Must not call this before TableManager is initilized
     * @since 2.0
     */
    public Column getParentNodeColumn(){
    	return TableManager.getInstance().getColumn(parentNodeColumnId);
    }
    /**
     * When isTree()==true, the returned column is the sign that current record
     * is tree leaf or not
     * @return null if isTree==false
     */
    public Column getSummaryColumn(){
    	return TableManager.getInstance().getColumn(summaryColumnId);
    }
    
    /**
     * @since 2.0
     */
    public void setParentNodeColumnId(Integer colId){
    	if( colId !=null)
    		parentNodeColumnId= colId.intValue();
    }
    /**
     * @since 2.0
     */
    public void setSummaryColumnId(Integer colId){
    	if( colId !=null)
    		summaryColumnId= colId.intValue();
    }    
    /**
     * If this is view, then it can be constructed by 
     * "select <columns> from <realtablename> as <name> where <filter>"
     * note filter should has all columns linked to tablename, such as 
     * "C_V1$CrossOrder.doctype='APPLY'"
     * if tablename not set, then some column may be ambiguous.
     * 
     * Note that view can has no filter at all ( but filtered some selection)
     * @return filter of the table to construct the view
     * @since 2.0
     */
    public String getFilter(){
    	return filter;
    }
    public void setFilter(String f){
    	filter=f;
    }
    public ArrayList getRefByTables(){
    	
    	return this.refByTables;
    }
    /**
     * 
     * @return procname of the submit method, will be used
     *  for location store procedure, if null will default to
     *  TableName +"Submit"
     * @since 2.0
     */
    public String  getSubmitProcName(){
    	return this.submitProc;
    }
    
    public void setSubmitProcName(String name){
    	this.submitProc=name;
    }
    /**
     * Whether this table support SMS(short message service) 
     * @return
     */
    public boolean isSMS(){
    	return isSMS;
    }
    /**
     * Is show on menu or query list. For item tables, this should be false
     * @return
     */
    public boolean isMenuObject(){
    	return isMenuObj;
    }
    /**
     * When true, when this table is used as FK table, the main table's column will show
     * as dropdown, else, show as object query form.
     * @return
     */
    public boolean isDropdown(){
    	return isDropdown;
    }
    public void setIsDropdown(boolean b){
    	isDropdown=b;
    }
    /**
     * Whether table has big record set, if so, should not allow user to query the data without any filter
     * @return 
     */
    public boolean isBig(){
    	return isBig;
    }
    public void setIsBig(boolean b ){ isBig=b;}
    /**
     * 当前表的记录数，允许手工输入调整。也可以通过AD_PROCESS中的"统计数据库行数"来进行自动计算。
     * 行数将影响到界面上查询时，是直接显示记录列表，还是要求用户输入必要的条件后再进行查询。
     * 临界值通过portal.properties的"query.fk.list"设定，缺省为40，统计行数小于该值的将直接列表。
     * @return 当前记录数
     */
    public int getRowCount(){
    	return rowCount;
    }
    public void setRowCount(int cnt){
    	rowCount=cnt;
    }
    
    public Date getModifiedDate(){
    	return modifiedDate;
    }
    public void setModifiedDate(Date date){
    	modifiedDate= date;
    }
    public void addRefByTable(RefByTable refByTable){
    	this.refByTables.add( refByTable);
    }
    
    public String getSecurityDirectory(){
    	if(Validator.isNull(securityDirectory)){
    		Table pt = this.getParentTable();
    		if(pt!=null) return pt.getSecurityDirectory();
    		else throw new NDSRuntimeException("Table definition error: neither directory nor parent table is set for table:"+ this.name);
    	}
    	return this.securityDirectory;
    }
    public void setSecurityDirectory(String sd){
    	this.securityDirectory=sd;
    }
    
    /**
     * Elements are elements are nds.web.bean.MenuItem
     * @return
     */
    public Collection getExtendMenuItems(){
    	return Collections.EMPTY_LIST;
    }
    /**
     *  Elements are nds.web.bean.Button or String
     * @return
     */
    public Collection getExtendButtons(int objectId, Object user){
    	return Collections.EMPTY_LIST;
    }
    /**
     * Table such as M_InOutItem, C_OrderItem will allow product detail information stored
     * in m_attributedetail table, such tables set supportAttributeDetail to true  
     * @return
     * @since 3.0
     */
    public boolean supportAttributeDetail(){
    	return false;
    }
    /**
     * has column named "status"
     * @return
     * @since 4.0
     */
    /*public boolean hasStatusColumn(){
    	if(statusColumnCheck==0){
    		if(this.getColumn("status")==null) statusColumnCheck=1;
    		else statusColumnCheck=2;
    	}
    	return statusColumnCheck==2;
    }*/
    /**
     * Indexed columns
     * @return
     * @since  4.0
     */
    public ArrayList getIndexedColumns(){
        ArrayList c=new ArrayList();
        for(int i=0;i<columns.size();i++ ){
        	Column col=(Column)columns.get(i);
        	if( col.isIndexed()) c.add(col);
        }
        return c;    	
    }
    public JSONObject toJSONObject(Locale locale) throws JSONException{
    	JSONObject jo=new JSONObject();
    	jo.put("id",id);
    	jo.put("name",name);
    	jo.put("description",getDescription(locale));
    	jo.put("mask",maskString);
    	return jo;
    }
    
    /**
     * Convert to hibernate xml file, note not use for virtual table, 
     * @param wirteHead if false , will not wirte head, so can merge all
     * definition classes into one file
     * @return
     */
    public String toHibernateXML(boolean writeHead){
        StringBuffer buf=new StringBuffer();
        StringBufferWriter b= new StringBufferWriter(buf);
        if(writeHead){
        b.print("<?xml version=\"1.0\"?>");
        b.println("<!DOCTYPE hibernate-mapping PUBLIC");
        b.println("\"-//Hibernate/Hibernate Mapping DTD//EN\"");
        b.println("\"http://hibernate.sourceforge.net/hibernate-mapping-2.0.dtd\" >");
        b.println("<hibernate-mapping package=\"nds.model\">");
        }
        b.pushIndent();
        b.println("<class name=\""+ StringBeautifier.beautify(name)+"\" table=\""+ name.toUpperCase() + "\">");
        b.pushIndent();
        // id
        Column col= this.getPrimaryKey();
        b.println("<id column=\""+ col.getName().toUpperCase()+"\" name=\""+ StringBeautifier.beautify( col.getName())+"\" type=\"" + 
        		SQLTypes.getHibernateType(col.getSQLType()) +"\" >" );
        b.pushIndent();
        b.println("<generator class=\"sequence\">");
        b.pushIndent();
        b.println("<param name=\"sequence\">SEQ_"+name.toUpperCase() +"</param>");
        b.popIndent();
        b.println("</generator>");
        b.popIndent();
        b.println("</id>");
        int pkId= col.getId();
        // properties
        for (int i=0;i< this.columns.size();i++){
        	col=  (Column )columns.get(i);
            if ( col.isVirtual())continue;
            if ( col.getId()==pkId ) continue;
            b.println("<property column=\"" + col.getName().toUpperCase()+"\" length=\"" + col.getLength()+
            		"\" name=\"" + StringBeautifier.beautify(col.getName())+"\" not-null=\"" + (col.isNullable()?"false":"true")+ "\" "+
					"type=\""+SQLTypes.getHibernateType(col.getSQLType()) +"\" />" );
             
        }
		// ak to other object , must strip id
        for (int i=0;i< this.columns.size();i++){
        	col=  (Column )columns.get(i);
        	if (col.isVirtual()) continue;
            if ( col.getReferenceTable()==null) continue;
            // for ad_client_id, ownerid and modifierid, continue
            if( sysColumns.get( col.getName().toUpperCase())!=null ) continue;
            // ref column
            // add Obj to avoid duplicate confliction to property
            String n=StringBeautifier.beautify(col.getName());
            if (StringBeautifier.stripLastID(n).equals(n)){
            	n= n +"Obj";
            }else n=StringBeautifier.stripLastID(n);
            // we disallow setting object in the defition
            b.println("<many-to-one class=\"" + StringBeautifier.beautify(col.getReferenceTable().getRealTableName())+"\" "+ 
            		"name=\"" +n+"\" insert=\"false\" update=\"false\" not-null=\"" + (col.isNullable()?"false":"true")+ "\" >");
            b.pushIndent();
            b.println("<column name=\"" + col.getName().toUpperCase()+"\" />");
            b.popIndent();
            b.println("</many-to-one>");
        }
        // ref-by-tables
        ArrayList al= getRefByTables();
        TableManager manager= TableManager.getInstance();
        for(int i=0;i<al.size();i++){
        	RefByTable holder=(RefByTable) al.get(i);
        	if( holder.getAssociationType() != holder.ONE_TO_MANY) continue;
        	b.println("<set inverse=\"true\" lazy=\"true\" name=\""+ 
        			StringBeautifier.beautify(manager.getTable(holder.getTableId()).getRealTableName())+"Set\" >");
        	b.pushIndent();
        	b.println("<key column=\"" + manager.getColumn( holder.getRefByColumnId()).getName().toUpperCase()+ "\" />");
        	b.println("<one-to-many class=\"" + StringBeautifier.beautify(manager.getTable(holder.getTableId()).getRealTableName())+ "\" />");
        	b.popIndent();
        	b.println("</set>");
        		
        }
        b.popIndent();
        b.println("</class>");
        b.popIndent();
        if( writeHead){
        b.println("</hibernate-mapping>");
        }
        return buf.toString();
    }
    
    public void setAlternateKey2(String ak2){
    	ColumnImpl column= (ColumnImpl)this.getColumn(ak2);
    	if(column!=null){
    		column.setIsAlternateKey2(true);
    		alternateKey2=column;
    	}else
    		throw new NDSRuntimeException("Column "+ ak2+" not found in "+ name+" as ak2");
    }
    /**
     * Set in ad_column.props as json object
     * @return null or a valid object
     */
    public JSONObject getJSONProps(){
    	return jsonProps;
    }
    public void setJSONProps(JSONObject jo){
    	this.jsonProps= jo;
    }
        
}
