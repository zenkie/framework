package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;



/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;

/**
 * Change status of crossorder and its children, include: m_inout, c_order, c_invoice, c_payment
 * note its children should not has two crossorder as its parent
 */

public class ChangeStatus extends Command {
	/**
	 * @param event contains "crossorder" and "tostatus"
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	event.setParameter("directory", "CHANGE_STATUS");
  	
  	helper.checkDirectoryWritePermission(event, usr);
  	
	int userid=Tools.getInt( event.getParameterValue("userid",true), -1);
	int crId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select id from c_crossorder where DOCNO='"+ event.getParameterValue("crossorder")+"'"), -1);
	int toStatus= Tools.getInt(event.getParameterValue("tostatus",true), 3); 
	int fromStatus=( toStatus==3?1:3);
	ArrayList params=new ArrayList();
	params.add(new Integer(crId));
	params.add(new Integer(toStatus));
	params.add(new Integer(fromStatus));
	QueryEngine.getInstance().executeStoredProcedure("change_status", params, false);
	ValueHolder holder= new ValueHolder();
	holder.put("message", "״̬�޸ĳɹ�!");
	holder.put("code","0");
	return holder;
  }
}