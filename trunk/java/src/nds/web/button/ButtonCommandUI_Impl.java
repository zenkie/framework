/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.Hashtable;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.Directory;
import nds.security.LoginFailedException;
import nds.util.WebKeys;
import nds.control.ejb.CommandFactory;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
/**
  在Column 定义里的ValueInterpeter 在 DisplayType ='button' 的时候，用于标识构造和响应按钮事件的方法。
  1)
  ValueInterpeter不含"." 或不是 "nds.web.ButtonCommandUI" 接口的实现时，表示显示按钮的控制类为 nds.web.ButtonCommandUI_Impl，
  生成的命令由nds.control.ejb.command.ButtonCommand处理。
  ButtonCommand 首先检查传入的事件中是否含有具体的事件处理类的信息（param='delegator')，如果不存在，将以Column的
  ValueInterpeter 作为delegator。delegator不含"."，具体的处理过程将交给 delegator 所指明的
  存储过程，如果delegator包含"."，具体的处理过程将交给delegator 所指明的类，此类应该继承Command。
  2)
  当ValueInterpeter 为ButtonCommandUI接口的实现时，表示显示按钮的控制类为ValueInterpeter所指明的类。可以考虑
  扩展nds.web.ButtonCommandUI_Impl, 如果仍希望借助ButtonCommand的机制，进入有关存储过程，可以override nds.web.ButtonCommandUI_Impl
  的 getDelegator() 方法，此方法缺省返回 Column.ValueInterpeter
 */
public class ButtonCommandUI_Impl implements ButtonCommandUI{
	protected Logger logger= LoggerManager.getInstance().getLogger(getClass().getName());
	private static ButtonCommandUI_Impl instance =null;
	
	/*public String constructHTML_Href( HttpServletRequest request, Column column, int objectId ){
		if(!this.isValid(request, column, objectId) || !this.isEnabled(request, column,objectId)){
			return "&nbsp;";
		}
		StringBuffer sb=new StringBuffer();
		sb.append("<div class='cul'><ul><li><a class='chref' href='");;
		String popType=getPopupType(request, column,objectId);
		sb.append("javascript:").append(popType).append("(\"");
			sb.append(getHandleURL(request, column,objectId)).append("\")'");
		sb.append(">").append(getCaption(request, column,objectId)).append("</a></li></ul></div>");
		return sb.toString();
		
	}*/
	public String constructHTML( HttpServletRequest request, Column column, int objectId ){
		if(!this.isValid(request, column, objectId)){
			return "&nbsp;";
		}
		StringBuffer sb=new StringBuffer();
		String caption=getCaption(request, column,objectId);
		sb.append("<input class='cbutton' type='button' name='col_").append(column.getId()).append("' value='").append( 
			caption).append("' ");
		if(!this.isEnabled(request, column,objectId))sb.append("disabled=true");
		else{
			String popType=getPopupType(request, column,objectId);
			sb.append(" onclick='javascript:").append(popType).append("(\"");
			sb.append(getHandleURL(request, column,objectId)).append("\"");
			if(nds.util.Tools.getYesNo(column.getDefaultValue(),false)){
				// will ask user before execute
				Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
				sb.append(",\"");
				String comments=TableManager.getInstance().getComments(column);  
				if(nds.util.Validator.isNotNull(comments)){
					sb.append(comments).append("\\n\\n");
				}
				sb.append(
						nds.util.MessagesHolder.getInstance().getMessage(locale, "are-you-sure-to-execute").replaceAll("\\{0\\}",caption)
				)
				.append("\"");
			}
			sb.append(")'");
		}
		sb.append(">");
		return sb.toString();
	}
	/**
	 * This is to get the UI handle page, if special page should be directed and get interaction
	 * between user and machine, this should be extended. 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId){
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append("/control/command?command=").append( this.getCommand(request, column,objectId)).append(
		"&objectid=").append(objectId).append("&columnid=").append(column.getId()).append("&delegator=")
		.append(getDelegator(request, column,objectId));
		return sb.toString();
	}
	/**
	 * Which real implementation will handle the command click event. Default sets to column.ValueInterpeter
	 * If extends this class, this method must be overriden.
	 * 
	 * 如果扩展了此类，则必须在Column.ValueInterpeter 设置上新的处理类，而此方法应该指明具体的命令执行类，故需要重载。
	 * 当然，如果重载了 getHandleURL 方法，这个方法就不会被调用了。
	 * 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getDelegator(HttpServletRequest request, Column column, int objectId){
		return column.getValueInterpeter();
	}
	/**
	 * This is to get Command handle class, the UI will be link specified by the return ValueHolder.
	 * To provide an interactvie interface the get further information from user, using #getHandleURL
	 * instead.
	 * of that command.
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getCommand( HttpServletRequest request, Column column, int objectId){
		return "ButtonCommand";
	}
	
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_DIALOG;
	}	
	/**
	 * Is command button enabled or not
	 * @return
	 */
	protected boolean isEnabled(HttpServletRequest request, Column column, int objectId ){
		return true;
	}
	/**
	 * Should display the button or just blank
	 * user permission should has all action permission set on table. We consume that button column
	 * should be on the main table.
	 * @return
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
	  	// user permission should has all action permission set on table
		UserWebImpl usr=(UserWebImpl) WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER);
		Table table= column.getTable();
		int perm=usr.getPermission(table.getSecurityDirectory());
	  	int maxPerm = 0;
	  	if (table.isActionEnabled(Table.SUBMIT)) maxPerm |= Directory.SUBMIT;
	  	if( table.isActionEnabled(Table.MODIFY)|| 
	  			table.isActionEnabled(Table.ADD)||table.isActionEnabled(Table.DELETE)) maxPerm |= Directory.WRITE;
	  	if ( table.isActionEnabled(Table.QUERY)) maxPerm |= Directory.READ;
	  	
	  	if( ((perm & maxPerm) != maxPerm) &&  !"root".equals(usr.getUserName())) return false;//throw new NDSEventException("Permission denied");
	  	//2008-06-29
	  	//增加对button的支持，如果字段为button, filter里的设置将决定button是否显示，过滤条件sql，将建立到filter中:
	  	//select count(*) from <table> where id=<id> and $filter, 当count=1时，显示button,否则不显示
	  	if(nds.util.Validator.isNotNull(column.getFilter())){
	  		String sql="select count(*) from "+ table.getRealTableName()+ " "+ table.getName()+" where "+
	  			table.getName()+"."+ table.getPrimaryKey().getName()+"="+ objectId+" AND "+ column.getFilter();
	  		try{
	  			int cnt= nds.util.Tools.getInt(nds.query.QueryEngine.getInstance().doQueryOne(sql),-1);
	  			return cnt==1;
	  		}catch(Throwable t){
	  			logger.error("fail to do sql:"+sql, t);
	  			return false;
	  		}
	  		
	  	}
	  	return true;
		
	}
	/**
	 * Button caption
	 * @return
	 */
	protected String getCaption(HttpServletRequest request, Column column, int objectId ){
		Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
		if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
		return column.getDescription(locale);
	}
	public static ButtonCommandUI_Impl getInstance(){
		if(instance ==null) instance= new ButtonCommandUI_Impl();
		return instance;
	}
}
