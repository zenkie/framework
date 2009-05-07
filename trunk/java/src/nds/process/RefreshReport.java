/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.util.SecurityUtils;
//import nds.olap.OLAPUtils;
import nds.security.User;
import nds.util.Tools;

/**
 * Refresh portlet olap report in file cache
 * 
 * @author yfzhu@agilecontrol.com
 */

public class RefreshReport extends SvrProcess
{
	private String portletName;
	private int queryId;
	/**
	 *  Parameters:
	 *    portletName, queryId(ad_
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("portletName"))
				portletName = ((String)para[i].getParameter());
			else if (name.equals("queryId"))
				queryId=  para[i].getParameterAsInt();
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
		// 	load query into cache file directly
		int userId= this.getAD_User_ID();
		User user= SecurityUtils.getUser(userId); 
		//OLAPUtils.createQueryResultToFile(queryId, userId, portletName, user.getName(), user.getClientDomain());
		throw new nds.util.NDSException("Deprecated");
//		return null;
	}
}
