/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import java.sql.*;
import java.util.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.util.Validator;



/**
 * 	Process Info with Utilities
 *
 */
public class ProcessInfoUtil
{
	/**	Logger							*/
	private static Logger		logger= LoggerManager.getInstance().getLogger(ProcessInfoUtil.class.getName());
	private final static String INSERT_LOG="INSERT INTO AD_PInstance_Log "
		+ "(id,creationdate, modifieddate, isactive,AD_PInstance_ID,ad_client_id, ad_org_id,ownerid, modifierid,P_Date,P_Msg)"
		+ " select get_sequences('ad_pinstance_log'),sysdate,sysdate,'Y',id, ad_client_id,ad_org_id,modifierid, modifierid,?,? from ad_pinstance where id=?";
	/**************************************************************************

	/**
	 *	Query PInstance for result.
	 *  Fill Summary and success in ProcessInfo
	 * 	@param pi process info
	 */
	public static void setSummaryFromDB (ProcessInfo pi)
	{
	//	logger.debug("setSummaryFromDB - AD_PInstance_ID=" + pi.getAD_PInstance_ID());
		//
		int sleepTime = 2000;	//	2 secomds
		int noRetry = 5;        //  10 seconds total
		//
		String SQL = "SELECT Result,ErrorMsg FROM AD_PInstance "
			+ "WHERE AD_PInstance_ID=?"
			+ " AND Result IS NOT NULL";
		Connection conn=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		try
		{
			conn=QueryEngine.getInstance().getConnection();
			pstmt = conn.prepareStatement (SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			for (int noTry = 0; noTry < noRetry; noTry++)
			{
				pstmt.setInt(1, pi.getAD_PInstance_ID());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					//	we have a result
					int i = rs.getInt(1);
					if (i == 0)
					{
						pi.setSummary("Success");
					}
					else
					{
						pi.setSummary("Failure", true);
					}
					String Message = rs.getString(2);
					rs.close();
					pstmt.close();
					//
					if (Message != null)
						pi.addSummary ("  (" +   Message  + ")");
				//	logger.debug("setSummaryFromDB - " + Message);
					return;
				}


			}
		}
		catch (Exception e)
		{
			logger.error("setSummaryFromDB", e);
			pi.setSummary (e.getLocalizedMessage(), true);
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception e){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
			if(conn!=null) try{conn.close();}catch(Exception e){}
			
		}
	}	//	setSummaryFromDB

	/**
	 *	Set Log of Process.
	 * 	@param pi process info
	 */
	/*public static void setLogFromDB (ProcessInfo pi)
	{
		String sql = "SELECT Log_ID, P_ID, P_Date, P_Number, P_Msg "
			+ "FROM AD_PInstance_Log "
			+ "WHERE AD_PInstance_ID=? "
			+ "ORDER BY Log_ID";

		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, pi.getAD_PInstance_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			//	int Log_ID, int P_ID, Timestamp P_Date, BigDecimal P_Number, String P_Msg
				pi.addLog (rs.getInt(1), rs.getInt(2), rs.getTimestamp(3), rs.getBigDecimal(4), rs.getString(5));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			logger.error("setLogFromDB", e);
		}
	}	*/

	/**
	 *  Create Process Log
	 * 	@param pi process info
	 */
	public static void saveLogToDB (ProcessInfo pi)
	{
		//logger.debug("Into log ("+ pi.hashCode()+")");
		ProcessInfoLog[] logs = pi.getLogs();
		if (logs == null || logs.length == 0)
		{
			//logger.debug("saveLogToDB - No Log ("+ pi.hashCode()+")");
			return;
		}
		if (pi.getAD_PInstance_ID() == 0)
		{
			//logger.debug("saveLogToDB - not saved - AD_PInstance_ID==0 ("+ pi.hashCode()+")");
			return;
		}
		Connection conn=null;
		PreparedStatement pstmt=null;
		
		//
		try{
			conn= QueryEngine.getInstance().getConnection();
			pstmt= conn.prepareStatement(INSERT_LOG);
			
			for (int i = 0; i < logs.length; i++)
			{
				if (logs[i].getP_Date() == null)
					pstmt.setNull(1, Types.DATE);
				else
					pstmt.setTimestamp(1, new java.sql.Timestamp(logs[i].getP_Date().getTime()));
				if (logs[i].getP_Msg() == null)
					pstmt.setNull(2, Types.VARCHAR);
				else
					pstmt.setString(2, logs[i].getP_Msg());
				pstmt.setInt(3, pi.getAD_PInstance_ID());
				pstmt.executeUpdate();
			}
		}catch(Throwable e){
			logger.error("Fail to log for pinstance id="+ pi.getAD_PInstance_ID(),e);
		}finally{
			if(pstmt!=null){try{ pstmt.close();}catch(Throwable t){}}
			if(conn!=null){try{ conn.close();}catch(Throwable t){}}
		}
		pi.setLogList(null);	//	otherwise log entries are twice
	}   //  saveLogToDB

	/**
	 *  Set Parameter of Process (and Client/User)
	 * 	@param pi Process Info, only need AD_PInstance set
	 */
	public static void setParameterFromDB (ProcessInfo pi)
	{
		ArrayList list = new ArrayList();
		String sql = "SELECT p.Name,"         			    	//  1
			+ " p.P_String,p.P_String_To, p.P_Number,p.P_Number_To,"    //  2/3 4/5
			+ " p.P_Date,p.P_Date_To, p.Info,p.Info_To, "               //  6/7 8/9
			+ " i.AD_Client_ID, i.AD_Org_ID, i.AD_User_ID "				//	10..12
			+ "FROM AD_PInstance_Para p"
			+ " INNER JOIN AD_PInstance i ON (p.AD_PInstance_ID=i.id) "
			+ "WHERE p.AD_PInstance_ID=? "
			+ "ORDER BY p.OrderNO";
		Connection conn=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;

		try
		{
			conn= QueryEngine.getInstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, pi.getAD_PInstance_ID());
			//System.out.println("After : " + sql+pi.getAD_PInstance_ID());
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				String ParameterName = rs.getString(1);
				//	String
				Object Parameter = rs.getString(2);
				Object Parameter_To = rs.getString(3);
				//	Big Decimal
				if (Parameter == null && Parameter_To == null)
				{
					Parameter = rs.getBigDecimal(4);
					Parameter_To = rs.getBigDecimal(5);
				}
				//	Timestamp
				if (Parameter == null && Parameter_To == null)
				{
					Parameter = rs.getTimestamp(6);
					Parameter_To = rs.getTimestamp(7);
				}
				//	Info
				String Info = rs.getString(8);
				String Info_To = rs.getString(9);
				//
				list.add (new ProcessInfoParameter(ParameterName, Parameter, Parameter_To, Info, Info_To));
				//
				if (pi.getAD_Client_ID() == null)
					pi.setAD_Client_ID (rs.getInt(10));
				if (pi.getAD_User_ID() == null)
					pi.setAD_User_ID(rs.getInt(12));
			}
		}
		catch (Exception e)
		{
			logger.error("getParameter", e);
		}finally{
			if(rs!=null) try{rs.close();}catch(Exception e){}
			if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
			if(conn!=null) try{conn.close();}catch(Exception e){}
			
		}
		//
		ProcessInfoParameter[] pars = new ProcessInfoParameter[list.size()];
		list.toArray(pars);
		pi.setParameter(pars);
	}   //  setParameterFromDB

	

}	//	ProcessInfoUtil
