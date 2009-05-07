package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.query.*;
import nds.util.*;




/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schedule.JobManager;
import nds.security.User;

/**
 * Resume or suspend queue job
 *
 */

public class SwitchQueueJobState extends Command {
	/**
	 *  objectid will be  ad_processqueue_id
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	//int columnId =Tools.getInt( event.getParameterValue("columnid"), -1);
  	int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
  	ValueHolder holder= new ValueHolder();
  	try{
	  	String queueName=(String) QueryEngine.getInstance().doQueryOne("select name from ad_processqueue where id="+ objectId);
	  	JobManager jbm= (nds.schedule.JobManager) WebUtils.getServletContextManager().getActor( nds.util.WebKeys.JOB_MANAGER);
	  	jbm.switchQueueJobState(queueName);
	  	
		holder.put("message", "@queue-job-swtiched@");
		holder.put("code","1");// refresh screen
		return holder;
  	}catch(Exception e){
  		logger.error("Fail to switch queue job with id="+objectId ,e);
  		//throw new NDSException("Fail to switch queue job with id="+objectId ,e);
  		holder.put("message","@exception@:"+ e.getMessage());
  		holder.put("code", "0");
  	}
  	return holder;
  }
}