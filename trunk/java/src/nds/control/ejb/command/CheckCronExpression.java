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
 * Check cron expression on ad_trigger instance
 * 
 */

public class CheckCronExpression extends Command {
	/**
	 *  objectid will be  ad_trigger.id
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	
  	//int columnId =Tools.getInt( event.getParameterValue("columnid",true), -1);
  	int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
  	String cronExpr=(String) QueryEngine.getInstance().doQueryOne("select cron from ad_trigger where id="+ objectId);
  	ValueHolder holder= new ValueHolder();
  	try{
	  	JobManager jbm= (nds.schedule.JobManager) WebUtils.getServletContextManager().getActor( nds.util.WebKeys.JOB_MANAGER);
		holder.put("message",jbm.getExpressionSummary(cronExpr));
		holder.put("code","0");
  	}catch(Exception e){
  		logger.error("Error cron expression:"+cronExpr ,e);
  		holder.put("message","@cron-expression-error@:"+cronExpr);
  		holder.put("code","0");
//  		throw new NDSException("@cron-expression-error@:"+cronExpr);
  	}
	return holder;
  }
}