/******************************************************************
*
*$RCSfile: Column.java,v $ $Revision: 1.8 $ $Author: Administrator $ $Date: 2006/06/24 00:35:36 $
*
*$Log: Column.java,v $
*Revision 1.8  2006/06/24 00:35:36  Administrator
*no message
*
*Revision 1.7  2005/12/18 14:06:17  Administrator
*no message
*
*Revision 1.6  2005/11/16 02:57:21  Administrator
*no message
*
*Revision 1.5  2005/08/28 00:27:04  Administrator
*no message
*
*Revision 1.4  2005/05/16 07:34:18  Administrator
*no message
*
*Revision 1.3  2005/03/21 00:55:43  Administrator
*no message
*
*Revision 1.2  2005/03/16 09:05:12  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.5  2004/02/02 10:42:41  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/05/29 19:40:00  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/04/03 09:28:15  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/03/30 08:11:36  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.5  2001/12/28 14:20:02  yfzhu
*no message
*
*Revision 1.4  2001/11/29 00:49:40  yfzhu
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
//Source file: F:\\work2\\tmp\\nds\\query\\Column.java

package nds.schema;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import org.json.*;

import nds.query.ColumnLink;
import nds.util.*;
/**
 */
public interface Column extends Serializable {
    /**
     * Gross type of integral value, including following java.sql.Types:
     * .BIGINT .DECIMAL .INTEGER .SMALLINT .TINYINT .FLOAT .DOUBLE .REAL
     */
    public final static int NUMBER=0;
    /**
     * Gross type of date value, including following java.sql.Types:
     * .TIME .TIMESTAMP .DATE
     */
    public final static int DATE=1;

    /**
     * Gross type of string value, including following java.sql.Types:
     * .VARCHAR .LONGVARCHAR .CHAR
     */
    public final static int STRING=2;
    /**
     * Number(8), used only for date input in format 'YYYYMMDD'
     */
    public final static int DATENUMBER=3;
    
    /**
     * Gross type of other value, including those not declared above
     */
    public final static int OTHER=9;

    /**
     * action on column, is useful when deciding whether column can be show or modify
     * This is for Add action on column
     */
    public final static int ADD=0;
    /**
     * action on column, is useful when deciding whether column can be show or modify
     * This is for Modify action on column
     */
    public final static int MODIFY=1;
    /**
     * action on column, is useful when deciding whether column can be show or modify
     * This is for Query action on column
     */
    public final static int QUERY_LIST=2;
 
    /**
     * action on column, is useful when deciding whether column can be show or modify
     * This is for Print action on column
     */
    public final static int QUERY_SUBLIST=3;
    /**
     * Viewable when in object single display page
     */
    public final static int QUERY_OBJECT=4;
    public final static int PRINT_LIST=5;
    public final static int PRINT_SUBLIST=6;
    public final static int PRINT_OBJECT=7;
    
    /**
     * Masks for column mask
     */
    public final static int MASK_CREATE_SHOW=0;
    public final static int MASK_CREATE_EDIT=1;
    public final static int MASK_MODIFIY_SHOW=2;
    public final static int MASK_MODIFY_EDIT=3;
    public final static int MASK_QUERY_LIST=4;
    public final static int MASK_QUERY_SUBLIST=5;
    public final static int MASK_QUERY_OBJECTVIEW=6;
    public final static int MASK_PRINT_LIST=7;
    public final static int MASK_PRINT_SUBLIST=8;
    public final static int MASK_PRINT_OBJECTVIEW=9;
        
    /**
     * On delete action 
     */
    public final static int ON_DELETE_NOACTION=0;
    public final static int ON_DELETE_SETNULL=1;
    public final static int ON_DELETE_CASCADE=2;
    
    /**
     * ���Դ����������У�����Ա�����ţ�������ˮ�ţ���ȻҲ�����أ�������С����һ
     * �����ǻ�������ֶ���ΪӦ�ñ��ѯʱ�Ķ�λ�ֶΡ�
     * @roseuid 3B81B15D02F7
     */
    public boolean isAlternateKey();

    public boolean isAlternateKey2();
    /**
     * �Ƿ���ʾ������һ������£���ʾ������AK������������������Էֿ�
     * ���磺��Ŀ���У�AK=Value, DK=Name
     * @return
     */
    public boolean isDisplayKey();
    /**
     * ����ע��
     * @return String of column meaning
     */
    public String getComment();
    /**
     * ����ע��
     * @return String of Showcomments meaning
     */
    public String getShowcomment();
    /**
     * ����ע��
     * @return String of Showtitle meaning
     */
    public Boolean getShowtitle();
    
    /**
     * ����ע��
     * @return String of rowspan meaning
     */
    public Boolean getRowspan();
    /**
     * Limit Value Group Name
     * @return
     */
    public String getLimitValueGroupName();
    
    /**
     * �Ƿ������ҳ������ʾ����id֮����ֶ���Ȼ������ʾ��
     * @param action: can be one of Column.ADD, Column.MODIFY, Column.QUERY
     * @roseuid 3B81B1690358
     */
    public boolean isShowable(int action);
    /**
     * Check whether mask is set on position
     * @param maskPosition should be in in between MASK_CREATE_SHOW and MASK_PRINT_OBJECTVIEW (0-9)
     * @return true if mask is set
     * @since 3.0
     */
    public boolean isMaskSet(int maskPosition);
    /**
     * �Ƿ������ҳ����ͨ��Input ���޸ģ���Щ�ֶΣ����û�����һ�������������޸�
     * ��Ҳ����˵����action= Column.MODIFY ʱ�������޸ģ���ͨ����ʱ��showable�ģ�
     * @param action: can be one of Column.ADD, Column.MODIFY, Column.QUERY
     * @roseuid 3B81B1690358
     */
    public boolean isModifiable(int action);

    /**
     * Whether this column allows for null input
     */
    public boolean isNullable();

    /**
     * some column should only has a limited set of valide value, int type .
     * Such as Bussinote's status. the limited set can be retrieved using getValues()
     * @return true if column can only accept a limited set of values
     * @see getValues()
     * @roseuid 3B81B21E0268
     */
    public boolean isValueLimited();

    /**
     * If range is not limited, return null, else return properties:
     * key: range value, value: value description.
     *
     * @return PairTable with  value and descriptions, the real value type
     *  should refer to getType
     * @roseuid 3B81B2480236
     */
    public PairTable getValues(Locale locale);

    /**
     * For char or date types this is the maximum number of characters, for
     * numeric or decimal types this is precision
     * @roseuid 3B81BADE00ED
     */
    public int getLength();
    /**
     * С������λ����length - scale -1 = precision��ȫ����
     * ��number(10,2) length= 13, precision=10, scale= 2
     * ������number ������Ч
     */
    public int getScale();
    /**
     * Statistics size of the column, which can be used for length setting in list layout 
     * @return -1 if no setting
     */
    public int getStatSize();
    /**
     * Some column is id of other table, we name those as "reference table".
     * @return reference table  if this column is id of other table, else return null
     * @roseuid 3B81BC1900A9
     */
    public Table getReferenceTable();

    public Column getReferenceColumn();
    
    /**
     * ����DATENUMBER��ʽ���ֶΣ�ϵͳ������Ĭ�ϵ�һ�ű� (t_day)���˱��ܹ���ʾ���ڶ�Ӧ���꣬�£����ȵ���Ϣ
     * ��ͳ�Ʒ���ʱ���ܹ��������ֶ��Ͻ��л����Ǳ�Ҫ�ġ�
     * 
     * ���ڴ����ҳ���getReferenceTable=trueʱʶ��Ϊfk����ʾ��ʽ�����ǲ���getReferenceTable���й�������
     * ��֧�֣������ӱ�������
     * @param tryDateNumberAsFK if true, will take t_day as reference table of datenumber type column
     * @return 
     */
    public Table getReferenceTable(boolean tryDateNumberAsFK);
    public Column getReferenceColumn(boolean tryDateNumberAsFK);
    /**
     * the order when display in pages
     * @roseuid 3B81BECC0338
     */
    public int getDisplayOrder();

    /**
     * ����text/textarea ���͵�column, ��ŵ�����Ӧ���� ColumnInterpreter ��ʵ��
     * 
     * ����button ���͵�column, ��ŵ����ݿ��� �洢���̵����ƣ������java�������
     *  �����ݰ���"."��ʱ�򣬱���Ϊ��java class. ������Ϊ�洢���̣����������ʹ�� 
     *  nds.web.ButtonCommandUI_Impl. 
     *  java class �����ʵ����nds.web.ButtonCommandUI �ӿڣ�����Ϊ�ǽ�������ࡣ����
     *  ����Ϊ������ִ���࣬�����������Ȼʹ��nds.web.ButtonCommandUI_Impl
     *  
     *  @since 1.0
     */
    public String getValueInterpeter();

    /**
     * Object construct UI for this column 
     * @return object construct UI 
     * @since 2.0
     */
    public Object getUIConstructor();
    /**
     * @roseuid 3B845BCE032C
     */
    public int getId();

    /**
     * @roseuid 3B84617902D8
     */
    public String getName();

    /**
     * @roseuid 3B84617F00B0
     */
    public String getDescription(Locale locale);

    /**
     * This is type in following nds.query.SQLType definition, int is their index +1, such as
     * "INT" getSQLType()=4
     * <p>
     * String[] sqlTypes = {    <br>
     *                          "CHAR","TINYINT","BIGINT","INT", <br>
     *                          "SMALLINT","FLOAT","REAL","DOUBLE",<br>
     *                          "NUMERIC","DECIMAL","DATE","VARCHAR",<br>
     *                          "LONGVARCHAR","TIMESTAMP","TIME","BIT",<br>
     *                          "BINARY","VARBINARY","LONGVARBINARY","NULL",<br>
     *                          "OTHER","CLOB","DATENUMBER" };
     * <p>
     * For gross type, use getType() instead
     * @see nds.query.SQLTypes
     * @roseuid 3B84618602A5
     */
    public int getSQLType();
    
    /**
     * Gross type of the column, can be any of following values:
     * <ul>
     *  <li>Column.DATE </li>
     *  <li>Column.NUMBER</li>
     *  <li>Column.STRING</li>
     * <li>Column.DATENUMBER</li>
     * </ul>
     *  For more detailed type, use getSQLType() instead
     *  @see getSQLType
     */
    public int getType();
    /**
     * @return table to which this column belong
     * @roseuid 3B8464FE0071
     */
    public Table getTable();

    /**
     * @���ر��ֶε����ƹ����ַ��������ڶ�����������ʱ��֤�Ϸ���
     * �磺"pz"�������ֱ������0��"paz"�������ֱ�����ڵ���0;
     * ���Ը�����Ҫ����κζ����ֶε�����
     */
    public String getLimit();//By Hawke

    /**
     * ���ػ�������
     * @return
     */
    public String getMoney();//By Hawke

    /**
     *   �����Ҫ�޸ĵ��ֶ�, �ڱ��޸�ʱ��ֻ��getModifiable()==true���ֶβŻᱻϵͳ�޸ģ�
     *   �������Ƿ���ҳ������ʾ����һ������£�modifierID, modifierDate ����getModifiable=true
     *   ,�����ǲ�һ����ҳ������ʾ��
     */
    public boolean getModifiable();//
    /**
     *   ����ض��ֶε�ֵ�Ļ�ȡ��ʽ
     */
    public String getObtainManner();//
    /**
     *   ����ض��ֶε�ȱʡֵ��ͨ�����ֶ��ǲ����޸ĵģ�����
     *   ÿ�����еĸ��ֶε�ֵ����һ���ģ�
     *   Will evaluate if value start with "=", which means the content is script
     *   �磺permission,status�ֶ�
     */
    public String getDefaultValue();//
    /**
     * Get default value without script evaluation. 
     * ����button ���͵İ�ť��defautlValue = "Y", ��ʾ��Ҫ��ʾ�� defaultValue="N" ����������ʾ����Ҫ��ʾ
     * ��ʾ���͵İ�ťͨ����Ҫִ�в���ȡ���Ķ�������ʾ����Ϊ"��ȷ����Ҫִ�С���ť���ơ�������"
     * @param eval false if just want to get the script, not the data
     * @return
     */
    public String getDefaultValue(boolean eval);
    public String getReferenceColumnName();// ���ض�Ӧ��reference���ж�Ӧ�е�����
    public String getTableColumnName();    // ���ظ�������Ӧ��TABLE�е���
    public Table getObjectTable();         // ���ظ�������Ӧ��TABLE
    
    /**
	     * ��4.0��ʼ���������ڽ����Ͽ�����������á�
	     * 
	     * �÷�1������html�ͻ��ˣ�����Ϊinput ���Ĳ�������, Ϊjavascript��function name�����磬ĳ�ֶ��ڽ���
	     * ����ʾΪ <input id=xx type='text' value='' onchange=$RegExpression> ���� RegExpression=
	     * 
	     * 
	     * �÷�2����� column.getDispalyType="xml"��getRegExpression ������һ��json ���󣬲�����ת��Ϊһ�����Ա���
	     * �� Column.getProperties �У� xml���͵��ֶΣ�������ʹ��1����ʽ, Ϊ��ֹ�÷�1���ĳ�������쳣���������ݽ������õ�
	     * getRegExpression ��
     *  
     * @return
     */
    public String getRegExpression();  
    /**
     * �� getRegExpression ��ȡ��json ����ת��Ϊ Properties
     * @since 4.0
     * @return null if not contain any infor in getRegExpression
     */
    public java.util.Properties getProperties();
    public String getErrorMessage();       // ���ش�����Ϣ-(�����ж�������ʽ����ʱ)
    /**
    * �ֶ��Ƿ�Ϊ�����ֶΣ������ֶξ��Ǹ��ֶβ��Ǳ���������ֶΣ�����һ������ֵ��
    * �������������ı�(Table#getAliasTables()����Ŀǰ��ʵ���У������Կ��Բ���д��ϵͳ�����ڲ��Զ�ʶ��
    * ���㷨�ǣ��ж���name���Ƿ����a-z,A-Z��Virtual Column��mask ֵΪ 001010
    */
    public boolean isVirtual();

    /**
     * ���ͳ�Ƶķ�������sum, count,min,aver,max�ȣ�Ŀǰֻ��sum ��֧��
     * �������Ҫͳ�ƣ�����null
     * @return
     */
    public String getSubTotalMethod();
    /**
     * ����ͳ�Ƶ��ֶ�������ĿǰΪ���ϼơ�(��Ӧsum)
     */
    public String getSubTotalDesc();
    
    /**
     * Filter is set to limit the value range.
     *  
     * Filter can contain Session Attribute( start and end with $) or
     * 
     * Page Variable (start and end with @)
     * 
     * Filter will be used by both query UI and Object Create/Modify command.
     * 
       * Filter can has $ or @ variables, $ is from QuerySession attribute,
       * @ is from page context @means some other column's input data, such like:
       * C_BPARTNER_LOCATION_ID is a column of table C_Invoice, it has filter
       * <br>
       *  C_BPARTNER_id=@C_BPARTNER_id@ and AD_Client_ID=$AD_Client_ID$
       * <br>
       *  means the reference object of C_BPARTNER_LOCATION must have C_BPARTNER_id=
       * @C_BPARTNER_id@, the variable @C_BPARTNER_id@ is input value of another column
       * in C_Invoice screen, also should exists in DefaultWebEvent/QuerySession
     * 
     * ���Ӷ�button��֧�֣�����ֶ�Ϊbutton, filter������ý�����button�Ƿ���ʾ����������sql����������filter��:
select count(*) from <table> where id=<id> and $filter, ��count=1ʱ����ʾbutton,������ʾ
     * @return String of that filter
     * @since 2.0
     */
    public String getFilter();
    
    /**
     * Layout in screen UI when creatio/modify
     * 
     * @return 
     * @since 2.0
     */
    public DisplaySetting getDisplaySetting();
    
    /**
     * There will exists a table name "ad_sequence", whose AK is like
     * 'PHC', will used to identify that sequence, if column's obtainmanner
     * is set as "sheetNo", the no will constructed by the sequence rule.
     * @return null if not specified
     * @since 2.0
     */
    public String getSequenceHead();
    
/**
     * �����ֶι��ˣ���ָ��ͬһ�������У�һ���ֶεĹ���������Ҫ�����������ɸ��ֶεĵ�ǰ���á��������������ֶγ�Ϊ�����������ֶΡ�(Column.isFilteredByWildCard ()=true)�������õ��ֶγ�Ϊ�������ֶΡ��������ֶκͺ��������ֶβ�һ����ͬһ�����ϡ�����������
1��	������ϵͳ����ⵥ��ϸ�У���Ʒ�ֶα����Ե���ͷ�����õĻ�����Ϊ��������
2��	��ҩ��ϵͳ�Ĳɹ������У���Ʒ�ֶα����Ե���ͷ�����õ���Ŀ��Ϊ����������
3��	�ڵ�ַ���棬�����ֶα�����ͬ���ʡ���ֶ���Ϊ����������
4��	�ڷ�װϵͳ�У�ӪҵԱ�ֶα�����ͬ��ĵ����ֶ�Ϊ��������

����Լ�����壺
1�������������ֶΡ�����FK���ͣ�
2���������ֶΡ�������ڡ����������ֶΡ����ڱ���ض��ڡ����������ֶΡ����ڱ��PARENT_TABLE_ID ��ָ���ı��ϡ�

     * @return true if ��ǰ�ֶ��Ǻ��������ֶ�
     */
    public boolean isFilteredByWildcard(); 
    /**
     * 
     * @return elements are Column, which is contained in wildcard filter of column
     */
    public List getReferenceColumnsInWildcardFilter();
    /**
     * ͨ������£������޸ĵ��ֶζ������ֹ������룬���⣺
     * ��ǰ�ֶ���FK�ֶΣ�ͬʱ������Ϊ�����ֶε� WildcardFilter��У����������ֶα���ͨ��
     * ���Ҷ�λ�������������룬�Ӷ���֤��ȷ��
     * @return false ������ܽ����ֹ����룬ע�����ﲻУ���Ƿ������޸ġ������������޸ģ���getReferenceTable=true
     * ���ֶν��е�ǰУ�顣
     */
    //public boolean acceptKeyInput(); 
    /**
     * �����ֶ����ƺ���";"�ֺ�ʱ����ʾ���ֶ�Ϊ�����ֶΣ���Ծ�˲�ֹһ�Σ���ʱ����ͨ��
     * #getColumnLink �����Ӧ������
     * @return true if name contains ";"
     * @since 2.0
     */
    public boolean isColumnLink();
    /**
     * �����ֶ����ƺ���";"�ֺ�ʱ����ʾ���ֶ�Ϊ�����ֶΣ���Ծ�˲�ֹһ�Σ�
     * @return column link that end to the last column
     * @since 2.0
     */
    public ColumnLink getColumnLink() throws nds.query.QueryException;
    /**
     * All sub class of  nds.alert.ColumnAlerter should have "static getIntance()" method
     * @return nds.alert.ColumnAlerter
     * @since 2.0
     */
    public Object getUIAlerter();
    /**
     * Whether this column only accept upper case characters
     * @return true when only accept upper case characters
     */
    public boolean isUpperCase();
    /**
     * If column is indexed, it can accept query parameter on page 
     * @return
     * @since 4.0
     */
    public boolean isIndexed();
    /**
     * Will drop down a list for current input and change automatically as user types characters in 
     * text type input
     * Currently only fk column (text) support auto complete type
     * @return true if support auto complete
     */
    public boolean isAutoComplete();
    /**
     * Security grade is for column level security control. the default value is zero. 
     * 
     * User should only get access to columns that have security level lower than him.
     * 
     * Designer should use column json props to set this value.
     *  
     * @return security grade of the column
     */
    public int getSecurityGrade();
    /**
     * When getReferenceColumn returns not null value, this value will be valid.
The ON DELETE clause lets you determine how DB automatically maintains referential integrity if you remove a referenced primary or unique key value. If you omit this clause, then Oracle does not allow you to delete referenced key values in the parent table that have dependent rows in the child table.
Specify CASCADE if you want db to remove dependent foreign key values. 
Specify SET NULL if you want db to convert dependent foreign key values to NULL. 
     *  
     * @return Column.ON_DELETE_CASCADE, Column.ON_DELETE_SETNULL, Column.ON_DELETE_NOACTION
     */
    public int getOnDeleteAction();
    public JSONObject toJSONSimpleObject(Locale locale) throws JSONException;    
    public JSONObject toJSONObject(Locale locale) throws JSONException;
    
    /**
     * Set in ad_column.props as json object.
     * Developer should take care, the returned object should not be modified such as change 
     * property inside. The later implementation will forbid such kind of operation. 
     * @return null or a valid object
     * @since 4.1 
     * 
     */
    public JSONObject getJSONProps();
}

