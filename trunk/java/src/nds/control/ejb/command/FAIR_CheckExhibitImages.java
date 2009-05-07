package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.fair.FairManager;
import nds.query.ColumnLink;
import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.query.QuerySession;
import nds.query.QueryUtils;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.NDSException;
import nds.util.Tools;
import nds.util.Validator;

public class FAIR_CheckExhibitImages extends Command{
	/**
	 * @param event parameters:
	 *  objectid - object id of b_fair table	   
	 *  该Command的作用为:检查该订货会所有的商品是不是已经全部生成了.  
	 *  若有图片没有生成的话,系统会调用生成图片的方法,生成相应的图片	
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	User usr=helper.getOperator(event);
    	int b_fair_id=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	ValueHolder holder=new ValueHolder();
    	FairManager fairmanager=FairManager.getInstance();
    	String clientdomain=usr.getClientDomain();
    	try {
			fairmanager.checkFairThumbnails(b_fair_id,clientdomain);
			holder.put("message", "检查成功!");
		} catch (Exception e) {
			logger.error("Failt ot check thumbnails: fair="+b_fair_id, e);
			holder.put("message", "检查失败!("+ e.getMessage()+")");
		}
		holder.put("code", new Integer(0));
    	return holder;
    }
}

