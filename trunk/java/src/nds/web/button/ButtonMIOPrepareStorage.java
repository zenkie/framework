/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.Hashtable;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.security.Directory;
import nds.security.LoginFailedException;
import nds.util.*;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.util.SecurityUtils;
import nds.control.web.*;
/**
 * For M_V_N_INOUT table , 处理库存预留、预增和解除
 * 手工出入库单可能需要过一段时间才真正执行，但库存在之前就需要冻结，可以使用本按钮完成相应操作
 * 
 * 按钮将根据 m_inout 上的字段 adj_storage 的值来进行动作判断，'Y' 标识已经预留或预增了库存，按钮将显示为解除
 * 'N' (default) 标识未预留或预增库存，按钮将根据出入库类型决定，如为入库，显示预增，出库显示为预留
 * 
 * 当单据处于预增预留状态，将禁止对明细进行调整
 */
public class ButtonMIOPrepareStorage extends ButtonCommandUI_Impl{
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
		return "M_INOUT_PREPARE_STORAGE";
	}	
	/**
	 * 
	 * @return true when user can do submit, and the mio is unsubmitted
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			int status= Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select status from m_inout a where id="+objectId), -1);
			if( status==1){
				userWeb=((UserWebImpl)WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER));	 
				b=SecurityUtils.hasObjectPermission(userWeb.getUserId(), userWeb.getUserName(),
						column.getTable().getName(),objectId, Directory.SUBMIT, userWeb.getSession() );
			}else{
				b=false;
			}
		}catch(Throwable t){
			logger.error("Could not check user permission on ButtonReclaim: column="+ column+", objectId="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
	/**
	 * Button caption
	 * 按钮将根据 m_inout 上的字段 adj_storage 的值来进行动作判断，'Y' 标识已经预留或预增了库存，按钮将显示为解除
 * 'N' (default) 标识未预留或预增库存，按钮将根据出入库类型决定，如为入库，显示预增，出库显示为预留
	 * @return
	 */
	protected String getCaption(HttpServletRequest request, Column column, int objectId ){
		boolean adjusted=false;
		String doctype;
		try{
			List al=QueryEngine.getInstance().doQueryList("select adj_storage, doctype from m_inout where id="+ objectId);
			adjusted= "Y".equals( ((List)al.get(0)).get(0));
			doctype=  (String)((List)al.get(0)).get(1);
		}catch(Exception e){
			logger.error("Fail to get columns from m_inout (id="+objectId+"):"+ e.getLocalizedMessage());
			return "unknown";
		}
		boolean in= "MMR".equals( doctype);
		String caption= adjusted? (in? "clear-added-storage":"clear-preserved-storage"): (in?"pre-add-storage":"pre-reserve-storage");
		Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
		return MessagesHolder.getInstance().getMessage(locale, caption);
	}	
}
