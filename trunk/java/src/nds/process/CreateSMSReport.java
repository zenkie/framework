/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.util.SecurityUtils;
//import nds.olap.OLAPUtils;
import nds.schema.TableManager;
import nds.security.User;
import nds.util.*;
import java.sql.*;
import java.util.*;
import nds.query.*;
/**
 * Create SMS report. 
 * 
 * @author yfzhu@agilecontrol.com
 */

public class CreateSMSReport extends SvrProcess
{
	private String query;
	private int priority;
	private String group;
	private String reportName;
	private final static String INSERT_OUTMSG="insert into sms_outmsg(id, ad_client_id, ad_org_id,user_id,priority,content,ownerid, modifierid,creationdate,modifieddate,isactive)values(get_sequences('sms_outmsg'),?,?,?,?,?,?,?,sysdate,sysdate,'Y')";
	
	/**
	 *  Parameters:
	 *    priority - the priority of the generated message
	 *    query    - the report query (sql), contains wildcard variables
	 *    group    - user group(name), if not null, all users in that group will get that query result as report
	 * 				 if null, only creator will get the report
	 *   reportname - report name that will shown in the first line, note report name can contail following
	 *               variables:
	 * 					$D - Day of the report creation action
	 *                  $Y - year, $M - month, $H - hour, $m - minute
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			//log.error("Parameter "+ para[i]);
			String name = para[i].getParameterName();
			if (name.equals("query")){
				query = ((String)para[i].getInfo()); // query is in infor param
			}else if (name.equals("priority"))
				priority=  para[i].getParameterAsInt();
			else if (name.equals("group"))
				group= ((String)para[i].getParameter());
			else if (name.equals("reportname"))
				reportName= ((String)para[i].getParameter());
			else 
				log.error("prepare - Unknown Parameter: " + name);
		}
		//log.error("query= "+ query);
	}	//	prepare	
	/**
	 *  Insert new record(s) into sms_outmsg
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		// 	load query into cache file directly
		int userId= this.getAD_User_ID();
		User user= SecurityUtils.getUser(userId); //creator
		Connection conn= null;
		PreparedStatement pstmt=null;
		
		ResultSet rs=null;
		conn= QueryEngine.getInstance().getConnection();
		
		Locale locale= TableManager.getInstance().getDefaultLocale();
		
		boolean variableSQL= query.indexOf("$")>0;
		String sql,rpt;
		int uid;
		try{
			int groupId=Tools.getInt( QueryEngine.getInstance().doQueryOne("select id from groups where name='"+group+"'", conn),-1);
			if(groupId!=-1){
				pstmt= conn.prepareStatement("select userid from groupuser where groupid=?");
				pstmt.setInt(1, groupId);
				rs=pstmt.executeQuery();
				if(variableSQL){
					// have to generate sql for each user
					while(rs.next()){
						uid= rs.getInt(1);
						sql= QueryUtils.replaceVariables(query, QueryUtils.createQuerySession(uid, "", locale));
						rpt= getReportContent(sql, conn);
						createReportRecord(user, uid,conn, locale, rpt );
					}
				}else{
					// create report content first, so can be quicker
					sql= query;
					rpt= getReportContent(sql, conn);
					while(rs.next()){
						uid= rs.getInt(1);
						createReportRecord(user, uid,conn, locale, rpt );
					}
					
				}
			}else{
				// create report for owner
				sql= QueryUtils.replaceVariables(query, QueryUtils.createQuerySession(userId, "", locale));
				rpt= getReportContent(sql, conn);
				createReportRecord(user, userId,conn,locale,rpt );
			}
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}
		return null;
	}
	/**
	 * 
	 * @param sql sql with on variable
	 * @param conn
	 * @param locale
	 * @return report content
	 * @throws Exception
	 */
	private String getReportContent(String sql,Connection conn)throws Exception{
		log.debug(sql);
		Statement stmt=null;
		stmt= conn.createStatement();
		ResultSet rs=null;
		StringBuffer sb=new StringBuffer();
		try{
			// sql now is for uid specially
			rs=stmt.executeQuery(sql);
			int colCount=rs.getMetaData().getColumnCount();
			int i;
			String rpt= getReportName();
			if(rpt.length()>0) sb.append(rpt).append("--");
			
			Object fd;
			// create one record in sms_outmsg
			while(rs.next()){
				sb.append(rs.getObject(1));
				for(i=2;i<=colCount ;i++){
					fd=rs.getObject(i);
					sb.append(",").append((fd==null?"":fd));
				}
				sb.append(";");
			}
		}finally{
				if(rs!=null)try{rs.close();}catch(Throwable t){}
				if(stmt!=null)try{stmt.close();}catch(Throwable t){}
		}	
		return sb.toString();
	}
	/**
	 * Create sms_outmsg for user of <param>uid</param>
	 * @param creator 
	 * @param uid   the report reader
	 * @param variableSQL whether query contains variable  
	 * @param conn
	 * @throws Exception
	 */
	private void createReportRecord(User creator, int uid, Connection conn, Locale locale, String reportCotent)throws Exception{
		String sql;
		Statement stmt=null;
		PreparedStatement pstmt=null;
		stmt= conn.createStatement();
		ResultSet rs=null;
		try{
			// sql now is for uid specially
			pstmt = conn.prepareStatement(INSERT_OUTMSG);
			pstmt.setInt(1, creator.adClientId);
			pstmt.setInt(2,creator.adOrgId);
			pstmt.setInt(3, uid);
			pstmt.setInt(4, priority);
			pstmt.setString(5, reportCotent);
			pstmt.setInt(6, creator.id.intValue());
			pstmt.setInt(7, creator.id.intValue());
			pstmt.executeUpdate();
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(stmt!=null)try{stmt.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
		}		
	}
	
	/**
	 * Get report name, replace variables
	 * @return
	 */
	private String getReportName(){
		if(Validator.isNull( reportName)) return "";
		if(reportName.indexOf('$')<0) return reportName;
		Calendar cal= Calendar.getInstance();
		cal.setTime(new java.util.Date());
		String s= StringUtils.replace(reportName, "$Y", "Y"+cal.get(Calendar.YEAR) );
		s= StringUtils.replace(s, "$M", ""+(cal.get(Calendar.MONTH)+1) );
		s= StringUtils.replace(s, "$D", ""+(cal.get(Calendar.DAY_OF_MONTH)) );
		s= StringUtils.replace(s, "$H", ""+(cal.get(Calendar.HOUR_OF_DAY)) );
		s= StringUtils.replace(s, "$m", ""+(cal.get(Calendar.MINUTE)) );
		
		return s;
	}
}
