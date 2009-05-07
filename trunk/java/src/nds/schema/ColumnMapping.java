/******************************************************************
*
*$RCSfile: ColumnMapping.java,v $ $Revision: 1.2 $ $Author: Administrator $ $Date: 2005/03/16 09:05:12 $
*
*$Log: ColumnMapping.java,v $
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
*Revision 1.4  2001/12/28 14:20:02  yfzhu
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
package nds.schema;

import java.util.Iterator;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.*;
import nds.util.StringBufferWriter;

public class ColumnMapping {
    private static Logger logger=LoggerManager.getInstance().getLogger(ColumnMapping.class.getName());

    PairTable limitValues=null;
    String name,desc,desc2, refTable, refColumn,interpreter,obtainManner,defaultValue;
    String  tableColumnName,objectTable;
    String regExpression;
    String errorMessage;
    int type,length=-1, scale=0;
    boolean  nullable=true;
    boolean modifiable = true;
    String mask="0000000";
    String limit = null;//By Hawke
    String money = null;//By hawke
    String dbType;// from setType()'s parameter
    String sumMethod=null;

    String comment = null; // added by yfzhu at 2003-05-23 for column special meaning denotation
    
    String filter=null;
    String displaySetting=null;
    String sequenceHead=null;
    public ColumnMapping() {}

    public void setComment(String c){ comment=c;}

    public void setName(String name) {
        this.name=name.toUpperCase();
    }
    public void setDescription(String desc) {
        this.desc=desc;
    }
    public void setDescription2(String desc2) {
        this.desc2=desc2;
    }
    /**
     * @param ctype must be identical to which shown in ERWIN
     */
    public void setType(String ctype) {
        dbType=ctype;
        try {
            SQLType t= TypeConverterFactory.getInstance().getConverter().convert(ctype.toLowerCase());
            this.type=t.getType();
            this.length=t.getLength();
            this.scale=t.getScale()  ;
        } catch(Exception e) {
            throw new RuntimeException("Found error when converting type string:"+ctype+": "+e);
        }
    }
    /**
     * @param "true" or "false"
     */
    public void setNullable(String b) {
        nullable= (new Boolean(b)).booleanValue();
    }
    /**
     * @param s 6 chars, each as meaning as( '0' as false, '1' as true):
     *      position from left to right:
     *      1   showable( action="ADD")
     *      2   modifiable( action="ADD")
     *      3   showable(action="MODIFY")
     *      4   modifiable(action="MODIFY")
     *      5   showable(action="QUERY")
     *      6   showable(action="PRINT")
     *		7   showable(action="OBJECTVIEW") ' default equal to bit 5
     *      so, "111000" means the column should show when add, and allow input,
     *          while during modification, it only allows for viewing, this column
     *          does not allowing showing when query.
     * 
     * From 2.0, add the 7th bit for object view page, if set, will display
     * in object view page, if only 6 chars given, will set as bit 5 ( Query)
     */
    public void setMask(String s) {
        if( s==null || (s.length() !=6 &&  s.length() !=7))
            throw new RuntimeException("Column "+ name+ " has a invalid mask:"+s);
        char[] cs= s.toCharArray();
        for( int i=0;i< cs.length;i++) {
            if( cs[i] !='0' && cs[i] !='1' )
                throw new RuntimeException("Column "+ name+ " has a invalid mask:"+s);
        }
        // if is modifiable, it must be showable
        if(cs[1]>cs[0]  || cs[3]>cs[2])
        	throw new RuntimeException("Found Column "+ name+ " has a invalid mask(some action allows for modify while not showable):"+s);
        
        if(s.length() ==6){
        	mask=s+cs[4];
        }else
        	mask=s;
        //System.out.println(" Column :"+ name+" 's mask:"+s);
    }
    public void setRefTable(String tableName) {
        refTable=tableName.toUpperCase();
    }
    public void setRefColumn(String columnName) {
        this.refColumn=columnName.toUpperCase();
    }
    public void setInterpreter(String interpreter) {
        this.interpreter=interpreter;
    }
    public boolean isValueLimited() {
        return (limitValues!=null && limitValues.size()>0);
    }
    /**
     * 
     * @param filter
     * @since 2.0
     */
    public void setFilter(String filter){
    	this.filter=filter;
    }
    /**
     * @since 2.0
     */
    public void setDisplaySetting(String setting){
    	this.displaySetting=setting; 
    }    
    
    
    /**
     * @param value must be int 
     */
    public void addLimitValue(String name, String value) {
        if(limitValues==null)
            limitValues=new PairTable();
        // value as the key
        limitValues.put(value,name);
    }
    public void setLimit(String limit){//By Hawke
        this.limit = limit;
    }
    public void setMoney(String money){//By Hawke
        this.money = money;
    }

    public void setModifiable(String b) {
        modifiable =( new Boolean(b)).booleanValue() ;
    }
    public void setObtainManner(String obtainManner) {
        this.obtainManner = obtainManner;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setTableColumnName(String tableColumnName) {
        this.tableColumnName = tableColumnName;
    }

    public void setObjectTable(String objectTable) {
        this.objectTable = objectTable.toUpperCase() ;
    }

    public void setRegExpression(String regExpression) {
        this.regExpression = regExpression;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public void setSumMethod(String method){
        this.sumMethod= method;
    }
    public void setSequenceHead(String s){
    	this.sequenceHead=s;
    }
    public void printColumnXML(StringBufferWriter b){
        b.println("<column>");
        b.pushIndent();
        printNode(b,"name", name.toLowerCase() );
        printNode(b,"description", desc);
        printNode(b,"dbtype", dbType);
        printNode(b,"nullable", ""+nullable);
//        System.out.println("column="+ name+", this.type=" + this.type+ ", gross Type="+ ColumnImpl.toGrossType(this.type));
        if(ColumnImpl.toGrossType(this.type)==Column.NUMBER  ){
            if(name.toLowerCase().indexOf("id")< 0 && this.limitValues ==null) printNode(b,"sum-method", "sum");
            //else )printNode(b,"sum-method", "sum");
        }
        printNode(b,"mask", mask);
        if(refTable !=null){
            printNode(b,"ref-table", refTable.toLowerCase() );
            printNode(b,"ref-column",refColumn.toLowerCase() );
        }
        if( interpreter !=null) printNode(b,"interpreter",interpreter);
        if(limitValues!=null){
            for( Iterator it=limitValues.keys();it.hasNext();){
                b.println("<limitValue>");
                b.pushIndent();
                Object k=  it.next();
                b.println("<desc>"+ limitValues.get(k )+"</desc><value>"+ k+"</value>");
                b.popIndent();
                b.println("</limitValue>");
            }

        }
        if(limit !=null)printNode(b,"limit", limit);
        if(money!=null) printNode(b,"money", money);
        b.popIndent();
        b.println("</column>");
    }
    public void printFlinkXML(StringBufferWriter b){
        b.println("<flink>");
        b.pushIndent();
        printNode(b,"name", name);
        printNode(b,"description", desc);
        printNode(b,"dbtype", dbType);
        printNode(b,"nullable", ""+nullable);
        printNode(b,"mask", mask);
        if(refTable !=null){
            printNode(b,"ref-table", refTable);
            printNode(b,"ref-column",refColumn);
        }
        if( interpreter !=null) printNode(b,"interpreter",interpreter);
        if(limitValues!=null){
            for( Iterator it=limitValues.keys();it.hasNext();){
                b.println("<limitValue>");
                b.pushIndent();
                Integer k= (Integer) it.next();
                b.println("<desc>"+ limitValues.get(k)+"</desc><value>"+ k+"</value>");
                b.popIndent();
                b.println("</limitValue>");
            }

        }
        b.popIndent();
        b.println("</flink>");

    }
    public void printSumfieldXML(StringBufferWriter b){
        b.println("<sumfield>");
        b.pushIndent();
        printNode(b,"description", desc);
        printNode(b,"dbtype", dbType);
        if( interpreter !=null) printNode(b,"interpreter",interpreter);
        b.popIndent();
        b.println("</sumfield>");
    }
    private void printNode(StringBufferWriter b, String name, Object value){
        b.println("<"+name+">"+ value+"</"+name+">");
    }
}
