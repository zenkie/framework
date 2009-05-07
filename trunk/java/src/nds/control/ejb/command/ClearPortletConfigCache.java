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
import nds.schema.*;
import nds.web.config.*;


/*import com.liferay.portal.ejb.UserManagerUtil;
import com.liferay.portal.ejb.UserManager;
import com.liferay.portal.ejb.UserManagerFactory;

*/
import nds.schedule.JobManager;
import nds.security.User;

/**
 * Clear cache for specified PortletConfig
 */
public class ClearPortletConfigCache extends Command {
	/**
	 *  objectid will be  one of following table:
	 *  	ad_listuiconf, ad_listdataconf, ad_objuiconf
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
  	User usr=helper.getOperator(event);
  	TableManager manager=TableManager.getInstance();
  	int columnId =Tools.getInt( event.getParameterValue("columnid", true), -1);
  	Table table= manager.getColumn(columnId).getTable();
  	int tableId= table.getId();
  	int objectId= Tools.getInt( event.getParameterValue("objectid",true), -1);
  	int type;
  	if(tableId==manager.getTable("ad_listdataconf").getId()){
  		type= PortletConfig.TYPE_LIST_DATA;
  	}else if(tableId== manager.getTable("ad_listuiconf").getId()){
  		type=PortletConfig.TYPE_LIST_UI;
  	}else if(tableId==manager.getTable("ad_objuiconf").getId()){
  		type= PortletConfig.TYPE_OBJECT_UI;
  	}else{
  		throw new NDSException("Internal Error: table is not supported for PortletConfig:"+table);
  	}
  	PortletConfigManager pcManager=(PortletConfigManager)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.PORTLETCONFIG_MANAGER);
  	pcManager.removePortletConfig(objectId, type);
  	// also try to clear config of specified name
  	String name =(String) nds.query.QueryEngine.getInstance().doQueryOne("select name from "+ table.getName()+" where id="+ objectId);
  	logger.debug("name="+ name);
  	pcManager.removePortletConfig(name, type);
  		
  	ValueHolder holder= new ValueHolder();
	holder.put("message", "@complete@(cache size:"+ pcManager.getCacheSize()+")" );
	holder.put("code","0");
	return holder;
  	
  }
}