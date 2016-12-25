/******************************************************************
*
*$RCSfile: ColumnImpl.java,v $ $Revision: 1.11 $ $Author: Administrator $ $Date: 2006/06/24 00:35:36 $
*
********************************************************************/


package nds.schema;
import nds.query.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Types;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.query.ColumnLink;
import nds.util.*;
import nds.web.button.ButtonCommandUI_Impl;
import bsh.EvalError;
import bsh.Interpreter;
/**
 */
public class ColumnImpl implements Column {
	private static final Log logger = LogFactory.getLog(ColumnImpl.class);
    //warning to programmer: Any time a new variable added, remind add it to clone() method
    private int id=-1;
    private String name;
    private String description;
    private String description2 = null;
    private boolean isNullable;

    private String limit = null;//By hawke
    private String money = null;//By hawke

    private boolean modifiable;  // The next is add by Tony
    private String obtainManner;
    private String defaultValue;
    private String refColumnName;
    private String tableColumnName;
    private Table objectTable;
    private String regExpression;
    private String errorMessage;
    private String comment=null;
    // 2.0
    private String filter;
    private DisplaySetting displaySetting;
    private boolean isUpperCase;
    /**
     * one of nds.schema.SQLTypes
     */
    private int sqltype;
    /**
     * one of Column.DATE, Column.STRING, Column.NUMBER
     */
    private int type;
    private boolean isAlternateKey;
    private boolean isAlternateKey2;//by Hawkins
    private boolean isDisplayKey;
    private String mask;
    private boolean isValueLimited;
    private PairTable values=null; // key: Locale, Value PairTable(key:value, value:desc)
    private int length;
    private Table rTable=null;
    private Column rColumn=null;
    private Table table=null;

    private int displayOrder;
    private String valueInterpreter=null;

    private boolean isVirtual=false;
    private String sumMethod=null;
    private String sumMethodDesc="";
    private int scale=0;
    private String sequenceHead=null;
    private String command=null;
    private transient Object uiConstructor=null;
    private transient Object uiAlerter=null;
    private transient boolean uiAlerterChecked=false;  
    private String valueGroupName; 
    private boolean isIndexed=false;
    private int onDeleteAction= Column.ON_DELETE_NOACTION;
    private int statSize=-1;
    
    private boolean isFilteredByWildcard=false;
    private List referenceColumnsInWildcardFilter=null;
    private Properties props=null;// parsed from regExpression(json type)
    private boolean isAutoComplete=false;
    private JSONObject jsonProps=null;
    private String showcomment;
    private int securityGrade;
	private Boolean showtitle;
	private boolean isRowspan=false;
    /**
     * 
     * @param id
     * @param table
     * @param columnName
     * @param desc
     * @param sqltype nds.query.SQLTypes
     * @param isNull
     */
    public ColumnImpl(int id, Table table,String columnName,String desc,int sqltype, boolean isNull) {
        this.id=id;
        this.table=table;
        this.name=columnName;
        this.description=desc;
        this.sqltype=sqltype;
        this.type=toGrossType(sqltype);
        this.isNullable= isNull;
        this.isVirtual=checkVirtual();
    }
    public void setIsIndexed(boolean b){
    	isIndexed=b;
    }
    public void setOnDeleteAction(String s){
    	if("SETNULL".equalsIgnoreCase(s)) onDeleteAction= Column.ON_DELETE_SETNULL;
    	else if("CASCADE".equalsIgnoreCase(s)) onDeleteAction=Column.ON_DELETE_CASCADE;
    }
    public boolean isFilteredByWildcard(){
    	return isFilteredByWildcard;
    }
    /**
     * �� getRegExpression ��ȡ��json ����ת��Ϊ Properties
     * @since 4.0
     * @return null if not contain any infor in getRegExpression
     */
    public final java.util.Properties getProperties(){
    	return props;
    }
    /**
     * If column is indexed, it can accept query parameter on page 
     * @return
     * @since 4.0
     */
    public boolean isIndexed(){return isIndexed;}
    /**
     * Will drop down a list for current input and change automatically as user types characters in 
     * text type input
     * Currently only fk column (text) support auto complete type
     * @return true if support auto complete
     */
    public boolean isAutoComplete(){
    	return isAutoComplete;
    }
    
    public boolean setIsAutoComplete(boolean b){
    	return isAutoComplete=b;
    }

    /**
     * When getReferenceColumn returns not null value, this value will be valid.
The ON DELETE clause lets you determine how DB automatically maintains referential integrity if you remove a referenced primary or unique key value. If you omit this clause, then Oracle does not allow you to delete referenced key values in the parent table that have dependent rows in the child table.
Specify CASCADE if you want db to remove dependent foreign key values. 
Specify SET NULL if you want db to convert dependent foreign key values to NULL. 
     *  
     * @return Column.ON_DELETE_CASCADE, Column.ON_DELETE_SETNULL, Column.ON_DELETE_NOACTION
     */
    public int getOnDeleteAction(){
    	return onDeleteAction;
    }
    public void setDescription(String desc){
    	this.description=desc;
    }
    public void setIsNullable(boolean n){
    	this.isNullable=n;
    }
    public void setSQLType(int sqlType){
    	this.sqltype=sqlType;
    	type=toGrossType(sqltype);
    }
    /**
     * Limit Value Group Name
     * @return
     */
    public String getLimitValueGroupName(){
    	return valueGroupName;
    }
    /**
 * С������λ����length - scale -1 = precision��ȫ����
 * ��number(10,2) length= 13, precision=10, scale= 2
 * ������number ������Ч
 */
    public int getScale(){
        return scale;
    }
    public void setScale(int scale){ this.scale=scale;}

    private boolean checkVirtual(){
    	char[] cs= name.toCharArray();
    	for ( int i=0;i< cs.length;i++)
    		if ((cs[i]>='0' && cs[i]<='9') || cs[i]=='_' ||
    			cs[i]=='-' || (cs[i]>='a' && cs[i]<='z') ||
    			(cs[i]>='A' && cs[i]<='Z')) continue;
    		else return true;
    	return false;
    }

    public static int toGrossType(int type) {
		int jType;
		switch (type) {
		case 1:
		case 12:
		case 13:
		case 22:
			jType = Column.STRING;
			break;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 8:
		case 9:
		case 10:
		case 7:
		case 16:
			jType = Column.NUMBER;
			break;
		case 23:
			jType = Column.DATENUMBER;
			break;
		case 11:
		case 14:
		case 15:
			jType = Column.DATE;
			break;
	    case 17:
	    case 18:
	    case 19:
	    case 20:
	    case 21:
		default:
			jType = Column.OTHER;
			break;
		}
		return jType;
	}
    
    /**
     * Statistics size of the column, which can be used for length setting in list layout 
     * @return -1 if no setting
     */
    public int getStatSize(){
    	return statSize;
    }
    public void setStatSize(int size){
    	statSize=size;
    }
    /**
     * ����ע��
     * @return String of column meaning
     */
    public String getComment(){
        return comment;
    }
    public void setComment(String s){
        comment=s;
    }
    public void setIsAlternateKey(boolean ak) {
        isAlternateKey=ak;
    }

    public void setIsAlternateKey2(boolean ak2) {
        isAlternateKey2=ak2;
    }
    public void setIsDisplayKey(boolean dk){
    	this.isDisplayKey=dk;
    }
    public boolean isDisplayKey(){
    	return isDisplayKey;
    }
    /**
     * @param mask 10 chars, each as meaning as( '0' as false, '1' as true):
     *      position from left to right:
     *      1   showable( action="ADD")
     *      2   modifiable( action="ADD")
     *      3   showable(action="MODIFY")
     *      4   modifiable(action="MODIFY")
     *      5   showable(action="QUERY List")
     *      6   showable(action="Query SubList")
     *		7   showable(action="Query OBJECTVIEW") ' default equal to bit 5
     *      8   printable(Print List)
     *      9   printable(Print Sublist)
     *     10   printable(Print Object)
     *      so, "111000" means the column should show when add, and allow input,
     *          while during modification, it only allows for viewing, this column
     *          does not allowing showing when query.
     * 
     * From 2.0, add the 7th bit for object view page, if set, will display
     * in object view page, if only 6 chars given, will set as bit 5 ( Query)
     */
    public void setMask(String mask) {
                /** --- yfzhu modified here for security testing--
                if( ! "id".equalsIgnoreCase(name)){
                    char[] ms= mask.toCharArray();
                    ms[4]='1';
                    mask=new String(ms);
                }
                 -- these code should be remove after testing --*/
        this.mask=mask;

    }
    public void setIsValueLimited(boolean limit) {
        isValueLimited=limit;
    }
    /**
     * 
     * @param valueGroupName
     * @param values Locale of TableManager.getDefaultLocale
     */
    public void setValues(String valueGroupName, PairTable vs) {
        this.valueGroupName=valueGroupName.toLowerCase();
    	if(values==null){
    		values=new PairTable();
    	}
        values.put(TableManager.getInstance().getDefaultLocale(), vs);
    }
    public void setLength(int leng) {
        length=leng;
    }
    public void setReferenceTable(Table rt) {
        rTable=rt;
    }
    public void setReferenceColumn(Column rc) {
        rColumn=rc;
    }
    public void setDisplayOrder(int order) {
        this.displayOrder=order;
    }
    public void setValueInterpreter(String interpreter) {
        if( interpreter ==null) {
            valueInterpreter=null;
            return;
        }
        // following lines have been marked up because the sum-field also use this for query string storage
        /*boolean isColumnInterpreter=true;
        try {
            if(! ( Class.forName(interpreter).newInstance() instanceof ColumnInterpreter))
                isColumnInterpreter=false;
        } catch(Exception e) {
            throw new IllegalArgumentException("Error:"+ e);
        }
        if(!isColumnInterpreter)
            throw new IllegalArgumentException(interpreter +" of column \""+this+"\" is not ColumnInterpreter");
        */
        this.valueInterpreter=interpreter;
    }
    //////////////////////////////////////////////////////////////
    //     following methods implementing Column Interface

    /**
     * @roseuid 3B8AFCFD0068
     */
    public boolean isAlternateKey() {
        return isAlternateKey;
    }

    public boolean isAlternateKey2() {
        return isAlternateKey2;
    }

    public boolean isNullable() {
        return isNullable || this.displaySetting.getObjectType()== DisplaySetting.OBJ_CHECK ;
    }
    /**
     * Check whether mask is set on position
     * @param maskPosition should be in in between MASK_CREATE_SHOW and MASK_PRINT_OBJECTVIEW (0-9)
     * @return true if mask is set
     * @since 3.0
     */
    public boolean isMaskSet(int maskPosition){
    	if(maskPosition<0 || maskPosition>=mask.length() ) return false;
    	try{
    		return mask.charAt(maskPosition)=='1';
    	}catch(IndexOutOfBoundsException e){
    		throw new IllegalArgumentException("mask position out of range:"+ maskPosition+",mask:"+ mask);
    	}
    }

    /**
     * @roseuid 3B8AFCFD0090
     */
    public boolean isShowable(int action) {
        char c='0';
        switch(action) {
            case Column.ADD:
                c= mask.charAt(0);
                break;
            case Column.MODIFY:
                c=mask.charAt(2);
                break;
            case Column.QUERY_LIST:
                c=mask.charAt(4);
                break;
            case Column.QUERY_SUBLIST:
                c=mask.charAt(5);
                break;
            case Column.QUERY_OBJECT:
                c=mask.charAt(6);
                break;
            case Column.PRINT_LIST:
                c=mask.charAt(7);
                break;
            case Column.PRINT_SUBLIST:
                c=mask.charAt(8);
                break;
            case Column.PRINT_OBJECT:
                c=mask.charAt(9);
                break;
            default:
                throw new IllegalArgumentException("action is not valid:"+action);
        }
        return c=='1';
    }
    public boolean isModifiable(int action) {
        char c='0';
        switch(action) {
            case Column.ADD:
                c= mask.charAt(1);
                break;
            case Column.MODIFY:
                c=mask.charAt(3);
                break;
            case Column.QUERY_SUBLIST:
            	c=mask.charAt(3);
            	break;
            case Column.QUERY_LIST:
            	c= mask.charAt(1);
            	break;
            case Column.QUERY_OBJECT:
            	c='0';
            	break;
            default:
                throw new IllegalArgumentException("action is not valid:"+action);
        }
        return c=='1';
    }

    /**
     * @roseuid 3B8AFCFD00AE
     */
    public boolean isValueLimited() {
        return isValueLimited;
    }

    /**
     * @roseuid 3B8AFCFD00D6
     */
    public PairTable getValues(Locale locale) {
    	if(values==null ) return null;
    	PairTable pt=(PairTable) values.get(locale);
       	if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode() && pt==null){
   	    	throw new NDSRuntimeException("Not found Values of Locale:"+locale +" of column "+ toString());
       	}
       	if(pt==null){
			pt= new PairTable();
			PairTable defpt=getValues(TableManager.getInstance().getDefaultLocale()); 
			logger.debug("PairTable value local->"+defpt.toJSONString());
			for(Iterator it3= defpt.keys();it3.hasNext();){
				String key= (String)it3.next();
				pt.put(key, MessagesHolder.getInstance().getMessage4(locale,String.valueOf(defpt.get(key))));
			}
//			for(Iterator it=getValues(TableManager.getInstance().getDefaultLocale()).keys();it.hasNext();){
//				Object value= it.next();
//				pt.put(value,MessagesHolder.getInstance().getMessage(locale, ("lmt_"+ this.valueGroupName+"_"+ value).toLowerCase()));
//			}
			logger.debug("PairTable value local->"+pt.toJSONString());
			values.put(locale, pt);
       	}
       	return pt;
    }    

    /**
     * @roseuid 3B8AFCFD00F4
     */
    public int getLength() {
        return length;
    }

    /**
     * @roseuid 3B8AFCFD011C
     */
    public Table getReferenceTable() {
        return rTable;
    }

    public Column getReferenceColumn() {
        return rColumn;
    }
    /**
     * ����DATENUMBER��ʽ���ֶΣ�ϵͳ������Ĭ�ϵ�һ�ű� (t_day)���˱��ܹ���ʾ���ڶ�Ӧ���꣬�£����ȵ���Ϣ
     * ��ͳ�Ʒ���ʱ���ܹ��������ֶ��Ͻ��л����Ǳ�Ҫ�ġ�
     * 
     * ���ڴ����ҳ���getReferenceTable=trueʱʶ��Ϊfk����ʾ��ʽ�����ǲ���getReferenceTable���й�������
     * ��֧�֣������ӱ�������
     * @param tryDateNumberAsFK if true, will take t_day as reference table of datenumber type column
     * @return 
     */
    public Table getReferenceTable(boolean tryDateNumberAsFK){
    	if(rTable!=null) return rTable;
    	if(tryDateNumberAsFK && this.type==Column.DATENUMBER) return TableManager.getInstance().getDateTable();
    	return null;
    }
    public Column getReferenceColumn(boolean tryDateNumberAsFK){
    	if(rColumn!=null) return rColumn;
    	if(tryDateNumberAsFK && this.type==Column.DATENUMBER){
    		Table tb=TableManager.getInstance().getDateTable();
    		if(tb!=null) return tb.getPrimaryKey(); 
    	}
    	return null;
    	
    }

    /**
     * @roseuid 3B8AFCFD0144
     */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /**
     * @roseuid 3B8AFCFD016C
     */
    public String getValueInterpeter() {
        return valueInterpreter;
    }

    /**
     * @roseuid 3B8AFCFD0194
     */
    public int getId() {
        return id;
    }

    /**
     * @roseuid 3B8AFCFD01C7
     */
    public String getName() {
        return name;
    }

    /**
     * @roseuid 3B8AFCFD01EF
     */
    public String getDescription(Locale locale) {
        if( TableManager.getInstance().getDefaultLocale().hashCode()==locale.hashCode())
        	return description; 
        return MessagesHolder.getInstance().getMessage2(locale,
        	( table.getName()+"."+ toMessageKey(name)).toLowerCase(),toMessageKey(name.toLowerCase() ),description);
    }
    
    /**
     * Replace any of "()+/*\.;," with "_", then eliminate spaces
     * @param s
     */
    private String toMessageKey(String s){
    	return spaceKeyPattern.matcher(msgKeyPattern.matcher(s).replaceAll("_")).replaceAll("");
    }
    private static Pattern msgKeyPattern = Pattern.compile("\\p{Punct}");
    private static Pattern spaceKeyPattern = Pattern.compile(" ");
    
    public void setDescription2(String desc2) {// by Hawkins
        description2 = desc2;
    }

    /**
     * @roseuid 3B8AFCFD0217
     */
    public int getSQLType() {
        return sqltype;
    }
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
    public int getType() {
        return type;
    }

    /**
     * @roseuid 3B8AFCFD0249
     */
    public Table getTable() {
        return table;
    }
    //////////////////////////////////////////////////
    /// override Object method
    public int hashCode() {
        return id;
    }
    public boolean equals(Object c) {
        if( (c instanceof Column)&& ((Column)c).getId()== id)
            return true;
        return false;
    }
    public String toString() {
        return table.toString()+"."+name;
    }
    public void clone(Object obj) {
        if(! (obj instanceof ColumnImpl))
            throw new IllegalArgumentException("Not a ColumnImpl");
        ColumnImpl ci=(ColumnImpl)obj;
        description=ci.description;
        description2=ci.description2;
        displayOrder=ci.displayOrder;
        id= ci.id;
        isAlternateKey=ci.isAlternateKey;
        isAlternateKey2=ci.isAlternateKey2;
        mask=ci.mask;
        isValueLimited= ci.isValueLimited;
        length=ci.length;
        name=ci.name;
        rTable=ci.rTable;
        sqltype=ci.sqltype;
        table=ci.table;
        type=ci.type;
        valueInterpreter=ci.valueInterpreter;
        values=ci.values;
        obtainManner = ci.obtainManner ;  // is this right?
        modifiable = ci.modifiable;
        refColumnName = ci.refColumnName ;
        tableColumnName = ci.tableColumnName;
        objectTable = ci.objectTable ;
        jsonProps=ci.jsonProps;
    }
    private void readObject(java.io.ObjectInputStream stream)throws IOException, ClassNotFoundException {
        int cid= stream.readInt();
        clone(TableManager.getInstance().getColumn(cid));
    }
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeInt(id);
    }

    public String getLimit(){//By Hawke
        return limit;
    }
    public void setLimit(String limit){//By Hawke
        this.limit = limit;
    }
    public String getMoney(){//By Hawke
        return money;
    }
    public void setMoney(String money){//By Hawke
        this.money = money;
    }
    // The next method is added by tony
    public boolean getModifiable(){
        return modifiable;
    }
    public void setModifiable(boolean modifiable){
        this.modifiable = modifiable;
    }
/*
*   �����õ�ĳ�еĻ��ֵ�ķ�ʽ
*/

    public String getObtainManner(){
        return obtainManner;
    }
    public void setObtainManner(String obtainManner){
        this.obtainManner = obtainManner;
    }
/*
*   �����õ�ĳ�е�ȱʡֵ��status,permission��ͨ������һ��ָ����ֵ
*/
    /** 
     * ����button ���͵İ�ť��defautlValue = "Y", ��ʾ��Ҫ��ʾ�� defaultValue="N" ����������ʾ����Ҫ��ʾ
    * ��ʾ���͵İ�ťͨ����Ҫִ�в���ȡ���Ķ�������ʾ����Ϊ"��ȷ����Ҫִ�С���ť���ơ�������"
	*/
    public String getDefaultValue(){
        return getDefaultValue(true);
    }
    /**
     * Get default value without script evaluation
     * ����button ���͵İ�ť��defautlValue = "Y", ��ʾ��Ҫ��ʾ�� defaultValue="N" ����������ʾ����Ҫ��ʾ
    * ��ʾ���͵İ�ťͨ����Ҫִ�в���ȡ���Ķ�������ʾ����Ϊ"��ȷ����Ҫִ�С���ť���ơ�������"
     * @param eval false if just want to get the script, not the data
     * @return
     */
    public String getDefaultValue(boolean eval){
    	if ( eval && defaultValue !=null && "=".equals(defaultValue.substring(0,1))){
            // take as script
            return ""+ QueryUtils.evalScript(defaultValue);
        }else
       return defaultValue;    	
    }
    public void setDefaultValue(String defaultValue){
        this.defaultValue = defaultValue;
    }

    public void setReferenceColumnName(String columnName) {
        refColumnName=columnName;
    }

     public String getReferenceColumnName() {
        return refColumnName;
    }
// ������ù����ı��ж�Ӧ���ֶ�,��ĳЩ���е��ֶ��õ��ı�product :retailUP
    public void setTableColumnName(String columnName) {
        tableColumnName=columnName;
    }

     public String getTableColumnName() {
        return tableColumnName;
    }
// ������ù����ı�,��ĳЩ���е��ֶ��õ��ı�product :retailUP
    public void setObjectTable(Table objectTable) {
        this.objectTable=objectTable;
    }

     public Table getObjectTable() {
        return objectTable;
    }

   /*
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
    */
    public void setRegExpresion(String regExpression) {
        /**
         * ��displaytype='xml' regExpression Ӧ������ȷ����Ϊjson �������ͽ�����ΪProperties
         * �����и�����Ҫ��DisplaySetting �����ڵ��ô˷���ǰ���� 
         * @since 4.0
         */
        if(Validator.isNotNull(regExpression)){
        	if(this.displaySetting==null) throw new NDSRuntimeException("Must call setDisplaySetting before setRegExpresion for Column");
        	if(this.displaySetting.getObjectType()== DisplaySetting.OBJ_XML){
	        	try{
	        		org.json.JSONObject jo=new org.json.JSONObject(regExpression);
	        		props=new Properties();
	        		for(Iterator it=jo.keys();it.hasNext();){
	        			String key=(String) it.next();
	        			props.setProperty(key, jo.getString(key));
	        		}
	        	}catch(Throwable t){
	        		throw new NDSRuntimeException("Could not load regExpression as json object:"+t,t);
	        	}
        	}else{
        		/**
        		 * For other display type, will set string to property "regExpression"
        		 */
        		this.regExpression = regExpression;
        	}
        }
    }
    /**
     * ��4.0��ʼ���������ڽ����Ͽ�����������á�����html�ͻ��ˣ�����Ϊinput ���Ĳ������ԣ����磬ĳ�ֶ��ڽ���
     * ����ʾΪ <input id=xx type='text' value='' $RegExpression> ���� RegExpression=
     * 	"onclick='' onchange=''" �����ԣ������������� 'READONLY' ���ƽ���ֻ��
     * 
     * @return
     */
    public String getRegExpression() {
        return regExpression;
    }
// �������ĳ���г���ʱ�ĳ�����Ϣ(������ָ���ж�Regular Expressionʱ�������Ϣ��)
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public String getErrorMessage(){
        return errorMessage;
    }
    /**
    * �ֶ��Ƿ�Ϊ�����ֶΣ������ֶξ��Ǹ��ֶβ��Ǳ���������ֶΣ�����һ������ֵ��
    * �������������ı�(Table#getAliasTables()����Ŀǰ��ʵ���У������Կ��Բ���д��ϵͳ�����ڲ��Զ�ʶ��
    * ���㷨�ǣ��ж���name���Ƿ����a-z,A-Z��Virtual Column��mask ֵΪ 001010
    */
    public boolean isVirtual(){
    	return isVirtual;
    }
    /**
     * ���ͳ�Ƶķ�������sum, count,min,aver,max�ȣ�Ŀǰֻ��sum ��֧��
     * �������Ҫͳ�ƣ�����null
     * @return
     */
    public String getSubTotalMethod(){
        return this.sumMethod ;
    }
    /**
     * ����ͳ�Ƶ��ֶ�������ĿǰΪ���ϼơ�(��Ӧsum)
     */
    public String getSubTotalDesc(){
        return this.sumMethodDesc ;
    }
    public void setSubTotalMethod(String method){
        this.sumMethod = method;
    }
    
    /**
     * 
     * @param filter
     * @since 2.0
     */
    public void setFilter(String filter){
    	this.filter=filter;
    	if(nds.util.Validator.isNotNull(filter)&& (this.getReferenceTable()!=null 
    			|| this.getDisplaySetting().getObjectType()==DisplaySetting.OBJ_XML ) 
    			&&(filter.indexOf('@')>-1 )){ //|| filter.indexOf('$')>-1
    		
    		this.isFilteredByWildcard=true;
    		// TableManager will do further check on this, see checkWildcardFilter, whihc may also
    		// reset isFilteredByWildcard
    	}
    }
    /**
     * TableManager may reset this property
     * @param b
     */
    void setIsFilteredByWildcard(boolean b){
    	this.isFilteredByWildcard=b;
    }
    /**
     * This is called in TableManager.checkWildcardFilter, which operates after all columns
     * loaded into memory
     * @param columns
     */
    public void setReferenceColumnsInWildcardFilter(List columns){
    	this.referenceColumnsInWildcardFilter= columns;
    }
    /**
     * @return List when column isFilteredByWildcard, null when else
     */
    public List getReferenceColumnsInWildcardFilter(){
    	return referenceColumnsInWildcardFilter;
    }
    /**
     * @since 2.0
     */
    public String getFilter(){
    	return filter;
    }
    /**
     * Layout in screen UI when creatio/modify
     * 
     * @return 
     * @since 2.0
     */
    public DisplaySetting getDisplaySetting(){
    	return displaySetting;
    }    
    public void setDisplaySetting(DisplaySetting setting){
    	this.displaySetting=setting;
    }
    public String getSequenceHead(){
    	return sequenceHead;
    }
    public void setSequenceHead(String s){
    	this.sequenceHead=s;
    }
    /**
     * �����ֶ����ƺ���";"�ֺ�ʱ����ʾ���ֶ�Ϊ�����ֶΣ���Ծ�˲�ֹһ�Σ���ʱ����ͨ��
     * #getColumnLink �����Ӧ������
     * @return true if name contains ";"
     * @since 2.0
     */
    public boolean isColumnLink(){
    	if(this.name.indexOf(";")>-1){
    		try{
    			getColumnLink();
    			return true;
    		}catch(Exception e){
    			return false;
    		}
    	}
    	return false;
    }
    /**
     * �����ֶ����ƺ���";"�ֺ�ʱ����ʾ���ֶ�Ϊ�����ֶΣ���Ծ�˲�ֹһ�Σ�
     * @return column link that end to the last column
     * @since 2.0
     */
    public ColumnLink getColumnLink() throws nds.query.QueryException{
    	return new ColumnLink(table.getName()+"."+ name);
    }

    /**
     * Object construct UI for this column , may be instance of nds.web.ButtonCommandUI when button
     * 
     * @return object construct UI 
     */
    public Object getUIConstructor(){
    	// current only button type supported
    	if( uiConstructor ==null && this.getDisplaySetting().getObjectType()==DisplaySetting.OBJ_BUTTON ){
    		if(Validator.isNull(this.getValueInterpeter())) throw new NDSRuntimeException("Not found interpreter of button column "+ this);
    		if( this.getValueInterpeter().indexOf('.')<0)
    			uiConstructor= ButtonCommandUI_Impl.getInstance();
    		else{
    			Object o=null;
    			try{
    				o=Class.forName(this.getValueInterpeter()).newInstance();
    			}catch(Exception e){
    				throw new NDSRuntimeException("Interpreter of button column "+ this +" could not be instantiated:"+ e);
    			}
				if(o instanceof nds.web.button.ButtonCommandUI){
					uiConstructor =(nds.web.button.ButtonCommandUI)o;
				}else{
					// when class is valid but not ButtonCommandUI, will take it as delegator ( but should be instanceof Command)
					if(o instanceof Command)
						uiConstructor =ButtonCommandUI_Impl.getInstance();
					else{
						throw new NDSRuntimeException("Class Interpreter of button column "+ this +" should be either ButtonCommandUI or Command");
					}
				}
    		}
    	}
    	return uiConstructor;
    }
    /**
     * All sub class of  nds.alert.ColumnAlerter should have "static getIntance()" method
     * @return nds.alert.ColumnAlerter
     */
    public Object getUIAlerter(){
    	if(!uiAlerterChecked){
    		if(Validator.isNotNull(this.getValueInterpeter()) &&  this.getValueInterpeter().indexOf('.')>0  ){
    			try{
    				Object o=Class.forName(this.getValueInterpeter()).getMethod("getInstance",null).invoke(Class.forName(this.getValueInterpeter()),null);
    				if(o instanceof nds.web.alert.ColumnAlerter)uiAlerter=(nds.web.alert.ColumnAlerter)o;
    			}catch(Exception e){
    				System.out.println("Found error in get ui alerter:"+ e);
    			}
    		}
    		uiAlerterChecked=true;
    	}
    	return uiAlerter;
    }
    public void setIsUpperCase(boolean b){
    	isUpperCase=b;
    }
    /**
     * Whether this column only accept upper case characters
     * @return true when only accept upper case characters
     */
    public boolean isUpperCase(){
    	return isUpperCase;
    }
    /**
     * Security grade is for column level security control. the default value is zero. 
     * 
     * User should only get access to columns that have security level lower than him.
     * 
     * Designer should use column json props to set this value.
     *  
     * @return security grade of the column
     */
    public int getSecurityGrade(){
    	return securityGrade;
    }
    /**
     * Load only nessisary property that UI needed for column check
     * @param locale
     * @return
     * @throws JSONException
     */
    public JSONObject toJSONSimpleObject(Locale locale) throws JSONException{
		JSONObject jo=new JSONObject();
		jo.put("id", id);
		//jo.put("name",name);
		jo.put("description",getDescription(locale));
		jo.put("isNullable", isNullable);
		//jo.put("obtainManner",obtainManner);
		//jo.put("defaultValue", defaultValue);
		jo.put("refColumnId", getReferenceColumn()==null? -1: getReferenceColumn().getId());
		if(getReferenceColumn()!=null){
			jo.put("refTableAK", this.getReferenceTable().getAlternateKey().toJSONSimpleObject(locale));
		}
		//jo.put("refTableId",getReferenceTable()==null?-1: getReferenceTable().getId());
		//jo.put("isAlternateKey",isAlternateKey);
		jo.put("mask", mask);
		jo.put("isValueLimited",isValueLimited);
		//jo.put("values", values);
		//jo.put("isUpperCase",isUpperCase);
		//jo.put("filter", filter);
		//jo.put("displaySetting",displaySetting.getObjectTypeString());
		jo.put("type", type);
		//jo.put("length",length);
		//jo.put("scale",scale);
		//jo.put("valueInterpreter", valueInterpreter);
		//jo.put("table",table.getId());
		//jo.put("isVirtual", isVirtual);
		//jo.put("isIndexed", isIndexed);
		return jo;
	} 
    public JSONObject toJSONObject(Locale locale) throws JSONException{
		JSONObject jo=new JSONObject();
		jo.put("id", id);
		jo.put("name",name);
		jo.put("description",getDescription(locale));
		jo.put("isNullable", isNullable);
		jo.put("obtainManner",obtainManner);
		jo.put("defaultValue", defaultValue);
		jo.put("refColumnId", getReferenceColumn()==null? -1: getReferenceColumn().getId());
		if(getReferenceColumn()!=null){
			jo.put("refTableAK", this.getReferenceTable().getAlternateKey().toJSONObject(locale));
		}		
		jo.put("refTableId",getReferenceTable()==null?-1: getReferenceTable().getId());
		jo.put("isAlternateKey",isAlternateKey);
		jo.put("mask", mask);
		jo.put("isValueLimited",isValueLimited);
		jo.put("values", values);
		jo.put("isUpperCase",isUpperCase);
		jo.put("filter", filter);
		jo.put("displaySetting",displaySetting.getObjectTypeString());
		jo.put("type", type);
		jo.put("length",length);
		jo.put("scale",scale);
		jo.put("valueInterpreter", valueInterpreter);
		jo.put("table",table.getId());
		jo.put("isVirtual", isVirtual);
		jo.put("isIndexed", isIndexed);
		jo.put("isAutoComplete", isAutoComplete);
		jo.put("props", jsonProps);
		return jo;
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
    	// update security grade info
    	if(jo!=null)
    		this.securityGrade = jo.optInt("sgrade",0);
    }
    /**
     * 
     * set in ad_column.showcomment as string
     * cyl 10.15
     * 
     */
    public String getShowcomment(){
    	return showcomment;
    }
    public void setShowcomment(String shcomment){
    	this.showcomment= shcomment;
    }    
    
    /***
     * set in ad_column.showtitle as string
     */
    
    public Boolean getShowtitle(){
    	return showtitle;
    }
    public void setShowtitle(Boolean ishowtite){
    	this.showtitle= ishowtite;
    }
	
	public Boolean getRowspan() {
		// TODO Auto-generated method stub
		return isRowspan;
	}
	
    public void setRowspan(Boolean isRowspan){
    	this.isRowspan= isRowspan;
    }
}
