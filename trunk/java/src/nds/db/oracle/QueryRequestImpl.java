/******************************************************************
*
*$RCSfile: QueryRequestImpl.java,v $ $Revision: 1.10 $ $Author: Administrator $ $Date: 2006/03/14 10:51:21 $
*
*$Log: QueryRequestImpl.java,v $
*Revision 1.10  2006/03/14 10:51:21  Administrator
*no message
*
*Revision 1.9  2006/03/13 01:15:32  Administrator
*no message
*
*Revision 1.8  2005/12/18 14:06:15  Administrator
*no message
*
*Revision 1.7  2005/10/25 08:12:52  Administrator
*no message
*
*Revision 1.6  2005/08/28 00:27:03  Administrator
*no message
*
*Revision 1.5  2005/05/16 07:34:14  Administrator
*no message
*
*Revision 1.4  2005/04/27 03:25:32  Administrator
*no message
*
*Revision 1.3  2005/03/30 13:13:56  Administrator
*no message
*
*Revision 1.2  2005/03/21 00:55:42  Administrator
*no message
*
*Revision 1.1.1.1  2005/03/15 11:23:17  Administrator
*init
*
*Revision 1.6  2003/09/29 07:37:28  yfzhu
*before removing entity beans
*
*Revision 1.5  2003/08/17 14:25:14  yfzhu
*before adv security
********************************************************************/
//Source file: F:\\work2\\tmp\\nds\\query\\QueryRequestImpl.java

package nds.db.oracle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

import nds.control.web.WebUtils;
import nds.query.*;
import nds.schema.AliasTable;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.Base64;
import nds.util.MessagesHolder;
import nds.util.Tools;
import nds.util.Validator;
import nds.velocity.ArrayUtil;



/**
 * ���ݿͻ���ҳ���ϵ����빹���ѯ��������ཫʹ��TableManager���SQL���Ĵ�����
 * Ŀǰ������
 * 2.0 �Ľ��Ĺ��ܣ�
 * 1���ֶε�������������ַֺ� ����ʾ�ǹ����ֶΣ������ֶα����ڱ�Ķ����г���
 * 
 * 2) �������� AliasTable ��FK�ֶΣ����磺 ������b_room �� b_house Ϊalias table,
 * ����ͨ������ b_house ��b_buidling ��Ϊb_room��һ����ʾ�ֶΣ�Ŀǰϵͳ��֧�֡�
 * AliasTable �е�ֱ��������ʾ���ֶ��ǿ��Ա��������õġ�
 *  
 * @since 2.o 
 */
public class QueryRequestImpl extends nds.query.QueryRequestImpl {

    private transient TableManager manager;
    /**
     * ����ڲ�ѯ�дӱ�ı�����
     *
     * �ӱ�Ķ��壭�������ṩ�Ĳ�ѯ��Ϊ�˻��ĳ�����󣬸ö�������Ӧ�ı��Ϊ����
     *               ��������������������ڵı��Ϊ�ӱ�
     *
     * ������ڶ����������ݿ��Ӧ�ı���orders����Ա����Ӧ�ı���employee����������
     * �����ˣ�����ˣ���orders������applierID, auditorID��ʾ�����Ҫ����ĳ������
     * employee���Ǵӱ��ӱ�������õ������ӱ�
     *
     * key: ColumnLink
     * value: table aliase(String), the real table can be abtained using
     *              ColumnLink.getLastColumn().getReferenceTable()
     *
     * ��������SQL��䣺
     *      select order.name, a1.name, a2.name from order , employee a1, \
     *      employee a2 where order.id=1 and a1.id=order.applierID and \
     *      a2.id=order.auditorID
     *
     * ������2��ColumnLink����Ӧ2���ӱ�(��Ȼ����employee)
     */
    private Hashtable rtables;

    private ArrayList whereClause;// WHERE clause in SQL, elements:String
    private ArrayList selections;//  SELECT clause in SQL, elements:ColumnLink, or String (a none-defined column)
    private ArrayList selectionDescs;//  SELECT clause in SQL, for head name

    /////////////////////////////////////////////////////////////////////
    // following param is only addParam()'s argument, while <code>whereClause</code> is parsed one
    private ArrayList paramColumns;// Where clause in SQL, elements:int[]
    private ArrayList paramValues;// Where clause in SQL, elements: String
    //orderColumn lenth=1
    private int[] orderColumn=null;
    //support orderColumnlink 
    private int[] orderColumnlink=null;
    private boolean orderAscending=false;

    private String orderbyClause=null;
    private int startRowIndex=0, range=QueryUtils.DEFAULT_RANGE;//default values
    
    private String resultHandler=null;
    private String directSQL=null;

    private ArrayList displayColumnIndices; // elements Integer(Object)

    private boolean fullRangeSubTotalEnabled= false;

    private StringBuffer paramDesc;

    private Expression paramExpr;// if addParam(Expression) is called, use can retrieve the expression back
    private boolean isStartingExpr=true;// internal variable to sign whether add operator description( and , or) or not
    private Locale locale;
    private int securityGrade; // from QuerySession
    public QueryRequestImpl() {
    	this(DummyQuerySession.getInstance());
    }
    public QueryRequestImpl(QuerySession session) {
    	
        manager=TableManager.getInstance();
        rtables=new Hashtable();
        selections=new ArrayList();
        selectionDescs=new ArrayList();
        whereClause =new ArrayList();
        paramColumns=new ArrayList();
        paramValues=new ArrayList();
        displayColumnIndices=new ArrayList();
        paramDesc= new StringBuffer();  
        this.session= session;
        if(session!=null) {
        	locale= session.getLocale();
        	securityGrade= session.getSecurityGrade();
        }else{
        	locale= manager.getDefaultLocale();
        	securityGrade= 0;
        }
    }
    //////////io
    private void readObject(java.io.ObjectInputStream stream)throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        // initialize table/tablemanager
        manager=TableManager.getInstance();
        mainTable=manager.getTable((String)stream.readObject());
    }
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        //write table
        stream.writeObject(mainTable.getName());
    }

    /**
     * ��ѯ������������SQL �� WHERE �Ӿ䣬�����Ӧ�����ڶ������ӱ���������
     * @param mainTableColumnID the column in main table
     * @param refTableColumnID the reference table column that query parameter
     * is set on, normally will has primary key(id) equals to <code>mainTableColumnID</code>
     * @param value the query parameter, see QueryUtils.toSQLClause() for more details
     * @throws QueryException if <code>mainTableColumnID</code> is not in mainTable
     * @roseuid 3B8231AD0299
     */
    public void addParam(int mainTableColumnID, int refTableColumnID, String value)throws QueryException {
        int[] ids=new int[2];
        ids[0]=mainTableColumnID;
        ids[1]=refTableColumnID;
        addParam(ids,value);
    }
    public void addParam(int mainTableColumnID, String value, String desc) throws QueryException {
        addParam(mainTableColumnID,value,true, desc);
    }
    public void addParam(int mainTableColumnID, String value) throws QueryException {
        addParam(mainTableColumnID,value,true, null);
    }

    /**
     * ��ѯ������������SQL �� WHERE �Ӿ䣬�����Ӧ������������������
     * @param mainColumnID the column in main table
     * @param value the query parameter, see QueryUtils.toSQLClause() for more details
     * @param desc param condition description, if null, construct it
     * @throws QueryException if <code>mainTableColumnID</code> is not in mainTable
     * @roseuid 3B8301D10220
     */
    private String addParam(int mainTableColumnID, String value, boolean bAddToWhereClause, String desc) throws QueryException {
        checkMainTable(mainTableColumnID);
        Column col=manager.getColumn(mainTableColumnID);
        if(col.isUpperCase()) value=(value==null?value: value.toUpperCase());
        if(col.isColumnLink()){
        	int[] cl=new int[]{mainTableColumnID}; // col.getColumnLink().getColumnIDs();
        	return this.addParam(cl,value,bAddToWhereClause,desc);
        }
        String columnName;
        if (! col.isVirtual() )columnName=mainTable.getName()+"."+ col.getName();
        else columnName= col.getName();
       // logger.debug("columnName ->"+columnName+" value->"+value+" type->"+col.getType());
        String sub=QueryUtils.toSQLClause(columnName,value, col.getType());
        if (bAddToWhereClause) whereClause.add(sub);
        
        String subDesc=(desc==null?QueryUtils.toSQLClauseDesc(col,col.getDescription(locale),
                value, col.getType(),locale):desc);
        startNewParamDesc(SQLCombination.SQL_AND );
        paramDesc.append(subDesc);
        isStartingExpr=false;

        paramColumns.add(new int[]{mainTableColumnID});
        paramValues.add(value);
        return sub;
    }
    /**
     * Add alias table according to <code>clink</code>.
     * ColumnLink ������һ��Ԫ�أ��������ĳ�ֶο�ʼ��ColumnLink�����ֶα�Ȼ��һ��FK����Ӧ��
     * alias table��������
     * ���� orders(id, fillerID, applierID),employee(id, departmentID),department(id,name)
     * ColumnLink=[order.fillerID]����ʾemployee������alias table����ʱ����Ҫ��Where �Ӿ���ָ��
     *      a.id=order.fillerId
     *  ColumnLink=[order.fillerID,employee.deparmentID]����ʾdepartment������alias table����
     *  where �Ӿ��У���:
     *      b.id=a.id and a.id=order.fillerID
     *  ע�⣬��ʱ����Ҫ�����rtables���Ƿ����[order.fillerID],����Ѿ����ڣ���ʹ������Ӧ�ı�����
     *  ����Ӧ���Դ���rtable���н���Ԫ�صݼ���ColumnLink�����ǵı���
     *
     * @param clink The ColumnLink to be added to alias table, if same link already exists in
     *              <code>rtables</code>, just return the alias name previously set, else a set of
     *              ColumnLinks(decease elements one by one) will be inserted into <code>rtables</code>
     * @return String the alias table name
     */
    private String addAliasTable(ColumnLink clink) throws QueryException {
        String ret= (String)rtables.get(clink);
        if( ret !=null)
            return ret;
        String preAlias;
        if( clink.getColumns().length >1) {
            ColumnLink smallerOne=clink.getColumnLinkExcludeLastColumn();
            preAlias=addAliasTable(smallerOne);
        } else {
            preAlias=mainTable.getName();
        }
        String thisAlias="a"+rtables.size();
        rtables.put(clink, thisAlias);
        String subWhere= thisAlias+".ID (+)="+ preAlias+"."+clink.getLastColumn().getName();
        whereClause.add(subWhere);
        return thisAlias;
    }
    /**
    * @param columnLinks �����xxxID����������value ������������, ������Ϊ����������
    *  ���в��ֽ��͡�
    * @param value ��value �Ľ������������Ƕ�Ӧ������ݣ�����һ����ѯ�����䣬�磺
    *	��ֵ����  in (data1, data2, ...) data1,data2������IDֵ,
    *   ��ѯ������ء�in ( SELECT ID FROM table2 where table2.no like '%df%') (SELECT ID FROM����)
    * @param condition OR/AND/NOT OR/NOT AND
    */
    /*public void addParam(int[] columnLinks, String value,  int condition) throws QueryException{

    }*/
    public Expression getParamExpression(){
        return paramExpr;
    }
	/**
	 * Add param directly to where clause
	 * @param filter
	 * @throws QueryException
	 */
	public void addParam(String filter)throws QueryException {
		this.whereClause.add(filter);
	}
    /**
    *  ������VB�ķ�ʽ, �����������Ѿ����ý��������������"��"�Ĺ�ϵ���������ÿ��Լ�һЩ
    *  Ŀǰ֧�ִ˷����Ķ�ε��ã���getParamExpression()�����ص������ǵĽ���
    *  @return �����������������Ĳ��֣����������ӵ����δ����
    */
    public String addParam(Expression expr) throws QueryException{

        if (expr==null || (expr.isEmpty())) return null;
        boolean b;
        if (paramExpr ==null){
            paramExpr=expr;
            b=true;

        }else{
            paramExpr=paramExpr.combine(expr, SQLCombination.SQL_AND , null);
            b=false;
        }
        String s= constructWhereClause(expr,b);
        logger.debug("expr"+s);
        this.whereClause.add(s);
        return s;
    }
    /**
     * Will add ���� or ���� to paramDesc, but if this is the first element
     * call, no any additional desc will be added.
     * @param oper SQLCombination variable
     */
    private void startNewParamDesc(int oper){
        if (isStartingExpr) return;
        switch( oper){
            case SQLCombination.SQL_AND:
                paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-and")+" " ); break;
            case SQLCombination.SQL_AND_NOT:
                paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-and-not")+" "  );break;
            case SQLCombination.SQL_OR :
                paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-or")+" "  );break;
            case SQLCombination.SQL_OR_NOT :
                paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-or-not")+" "  );break;
        }
    }
    private String constructWhereClause(Expression expr, boolean startNewExpr) throws QueryException{
        String s;
        this.isStartingExpr = startNewExpr;

        if (expr.isLeaf() == false){
        	//logger.debug(" isleaf node");
            Expression exprLeft , exprRight;
            int oper;

            exprLeft = expr.getLeftElement();
            exprRight = expr.getRightElement();
            oper = expr.getOperator();
            paramDesc.append("(" );
            s = " (" + constructWhereClause(exprLeft, true);
            switch( oper){
                case SQLCombination.SQL_AND:
                	paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-and")+" " );
                    s += " AND "; break;
                case SQLCombination.SQL_AND_NOT:
                	paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-and-not")+" "  );
                    s += " AND NOT "; break;
                case SQLCombination.SQL_OR :
                	paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-or")+" "  );
                    s += " OR "; break;
                case SQLCombination.SQL_OR_NOT :
                	paramDesc.append(" "+MessagesHolder.getInstance().getMessage(locale,"sql-or-not")+" "  );
                    s += " OR NOT "; break;
            }
            s += constructWhereClause(exprRight, true) + ") ";
            paramDesc.append(")" );
        }else{ // is single leaf node
            if(expr.getColumnLink()!=null){
        		//logger.debug(" is single leaf node");
            	s = addParam(expr.getColumnLink().getColumnIDs() , expr.getCondition(), false, expr.getDescription() );
            }else{
            	// only add condition to where clause
            	logger.debug("Not found column link, construct exists clause");
                startNewParamDesc(SQLCombination.SQL_AND );
                paramDesc.append(expr.getDescription() );
                isStartingExpr=false;
            	s=expr.getCondition();
            }
        }
        return s;

    }

    public void addParam(int[] columnLinks, String value)  throws QueryException {
        addParam(columnLinks, value, true,null);
    }
    public void addParam(int[] columnLinks, String value, String desc) throws QueryException {
        addParam(columnLinks, value, true, desc);
    }
    /**
     * ��ѯ������������SQL �� WHERE �Ӿ�
     * @param columnLinks the column ids in order of mainTable columnID, referenceTable columnID,
     *        and referenceTable columnID's reference table columnID, and so on.
     *        columnLinks should have at least 1 elements.
     * @param value the query parameter, see QueryUtils.toSQLClause() for more details
     * @param bAddToParamList �Ƿ��޸�whereClause
     * @param desc param description, if null, construct it.
     * From 2.0 since column in table defintions may also be a columnlink, so we will
     * expand the input into real link first
     */
    private String addParam(int[] columns, String value, boolean bAddToWhereClause, String desc ) throws QueryException {
        //nmdemo, here's a bug in QueryInputHandler, there's one special operator
        //named "in" which must has a space after column, while the HttpServletRequest.getParameter()
        //will remove it from the raw input. SO I ADD A SPACE HERE FOR EVERY PARAM
        value=" "+value;
        //above is the modification
        
        if( manager ==null)
            manager=TableManager.getInstance();
        
        int[] columnLinks= this.parseColumnsToColumnLink(columns);
        if( columnLinks.length < 1)
            throw new QueryException("Size of columns "+Tools.toString(columnLinks)+" should be greater than 1");
        //logger.debug("columnLinks.length is "+columnLinks.length);
        if( columnLinks.length == 1) {
            return addParam(columnLinks[0],value, bAddToWhereClause,desc);
        } else {
            // check main table is identical to previous settings
            checkMainTable(columnLinks[0]);

            // create ColumnLink using elements before the last
            int[] c=new int[columnLinks.length -1];
            System.arraycopy(columnLinks, 0, c, 0, columnLinks.length -1);

            ColumnLink clink=new ColumnLink( c);
            // add table alias if needed
            String aliasTable= addAliasTable(clink);
            // create where clause
            Column lastColumn=manager.getColumn(columnLinks[columnLinks.length -1]);
            if(lastColumn.isUpperCase()) value=value.toUpperCase();
            
            String columnName=aliasTable+"."+ lastColumn.getName();
            //logger.debug("columnName ->"+columnName+" value->"+value+" type->"+lastColumn.getType());
            String sub=QueryUtils.toSQLClause(columnName,value, lastColumn.getType());

            if (bAddToWhereClause){
                whereClause.add(sub);
            }
            String subDesc=(desc==null?QueryUtils.toSQLClauseDesc(manager.getColumn(columnLinks[columnLinks.length-1]),(new ColumnLink(columnLinks)).getDescription(locale),
                    value, lastColumn.getType(),locale):desc);
            startNewParamDesc(SQLCombination.SQL_AND );
            paramDesc.append(subDesc);
            isStartingExpr=false;

            paramColumns.add(columnLinks);
            paramValues.add(value);
            return sub;
        }
    }
    /**
     * @param columnID the id must be in mainTable
     */
    private void checkMainTable(int columnID) throws QueryException {
        Column c=manager.getColumn(columnID);
        if(mainTable ==null ) {
            mainTable= c.getTable();
            return;
        }
        if( c.getTable().getId() !=mainTable.getId()) {
            throw new QueryException("Column linked ("+c+") must start from main table:"+ mainTable);
        }
    }
    /**
     * Add selection item to be retrieved, the item is from secendary table,
     * with primary key referred by primary table row. In this condition, not
     * only the very item should be selected, but also do the primary table column, if
     * <code>showAK</code> is set to true.
     *
     * ������Ҫָ�����ǣ�����Ҫ��ʾ���ֶ����Դӱ����������<code>showAK</code>Ϊtrue��
     * �������Ӧ�����ֶ�ҲҪ��ѡ����( ��Ϊ��ʾ�ֶε�hyperlink��ʾ��ҳ����)���Է�
     * ���û��鿴���ֶζ�Ӧ������������ݡ�����������£����������ֶν���ӱ��ֶ�֮��
     *
     * @param mainTableColumnID column of main table, note this should also be
     * selected but displayed only as a link on reTableColumn.
     * @param refTableColumnID the column to be selected to show
     * @param showAK If true, the reference table primary key will also be added to
     *      selection, and will be ordered next to <code>refTableColumnID</code>
     * @roseuid 3B830A780298
     */
    public void addSelection(int mainTableColumnID, int refTableColumnID, boolean showAK)
    throws QueryException {
    	int[] ids=new int[2];
		ids[0]=mainTableColumnID;
		ids[1]=refTableColumnID;
    	
    	/*Column cl=manager.getColumn(mainTableColumnID);
    	if(cl.isColumnLink()){
    		// recontructed to column link
    		ColumnLink lk= cl.getColumnLink();
    		int[] cis= lk.getColumnIDs();
    		ids=new int[1+ cis.length];
    		for(int i=0;i< cis.length;i++) ids[i]= cis[i];
    		ids[cis.length]= refTableColumnID;
    	}else{  	
    		ids=new int[2];
    		ids[0]=mainTableColumnID;
    		ids[1]=refTableColumnID;
    	}*/
		addSelection( ids, showAK, manager.getColumn(mainTableColumnID).getDescription(locale));
    	
    }
    /**
     * Input is columns in application dictionary, output is the real column link in Database
     * For instance, Column name like "B_HOUSE_ID;B_BUILDING_ID" can be a column in B_ROOM
     * That's a ColumnLink already
     * @param columns column link in application dictionary.
     * @return int[] column link in database
     * @throws QueryException
     */
    private int[] parseColumnsToColumnLink(int[] columns) throws QueryException {
    	ArrayList al=new ArrayList();
    	for(int i=0;i<columns.length;i++){
    		Column col= manager.getColumn(columns[i]);
    		if(col.isColumnLink()){
    			al.addAll( Arrays.asList( col.getColumnLink().getColumns()));
    		}else al.add(col);
    	}
    	int[] realCols= new int[al.size()];
    	for(int i=0;i< al.size();i++) realCols[i]= ((Column)al.get(i)).getId();
    	return realCols;
    }
    /**
     * Add selection item to be retrieved, each item is from the table that previous
     * item referred as reference table. The item before last item should also be
     * selected out( and will be displayed as link one page) if <code>showAK</code>
     * is set to <code>true</code>
     *
     * @param columnLinks column IDs in order of mainTable, refTable, refTable's refTable...
     * @param showAK if set to true, the column's PK will also be selected out.
     * From 2.0, since column may be ColumnLink it's self, so will parse each column first
     * to gain the real link.
     * @param title if null, will use first column's desc
     *   
     */
    public void addSelection(int[] columns, boolean showAK, String title)throws QueryException {
        if(Validator.isNull(title)) title= manager.getColumn(columns[0]).getDescription(locale);
    	int[] columnLinks= parseColumnsToColumnLink(columns);
    	if( columnLinks.length < 2) {
	        ColumnLink clink=new ColumnLink(columnLinks);
	        addSelection( clink,true,title );
            return;
        }
        // create ColumnLink using elements before the last
        int[] c=new int[columnLinks.length -1];
        System.arraycopy(columnLinks, 0, c, 0, columnLinks.length -1);

        ColumnLink clink=new ColumnLink(c);

        // add table alias if needed
        addAliasTable(clink);

        ColumnLink allCols=new ColumnLink(columnLinks);
        addSelection( allCols,true, title );

        if( !showAK)
            return;
        // add AK to selection list too
        int[] cc=new int[columnLinks.length];
        System.arraycopy(columnLinks, 0, cc, 0, columnLinks.length);
        cc[columnLinks.length -1]= allCols.getLastColumn().getTable().getPrimaryKey().getId();

        clink=new ColumnLink(cc);
        addSelection( clink,false ,title);// last table's primary key
    }

    /**
     * Add column to be selected
     * @param mainTableColumnID the column of the main table, for columns of
     * referenct table, refers to addSelection(int,int, int)
     * @2.0 �Ľ� �����ֶξ���һ��ColumnLink ���������� ";"�ķ���ʱ
     */
    public void addSelection(int mainTableColumnID) throws QueryException {
    	int[] c=new int[1];
        c[0]=mainTableColumnID;
        addSelection( c,false, manager.getColumn(mainTableColumnID).getDescription(locale) );
    }
    
    /**
     * Add selection item into selection list
     * @param clink the column item to be added
     * @param shown if ture, the item will be displayed, else, it's only used for links of previous
     *      selection item
     */
    private void addSelection(ColumnLink clink, boolean shown, String desc) {
        selections.add(clink);
        selectionDescs.add(desc);
        if(shown) {
            displayColumnIndices.add(new Integer(selections.size() -1));
        }
    }
    /**
     * This is to add a none-defined column into selection.
     * It's used by nds.cxtab.CxtabReport solely. Be vary careful, currently on toSQL and toGroupBySQL support
     * this method. and NERVER use QueryResultMetadata is used this method!
     * @param selectItem
     * @param desc
     * @throws QueryException
     */
    public void addSelection(String selectItem, String desc) throws QueryException{
    	selections.add(selectItem);
    	selectionDescs.add(desc);
    }
    
    /**
     * ���Ӷ๫˾���ݵĸ���
     * @param tableID
     * @param includeFilter true if need to include filter in the where clause,
     * use false only when copy records from other tables, which have the same
     * real table name as this table.
     * @param addtionalFilter just like ad_table.filter
     * @throws QueryException
     * @see nds.control.ejb.command.CopyTo
     * @since 2.0
     */
    public void setMainTable(int tableID, boolean includeFilter, String addtionalFilter)throws QueryException {
        if(mainTable ==null) {
            mainTable=manager.getTable(tableID);
        }else{
        	if ( tableID != mainTable.getId())
        		throw new QueryException("Not allow to set main table twice and different " );
        }
        if(mainTable ==null)
            throw new QueryException( "Table (id="+ tableID+") not found.");
        if (includeFilter && mainTable.getFilter()!=null ){
        	whereClause.add(mainTable.getFilter());
        }
        if(Validator.isNotNull(addtionalFilter)){
        	whereClause.add(addtionalFilter);
        } 
        // multiple 
    	if( mainTable.isAdClientIsolated()){
    		if(this.session !=null){
    			//throw new QueryException("Internal error:�� " + mainTable.getName()+" �ڶ����˾�乲������ǰ��ѯδ����������˾��Ϣ");
    		int ad_client_id= Tools.getInt(session.getAttribute("$AD_CLIENT_ID$"),-1);
    		if (ad_client_id ==-1 ) throw new QueryException("Internal error:�� " + mainTable.getName()+" �ڶ����˾�乲������ǰ��ѯδָ��������˾��Ϣ");
    		//add to param
    		Column adClientColumn= manager.getColumn(mainTable.getName(),"AD_CLIENT_ID");
    		// 2008-07-04 do not display client infor in filter description
    		String usersname= (String) session.getAttribute("$USER_NAME$");
    		//�������rootʱȥ����˾���ݵĹ�������
 			if(usersname!=null){
 			boolean mulit_clients = "true".equals(WebUtils.getProperty("mulit_clients", "false"));
			  if(mulit_clients&&"root".equals(usersname)&&ad_client_id==37 ) {
		            // root
		            return;
		        }
 			}else{
 				logger.debug("session is null"+session.toDebugString());
 				logger.debug("manager is null");
 			}
    		this.addParam(adClientColumn.getId(), ""+ad_client_id , true, "");//"("+ adClientColumn.getDescription(locale)+"="+ session.getAttribute("$AD_CLIENT_NAME$")+")");
    		}
    	}
    }
    /**
     * 
     * @param cols elements may be ColumnLink, will split it
     * @return
     * @throws QueryException
     */
    private int[] expandColumns(int[] cols)throws QueryException {
    	ColumnLink cs= new ColumnLink(cols);
    	cs = new ColumnLink(cs.toString());
    	return cs.getColumnIDs();
    }
    /**
     * 
     * Use this one when only one order by column is issued, else use addOrdeBy.
     */
    public void setOrderBy(int[] cols, boolean ascending) throws QueryException {
        
    	if ( cols==null || cols.length ==0) return;
        if( manager ==null)
            manager=TableManager.getInstance();
        orderAscending=ascending;
        orderColumn=cols;
        
        cols=expandColumns(cols); 
        
        if( cols.length == 1) {
            Column col= manager.getColumn(cols[0]);
            if( col.isVirtual()== false){
            	addOrderByClause(col.getTable().getName()+"."+col.getName()+(ascending?" asc":" desc"));
            }else{
            	addOrderByClause(col.getName()+(ascending?" asc":" desc"));
            }
            return;
        }
        // create ColumnLink using elements before the last
        int[] c=new int[cols.length -1];
        System.arraycopy(cols, 0, c, 0, cols.length -1);

        ColumnLink clink=new ColumnLink( c);
        // add table alias if needed
        String tableAlias=addAliasTable(clink);
        if(clink.getLastColumn().isVirtual()== false) {
        	addOrderByClause(tableAlias+"."+manager.getColumn(cols[cols.length-1]).getName()+(ascending?" asc":" desc"));
        }else{
        	addOrderByClause(clink.getLastColumn().getName()+(ascending?" asc":" desc"));
        }

    }
    /**
     * Add more order by clause to query, you can call this to replace setOrderBy.
     * Note only the first order by will be returned as getOrderAscending, or getOrderColumn
     * support orderColumnlink and orderColumn not return first order
     * @param cols
     * @param ascending
     * @throws QueryException
     * @since 2.0
     */
    public void addOrderBy(int[] cols, boolean ascending) throws QueryException {
        if ( cols==null || cols.length ==0) return;
        if( manager ==null)
            manager=TableManager.getInstance();
        //set orderColumnlink
		if (orderColumnlink == null) {
			orderAscending = ascending;
			if (cols.length > 1) {
				orderColumnlink = cols;
			}
		} else {
			ArrayUtil a = new ArrayUtil();
			if (cols.length > 1) {
				int[] newArray = a.append(orderColumnlink, cols);
				orderColumnlink = newArray;
			}

		}
		// set orderColumn
		if (orderColumn == null) {
			orderAscending = ascending;
			if (cols.length == 1) {
				orderColumn = cols;
			}
		} else {
			ArrayUtil a = new ArrayUtil();
			if (cols.length == 1) {
				int[] newArray = a.append(orderColumn, cols);
				orderColumn = newArray;
			}
		}
		
        cols=expandColumns(cols); 
        
        if( cols.length == 1) {
            Column col= manager.getColumn(cols[0]);
            if( col.isVirtual()== false){
            	addOrderByClause( col.getTable().getName()+"."+col.getName()+(ascending?" asc":" desc"));
            }else{
            	addOrderByClause( col.getName()+(ascending?" asc":" desc"));
            }
            return;
        }
        // create ColumnLink using elements before the last
        int[] c=new int[cols.length -1];
        System.arraycopy(cols, 0, c, 0, cols.length -1);

        ColumnLink clink=new ColumnLink( c);
        // add table alias if needed
        String tableAlias=addAliasTable(clink);
        if(clink.getLastColumn().isVirtual()== false) {
        	addOrderByClause( tableAlias+"."+manager.getColumn(cols[cols.length-1]).getName()+(ascending?" asc":" desc"));
        }else{
        	addOrderByClause(  clink.getLastColumn().getName()+(ascending?" asc":" desc"));
        }

    }
    private void addOrderByClause(String newOrderBy){
    	if (Validator.isNull(orderbyClause))
    		orderbyClause= newOrderBy;
    	else
    		orderbyClause += ","+newOrderBy;
    }

    /**
     * ���ҷ�Χ�����ҵ������б��ȡ����startIdx��ʼ�����range���ļ�¼��
     * @roseuid 3B830C630190
     */
    public void setRange(int startIdx, int range) {
        startRowIndex=startIdx;
        this.range=range;
    }

    /**
     * ��˭(jsp or servlet)�����ѯ���
     * getServletConfig().getServletContext().getRequestDispatcher(url).forward(req, resp);
     * @param url the handler of QueryResult to be generated. If null, use
     * REFERENCE attribute of Request.
     * @roseuid 3B84C8C70060
     */
    public void setResultHandler(String url) {
        resultHandler=url;
    }

    /**
     * ��Щselect���Ľṹ�ܸ��ӣ����ǿ���ֱ��дSQL�ķ�ʽ
     * @roseuid 3B86EC9E0315
     */
    public void setSQL(String sql) {
        directSQL=sql;
    }
    /**
     * @param replaceVariables if true, will replace wildcard variables to QuerySession attributes
     * @return description of param conditions
     */
    public String getParamDesc(boolean replaceVariables){
        return replaceVariables? replaceVariables( paramDesc.toString()) :paramDesc.toString();
    }

    /**
     * Similiar to #toCountSQL, in such format of return string:
     *   select t1.id from t1,t2 where ...
     * This is used for sub query.
     * @param replaceVariables if true, will replace wildcard variables to QuerySession attributes
     * @return SQL that only select primarky key of main table
     * @throws QueryExpression
     */
    public String toPKIDSQL(boolean replaceVariables) throws QueryException{
        if(directSQL !=null)
            throw new Error("Internal error, method toPKIDSQL is not valid for direct sql");
        String sql="SELECT "+ mainTable.getName()+ "."+ mainTable.getPrimaryKey().getName() +  getGrossSQL();
        return replaceVariables? replaceVariables( sql) :sql;
    }
    /**
     * 
     * @return if table is view, return "real-table-name tablename",
     * else return "real-table-name"
     */
    private void appendFromMainTableClause(StringBuffer  sql){
    	if (mainTable.isView() ) sql.append(mainTable.getRealTableName()).append(" ");
		sql.append(mainTable.getName());
    }
    
    /**
     * Only return the sql string after From, so be useful for
     * both toPKIDSQL and toCountSQL
     */
    private String getGrossSQL() throws QueryException{
        StringBuffer sql=new StringBuffer();
        sql.append(" FROM ");
        appendFromMainTableClause(sql);	
        
		Enumeration enu= rtables.keys();
    	/**
    	 * If alias table is view instead of table, the table name should
    	 * not be the view name(only exists in definition file, not exists in
    	 * db), but the table name(exists in db)
    	 * @since 2.0
    	 */
        while(enu.hasMoreElements()) {
            ColumnLink clink=(ColumnLink)enu.nextElement();
            Table table= clink.getLastColumn().getReferenceTable(true);
            String alias=(String)rtables.get(clink);
            //sql.append(",").append( table.getName()).append(" ").append(alias);
            sql.append(",").append( table.getRealTableName()).append(" ").append(alias);
        }
        if( whereClause.size()>0) {
            sql.append(" WHERE (").append(whereClause.get(0)).append(")" );
            for(int i=1;i< whereClause.size();i++) {
                sql.append(" AND (").append(whereClause.get(i)).append(")") ;
            }
        }
        return sql.toString();
    }
    public String toCountSQL() throws QueryException {
        if(directSQL !=null)
            throw new Error("Internal error, method toCountSQL is not valid for direct sql");
        String sql="SELECT count(*)" + getGrossSQL();
        return this.replaceVariables(sql);
    }

    /** This is a sample converting a normal sql string to sql with range:
     *
     *  original sql:
     *
     *  SELECT Customer.NO b0,Customer.NAME b1,a0.CUSTOMERSORTDETAIL b2,a0.ID b3,a1.NO b4,a1.ID b5,Customer.BEGINDATE b6,Customer.ENDDATE b7,Customer.STORENO b8,Customer.COUNTRY b9,Customer.PROVINCE b10,Customer.CITY b11,Customer.ADDRESS b12,Customer.POSTCODE b13,Customer.LINKMAN b14,Customer.LINKMANDEPT b15,Customer.POSITION b16,Customer.OFFICEPHONE b17,Customer.OFFICEFAX b18,Customer.HOMEPHONE b19,Customer.MOBILE b20,Customer.REGISTERBANK b21,Customer.TAXNO b22,Customer.BANKNO b23,Customer.SAVAMT b24,a2.NAME b25,a2.ID b26,a3.NAME b27,a3.ID b28,Customer.CREATIONDATE b29,Customer.MODIFIEDDATE b30,Customer.PERMISSION b31
     *   FROM Customer,Department a1,Users a3,CustomerSort a0,Users a2 WHERE (a0.ID (+)=Customer.CUSTOMERSORTID) AND (a1.ID (+)=Customer.DEPARTMENTID) AND (a2.ID (+)=Customer.OWNERID) AND (a3.ID (+)=Customer.MODIFIERID) AND ( (Customer.ID=0) )
     *
     *  converted to:
     *
     *   SELECT b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11,b12,b13,b14,b15,b16,b17,b18,b19,b20,b21,b22,b23,b24,b25,b26,b27,b28,b29,b30,b31
     *   FROM ( SELECT ROWNUM row_num, b0,b1,b2,b3,b4,b5,b6,b7,b8,b9,b10,b11,b12,b13,b14,b15,b16,b17,b18,b19,b20,b21,b22,b23,b24,b25,b26,b27,b28,b29,b30,b31 FROM (
     *   SELECT Customer.NO b0,Customer.NAME b1,a0.CUSTOMERSORTDETAIL b2,a0.ID b3,a1.NO b4,a1.ID b5,Customer.BEGINDATE b6,Customer.ENDDATE b7,Customer.STORENO b8,Customer.COUNTRY b9,Customer.PROVINCE b10,Customer.CITY b11,Customer.ADDRESS b12,Customer.POSTCODE b13,Customer.LINKMAN b14,Customer.LINKMANDEPT b15,Customer.POSITION b16,Customer.OFFICEPHONE b17,Customer.OFFICEFAX b18,Customer.HOMEPHONE b19,Customer.MOBILE b20,Customer.REGISTERBANK b21,Customer.TAXNO b22,Customer.BANKNO b23,Customer.SAVAMT b24,a2.NAME b25,a2.ID b26,a3.NAME b27,a3.ID b28,Customer.CREATIONDATE b29,Customer.MODIFIEDDATE b30,Customer.PERMISSION b31
     *   FROM Customer,Department a1,Users a3,CustomerSort a0,Users a2 WHERE (a0.ID (+)=Customer.CUSTOMERSORTID) AND (a1.ID (+)=Customer.DEPARTMENTID) AND (a2.ID (+)=Customer.OWNERID) AND (a3.ID (+)=Customer.MODIFIERID) AND ( (Customer.ID=0) ) ))
     *   WHERE row_num BETWEEN 0 AND 5
     */
    public String toSQLWithRange()throws QueryException {
        if(directSQL !=null)
            return directSQL;
        // assemble SQL
        StringBuffer sql=new StringBuffer("SELECT "), outSelect=new StringBuffer();

        for( int i=0;i<selections.size();i++) {
            if(i >0)
                sql.append(",");
            Object sitem= selections.get(i);//  ColumnLink or String
            ColumnLink c=null;
            if(sitem instanceof ColumnLink)
            	c=(ColumnLink)selections.get(i);
            String alias;
            if(c!=null){
            	// check security grade,yfzhu 20100501
                if(this.securityGrade<c.getLastColumn().getSecurityGrade()){
                	//do not load this column
                	sql.append("null b"+i);
                }else{
		            if( c.getColumns().length > 1) {
		                // has alias for table
		                alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
		                if(alias ==null)
		                    throw new Error("Internal error: table alias not found for selection:"+c);
	                	sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
		            } else {
		                if(c.getLastColumn().isVirtual()== false) {
		                	alias=mainTable.getName();
		                	sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
		                }else{
		                	sql.append("("+c.getLastColumn().getName()+") b"+i);
		                }
		            }
                }
            }else{
            	sql.append("("+sitem+") b"+i);
            }
        }

        for( int j=0;j< selections.size();j++){
            if(j>0) outSelect.append(",");
            outSelect.append("b"+j);
        }

        sql.append(" FROM ");
        appendFromMainTableClause(sql);
        
        Enumeration enu= rtables.keys();

        while(enu.hasMoreElements()) {
            ColumnLink clink=(ColumnLink)enu.nextElement();
            Table table= clink.getLastColumn().getReferenceTable(true);
            String alias=(String)rtables.get(clink);
            sql.append(","+ table.getRealTableName()+" "+alias);
        }
        //  ######## yfzhu added code here for handling virtual column
        ArrayList aliasTables= mainTable.getAliasTables();
        AliasTable aliasTable=null;
        if (aliasTables!=null && aliasTables.size()>0){
        	for (int i=0; i< aliasTables.size(); i++){
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(","+ aliasTable.getRealTableName()+" "+ aliasTable.getName());
        	}
        }
        // ############ added above
        if( whereClause.size()>0 ) {
            sql.append(" WHERE ("+whereClause.get(0)+")" );
            for(int i=1;i< whereClause.size();i++) {
                sql.append(" AND ("+whereClause.get(i)+")") ;
            }
        }
        //  ######## yfzhu added code here for handling virtual column
        if ( aliasTables !=null && aliasTables.size()>0){
        	aliasTable=(AliasTable)aliasTables.get(0);
        	if ( whereClause.size() ==0 ) sql.append(" WHERE ("+aliasTable.getCondition()+")" );
            else sql.append(" AND ("+aliasTable.getCondition()+")" );
        	for (int i=1;i< aliasTables.size();i++) {
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(" AND (" +aliasTable.getCondition()+")" );
        	}
        }
        // ############ added above
        if(orderbyClause !=null)
            sql.append(" ORDER BY "+ orderbyClause);
//        return "SELECT "+ outSelect+" FROM ( SELECT ROWNUM row_num, "+ outSelect+" FROM ( "+
//                sql+ " )) WHERE row_num BETWEEN "+(getStartRowIndex()+1)+" AND "+(getStartRowIndex()+getRange());
        String asql=this.replaceVariables(sql.toString());
        
        // if start index <0 then, fetch last records
        String bsql;
        if( getStartRowIndex() <0){
        	// FORMAT:
        	// SELECT * FROM (SELECT ROWNUM rownum, column1, column2, upto columnN FROM DATA_TABLE)
        	// WHERE rownum > ( SELECT (MAX(ROWNUM)-10) FROM DATA_TABLE);
        	asql= nds.util.StringUtils.replace(asql, "SELECT ", "SELECT ROWNUM row_num, ",1);
        	bsql=this.replaceVariables("SELECT "+ outSelect+" FROM ( "+
            		asql+ " ) WHERE ROWNUM > (SELECT MAX(ROWNUM)-"+ getRange()+ getGrossSQL()+ ")") ;
            return this.replaceVariables(bsql);
        }else{
        	bsql=this.replaceVariables("SELECT "+ outSelect+" FROM ( SELECT ROWNUM row_num, "+ outSelect+" FROM ( "+
            		asql+ " ) WHERE ROWNUM <= "+ (getStartRowIndex()+getRange()) + " ) WHERE row_num>="+ (getStartRowIndex()+1));
        }
        return bsql;

    }
    
    public String toGroupBySQL(List paramList)
    		throws QueryException
    		{
    	return toGroupBySQL(paramList, false);
    		}

    /**
     * To group by sql 
     * @param facts elements are String 
     * @return all selection columns will be set as category column, so the return sql
     * will be like:
     *   select <selections>, <facts> from <maintable>, <fkTable> where <filter> group by <selections>
     * @throws QueryException
     */
    public String toGroupBySQL(List facts,boolean isorder ) throws QueryException{
        // assemble SQL
        StringBuffer sql=new StringBuffer("SELECT ");
        for( int i=0;i<selections.size();i++) {
            String alias;
            if(i >0)
                sql.append(",");
            Object sitem= selections.get(i);
            ColumnLink c=null;
            if(sitem instanceof ColumnLink){
            	c=(ColumnLink)selections.get(i);
            	// check security grade,yfzhu 20100501
                if(this.securityGrade<c.getLastColumn().getSecurityGrade()){
                	//do not load this column
                	sql.append("null b"+i);
                }else{
		            if( c.getColumns().length > 1) {
		                // has alias for table
		                alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
		                if(alias ==null)
		                    throw new Error("Internal error: table alias not found for selection:"+c);
						sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
		            } else {
		                if(c.getLastColumn().isVirtual()== false) {
		                	alias=mainTable.getName();
		                	sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
		                }else{
		                	sql.append(c.getLastColumn().getName()+" b"+i);
		                }
		            }
                }
            }else{
            	sql.append(sitem+" b"+i);
            }
        }
        if( selections.size()>0) sql.append(",");
        // sum 
        for(int i=0;i<facts.size();i++){
        	if(i>0)sql.append(",");
        	sql.append(facts.get(i)+" s"+i);
        }

        sql.append(" FROM ");
        appendFromMainTableClause(sql);
        
        Enumeration enu= rtables.keys();

        while(enu.hasMoreElements()) {
            ColumnLink clink=(ColumnLink)enu.nextElement();
            Table table= clink.getLastColumn().getReferenceTable(true);
            String alias=(String)rtables.get(clink);
            sql.append(","+ table.getRealTableName()+" "+alias);
        }

        ArrayList aliasTables= mainTable.getAliasTables();
        AliasTable aliasTable=null;
        if (aliasTables!=null && aliasTables.size()>0){
        	for (int i=0; i< aliasTables.size(); i++){
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(","+ aliasTable.getRealTableName()+" "+ aliasTable.getName());
        	}
        }

        if( whereClause.size()>0) {
            sql.append(" WHERE ("+whereClause.get(0)+")") ;
            for(int i=1;i< whereClause.size();i++) {
                sql.append(" AND ("+whereClause.get(i)+")" );
            }
        }
        if ( aliasTables !=null && aliasTables.size()>0){
        	aliasTable=(AliasTable)aliasTables.get(0);
        	if ( whereClause.size()==0 ) sql.append(" WHERE ("+aliasTable.getCondition()+")" );
            else sql.append(" AND ("+aliasTable.getCondition()+")" );
        	for (int i=1;i< aliasTables.size();i++) {
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(" AND (" +aliasTable.getCondition()+")" );
        	}
        }
        // group by 
        if(selections.size()>0) sql.append(" GROUP BY ");
        for( int i=0;i<selections.size();i++) {
            if(i >0)
                sql.append(",");
            Object sitem= selections.get(i);
            ColumnLink c=null;
            if(sitem instanceof ColumnLink){
            
            	c=(ColumnLink)selections.get(i);
	            String alias;
	            if( c.getColumns().length > 1) {
	                // has alias for table
	                alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
	                if(alias ==null)
	                    throw new Error("Internal error: table alias not found for selection:"+c);
					sql.append(alias+"."+c.getLastColumn().getName());
	            } else {
	                if(c.getLastColumn().isVirtual()== false) {
	                	alias=mainTable.getName();
	                	sql.append(alias+"."+c.getLastColumn().getName());
	                }else{
	                	sql.append(c.getLastColumn().getName());
	                }
	            }
            }else{
            	sql.append(sitem);
            }
        }        
        if(!isorder){
        if(orderbyClause !=null)
            sql.append(" ORDER BY "+ orderbyClause);
        }else{
        	sql.append(" ORDER BY ");
        	for (int j = 0; j < selections.size(); j++) {
        		if (j > 0) {
        			sql.append(",");
        		}
        		Object sitem= selections.get(j);
        		ColumnLink c=null;
        		if (sitem instanceof ColumnLink){

        			c=(ColumnLink)selections.get(j);
        			String alias;
        			if (c.getColumns().length > 1)
        			{
        				alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
        				if(alias ==null)
        				{
        					throw new Error("Internal error: table alias not found for selection:" + c);
        				}sql.append((String)alias + "." + c.getLastColumn().getName());
        			}
        			else if (!c.getLastColumn().isVirtual()) {
        				alias = this.mainTable.getName();
        				sql.append((String)alias + "." + c.getLastColumn().getName());
        			} else {
        				sql.append(c.getLastColumn().getName());
        			}
        		}
        		else {
        			sql.append(sitem);
        		}
        	}


        }
        
        return this.replaceVariables(sql.toString());    	
    }
    
     public String toSQL() throws QueryException {
        return toSQL(true);
   }
    
    
    /**
     * This first column must be PK of the main table, which will not count in display columns
     * @roseuid 3B8AFCFB0083
     */
    public String toSQL(boolean paramBoolean) throws QueryException {
        if(directSQL !=null)
            return directSQL;
        // assemble SQL
        StringBuffer sql=new StringBuffer("SELECT ");//+ mainTable.getName()+"."+mainTable.getPrimaryKey().getName();
        for( int i=0;i<selections.size();i++) {
            String alias;
            if(i >0)
                sql.append(",");
            Object sitem= selections.get(i);
            ColumnLink c=null;
            if(sitem instanceof ColumnLink){
            
	            c=(ColumnLink)selections.get(i);
	            // 	check security grade,yfzhu 20100501
                if(this.securityGrade<c.getLastColumn().getSecurityGrade()){
                	//do not load this column
                	if (paramBoolean) sql.append("null b" + i); else
                		 sql.append("null ").append(((ColumnLink)c).toString('_'));
                	//sql.append("null b"+i);
                }else{
		            if( c.getColumns().length > 1) {
		                // has alias for table
		                alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
		                if(alias ==null)
		                    throw new Error("Internal error: table alias not found for selection:"+c);
						sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
		            } else {
		                if(c.getLastColumn().isVirtual()== false) {
		                	alias=mainTable.getName();
		                	sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
		                }else{
		                	sql.append(c.getLastColumn().getName()+" b"+i);
		                }
		
		            }
                }
            }else{
            	sql.append(sitem+" b"+i);
            }
        }
        // }##### added above


        sql.append(" FROM ");
        appendFromMainTableClause(sql);
        //logger.debug("FROM sql"+sql);
        Enumeration enu= rtables.keys();

        while(enu.hasMoreElements()) {
            ColumnLink clink=(ColumnLink)enu.nextElement();
            Table table= clink.getLastColumn().getReferenceTable(true);
            String alias=(String)rtables.get(clink);
            sql.append(","+ table.getRealTableName()+" "+alias);
        }
        //logger.debug("whereClause1 sql"+sql);
        //  ######## yfzhu added code here for handling virtual column
        ArrayList aliasTables= mainTable.getAliasTables();
        AliasTable aliasTable=null;
        if (aliasTables!=null && aliasTables.size()>0){
        	for (int i=0; i< aliasTables.size(); i++){
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(","+ aliasTable.getRealTableName()+" "+ aliasTable.getName());
        	}
        }
        // ############ added above
       
        if( whereClause.size()>0) {
            sql.append(" WHERE ("+whereClause.get(0)+")") ;
            //logger.debug("whereClause2 sql"+sql);
            for(int i=1;i< whereClause.size();i++) {
                sql.append(" AND ("+whereClause.get(i)+")" );
            }
        }
        //logger.debug("whereClause3 sql"+sql);
        //  ######## yfzhu added code here for handling virtual column
        if ( aliasTables !=null && aliasTables.size()>0){
        	aliasTable=(AliasTable)aliasTables.get(0);
        	if ( whereClause.size()==0 ) sql.append(" WHERE ("+aliasTable.getCondition()+")" );
            else sql.append(" AND ("+aliasTable.getCondition()+")" );
        	for (int i=1;i< aliasTables.size();i++) {
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(" AND (" +aliasTable.getCondition()+")" );
        	}
        }
        // ############ added above

        if(orderbyClause !=null)
            sql.append(" ORDER BY "+ orderbyClause);
        return this.replaceVariables(sql.toString());
    }

    /**
     * @roseuid 3B8AFCFB00D3
     */
    public String getResultHandler() {
        return resultHandler;
    }

    /**
     * @roseuid 3B8AFCFB00FB
     */
    public Table getMainTable() {
        return mainTable;
    }

    /**
     * Get selections columnlink's last column, might be String instead of Column 
     * @roseuid 3B8AFCFB0123
     */
    public ArrayList getAllSelectionColumns() {
        ArrayList list=new ArrayList();
        for( int i=0;i< selections.size();i++) {
        	Object sitem=selections.get(i);
        	if(sitem instanceof ColumnLink)
        		list.add( ((ColumnLink)sitem).getLastColumn());
        	else list.add(sitem);
        }
        return list;
    }
    public ArrayList getAllSelectionColumnLinks(){
        return selections;
    }
    /**
     * Just selection titles
     * @return Selection's description in order as selection column links
     * 
     */
    public ArrayList getAllSelectionDescriptions(){
    	return selectionDescs;
    }
    /**
     * From 2.0 changed to first column's name
     * @return column description concatenated by references.
     * ������Ҫ��ʾ���Ƕ��������������ڵĲ������ƣ���Խ��column�ǣ�
     *  order.applierID, employee.departmentID, department.name
     *  ��Ӧ��column���Ʒֱ���:������, ���ţ����ơ���ϳɵ�����Ϊ��
     *      �����˲�������
     */
    public String[] getDisplayColumnNames(boolean showNullableIndicator) {
        int[] ids=  getDisplayColumnIndices();
        String[] dcns=new String[ids.length];
        for(int i=0;i< ids.length;i++) {
            dcns[i]=(String) selectionDescs.get(ids[i]);
            /*ColumnLink clink=(ColumnLink)selections.get(ids[i]);
            dcns[i]="";
            for(int j=0;j< clink.getColumns().length;j++) {
                dcns[i] += "" + clink.getColumns()[j].getDescription();
            }
            dcns[i] += (showNullableIndicator && !clink.getColumns()[0].isNullable())?"*":" ";
            */
        }
        return dcns;
    }
    public String[] getDisplayColumnNames2(boolean showNullableIndicator) {
        throw new UnsupportedOperationException("Unsupported function");
    }
    /**
     * @roseuid 3B8AFCFB014B
     */
    public int[] getDisplayColumnIndices() {
        int[] ids=new int[displayColumnIndices.size()];
        for(int i=0;i< displayColumnIndices.size();i++) {
            ids[i]= ((Integer)displayColumnIndices.get(i)).intValue();
        }
        return ids;
    }

    public int getRange() {
        return range;
    }
    /**
     * query ���Խ����ҳ��ʾ�����ﷵ�ظò�ѯ�������ʾҳ�У�
     * ��һ����¼�ڲ�ѯ����е��к�
     */
    public int getStartRowIndex() {
        return startRowIndex;
    }
    /////////override java.lang.Object////
    public String toString() {
        try {
            return this.replaceVariables(toSQL());
        } catch(Exception e) {
            return "QueryRequestImpl";
        }
    }

    


    /**
     * Wrapper this request to a string so can be stored in HTML page, note it's base64 encoded
     * @see QueryRequestImpl.toQueryRequest
     */
    public String toStorageString()throws IOException {
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(baos);
        oos.writeObject(this);

        byte[] data= baos.toByteArray();
        return new String(Base64.encode(data));
    }

    public int getSelectionCount() {
        return selections.size();
    }
    public int[] getSelectionColumnLink(int position) {
    	if(selections.get(position) instanceof ColumnLink){
        ColumnLink link=(ColumnLink) selections.get(position);
        Column[] columns= link.getColumns();
        int[] cs=new int[columns.length];
        for(int i=0;i< columns.length;i++) {
            cs[i]= columns[i].getId();
        }
        return cs;
    	}else
    		return null;
    }

    public boolean isSelectionShowable(int position) {

        for( int i=0;i< displayColumnIndices.size();i++) {
            if( ((Integer)displayColumnIndices.get(i)).intValue()==position)
                return true;
        }
        return false;
    }
    public int getParamCount() {
        return paramValues.size();
    }
    public int[] getParamColumnLink(int position) {
        return (int[]) paramColumns.get(position);
    }
    public String getParamValue(int position) {
        return (String) paramValues.get(position);
    }
    /**
     * @return boolean if has serveral order columns, this only return the first one
     */
    public boolean isAscendingOrder() {
        return orderAscending;
    }
    /**
     * @return boolean if has serveral order columns, this only return the first one
     * change renturn all
     */
    public int[] getOrderColumnLink() {
        return orderColumn;
    }
    
   /**
    *  @return boolean if has serveral order columns, this only return the all
    */
    public int[] getOrderColumnLinks() {
        return orderColumnlink;
    }
    
    /**
     * Similiar to #getDisplayColumnIndices, except that when
     * pk and ak are set not show, the indices will also be excluded.
     */
    public int[] getReportDisplayColumnIndices(boolean pk, boolean ak){
        int[] display= getDisplayColumnIndices();
        ArrayList al=new ArrayList();
        for( int i=0;i<display.length;i++) {
        	if(! (selections.get(display[i]) instanceof ColumnLink)) continue;
            ColumnLink c=(ColumnLink)selections.get(display[i]);
            if(!pk && c.getLastColumn().getId() == mainTable.getPrimaryKey().getId())
                continue;//�������Ҫ��ʾ�������Ͳ���ʾ
            if(!ak && c.getLastColumn().getId() == mainTable.getAlternateKey().getId())
                continue;//�������Ҫ��ʾAK���Ͳ���ʾ
            al.add( new Integer(display[i]));
        }
        int[] ret= new int[al.size()];
        for (int i=0;i< ret.length;i++) ret[i]=( (Integer)al.get(i)).intValue();
        return ret;
    }
    /**
     * ��ȡ�����������棨report����SQL���,Hawke
     */
    public String getSQLForReport(boolean pk, boolean ak) throws QueryException {
        if(directSQL !=null)
            return directSQL;
        // assemble SQL
        StringBuffer sql=new StringBuffer("SELECT ");//+ mainTable.getName()+"."+mainTable.getPrimaryKey().getName();
        int[] display = getDisplayColumnIndices();
        String buff = ""; int j=0;
        for( int i=0;i<display.length;i++) {
        	if (!(selections.get(display[i]) instanceof ColumnLink)) continue;
            ColumnLink c=(ColumnLink)selections.get(display[i]);
            if(!pk && c.getLastColumn().getId() == mainTable.getPrimaryKey().getId())
                continue;//�������Ҫ��ʾ�������Ͳ���ʾ
            if(!ak && c.getLastColumn().getId() == mainTable.getAlternateKey().getId())
                continue;//�������Ҫ��ʾAK���Ͳ���ʾ
            String alias;
            if(j >0)
                sql.append(",");
            j++;
            
         // check security grade,yfzhu 20100501
            if(this.securityGrade<c.getLastColumn().getSecurityGrade()){
            	//do not load this column
            	sql.append("null");
            }else{
	            if( c.getColumns().length > 1) {
	                // has alias for table
	                alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
	                if(alias ==null)
	                    throw new Error("Internal error: table alias not found for selection:"+c);
					sql.append(alias+"."+c.getLastColumn().getName());
	            } else {
	                if(c.getLastColumn().isVirtual()== false) {
	                	alias=mainTable.getName();
	                	sql.append(alias+"."+c.getLastColumn().getName());
	                }else{
	                	sql.append(c.getLastColumn().getName());
	                }
	
	            }
            }
        }
        sql.append(" FROM ");
        appendFromMainTableClause(sql);
        Enumeration enu= rtables.keys();

        while(enu.hasMoreElements()) {
            ColumnLink clink=(ColumnLink)enu.nextElement();
            Table table= clink.getLastColumn().getReferenceTable(true);
            String alias=(String)rtables.get(clink);
            sql.append(","+ table.getRealTableName()+" "+alias);
        }
        //  ######## yfzhu added code here for handling virtual column
        ArrayList aliasTables= mainTable.getAliasTables();
        AliasTable aliasTable=null;
        if (aliasTables!=null && aliasTables.size()>0){
        	for (int i=0; i< aliasTables.size(); i++){
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(","+ aliasTable.getRealTableName()+" "+ aliasTable.getName());
        	}
        }
        // ############ added above

        if( whereClause.size()>0) {
            sql.append(" WHERE ("+whereClause.get(0)+")" );
            for(int i=1;i< whereClause.size();i++) {
                sql.append(" AND ("+whereClause.get(i)+")") ;
            }
        }
        //  ######## yfzhu added code here for handling virtual column
        if ( aliasTables !=null && aliasTables.size()>0){
        	aliasTable=(AliasTable)aliasTables.get(0);
        	if ( whereClause.size()==0 ) sql.append(" WHERE ("+aliasTable.getCondition()+")" );
            else sql.append(" AND ("+aliasTable.getCondition()+")" );
        	for (int i=1;i< aliasTables.size();i++) {
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(" AND (" +aliasTable.getCondition()+")" );
        	}
        }
        // ############ added above

        if(orderbyClause !=null)
            sql.append(" ORDER BY "+ orderbyClause);
        return this.replaceVariables(sql.toString());
        //return sql.toString();
    }
    /**
     * ��ȡ�����������棨report����SQL���,Hawke
     */
    public String getSQLForReportWithRange(boolean pk,boolean ak) throws QueryException {
        if(directSQL !=null)
            return directSQL;
        // assemble SQL
        int[] display = getDisplayColumnIndices();
        StringBuffer sql=new StringBuffer("SELECT "), outSelect=new StringBuffer("");
        //String buff = "";
        int j=0;
        for( int i=0;i<display.length;i++) {
        	if (!(selections.get(display[i]) instanceof ColumnLink)) continue;
            ColumnLink c=(ColumnLink)selections.get(display[i]);
            if(!pk && c.getLastColumn().getId() == mainTable.getPrimaryKey().getId())
                continue;//�������Ҫ��ʾ�������Ͳ���ʾ
            if(!ak && c.getLastColumn().getId() == mainTable.getAlternateKey().getId())
                continue;//�������Ҫ��ʾAK���Ͳ���ʾ
            String alias;
            if(j >0){
                sql.append(",");
                outSelect.append(",");
            }
            j++;
         // check security grade,yfzhu 20100501
            if(this.securityGrade<c.getLastColumn().getSecurityGrade()){
            	//do not load this column
            	sql.append("null b"+i);
            }else{
	            if( c.getColumns().length > 1) {
	                // has alias for table
	                alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
	                if(alias ==null)
	                    throw new Error("Internal error: table alias not found for selection:"+c);
					sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
	            } else {
	                if(c.getLastColumn().isVirtual()== false) {
	                	alias=mainTable.getName();
	                	sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
	
	                }else{
	                	sql.append("("+c.getLastColumn().getName()+") b"+i);
	
	                }
	            }
            }
			outSelect.append("b"+i);
        }

        sql.append(" FROM ");
        appendFromMainTableClause(sql);
        Enumeration enu= rtables.keys();

        while(enu.hasMoreElements()) {
            ColumnLink clink=(ColumnLink)enu.nextElement();
            Table table= clink.getLastColumn().getReferenceTable(true);
            String alias=(String)rtables.get(clink);
            sql.append(","+ table.getRealTableName()+" "+alias);
        }
        //  ######## yfzhu added code here for handling virtual column
        ArrayList aliasTables= mainTable.getAliasTables();
        AliasTable aliasTable=null;
        if (aliasTables!=null && aliasTables.size()>0){
        	for (int i=0; i< aliasTables.size(); i++){
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(","+ aliasTable.getRealTableName()+" "+ aliasTable.getName());
        	}
        }
        // ############ added above

        if( whereClause.size()>0) {
            sql.append(" WHERE ("+whereClause.get(0)+")" );
            for(int i=1;i< whereClause.size();i++) {
                sql.append(" AND ("+whereClause.get(i)+")") ;
            }
        }
        //  ######## yfzhu added code here for handling virtual column
        if ( aliasTables !=null && aliasTables.size()>0){
        	aliasTable=(AliasTable)aliasTables.get(0);
        	if ( whereClause.size() ==0 ) sql.append(" WHERE ("+aliasTable.getCondition()+")" );
            else sql.append(" AND ("+aliasTable.getCondition()+")" );
        	for (int i=1;i< aliasTables.size();i++) {
        		aliasTable=(AliasTable)aliasTables.get(i);
        		sql.append(" AND (" +aliasTable.getCondition()+")" );
        	}
        }
        // ############ added above

        if(orderbyClause !=null)
            sql.append(" ORDER BY "+ orderbyClause);
        
        //return "SELECT "+ outSelect+" FROM ( SELECT ROWNUM row_num, "+ outSelect+" FROM ( "+
        //        sql+ " )) WHERE row_num BETWEEN "+(getStartRowIndex()+1)+" AND "+(getStartRowIndex()+getRange());
          return "SELECT "+ outSelect+" FROM ( SELECT ROWNUM row_num, "+ outSelect+" FROM ( "+
          this.replaceVariables(sql.toString())+ " ) WHERE ROWNUM <= "+ (getStartRowIndex()+getRange()) + " ) WHERE row_num>="+ (getStartRowIndex()+1);

    }
    /**
     * ���maintable �����ֶ� getSumMethod !=null, ������ѯ��ʱ��Ϳ��Ե��ñ�����
     * ����ڲ�ѯ���ȫ��Χ��ͳ�ƣ���ҳͳ����QueryResult�л��)
     * ��Ȼ���ڵ��ñ�����ǰ�������ͨ��#isFullRangeSubTotalEnabled()���ж��Ƿ���Ҫ
     * @return null if not full range sql (schema decide it)
     * @throws QueryException
     */
    public String toFullRangeSubTotalSQL() throws QueryException{
        /* implementation:
           copy from toSQL()
           changed selection to columns whose sum-method is not null
           if column's sum-method is null, the selection will be null
           so the sql will has the same column count as toSQL()
        */
        if(! mainTable.isSubTotalEnabled()  ){
            // this means no subtotal item found, as defined, return null;
            return null;
        }

        StringBuffer sql=new StringBuffer("SELECT ");//+ mainTable.getName()+"."+mainTable.getPrimaryKey().getName();
        Column selCol; //boolean isFirstSelect= true;
        for( int i=0;i<selections.size();i++) {
        	if (!(selections.get(i) instanceof ColumnLink)) continue;
            ColumnLink c=(ColumnLink)selections.get(i);
            String alias;
            if(i>0 ){
                sql.append(",");
            }
            //security grade check
            if(this.securityGrade< c.getLastColumn().getSecurityGrade()){
            	sql.append("null");
            }else{
	            if( c.getColumns().length > 1) {
	                // has alias for table
	                alias=(String)rtables.get(c.getColumnLinkExcludeLastColumn());
	                if(alias ==null)
	                    throw new Error("Internal error: table alias not found for selection:"+c);
	                // toFullRangeSubTotalSQL special handling
	                selCol= c.getLastColumn();
	                if (selCol.getSubTotalMethod() !=null){
	                    sql.append( selCol.getSubTotalMethod()+ "("+
	                                alias+"."+selCol.getName()+ ")" );
	
	                }else sql.append("null");
	//                sql.append(alias+"."+c.getLastColumn().getName()+" b"+i);
	            } else {
	                selCol= c.getLastColumn();
	                if (selCol.getSubTotalMethod() !=null){
	
	                    if(selCol.isVirtual()== false) {
	                        alias=mainTable.getName();
	                        sql.append( selCol.getSubTotalMethod()+ "("+
	                                    alias+"."+selCol.getName()+ ")" );
	                    }else{
	                        sql.append( selCol.getSubTotalMethod()+ "("+
	                                    selCol.getName()+ ")" );
	                    }
	                }else sql.append("null");
	            }
            }
        }

        sql.append(" FROM ");
        appendFromMainTableClause(sql);
        Enumeration enu= rtables.keys();

        while(enu.hasMoreElements()) {
            ColumnLink clink=(ColumnLink)enu.nextElement();
            Table table= clink.getLastColumn().getReferenceTable(true);
            String alias=(String)rtables.get(clink);
            sql.append(","+ table.getRealTableName()+" "+alias);
        }
        //  ######## yfzhu added code here for handling virtual column
        ArrayList aliasTables= mainTable.getAliasTables();
        AliasTable aliasTable=null;
        if (aliasTables!=null && aliasTables.size()>0){
            for (int i=0; i< aliasTables.size(); i++){
                aliasTable=(AliasTable)aliasTables.get(i);
                sql.append(","+ aliasTable.getRealTableName()+" "+ aliasTable.getName());
            }
        }
        // ############ added above

        if( whereClause.size()>0) {
            sql.append(" WHERE ("+whereClause.get(0)+")") ;
            for(int i=1;i< whereClause.size();i++) {
                sql.append(" AND ("+whereClause.get(i)+")" );
            }
        }
        //  ######## yfzhu added code here for handling virtual column
        if ( aliasTables !=null && aliasTables.size()>0){
            aliasTable=(AliasTable)aliasTables.get(0);
            if ( whereClause.size()==0 ) sql.append(" WHERE ("+aliasTable.getCondition()+")" );
            else sql.append(" AND ("+aliasTable.getCondition()+")" );
            for (int i=1;i< aliasTables.size();i++) {
                aliasTable=(AliasTable)aliasTables.get(i);
                sql.append(" AND (" +aliasTable.getCondition()+")" );
            }
        }
        // ############ added above

        if(orderbyClause !=null)
            sql.append(" ORDER BY "+ orderbyClause);
        return this.replaceVariables(sql.toString());
    }

    /**
     * ��QueryRequest�����ʱ�����Ҫ�����ȫ��Χ��ͳ�ƣ���QueryRequestImpl����
     * enableFullRangeSubTotal()������
     * @return
     */
    public boolean isFullRangeSubTotalEnabled(){
        return fullRangeSubTotalEnabled;
    }

    public void enableFullRangeSubTotal(boolean  b){
        fullRangeSubTotalEnabled=b;
    }
}
