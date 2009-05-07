package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.*;
/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.security.User;
import nds.ahyy.*;


/**
 * 
 * 除了正常的保存外，自动更新图片
 **/

public class WEB_CLIENTModify extends Command {
	
	
	/**
	 * @param event
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
	  logger.debug(event.toDetailString());
	  User usr=helper.getOperator(event);
	  event.put("command", "ObjectModify");
	  ValueHolder holder=helper.handleEvent(event);
	  
      // update images for this client
      /*nds.control.util.WebClientUtils.createImages(usr.clientDomain, 
    		  (String)event.getParameterValue("NAME", true), 
    		  (String)event.getParameterValue("SAYING", true));
	  */
      // reload client information in cache
      nds.control.web.WebUtils.unloadAdClientId(usr.adClientId);
	  
      return holder;
      
  }
}