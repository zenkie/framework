package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.query.*;
import nds.util.*;

/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.Directory;
import nds.security.User;

/**
 * Adjust plan M_InOut by picking list 
 * May create a new picking list when items found not exists in m_inout
 */

public class AdjustMPIOByPKL extends Command {
	/**
	 *  
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	int columnId =Tools.getInt( event.getParameterValue("columnid",true), -1);
  	int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
	/**
	 * Check plan inout write permission and picking list creation permission first
	 */
	int inoutId=nds.util.Tools.getInt(QueryEngine.getInstance().doQueryOne(
			"select io.id from m_pick p, m_inout io where io.nodetype='P' and io.status=1 and io.m_warehouse_id=p.m_warehouse_id and io.id=p.m_inout_id and p.id="+objectId),-1);
	if(inoutId != -1 ){
		
		// get m_inout pos: M_V_IVP_INOUT or M_V_OSP_INOUT
		String doctype=(String)QueryEngine.getInstance().doQueryOne("select DOCTYPE from m_inout where id="+ inoutId);
		String tableName=null;
		if("MMS".equals(doctype)) tableName="M_V_OSP_INOUT";
		else if("MMR".equals(doctype)) tableName="M_V_IVP_INOUT";
		else {
			logger.error("Internal error: unexpeced doctype of m_inout id="+ inoutId);
			// to avoid error, take one for default;
			tableName="M_V_IVP_INOUT";
		}
		if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.getId().intValue(), usr.getName(),tableName, inoutId,Directory.WRITE, event.getQuerySession() )){
			throw new NDSException("@no-permission@");
		}
	}else{
		throw new NDSException("@pls-check-plan-inout-for-picklist@");
	}
  	
  	
  	ArrayList al=new ArrayList();
  	al.add(new Integer(objectId));
  	al.add(usr.getId());
  	SPResult result= helper.executeStoredProcedure("M_PICK_ADJ_MPIO", al, true  );
  	
  	ValueHolder holder= new ValueHolder();
  	if(result.isSuccessful() ){
  		holder.put("message",result.getMessage() ) ;
  		holder.put("next-screen", "/html/nds/info.jsp");
        
    }else{
        throw new NDSEventException(result.getDebugMessage());
    }  	
	return holder;
  }
}