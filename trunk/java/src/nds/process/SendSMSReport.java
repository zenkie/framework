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
 * Send short message from sms_outmsg to smprocessor, and set msg state to 'B'
 * 
 * @author yfzhu@agilecontrol.com
 */

public class SendSMSReport extends SvrProcess
{
	private final static String GET_OUTMSG="select u.phone2,m.priority,m.content,nvl(u2.truename,u2.name),m.modifieddate,m.id from users u, sms_outmsg m, users u2 where u2.id=m.modifierid and u.id=m.user_id and m.state='A' and m.isactive='Y' and u.phone2 is not null order by m.priority desc, m.modifieddate asc";
	private final static String UPDATE_OUTMSG="update sms_outmsg set state='B' , modifieddate=sysdate where id=?";
	/**
	 *  Parameters:
	 *    no 
	 */
	protected void prepare()
	{
		/*ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
		}*/
	}	//	prepare	
	/**
	 *  Perrform process.
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		// 	load query into cache file directly
		//int userId= this.getAD_User_ID();
		//User user= SecurityUtils.getUser(userId); 
		SMProcessor processor= (SMProcessor)WebUtils.getServletContextManager().getActor( WebKeys.SM_PROCESSOR );
		
		Connection conn= null;
		Statement stmt=null;
		PreparedStatement pstmt=null;
		conn= QueryEngine.getInstance().getConnection();
		
		ResultSet rs=null;
		try{
			pstmt=conn.prepareStatement(UPDATE_OUTMSG);
			stmt= conn.createStatement();
			rs=stmt.executeQuery(GET_OUTMSG);
			int cnt=0;
			// create one record in sms_outmsg
			while(rs.next()){
				ShortMessage msg=new ShortMessage();
				msg.setReceiver(rs.getString(1));
				msg.setProperty("priority", ""+rs.getInt(2));
				msg.setContent(rs.getString(3));
				msg.setSender(rs.getString(4));
				msg.setCreationDate(rs.getTimestamp(5));
				// message will be valid in 24 hours, after then, sm processor will
				// not send this message, if DurationFilter configured
				msg.setDuration(-1);
				try{
					processor.sendMessage(msg);
					cnt ++;
					// 	mark msg to delete
					pstmt.setInt(1,rs.getInt(6) );
					if(pstmt.executeUpdate()<1)log.debug("message with id="+ rs.getInt(6)+" not updated?");
				}catch(Throwable t){
					// will not delete failed message
					log.error("Fail to send msg id="+ rs.getInt(6), t);
				}
				/*try{
				// 	mark msg to delete
					pstmt.setInt(1,rs.getInt(6) );
					if(pstmt.executeUpdate()<1)log.debug("message with id="+ rs.getInt(6)+" not updated?");
				}catch(Throwable t){
					log.error("Fail to update msg id="+ rs.getInt(6), t);
				}*/
			}
			log.debug("Total "+cnt+ " message sent.");
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(stmt!=null)try{stmt.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}			
		
		return null;
	}
}
