/******************************************************************
*
*$RCSfile: TableMapping.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/16 09:05:13 $
*
*$Log: TableMapping.java,v $
*Revision 1.2  2005/03/16 09:05:13  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:24  Administrator
*init
*
*Revision 1.7  2004/02/02 10:42:41  yfzhu
*<No Comment Entered>
*
*Revision 1.6  2003/05/29 19:40:00  yfzhu
*<No Comment Entered>
*
*Revision 1.5  2003/04/03 09:28:15  yfzhu
*<No Comment Entered>
*
*Revision 1.4  2003/03/30 08:11:37  yfzhu
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
*Revision 1.3  2001/11/29 00:49:40  yfzhu
*no message
*
*Revision 1.2  2001/11/07 20:58:47  yfzhu
*no message
*
*
********************************************************************/
package nds.schema;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.StringBufferWriter;
import nds.util.*;
/**
* This class is for generating TableMapping from a xml file
*
*/
public class TableMapping {
    private static Logger logger=LoggerManager.getInstance().getLogger(TableMapping.class.getName());

    String name;
    String desc,desc2;
    String url;
    String classname;
    int order=99; // the default order which means error
    boolean[] mask;// QADMSP
    private String maskString;

    String category="error, no category";
    String itemTable = null;//by Hawke
    String prefetchColumn = null;//by Hawke
    String prefetchSql = null;//by Hawke
    Vector primaryKeys;// element: String
    Vector alternateKeysList;// element: Vector(element: String),
    Vector alternateKey2sList;//by Hawkins
    // every element is an AK(has at least one column name)
    Vector columns;//element: ColumnMapping
    Vector sumFields;//element:ColumnMapping
    Vector fLinks;//element:ColumnMapping
    Vector aliasTables; // element: AliasTable
    TriggerHolder triggers; // key: trigger type ( such as before-modify), value: trigger name ( if "", then use default name)
    /* --- following is added for dispatching --*/
    boolean shouldDispatch = false; // whether dispatch this table's records to shop or not
    String dispatchColumnName =null; //dipatch column, is valid when shouldDispatch= true
    String comment = null; // added by yfzhu at 2003-05-23 for column special meaning denotation
    String realTableName=null;
    String filter=null;
    ArrayList refByTables;
    String securityDirectory;
    
    public TableMapping() {
        primaryKeys=new Vector();
        alternateKeysList=new Vector();
        alternateKey2sList=new Vector();
        sumFields=new Vector();
        columns=new Vector();
        fLinks = new Vector();
        aliasTables=new Vector();
        refByTables=new ArrayList();
        mask= new boolean[7];
        for( int i=0;i< 7;i++) mask[i]= false;
    }

    public void setComment(String c){ comment=c;}

    public void setName(String name) {
        this.name=name;//.toUpperCase();
    }
    public void setDescription(String desc) {
        this.desc=desc;
    }
    public void setDescription2(String desc2) {
        this.desc2=desc2;
    }
    /**
     * @param mask can be combination of following chars
     *   "A" - Add
     *   "D" - Delete
     *   "M" - Modify
     *   "S" - Submit
     *   "Q" - Query
     *   "P" - Permit
     *   "G" - Group Submit( all selected id whill be concated by comma, and send to ObjectSubmit('id1,id2')
     *   if action mask not found, the permission on the table will be denied
     */
    public void setMask(String m) {
        if(m ==null || "".equals(m.trim()))return;
        maskString=m;

        char[] c= m.toUpperCase().toCharArray();
        for( int i=0;i<c.length;i++){
            switch(c[i]){//QADMSP
                case 'Q': mask[0]=true;break;
                case 'A': mask[1]=true;break;
                case 'D': mask[2]=true;break;
                case 'M': mask[3]=true;break;
                case 'S': mask[4]=true;break;
                case 'P': mask[5]=true;break;
                case 'G': mask[6]=true;break;
                default: throw new Error("Unsupported action mask:"+ m+"(table="+ name+")");
            }
        }
    }
    public void setURL(String url) {
        this.url=url;
    }
    public void setClassName(String cls) {
        this.classname=cls;
    }
    public void setOrder(String o){
        try{
            this.order= (new Integer(o)).intValue() ;
        }catch(Exception e){
            logger.error( name + ".table's order is error:" + o);
        }
    }
    public void setCategory(String category) {
        this.category=category;
    }
    /**
     * @param key format is "PK1[,PK2]"
     */
    public void setPrimaryKey(String keys) {
        StringTokenizer tokens=new StringTokenizer(keys, ",");
        while( tokens.hasMoreElements()) {
            String key=(String) tokens.nextToken().toUpperCase();
            primaryKeys.addElement(key);
        }
        if(primaryKeys.size()> 1)
            logger.error("Found multiple columns as PK: "+ keys+"(table="+ name+"), current system does not support this");
    }
    public void setItemTable(String name){
        this.itemTable = name;
    }
    public void setPrefetchColumn(String columnName){
        this.prefetchColumn = columnName;
    }
    public void setPrefetchSql(String sql){
        this.prefetchSql = sql;
    }
    //------------ yfzhu added at 2002-12-15 for dispatch-column
    public void setDispatchColumn(String colName){
        this.shouldDispatch=true;
        this.dispatchColumnName= colName;
    }
    public void setRealTable(String tn){
    	this.realTableName= tn;
    }
    public void setFilter(String f){
    	this.filter=f;
    }
    
    /**
     * @param key format is "AK1[,AK2]"
     */
    public void addAlternateKey(String keys) {
        Vector v=new Vector();
        StringTokenizer tokens=new StringTokenizer(keys, ",");
        while( tokens.hasMoreElements()) {
            String key=(String) tokens.nextToken().toUpperCase();
            v.addElement(key);
        }
        if(v.size()> 1)
            logger.error("Found multiple columns as AK: "+ keys+"(table="+ name+"), current system does not support this");
        alternateKeysList.addElement(v);
        if(alternateKeysList.size()> 1) {
            String cs="[";
            for( int i=0;i< alternateKeysList.size();i++) {
                Vector vv=(Vector)alternateKeysList.elementAt(i);
                for(int j=0;j< vv.size();j++) {
                    cs += vv.elementAt(j)+",";
                }
                cs += ";";
            }
            cs +="]";
            logger.error("Found multiple AKs"+ "(table="+ name+", AKs="+cs+"), current system does not support this");
        }

    }
    public void setTriggers(TriggerHolder th){
        triggers= th;
    }
    public void addAlternateKey2(String keys) {//by Hawkins
        Vector v=new Vector();
        StringTokenizer tokens=new StringTokenizer(keys, ",");
        while( tokens.hasMoreElements()) {
            String key=(String) tokens.nextToken().toUpperCase();
            v.addElement(key);
        }
        if(v.size()> 1)
            logger.error("Found multiple columns as AK: "+ keys+"(table="+ name+"), current system does not support this");
        alternateKey2sList.addElement(v);
        if(alternateKey2sList.size()> 1) {
            String cs="[";
            for( int i=0;i< alternateKey2sList.size();i++) {
                Vector vv=(Vector)alternateKey2sList.elementAt(i);
                for(int j=0;j< vv.size();j++) {
                    cs += vv.elementAt(j)+",";
                }
                cs += ";";
            }
            cs +="]";
            logger.error("Found multiple AKs"+ "(table="+ name+", AKs="+cs+"), current system does not support this");
        }

    }

    public void addColumnMapping(ColumnMapping col) {
        columns.addElement(col);
    }
    public void addSumField(ColumnMapping sf){
        sf.name= "sf"+sumFields.size();
        sumFields.addElement(sf);

}

    public void addFlink(ColumnMapping flink){
        //logger.debug(flink.name);
        fLinks.addElement(flink);
    }
    public void addAliasTable(AliasTable at){
    	aliasTables.addElement(at);
    }
    public void addRefByTable(RefByTableHolder tb){
    	refByTables.add(tb);
    }
    public void setSecurityDirectory(String sd){
    	this.securityDirectory=sd;
    }
    public String toString() {
        return name;
    }
    /**
     * Oracle ddl
     * @return
     */
    public String toDDL(){
    	TableManager manager=TableManager.getInstance();
    	StringBuffer sb=new StringBuffer();
    	StringBufferWriter b= new StringBufferWriter(sb);
    	Table tb= manager.getTable(name);
    	b.print("insert into AD_TABLE(ID,AD_CLIENT_ID,ISACTIVE,NAME,REALTABLE_ID,FILTER,"+
    			"DESCRIPTION,MASK,AD_TABLECATEGORY_ID,ORDERNO,URL,PK_COLUMN_ID,AK_COLUMN_ID,"+
				"ISDISPATCHABLE,DISP_COLUMN_ID,U_CLOB_ID,DIRECTORY_ID,ISSYSTEM,"+
				"HAS_TRIG_AM,TRIG_AM,HAS_TRIG_BM,TRIG_BM,HAS_TRIG_BD,TRIG_BD,"+
				"CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID,COMMENTS,AD_ORG_ID) values("+
    			 tb.getId()+ ", :AD_CLIENT_ID,'Y','"+name.toUpperCase()+"',"+ (manager.getTable(realTableName)==null?"null": ""+manager.getTable(realTableName).getId())+",'"+StringUtils.escapeForSQL(filter)+"',"+
				 "'"+ StringUtils.escapeForSQL(desc)+"','"+ maskString+"',get_id_by_name(:AD_CLIENT_ID,'AD_TABLECATEGORY','"+ StringUtils.escapeForSQL(this.category)+"'), "+ order+",'"+StringUtils.escapeForSQL(url)+"',"+ tb.getPrimaryKey().getId()+","+ tb.getAlternateKey().getId()+
				 ",'" + (this.dispatchColumnName==null?"N":"Y")+"'," +(tb.getDispatchColumn()==null?"null": ""+ tb.getDispatchColumn().getId() )+",null,get_id_by_name(:AD_CLIENT_ID,'DIRECTORY	','"+ tb.getSecurityDirectory()+"'),'N',"  );
    	if(triggers==null) triggers=new TriggerHolder();
    	b.print(( triggers.getTrigger("AC")!=null?"'Y'":"'N'")+","+(triggers.getTrigger("AC")!=null?"'"+triggers.getTrigger("AC")+"'":"null")+",");
    	b.print(( triggers.getTrigger("AM")!=null?"'Y'":"'N'")+","+(triggers.getTrigger("AM")!=null?"'"+triggers.getTrigger("AM")+"'":"null")+","); 
    	b.print(( triggers.getTrigger("BM")!=null?"'Y'":"'N'")+","+(triggers.getTrigger("BM")!=null?"'"+triggers.getTrigger("BM")+"'":"null")+",");
    	b.print(( triggers.getTrigger("BD")!=null?"'Y'":"'N'")+","+(triggers.getTrigger("BD")!=null?"'"+triggers.getTrigger("BD")+"'":"null")+",");
    	b.print("sysdate, sysdate, 0,0,null,null);");
    	b.println();
    	// alias tables
    	for(int i=0;i< this.aliasTables.size();i++){
    		AliasTable at= (AliasTable)aliasTables.elementAt(i);
    		b.print("insert into AD_ALIASTABLE(ID,AD_CLIENT_ID,AD_ORG_ID,ISACTIVE,NAME,AD_TABLE_ID,REALTABLE_ID,CONDITION,ORDERNO,CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID,COMMENTS) VALUES(");
    		b.print("get_sequences('ad_aliastable'), :AD_CLIENT_ID,null,'Y'," + tb.getId()+ ","+ manager.getTable(at.getRealTableName()).getId()+",'"+ StringUtils.escapeForSQL(at.getCondition())+","+ (i*10)+
    				",sysdate, sysdate, 0,0,null);");
    		b.println();
    	}
    	// ref-by tables
    	for(int i=0;i< tb.getRefByTables().size();i++){
    		RefByTable rt= (RefByTable)tb.getRefByTables().get(i);
    		b.print("insert into AD_REFBYTABLE(ID,AD_CLIENT_ID,AD_ORG_ID,ISACTIVE,AD_TABLE_ID,AD_REFBY_TABLE_ID,AD_REFBY_COLUMN_ID,FILTER ,ASSOCTYPE,ORDERNO,CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID) VALUES(");
    		b.print("get_sequences('ad_aliastable'), :AD_CLIENT_ID,null,'Y'," + tb.getId()+ ","+ rt.getTableId()+","+ rt.getRefByColumnId()+",'"+ StringUtils.escapeForSQL(rt.getFilter())+"','"+ (rt.getAssociationType()== RefByTable.ONE_TO_MANY?"n":"1")+"'," + (i*10)+
			",sysdate, sysdate, 0,0);");
    		b.println();
    	}
    	 for (int i=0;i< columns.size();i++){
            ColumnMapping map= (ColumnMapping)columns.elementAt(i);
            Column col= tb.getColumn(map.name);
            b.print("insert into AD_COLUMN(ID,AD_CLIENT_ID,ISACTIVE,DBNAME,NAME,DESCRIPTION,COMMENTS,ORDERNO,SUMMETHOD,"+
            		"COLTYPE,COLLENGTH,COLPRECISION,NULLABLE,MASK,REF_TABLE_ID,REF_COLUMN_ID,"+
					"OBTAINMANNER,AD_LIMITVALUE_GROUP_ID,DEFAULTVALUE,MODIFIABLE,"+
					"REGEXPRESSION,ERRMSG,INTERPRETER,FILTER,"+
					"DISPLAYTYPE,DISPLAYROWS,DISPLAYCOLS,DISPLAYWIDTH,CREATIONDATE,MODIFIEDDATE,OWNERID,MODIFIERID,ISSYSTEM,AD_TABLE_ID,AD_ORG_ID,U_CLOB_ID,ISAK) values(");
            b.print(col.getId()+",:AD_CLIENT_ID,'Y','"+ map.name.toUpperCase()+"','"+ (tb.getName()+"."+map.name).toUpperCase()+"','"+ StringUtils.escapeForSQL(map.desc)+"',null,"+ (i*10)+"," + (col.getSubTotalMethod()==null?"null":"'"+col.getSubTotalMethod()+"'")+"," +
            		"'"+map.dbType+"', null, null,'" + (col.isNullable()?"Y":"N")+"','"+ map.mask+"',"+ (col.getReferenceTable()==null?"null": col.getReferenceTable().getId()+"")+","+  (col.getReferenceColumn()==null?"null":""+col.getReferenceColumn().getId())+
					",'"+ (Validator.isNull(map.obtainManner)?"byPage":map.obtainManner)+"',"+ (col.isValueLimited()?"get_id_by_name(:AD_CLIENT_ID,'AD_LIMITVALUE_GROUP','"+map.name+"')":"null")+",'"+ StringUtils.escapeForSQL(map.defaultValue)+"',"+
					(col.getModifiable()?"'Y'":"'N'")+",'"+ StringUtils.escapeForSQL(map.regExpression)+"','"+StringUtils.escapeForSQL(map.errorMessage)+"','"+ StringUtils.escapeForSQL(map.interpreter)+  "','"+StringUtils.escapeForSQL( map.filter)+"'");
            DisplaySetting ds= col.getDisplaySetting();
            b.print(",'"+ds.getObjectTypeString()+"',"+ ds.getRows()+","+ ds.getColumns()+","+ ds.getCharsPerRow()+",sysdate, sysdate, 0,0,'N',"+tb.getId()+",null, null,'"+ (col.isAlternateKey()?"Y":"N") +"');");
            b.println();
    	 }    	
    	return sb.toString();
    }
    /**
      Convert this class to xml
      @param lowerCase if true, all columns and the table name will be lower case
    */
    public String toXML(){
        StringBuffer buf=new StringBuffer();
        StringBufferWriter b= new StringBufferWriter(buf);
        b.print("<?xml version=\"1.0\" encoding=\"GB2312\"?>");
        b.println("<table xmlns=\"x-schema:schema.xml\">");
        b.pushIndent();
        printNode(b,"name",(name.toLowerCase()));
        printNode(b,"description", desc);
        printNode(b,"mask", "Q");//maskString);
        printNode(b,"category", category);
        printNode(b,"url", url);

        printNode(b,"primary-key",(""+primaryKeys.elementAt(0)).toLowerCase());
        if (alternateKeysList.size()>0)
            printNode(b,"alternate-key", (""+((Vector)alternateKeysList.elementAt(0)).elementAt(0)).toLowerCase());
/*        for (int i=0;i< aliasTables.size() ;i++)
            ((AliasTable)aliasTables.elementAt(i)).printXML(b) ;
*/        for (int i=0;i< columns.size();i++){
            ColumnMapping map= (ColumnMapping)columns.elementAt(i);
            if( map.name.indexOf(".")>0 || map.name.indexOf(")")>0){
                //pass
            }else
             map.printColumnXML(b);
        }
/*        for(int i=0;i< sumFields.size();i++){
            ColumnMapping map= (ColumnMapping)sumFields.elementAt(i);
            map.printSumfieldXML(b );
        }
        for(int i=0;i< fLinks.size();i++){
            ColumnMapping map= (ColumnMapping)fLinks.elementAt(i);
            map.printFlinkXML(b);
        }
*/        b.popIndent();
        b.println("</table>");
        return buf.toString();
    }
    private void printNode(StringBufferWriter b, String name, Object value){
        b.println("<"+name+">"+ value+"</"+name+">");
    }
}
