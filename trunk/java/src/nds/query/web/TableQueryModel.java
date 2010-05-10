/******************************************************************
*
*$RCSfile: TableQueryModel.java,v $ $Revision: 1.5 $ $Author: Administrator $ $Date: 2006/06/24 00:35:05 $
*
*$Log: TableQueryModel.java,v $
*Revision 1.5  2006/06/24 00:35:05  Administrator
*no message
*
*Revision 1.4  2005/12/18 14:06:16  Administrator
*no message
*
*Revision 1.3  2005/08/28 00:27:04  Administrator
*no message
*
*Revision 1.2  2005/05/27 05:01:49  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.4  2003/09/29 07:37:18  yfzhu
*before removing entity beans
*
*Revision 1.3  2003/05/29 19:40:03  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/03/30 08:11:41  yfzhu
*Updated before subtotal added
*
*Revision 1.7  2002/01/04 01:43:22  yfzhu
*no message
*
*Revision 1.6  2001/12/09 03:43:32  yfzhu
*no message
*
*Revision 1.5  2001/11/29 00:49:27  yfzhu
*no message
*
*Revision 1.4  2001/11/22 06:28:18  yfzhu
*no message
*
*Revision 1.3  2001/11/13 22:37:14  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.query.web;

import java.util.*;

import nds.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.schema.*;

/**
 * Title:        进销存系统
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      GiniusGift
 * @author
 * @version 1.0
 */

public class TableQueryModel {
    private static Logger logger= LoggerManager.getInstance().getLogger(TableQueryModel.class.getName());
    private Table mainTable = null;
    private HashMap queryColumns = null;//key:column(Column), value:QueryColumn
    private TableManager manager ;
    private boolean showInHtml;
    public TableQueryModel(int tableId, Locale locale) throws Exception {
        this(tableId, false,locale);
    }
    /**
     * @param showNullIndicator if true, the not nullable column will have (*) on its name
     * this is equal to TableQueryModel(tableId, Column.QUERY_LIST, showNullIndicator)
     */
    public TableQueryModel(int tableId,boolean showNullIndicator,Locale locale) throws Exception {
        this(tableId,Column.QUERY_LIST, showNullIndicator,locale);
    }
    /**
     * @param showNullIndicator if true, the not nullable column will have (*) on its name
     * @param actionType can be :
     *      Column.ADD:Column.MODIFY:Column.QUERY_LIST
     *
     */
    public TableQueryModel(int tableId,int actionType,boolean showNullIndicator, Locale locale) throws Exception {
        this(tableId,actionType, showNullIndicator, false,locale);
    }
    /**
     * @param isHtml if showNullIndicator and isHtml are all true, then the indicator will be red
     */
    public TableQueryModel(int tableId,int actionType,boolean showNullIndicator, boolean isHtml, Locale locale) throws Exception {
        manager= TableManager.getInstance();
        mainTable=manager.getTable(tableId);
        this.showInHtml= isHtml;
        queryColumns=new HashMap();
        ArrayList showColumns= mainTable.getShowableColumns(actionType);
        createColumns(showColumns, showNullIndicator,locale);
    }
    public TableQueryModel(int tableId,int[] columnMasks,boolean showNullIndicator,boolean isHtml,Locale locale,int securityGrade) throws Exception {
        manager= TableManager.getInstance();
        mainTable=manager.getTable(tableId);
        this.showInHtml= isHtml;
        queryColumns=new HashMap();
        createColumns(mainTable.getColumns(columnMasks, false,securityGrade), showNullIndicator,locale);
        
    }
    public TableQueryModel(int tableId,ArrayList columns,boolean showNullIndicator,boolean isHtml,Locale locale) throws Exception {
        manager= TableManager.getInstance();
        mainTable=manager.getTable(tableId);
        this.showInHtml= isHtml;
        queryColumns=new HashMap();
        createColumns(columns, showNullIndicator,locale);
        
    }

    public Table getTable() {
        return mainTable;
    }
    private QueryColumn getQueryColumn(Column column) {
        return (QueryColumn)queryColumns.get(column);
    }
    private QueryColumn getQueryColumn(String name) {//by Hawkins
        return (QueryColumn)queryColumns.get(name);
    }
    public String getDescriptionForColumn(Column column) {
        QueryColumn c= getQueryColumn(column);
        return c.desc;
    }
    /**
     * @return If column specified column is FK, this will return the AK column
     * description of Reference Table, else return "";
     */
    public String getDescriptionForFKColumn(Column column) {
        QueryColumn c= getQueryColumn(column);
        return c.fkDesc;
    }
    public String getDescriptionForColumn(String name) {//by Hawkins
        QueryColumn c= getQueryColumn(name);
        return c.desc;
    }
    /**
     * @return If name specified column is FK, this will return the AK column
     * description of Reference Table, else return "";
     */
    public String getDescriptionForFKColumn(String name){
        return getQueryColumn(name).fkDesc ;
    }
    public String getNameForInput(Column column) {
        QueryColumn c= getQueryColumn(column);
        return c.inputName;
    }
    public String getNameForSelect(Column column) {
        QueryColumn c= getQueryColumn(column);
        return c.selectName;
    }
    public String getNameForSelect(String name) {//by Hawkins
        QueryColumn c= getQueryColumn(name);
        return c.selectName;
    }
    public int getSizeForInput(Column column) {
        QueryColumn c= getQueryColumn(column);
        return c.inputSize;
    }
    public String getColumns(Column column) {
        QueryColumn c= getQueryColumn(column);
        return c.columns;
    }
    public String getColumns(String name) {
        QueryColumn c= getQueryColumn(name);
        return c.columns;
    }
    public String getTypeMeaningForInput(Column column) {
        QueryColumn c= getQueryColumn(column);
        return c.typeMeaning;
    }
    /**
     * Get request vector of querying tables who has FK set on this table and the <code>id</code>
     * specified object, request should has permission(READ) on the table
     * @param id the record specified
     * @return Vector of elements: String[2], 0 for name desc, 1 for url parameter
     */
    public Vector getRequestList(int id, javax.servlet.http.HttpServletRequest request) throws QueryException {
		Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
		if(locale==null)locale= TableManager.getInstance().getDefaultLocale();        
    	Iterator it=manager.getColumnsBeingReferred(mainTable.getPrimaryKey().getId());
        Vector v=new Vector();
        while( it.hasNext()) {
            Column column=(Column) it.next();
            Table table= column.getTable();
            try{
                nds.control.web.WebUtils.checkTableQueryPermission(table.getName(), request);
            }catch(nds.security.NDSSecurityException e){
                continue;
            }
            String[] s=new String[2];
            s[0]= table.getDescription(locale)+"["+ column.getDescription(locale)+"]";
            s[1]= "table="+ table.getId()+"&column="+column.getId()+"&show_all=true&id="+ id;
            v.addElement(s);
        }
        return v;
    }
    public String createValuesList( String name,nds.util.PairTable pt, Locale locale) {
        StringBuffer htmlString = new StringBuffer();
        if(pt ==null)
            return htmlString.toString();
        
        Iterator temp = pt.keys();
        htmlString.append("<select name='"+name+"'>\r\n");
        htmlString.append("<option value='0' selected > "+MessagesHolder.getInstance().getMessage(locale, "combobox-select")+"</option>\r\n");
        while(temp.hasNext()) {
            Object keyValue = temp.next();
            htmlString.append("<option value=' "+keyValue+"' > "+pt.get(keyValue)+"</option>\r\n");
        }
        htmlString.append("</select>\r\n");
        return htmlString.toString();
    }
    public String createValuesList( String name,nds.util.PairTable pt, String defaultValue) {
        StringBuffer htmlString = new StringBuffer();
        if(pt ==null)
            return htmlString.toString();
        Iterator temp = pt.keys();
        htmlString.append("<select name='"+name+"'>\r\n");
        defaultValue=defaultValue.trim();
        while(temp.hasNext()) {
            Object keyValue = temp.next();
            htmlString.append("<option value=' "+keyValue+"' ");
            if(pt.get(keyValue).equals(defaultValue))
                htmlString.append(" selected ");
            htmlString.append("> "+pt.get(keyValue));
            htmlString.append(" </option>\r\n");
        }
        htmlString.append("</select>\r\n");
        return htmlString.toString();
    }
    /**
     * 
     */
    private void createColumns(ArrayList showColumns, boolean showNullableIndicator,Locale locale) {
        //ArrayList showColumns= mainTable.getShowableColumns(actionType);
        
        String desc, inputName, selectName, columns;
        Column column;
        int size,type,i;String fkDesc;
        for(i=0;i<showColumns.size();i++) {
            column = (Column)showColumns.get(i);
            desc= column.getDescription(locale);
            columns=""+column.getId();
            inputName="param/"+i;
            selectName="select/"+i;
            fkDesc="";
            if( column.getReferenceTable() !=null) {
                Column c2= column.getReferenceTable().getAlternateKey();
                if( c2 ==null) {
                    logger.error("Found Column error:"+ column+", whose reference table has no AK.");
                    size=column.getLength();
                    type=column.getType();
                } else {
                    size= c2.getLength();
                    type=c2.getType();
                    //desc += "" + c2.getDescription(locale);
                    fkDesc= c2.getDescription(locale);
                    columns +=","+c2.getId();
                }
            } else {
                size= column.getLength();
                type=column.getType();
            }
            if( showNullableIndicator && !column.isNullable()){
                if (showInHtml)  desc +="<font color='red'>*</font>";
                else desc +="*";
            }
            QueryColumn qc=new QueryColumn(desc,inputName,selectName,columns,size,toTypeDesc(column,locale),fkDesc);
            queryColumns.put(column,qc);
        }


    }
    /**
     * Get text input css class
     * @param columnsPerRow how many columns in one row of object page
     * @param column if column is fk, text input class will consider the ucase of reference column
     * @return css class name, in format like "ucase ipt-3-2", 3 is for columnsPerRow, 2 means column
     * was defined to occupy 2 columns in page.
     */
    public static String getTextInputCssClass(int columnsPerRow, Column column){
    	StringBuffer css=new StringBuffer();
    	DisplaySetting ds= column.getDisplaySetting();
    	if((column.getReferenceTable()!=null && column.getReferenceTable().getAlternateKey().isUpperCase())||
            	column.isUpperCase()){
            css=new StringBuffer("ucase ipt");
        }else{
        	css=new StringBuffer("ipt");
        }
    	css.append("-").append(columnsPerRow).append("-").append(ds.getColumns());
    	
    	if( (column.isFilteredByWildcard() && Validator.isNotNull(column.getRegExpression())) || ds.getObjectType()== DisplaySetting.OBJ_XML){
    		// mark readonly
    		/**
    		 * 对于客户端的wildcardfilter 类型的字段,并且配置了事件触发程序，配置为只读
    		 */
    		css.append(" readonly");
    	}
    	if(!column.isNullable()) css.append(" nl");
    	return css.toString();
    }
    /**
     * Get textarea input css class
     * @param columnsPerRow how many columns in one row of object page
     * @param column if column is fk, text input class will consider the ucase of reference column
     * @return css class name, in format like "ucase ta-3-2-6", 3 is for columnsPerRow, 2 means column
     * was defined to occupy 2 columns in page, 6 meas 6 rows in page
     */
    public static String getTextAreaInputCssClass(int columnsPerRow, Column column){
    	StringBuffer css=new StringBuffer();
    	DisplaySetting ds= column.getDisplaySetting();
    	if((column.getReferenceTable()!=null && column.getReferenceTable().getAlternateKey().isUpperCase())||
            	column.isUpperCase()){
            css=new StringBuffer("ucase ta");
        }else{
        	css=new StringBuffer("ta");
        }
    	css.append("-").append(columnsPerRow).append("-").append(ds.getColumns()).append("-").append(ds.getRows());
    	if(ds.getObjectType()== DisplaySetting.OBJ_XML){
    		// mark readonly
    		/**
    		 * 对于客户端的wildcardfilter 类型的字段,并且配置了事件触发程序，配置为只读
    		 */
    		css.append(" readonly");
    	}    	
    	return css.toString();
    }    
    public static String toTypeDesc(int type, int maxLength, Locale locale) {
        /*
         * For fast loading yfzhu 2008-09-05
         */
    	if(true) return "";
        
    	String desc;
        String cm;
        switch (type) {
        	case Column.STRING :
	            desc="<img src='"+WebKeys.NDS_URI+"/images/char.gif' width=16 height=18 align=absmiddle title='"+MessagesHolder.getInstance().getMessage(locale, "string-format1")  + (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")+":"+maxLength:"")+"'>";
	            break;
            case Column.NUMBER :
                desc="<img src='"+WebKeys.NDS_URI+"/images/num.gif' width=16 height=18 align=absmiddle title='" + MessagesHolder.getInstance().getMessage(locale, "number-format1") + (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")+":"+maxLength:"")+"'>";
                break;
            case Column.DATENUMBER :
                desc="<img src='"+WebKeys.NDS_URI+"/images/datenum.gif' width=16 height=18 align=absmiddle title='" + MessagesHolder.getInstance().getMessage(locale, "datenumber-format1") + (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")+":"+maxLength:"")+"'>";
                break;
            case Column.DATE :
                desc="<img src='"+WebKeys.NDS_URI+"/images/date.gif' width=16 height=18 align=absmiddle title='"+MessagesHolder.getInstance().getMessage(locale, "date-format1") +"' >";
                break;
            default :
                desc="N/A";
                break;
        }
        return "<font size=2>"+desc+"</font>";

    }
    /**
     * 
     * @param type "n" for number, "s" for string, "d" for datenumber 
     * @param maxLength
     * @param locale
     * @return
     */
    public static String toTypeDesc(String type, int maxLength, Locale locale) {
    	 /*
         * For fast loading yfzhu 2008-09-05
         */
    	if(true) return "";
    	
    	String desc;
        String cm;
        if("s".equalsIgnoreCase(type)) {
        	desc="<img src='"+WebKeys.NDS_URI+"/images/char.gif' width=16 height=18 align=absmiddle title='"+MessagesHolder.getInstance().getMessage(locale, "string-format1")  + (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")+":"+maxLength:"")+"'>";
        }else if("n".equalsIgnoreCase(type)){
            desc="<img src='"+WebKeys.NDS_URI+"/images/num.gif' width=16 height=18 align=absmiddle title='" + MessagesHolder.getInstance().getMessage(locale, "number-format1") + (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")+":"+maxLength:"")+"'>";
        }else if("d".equalsIgnoreCase(type)){
            desc="<img src='"+WebKeys.NDS_URI+"/images/datenum.gif' width=16 height=18 align=absmiddle title='" + MessagesHolder.getInstance().getMessage(locale, "datenumber-format1") + (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")+":"+maxLength:"")+"'>";
        }else{        
            desc="N/A";
        }
        return "<font size=2>"+desc+"</font>";
    }
    public static String toTypeDesc(int type, Locale locale){
        return toTypeDesc(type, 0, locale);
    }
    public static String toTypeDesc(Column column, Locale locale){
        String desc=null;
        Table refTable= column.getReferenceTable();
        if( refTable !=null) {
               desc=toTypeDesc(refTable.getAlternateKey().getType(), refTable.getAlternateKey().getLength() ,locale);
        }else
            desc=toTypeDesc(column.getType(), column.getLength(),locale);
        return desc;
    }
    public  static String toTypeIndicator(Column column, Locale locale){
        return toTypeIndicator(column, null, locale);
    }
    public static String toTypeIndicator(Column column, String inputBoxName, Locale locale){
        String desc=null;
        Table refTable= column.getReferenceTable();
        if( refTable !=null) {
               desc=toTypeIndicator(refTable.getAlternateKey().getType(), refTable.getAlternateKey().getLength(), column.getComment(),inputBoxName,locale);
        }else{
            desc=toTypeIndicator(column.getType(), column.getLength(), column.getComment(),inputBoxName, locale);
        }
        return desc;
    }
    public static String toTypeIndicator(int type,int maxLength, String comments, Locale locale) {
        return toTypeIndicator( type, maxLength, comments,null, locale);
    }
    /**
     * Show as input box's title
     * @param column
     * @param inputBoxName
     * @param locale
     * @return
     */
    public static String getInputBoxIndicator(Column column, String inputBoxName, Locale locale) {
    	Table refTable= column.getReferenceTable();
    	int type, maxLength;
    	if( refTable !=null) {
            //desc=toTypeIndicator(refTable.getAlternateKey().getType(), refTable.getAlternateKey().getLength(), column.getComment(),inputBoxName,locale);
    		type= refTable.getAlternateKey().getType();
    		maxLength=refTable.getAlternateKey().getLength();
    	}else{
    		//desc=toTypeIndicator(column.getType(), column.getLength(), column.getComment(),inputBoxName, locale);
    		type= column.getType();
    		maxLength=column.getLength();
    	}
    	
        String desc=null;
        String imageCalendar;
        switch (type) {
	        case Column.STRING :
	            desc= MessagesHolder.getInstance().getMessage(locale, "string-format2")+ (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")  +":"+maxLength:"");
	            break;
	        case Column.NUMBER :
	            desc= MessagesHolder.getInstance().getMessage(locale, "number-format2") +
	            	(maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")  +":"+maxLength:"");
	            if(column.getScale()>0)desc +=","+column.getScale();
	            break;
	        case Column.DATENUMBER :
	        case Column.DATE :
        		desc= MessagesHolder.getInstance().getMessage(locale, "datenumber-format2");// + (maxLength>0?","+MessagesHolder.getInstance().getMessage(locale, "format-length")  +":"+maxLength:"");
	            break;
	        default :
	            desc="";
	        break;
        }
        return  desc;

    }
    public static String toTypeIndicator(int type,int maxLength, String comments, String inputBoxName, Locale locale) {
        String desc="";
        String imageCalendar;
        if(type==Column.DATENUMBER||type==Column.DATE){
        	if(inputBoxName==null)
        		desc="<img src='"+WebKeys.NDS_URI+"/images/datenum.gif' width=16 height=18 align=absmiddle>";
        	else{
            	imageCalendar="imageCalendar"+ nds.util.Sequences.getNextID("TableQueryModel");
            	
            	//<span id="<%=namespace%>cbt_<%=column.getId()%>"  onaction="<%=fkQueryModel.getButtonClickEventScript()%>"><img border=0 width=16 height=16 align=absmiddle src='<%=fkQueryModel.getImageURL()%>' title='<%= PortletUtils.getMessage(pageContext, "open-new-page-to-search" ,null)%>'></span>
            	//desc ="<span id='"+ imageCalendar+"' onaction=\"javascript:showCalendar('"+imageCalendar+"',false,'"+inputBoxName+"',null,null,true);\"><img width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></span><script>createButton(document.getElementById(\""+imageCalendar+"\"));</script>";
            	
            	/*  must has class named as coolButton here as javascript use this class to locate buttons and hide/show/fix column*/
                desc ="&nbsp;<a class=\"coolButton\" onclick=\"event.cancelBubble=true;\" href=\"javascript:showCalendar('"+imageCalendar+"',false,'"+inputBoxName+"',null,null,true);\"><img id='"+imageCalendar+"' width='16' height=18 src='"+WebKeys.NDS_URI+"/images/datenum.gif' border='0' align='absmiddle'></a>";
        	}
        }
        
        return desc;

    }

    
    public static String toTypeIndicator(int type,Locale locale) {
        return toTypeIndicator(type, -1,null, locale);
    }
    
    
    private class QueryColumn {
        String desc;
        String inputName,selectName, columns;
        int inputSize;
        String typeMeaning;
        String fkDesc;
        public QueryColumn(String d,String n,String s,String c, int i,String t,String fkd) {
            desc=d;
            inputName=n;
            selectName=s;
            inputSize=i;
            typeMeaning=t;
            columns=c;
            fkDesc= fkd;
        }
    }
}
