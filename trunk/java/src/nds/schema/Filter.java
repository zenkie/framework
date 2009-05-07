package nds.schema;
import org.json.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import nds.util.*;
import nds.util.xml.XmlMapper;
import nds.control.ejb.AsyncControllerBean;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
/**
 * 过滤器对象
 * 
 * 过滤器字段存储的是一个过滤器的定义，数据库或程序将此过滤器应用于业务数据，用于构造一个数据集合。典型用例：
工作流定义时，需要选择进入工作流程必须满足的条件
配货时，在配货单上定义需要配货的商品的范围。
原理上，过滤器设置将以XML（clob）结构保存到数据库的指定字段，通过Interpreter完成界面构造，通过REGEXPRESSION 存放关于filter 构造时需要的额外参数，如AD_TABLE_ID 等。
过滤器字段的内容只能通过按钮设置，形式与 file类型的字段类似
存储结构
filter
filter/desc
filter/sql
filter/expr
desc: 过滤器描述，将显示在界面上
sql:为ID 满足的条件，如 select m_product.id from m_product where M_PRODUCT.ISACTIVE=’Y’
expr，封装的Expression 表达式
样例：
<filter>
	<desc>产品包含(1,2,3)</desc>
	<sql> select m_product.id from m_product where id IN (1,2,3)</sql>
	<expr>
<![CDATA[<expr>
    <expr>
        <clink>C_V_PO_ORDER.TOT_SUM</clink>
        <condition>&gt;1000000</condition>
    </expr>
    <oper>1</oper>
    <expr>
        <clink>C_V_PO_ORDER.ISACTIVE</clink>
        <condition>=Y</condition>
    </expr>
</expr>
]]>
</expr>
</filter>

 * 2008-12-18 initiated
 * @author yfzhu
 * @since 4.0
 * 
 */
public class Filter implements java.io.Serializable {
    private static Logger logger= LoggerManager.getInstance().getLogger(Filter.class.getName());
    private static ThreadLocal lxh=new ThreadLocal(){
    	protected synchronized Object initialValue() {
    		XmlMapper xh=new XmlMapper();
            xh.setValidating(true);
            // By using dtdURL you brake most buildrs ( at least xerces )
            String dtdURL = "file:" ;
            xh.register("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
                        dtdURL );
            xh.addRule("filter/desc", xh.methodSetter("setDescription", 0) );
            xh.addRule("filter/sql", xh.methodSetter("setSql", 0) );
            xh.addRule("filter/expr", xh.methodSetter("setExpression", 0) );

    		return xh;
        }
    };	
	private String description;
	private String sql;
	private String expression;
	public Filter(){}
	/**
	 * 如果希望处理xml发生异常时能抛出错误，应使用 #parse 方法，本构造方法不会抛出异常
	 * @param xml
	 */
	public Filter(String xml) {
		if(nds.util.Validator.isNotNull(xml)){
		try{
            // if from html, which will be wrappered by cdata
            if (xml.startsWith("<![CDATA[") && xml.endsWith( "]]>")){
                xml=  xml.substring(9, xml.length() -3);
            }
            xml ="<?xml version=\"1.0\" encoding=\"GB2312\"?>"+xml;
            byte[] bs=xml.getBytes();
            ByteArrayInputStream bis=new ByteArrayInputStream(bs );
            this.loadMapping(bis);
        }catch(Exception e){
            logger.error("无法解析字符串为Filter对象:"+ xml, e);
            this.description="无法解析过滤器设置，请重新设置";
            this.sql="IS NULL";
            this.expression="";
            //throw new QueryException("无法解析字符串为Filter对象:"+ xml, e);
        }
		}
/*		if(nds.util.Validator.isNotNull(xml)){
		try{
			JSONObject jo= org.json.XML.toJSONObject(xml);
			description= jo.getString("desc");
			sql=jo.getString("sql");
			expression= jo.getString("expr");
			
		}catch(Throwable t){
			logger.error("Fail to parse xml as Filter:"+ xml, t);
			throw new NDSException("Fail to parse xml as Filter",t);
		}
		}*/
	}
	/**
	 * 与构造方法不同的是，此方法在解析失败时会直接抛出异常
	 * @param xml
	 * @throws NDSException
	 */
	public void parse(String xml)throws NDSException{
		if(nds.util.Validator.isNotNull(xml)){
			try{
	            // if from html, which will be wrappered by cdata
	            if (xml.startsWith("<![CDATA[") && xml.endsWith( "]]>")){
	                xml=  xml.substring(9, xml.length() -3);
	            }
	            xml ="<?xml version=\"1.0\" encoding=\"GB2312\"?>"+xml;
	            byte[] bs=xml.getBytes();
	            ByteArrayInputStream bis=new ByteArrayInputStream(bs );
	            this.loadMapping(bis);
	        }catch(Exception e){
	            logger.error("无法解析字符串为Filter对象:"+ xml, e);
	            throw new QueryException("无法解析字符串为Filter对象:"+ xml, e);
	        }
		}
	}
	 private void loadMapping(InputStream stream) throws Exception {
	        String dtdURL = "file:" ;
	        XmlMapper xh= (XmlMapper)lxh.get();
	        xh.readXml(stream, this);
	    }	
	public String toXML() throws org.json.JSONException{
		StringBuffer sb=new StringBuffer("<filter><desc>");
		sb.append(description==null?"":StringUtils.escapeForXML(description)).append("</desc><sql>");
		sb.append(sql==null?"":StringUtils.escapeForXML(sql)).append("</sql><expr>");
		sb.append(expression==null?"":StringUtils.escapeForXML(expression)).append("</expr></filter>");
		
		return sb.toString();
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Expression getExprObject() throws nds.query.QueryException{
		return new Expression(expression);
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	
}
