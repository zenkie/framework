package nds.control.ejb.command;

import java.rmi.RemoteException;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.fair.FairManager;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;

public class Fair_ClearCache extends Command{
	/**
	 * @param event parameters:
	 *  objectid - object id of b_fair table	
	 *  Çå³ý b_fairidµÄProductCategoryListµÄCache
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	int b_fair_id=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	ValueHolder holder=new ValueHolder();
    	FairManager fairmanager=FairManager.getInstance();
    	try {
			fairmanager.clearCache(b_fair_id);
			holder.put("message", "@clear-cache-success@");
		} catch (Exception e) {
			holder.put("message", "@clear-cache-fail@"+"("+ e.getMessage()+")");
		}
		holder.put("code", new Integer(0));
    	return holder;
    }
}