/******************************************************************
*
*$RCSfile: TableManager.java,v $ $Revision: 1.7 $ $Author: Administrator $ $Date: 2006/03/13 01:13:32 $
*

*
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\TableManager.java

package nds.schema;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import nds.control.util.EJBUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.*;
import nds.web.action.WebActionUtils;
/**
 * There have only one public instance of TableManager
 * But when reloading, we have two. Only when we successfully load the schema from db, will the
 * public one be replaced.
 */
public class TableManager implements SchemaConstants,java.io.Serializable , nds.util.DestroyListener {
    private static Logger logger=LoggerManager.getInstance().getLogger(TableManager.class.getName());
    /**
     * Table name for date table, which is referenced by datenumber type columns
     */
    private final static String DATE_TABLE="T_DAY"; // this must be uppercase
    /**
     * This is public instance
     */
    private static TableManager instance=null;
    /**
     * private instance used only for loading, if failed, will not affect public instance
     * @serialData 2007-08-06
     * @since 3.0
     */
    private static TableManager tmpInstance=null;
    
    private Dictionary dict;
    private Properties props;
    private CollectionValueHashtable fkColumns;//key: PK columnId(Integer),value: Columns(Collection) that referece to that pk
    private ArrayList tableCategories;  //elements are TableCategory
    private ArrayList subSystems;  //elements are SubSystem
    private boolean isInitializing;
    private Locale defaultLocale= Locale.getDefault();
    /**
     * Has initialization been performed yet ?
     */
    private boolean isInitialized = false;
    /**
     * T_Day
     */
    private Table dateTable;
    /**
     * key: tableID(integer)
     * value:table(Table)
     */
    private Hashtable tableIDs;

    /**
     * key:tableName(String)
     * value:table(Table)
     */
    private Hashtable tableNames;

    /**
     * key:column id(Integer)
     * value:Column
     */
    private Hashtable columnIDs;

    /**
     * key: tablename.columnName(String) 
     * value:Column
     */
    private Hashtable columnNames;

    private Vector tableList;// element: Table
    /**
     * Key: table.id (Integer)
     * Value:Column in table which refere to Parent table
     * 
     */
    private Hashtable parentTables;
    /**
     * Key: real table name (String)
     * Value: List elements are Table (may be empty, see LasyList ) whose realtable_id is set to the real table.
     * 2008-05-03 for Table that not is menuObject, will not take as view
     */
    public Hashtable views;
    
    /**
     * Key: Ad_Action.id 
     * Value: WebAction
     */
    private Hashtable webActions;
    /**
     * TableManager's name
     */
    private String name;
    /**
     * Construct an instance
     */
    private TableManager() {
        
        //nds.util.LicenseManager.validateLicense("Agile ERP","2.0",  EJBUtils.getApplicationConfigurations().getProperty("license","/license.xml") );
    	name="TM"+ Sequences.getNextID("nds.schema.TableManager");
        tableIDs=new Hashtable(60,0.1f);
        tableNames=new Hashtable(60,0.1f);
        columnIDs=new Hashtable(300,0.1f);
        columnNames=new Hashtable(300,0.1f);
        views = new Hashtable(300,0.1f);
        dict=new Dictionary();
        fkColumns=new CollectionValueHashtable();
        tableList=new Vector();
        parentTables=new Hashtable(20,0.1f);
        webActions=new Hashtable();
        props= new Properties();
        logger.debug("New instance of TableManager created:"+ name);
        
    }
    
    /**
	 * By order specified in ad_tablecategory
	 * @return elements are TableCategory
	 */
	public ArrayList getTableCategories(){
		return this.tableCategories;
	}
	public TableCategory getTableCategory(int id){
		for(int i=0;i< tableCategories.size();i++)
			if ( ((TableCategory)tableCategories.get(i)).getId()==id) return ((TableCategory)tableCategories.get(i));
		return null;
	}
    /**
	 * By order specified in SubSystem
	 * @return elements are SubSystem
	 */
	public ArrayList getSubSystems(){
		return this.subSystems;
	}
	public SubSystem getSubSystem(int id){
		for(int i=0;i< subSystems.size();i++)
			if ( ((SubSystem)subSystems.get(i)).getId()==id) return ((SubSystem)subSystems.get(i));
		return null;
	}
	
	/**
	 * Return column interpreter if found
	 * @param columnId
	 * @return null if not found 
	 */
    public ColumnInterpreter getColumnInterpreter(int columnId){
    	return dict.getColumnInterpreter(columnId);
    }
    /**
     * @return description of value if column.getValue()!=null || column.getValueInterpeter() !=nll
     * else return ""+value;
     */
    public String getColumnValueDescription(int columnId, Object value,Locale locale) {
        // adapter pattern
        return dict.getDescription(columnId,value,locale);
    }
    public String getColumnValueByDescription(int columnId, String desc, Locale locale){
        return dict.getValue( getColumn(columnId), desc,locale);
    }
    /**
     * Get column value by check data in sequence of value first, desc second
     * @param columnId
     * @param valueOrDesc
     * @param locale
     * @return null if input is neither value nor description of specified column
     * @since 3.0
     */    
    public String getColumnValueByValueOrDesc(int columnId, String valueOrDesc, Locale locale){
        return dict.getColumnValueByValueOrDesc( columnId, valueOrDesc,locale);
    }
    /**
     * Remove all data
     */
    private void clearAll() {
        tableIDs.clear();
        tableNames.clear();
        columnIDs.clear();
        columnNames.clear();
        fkColumns.clear();
        tableList.clear();
        parentTables.clear();
        views.clear();
        webActions.clear();
        dateTable=null;
        /**
         * Clear limit value alerter cache
         */
        nds.web.alert.AlerterManager.getInstance().clear();
        
    }
    // make table list order by table.getName
    private void sortTableList() {
        // this is desc
        nds.util.ListSort.sort(tableList);
    }
    
    private Iterator getTablesFromDB(){
    	logger.debug("Begin setup TableManager using database");
    	Iterator it=null;
        try {
            DBSchemaLoader loader=new DBSchemaLoader();
            loader.setup(1);
            it=loader.getTables();
            tableCategories = loader.getTableCategories();
            subSystems= loader.getSubSystems();
            
        } catch(Exception e) {
            logger.error("Could not get schema structure from db",e);
            throw new NDSRuntimeException("Internal Error, schema could not load from db.", e);
        }
        return it;
    }
    /**
     * Add table categories to subsystem
     *
     */
    private void initSubSystems(){
    	for(int i=0;i<tableCategories.size();i++){
    		TableCategory tc=(TableCategory)tableCategories.get(i);
    		tc.getSubSystem().addTableCategory(tc);
    	}
    	for(int i=0;i< this.subSystems.size();i++)
    		((SubSystem)subSystems.get(i)).sortTableCategoryAndActions();
    }
    /**
     * Add tables to category
     * Since this contains sorting method over WebAction, call this after #initActions
     */
    private void initTableCategories(){
    	for(int i=0;i<tableList.size();i++){
    		Table t=(Table)tableList.get(i);
    		t.getCategory().addTable(t);
    	}
    	for(int i=0;i<tableCategories.size();i++){
    		TableCategory tc=(TableCategory)tableCategories.get(i);
    		tc.sortTablesAndActions();
    	}
    }
    /**
     * Load actions from db and update memory table/category
     * Must be called after tables are initialized
     */
    private void initActions(){
    	try{
	    	List<WebAction> list=WebActionUtils.loadActions();
	    	for(int i=0;i<list.size();i++){
	    		WebAction a=list.get(i);
	    		if(a.getSubSystemId()!=-1){
	    			SubSystem ss=getSubSystem(a.getSubSystemId());
	    			if(ss==null) throw new NDSException("Web action id="+ a.getId()+" has subsystem id="+a.getSubSystemId()+" not loaded in memory");
	    			ss.addWebAction(a);
	    		}else
	    		if(a.getTableCategoryId()!=-1){
	    			TableCategory tc= getTableCategory(a.getTableCategoryId());
	    			if(tc==null) throw new NDSException("Web action id="+ a.getId()+" has tablecategory id="+a.getTableCategoryId()+" not loaded in memory");	    			
	    			tc.addWebAction(a);
	    		}else
	    		if(a.getTableId()!=-1){
	    			TableImpl t=(TableImpl)tableIDs.get(new Integer(a.getTableId()));
	    			if(t==null) throw new NDSException("Web action id="+ a.getId()+" has table id="+a.getTableId()+" not loaded in memory");
	    			t.addWebAction(a);
	    		}else{
	    			throw new NDSException("Web action id="+ a.getId()+" does not combined with any of subsystem,tablecategory or table");
	    		}
	    		webActions.put( a.getId(), a);
	    	}
    	}catch(Throwable t){
    		logger.error("Fail to load actions from db",t);
    		throw new NDSRuntimeException("Internal Error, some actions could not load from db.", t);
    	}
    }
    /**
     * 
     * @param it elements are Table
     */
    private void setup(Iterator it) {
        

        while(it.hasNext()) {
            Table table=(Table)it.next();
            String tableName= table.getName().toUpperCase();
            // add to tableIDs and tableNames
            tableIDs.put(new Integer(table.getId()), table);
            tableNames.put(tableName,table);
            tableList.add(table);
            ArrayList al=table.getAllColumns();
            for(int i=0;i< al.size();i++) {
                Column col=(Column)al.get(i);
                columnIDs.put(new Integer(col.getId()), col);
                columnNames.put(tableName+"."+col.getName().toUpperCase(),col);
            }
        }
        initReferedColumns();
        sortTableList();
        initTableRfColumns();
        initViews(); // must be init before initParentTables, which will use view hashtable
        initParentTables();
        dict.init(this);
        initActions();
        initSubSystems();
        initTableCategories();
    	/*
    	 * 建立时间表 T_DAY
    	 */
    	dateTable=(Table) tableNames.get(DATE_TABLE);
    	if(dateTable==null){
    		logger.warning(DATE_TABLE + " was not found in table list");
    		//throw new NDSRuntimeException(DATE_TABLE + " was not found in table list");
    	}
    	//this one must be behinde initReferedColumns
    	initAutoCompleteTables();
        
    }
    /**
     * If a table has jsonProp "autocomplete" set to true, will update all fk column IsAutoComplete to true
     *
     */
    private void initAutoCompleteTables(){
    	for(int i=0;i<tableList.size();i++){
    		Table table= (Table) tableList.elementAt(i);
    		if(table.getJSONProps()!=null && table.getJSONProps().optBoolean("autocomplete", false)){
    			Collection c=this.fkColumns.get(Integer.valueOf( table.getPrimaryKey().getId()));
    			if(c!=null)for(Iterator it=c.iterator();it.hasNext();){
    				ColumnImpl col= (ColumnImpl)it.next();
    				col.setIsAutoComplete(true);
    			}
    		}
    	}    	
    }
    public void removeTable(Table table){
    	String tableName= table.getName().toUpperCase();
    	tableIDs.remove(new Integer(table.getId()));
    	tableNames.remove(tableName);
    	tableList.remove(table);
    	ArrayList al=table.getAllColumns();
        for(int i=0;i< al.size();i++) {
            Column col=(Column)al.get(i);
            columnIDs.remove(new Integer(col.getId()));
            columnNames.remove(tableName+"."+col.getName().toUpperCase());
        }
        dict.init(this);
        initParentTables();    	
    }
    public void removeColumn(Column col){
    	
    }
    public void putColumn(Column col){
    	columnIDs.put(new Integer(col.getId()), col);
    	columnNames.put((col.getTable().getName()+"."+col.getName()).toUpperCase(),col);
    	dict.init(this);
    }
    public void putTable(Table table){
        String tableName= table.getName().toUpperCase();
        // add to tableIDs and tableNames
        tableIDs.put(new Integer(table.getId()), table);
        tableNames.put(tableName,table);
        // remove all columns
        tableList.removeElement(table);
        tableList.addElement(table);
        ArrayList al=table.getAllColumns();
        for(int i=0;i< al.size();i++) {
            Column col=(Column)al.get(i);
            columnIDs.put(new Integer(col.getId()), col);
            columnNames.put(tableName+"."+col.getName().toUpperCase(),col);
        }
        dict.init(this);
        sortTableList();
        initParentTables();
    }

    /**
     * In schemastructure, the columns' reference column only defined by
     * columnname(String), and could not set using setReferenceColumn,
     * we will set here.
     *
     */
    private void initTableRfColumns(){
    	for(int i=0;i<tableList.size();i++){
    		Table table= (Table) tableList.elementAt(i);
    		ArrayList al=table.getAllColumns();
			for(int j=0;j< al.size();j++){
				Column col= (Column)al.get(j);
				if( col.getReferenceTable()!=null && 
						col.getReferenceColumnName()!=null){
					// found the column should has reference column set
					Column rcol=(Column) columnNames.get(col.getReferenceTable().getName().toUpperCase()+"."+col.getReferenceColumnName().toUpperCase());
					if( rcol ==null) throw new Error("The reference column ('"+col.getReferenceColumnName()+"'  not found in table "+ table);
					((ColumnImpl) col).setReferenceColumn(rcol);
				}
			}
    	}
    }
    /**
     * If table has parent table set, then it must have at least one fk column refering to parent table
     * if more that one columns refer to that parent, only first one is taken as that fk column.
     * 
     * If no columns found, error will be thrown.
     */
    private void initParentTables(){
    	parentTables.clear();
    	boolean parentColumnFound;
    	for(int i=0;i<tableList.size();i++){
    		TableImpl table=(TableImpl) tableList.elementAt(i);
    		if(table.getParentTableId()!=-1){
    			Table parentTable=(Table) this.tableIDs.get(new Integer(table.getParentTableId()));
    			if(parentTable!=null){
    				// try every column, and stop at first column which reference to parent table or its views
    				int realParentTableId=((Table) this.tableNames.get( parentTable.getRealTableName())).getId();
    				parentColumnFound=false;
    				List cols= table.getAllColumns();
    				for(int j=0;j<cols.size();j++){
    					Column col= (Column) cols.get(j);
    					Table rt=col.getReferenceTable();
    					if(rt!=null){
    						int realTableId=((Table) this.tableNames.get( rt.getRealTableName())).getId();
    						if(realTableId== realParentTableId){
    							parentTables.put(new Integer(table.getId()),col);
    							parentColumnFound=true;
    							break;
    						}
    					}
    				}
    				if(!parentColumnFound){
    					//如果指明了父表，则必须有至少一个字段指向父表或父表的视图，否则为异常
    					throw new nds.util.NDSRuntimeException("Table "+ table.getName()+" has parent set ("+ parentTable.getName()+") yet no column refers to that");
    				}
    			}
    		}
    	}	
    }
    /**
     *
     * @return null if parent table not found
     * @see nds.schema.Table#getParentTable()
     */
    public Table getParentTable(Table table){
    	return table.getParentTable();
    }
    /**
     * 将table中第一个指向ParentTable 或其 虚拟表的fk字段作为 ParentFKColumn
     * @param table
     * @return null if Parent table is not set,or no column refere to parent table or its views
     */
    public Column getParentFKColumn(Table table){
    	return (Column)parentTables.get(new Integer(table.getId()));
    }
    
    /**
     * Get all tables who has a FK to the specified columnId.
     * 例如，如果要查userId的引用表，则employee, group, 还有所有的安全对象都会被选出
     * @return will nerver be null, if no tables found, return a no element Iterator
     * Note: this is primary used for nds.query.web.TableQueryModel
     */
    public Iterator getColumnsBeingReferred(int columnId) {
        Collection c= fkColumns.get(new Integer(columnId));
        if( c==null)
            c=new ArrayList();
//        logger.debug("find Columns size:"+ c.size());
        return c.iterator();
    }
    private void initReferedColumns() {
    	fkColumns.clear();
        for(Iterator it=columnIDs.values().iterator();it.hasNext();) {
            Column col=(Column) it.next();
            if( col.getReferenceTable() !=null) {
                Column pk= col.getReferenceTable().getPrimaryKey();
                if(pk==null) throw new nds.util.NDSRuntimeException("Could not get column fk info:"+ col);
                fkColumns.add( new Integer(pk.getId()), col);
                //logger.debug("Column "+ pk + " is FK of "+col);
            }
        }
    }

    /**
     * @roseuid 3B845B9303C7
     */
    public Column getColumn(int columnID) {
    	checkInit();
        return (Column)columnIDs.get(new Integer(columnID));
    }

    /**
     * @roseuid 3B845BAD01BC
     */
    public Table getTable(int tableID) {
    	checkInit();
        return (Table)tableIDs.get(new Integer(tableID));
    }
    /**
     * Get all tables whose realTable is the same as <param>tableId</param>
     * @param tableId
     * @param includeMe if true, the <param>tableId</param> will be included
     * @return elements are Table, not include those who's not menuObject
     */
    public List getViews(int tableId, boolean includeMe){
    	checkInit();
    	Table tb= getTable(tableId);
    	String realTableName =  tb.getRealTableName().toUpperCase();
    	Object lazylist = views.get(realTableName);
    	ArrayList list=new ArrayList( LazyList.getList(lazylist));
    	if(!includeMe) list.remove(tb); 	
   		return  list;
    }
    /**
     * Init real table name and all it's children tables
     * 
     */
    private void initViews(){
    	Table tb2;
    	String realTableName;
    	for(int i=0;i< tableList.size();i++){
    		tb2= (Table)tableList.elementAt(i);
    		if(!tb2.isMenuObject()) continue;
    		realTableName = tb2.getRealTableName().toUpperCase();
    		Object lazylist= views.get(realTableName);
    		lazylist= LazyList.add(lazylist,tb2);
    		views.put( realTableName,lazylist);
    	}    	
    }
    private void checkInit(){
        if( !isInitialized)
            throw new RuntimeException("TableManager not ininitialized yet, call init() before this method");
    }
    /**
     * 时间表用于所有datenumber类型的字段的外键关联表，系统中为T_DAY，这是张client无关的表
     * @return
     */
    public Table getDateTable(){
    	return dateTable;
    }
    /**
     * Note all table name is in upper case
     * @roseuid 3B845EA302B6
     */
    public Table getTable(String name) {
    	checkInit();
        if( name==null)
            return null;
        return (Table)tableNames.get(name.toUpperCase());
    }

    /**
     * 检索获得class对应的表，不存在则返回null
     * @roseuid 3B84698E02BF
     */
    public Table getTable(Class cls) {
        String clsname=cls.getName();
        Iterator it= tableIDs.values().iterator();
        while(it.hasNext()) {
            Table table= (Table)it.next();
            if( clsname.equals(table.getRowClass()))
                return table;
        }
        return null;
    }

    /**
     * 主要提供给查找的poral 页面
     * @roseuid 3B8501C20000
     */
    public Collection getAllTables() {
        return tableList;
    }
    /**
     * @param action can be of following variables:
     *      Table#ADD, Table#MODIFY, Table#DELETE, Table#SUBMIT, Table#QUERY
     */
    public Collection getTables(int action) {
        ArrayList a=new ArrayList();
        for(int i=0;i< tableList.size();i++){
            Table table= (Table)tableList.elementAt(i);
            if( table.isActionEnabled(action)){
                a.add(table);
            }

        }
        return a;
    }
    
    public Column getColumn(String tableNameDOTcolumnName){
    	return (Column)columnNames.get(tableNameDOTcolumnName.toUpperCase());
    }
    /**
     * Note that all tables and columns stored are upper case
     * @roseuid 3B845EC10331
     */
    public Column getColumn(String tableName, String columnName) {
    	checkInit();
        if( tableName ==null || columnName ==null)
            return null;
        tableName= tableName.toUpperCase();
        columnName= columnName.toUpperCase();
        return (Column)columnNames.get(tableName+"."+columnName);
    }

    public ArrayList getReferedColumns(int tableId, int hier, Hashtable ht) {
        ReferTable rf=new ReferTable(this,ht);
        return rf.getReferedColumns(tableId, hier);
    }
    public Locale getDefaultLocale(){
    	return defaultLocale;
    }
    /**
     * @return true if the logging system has already been initialized.
     */
    public boolean isInitialized() {
        return this.isInitialized;
    }
    /**
     * @param props including following properties:<br>
     *      "directory" - the absolute file path of table configurations
     *      "defaultConverter" - TypeConverter class name
     *      "store" - "file" to use files in path specified by 'directory'
     *                "db" to read from database tables "ad_table"
     * 	    "defaultLocale" - locale of default schema description, such as for Column/Table
     */
    public void init(Properties props) {
        init(props, false);
    }

    /**
     * Initialize the table system. Need to be called once before calling
     * <code>getTable()</code>.
     *
     * @param props including following properties:<br>
     *      "directory" - the absolute file path of table configurations <br>
     *      "defaultTypeConverter" - TypeConverter class name( in fact, this is used for TypeConverterFactory )<br>
     *      if null, will use previous properties set.
     *  @param forceInit if true, the table system should be initialized no matter
     *        it has already be initialized or not
     */
    public synchronized void  init(Properties props, boolean forceInit) {
        // If logging system already initialized, do nothing
        if (isInitialized() && (forceInit ==false) ) {
            return;
        }
        if (isInitializing) throw new RuntimeException("Schema is initializing, please wait");
        try{
        	isInitializing=true;
	        if(props !=null) this.props.putAll(props);
	        // init TypeConverterFactory
	        TypeConverterFactory.getInstance().init(this.props);
	        clearAll();
	        Iterator it;
	        defaultLocale= Tools.getLocale(this.props.getProperty("defaultLocale","zh_CN"));
	        boolean isDebugMode= "true".equalsIgnoreCase(this.props.getProperty("modify","true"));
        	// read from db
        	setup(this.getTablesFromDB());
       		checkADTables(isDebugMode);
        	this.isInitialized = true;
        }finally{
        	isInitializing=false;
        }
        // this check will not prohibit tables from loading into memory
   		checkAll();
    }
	/**
	 * Check system mode, if debug and table is shared among multiple companies, such as AD_TABLE, AD_COLUMN, AD_SUBSYSTEM,
	 * will set as menu object, and marked read only
	 * @param table
	 */
	private void checkADTables(boolean isDebugMode){
		if(isDebugMode) return;
		try{
			logger.debug("Disable AD tables for modification");
			List apTables=QueryEngine.getInstance().doQueryList("select name from ad_table where isactive='Y' and not exists (select 1 from ad_column where ad_column.ad_table_id= ad_table.id and ad_column.dbname='AD_CLIENT_ID' and ad_column.isactive='Y')");
			for(int i=0;i< apTables.size();i++){
				TableImpl table=(TableImpl)tableNames.get((String)apTables.get(i));
				if(table!=null){
					table.setIsMenuObject(false);
					table.setMask("Q");
				}
			}
		}catch(Throwable t){
			throw new Error("Internal error when checking ad tables:"+ t);
		}
	}    
    /**
     * Throw NDSRuntimeException if errors found
     *
     */
    private void checkAll() {
    	// trigger column should not modifiable and should be nullable
    	for(Iterator it=columnNames.values().iterator();it.hasNext();){
    		ColumnImpl column= (ColumnImpl)it.next();
    		//checkTriggerColumn(column);
    		if(column.isFilteredByWildcard())checkWildcardFilter(column);
    	}
    	
    }
    
    /**
     * 方法将被TableManager 在所有的表都装载后调用，因为父表可能在子表后被加载
    1）“含过滤器字段”必是FK类型；
    2）“关联字段”如果不在“含过滤器字段”所在表，则必定在“含过滤器字段”所在表的PARENT_TABLE_ID 所指明的表上
   	
    此方法必须放在TableManager里，因为TableManager 需要完成校验后才能设置为已初始化
    @throws RuntimeException
    */
    private void checkWildcardFilter(ColumnImpl column){
		// 定位关联字段
		Pattern a= Pattern.compile("@(.*?)@");
		Matcher m= a.matcher(column.getFilter());
		java.util.ArrayList cs=new ArrayList();  
		while(m.find()){
			String c= m.group(1); // this will be the ref column
			Column fc= (Column)this.columnNames.get(c.toUpperCase());
			if(fc==null)throw new NDSRuntimeException("Wildcard filter error for column"+ column+": Could not find column '"+ c+"' defined in filter");
			if(column.getTable().getId() != fc.getTable().getId()){
				Table pt= column.getTable().getParentTable();
				if(pt ==null ) throw new NDSRuntimeException( "Wildcard filter error for column"+ column+":"+ c +" found but not exists in table "+ column.getTable());
				if(pt.getId() != fc.getTable().getId())
					throw new NDSRuntimeException("Wildcard filter error for column"+ column+":"+ c +" found but neither exists in table "+ column.getTable()+" nor in parent table ("+ pt+")");
			}
			
			cs.add(fc);
		}
		if(cs.size()==0){
			// column has no double @, so take it as not WFC
			column.setIsFilteredByWildcard(false);
		}else{
			column.setReferenceColumnsInWildcardFilter(cs);
		}
    }    
    private void checkTriggerColumn(Column column){
    	if(!"trigger".equalsIgnoreCase(column.getObtainManner())) return;
    	try{
    	//Assert.assertEquals("Found nullable error in trigger column:"+ column,true, column.isNullable());
    	Assert.assertEquals("Found modifiable error in trigger column:"+ column,false, column.getModifiable());
    	Assert.assertEquals("Found mask error in trigger column:"+ column,false, column.isModifiable(Column.ADD));
    	Assert.assertEquals("Found mask error in trigger column:"+ column,false, column.isModifiable(Column.MODIFY));
    	}catch(Exception e){
    		logger.error(e.getMessage());
    	}
    	
    }
    
    public WebAction getWebAction(int actionId){
    	return (WebAction)webActions.get(actionId);
    }    
    /**
     * This will load comment from db and set to Column, comment of column is quite big, 
     * so we do lazy loading here 
     * @param column
     * @return
     */
    public String getComments(Column column){
    	if(column.getComment()!=null) return column.getComment();
    	try{
    		String c= (String)QueryEngine.getInstance().doQueryOne("select comments from ad_column where id="+ column.getId());
    		((ColumnImpl)column).setComment(c);
    		return c;
    	}catch(Throwable t ){
    		logger.error("Fail to get comments of column "+ column, t);
    	}
    	return "";
    }
    /**
     * 当导入Excel时，最有可能导致导入错误的Unique Index，目前支持的UNIQUE INDEX 应当：
     * 所有的字段都是在AD_COLUMN里定义了的，而且至少有一个字段是可以在新增时输入的。
     * 如果有多个UNIQUE INDEX符合，将按名称排倒序，取第一个
     * @param table
     * @return null 没有符合上述条件的Unique Index
     */
    public String getUniqueIndexName(Table table){
    	if( ((TableImpl)table).getUniqueIndexColumns()==null){
    		findUniqueIndex((TableImpl)table);
    	}
    	return  ((TableImpl)table).getUniqueIndexName();
    }
    /**
     *
     * @param table
     * @return elements are Column or Collections.EMPTY_LIST
     */
    public List getUniqueIndexColumns(Table table){
    	
    	if( ((TableImpl)table).getUniqueIndexColumns()==null){
    		findUniqueIndex((TableImpl)table);
    	}
    	return  ((TableImpl)table).getUniqueIndexColumns();
    }
    /**
     * @todo This one should be moved to database specific implementation
     * @param table
     */
    private void findUniqueIndex(TableImpl table){
    	try{
    		List idx=QueryEngine.getInstance().doQueryList("SELECT INDEX_NAME FROM USER_INDEXES WHERE TABLE_NAME='"+ table.getRealTableName()+"' AND index_type='NORMAL' AND UNIQUENESS='UNIQUE' ORDER BY INDEX_NAME DESC");
    		String idxName=null;
    		for(int i=0;i< idx.size();i++){
    			idxName=(String) idx.get(i);
    			//  所有的字段都是在AD_COLUMN里定义了的，而且至少有一个字段是可以在新增时输入的。
    			List cols=QueryEngine.getInstance().doQueryList(
    					"SELECT COLUMN_NAME FROM USER_IND_COLUMNS WHERE INDEX_NAME='"+ idxName +"' AND TABLE_NAME='"+ table.getRealTableName()+"'"
    					);
    			boolean b=true;
    			int cnt=0;// editable columns count
    			ArrayList al=new ArrayList(); 
    			for(int j=0;j< cols.size();j++){
    				Column col= table.getColumn((String)cols.get(j));
    				if(col!=null ){ 
    					if(col.isMaskSet(col.MASK_CREATE_EDIT)){
    						cnt++;
    					}
    				}else{
    					// not found in ad, so suppose to be a column used by proc or trigger, this index is
    					// not qualified as our unique index
    					b=false;
    					break;
    				}
    				al.add(col);
    			}
    			if(b && cnt >0){
    				// ok, we just need one
    				table.setUniqueIndex(idxName, al);
    				logger.debug("find unique index "+ idxName+" on "+ table);
    				break;
    			}else{
    				logger.debug(idxName+" is not unique index what we need");
    			}
    		}
    		if( ((TableImpl)table).getUniqueIndexColumns()==null){
    			// no unique index 
    			table.setUniqueIndex(null, Collections.EMPTY_LIST);
    		}
    	}catch(Throwable t){
    		logger.error("fail to load udx", t);
    	}
    }
    /**
     * Replace public instance with tmp instance
     * This method should be call after tmp instance successfully loaded schema
     *
     */
    public static void replacePublicInstance(){
    	instance.destroy();
    	instance.dict= tmpInstance.dict;
    	instance.fkColumns=tmpInstance.fkColumns;
    	instance.tableCategories=tmpInstance.tableCategories;
    	instance.subSystems = tmpInstance.subSystems;
    	instance.tableIDs=tmpInstance.tableIDs;
    	instance.tableNames=tmpInstance.tableNames;
    	instance.columnIDs= tmpInstance.columnIDs;
    	instance.columnNames=tmpInstance.columnNames;
    	instance.tableList= tmpInstance.tableList;
    	instance.parentTables= tmpInstance.parentTables;
    	instance.views=tmpInstance.views;
    	instance.dateTable= tmpInstance.dateTable;
    	instance.webActions= tmpInstance.webActions;
    	logger.debug("TableManger "+ instance.name+" is updated by "+ tmpInstance.name);
    	tmpInstance=null;
    }
    public static synchronized TableManager getTmpInstance() {

        if( tmpInstance ==null) {
        	tmpInstance= new TableManager();
        }
        return tmpInstance;
    }    
    public static synchronized TableManager getInstance() {

        if( instance ==null) {

            instance= new TableManager();
            /**
             * @todo
             * Remember remove following lines to ensure initialize from outside
             */
            /*Properties prop=new Properties();
            prop.setProperty("directory",SchemaStructure.DEFAULT_SCHEMA_PATH);
            prop.setProperty("defaultTypeConverter","nds.schema.OracleTypeConverter" );
            instance.init(prop);*/

        }
        return instance;
    }

    public void destroy() {
        clearAll();
        logger.debug("TableManager "+ name+ " is destroied." );
    }

}
/**
 * 根据一个索引Table找到与他关联的其他表，和这些表的下一级关联的表，等等。
 */
class ReferTable{
    private TableManager tm;
    private Hashtable htColumns;// key:Integer columnId, value:ArrayList ( the column's column being referred)
    public ReferTable(TableManager tmr, Hashtable ht){
        tm=tmr;
        htColumns=ht;
    }
    public Hashtable getHtColumns(){
        return htColumns;
    }
    /**
    * @return elements are Column, is the table's column who use <param>column</param> as the FK
    * note if the column already exist in htCollumns, then will not be set again
    */
    private ArrayList getReferedColumns(Column column){
        ArrayList al=new ArrayList();
        Iterator it=tm.getColumnsBeingReferred(column.getTable().getPrimaryKey().getId());
        ArrayList v=new ArrayList();
        while( it.hasNext()) {
            Column cl=(Column)it.next();
            v.add(cl);
        }
        return v;
    }
    /**
    * Fill columns of <param>cs</param> with their deeper level refereed columns
    * @param hier fill to which level, if 0, no futher
    */
    private void fillRelateColumns(ArrayList cs, int hier) {
        if (hier==0) return;
        Column col;ArrayList al;
        for(int i=0;i< cs.size();i++){
            col=(Column)cs.get(i);
            if ( htColumns.get(col)==null){
                al=getReferedColumns(col);
                if( al!=null && al.size()>0){
                    htColumns.put(col,al);
                    fillRelateColumns(al, hier-1);
                }
            }
        }

    }
    /**
    * Width first
    */
    public ArrayList getReferedColumns(int tableId, int hier) {
        if(hier==0) return null;
        Iterator it=tm.getColumnsBeingReferred(tm.getTable(tableId).getPrimaryKey().getId());
        ArrayList v=new ArrayList();
        while( it.hasNext()) {
            Column column=(Column) it.next();
            v.add(column);
        }
        fillRelateColumns(v, hier-1);
        return 	v;
    }
    
    
    
}
