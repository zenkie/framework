/******************************************************************
* @since 2.0
*关于关联机构的说明：
所谓关联结构，是指order和 order item 的关联，在order页面打开的时候，可以显示其对应的order items, 
并且允许批量修改,如果单击序号，则打开新的页面进入编辑状态（对应关联的orderitem 是其他单据的头信息采用这种方式）
关联结构还应该允许1:1 和 1:n 的关系，1:1时，直接在第二个页面进入修改状态（应禁止关联字段的修改），如果关联表还没有
记录，应直接进入新增的状态，同时禁止关联字段的修改。
1:n时，直接显示批量列表，允许两种新增的方式：批量新增界面和单一新增界面，批量新增界面将允许一次输入多个关联对象，但
关联字段为缺省值，单一新增界面也是一样。

table的结构：
<table>
   <name>table1</name>
   <primary-key>key1</primary-key>
   <ref-by-table>
       <name>table2</name>
       <ref-by-column>table2.column</ref-by-column> <!-- this column should reftable to table1.key1, but not nessessary-->
       <filter>f</filter>
       <association>1<association>
   </ref-by-table>
   <ref-by-table>....
   ....
</table>

如果table2的 ref-by-column 不指明，系统自动在table2里寻找 ref-column 为table1的字段，如果没找到，或者有多个，显示配置错误！
ref-by-column 不一定要求就是指向 table1.key ,举例：c_v_v_parnter即供应商信息，对应到c_bpartner中是id项目，这时就允许id上并
为指明refer-column，当然，系统这时应该予以提示。另外，ref-by-column 必须是number()这是肯定的。

ref-by-table 不一定显示，例如，c_bpartner 上客户类型是供应商时，才需要显示供应商tab, 故在tab设计中增加<filter>
系统在构造tab的时候，需要根据条件判断是否满足条件，方法是：checkvalid: select 1 from table1 where id=1020 and <filter>
如果存在记录，则显示此tab.

association 表示了关联关系，1 表示分页上仅一条记录，（直接显示）n 表示分页上有多条记录，直接显示列表
*
********************************************************************/

package nds.schema;
import java.io.Serializable;
import java.util.*;

import nds.util.PairTable;
import org.json.JSONString;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * This can only be used for table which has primary key as "id"
 */
public interface Table extends Serializable  {
	/**
	*别名表。在Column 中，有些字段为virtual, 表示该column是计算列，
	* 一般来说，计算列是当前表某字段的值和其他表（alias table）的结合产生的值
	*@return elements are nds.schema.AliasTable
	*/
	public ArrayList getAliasTables();
    /**
     * @roseuid 3B80A2F20044
     */
    public ArrayList getAllColumns();
    
    /**
     * Legend to mark table records style. 
     * For each column that has UIAlerter, will construct a legend. The returned 
     * PairTable will contain all these columns and their legends
     * @param columnMask Column.QUERY_LIST, or Column.QUERY_SUBLIST, 
     * currently only Column.QUERY_LIST implemented
     * @return PairTable may be null, key: Column, value: Legend
     */
    public PairTable getLegends(int columnMask);
    /**
     * Get columns which has any of the bit masks set in specified positions.
     * For instance, getColumns([0,3]) will return columns which
     * is showable when creation form <b>OR</b> modifiable in update form.
     * refer to Column.isMaskSet for mask information. 
     * @param columnMasks elements shoule be 0-9
     * @param includeUIController if false, will not add column that getDisplaySetting().isUIController()==true
     * @return elements are array
     * @since 3.0
     */
    public ArrayList getColumns(int[] columnMasks, boolean includeUIController);
    /**
     * 
     * @param showAction either Column.QUERY_SUBLIST or Column.Modify
     * @return
     */
    public ArrayList getModifiableColumns(int showAction);
    /**
     * Primary Key
     */
    public Column getPrimaryKey();

    /**
     * 表的字段描述
     * @param columnID should be one of getAllColumns(),
     * @return null if column not found.
     * @roseuid 3B80A333005C
     */
    public Column getColumn(int columnID);

    public Column getColumn(String columnName);
    /**
     * 含义注释
     * @return String of table meaning
     */
    public String getComment();

    /**
     * @return getAllColumns() 的子集，仅仅包含需要在在某种操作下的页面上的可显示的column。
     * 例如大多数表都有ID字段，这时无需给用户看到的。
     * @param action, can be one of Column.ADD, Column.MODIFY, Column.QUERY_LIST,Column.PRINT_LIST
     * @see Column#ADD, Column#MODIFY, Column#QUERY, Column#PRINT
     * equals to getShowableColumns(action, true); 
     */
    public ArrayList getShowableColumns(int action);
    /**
     * 
     * @param action
     * @param includeUIController if false, will not add column that getDisplaySetting().isUIController()==true
     * @return
     */
    public ArrayList getShowableColumns(int action, boolean includeUIController) ;
    /**
     * Indexed columns
     * @return
     * @since  4.0
     */
    public ArrayList getIndexedColumns();
    /**
     * @return column name which can substitute primary key. For instance,
     * in Employee table, it would be workNO, in bussiness  note table, it
     * would be NO. Alternate key will be displayed as identity when this
     * table is being referred by other tables.
     * @return null if no AK for this table
     * @roseuid 3B80B71B00C8
     */
    public Column getAlternateKey();

    public Column getAlternateKey2();//by Hawkins
    public Column getDisplayKey();
    /**
     * @roseuid 3B845BD90197
     */
    public int getId();

    /**
     * @roseuid 3B84616C0012
     */
    public String getName();

    /**
     * @roseuid 3B8461710005
     */
    public String getDescription(Locale locale);

    /**
     * @return url of single row in this table, normally, each row of table signs an object of fixed class. If we specified that each class to be instantiated must has a fascade url, then all operations related to that class of object can be directed to that url.
     * @roseuid 3B8468CF029C
     */
    public String getRowURL();

    /**
     * @return null if this table's row can not be mapped to a class, else return the full name of that Class
     * @roseuid 3B84690302D3
     */
    public String getRowClass();

    /**
     * For categorize all tables
     */
    public TableCategory getCategory();

    /**
     * Check whether <code>action</code> is enabled on table
     * @param actoin - can be any of following values:
     *      Table.ADD, Table.MODIFY, Table.DELETE, Table.SUBMIT, Table.QUERY
     *
     */
    public boolean isActionEnabled(int action);
    /**
     * @return order of table in category, mainly used for display category in Query/Query_portal.jsp
     */
    public int getOrder();

    /**
     * Get trigger name of specified condition, the trigger is a kind of procedure
     * which will be called during execution
     * @param condition such as "after-modify", for more, see Schema.TriggerHolder
     * @return
     */
    public String getTriggerName(String condition);
    /**
     * Sum fields of this table，elements are Column
     * 通常这些伪Column 有如下共同特征：
     *  type=int or float
     *  nullable=true
     *  mask=000101
     *  而且只在单对象页面中显示
     */
    public Iterator getSumFields();

    /**
     * flink用来在页面上显示一个链接
     * 属性只有name,desc,interpreter(相对于web Container的context的url)
     * @return
     * 返回所有的flinks
     */
    public Collection getFlinks();

    /**
     * 返回table的itemTable的名字
     * @return
     */
    public String getItemTable();

    /**
     * 返回需要预先取值的字段名，这个方法必须与getPrefetchSql()同时使用
     * @return
     */
    public String getPrefetchColumn();

    /**
     * 返回取值的SQL语句
     * @return
     */
    public String getPrefetchSql();

    /* @return DISPATCH_NONE 表示不需要下发
    DISPATCH_ALL 表示下发给所有的门店（也就是在expdata.customerID字段为空)
    DISPATCH_SPEC 表示下发给指定门店，门店ID字段通过getDispatchColumn()指明的字段的当前记录的值来获得
    */
    public int getDispatchType() ;
    /* @return null 如果不需要下发，或下发给所有门店
    否则对应门店ID字段，系统将根据此字段的值插入到ExpData.customerID字段
    */
    public Column getDispatchColumn();

    /**
     * 规则：如果表的名称起头是"POS"（不区分大小写），将起头的三个字母去除，取剩余部分
     * @return 下面的pos表的名称。
     */
    public String getDispatchTableName();
    /**
     * if at least one column has sub-method set, return ture
     * when true, the UI will display subtotal items in page result, and inform the
     * user that full range subtotal can be viewed on the current query request.
     * @return
     */
    public boolean isSubTotalEnabled();
    
    /**
     * The tree type will has a column refered to the table's PK as FK,
     * which is the parent node id
     * @return 
     * @since 2.0
     */
    public boolean isTree();

    /**
     * When isTree()==true, the returned column is the FK to parent node of tree
     * node
     * @return null if isTree==false
     */
    public Column getParentNodeColumn();
    
    /**
     * When isTree()==true, the returned column is the sign that current record
     * is tree leaf or not
     * @return null if isTree==false
     */
    public Column getSummaryColumn();
    /**
     * The real table name, if isView()==false, that will be equal to table name
     * @since 2.0
     */
    public String getRealTableName();
    
    /**
     * Check table is view or not, if is view, then the real table name can be 
     * retrieved by getRealTableName()
     * @return true if this is view.
     * @since 2.0
     */
    public boolean isView();
    
    /**
     * If this is view, then it can be constructed by 
     * "select <columns> from <realtablename> as <name> where <filter>"
     * note filter should has all columns linked to tablename, such as 
     * "C_V1$CrossOrder.doctype='APPLY'"
     * if tablename not set, then some column may be ambiguous
     * @return filter of the table to construct the view
     * @since 2.0
     */
    public String getFilter();
    
    /**
     * Table can has reference table, just like order and order lines, when
     * display order, we will provide a tab control, first tab page is the order
     * , second page will the lines of that order
     * @return elements are RefByTable
     * @see nds.schema.RefByTableHolder
     * @since 2.0
     */
    public ArrayList getRefByTables();
    /**
     * Is this table's data isolated by client
     * 本系统从2.0开始支持多公司结构，如果表中有ad_client_id字段，则每家公司的用户应当
     * 被限制在该公司数据，而不能看到其他公司的数据
     * @return true if table contains column "ad_client_id"
     * @since 2.0
     */
    public boolean isAdClientIsolated();
    /**
     * 是否在新增或修改记录的时候，关联表(FK)所引用的记录必须是可用的。
     * 例如，如果表department isAcitveEnabled=true,则在创建或修改 employee的时候，
     * 只能选择 isactive='Y' 的部门
     * @return true if table has column "isactive" and that column
     * can be changed when insert or update, that is, 
     * column.isModifiable(Column.ADD) || column.isModifiable(Column.MODIFY)
     * @since 2.0
     */
    public boolean isAcitveFilterEnabled();
    /**
     * Get belonging security directory name, default to 
     * TableName.upperCase()+"_LIST".
     * 
     * If table name ends with "Item" or "Line"
     * will default to parent table's directory, if that exists
     * 
     * If definition has neither parent table nor directory set, system will throw defintion
     * error 
     * 
     * @return String of the security directory
     * @since 2.0
     */
    public String getSecurityDirectory();
    
    /**
     * Get WebAction from ad_action
     * @param dte actions of which display type
     * @return List nerver be null
     * @since 4.1
     */
    public List<WebAction> getWebActions(WebAction.DisplayTypeEnum dte);
    /**
     * 
     * @return procname of the submit method, will be used
     *  for location store procedure, if null will default to
     *  TableName +"Submit"
     * @since 2.0
     */
    public String  getSubmitProcName();
    
    /**
     * Elements are elements are nds.web.bean.MenuItem
     * @return
     */
    public Collection getExtendMenuItems();
    /**
     *  Elements are nds.web.bean.Button or String, will check user permission on the object.
     *  Only if permission granted, will the button returned.
     * @param objectId the record id 
     * @param user should be nds.control.web.UserWebImpl
     * @return
     */
    public Collection getExtendButtons(int objectId, Object userWebImpl);
    /**
     * Whether this table support SMS(short message service) 
     * @return
     */
    public boolean isSMS();
    /**
     * Is show on menu or query list. For item tables, this should be false
     * @return
     */
    public boolean isMenuObject();
    
    /**
     * Table such as M_InOutItem, C_OrderItem will allow product detail information stored
     * in m_attributedetail table, such tables set supportAttributeDetail to true  
     * @return
     * @since 3.0
     */
    public boolean supportAttributeDetail();
    /**
     * When true, when this table is used as FK table, the main table's column will show
     * as dropdown, else, show as object query form.
     * @return
     */
    public boolean isDropdown();
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
    public Table getParentTable();
    /**
     * 当前表的记录数，允许手工输入调整。也可以通过AD_PROCESS中的"统计数据库行数"来进行自动计算。
     * @return 当前记录数
     */
    public int getRowCount();
    /**
     * Last modified date
     * @return date
     */
    public java.util.Date getModifiedDate();
    /**
     * Whether table has big record set, if so, should not allow user to query the data without any filter
     * @return 
     */
    public boolean isBig();
    /**
     * UI Configuration, that is id of nds.web.config.ObjectUIConfig. ObjectUIConfig can be constructed 
     * using {@link nds.web.config.PortletConfigManager#getPortletConfig(int, int)}
     * @return 
     */
    public int getUIConfigId();
    /**
     * Convert to JSON Object for javascript
     * @param locale table description will be locale specific
     * @return
     * @throws JSONException
     */
    public JSONObject toJSONObject(Locale locale) throws JSONException;
    /**
     * DISPATCH_NONE 表示不需要下发
     */
    public final static int DISPATCH_NONE=16;
    /**
     DISPATCH_ALL 表示下发给所有的门店（也就是在expdata.customerID字段为空)
     */
    public final static int DISPATCH_ALL=32;
    /**
     * DISPATCH_SPEC 表示下发给指定门店组，门店组ID字段通过getDispatchColumn()指明的字段的当前记录的值来获得
     */
    public final static int DISPATCH_SPEC=64;

    /**
     * QUERY action on table, which means user can query rows from GUI, if
     * isActionEnabled(Table.QUERY) is return true;
     */
    public final static int QUERY=0;
    /**
     * Add action on table, which means user can create row from GUI, if
     * isActionEnabled(Table.ADD) is return true;
     */
    public final static int ADD=1;
    /**
     * DELETE action on table, which means user can delete row from GUI, if
     * isActionEnabled(Table.DELETE) is return true;
     */
    public final static int DELETE=2;
    /**
     * MODIFY action on table, which means user can update row from GUI, if
     * isActionEnabled(Table.MODIFY) is return true;
     */
    public final static int MODIFY=3;
    /**
     * SUBMIT action on table, which means user can do submit action from GUI, if
     * isActionEnabled(Table.SUBMIT) is return true; normally only xxxSht table will
     * has this action set.
     */
    public final static int SUBMIT=4;
    /**
     * AUDIT action on table, which means user can do permit or rollback action from GUI, if
     * isActionEnabled(Table.AUDIT) is return true; normally only xxxSht table will
     * has this action set. If table has Audit action, submit will be set what ever it
     * set or not.
     */
    public final static int AUDIT=5;
    /*
    * When submit, all selected ones will be concated as just one parameter, and send to request
    * method, such as TranscredShtGroupSubmit('id1,id2,id3', r_code, r_msg);
    */
    public final static int GROUPSUBMIT=6;
    /**
     * Support unsubmit on object
     * 
     */
    public final static int UNSUBMIT=7;

}
