/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.security.LoginFailedException;
import nds.util.Tools;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
/**
 * Used for table which contains "ad_table_id" and "record_id" as specifing
 * destination table reocrd
 * 
 * The url will direct to "REAL TABLE" screen, use nds.web.button.ButtonViewRecord2 for specified
 * table screen.
 */
public class ButtonViewRecord extends ButtonCommandUI_Impl{
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
		int tableId, recordId;
		// only go to real table's UI, since object's status may be changed at that time
		// if you want to go to specified table ui, use nds.web.button.ButtonViewRecord2 instead
		try{
			List al=QueryEngine.getInstance().doQueryList("select record_id, ad_table_id from "+ column.getTable().getName()+" where id="+objectId);
			recordId = Tools.getInt(((List)al.get(0)).get(0),-1);
			tableId=Tools.getInt(((List)al.get(0)).get(1), -1);
			tableId=TableManager.getInstance().getTable(TableManager.getInstance().getTable(tableId).getRealTableName()).getId();
			
			sb.append(WebKeys.WEB_CONTEXT_ROOT).
			append(WebKeys.NDS_URI+ "/object/object.jsp?table="+tableId+"&id="+recordId );
		}catch(Throwable t){
			logger.error("Fail to fetch infor of pi:"+ objectId, t);
			sb.append("#");
		}
		return sb.toString();
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_BLANK;
	}	

}
