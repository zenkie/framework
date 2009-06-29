/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.util.SecurityUtils;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.security.User;
import nds.util.*;
import nds.sms.*;

import java.util.*;
import java.sql.*;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.mail.*; 

/**
 * Send u_notice via email.
 * 
 * U_Notice.status=2 and ismail='Y' and mail_status in ('INIT','TRANS') 的通知单将被创建mail通知, 对于通知在 24 小时内仍未发出
 * 的情况下，将不再发送。邮件服务器异常，无法链接服务器类似的错误将不含被设置为发件失败。
 * 
 * @author yfzhu@agilecontrol.com
 */

public class SendNoticeMail extends SvrProcess
{
	private final static String GET_OUTMSG="select u.id, u.title, u.description, u.fileurl, s.email,s.truename from u_notice u, users s where u.status=2 and u.ismail='Y' and u.mail_status in ('INIT','TRANS') and s.id=u.ownerid and u.modifieddate>sysdate-1 order by u.priorityrule asc";
	// 来自于 user 和 u_group
	private final static String GET_OUTMSG_TO="select u.email,u.truename from u_notice n, users u where n.id=? and u.id=n.user_id union select u.email,u.truename from u_notice n, users u, u_groupuser g where n.id=? and g.u_group_id=n.U_GROUP_ID and u.id=g.USER_ID";
	private final static String UPDATE_OUTMSG="update u_notice set mail_status=? , mail_info=? where id=?";
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
	 *  @return Message that would be set to process info summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		// 	load query into cache file directly
		//int userId= this.getAD_User_ID();
		//User user= SecurityUtils.getUser(userId); 
	    Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
	    String bounceAddress= conf.getProperty("email.returnpath");
		String mailSessionJNDIName=conf.getProperty("jndi.mailsession","mail/MailSession");
		Context ctx = new InitialContext();
		javax.mail.Session msession=(javax.mail.Session) ctx.lookup(mailSessionJNDIName);
		
		Connection conn= null;
		Statement stmt=null;
		PreparedStatement pstmt=null;
		PreparedStatement pstmt2=null;
		conn= QueryEngine.getInstance().getConnection();
		
		ResultSet rs=null;
		try{
			pstmt=conn.prepareStatement(UPDATE_OUTMSG);
			pstmt2=conn.prepareStatement(GET_OUTMSG_TO);
			stmt= conn.createStatement();
			rs=stmt.executeQuery(GET_OUTMSG);
			
			int cnt=0;
			// create one record
			while(rs.next()){
				int noticeId= rs.getInt(1);
				String title= rs.getString(2);
				String content= rs.getString(3);
				String fileURL= rs.getString(4);
				if(nds.util.Validator.isNotNull(fileURL))
					content=content+ "\n附件链接：" + fileURL; 
				String fromAddr= rs.getString(5);
				String fromUser= rs.getString(6);
				StringBuffer error=new StringBuffer();
				boolean partFail=false, allFail=true;
				// every 20 users will be one email
				String[][][] recs = getRecievers(pstmt2, noticeId);
				if( recs==null){
					log.debug("no reciever for u_notice id="+ noticeId);
					continue;
				}
				for(int i=0;i< recs.length;i++){
					SimpleEmail email= new SimpleEmail();
					//email.setMailSessionFromJNDI(mailSessionJNDIName);
					email.setMailSession(msession);
					email.setCharset("GBK");
					
					StringBuffer sb=new StringBuffer("To: ");
					for(int j=0;j< recs[i].length;j++){
						if(recs[i][j][0]!=null){
							email.addTo(recs[i][j][0], recs[i][j][1]);
							sb.append(recs[i][j][0]).append(",");
						}
					}
					log.debug(sb.toString());
					
					email.addReplyTo(fromAddr, fromUser,"GBK");
					if(bounceAddress!=null)email.setBounceAddress(bounceAddress);
					email.setSubject(title);
					email.setMsg(content);
					//log.debug(toString(email));
					try{
						email.send();
						allFail=false;
					}catch(Throwable te){
						log.error("fail to send", te);
						error.append(te.getLocalizedMessage()).append("."); 
						partFail=true;
					}

				}
				if(partFail && allFail){
					//all fail
					pstmt.setString(1, "FAIL");
					pstmt.setString(2,error.toString());
				}else if(!allFail && partFail){
					// part fail
					pstmt.setString(1, "PART");
					pstmt.setString(2,error.toString());
				}else if(!partFail){
					// all ok
					pstmt.setString(1, "OK");
					pstmt.setString(2,"");
				}
				pstmt.setInt(3, noticeId);
				pstmt.executeUpdate();
				cnt++;
				
			}
			log.debug("Total "+cnt+ " message sent.");
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(stmt!=null)try{stmt.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(pstmt2!=null)try{pstmt2.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}			
		
		return null;
	}
	/**
	 * 
	 * @param pstmt
	 * @param noticeId
	 * @return [] 20 users ,  recs[][] users, [][][0] email, [][][1] truename 
	 * @throws Excpetion
	 */
	String[][][] getRecievers(PreparedStatement pstmt, int noticeId) throws Exception{
		ResultSet rs=null;
		pstmt.setInt(1, noticeId);
		pstmt.setInt(2,noticeId);
		rs= pstmt.executeQuery();
		ArrayList emails=new ArrayList();
		ArrayList trueNames=new ArrayList();
		while(rs.next()){
			String email=rs.getString(1);
			String trueName=rs.getString(2);
			emails.add(email);
			trueNames.add(trueName);
		}
		if(emails.size()==0) return null;
		int k=(int)Math.ceil(emails.size()/ 20.0);
		
		String[][][] r=new String[k][][];
		
		for(int i=0;i<(int)Math.floor(emails.size()/ 20.0) ;i++){
			r[i]=new String[20][2];
			for(int j=0;j<20;j++){
				r[i][j][0]= (String)emails.get(i*20+ j);
				r[i][j][1]= (String)trueNames.get(i*20+j);
			}
		}
		int start= ((int)Math.floor(emails.size()/ 20.0))* 20;
		if(emails.size()-start >0){
			r[k-1]=new String[emails.size()-start][2];
			for(int i=start;i<emails.size();i++ ){
				r[k-1][i-start][0]=(String)emails.get(i);
				r[k-1][i-start][1]= (String)trueNames.get(i);
			
			}
		}
		return r;
	}
	
}
