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
	 *  清除 b_fairid的ProductCategoryList的Cache
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	int b_fair_id=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	ValueHolder holder=new ValueHolder();
    	FairManager fairmanager=FairManager.getInstance();
    	try {
			fairmanager.clearCache(b_fair_id);
			holder.put("message", "清除缓存成功!");
		} catch (Exception e) {
			holder.put("message", "清除缓存失败!("+ e.getMessage()+")");
		}
		holder.put("code", new Integer(0));
    	return holder;
    }
}