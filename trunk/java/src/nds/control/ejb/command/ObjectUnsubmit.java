package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.AuditUtils;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;
/**
 *  必须是已经提交的单据才支持反提交，反提交要求用户是单据的最后修改人（即由本人提交的单据）
 * @author yfzhu
 * @since 4.0
 */

public class ObjectUnsubmit extends Command{
   
    /**
     * 必须是已经提交的单据才支持反提交，反提交要求用户具有审核权限
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
    	Integer pid = new Integer(Tools.getInt(event.getParameterValue("id") ,-1));
        String spName = (String)event.getParameterValue("spName");
        if(nds.util.Validator.isNull(spName)){
        	spName=(String)event.getParameterValue("command");
        }
        String tableName=null;
        Table table=null;
        if(nds.util.Validator.isNotNull(spName)){ 
        	tableName=spName.substring(0,spName.indexOf("Unsubmit") ) ;
        }else{
        	Object to=event.getParameterValue("table");
        	if(to!=null){
        		logger.debug("table="+ to);
        		table= TableManager.getInstance().getTable(to.toString());
        		if(table!=null) tableName= table.getName();
        		else{
        			table=TableManager.getInstance().getTable(Tools.getInt(to, -1));
        			if(table!=null) tableName= table.getName();
        		}
        		
        	}else{
        		throw new NDSException("Could not fetch table information");
        	}
        	
        }
        logger.debug("table name="+ tableName +", table="+ table);
        String origTableName= tableName;
        if(table==null) table=TableManager.getInstance().getTable(tableName);
        
        tableName=table.getRealTableName();
        QueryEngine engine = QueryEngine.getInstance() ;
        
        int status = engine.getSheetStatus(tableName,pid.intValue() );
        if(status!=2){
            throw new NDSEventException("@object-not-submitted@" );
        }
        User user=helper.getOperator(event);
        int userId=user.getId().intValue()  ;

        // security check
        boolean b=false;
        try{
        	b=SecurityUtils.hasObjectPermission(userId, user.getName(), origTableName, 
        			pid.intValue(),Directory.AUDIT, event.getQuerySession());
        }catch(Exception e){
            throw new NDSEventException(e.getMessage() );
        }
        if (!b) throw new NDSEventException("@no-permission@!" );
        
        /*
        // security check, last modifier must be operator itself
        boolean b=false;
        int modifierId=-1;
        try{
        	modifierId=Tools.getInt(engine.doQueryOne("select modifierid from "+ tableName+" where id="+ pid),-1);
        }catch(Exception e){
            throw new NDSEventException("@could-not-find-modifier@" );
        }
    	if(userId!=modifierId){
    		throw new NDSEventException("@no-permission-to-unsubmit@" );
    	}
		*/
        ValueHolder v = null;
        String state=null;
        SPResult s=null;
        try{
        	spName=  table.getSubmitProcName();
           	if( nds.util.Validator.isNull(spName))spName=tableName+ "Unsubmit";
           	else spName= StringUtils.replace(spName.toUpperCase(), "SUBMIT", "UNSUBMIT");
           	
           	boolean isJavaClass= spName.indexOf('.')>0;
           	if(isJavaClass) throw new NDSException("Not support java class unsubmit method currently");
            ArrayList list = new ArrayList();
            list.add(new Integer(pid));
            s=engine.executeStoredProcedure(spName,list,true);
        }catch(Exception e){
        	     	
            if( e instanceof NDSException) throw (NDSException)e;
        	else throw new NDSEventException(e.getMessage(), e );
        }
        if(s.getCode()!=0){
        	throw new NDSEventException(s.getMessage());
        }
        v=new ValueHolder();
        v.put("code", String.valueOf(s.getCode()));
        v.put("message",s.getMessage() ) ;
        v.put("next-screen", "/html/nds/info.jsp");
        
        return v;

    }
}