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
import nds.schema.*;

import java.sql.*;
import java.util.*;

/**
 * Analyze schema statistic information, such as table row count ( only on table that is loaded in TableManager)
 * 
 * @author yfzhu@agilecontrol.com
 */

public class AnalyzeSchema extends SvrProcess
{
	private final static String UPDATE_TABLE_ROWCNT="UPDATE AD_TABLE SET ROWCNT=? WHERE ID=?";
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
		
		Connection conn= null;
		PreparedStatement pstmt=null;
		QueryEngine engine= QueryEngine.getInstance();
		conn= engine.getConnection();
		ArrayList al =new ArrayList();
		try{
			pstmt=conn.prepareStatement(UPDATE_TABLE_ROWCNT);
			
			int cnt=0;
			StringBuffer query;
			// create one record in sms_outmsg
			for(Iterator it=TableManager.getInstance().getAllTables().iterator();it.hasNext();){
				TableImpl tb=(TableImpl) it.next();
				try{
				query=new StringBuffer();
				query.append("SELECT COUNT(*) FROM ").append(tb.getRealTableName()).append(" ").append( tb.getName())
				.append(" WHERE ").append(tb.isAcitveFilterEnabled()?"ISACTIVE='Y'":"1=1");
			      // add refTable filter
    		      if(tb.getFilter()!=null){
			      	query.append(" AND "+ tb.getFilter());
			      }
    		    log.debug(query.toString());
    		    cnt=Tools.getInt(engine.doQueryOne(query.toString(), conn), 0);
    		    
    		    pstmt.setInt(1,cnt );
    		    pstmt.setInt(2, tb.getId());
    		    pstmt.executeUpdate();
    		    
    		    // update table in memory
    		    tb.setRowCount(cnt);
				}catch(Throwable t){
					log.error("Fail to analyze "+ tb.getName(), t);
					al.add(tb.getName() + "("+ StringUtils.getRootCause(t).getLocalizedMessage() +")"   );
				}
			}
			if(al.size()>0){
				this.addLog("Failed tables:" +  Tools.toString(al.toArray()));
				return "Some tables failed, see log for detail";
			}else
				return null;
		}finally{
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}			
		
		
	}
	
}
