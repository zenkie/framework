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

public class FAIR_CreateMiniatures extends Command{
	/**
	 * @param event parameters:
	 * objectid - object id of b_pdt_media table	
	 * 该 Command 的作用是:将该 b_pdt_media对应的商品图片生成出来. 	
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	User usr=helper.getOperator(event);
    	int b_pdt_media_id=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	ValueHolder holder=new ValueHolder();
    	FairManager fairmanager=FairManager.getInstance();
    	int m_product_id=-1;
    	m_product_id=Tools.getInt(QueryEngine.getInstance().doQueryOne("select t.m_product_id from b_pdt_media t where t.id="+b_pdt_media_id), -1);
    	String clientdomain=usr.getClientDomain();
    	try {
			fairmanager.createThumbnails(b_pdt_media_id,m_product_id,clientdomain, true);
			holder.put("message", "图片生成成功!");
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			holder.put("message", "图片生成失败!");
		}
		holder.put("code", new Integer(0));//no change for current page		
    	return holder;
    }
    
    public static void  main(String arg[]){
    	FairManager fairmanager=FairManager.getInstance();
    	try {
			fairmanager.createThumbnails(7,38003,"burgeon",true);
		} catch (Exception e) {
		//	logger.error(e.getMessage(),e);
			e.printStackTrace();
		}
    	
    }
	    
}

