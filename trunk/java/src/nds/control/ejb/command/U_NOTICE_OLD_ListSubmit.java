package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.ejb.command.tree.TreeNodeHolder;
import nds.control.ejb.command.tree.TreeNodeManager;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.NavNode;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.Tools;
import nds.model.*;
import nds.model.dao.*;
import org.hibernate.*;
import nds.log.*;


import com.liferay.util.SimpleCachePool;
//import com.liferay.portlet.calendar.ejb.CalEventLocalUtil;

/**
 * 由于底层的portal 设置了cache, 使得当有新的event 加入到数据库的时候，
 * 界面上并不会显示此event。cache 的位置在 com.liferay.portlet.calendar.ejb.CalEventLocalUtil
 * cache 以用户名(userid)定位。
 * 
 * 本方法按系统正常流程将单据提交到数据库后，将设置当前ad_client 内的所有用户的cache无效，
 * 从而迫使portal 重新装载数据。
 * 
 * 具体方式为，依次寻找 SimpleCachePool 中含有 ad_client.domain 的Object, 清除之。
 * 
 * 参见:
 * com.liferay.portlet.calendar.ejb.CalEventLocalUtil
 * com.liferay.util.SimpleCachePool 
 */
public class U_NOTICE_OLD_ListSubmit extends Command {

	public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
    	String clientDomain= helper.getOperator(event).getClientDomain();

    	String[] itemidStr = event.getParameterValues("itemid");
        if (itemidStr==null) itemidStr= new String[0];
        String oid="";
        if(itemidStr.length >0 ) oid= itemidStr[0];
        for(int i=1;i< itemidStr.length;i++){
        	oid += ","+itemidStr[i];
        }
        
    	event.setParameter("command","ListSubmit");
    	ValueHolder vh=helper.handleEvent(event);
    	try{
    		U_NOTICE_OLD_Submit.clearCacheOfRecievers( clientDomain,oid,  logger);
    		vh.put("message","通知单提交成功!") ;
    		return vh;
    	}catch(Exception e){
    		logger.error("Could not submit notice:", e); 
    		throw new NDSEventException(e.getMessage());    		
    	}finally{
    	}
    }
    

}
