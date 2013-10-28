
package nds.taglibs.input;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.portlet.util.PortletUtils;
import nds.util.*;

import java.util.*;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.query.*;
import nds.schema.*;

/**
 *	用于构建Filter 对象的输入框
 * 
 * 输出样式：
<input type=’hidden’ name=”column_1232” id=”column_1232” value=””>
<input class='ipt-3-2' type=’text’ readonly id=”column_1232_fd” value=”公司=1产品包含(1,2,3)”> （1行）或
<textarea class='ipt-3-2' id=”column_1232_fd” READONLY>公司=1产品包含(1,2,3)</textarea>
<span onclick="dq.toggle('/html/nds/query/filterobj.jsp?
table=12710&amp;return_type=s&amp;column=23994&amp;accepter_id=column_23994&amp;
qdata='+encodeURIComponent(document.getElementById('column_23994').value),'column_23994')" 
id="cbt_23994" class="coolButton"><img width="16" height="16" border="0" align="absmiddle" 
title="Find" src="/html/nds/images/filter.gif"/></span>
  @see nds.schema.Filter 
 */

public class Filter extends TagSupport {
	private static Logger logger= LoggerManager.getInstance().getLogger((Filter.class.getName()));
    
    private String name; // name of the text field

    private Map attributes; // attributes of the <input> element

    private String id; // bean id to get default values from
    
    private String desc; // description for filter
    private String xml;  // xml string
    private String attributesText; // attributes of the <input> element as text
    private String columnId;// id of the filter column
    
    public void release() {
        super.release();
        id = null;
        name = null;
        attributes = null;
        attributesText=null;
    }

    public int doStartTag() throws JspException {
        try {
        	
            // get what we need from the page
            ServletRequest req = new nds.control.web.NDSServletRequest( pageContext.getRequest());
            JspWriter out = pageContext.getOut();
            
            // hidden input
            out.print("<input type='hidden' id='"+ Util.quote(id)+"' "); // fd: filter description
            out.print("name=\"" + Util.quote(name) + "\" value='");
            if(nds.util.Validator.isNotNull(xml))out.print(Util.quote(xml));
        	out.print("'>");
        	
        	int colId= Integer.parseInt(columnId);
        	Column column= TableManager.getInstance().getColumn(colId);
        	
        	
            // from input
        	boolean isTextArea= column.getDisplaySetting().getRows()>1;
        	
        	if(isTextArea)out.print("<textarea readonly wrap='soft' ");
        	else out.print("<input type='text' readonly ");
        	out.print("id='"+ Util.quote(id)+"_fd' ");
            if(isTextArea){
                out.print("cols=\"" + (column.getDisplaySetting().getCharsPerRow()) + "\" ");
                out.print("rows=\"" + (column.getDisplaySetting().getRows()) + "\" ");
            }
//        	 include any attributes we've got here
            Util.printAttributes(out, attributes);
            if (attributesText != null) {
                out.print(attributesText + " ");
            }
            if(!isTextArea){
            	out.print("value=\""+(nds.util.Validator.isNotNull(desc)?Util.quote(desc):"")+"\">" );
            }else{
            	out.print(">"+(nds.util.Validator.isNotNull(desc)?Util.quote(desc):"")+"</textarea>" );
            }
            /**
             * Add 2010-3-15 to support filter for xml type column
             */
            Column searchOnColumn=column;
            
            String options=null;  
            
            //2010-3-15 added support for wildcardfilter
            if(searchOnColumn.isFilteredByWildcard()){
				int tableId= searchOnColumn.getTable().getId();
				JSONObject jo=new JSONObject();
				org.json.JSONArray ja=new JSONArray();
				org.json.JSONArray jap=new JSONArray();
				
				jo.put("ta", true);// currently we only support titlt area wildcard filter columns
				
				//{ta:true|false, rc:[<columnId>,<columnId>...], prc:[<columnId>,<columnId>...]}
				List al=searchOnColumn.getReferenceColumnsInWildcardFilter();
				for(int i=0;i< al.size();i++){
					Column col=(Column)al.get(i);
					if(col.getTable().getId()== tableId)ja.put(col.getId());
					else jap.put(col.getId());
				}
				jo.put("rc", ja);
				jo.put("prc",jap);
				options= jo.toString().replaceAll("\"", "&quot;");
			}   
            //System.out.println("searchOnColumn="+searchOnColumn+",isFilteredByWildcard="+searchOnColumn.isFilteredByWildcard()+", options="+ options);
            /**
包含以下信息：
“type”: 当前xml 的用途，目前仅支持 filter 类型
“tablesrc”: 表的来源，"F"  固定值，从 “table” 参数上获取；"V"  变化值，从”tableinput” 指明的input 的输入项目获取; 缺省为“F”
“table”: 可选项，过滤器针对的表的name，如m_product。此用法在业务表上设定时使用。
“tableinput”: 可选项，且与”table” 两者有且只能有一个。指明ad_table_id 的输入字段，如”ad_filter.ad_table_id”, 此字段只能来源于当前字段所在表，故可省略表名，系统将根据”column_{id}” 的规则从界面上获取字段的设定值，作为table的name。此用法主要在工作流定义的设定上使用。
“filter”: 可选项，搜索时的过滤条件，将写在where 条件之后，注意所有的字段都必须写 table的名称做前缀。
示例：
{type: “filter”, table: “m_product”, filter: “M_PRODUCT.ISACTIVE=’Y’ AND M_PRODUCT.TYPE IN (‘A’, ‘B’)”}
或
{type: filter, tablesrc:V, tableinput:ad_table_id}
             */
            Properties props= column.getProperties();
            if(props==null)props=new Properties();
            String tableSrc= props.getProperty("tablesrc", "F");
            String table;
            String temp_table;
            String checkScript;
            if("V".equalsIgnoreCase(tableSrc)){
            	//变化值 从”tableinput” 指明的input 的输入项目获取
            	String tableInput= props.getProperty("tableinput");
            	if(Validator.isNull(tableInput)){
            		throw new NDSException("Not find tableinput in column regexpression "+ column + " as tablesrc set 'V'");
            	}
            	Column c= column.getTable().getColumn(tableInput);
            	if(c==null)throw new NDSException("Not find column in table "+ column.getTable() + " with name:"+tableInput);
            	
            	table="encodeURIComponent(dwr.util.getValue('column_"+ c.getId()+"'))";
            	temp_table="";
            	checkScript="if(oq.isEmpty('column_"+ c.getId()+"')==true){alert('"+PortletUtils.getMessage(pageContext,"input-first")+":"+c.getDescription(req.getLocale())+"');return;}";
            }else{
            	table= props.getProperty("table");
            	if(table==null) throw new NDSException("Not find table in column regexpression "+ column +" as tablesrc set 'F'");
            	temp_table=table;
            	table="'"+ table+"'";
            	checkScript="";
            }
            String action="'/html/nds/query/search.jsp?table='+"+ table+"+'&return_type=f&column="+columnId+"&accepter_id="+ id +"'";
            String imageurl="";
            String popflag="";
            String toggle;
            Table toggle_table=null;
            if(temp_table.equals("")){
            	toggle="oq.toggle_m(";
            }else{
            	toggle_table=TableManager.getInstance().getTable(temp_table);
            	toggle = toggle_table.isDropdown() ? "oq.toggle(": "oq.toggle_m(";  
            }
            imageurl=nds.util.Validator.isNotNull(desc)?"clear.gif":"filterobj.gif";
            popflag=nds.util.Validator.isNotNull(desc)?"clear":"popup";         
			out.print("<span class='coolButton' id=\""+Util.quote(id)+"_link\" title="+popflag+" onclick=\""+checkScript+toggle+ action+", '"+this.id+"'"+(options==null?"":","+options)+");\"><img id='"+this.id+"_img' border=0 width=16 height=16 align=absmiddle src='/html/nds/images/"+imageurl+"'></span>");
            out.print("<script>createButton(document.getElementById('"+ Util.quote(id) +"_link'));</script>");           
        } catch (Exception ex) {
            throw new JspTagException(ex.getMessage());
        }
        return SKIP_BODY;
    }
    
    
    public void setName(String x) {
        name = x;
    }
    public String getName() {
        return name;
    }    
    public Map getAttributes() {
        return attributes;
    }
    public void setAttributes(Map x) {
        attributes = x;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}
	
    /**
     * Getter for property attributesText.
     * 
     * @return Value of property attributesText.
     */
    public String getAttributesText() {
        return attributesText;
    }
    public void setAttributesText(String x) {
        attributesText = x;
    }

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}


}