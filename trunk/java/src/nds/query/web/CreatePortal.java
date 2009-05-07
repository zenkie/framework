/******************************************************************
*
*$RCSfile: CreatePortal.java,v $ $Revision: 1.9 $ $Author: Administrator $ $Date: 2006/03/28 02:25:56 $
*
*$Log: CreatePortal.java,v $
*Revision 1.9  2006/03/28 02:25:56  Administrator
*no message
*
*Revision 1.8  2005/12/18 14:06:16  Administrator
*no message
*
*Revision 1.7  2005/10/25 08:12:53  Administrator
*no message
*
*Revision 1.6  2005/08/28 00:27:04  Administrator
*no message
*
*Revision 1.5  2005/06/16 10:19:20  Administrator
*no message
*
*Revision 1.4  2005/05/16 07:34:18  Administrator
*no message
*
*Revision 1.3  2005/04/18 03:28:19  Administrator
*no message
*
*Revision 1.2  2005/03/16 09:05:12  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.4  2004/02/02 10:42:49  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/05/29 19:40:03  yfzhu
*<No Comment Entered>
*
*Revision 1.3  2003/04/03 09:28:20  yfzhu
*<No Comment Entered>
*
*Revision 1.2  2003/03/30 08:11:55  yfzhu
*Updated before subtotal added
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.5  2001/11/29 00:49:32  yfzhu
*no message
*
*Revision 1.4  2001/11/13 22:37:14  yfzhu
*no message
*
*Revision 1.3  2001/11/13 07:19:00  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.query.web;

import java.util.*;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.schema.*;
import nds.schema.TableManager;
import nds.security.NDSSecurityException;
import nds.util.*;

/**
 * Yfzhu modified at 2003-03-07 for Linyan's Request
 */

public class CreatePortal {
    /*private final String[] categories={
    		"订单管理", "伙伴关系管理", "物料管理","代理商界面" ,"基本信息", "系统管理", "常用工具","财务管理","仓库管理","系统安全",    		
    	"订货","退货","配货","收货","仓库","盘点调整",
        "成本单价","财务到款","财务结算","月收发存","门店(有POS)","门店(无POS)","门店(综合)"};
        */
    TableManager manager = TableManager.getInstance();
    
    private String category=null;
    private String preTableDescText="";
    private boolean categoryTableIndent=true;
    private String template=TABLE_TEMPLATE;
    private String hrefAttributesText="";
    private String categoryAttributesText="";
    
    private final static String TAG_TITLE="$TITLE$";
    private final static String TAG_CONTENT="$CONTENT$";
 
    public final static String TABLE_TEMPLATE="<table width=80% align=center border=0 cellpadding=0 cellspacing=0><tr><td>"+
            "<table width=100% border=0 cellpadding=0 cellspacing=0>\n"+
            "<tr valign=top><td width=38 rowspan=2><img src=../images/62542.gif width=38 height=28></td>\n"+
            "<td width=100% height=3 colspan=3></td></tr>\n"+
            "<tr><td height=25 width=30% class=s01 background=../images/62543.gif><b>$TITLE$</b></td>\n"+
            "<td width=14><img src=../images/62544.gif width=14 height=25></td>\n"+
            "<td width=100% background=../images/62545.gif valign=bottom align=right><img src=../images/62546.gif width=5 height=9></td></tr>\n"+
            "<tr><td height=3></td></tr>\n"+
            "</table>\n"+
            "<table width=100% border=0 cellpadding=0 cellspacing=0 bgcolor=#EBF5FD>\n"+
            "<tr valign=top><td width=97%><img src=../images/62547.gif width=5 height=5></td><td width=5><img src=../images/62548.gif width=5 height=5></td></tr>\n"+
            "</table>\n"+
            "<table width=100% border=0 cellpadding=0 cellspacing=0 bgcolor=#EBF5FD>\n"+
            "<tr> \n"+
            "<td height=10 colspan=2><img src=../images/c.gif width=1 height=1></td>\n"+
            "</tr>\n"+
            "<tr valign=top> \n"+
            "<td>$CONTENT$</td>\n"+
            "<td width=5><br>\n"+
            "</td>\n"+
            "</tr>\n"+
            "</table>\n"+
            "<table width=100% border=0 cellpadding=0 cellspacing=0 bgcolor=#EBF5FD>\n"+
            "<tr valign=top><td width=98%><img src=../images/62549.gif width=5 height=5></td><td width=5><img src=../images/62550.gif width=5 height=5></td></tr>\n"+
            "</table></td></tr></table>";    
    public CreatePortal() {}
    private boolean isTailedWith(String str, String tail) {
        boolean b;
        if (str.lastIndexOf(tail) ==( str.length()- tail.length()))
            b= true ;
        else
            b=false;
        return b;
    }
    /**
     * element are Integer of category id
     * @param request
     * @return
     */
    public List getTableCategoryIds(HttpServletRequest request) {
        Iterator tables = manager.getTables(Table.QUERY).iterator();
        PairTable categories = new PairTable();
        while(tables.hasNext()) {
            Table table = (Table)tables.next();
            Integer tableCategoryName =new Integer( table.getCategory().getId());
            if(!categories.containsKey(tableCategoryName)) { //has thisCategory
	            // check for current user permission
	            try{
	                WebUtils.checkTableQueryPermission(table.getName(),request );
		            categories.put(tableCategoryName,tableCategoryName);
	            }catch(Throwable e){
	                continue;
	            }
            }
        }   
        return categories.keyList();
    }
    /**
     * No user permission check
     * @param tableId
     * @return
     */
    public ArrayList getRelateTables(int tableId) {
    	return getRelateTables(null,tableId);
    }
    /**
     * Get other tables of the same category
     * @param request
     * @param tableId
     */
    public ArrayList getRelateTables(HttpServletRequest request, int tableId) {
    	Iterator tables = manager.getTables(Table.QUERY).iterator();
    	ArrayList al=new ArrayList();
    	int categoryId= manager.getTable(tableId).getCategory().getId();
    	while(tables.hasNext()) {
            Table table = (Table)tables.next();
            int tableCategoryId = table.getCategory().getId();
            if(categoryId!=tableCategoryId ) continue;
            //remove item check
            if(!table.isMenuObject() /* || table.getName().toUpperCase().endsWith("ITEM")*/) continue;
            // check for current user permission
            try{
                if(request!=null)WebUtils.checkTableQueryPermission(table.getName(),request );
            }catch(NDSSecurityException e){
                continue;
            }
            al.add(table); 
        }   
    	ListSort.sort(al,"Order", true);
    	return al;
    }
    public ArrayList getReferenceTables( int tableId){
    	return getReferenceTables(null, tableId);
    }
    public ArrayList getReferenceTables(HttpServletRequest request, int tableId) {
    	ArrayList al=new ArrayList();
    	Hashtable ht=new Hashtable();
    	Table tb= manager.getTable(tableId);
    	ArrayList  cols=tb.getAllColumns();
    	for (int i=0;i<cols.size();i++){
    		Table rt= ((Column)cols.get(i)).getReferenceTable();
    		if(rt!=null){
    			if(!((Column)cols.get(i)).isShowable(Column.QUERY_LIST)) continue;
                //skip item table, yfzhu 2005-04-01
                if(!rt.isMenuObject() /* || rt.getName().toUpperCase().endsWith("ITEM")*/) continue;
        		try{
                    if(request!=null)WebUtils.checkTableQueryPermission(rt.getName(),request );
                }catch(NDSSecurityException e){
                    continue;
                }    		
    			ht.put( new Integer(rt.getId()), rt);
    		}
    	}
    	for(Iterator it=ht.values().iterator();it.hasNext();)
    		al.add(it.next());
    	return al;
    	
    }

    /**
     * 
     * @param table
     * @param category
     * @param wild if true, will check table name/description, is contains category
     * , will take it as in that category
     * @return
     */
    private boolean tableInCategory(Table table, String category, boolean wild, Locale locale){
    	if( Validator.isNull( category ) || table ==null) return false;
    	String desc = table.getDescription(locale);
    	String ca= table.getCategory().getDescription(locale);
    	String name= table.getName().toUpperCase();
    	String c= category.toUpperCase();
    	if( name.indexOf(c)!=-1 ||desc.indexOf(c)!=-1 || ca.indexOf(c)!=-1) return true;
    	return false;
    }
    /**
     * return key: table.category.id, value: Vector( table of that category)
     */
    public Hashtable getTableCategories(HttpServletRequest request) {
		Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
		if(locale==null)locale= TableManager.getInstance().getDefaultLocale();    	
        Iterator tables = manager.getTables(Table.QUERY).iterator();
        Hashtable categories = new Hashtable(50,20);
        while(tables.hasNext()) {
            Table table = (Table)tables.next();
            int tableCategoryId = table.getCategory().getId();
            if(Validator.isNotNull( category )){
            	if (!tableInCategory(table, category, true, locale)){
                		continue;
                }
            }
            //skip item table, yfzhu 2005-04-01
            if(/*table.getName().toUpperCase().endsWith("ITEM") ||*/!table.isMenuObject()){
            	continue;
            }

            // check for current user permission
            try{
                WebUtils.checkTableQueryPermission(table.getName(),request );
            }catch(NDSSecurityException e){
                continue;
            }

            //nds.util.Tools.log("The Table's "+table.getName()+" category is "+ tableCategoryName+ "   URL is "+ table.getRowURL());
            if(categories.containsKey(new Integer(tableCategoryId))) { //has thisCategory
                Vector oldCatedGroup = (Vector)categories.get(new Integer(tableCategoryId));
                oldCatedGroup.addElement(table);
            } else { //First Init this Category
                Vector catedGroup = new Vector();
                catedGroup.addElement(table);
                categories.put(new Integer(tableCategoryId),catedGroup);
            }
        }
        Vector v;
        for ( Iterator it= categories.keySet().iterator();it.hasNext();){
            v= (Vector) categories.get( it.next() );
            nds.util.ListSort.sort(v,"Order");
        }
        return categories;
    }
    
    /**
     * 
     * @param request
     * @param targetPage
     * @param columns how many columns per line
     * @return
     * @throws java.io.IOException
     */
    public void preparePortalHTML(HttpServletRequest request,String targetPage, int columns)throws java.io.IOException {
		Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
        if(locale ==null ) locale= TableManager.getInstance().getDefaultLocale();
    	StringBuffer html = new StringBuffer();
    	// getTableCategories return key: table.category.id, value: Vector( table of that category)
        Vector sortedTables = sortTables(getTableCategories(request));
        int[] subTotal =new int[columns];
        Integer categoryId ;
        String categoryDesc;
        Vector catedTableGroup =null;
        //linkTitle=new String[sortedTables.size()];
        subHtml = new StringBuffer[columns];
        for(int i=0;i< columns ;i++ ) subHtml[i]= new StringBuffer();
        int idx;
        for (int i = 0; i < sortedTables.size();i++) {
        	categoryId =(Integer) ((Vector)sortedTables.get(i)).get(0);
        	categoryDesc= manager.getTableCategory(categoryId.intValue()).getDescription(locale);
            linkTitle.put(categoryId, categoryDesc);
            catedTableGroup = (Vector)((Vector)sortedTables.get(i)).get(1);
            //append to shorter columns
            idx=getShortestArrayIdx(subTotal);
            subHtml[idx].append(getLittleTable(locale, categoryDesc,catedTableGroup,targetPage));
            subTotal[idx]=subTotal[idx]+ catedTableGroup.size();
        } 
        /*if( Validator.isNull(category))
        	html.append("<table width='100%' align=center><tr><td width='80%' align='center'>"+linkTitle+"</td></tr></table>");
        html.append("<br>");
        html.append("\r\n<table width='100%' align=center>");
        html.append("<tr>");
        for(int i=0;i< columns;i++)
        	html.append("<td width='"+ 100/columns +"%' valign='top'>"+subHtml[i]+"</td>");
        html.append("</tr>");
        html.append("</table>");
        return html.toString();*/
    }
    // will be prepared by preparePortalHTML
    private nds.util.PairTable linkTitle=new nds.util.PairTable(); 
    // will be prepared by preparePortalHTML
    private StringBuffer[] subHtml=null;
    public nds.util.PairTable getCategoriesHTML(){
    	return linkTitle;
    }
    public StringBuffer[] getColumnsHTML(){
    	return subHtml;
    }
    /**
     * get index of the data which is smallest
     * for instance a={30,10,20}, return 1;
     * @param a
     * @return
     */
    private int getShortestArrayIdx(int[] a){
    	int value= Integer.MAX_VALUE;int idx=0;
    	for(int i=0;i< a.length;i++){
    		if(a[i]< value){
    			idx=i;
    			value=a[i];
    		}
    	}
    	return idx;
    }
    public void  setCategoryAttributeTexts(String cate){
    	this.categoryAttributesText= cate;
    }
    public String getLinkCategory(String cateName) {
        String s= "<a "+ categoryAttributesText+" href='#"+cateName+"'>"+cateName+"</a>&nbsp;|&nbsp ";
        return s;
    }
    private String getLittleTable(Locale locale, String cateName ,Vector tableGroup, String targetPage) {
        StringBuffer html = new StringBuffer(categoryTableIndent?"<ul>":"");
        String s= nds.util.StringUtils.replace(template,TAG_TITLE,"<a name='"+cateName+"'>"+cateName+"</a>");
        String tn;
        for (int j = 0; j <tableGroup.size(); j++) {
            Table temp = (Table)tableGroup.get(j);
            tn= temp.getName().toLowerCase() ;
            if ( !shouldInsertRow(tableGroup, temp) )continue;
            html.append( (categoryTableIndent?"<li>":"")+preTableDescText+getItemLinkStringOfTable(locale, temp,targetPage) + (categoryTableIndent?"</li>":"<br>") );
        }
        html.append(categoryTableIndent?"</ul>":"<br>");
        s=nds.util.StringUtils.replace(s,TAG_CONTENT,html.toString());
        return s;
    }
    
    public boolean shouldInsertRow( Vector tableGroup, Table table){
        // added at 2005-11-18 to eliminate none-menu object tables
    	if(! table.isMenuObject()) return false;
    	String tn= table.getName().toLowerCase() ;
        String sht;
        if ( tn.endsWith("item")){
            sht= tn.substring(0, tn.length() -4);
            if ( manager.getTable(sht) !=null ) return false;
        }else if ( tn.endsWith("ftp")){
            sht= tn.substring(0, tn.length() -3) + "Sht";
            if ( manager.getTable(sht) !=null) return false;
//            else System.out.println( sht + " is null, should insert .............");
        }
        return true;
    }
    /**
     * May including both item or ftp
     */
    public String getItemLinkStringOfTable(Locale locale, Table table, String targetPage){
        String tn= table.getName();
        String tag="";
        //String t= "<a "+ hrefAttributesText+" href=\"javascript:"+targetPage+"("+table.getId()+")\">"+table.getDescription(locale)+" </a>";
        String t= "<a "+ hrefAttributesText+" href=\"javascript:"+targetPage+"('"+table.getName()+"')\">"+table.getDescription(locale)+" </a>";
        /*Table tp;
        tp=manager.getTable(tn + "Item");
        if( tp!=null ){
            t += "<a "+ hrefAttributesText+"  href=\"javascript:open_nds_window('"+targetPage+"?table="+tp.getName()+"')\">【明细】</a>";
        }
        if ( tn.toLowerCase().endsWith("sht")){
            tp=manager.getTable(tn.substring(0,tn.length()-3) + "Ftp");
            if ( tp !=null){
                t += "<a "+ hrefAttributesText+" href=\"javascript:open_nds_window('"+targetPage+"?table="+tp.getName()+"')\">【记录】</a>";
            }
        }*/
        return t;
    }
    
    // elements: Vector ( 3 elements: categoryId(Integer),  table vector ( of that category),
    // and vector size ( of that category to be row count in category table, may eliminate some tables
    //        such as item or ftp tables)
    /**
     * @param tables: key: table.category.id, value: Vector( table of that category)
     */
    public Vector sortTables(Hashtable tables) {
        Vector rtn = new Vector();
        Vector row;Vector catedTableGroup;
        Integer categoryId;
        ArrayList categories= TableManager.getInstance().getTableCategories(); // elements are TableCategory
        for ( int i=0;i< categories.size();i++){
        	categoryId=new Integer(( (TableCategory)categories.get(i)).getId());
            catedTableGroup = (Vector)tables.get(categoryId);
            if( catedTableGroup !=null){
                row= new Vector();
                row.addElement(categoryId);
                row.addElement(catedTableGroup);
                row.addElement(new Integer(countSize(catedTableGroup)));
                rtn.addElement(row);
                tables.remove(categoryId);
            }
        }
        // yfzhu 2003-11-10 do not add category that not specified by this.categories
        for (Enumeration enu =tables.keys(); enu.hasMoreElements();) {
        	categoryId = (Integer)enu.nextElement();
            catedTableGroup = (Vector)tables.get(categoryId); // vector's element is Table
            Integer groupsize  = new Integer(countSize(catedTableGroup));//new Integer(catedTableGroup.size());

            row=new Vector();
            row.addElement(categoryId);
            row.addElement(catedTableGroup);
            row.addElement(groupsize);
            rtn.addElement(row);
        }
        return rtn;
    }
    /**
     * Count table size , Item,FTP will be taken as 0
     */
    private int countSize(Vector tables){
        int c=0;String tn;
        for( int i=0;i< tables.size();i++){
            if ( shouldInsertRow(tables, (Table) tables.elementAt(i))) c++;
        }
        return c;
    }
    public void setTemplate(String temp){
    	template=temp;
    }
    public void setHrefAttributesText(String text){
    	hrefAttributesText=text;
    }
    public void setCategoryTableIndent(boolean b){
    	categoryTableIndent=b;
    }
    public void setPreTableDescText(String t){
    	preTableDescText=t;
    }
    /**
     * Will allow tables of the specified category be displayed, if ca is Integer, that means
     * ad_tablecategory.id is input, so will retrieve category name from that table
     * @param ca can be string or integer(id)
     */
    public void setCategory(String ca) throws QueryException{
    	if( Tools.getInt(ca, -1)==-1){
    		category= ca;
    		return;
    	}
    	
    	category= (String)QueryEngine.getInstance().doQueryOne("select name from ad_tablecategory where id="+ca);
    }
    public String getCategory(){
    	return category;
    }
    
}
