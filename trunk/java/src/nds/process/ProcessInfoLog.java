/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import java.io.*;
import java.sql.*;
import java.math.*;

/**
 * 	Process Info Log (VO)
 *
 *  @author Jorg Janke
 *  @version $Id: ProcessInfoLog.java,v 1.1 2006/01/31 03:00:57 Administrator Exp $
 */
public class ProcessInfoLog implements Serializable
{
	private Timestamp 	m_P_Date;
	private String 		m_P_Msg;
	
	/**
	 * 	Create Process Info Log.
	 *	@param P_Date Process Date
	 *	@param P_Msg Process Messagre
	 */
	public ProcessInfoLog (Timestamp P_Date,  String P_Msg)
	{
		m_P_Date= P_Date;
		m_P_Msg=P_Msg;
	}	//	ProcessInfoLog

	/**
	 * 	Create Process Info Log.
	 *	@param P_Msg Process Messagre
	 */
	public ProcessInfoLog ( String P_Msg)
	{
		this( new Timestamp(System.currentTimeMillis()), P_Msg );
	}	//	ProcessInfoLog


	/**
	 * Method getP_Date
	 * @return Timestamp
	 */
	public Timestamp getP_Date()
	{
		return m_P_Date;
	}
	/**
	 * Method setP_Date
	 * @param P_Date Timestamp
	 */
	public void setP_Date (Timestamp P_Date)
	{
		m_P_Date = P_Date;
	}


	/**
	 * Method getP_Msg
	 * @return String
	 */
	public String getP_Msg()
	{
		return m_P_Msg;
	}
	/**
	 * Method setP_Msg
	 * @param P_Msg String
	 */
	public void setP_Msg (String P_Msg)
	{
		m_P_Msg = P_Msg;
	}

}	//	ProcessInfoLog
