/******************************************************************
*
*$RCSfile: CalculatePrice.java,v $ $Revision: 1.1 $ $Author: Administrator $ $Date: 2006/06/24 00:32:31 $
*

********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import nds.schema.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.SPResult;
import nds.util.NDSException;
import nds.control.util.*;
import nds.security.*;
import nds.util.Tools;
/**
 * Calculate order/invoice item list/actual/limit price according to price list
 * 
 * @author yfzhu@agilecontrol.com
 */
public class CalculatePrice  extends Command{

    /**
     * @param event - special parameters:
     *  objectid - record.id
     *  columnid - the table's column, from which we can retrieve the table name of the record.
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	//logger.debug(event.toDetailString());
    	int objectId= Tools.getInt(event.getParameterValue("objectid",true), -1);
    	int columnId= Tools.getInt(event.getParameterValue("columnid",true), -1);
    	Table table= TableManager.getInstance().getColumn(columnId).getTable();
    	
    	String dir= table.getSecurityDirectory();
    	event.setParameter("directory",  dir);
		User usr =helper.getOperator(event);
		int clientId= usr.adClientId;
		int orgId= usr.adOrgId;
		int uId= usr.id.intValue();

		boolean b= (1== Tools.getInt(QueryEngine.getInstance().doQueryOne("select status from "+ table.getRealTableName()+" where id="+objectId),-1));
		if(!b) throw new NDSException("@no-permission@");
		
		if(!nds.control.util.SecurityUtils.hasObjectPermission(usr.getId().intValue(), usr.getName(),table.getName(), objectId,Directory.WRITE, event.getQuerySession() )){
			throw new NDSException("@no-permission@");
		}    	
		String spName= table.getRealTableName()+"_UPDATE_PRICE";
		ArrayList params=new ArrayList();
		params.add(new Integer(objectId));
		params.add(usr.id);
      	SPResult result =helper.executeStoredProcedure(spName, params, true);
        ValueHolder v = new ValueHolder();
      	if(result.isSuccessful() ){
            v.put("message",result.getMessage() ) ;
            v.put("next-screen", "/html/nds/info.jsp");
        }else{
            throw new NDSEventException(result.getDebugMessage());
        }
        return v;

    }
}