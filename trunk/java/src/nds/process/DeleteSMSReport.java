/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.util.SecurityUtils;
import nds.control.web.WebUtils;
//import nds.olap.OLAPUtils;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.security.User;
import nds.util.*;
import nds.sms.*;

import java.sql.*;

/**
 * delete 'B' state short message from sms_outmsg, whose modifieddate is <para>days</param> ago(not included that day).
 * 
 * @author yfzhu@agilecontrol.com
 */

public class DeleteSMSReport extends SvrProcess
{
	
	int days;
	/**
	 *  Parameters:
	 *    days - in days
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
			else if (name.equals("days"))
				days=  para[i].getParameterAsInt();
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

		int cnt=QueryEngine.getInstance().executeUpdate("delete sms_outmsg where state='B' and modifieddate<sysdate-"+days);
		log.debug("Total "+ cnt + " messages deleted.");
		
		return null;
	}
}
