package nds.web.action;


import nds.log.Logger;
import nds.log.LoggerManager;
import nds.monitor.MonitorManager;
import nds.monitor.ObjectActionEvent;
import nds.monitor.ObjectActionEvent.ActionType;
import nds.query.*;
import nds.schema.*;
import nds.security.User;
import nds.util.*;
import nds.control.web.*;
import nds.control.ejb.StateMachine;
import nds.control.util.*;

import org.json.*;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class WebActionImpl implements WebAction{
	protected Logger logger=LoggerManager.getInstance().getLogger(this.getClass().getName());
	public static enum DisplayFilterType{
		SQL,
		BEANSHELL,
		PYTHON
	}
	protected int id;
	protected String name;
	protected String description;
	protected String iconURL;
	/**
	 * AD_TABLE.ID
	 */
	protected int tableId=-1;
	/**
	 * AD_TABLECATEGORY.ID仅针对TreeNode
	 */
	protected int tableCategoryId=-1;
	/**
	 * ad_subsystem.id 仅针对TreeNode
	 */
	protected int subSystemId=-1;
	/**
	 * 显示条件字段，支持sql 和 beanshell, (java 的解释形脚本环境),python 三种语法
	 * sql	举例:Select count(*) from xxx where yyy
			当select 的第一个字段返回的内容大于零表示显示，小于等于零表示不显示。Sql 中可以有用户环境变量
		beanshell	举例：Xxx
			Return true | false;
			Script 可以获得的环境变量为 request, response, session等
		到底是sql 还是 beanshell，可以通过{@link #isSQLFilter} 来确认	
	 */
	protected String filter;
	/**
	 * 显示条件字段的内容是sql 还是 beanshell可以通过这里进行判定
	 */
	protected DisplayFilterType filterType;
	/**
	 * 包括url, javascript, stored procedure, ad_process, beanshell, os command 在内的都属于脚本内容
	 */
	protected String script;
	/**
	 * 针对url, 如果不是 _xxx起头，就认为是当前页面的一个div的id
	 */
	protected String urlTarget;
	/**
	 * 
	 * 是否提醒 [YESNO]， 针对Button和MenuItem
	 */
	protected boolean shouldConfirm;
	protected String comments;
	/**
	 * 在界面上排放的位置, 越小越靠前
	 */
	protected int order;
	/**
	 * 折叠菜单对应
	 */
	protected int AcordionId=-1;
	
	protected SaveObjectEnum saveObjType;
	protected ActionTypeEnum actionType;
	protected DisplayTypeEnum displayType;
	
	public WebActionImpl(){}
	public WebActionImpl(int id){
		this.id =id;
	}
	 
	/**
	 * Used for {@link #canDisplay(Map)}
	 * @param param the parameter name to look for
	 * @param env
	 * @param defaultValue 
	 * @param mustExist, will throw error if not found in env
	 * @return object in env
	 */
	protected Object getValueFromMap(String param,Map env, Object defaultValue, boolean mustExist){
		Object v= env.get(param);
		if(v==null){
			if(mustExist) throw new IllegalArgumentException(param + " not found in env :"+ Tools.toString(env));
			else v= defaultValue;
		}
		
		return v;
	}
	/**
	 * Check whether this action can display in specified session or not.
	 * @param env contains web environment, mainly "httpservletrequest", "connection" 
	 * @return true if can be displayed
	 */
	public boolean canDisplay(Map env) throws Exception{
		boolean b=false;
		if(nds.util.Validator.isNull(filter)) return true;
		String f=filter;
		HttpServletRequest request=(HttpServletRequest) getValueFromMap("httpservletrequest", env, null,true);
		UserWebImpl userWeb= (UserWebImpl)getValueFromMap("userweb", env, null,true);
		Connection conn= (Connection)getValueFromMap("connection", env, null,true);
		
		f=QueryUtils.replaceVariables(f,userWeb.getSession());
		Object ret;
		switch( filterType){
		case SQL:
			// replace environment variables
			
			int cnt= Tools.getInt(QueryEngine.getInstance().doQueryOne(f,conn), -1);
			b=(cnt>0);
			break;
		case BEANSHELL:
			ret=BshScriptUtils.evalScript(f,new StringBuffer(),false, env);
			//when null, return false
			if(ret!=null){
				if(ret instanceof Boolean) b= ((Boolean)ret).booleanValue();
				else if(ret instanceof java.lang.Number) b=((Number)ret).intValue()>0;
				else b= Tools.getBoolean(ret, false);
			}
			break;
		case PYTHON:
			ret=PythonScriptUtils.evalScript(f,new StringBuffer(),false, env);
			b=(PythonScriptUtils.convertInt(ret))>0;
		}
		return b;
	}

	/**
	 * This can be url, ad_process.name, beashell script, os command, and so on
	 * the content comes from ad_action.content, ad_action.scripts in order
	 * @return
	 */
	public String getScript(){
		return script;
	}
	
	/**
	 * Execute scripts defined by script including BeanShell, stored procedure, and OS command
	 * @param params contains environment parameters, such as operator, connection.
	 * @return at least contains "code" (String of Integer), message (String), some script may
	 * 	return more information
	 */
	public Map execute(Map params) throws Exception{
		HashMap map=new HashMap();
		switch(actionType){
		case StoredProcedure:
			
			Connection conn= (Connection)getValueFromMap("connection",params,null,true);
			Integer userId= (Integer)getValueFromMap("userid",params,null,true);
			JSONObject query= (JSONObject)getValueFromMap("query",params,null,false);
			String qs=null;
			if(query!=null){
				// recontruct query to xml format
				qs= org.json.XML.toString(query);
				logger.debug("query:"+qs);
				//int qtid= query.optInt("table", -1);
				int oid= query.optInt("id", -1);
				if(oid!=-1&&query.optBoolean("lock",true)){
					//try lock it
					QueryUtils.lockRecord(TableManager.getInstance().findTable(query.opt("table")), oid,conn);
				}

			}else
				qs="";
			//lock current record to avoid deadlock 2010-05-07 yfzhu
			ArrayList p=new ArrayList();
			p.add(userId);
			p.add(qs);
			
			SPResult ret=QueryEngine.getInstance().executeStoredProcedure(this.getScript(), p, true, conn);
			map.put("code",ret.getCode());
			map.put("message",ret.getMessage());
			break;
		case BeanShell:
			
			Object br=BshScriptUtils.evalScript(this.getScript(),new StringBuffer(),false, params);
			if(br instanceof Map){
				map.putAll((Map)br);
			}else if(br instanceof Number){
				map.put("code", ((Number)br).intValue());
			}else{
				map.put("code",0);
				map.put("message", br==null?"complete":br.toString());
			}
			break;
		case Python:
			Object py=PythonScriptUtils.evalScript(this.getScript(),new StringBuffer(),false, params);
			if(py instanceof Map){
				map.putAll((Map)py);
			}else{
				map.put("code",0);
				map.put("message", py==null?"complete":py.toString());
			}
			break;
		case OSShell:
			UserWebImpl usr= (UserWebImpl)getValueFromMap("userweb",params,null,true);
			JSONObject jo=(JSONObject)getValueFromMap("jsonobj",params,null,false);
			
			Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
			
			String logFile = conf.getProperty("dir.tmp","/tmp") + File.separator+ "ExecWebAction_"+ this.getId()+"_"+System.currentTimeMillis()+".log"; 
			CommandExecuter cmdE= new CommandExecuter(logFile);
			
			String sc=this.getScript();
			if(usr!=null) sc=QueryUtils.replaceVariables(sc,usr.getSession());
			logger.debug("for shell:"+jo);
			if(jo!=null)sc= JSONUtils.replaceVariables(sc, jo);
			
			int err=cmdE.run(sc);
			
			logger.info("User "+ usr.getUserName() + "@" + usr.getClientDomain()+" runs command :"+ this.getScript()+" with return code:"+err);
    		SysLogger.getInstance().info("sys", "exec", usr.getUserName(), "", getScript()+"("+ err+")", usr.getAdClientId());
			String message= Tools.readFile(logFile);
			//delete log file
			FileUtils.delete(logFile);
			
			map.put("code",err);
			map.put("message",message==null?"complete":message);
			break;
		default:
			throw new NDSException("Not supported action type in execute:"+ actionType.getType());
		}
		
		if ((this.displayType == WebAction.DisplayTypeEnum.ObjButton)
				|| (this.displayType == WebAction.DisplayTypeEnum.ObjMenuItem)) {

			int i = Tools.getInt(
					getValueFromMap("objectid", params, Integer.valueOf(-1),
							false), -1);
			if (i == -1) {
				JSONObject jor = (JSONObject) getValueFromMap("query",
						params, null, false);
				if (jor != null) {
					XML.toString(jor);
					logger.debug("WebAction query:"+XML.toString(jor));
					i = jor.optInt("id", -1);
				}
			}
			if (i > -1) {
				logger.debug("dispatch event for object id=" + i
						+ " of table =" + this.getTableId()
						+ " over action id=" + this.getId());
				User usr = (User) getValueFromMap("user", params, null, true);
				Connection con = (Connection) getValueFromMap("connection",
						params, null, true);
				StateMachine statemachine = (StateMachine) getValueFromMap(
						"statemachine", params, null, true);
        		   //monitor plugin
     		   JSONObject cxt=new JSONObject();
     		   cxt.put("source", this);
     		   cxt.put("connection", con);
     		   cxt.put("statemachine", statemachine);
     		   cxt.put("javax.servlet.http.HttpServletRequest", getValueFromMap("httpservletrequest", params, null, false));
     		   ObjectActionEvent oae=new ObjectActionEvent(getTableId(),
     				   i, usr.adClientId,getId(), usr, cxt);
     		   MonitorManager.getInstance().dispatchEvent(oae);

			} else {
				logger.debug("Not find object id from env");
			}

		}
		return map;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public void setActionType(ActionTypeEnum actionType) {
		this.actionType = actionType;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setDisplayType(DisplayTypeEnum displayType) {
		this.displayType = displayType;
	}
	
	public void setFilter(String filter) {
		if(filter ==null) return;
		this.filter = filter.trim();
		String uc= this.filter.toUpperCase();
		if(uc.startsWith("SELECT")){
			this.filterType=DisplayFilterType.SQL;
		}else if(uc.startsWith("python:")){
			this.filterType=DisplayFilterType.PYTHON;
			this.filter=this.filter.substring(7);
		}else{
			this.filterType=DisplayFilterType.BEANSHELL;
		}
		
	}
	
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setOrder(int od) {
		this.order = od;
	}
	
	public void setTableCategoryId(int tableCategoryId) {
		this.tableCategoryId = tableCategoryId;
	}
	
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	public void setSaveObjType(SaveObjectEnum saveObjType) {
		this.saveObjType = saveObjType;
	}
	public int getId(){
		return id;
	}
	
	public ActionTypeEnum getActionType() {
		return actionType;
	}
	public String getComments() {
		return comments;
	}
	public String getDescription() {
		return description;
	}
	public DisplayTypeEnum getDisplayType() {
		return displayType;
	}
	public String getFilter() {
		return filter;
	}
	public String getIconURL() {
		return iconURL;
	}
	public String getName() {
		return name;
	}
	public int getOrder() {
		return order;
	}
	public SaveObjectEnum getSaveObjType() {
		return saveObjType;
	}
	public int getTableCategoryId() {
		return tableCategoryId;
	}
	public int getTableId() {
		return tableId;
	}
	public String getUrlTarget() {
		return urlTarget;
	}
	
	public void setUrlTarget(String urlTarget) {
		this.urlTarget = urlTarget;
	}
	
	public void setScript(String script) {
		if(script!=null)this.script = script.trim();
		else script="";
	}
	public int getSubSystemId() {
		return subSystemId;
	}
	public void setSubSystemId(int subSystemId) {
		this.subSystemId = subSystemId;
	}
	public int getAcordionId() {
		return AcordionId;
	}
	public void setAcordionId(int AcordionId) {
		this.AcordionId = AcordionId;
	}
	
}
