/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.util.*;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import nds.security.Directory;
import nds.security.LoginFailedException;
import nds.util.Tools;
import nds.util.WebKeys;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
/**
 * For 
 */
public class ButtonAdjustCrossOrder extends ButtonCommandUI_Impl{
	/**
	 * Call db procedure c_invoice_adj  
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId){
		/*ArrayList params=new ArrayList();
		params.add(column.getTable().getRealTableName());
		SPResult res= QueryEngine.getInstance().executeStoredProcedure("c_invoice_adj", params,true);
		int crossorderId=res.getCode();
		*/
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append(WebKeys.NDS_URI+ "/objext/c_crossorder_adj.jsp?column="+ column.getId()+"&objectid="+ objectId);
		return sb.toString();
	}
	/**
	 * Should display the button or just blank
	 * user permission should have submit action permission set on c_v_f_crossorder. 
	 * @return
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		if(column.getTable().getRealTableName().equals("C_CROSSORDER") ){
			// this record must have status=2
			try{
				if(Tools.getInt( QueryEngine.getInstance().doQueryOne("select status from C_CROSSORDER where nodetype in ('E','F') and id="+objectId), -1)!=2){
					return false;
				}
			}catch(Throwable t){
				logger.error("Fail to check status of crossorder of id="+ objectId, t);
			}
			
		}else if(column.getTable().getRealTableName().equals("C_CROSSORDERITEM")){
			// this record must have status=2
			try{
				if(Tools.getInt( QueryEngine.getInstance().doQueryOne("select status from C_CROSSORDERitem a where exists (select 1 from C_CROSSORDER b where b.id=a.c_crossorder_id and b.nodetype in ('E','F')) and a.id="+objectId), -1)!=2){
					return false;
				}
			}catch(Throwable t){
				logger.error("Fail to check status of crossorderitem of id="+ objectId, t);
			}
		}
		// user permission should has all action permission set on table
		UserWebImpl usr=(UserWebImpl) WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER);
		Table table= TableManager.getInstance().getTable("c_v_f_crossorder");
		int perm=usr.getPermission(table.getSecurityDirectory());
	  	int maxPerm = 0;
	  	if (table.isActionEnabled(Table.SUBMIT)) maxPerm |= Directory.SUBMIT;
	  	if( table.isActionEnabled(Table.MODIFY)|| 
	  			table.isActionEnabled(Table.ADD)||table.isActionEnabled(Table.DELETE)) maxPerm |= Directory.WRITE;
	  	if ( table.isActionEnabled(Table.QUERY)) maxPerm |= Directory.READ;
	  	
	  	if( ((perm & maxPerm) != maxPerm) &&  !"root".equals(usr.getUserName())) return false;//throw new NDSEventException("Permission denied");
	  	return true;
		
	}	
	
}
