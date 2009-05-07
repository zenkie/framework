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
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
/**
 * For M_AttributeSet
 * Redirect to attribute set instance creation page when current attribute set object contains all attributes
 * that are "List" type (others are String | Number)  
 */
public class ButtonAddAttributeSetInstances extends ButtonCommandUI_Impl{
	/**
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId){
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append(WebKeys.NDS_URI+ "/pdt/addattrsetinsts.jsp?columnid=").append(column.getId()).append("&objectid=").append(objectId);
		return sb.toString();
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_BLANK;
	}
	/**
	 * 
	 * 
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		UserWebImpl userWeb= null;
		boolean b=false;
		try{
			// check object state first, that will use less time than check permission
			int cnt=nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select count(*) from m_attribute where ATTRIBUTEVALUETYPE ='L' and id in (select m_attribute_id from m_attributeuse where m_attributeset_id="+objectId+")"),-1);
			return cnt>0 && cnt==nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
					"select count(*) from m_attributeuse where m_attributeset_id="+objectId),-1);
		}catch(Throwable t){
			logger.error("Could not check attributes on attributeset id="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
		
}
