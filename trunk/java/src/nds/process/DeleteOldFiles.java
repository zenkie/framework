/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.util.SecurityUtils;
import nds.control.web.WebUtils;
//import nds.olap.OLAPUtils;
import java.io.*;
import nds.query.QueryUtils;
import nds.security.*;
import nds.util.*;
import nds.sms.*;

import java.sql.*;

/**
 * Delete files in user home directoy older than specified days 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class DeleteOldFiles extends SvrProcess
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
		Configurations conf=((Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS));
		String exportRootPath=conf.getProperty("export.root.nds","/act/home");
		
		User user= SecurityUtils.getUser(this.getAD_User_ID());

		String dirWay=exportRootPath+File.separator+ user.getClientDomain();
			
		int cnt=nds.util.FileUtils.deleteFilesOlderThanNdays(days, dirWay, true);
		
		String msg="Total "+ cnt + " files deleted in "+ dirWay+" older than "+ days+ " days";
		
		this.addLog(msg);
		log.debug(msg);
		return msg;
		
	}
}
