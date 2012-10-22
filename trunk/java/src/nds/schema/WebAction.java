package nds.schema;
import java.util.*;
/**
 * 
 * 界面动作定义，扩展的类可以实现为: 菜单，按钮，树节点等。动作的具体实现可以是超链，javascript，
 * 存储过程，ad_process，BeanShell 脚本，操作系统脚本命令等。通过界面动作定义，可以超越ad_table界面局限，
 * 实现形式多样的功能集成
 * 
 * @author yfzhu
 *
 */
public interface WebAction {
	/**
	 * Order in display
	 * @return
	 */
	public int getOrder();
	/**
	 * Check whether this action can display in specified session or not.
	 * @param env contains web environment value pair, mainly "httpservletrequest", "connection",
	 * note param name is recommended to be lower case.  
	 * @return true if can be displayed
	 * @throws exception when error found
	 */
	public boolean canDisplay(Map env)throws Exception;
	/**
	 * Will create html code for displaying. Using canDisplay to check whether
	 * this action can display in specified session or not before this. 
	 * @param locale
	 * @param env same as canDisplay
	 * @return html code
	 */
	public String toHTML(Locale locale,Map env);
	/**
	 * This can be url, ad_process.name, beashell script, os command, and so on
	 * the content comes from ad_action.content, ad_action.scripts in order
	 * @return
	 */
	public String getScript();
	
	/**
	 * Execute scripts defined by script including BeanShell, stored procedure, and OS command
	 * @param params contains environment parameters, such as operator, connection.
	 * @return at least contains "code" (String of Integer), message (String), some script may
	 * 	return more information
	 */
	public Map execute(Map params) throws Exception;
	
	public int getId();
	
	public ActionTypeEnum getActionType();
	public String getComments();
	public String getDescription() ;
	public DisplayTypeEnum getDisplayType();
	public String getFilter() ;
	public String getIconURL() ;
	public String getName() ;
	
	public SaveObjectEnum getSaveObjType();
	public int getSubSystemId();
	public int getAcordionId();
	public int getTableCategoryId();
	public int getTableId() ;
	public String getUrlTarget();
	
	public enum SaveObjectEnum {
	    ASK("ask"), FORCE("force"), NONE("none");
	    private String saveObjType;

	    private SaveObjectEnum(String saveobj) {
	    	this.saveObjType = saveobj;
	    }

	     public String getType() {
	    	 return saveObjType;
	    }
	    public static SaveObjectEnum parse(String type){
	    	if("ask".equals(type)) return ASK;
	    	else if("force".equals(type)) return FORCE;
	    	else 
	    		 return NONE;
	    	//else throw new nds.util.NDSRuntimeException("type "+ type +" is invalid as SaveObjectEnum");
	    }
	};
	public enum DisplayTypeEnum{
		ListButton("listbutton",0), ListMenuItem("listmenuitem",1), ObjButton("objbutton",2),
		ObjMenuItem("objmenuitem",3),TabButton("tabbutton",4),
		TreeNode("treenode",99)
		;
	    private String dpt;
	    private int idx;
	    private DisplayTypeEnum(String d, int idx) {
	    	this.dpt = d;
	    	this.idx=idx;
	    }
	    public int getIndex(){
	    	return idx;
	    }
	    public String getType() {
	    	 return dpt;
	    }
	    public static DisplayTypeEnum parse(String type){
	    	if("listbutton".equals(type)) return ListButton;
	    	else if("listmenuitem".equals(type)) return ListMenuItem;
	    	else if("objbutton".equals(type)) return ObjButton;
	    	else if("objmenuitem".equals(type)) return ObjMenuItem;
	    	else if("tabbutton".equals(type)) return TabButton;
	    	else if("treenode".equals(type)) return TreeNode;
	    	else throw new nds.util.NDSRuntimeException("type "+ type +" is invalid as DisplayTypeEnum");
	    }	    
	};
	public enum ActionTypeEnum{
		URL("url"), StoredProcedure("sp"), AdProcess("adproc"),
		JavaScript("js"),BeanShell("bsh"), OSShell("shell"),Python("py");
		;
	    private String dpt;

	    private ActionTypeEnum(String d) {
	    	this.dpt = d;
	    }
	    public String getType() {
	    	return dpt;
	    }
	    public static ActionTypeEnum parse(String type){
	    	if("url".equals(type)) return URL;
	    	else if("sp".equals(type)) return StoredProcedure;
	    	else if("adproc".equals(type)) return AdProcess;
	    	else if("js".equals(type)) return JavaScript;
	    	else if("bsh".equals(type)) return BeanShell;
	    	else if("shell".equals(type)) return OSShell;
	    	else if("py".equals(type)) return Python;
	    	else throw new nds.util.NDSRuntimeException("type "+ type +" is invalid as ActionTypeEnum");
	    }	    
	};
}
 