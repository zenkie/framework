/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.report.ReportTools;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * 欲删除AU_PHASEINSTANCE 必须要求用户具有删除对应AU_PROCESS 的权限
 * @since 4.0
 * @author yfzhu
 */

public class DeleteAuditPhaseInstance extends Command{
	/**
	 * @param event parameters:
	 *    objectid - object id of AU_PHASEINSTANCE table		
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	logger.debug(event.toDetailString());
    	User usr=helper.getOperator(event);
    	int id=Tools.getInt( event.getParameterValue("objectid",true), -1);
    	if(!usr.name.equals("root")){
    		int processId=Tools.getInt(QueryEngine.getInstance().doQueryOne("select AU_PROCESS_ID from AU_PHASEINSTANCE where id="+id),-1);
    		if (!nds.control.util.SecurityUtils.hasObjectPermission(usr.id.intValue(), usr.name, "AU_PROCESS", processId, nds.security.Directory.WRITE, event.getQuerySession()))
    			throw new NDSException("@no-permission@");
    	}
    	ValueHolder holder=new ValueHolder();
    	String message;
    	java.sql.Connection conn=QueryEngine.getInstance().getConnection();
    	try{
    		ArrayList al=new ArrayList();
    		al.add(new Integer(id));
    		QueryEngine.getInstance().executeStoredProcedure("AU_PHASEINSTANCE_DEL", al, false, conn);
    		holder.put("message","@complete@");
    		holder.put("code","2");// close ui
    	}catch(Throwable e){
    		throw new NDSException(e.getMessage());
    	}finally{
    		try{
    			conn.close();
    		}catch(Exception e){}
    	}
    	return holder;
    }
    
	    
}
