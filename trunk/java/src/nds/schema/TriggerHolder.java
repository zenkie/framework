package nds.schema;

import java.util.*;
import java.util.regex.*;

import nds.util.*;
/**
 * 表的AC/AM/BD的方法的容器
 * AC/AM/BD 方法支持2个接口版本，
     * ver 1, with only 1 param, that is objectid (int)
     * ver 2, with 3 params, as following:
      		PROC_NAME ( id in number, p_code out number, p_message out varchar2)
			其中 p_code:
			代码描述（与单对象界面按钮/菜单项的处理方式一致）
			0 不刷新
			1 刷新当前对象页
			2 关闭当前对象窗口
			3 尝试刷新当前界面的明细标签页，如果失败（如：不存在明细标签页），刷新当前页面
			4 以p_message内容作为新的URL，按URL目标页定义替换当前页面的DIV或者构造HREF
			5 以p_message内容作为新的JAVASCRIPT, 解析并执行
			99 关闭当前窗口

 * @version 4.0
 */
public class TriggerHolder {
	/**
	 * 版本化的trigger定义
	 * @author yfzhu
	 *
	 */
	public class VersionedTrigger {
		private String name;
		private int version;
		/**
		 * Default to version 1
		 * @param name trigger name
		 */
		public VersionedTrigger(String name){
			this.name=name;
			this.version=1;
		}
		/**
		 * 
		 * @param name trigger name
		 * @param ver version code
		 */
		public  VersionedTrigger(String name, int ver){
			this.name=name;
			this.version=ver;
		}
		
		public String getName(){return name;}
		public int getVersion(){return version;};
		public String toString(){
			return name+":"+ version;
		}
	}	
	/**
	 * key: "AC"/"AM"/"BD", value: VersionedTrigger  
	 */
	private HashMap<String, VersionedTrigger> triggers=new HashMap();
	private Pattern ptn;
    public TriggerHolder() {
    	ptn=Pattern.compile("[ ,;:]");
    }
    /**
     * @param t in format like "m_product:2" or just "m_product" the :2 signs for interface of version 2 
     * @param defaultName
     * @return
     */
    private VersionedTrigger parseTrigger(String t, String defaultName){
    	if ( Validator.isNull(t)) t=defaultName;
    	t= t.trim();
    	String[] s=ptn.split(t);
    	VersionedTrigger vt;
    	if(s.length>1) vt=new VersionedTrigger(s[0], Tools.getInt(s[1], 1));
    	else vt=new VersionedTrigger(s[0]);// default to v1
    	
    	return vt;
    }
    void setAfterCreate(String t, String defaultName){
    	
        triggers.put("AC", parseTrigger(t,defaultName));
   }
    void setBeforeModify(String t, String defaultName){
        triggers.put("BM", parseTrigger(t,defaultName));
    }
    void setAfterModify(String t, String defaultName){
         triggers.put("AM", parseTrigger(t,defaultName));
    }
    void setBeforeDelete(String t, String defaultName){
        triggers.put("BD", parseTrigger(t,defaultName));
    }
    /**
     * Get trigger definition accroding to event 
     * @param event currently only support "AC","AM","BD"
     * @return
     */
    public VersionedTrigger getTrigger(String event){
    	return triggers.get(event);
    }
    
}