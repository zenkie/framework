package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.process.ProcessUtils;
import nds.query.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schedule.JobManager;
import nds.security.User;

/**
 * Execute immediately ad_pinstance_id
 *
 */

public class ExecuteImmediateADProcessInstance extends Command {
	/**
	 *  objectid will be  ad_processqueue_id
	 */
	
	public boolean internalTransaction(DefaultWebEvent event) {
		return true;
	}
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	int objectId= Tools.getInt( event.getParameterValue("objectid"), -1);
  	try{
  		// the transaction will be maintained by out side ClientControllerBean
    	ValueHolder hd=ProcessUtils.executeImmediateADProcessInstance(objectId , usr.getId().intValue(), false);
	  	if(!hd.isOK()){
	  		throw new NDSException((String)hd.get("message"));
	  	}
	  	ValueHolder holder= new ValueHolder();
		holder.put("message", "@complete@");
		holder.put("code","0");
		return holder;
  	}catch(Exception e){
  		logger.error("Fail to execute pinstance id="+objectId ,e);
  		if( e instanceof NDSException )
  			throw ( NDSException)e;
  		else
  			throw new NDSException("Failed" ,e);
  	}
  }
}