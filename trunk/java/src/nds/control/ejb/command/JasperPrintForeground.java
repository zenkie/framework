/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.util.Tools;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.*;

/**
 * Return JasperPrint object directly to caller (UI)
 * @author yfzhu@agilecontrol.com
 */

public class JasperPrintForeground extends Command{
	/**
	 * @param event parameters:
	 * 		"reportobject"	- JasperReport
	 * 		"reportparam"   - HashMap 
	 * This method will also return JasperPrint object in valueholder ("print")
	 */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException{
    	long startTime= System.currentTimeMillis();
    	JasperReport report= (JasperReport)event.getParameterValue("reportobject");
    	HashMap parameters=  (HashMap)event.getParameterValue("reportparam");
    	String message=null;
    	nds.security.User usr= helper.getOperator(event);
    	ValueHolder holder=new ValueHolder();
    	java.sql.Connection conn=QueryEngine.getInstance().getConnection();
    	try{
    		// catch all errors so write to destfolder
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, conn);
	    	holder.put("code","0");
	    	holder.put("message", "OK");
	    	holder.put("print", jasperPrint);
	    	return holder;
    	}catch(Throwable e){
    		logger.error("User "+ usr.getName() + "@" + usr.getClientDomain()+" fail to print foregound:", e);
    		throw new NDSException("@dotnot-print@"+ e);
    	}finally{
    		try{conn.close();}catch(Exception e){}
    	}
    }
}
