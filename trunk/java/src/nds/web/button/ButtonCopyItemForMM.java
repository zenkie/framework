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
import nds.security.LoginFailedException;
import nds.util.*;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
/**
 * Copy item to material of other attributes, only for material whose attributeset contains all List type attribute.
 * For item of these condition:
 * 	has columns: m_product_id, m_attributesetinstance_id, at least one column that is editable number type
 *  table is addable
 */
public class ButtonCopyItemForMM extends ButtonCommandUI_Impl{
	/**
	 * 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId){
		PairTable fixedColumns=PairTable.parseIntTable(request.getParameter("fixedcolumns"), null);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append(WebKeys.NDS_URI+ "/pdt/copyitem.jsp?table="+ column.getTable().getId()+"&id=").append(objectId).
		append("&fixedcolumns=").append(java.net.URLEncoder.encode(fixedColumns.toURLQueryString(""))).
		append("&").append(WebUtils.getMainTableLink(request));
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
		/*Table table= column.getTable();
		if (!table.isActionEnabled(Table.ADD)) return false;
		if( (table.getColumn("m_product")==null && !table.getName().equalsIgnoreCase("m_product")) 
				|| table.getColumn("m_attributesetinstance_id")==null) return false;
		*/
		UserWebImpl userWeb= null;
		boolean b=false;
		try{

			int setId=-1;
			if( column.getTable().getName().equalsIgnoreCase("M_PRODUCT")){
				setId= nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select m_attributeset_id from m_product where id ="+objectId),-1);
			}else{
				// m_product_id should exists
				setId= nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select m_attributeset_id from m_product where id =(select m_product_id from "+
						column.getTable().getRealTableName()+" where id="+objectId+")"),-1);
			}
			if(setId !=-1){
				// check object state first, that will use less time than check permission
				int cnt=nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select count(*) from m_attribute where ATTRIBUTEVALUETYPE ='L' and id in (select m_attribute_id from m_attributeuse where m_attributeset_id="+setId+")"),-1);
				return cnt>0 && cnt==nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
						"select count(*) from m_attributeuse where m_attributeset_id="+setId),-1);
			}
		}catch(Throwable t){
			logger.error("Could not check attributes on table "+ column.getTable()+", id="+objectId+
					", user="+ ( userWeb!=null? userWeb.getUserId():-1), t);
		}
		return b;
	}	
			

}
