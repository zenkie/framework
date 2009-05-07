/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.event.DefaultWebEvent;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
//import nds.olap.OLAPUtils;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.*;

import java.io.*;

import java.sql.*;
import java.util.Locale;

/**
 * Audit process phases are time-limited to execute. If phase wait too long, it should be 
 * handled using action specified by "time out action".
 * 
 * @author yfzhu@agilecontrol.com
 */

public class CheckTimeoutAuditProcess extends SvrProcess
{
	
	/**
	 *  Parameters:
	 *    
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else 
				log.error("prepare - Unknown Parameter: " + name);			
		}
	}	//	prepare	
	/**
	 *  Perrform process.
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		/**
		 * Load all waiting phase instances that is out of time, order by creationdate, then
		 * do one by one ( one transaction each):
		 *      case reject: reject it, and mark object unsubmit
		 * 	    case accept: accept it, process next phase, until complete or wait
		 *      case wait:   do nothing and let it be
		 * 		case program: execute program and do according to it's return data
		 */
		Connection conn= null;
		Statement pstmt=null;
		
		ResultSet rs=null;
		conn= QueryEngine.getInstance().getConnection();
		
		Locale locale= TableManager.getInstance().getDefaultLocale();

		try{
			pstmt= conn.createStatement();
			rs=pstmt.executeQuery("select pi.id, p.timeout_action, p.timeout_program from au_phaseinstance pi, au_phase p where p.id=pi.au_phase_id and pi.state='W' and p.timeout_action<>'W' and pi.CREATIONDATE < sysdate- p.waittime  order by pi.CREATIONDATE asc" );
			while(rs.next()){
				checkPhaseInstance( rs.getInt(1), rs.getString(2), rs.getString(3));
			}
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		log.debug("Finished");
		
		return null;
	}
	/**
	 * Do transaction , throw no error
	 * @param phaseInstanceId the timeout phase instance that has time-out-action not equal to 'W'
	 */
	private void checkPhaseInstance(int phaseInstanceId, String timeOutAction, String timeOutProgram) {
		DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
		event.setParameter("command","ExecuteAuditTimeout");
		event.setParameter("operatorid",String.valueOf(this.getAD_User_ID()));
		event.setParameter("au_phaseinstance_id",  String.valueOf(phaseInstanceId));
		event.setParameter("timeout_action", timeOutAction);
		if(timeOutProgram!=null)event.setParameter("timeout_program", timeOutProgram);
        ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
        try{
        	ValueHolder vh=controller.handleEvent(event);
        }catch(Throwable t){
        	log.error("Fail to do time out action on phase instance id="+phaseInstanceId,t );
        	this.addLog("phase instance id="+phaseInstanceId+ " failed: "+ t.getMessage() );
        }
	}
	
}
