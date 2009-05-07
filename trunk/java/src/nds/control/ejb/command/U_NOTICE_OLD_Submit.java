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
import com.liferay.portlet.calendar.service.CalEventServiceUtil;

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
public class U_NOTICE_OLD_Submit extends Command {
	private final static String GET_NOTICE_RECIEVERS=""+
	"select name from users where id in (select user_id from u_notice where id in(?)) or c_bpartner_id in "+
	"(select C_BPARTNER_ID from u_notice where id in (?)) union select users.name from u_groupuser, users "+
    "where u_groupuser.u_group_id in (select U_GROUP_ID from u_notice where id in (?)) and "+
    "users.id = u_groupuser.user_id";
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
    	String clientDomain= helper.getOperator(event).getClientDomain();
    	String oid = ""+ Tools.getInt(event.getParameterValue("id") ,-1); 
    	event.setParameter("command","ObjectSubmit");
    	ValueHolder vh=helper.handleEvent(event);
    	try{
    		clearCacheOfRecievers( clientDomain,oid,  logger);
    		vh.put("message","通知单提交成功!") ;
    		return vh;
    	}catch(Exception e){
    		logger.error("Could not submit notice:", e); 
    		throw new NDSEventException(e.getMessage());    		
    	}finally{
    	}
    }
    private static void clearCache( String userName, String clientDomain, Logger logger){
    	String scpId = CalEventServiceUtil.class.getName() + "." + (userName+"@"+clientDomain);
    	Map eventsPool = (Map)SimpleCachePool.get(scpId);
    	if(eventsPool !=null ){
    		eventsPool.clear();
    		logger.debug("Cleared calevent cache for " +  (userName+"@"+clientDomain));
    	}
    }
    /**
     * Recievers of the notices
     * @param noticeId in format like '32,3434' or '32'
     * @return elements are (String), user name in portal, that is user.name
     * @throws Exception
     */
    static  void clearCacheOfRecievers(String clientDomain, String noticeIds, Logger logger) throws Exception{
    	Connection conn= QueryEngine.getInstance().getConnection();
    	Statement stmt=null;
    	ResultSet rs= null;
    	try{
    	stmt= conn.createStatement();
    	String sql= nds.util.StringUtils.replace( GET_NOTICE_RECIEVERS, "?", noticeIds);
    	logger.debug("get recievers: " +  sql);
    	rs= stmt.executeQuery(sql);
    	while(rs.next()){
    		String userName= rs.getString(1) ;
    		clearCache(userName, clientDomain, logger);
    	}
    	}finally{
            try{if(stmt!=null)stmt.close();}catch(Exception ea){}
            try{if(rs!=null)rs.close();}catch(Exception e){}
            try{if(conn!=null)conn.close();}catch(Exception eb){}
    	}
    }

}
