
package nds.web.interpreter;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.control.web.AjaxController;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.query.QueryResult;
import nds.schema.Column;
import nds.schema.Filter;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.*;

/**
 * <a href="/Goods/GoodsManage?type=0&amp;cateID=-3214"
 * class="operateBtn">查看商品</a>
 * 
 * <a href="javascript:void(0)" class="operateBtn" onclick="editCate(-3214,'parentRow')">编辑</a> 
 * <a href="javascript:void(0)" class="operateBtn" onclick="deleteCate(-3214)">删除</a>
 * <a class="operateBtn board" id="board-3214" dataid="board-3214" href="javascript:void(0)" data=
 * "http://localhost/nds/oto/webapp/product.vml?openid=OPENID&type=1&relateID=-3214&goodTypeID=0"
 * onclick="toClipboard('board-3214','http://weixin.kun-hong.com/wap/CategoryList/421?openid=OPENID&amp;type=1&amp;relateID=-3214&amp;goodTypeID=0')"
 * title="复制链接">复制链接</a> <br>
 * <a class="operateBtn" href="/weixin/AnswerByKeyword?source=14">设置微信回复关键词</a>
 * <a class="operateBtn" href="/TemplateManager/index?source=14">设置微网站栏目</a>
 * {colforacs=[{},{},{},{}]}
 * example {}={des:"编辑",attr:"href=\"javascript:void(0)\" class=\"operateBtn\" onclick=\"editCate(-3214,'parentRow')\"}
 * value=1;43005 objectid;columnid
 * action 增加script 节点
 * refcol 增加 关联字段值
 */
 
public class AcionColnterpreter implements ColumnInterpreter,java.io.Serializable {
	private static Logger logger= LoggerManager.getInstance().getLogger(AcionColnterpreter.class.getName());	
    public AcionColnterpreter() {}
    /** Add <a href=''></a> to the value
     *  Set max display length to 60 (yfzhu 2005-05-07)
     * @throws ColumnInterpretException if input value is not valid
     */
    private String value;
  	private Locale locale;
  	private int objid;
  	private int colid;
  	private String refcolval="";
  	private Table table;
    
  	public String parseValue(Object value,Locale locale) {
  		StringBuffer ret=new StringBuffer();
    	if(value==null) return "";
    	String[] vals=((String) value).split("@");
    	//System.out..print("vals.size ->"+vals.length);
    	if(vals.length==2){
    		this.objid=Integer.parseInt(vals[0]);
    		this.colid=Integer.parseInt(vals[1]);
    		Column col=TableManager.getInstance().getColumn(this.colid);
    		table=col.getTable();
    		JSONObject jor=col.getJSONProps();
    		//System.out..print("objid ->"+this.objid+"colid ->"+this.colid);
    		JSONArray aclist=jor.optJSONArray("colforacs");
    		//System.out..print("aclist ->"+aclist.toString());
    	     if (aclist != null) for (int i = 0; i < aclist.length(); i++) {
    	         JSONObject jae = aclist.optJSONObject(i);
    	         String des=jae.optString("des");
    	         String attr=jae.optString("attr");
    	         Boolean isbr=jae.optBoolean("isbr");
    	         String action=jae.optString("action");
    	         int refcol=jae.optInt("refcol",-1);
    	         if(refcol>0){
    	        	 logger.debug("table ->"+table.getName()+" refcol->"+refcol);
    	        	 getrefColval(refcol);
    	         }
    	         ret.append(constrHtml(des,attr,locale,isbr));
    	         if(action!=null)ret.append("<script>"+replaceVariable(action)+"</script>");
    	     }
    		return ret.toString();
    	}else{
    		return "";
    	}
    }
  	//refcol get val
  	//根据关联字段获取关联字段的值
  	private void getrefColval(int refcol){
  		QueryResult result=null;
  		try{
  		QueryRequestImpl query=QueryEngine.getInstance().createRequest(null);
  		query.setMainTable(table.getId());
  		query.addSelection(table.getColumn(refcol).getId());
  		query.addParam(table.getColumn("ID").getId(),String.valueOf(this.objid));
  		result= QueryEngine.getInstance().doQuery(query);
  		
  		for (int j = 0; j < result.getRowCount(); j++) {
			result.next();	
			refcolval = result.getObject(1).toString();
  		}
  			//return refcolval;
  			
  		}catch(Exception e){
  			logger.debug("get refcolval error!");
  		}
  		
  	}
  	
  	//根据配置构造界面的html动作
    private String constrHtml(String des, String attr, Locale local,Boolean isbr) {
		// TODO Auto-generated method stub
    	attr=replaceVariable(attr);
    	String pa="";
    	if(isbr){
    	  pa="<a "+attr+">" + des + "</a><br>";
    	}else{
    	  pa="<a "+attr+">" + des + "</a>";
    	}
		return pa;
	}
    
    
    //替代变量
    
    private String replaceVariable(String attr){
    	attr=replace(attr,"$objectid$",String.valueOf(this.objid));
    	attr=replace(attr,"$colid$",String.valueOf(this.colid));
    	attr=replace(attr,"$refcolval$",String.valueOf(this.refcolval));
    	return attr;
    }
    

    /**
     * Efficient string replace function. Replaces instances of the substring
     * find with replace in the string subject. karl@xk72.com
     * 
     * @param subject
     *            The string to search for and replace in.
     * @param find
     *            The substring to search for.
     * @param replace
     *            The string to replace instances of the string find with.
     */
    public static String replace(String subject, String find, String replace) {
        StringBuffer buf = new StringBuffer();
        int l = find.length();
        int s = 0;
        int i = subject.indexOf(find);
        //System.out..print("find ->"+find+" pos->"+i);
        while (i != -1) {
            buf.append(subject.substring(s, i));
            buf.append(replace);
            s = i + l;
            i = subject.indexOf(find, s);
        }
        buf.append(subject.substring(s));
        return buf.toString();
    }
    
	/**
    * Just the str
    */
    public Object getValue(String str,Locale locale) {
        return str;
    }
	@Override
	public String changeValue(String str, Locale locale)
			throws ColumnInterpretException {
		// TODO Auto-generated method stub
		return null;
	}
}
