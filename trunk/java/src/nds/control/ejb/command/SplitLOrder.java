package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.*;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;

/**
 * Split L_Order
 */

public class SplitLOrder extends Command {
	
	/**
	 * @param event contains 
	 * "l_order_id"
	 * "splitmethod"
	 * "sub1","sub2".."sub10"
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	logger.debug(event.toDetailString());
	  User usr=helper.getOperator(event);
  	
  	int objectId= Tools.getInt(event.getParameterValue("l_order_id", true),-1);
  	String splitMethod= (String)event.getParameterValue("splitmethod", true);
  	StringBuffer sb=new StringBuffer();
  	String value;
  	for(int i=1;i< 11;i++){
  		value=  (String)event.getParameterValue("sub"+i, true);
  		if(Validator.isNotNull(value)){
  			if(sb.length()!=0)sb.append(",");
  			sb.append(value);
  		}
  	}
  	ArrayList params=new ArrayList();
  	params.add(new Integer(objectId));
  	params.add(splitMethod);
  	params.add(sb.toString());
  	params.add(usr.getId());
	try{
		helper.executeStoredProcedure("l_order_split", params, false);
	}catch(Exception e){
		logger.error("Error when split order:", e);
		if(e instanceof NDSException)throw (NDSException)e;
		else{
			throw new NDSException(e.getMessage(),e);
		}
	}
	ValueHolder holder= new ValueHolder();
	holder.put("message", "@complete@");
	holder.put("code","0");
	return holder;
  }
}