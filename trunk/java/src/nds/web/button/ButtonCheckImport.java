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
import nds.query.QueryEngine;
import nds.security.Directory;
import nds.security.LoginFailedException;
import nds.util.Tools;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.util.SecurityUtils;
import nds.control.web.*;
/**
 * 当记录的doctype类型为 'POO'，或 'SOO'的时候，显示按钮
 * 
 */
public class ButtonCheckImport extends ButtonCommandUI_Impl{

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
		return  column.getTable().getRealTableName()+"_IMPORT";
	}	
	/**
	 * 
	 * @return true when user can do submit, and the mio's parent is unsubmitted
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		boolean b=super.isValid(request, column, objectId);
		if(b){
			try{
			 String s=(String)QueryEngine.getInstance().doQueryOne("select doctype from "+ column.getTable().getRealTableName()+" where id="+ objectId);
			 b= (column.getTable().getRealTableName().equalsIgnoreCase("M_SALE") && "SOO".equals(s))||
			 	(column.getTable().getRealTableName().equalsIgnoreCase("M_PURCHASE") && "POO".equals(s));
			}catch(Throwable t){
				logger.error("Fail to do import check on "+ column+", id="+objectId,t);
				b= false;
			}
		}
		return b;
	}	
}
