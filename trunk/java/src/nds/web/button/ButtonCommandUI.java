/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.LoginFailedException;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
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

public interface ButtonCommandUI {
	public final static String POPUP_TARGET_SELF="pop_self";
	public final static String POPUP_TARGET_BLANK="popup_window";
	public final static String POPUP_TARGET_TOP="pop_top";
	public final static String POPUP_TARGET_PARENT="pop_parent";
	public final static String POPUP_TARGET_DIALOG="btn_dialog_window";
	public final static String POPUP_TARGET_SMALL_DIALOG="btn_dialog_small";
	public final static String POPUP_TARGET_MEDIUM_DIALOG="btn_dialog_medium";
	/**
	 * Contruct html ui for the command of Input style
	 * @param request
	 * @param column
	 * @param objectId
	 * @return a whole input html code <input type='button' ...>
	 */
	public String constructHTML( HttpServletRequest request, Column column, int objectId );
	/**
	 * Contruct html ui for the command of Href style 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return a whole input html code <a href='' ...>
	 */
	//public String constructHTML_Href( HttpServletRequest request, Column column, int objectId );
}
