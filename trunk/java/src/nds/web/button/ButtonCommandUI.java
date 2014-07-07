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
  ��Column �������ValueInterpeter �� DisplayType ='button' ��ʱ�����ڱ�ʶ�������Ӧ��ť�¼��ķ�����
  1)
  ValueInterpeter����"." ���� "nds.web.ButtonCommandUI" �ӿڵ�ʵ��ʱ����ʾ��ʾ��ť�Ŀ�����Ϊ nds.web.ButtonCommandUI_Impl��
  ���ɵ�������nds.control.ejb.command.ButtonCommand����
  ButtonCommand ���ȼ�鴫����¼����Ƿ��о�����¼����������Ϣ��param='delegator')����������ڣ�����Column��
  ValueInterpeter ��Ϊdelegator��delegator����"."������Ĵ�����̽����� delegator ��ָ����
  �洢���̣����delegator����"."������Ĵ�����̽�����delegator ��ָ�����࣬����Ӧ�ü̳�Command��
  2)
  ��ValueInterpeter ΪButtonCommandUI�ӿڵ�ʵ��ʱ����ʾ��ʾ��ť�Ŀ�����ΪValueInterpeter��ָ�����ࡣ���Կ���
  ��չnds.web.ButtonCommandUI_Impl, �����ϣ������ButtonCommand�Ļ��ƣ������йش洢���̣�����override nds.web.ButtonCommandUI_Impl
  �� getDelegator() �������˷���ȱʡ���� Column.ValueInterpeter
 */

public interface ButtonCommandUI {
	public final static String POPUP_TARGET_SELF="pop_self";
	public final static String POPUP_TARGET_BLANK="popup_window";
	public final static String POPUP_TARGET_TOP="pop_top";
	public final static String POPUP_TARGET_PARENT="pop_parent";
	public final static String POPUP_TARGET_DIALOG="btn_dialog_window";
	public final static String POPUP_TARGET_SMALL_DIALOG="btn_dialog_small";
	public final static String POPUP_TARGET_MEDIUM_DIALOG="btn_dialog_medium";
	public final static String POPUP_TARGET_LARGE_DIALOG="btn_dialog_large"; // JUST ANOTHER OBJ IN OBJ DLG
	public final static String POPUP_TARGET_NODIALOG="btn_nodialog";
	
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
