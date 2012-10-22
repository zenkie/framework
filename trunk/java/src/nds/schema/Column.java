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
     * 可以代替主键的列，例如员工工号，单据流水号（虽然也可能重，但几率小）。一
     * 般我们会把这种字段作为应用表查询时的定位字段。
     * @roseuid 3B81B15D02F7
     */
    public boolean isAlternateKey();

    public boolean isAlternateKey2();
    /**
     * 是否显示主键。一般情况下，显示主键＝AK，但对于特殊情况可以分开
     * 例如：科目表中，AK=Value, DK=Name
     * @return
     */
    public boolean isDisplayKey();
    /**
     * 含义注释
     * @return String of column meaning
     */
    public String getComment();
    /**
     * 含义注释
     * @return String of Showcomments meaning
     */
    public String getShowcomment();
    /**
     * Limit Value Group Name
     * @return
     */
    public String getLimitValueGroupName();
    
    /**
     * 是否可以在页面上显示，像id之类的字段显然不能显示。
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
     * 是否可以在页面上通过Input 框修改，有些字段，如用户名，一旦建立，不许修改
     * ，也就是说，在action= Column.MODIFY 时，不许修改，（通常这时是showable的）
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
     * 小数点后的位数，length - scale -1 = precision，全精度
     * 如number(10,2) length= 13, precision=10, scale= 2
     * 仅仅对number 类型有效
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
     * 对于DATENUMBER形式的字段，系统关联到默认的一张表 (t_day)，此表能够显示日期对应的年，月，季度等信息
     * 做统计分析时，能够在日期字段上进行汇总是必要的。
     * 
     * 由于大多数页面对getReferenceTable=true时识别为fk的显示样式，我们不对getReferenceTable进行关于日期
     * 的支持，而增加本方法。
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
     * 对于text/textarea 类型的column, 存放的内容应该是 ColumnInterpreter 的实现
     * 
     * 对于button 类型的column, 存放的内容可以 存储过程的名称，或具体java类的名称
     *  当内容包含"."的时候，被认为是java class. 否则作为存储过程，界面控制类使用 
     *  nds.web.ButtonCommandUI_Impl. 
     *  java class 如果是实现了nds.web.ButtonCommandUI 接口，则被认为是界面控制类。否则
     *  被认为是命令执行类，界面控制类仍然使用nds.web.ButtonCommandUI_Impl
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
     * @返回表字段的限制规则字符串，用于对数据做操作时验证合法性
     * 如："pz"代表数字必须大于0，"paz"代表数字必须大于等于0;
     * 可以根据需要添加任何对于字段的限制
     */
    public String getLimit();//By Hawke

    /**
     * 返回货币类型
     * @return
     */
    public String getMoney();//By Hawke

    /**
     *   获得需要修改的字段, 在表被修改时，只有getModifiable()==true的字段才会被系统修改，
     *   不管他是否在页面上显示，如一般情况下，modifierID, modifierDate 总是getModifiable=true
     *   ,而他们不一定在页面上显示。
     */
    public boolean getModifiable();//
    /**
     *   获得特定字段的值的获取方式
     */
    public String getObtainManner();//
    /**
     *   获得特定字段的缺省值（通常该字段是不可修改的，并且
     *   每个表中的该字段的值都是一样的）
     *   Will evaluate if value start with "=", which means the content is script
     *   如：permission,status字段
     */
    public String getDefaultValue();//
    /**
     * Get default value without script evaluation. 
     * 对于button 类型的按钮，defautlValue = "Y", 表示需要提示， defaultValue="N" 或其他，表示不需要提示
     * 提示类型的按钮通常需要执行不可取消的动作，提示内容为"您确认需要执行《按钮名称》动作吗？"
     * @param eval false if just want to get the script, not the data
     * @return
     */
    public String getDefaultValue(boolean eval);
    public String getReferenceColumnName();// 返回对应的reference表中对应列的名称
    public String getTableColumnName();    // 返回该列所对应的TABLE中的列
    public Table getObjectTable();         // 返回该列所对应的TABLE
    
    /**
	     * 从4.0开始，被用作在界面上控制输入的设置。
	     * 
	     * 用法1）对于html客户端，将作为input 语句的补充属性, 为javascript的function name，例如，某字段在界面
	     * 上显示为 <input id=xx type='text' value='' onchange=$RegExpression> 配置 RegExpression=
	     * 
	     * 
	     * 用法2）如果 column.getDispalyType="xml"，getRegExpression 将包含一个json 对象，并将被转换为一组属性保存
	     * 到 Column.getProperties 中， xml类型的字段，将不能使用1）方式, 为防止用法1）的程序出现异常，这种内容将不设置到
	     * getRegExpression 中
     *  
     * @return
     */
    public String getRegExpression();  
    /**
     * 将 getRegExpression 获取得json 对象转换为 Properties
     * @since 4.0
     * @return null if not contain any infor in getRegExpression
     */
    public java.util.Properties getProperties();
    public String getErrorMessage();       // 返回错误消息-(用于判断正则表达式出错时)
    /**
    * 字段是否为虚拟字段，虚拟字段就是该字段并非本表的物理字段，而是一个计算值，
    * 或者来自其他的表(Table#getAliasTables()。（目前的实现中，该属性可以不填写，系统将在内部自动识别
    * （算法是：判断在name中是否仅有a-z,A-Z）Virtual Column的mask 值为 001010
    */
    public boolean isVirtual();

    /**
     * 获得统计的方法，如sum, count,min,aver,max等，目前只有sum 被支持
     * 如果不需要统计，返回null
     * @return
     */
    public String getSubTotalMethod();
    /**
     * 返回统计的字段描述，目前为“合计”(对应sum)
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
     * 增加对button的支持，如果字段为button, filter里的设置将决定button是否显示，过滤条件sql，将建立到filter中:
select count(*) from <table> where id=<id> and $filter, 当count=1时，显示button,否则不显示
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
     * 关联字段过滤，是指在同一个界面中，一个字段的过滤条件需要参照另外若干个字段的当前设置。过滤条件所在字段称为“含过滤器字段”(Column.isFilteredByWildCard ()=true)，所引用的字段称为“关联字段”。关联字段和含过滤器字段不一定在同一个表上。典型用例：
1）	在物流系统的入库单明细中，产品字段必须以单据头上设置的货主作为过滤条件
2）	在药招系统的采购订单中，产品字段必须以单据头上设置的项目作为过滤条件。
3）	在地址界面，城市字段必须以同表的省份字段作为过滤条件。
4）	在服装系统中，营业员字段必须以同表的店铺字段为过滤条件

建立约束定义：
1）“含过滤器字段”必是FK类型；
2）“关联字段”如果不在“含过滤器字段”所在表，则必定在“含过滤器字段”所在表的PARENT_TABLE_ID 所指明的表上。

     * @return true if 当前字段是含过滤器字段
     */
    public boolean isFilteredByWildcard(); 
    /**
     * 
     * @return elements are Column, which is contained in wildcard filter of column
     */
    public List getReferenceColumnsInWildcardFilter();
    /**
     * 通常情况下，允许修改的字段都接受手工公输入，例外：
     * 当前字段是FK字段，同时又是作为其他字段的 WildcardFilter的校验项。这样的字段必须通过
     * 查找定位，而不能手输入，从而保证正确性
     * @return false 如果不能接受手工输入，注意这里不校验是否允许修改。仅仅对允许修改，且getReferenceTable=true
     * 的字段进行当前校验。
     */
    //public boolean acceptKeyInput(); 
    /**
     * 当此字段名称含有";"分号时，表示此字段为级联字段（跳跃了不止一次）这时可以通过
     * #getColumnLink 获得相应的链接
     * @return true if name contains ";"
     * @since 2.0
     */
    public boolean isColumnLink();
    /**
     * 当此字段名称含有";"分号时，表示此字段为级联字段（跳跃了不止一次）
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

