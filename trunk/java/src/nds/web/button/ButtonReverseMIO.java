/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.Hashtable;

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
 * For M_V_OSM_INOUT  and M_V_IVM_INOUT table,
 * 未提交的货运单 并且对应的货运计划单也未提交，可以显示此按钮。用户还应该有提交权限才能操作。
 * 将货运单的内容写回计划单，然后由系统删除此货运单。
 */
public class ButtonReverseMIO extends ButtonCommandUI_Impl{
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
		return "M_INOUT_REVERSE_MIO";
	}	
	/**
	 * 
	 * @return true when user can do submit, and the mio's parent is unsubmitted
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			int status= Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select status from m_inout a where id="+objectId+" and exists (select 1 from m_inout p where p.id=a.REF_INOUT_ID and p.status=1)"), -1);
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
}
