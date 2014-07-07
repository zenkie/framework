/******************************************************************
* @since 2.0
*���ڹ���������˵����
��ν�����ṹ����ָorder�� order item �Ĺ�������orderҳ��򿪵�ʱ�򣬿�����ʾ���Ӧ��order items, 
�������������޸�,���������ţ�����µ�ҳ�����༭״̬����Ӧ������orderitem ���������ݵ�ͷ��Ϣ�������ַ�ʽ��
�����ṹ��Ӧ������1:1 �� 1:n �Ĺ�ϵ��1:1ʱ��ֱ���ڵڶ���ҳ������޸�״̬��Ӧ��ֹ�����ֶε��޸ģ������������û��
��¼��Ӧֱ�ӽ���������״̬��ͬʱ��ֹ�����ֶε��޸ġ�
1:nʱ��ֱ����ʾ�����б��������������ķ�ʽ��������������͵�һ�������棬�����������潫����һ���������������󣬵�
�����ֶ�Ϊȱʡֵ����һ��������Ҳ��һ����

table�Ľṹ��
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

���table2�� ref-by-column ��ָ����ϵͳ�Զ���table2��Ѱ�� ref-column Ϊtable1���ֶΣ����û�ҵ��������ж������ʾ���ô���
ref-by-column ��һ��Ҫ�����ָ�� table1.key ,������c_v_v_parnter����Ӧ����Ϣ����Ӧ��c_bpartner����id��Ŀ����ʱ������id�ϲ�
Ϊָ��refer-column����Ȼ��ϵͳ��ʱӦ��������ʾ�����⣬ref-by-column ������number()���ǿ϶��ġ�

ref-by-table ��һ����ʾ�����磬c_bpartner �Ͽͻ������ǹ�Ӧ��ʱ������Ҫ��ʾ��Ӧ��tab, ����tab���������<filter>
ϵͳ�ڹ���tab��ʱ����Ҫ���������ж��Ƿ����������������ǣ�checkvalid: select 1 from table1 where id=1020 and <filter>
������ڼ�¼������ʾ��tab.

association ��ʾ�˹�����ϵ��1 ��ʾ��ҳ�Ͻ�һ����¼����ֱ����ʾ��n ��ʾ��ҳ���ж�����¼��ֱ����ʾ�б�
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
	*��������Column �У���Щ�ֶ�Ϊvirtual, ��ʾ��column�Ǽ����У�
	* һ����˵���������ǵ�ǰ��ĳ�ֶε�ֵ��������alias table���Ľ�ϲ�����ֵ
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
     * @return elements are Column
     * @since 3.0
     */
    public ArrayList getColumns(int[] columnMasks, boolean includeUIController,int securityGrade);
    /**
	 Get columns which has any of the bit masks set in specified positions.
     * For instance, getColumns([0,3]) will return columns which
     * is showable when creation form <b>OR</b> modifiable in update form.
     * refer to Column.isMaskSet for mask information. 
     * @param columnMasks elements shoule be 0-9
     * @param includeUIController if false, will not add column that getDisplaySetting().isUIController()==true
     * @param securityGrade return column's security grade should not be greater than this one
     * @return elements are Column
     * add includecheck
     */
    public ArrayList getColumns(int[] columnMasks, boolean includeUIController, int securityGrade,boolean includeCheck);
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
     * ����ֶ�����
     * @param columnID should be one of getAllColumns(),
     * @return null if column not found.
     * @roseuid 3B80A333005C
     */
    public Column getColumn(int columnID);

    public Column getColumn(String columnName);
    /**
     * ����ע��
     * @return String of table meaning
     */
    public String getComment();

    /**
     * @return getAllColumns() ���Ӽ�������������Ҫ����ĳ�ֲ����µ�ҳ���ϵĿ���ʾ��column��
     * ������������ID�ֶΣ���ʱ������û������ġ�
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
     * Target for Row URL to display, "_blank" for seperate window, others will be as dialog
     * @return
     */
    public String getRowURLTarget();
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

    //deprecated after versioned trigger created 
    //public String getTriggerName(String condition);
    
    /**
     * Get trigger of specified event, the trigger is a kind of procedure
     * which will be called during execution
     * @param event currently support "AC","AM","BD" only
     * @return null or VersionedTrigger
     */
    public TriggerHolder.VersionedTrigger getTrigger(String event);
    /**
     * Sum fields of this table��elements are Column
     * ͨ����ЩαColumn �����¹�ͬ������
     *  type=int or float
     *  nullable=true
     *  mask=000101
     *  ����ֻ�ڵ�����ҳ������ʾ
     */
    public Iterator getSumFields();

    /**
     * flink������ҳ������ʾһ������
     * ����ֻ��name,desc,interpreter(�����web Container��context��url)
     * @return
     * �������е�flinks
     */
    public Collection getFlinks();

    /**
     * ����table��itemTable������
     * @return
     */
    public String getItemTable();

    /**
     * ������ҪԤ��ȡֵ���ֶ������������������getPrefetchSql()ͬʱʹ��
     * @return
     */
    public String getPrefetchColumn();

    /**
     * ����ȡֵ��SQL���
     * @return
     */
    public String getPrefetchSql();

    /* @return DISPATCH_NONE ��ʾ����Ҫ�·�
    DISPATCH_ALL ��ʾ�·������е��ŵ꣨Ҳ������expdata.customerID�ֶ�Ϊ��)
    DISPATCH_SPEC ��ʾ�·���ָ���ŵ꣬�ŵ�ID�ֶ�ͨ��getDispatchColumn()ָ�����ֶεĵ�ǰ��¼��ֵ�����
    */
    public int getDispatchType() ;
    /* @return null �������Ҫ�·������·��������ŵ�
    �����Ӧ�ŵ�ID�ֶΣ�ϵͳ�����ݴ��ֶε�ֵ���뵽ExpData.customerID�ֶ�
    */
    public Column getDispatchColumn();

    /**
     * ����������������ͷ��"POS"�������ִ�Сд��������ͷ��������ĸȥ����ȡʣ�ಿ��
     * @return �����pos������ơ�
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
     * ��ϵͳ��2.0��ʼ֧�ֶ๫˾�ṹ�����������ad_client_id�ֶΣ���ÿ�ҹ�˾���û�Ӧ��
     * �������ڸù�˾���ݣ������ܿ���������˾������
     * @return true if table contains column "ad_client_id"
     * @since 2.0
     */
    public boolean isAdClientIsolated();
    /**
     * �Ƿ����������޸ļ�¼��ʱ�򣬹�����(FK)�����õļ�¼�����ǿ��õġ�
     * ���磬�����department isAcitveEnabled=true,���ڴ������޸� employee��ʱ��
     * ֻ��ѡ�� isactive='Y' �Ĳ���
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
     * ��ǰ��ļ�¼���������ֹ����������Ҳ����ͨ��AD_PROCESS�е�"ͳ�����ݿ�����"�������Զ����㡣
     * @return ��ǰ��¼��
     */
    public int getRowCount();
    /**
    *��ȡ��������۵��˵���ID
    *
    */
    public int getAccordid();
    /**
     *��ȡ��������۵��˵���ICO
     *
     */
     public String getAccordico();
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
     * Set in ad_table.props as json object.
     * Developer should take care, the returned object should not be modified such as change 
     * property inside. The later implementation will forbid such kind of operation. 
     * @return null or a valid object
     * @since 4.1 
     * 
     */
    public JSONObject getJSONProps();
    
    /**
     * Alias name is a short yet unique name for Table, support comma separated values,
     * each one could be a unique name, that is, one table can have several alias names.
     * 
     * So designer can build up several alias name systems in the same meta data 
     * @return string may contain comma for alias
     * @since 4.1
     */
    public String getAliasName();
    /**
     * DISPATCH_NONE ��ʾ����Ҫ�·�
     */
    public final static int DISPATCH_NONE=16;
    /**
     DISPATCH_ALL ��ʾ�·������е��ŵ꣨Ҳ������expdata.customerID�ֶ�Ϊ��)
     */
    public final static int DISPATCH_ALL=32;
    /**
     * DISPATCH_SPEC ��ʾ�·���ָ���ŵ��飬�ŵ���ID�ֶ�ͨ��getDispatchColumn()ָ�����ֶεĵ�ǰ��¼��ֵ�����
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
     * Support unsubmit on object
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
     * Object isactive=N, table that records can be void should not be child table(such as poitem)
     * When we do void action on records, we will not do any actions on other tables
     */
    public final static int VOID=7;
    /**
     * Support unsubmit on object
     * 
     */
    //public final static int UNSUBMIT=7;
    
    /**
     * ��̨��ѯָ���и�����չ���Է����ж�������
     */
    public ArrayList GetExtendColumns(String extendName,int securityGrade)throws Exception;
    
    /**
     * get sysmodel
     */
    public SysModel getSysmodel();
}

