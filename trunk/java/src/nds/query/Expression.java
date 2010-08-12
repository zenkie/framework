package nds.query;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.*;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.TableManager;
import nds.schema.*;
import nds.util.PairTable;
import nds.util.StringBufferWriter;
import nds.util.StringUtils;
import nds.util.Validator;
import nds.util.xml.XmlMapper;

/**
 * 保存复杂的查询条件
 * 2008-03-10 find bug when multiple theads call this function since xh is shared among them
 * Changed xh to ThreadLocal variable
 */
public class Expression implements SQLCombination, Serializable{
    private static Logger logger= LoggerManager.getInstance().getLogger((Expression.class.getName()));
    //private static XmlMapper xh; // xml mapper
    private static ThreadLocal lxh=new ThreadLocal(){
    	protected synchronized Object initialValue() {
    		XmlMapper xh=new XmlMapper();
            xh.setValidating(true);
            // By using dtdURL you brake most buildrs ( at least xerces )
            String dtdURL = "file:" ;
            xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
                        dtdURL );
            xh.addRule("expr/desc", xh.methodSetter("setDescription", 0) );
            xh.addRule("expr/clink", xh.methodSetter("setColumnLink", 0) );
            xh.addRule("expr/condition", xh.methodSetter("setCondition", 0) );
            xh.addRule("expr/oper", xh.methodSetter("setOperator", 0) );
            xh.addRule("expr/expr", xh.objectCreate("nds.query.Expression") );
            xh.addRule("expr/expr", xh.addChild("addExpression", null) );

    		return xh;
        }
    };
    private Expression exprLeft, exprRight;
    private boolean isLeaf;

    private int operator; /*SQLCombination.SQL_AND,OR,NOT*/
    /**
     * the higher the value, the complex the expression is.
     * It's the count of children expression
     */
    private int complexLevel ;
    /*-- folllowing variables are used when this expression is a leaf */
    private ColumnLink clink;
    private String condition;
    private String desc;
    public  static final Expression EMPTY_EXPRESSION=new Expression(null, null, null);

    /*-- special exists clause like 
     * exists ( select 1 from c_order where c_order.id=MainTable.Id and c_order.status=2) 
     * in this case , clink will be null
     */
    
    public Expression(Expression left, Expression right, int oper, String desc) {
        exprLeft=left;
        exprRight=right;
        operator= oper;
        this.desc = desc;
        isLeaf=false;
        complexLevel= left.complexLevel + right.complexLevel;
    }
    /**
     * If cl's last column is uppercase, then will convert <param>condition</param> to
     * upper case 
     * @param cl
     * @param condition
     * @param desc
     */
    public Expression(ColumnLink cl, String condition, String desc){
    	/**
    	 * robin 2010-08-10 当condition中含有","时，解析为多选条件
    	 * 及每个","隔开的都是或者关系
    	 */
    	if(null!=condition&&condition.contains(",")){
    		String[] cons=condition.split(",");
    		List<String> conls=new ArrayList<String>();
    		for(String s:cons){
    			if(!s.equals("")){
    				conls.add(s);
    			}
    		}
    		Expression eprs=null;
    		for(String s1:conls){
    			if(null==eprs){
    				eprs=new Expression(cl,s1,desc);
    			}else{
    				eprs=new Expression(eprs,new Expression(cl,s1,desc),SQLCombination.SQL_OR,desc);
    			}
    		}
    		if(eprs.isLeaf()){
    			this.clink=eprs.getColumnLink();
    			this.condition=eprs.getCondition();
    		}
    		this.desc=eprs.getDescription();
    		this.exprLeft=eprs.getLeftElement();
    		this.exprRight=eprs.getRightElement();
    		this.isLeaf=eprs.isLeaf();
    		this.operator=eprs.getOperator();
    		return;
    	}
        /**
         * yfzhu 2005-05-15 发现关于LimitValue 的字段在界面上直接输入描述选项时查询会出现错误。
         * 例如：状态字段 输入"提交" 时应该由系统自动转换为2
         * 这段处理放在此处，以简化其他地方的修改。
         * 
         * 暂时发现只有quich_search 才有用户手工输入，而非选择范围的情况，这里不处理，
         * 而直接放在QueryInputHandler的相应代码处
         */
    	//condition= checkCondition(cl.getLastColumn(), condition);
    	this.clink = cl;
    	if(condition!=null && condition.indexOf("@")>0){
    		logger.warning("find @ in condition:"+ condition +" for columnlink:"+ (cl!=null?cl.toString():""));
    		Thread.dumpStack();
    	}
    	/**
    	 * 2006-7-27 
    	 */
    	if (cl!=null && cl.getLastColumn().isUpperCase()){
    		this.condition= condition.toUpperCase();
    	}else
    		this.condition=condition;
    	
        this.desc = desc;
        this.isLeaf = true;
        complexLevel=1;
        if(clink!=null && "AD_CLIENT_ID".equals(clink.getLastColumn().getName())){
        	logger.warning("find AD_CLIENT_ID:"+ condition +" ,"+ desc);
        }
    }
    
    public Expression(){
        isLeaf=true;
        complexLevel=0;
    }
    /**
     * Parse pairtable, key as column link, value as condition
     * @param pt key may be String or ColumnLink, value is String
     * @return never null
     */
    public static Expression parsePairTable(PairTable pt) throws QueryException{
    	 Expression expr=Expression.EMPTY_EXPRESSION;
    	 Object key; String value;
    	 ColumnLink clink;
    	 Expression fe;
    	 for(int i=0;i< pt.size();i++){
    	 	key=pt.getKey(i);
    	 	value=pt.getValue(i).toString();
    	 	if(Validator.isNull(value)) continue;
    	 	
    	 	if(key instanceof ColumnLink) clink=(ColumnLink)key;
    	 	else{
    	 		clink = new ColumnLink(key.toString());
    	 	}
    	 	fe=new Expression(clink, value, null);
    	 	expr=fe.combine(expr,Expression.SQL_AND,null);
    	 }    	
    	 return expr;
    }
    
    
    /**
     * yfzhu 2005-05-15 发现关于LimitValue 的字段在界面上直接输入描述选项时查询会出现错误。
     * 例如：状态字段 输入"提交" 时应该由系统自动转换为2
     * 如果发现Column.isValueLimited=true, 将设法替换其中的内容
     * 当前不处理增加了比较符的输入，即如果rawCondtion 含有除了 描述以外的符号，如"=", ">"之类
     * 将无法转换
     * @param rawCondition 形如 "未提交"，"2"等 
     * @return 重构的condition
     */
    private String checkCondition(Column col, String rawCondition,Locale locale){
    	if (rawCondition==null) return rawCondition;
    	if(col.isValueLimited()){
    		String real= 
    			TableManager.getInstance().getColumnValueByDescription(col.getId(), rawCondition.trim(),locale);
    		if(real!=null) {
    			//logger.debug("Found " + col + ":" + rawCondition + " converted to real:"+ real+ "," + StringUtils.replace(rawCondition, rawCondition.trim(), real));
    			return StringUtils.replace(rawCondition, rawCondition.trim(), real);
    		}
    	}
    	return rawCondition;
    }
    /**
     * Construct expression according to sql like string
     * @param sql, format like
     * <expr desc="东北地区的零售额大于1000门店">
     *      <expr clink="column1;column2" condition="CSA012" desc="零售额大于100"/>
     *      <expr oper="and" clink="column3;column4" condition="CDATA[>399]" desc="东北地区"/>
     * </expr>
     *
     * 或者
     * <expr>
     *      <expr clink="column1.column2" condition="CSA012"/>
     *      <expr oper="or">
     *           <expr clink=xxx codition=ccc/>
     *           <expr oper="xxx">
     *                <expr clink=xxx condition=xxx/>
     *                <expr oper="xxx" clink=xxx condition=xxx/>
     *           </expr>
     *      </expr>
     * </expr>
     */
    public Expression(String xml) throws QueryException{
    	if(nds.util.Validator.isNotNull(xml)){ 	
	        try{
	            // if from html, which will be wrappered by cdata
	            if (xml.startsWith("<![CDATA[") && xml.endsWith( "]]>")){
	                xml=  xml.substring(9, xml.length() -3);
	            }
	            xml ="<?xml version=\"1.0\" encoding=\"GBK\"?>"+xml;
	            byte[] bs=xml.getBytes();
	            ByteArrayInputStream bis=new ByteArrayInputStream(bs );
	            this.loadMapping(bis);
	        }catch(Exception e){
	            logger.error("无法解析字符串为Expression对象:"+ xml, e);
	            throw new QueryException("无法解析字符串为Expression对象:"+ xml, e);
	        }
    	}else{
    		isLeaf=true;
            complexLevel=0;    		
    	}
    }
    /**
     * Find first expression which is leaf and has column link equals to param
     * @param columnLink
     * @return condition string if not found, return null;
     */
    public String findConditionOfColumnLink(ColumnLink columnLink) throws Exception{
    	String c=null;
    	if(this.isLeaf){
    		if(columnLink.equals(clink)) c= this.condition; 
    	}else{
    		if(exprLeft!=null) c= exprLeft.findConditionOfColumnLink(columnLink); 
    		if(c==null && exprRight!=null)c= exprRight.findConditionOfColumnLink(columnLink);
    	}
    	return c;
    }
    public boolean isLeaf(){
        return this.isLeaf ;
    }
/*    public void setOperator(int oper){
        this.operator = oper;
    }*/
    public int getOperator(){
        return this.operator ;
    }
/*    public void setLeftElement(Expression e){
        this.exprLeft=e;
    }*/
    public Expression getLeftElement(){
        return this.exprLeft ;
    }
/*    public void setRightElement(Expression e){
        this.exprRight = e;
    }*/
    public Expression getRightElement(){
        return this.exprRight ;
    }

/*    public void setColumnLink(ColumnLink cl){
        this.clink=cl;
    }*/
    public ColumnLink getColumnLink(){
        if(! this.isLeaf ) throw new Error("Internal Error:Current Expression is not leaf!");
        return this.clink;
    }
    public String getDescription(){
        return desc;
    }
/*    public void setCondition(String c){
        this.condition=c;
    }*/

    /**
     * the input user condition, such as ">394", "Between 23 And 45", "like %KD"
     * @return
     */
    public String getCondition(){
        if(! this.isLeaf ) throw new Error("Internal Error:Current Expression is not leaf!");
        return this.condition;
    }
    /**
     * @return true if isLeaf() && getCondition()==null
     */
    public boolean isEmpty(){
        return (isLeaf() &&  Validator.isNull(this.getCondition()));
    }
    /**
     * Combine this expression (as left one) with another expression.
     * @param operator nds.query.SQLCombination static data, SQL_AND/SQL_OR/SQL_AND_NOT/SQL_OR_NOT
     */
    public Expression combine(Expression rightExp, int operator, String desc){
        if(rightExp==null|| rightExp.isEmpty()){
        	//logger.debug("rightExpr is empty");
        	return this;
        }
        if(this.isEmpty()) return rightExp;
        
        if(operator== SQLCombination.SQL_AND && (rightExp.complexLevel==1|| complexLevel==1)){
        	if(rightExp.complexLevel>complexLevel){
        		if(rightExp.contains(this)) return rightExp;
        	}else{
        		if(this.contains(rightExp))return this;
        	}
        }
        /*else{
        		logger.debug("---------not equal for -----"+ rightExp.complexLevel + "####"+ this.complexLevel);
				logger.debug(toString());
				logger.debug(rightExp.toString());
        	}*/
        // if operator is SQL_AND, will check all sublings if the expression already exists
    	Expression exp= new Expression(this, rightExp, operator, desc);
        return exp;
    }


    /**
    * Construct expression according to sql like string
    * @param sql, format like
    * <expr desc="东北地区的零售额大于1000门店">
    *      <expr clink="column1;column2" condition="CSA012" desc="零售额大于100"/>
    *      <expr oper="and" clink="column3.column4" condition="CDATA[>399]" desc="东北地区"/>
    * </expr>
    *
    * 或者
    * <expr>
    *      <expr>
    *           <clink>"column1.column2"</clink>
    *           <condition>"CSA012"</condition>
    *      </expr>
    *      <oper>or</oper>
    *      <expr>
    *           <expr clink=xxx codition=ccc/>
    *           <oper>xxx</oper>
    *           <expr>
    *                <expr clink=xxx condition=xxx/>
    *                <expr oper="xxx" clink=xxx condition=xxx/>
    *           </expr>
    *      </expr>
    * </expr>
    * @param escapeTime how to escape internal xml data
    */
   private void printXML(StringBuffer b, int escapeTimes){
        b.append("<expr>");
        String s="";
        if ( this.desc !=null){
            s=StringUtils.escapeForXML(desc);
            for(int i=0;i< escapeTimes-1;i++) s=StringUtils.escapeForXML(s);
            printNode(b,"desc", s);
        }
        if( this.isLeaf  ){
            if( clink !=null){
                String ck=""; int[] cki=clink.getColumnIDs();
                TableManager tm= TableManager.getInstance();
                if (cki.length> 0){
                    ck+= tm.getColumn(cki[0]).getTable().getName() +"."+ tm.getColumn(cki[0]).getName();
                }
                for( int j=1;j< cki.length;j++) {
                    ck+=";"+ tm.getColumn(cki[j]).getName() ;
                }
                printNode(b, "clink", ck);
                s=StringUtils.escapeForXML(condition);
                for(int i=0;i< escapeTimes-1;i++) s=StringUtils.escapeForXML(s);
                printNode(b, "condition", s);
            }else{
            	printNode(b, "clink", "");
            	s=StringUtils.escapeForXML(condition);
                for(int i=0;i< escapeTimes-1;i++) s=StringUtils.escapeForXML(s);
                printNode(b, "condition", s);
            }
        }else{
            exprLeft.printXML(b,escapeTimes);
            printNode(b,"oper", operator+"");
            exprRight.printXML(b,escapeTimes);
        }
        b.append("</expr>");
    }
    private void printNode(StringBuffer b, String name, Object value){
        b.append("<"+name+">"+ value+"</"+name+">");
    }
    /**
     * The whole expression will be set as value of an Input element of HTML form
     * We will wrapper it using "<![CDATA[]]>", further, since the browser will also
     * convert the internal data such as condition when equals to " &lt;399.00" to
     * "<299.00", so we should escape the internl xml twice!
     * and the condition in html form will be " &amp;lt;399.00"
     */
    public String toHTMLInputElement(){
        StringBuffer sb=new StringBuffer();
        sb.append("<![CDATA[");
        printXML(sb,2);
        sb.append("]]>");
//        logger.debug( sb.toString() );

        return sb.toString();

    }
    public String toString(){
        StringBuffer sb=new StringBuffer();
        printXML(sb,1);
//        logger.debug( sb.toString() );

        return sb.toString();
    }
    /**
     * @param cmb   “and” | “or” | “and not” | “or not”
     * @return SQL_AND,SQL_OR,SQL_AND_NOT,SQL_OR_NOT
     */
    public static int parseCombination(String cmb){
    	if("and".equalsIgnoreCase(cmb)) return SQL_AND;
    	else if("or".equalsIgnoreCase(cmb)) return SQL_OR;
    	else if("and not".equalsIgnoreCase(cmb)) return SQL_AND_NOT;
    	else if("or not".equalsIgnoreCase(cmb)) return SQL_OR_NOT;
    	else
    		throw new java.lang.IllegalArgumentException(cmb);
    }
    /*-------- following is for xml ------------ */

    public void setDescription(String d){
        this.desc =StringUtils.unescapeFromXML(d);
    }
    /**
     * @param k format: table1.id1;id2;id3
     *
     */
    public void setColumnLink(String k) throws QueryException{
        // allow for exists clause, which has no column link
    	if (Validator.isNull(k)) return;
    	try{
        	StringTokenizer st=new StringTokenizer(k,";");
        ArrayList al=new ArrayList();
        while( st.hasMoreTokens() ){
            al.add(st.nextToken() );
        }
        String[] ids=new String[ al.size() ];

        for(int i=0;i< ids.length;i++) {
            ids[i]=(String)al.get(i);
        }
        clink= new ColumnLink(ids);
        this.isLeaf = true;
        }catch(Exception e){
            logger.error("Error set column link :"+ k, e);
            throw new QueryException("配置错误："+ k, e);
        }
    }

    public void setCondition(String c){
        this.condition = StringUtils.unescapeFromXML(c);
        this.isLeaf = true;
        this.complexLevel=1;
    }
    public void setOperator(String o){
        this.operator= (new Integer(o)).intValue();
        this.isLeaf = false;
    }
    public void addExpression(Expression e){
        if( exprLeft==null) exprLeft= e;
        else exprRight=e;
        this.isLeaf = false;
        this.complexLevel = this.complexLevel+  e.complexLevel;
    }
    /**
     * @return if isLeaf()==true, it will be columnlink ids + condition.hashCode
     * else, it will be -1
     */
    public int hashCode(){
    	int code=-1;
    	if( isLeaf){
    		code= this.clink.hashCode() + this.condition.hashCode();
    	}
    	return code; 
    }
    /**
     * Every node must be equal
     */
    public boolean equals(Object o){
    	if(o==null || !(o instanceof Expression)) return false;
    	Expression e= (Expression)o;
    	if(isEmpty()) return e.isEmpty();
    	else{
    		if (e.isEmpty()) return false;
    	}
    	// so the two are not empty
    	if(isLeaf()){
    		if(e.isLeaf() ){
    			return 
					(
							(this.clink==null && e.clink==null) || 
							(this.clink!=null && e.clink!=null &&  this.clink.hashCode()== e.clink.hashCode() && this.clink.equals(e.clink))
					) && this.condition.equals(e.condition);
    		}else return false;
    	}else{
    		if(e.isLeaf()) return false;
    		// both are not leaf
    		return e.operator==operator && ((
    			e.exprLeft.equals(exprLeft) && e.exprRight.equals(exprRight)) ||(
    					e.exprLeft.equals(exprRight) && e.exprRight.equals(exprLeft)	)
				);
    	}
    	
    }
    /**
     * Check if <param>e</param> is contained as elements of this expression
     * Only check for Expression that combined by SQL_AND of this expression' children
     * @param e 
     * @return true if e is empty or null, or current expression's sublings is equal to e;
     */
    public boolean contains(Expression e){
    	if(e==null || e.isEmpty()) return true;
    	if(this.isLeaf) return this.equals(e);
    	else{
    		if (this.equals(e)) return true;
    		// i'm compound one
    		if (operator== SQLCombination.SQL_AND ){
    			// first right then left, since right are always simple one
    			return  this.exprRight.contains(e) ||this.exprLeft.contains(e);
    		}
    		return false;
    	}
    }
    
    /**
     * Create a new expression object. Note the internal expressions are not cloned.
     */
    public Object clone(){
        Expression expr=new Expression();
        expr.clink = this.clink ;
        expr.condition= this.condition ;
        expr.desc= this.desc ;
        expr.exprLeft= this.exprLeft ;
        expr.exprRight = this.exprRight ;
        expr.isLeaf= this.isLeaf;
        expr.operator = this.operator ;
        return expr;
    }
    private void loadMapping(InputStream stream) throws Exception {
        String dtdURL = "file:" ;
        XmlMapper xh= (XmlMapper)lxh.get();
        /*if( xh==null){
            xh=new XmlMapper();
            xh.setValidating(true);
            // By using dtdURL you brake most buildrs ( at least xerces )
            xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
                        dtdURL );
            xh.addRule("expr/desc", xh.methodSetter("setDescription", 0) );
            xh.addRule("expr/clink", xh.methodSetter("setColumnLink", 0) );
            xh.addRule("expr/condition", xh.methodSetter("setCondition", 0) );
            xh.addRule("expr/oper", xh.methodSetter("setOperator", 0) );
            xh.addRule("expr/expr", xh.objectCreate("nds.query.Expression") );
            xh.addRule("expr/expr", xh.addChild("addExpression", null) );
        }*/
        xh.readXml(stream, this);
    }
    public static void main(String[] args) throws Exception{
        TableManager tm=nds.schema.TableManager.getInstance();
        if( !tm.isInitialized()){
            String tablePath= "file:///e:/aic/tables";
            String converter= "nds.schema.OracleTypeConverter";

            //String tablePath="file:/aic/tables";
            logger.debug("Initializing TableManager, using path "+tablePath);
            Properties props=new Properties();
            props.setProperty("defaultTypeConverter",converter);
            props.setProperty("directory", tablePath);
            tm.init(props);
        }
        String s="<?xml version=\"1.0\" encoding=\"GB2312\"?>";
        s +="<expr><expr>    <desc>(编号 含有 B)</desc>    <clink>Employee.DEPARTMENTID</clink>    <condition>IN (SELECT Department.ID FROM Department WHERE ( (Department.NO LIKE &apos;%B%&apos;) ))</condition></expr><oper>1</oper><expr>    <desc>(编号 含有 EDP)</desc>    <clink>Employee.DEPARTMENTID</clink>    <condition>IN (SELECT Department.ID FROM Department WHERE ( (Department.NO LIKE &apos;%EDP%&apos;) ))</condition></expr></expr>";
        Expression expr=new Expression(s);
        System.out.println(expr);

    }
}