/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import java.sql.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;

import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.schema.TableManager;
import nds.util.MessagesHolder;
import nds.util.Tools;
import nds.util.Validator;

/**
 * Server Process Template
 *  
 */
public abstract class SvrProcess implements ProcessCall {
	protected Logger log = LoggerManager.getInstance().getLogger(
			getClass().getName());

	private Properties m_ctx;

	private ProcessInfo m_pi;
	
	/**
	 * Server Process. Note that the class is initiated by startProcess.
	 */
	public SvrProcess() {
	} //  SvrProcess

	/**
	 * Start the process. Calls the abstract methods <code>process</code>. It
	 * should only return false, if the function could not be performed as this
	 * causes the process to abort.
	 * 
	 * @param ctx
	 *            Context
	 * @param pi
	 *            Process Info
	 * @return true if the next process should be performed
	 * @see org.compiere.process.ProcessCall#startProcess(Properties,
	 *      ProcessInfo)
	 */
	public final ValueHolder startProcess(Properties ctx, ProcessInfo pi) {
		//  Preparation
		m_ctx = ctx;
		m_pi = pi;
		
		lock();
		//
		ValueHolder holder=process();
		unlock();
		
		//return !m_pi.isError();
		return holder;
	} //  startProcess
	

	/** ********************************************************************** */

	/**
	 * Process - called when pressing the button ... in ...
	 */
	protected ValueHolder process() {
		String msg = null;
		boolean error = false;
		ValueHolder holder=new ValueHolder();
		try {
			prepare();
			msg = doIt();
			if(msg==null)msg="Finished at "+(new java.util.Date());
		} catch (Throwable e) {
			// get cause of e
			msg =nds.util.StringUtils.getRootCause(e).getMessage();
			log.error("Error processing pinstance id="
					+ this.getAD_PInstance_ID(), e);
			error = true;
			holder.put("message", msg);
			holder.put("code", "-1");
		}
		//unlock();

		//	Parse Variables
		//msg =MessagesHolder.getInstance().translateMessage( msg,
		// (m_ctx==null?Locale.getDefault():(Locale)m_ctx.get("locale")));
		m_pi.setSummary(msg, error);
		if(error)m_pi.addLog(new ProcessInfoLog(msg));
		ProcessInfoUtil.saveLogToDB(m_pi);
		return holder;
	} //  process

	/**
	 * Prepare - e.g., get Parameters.
	 */
	abstract protected void prepare();

	/**
	 * Perform process.
	 * 
	 * @return Message (variables are parsed)
	 * @throws Exception
	 *             if not successful
	 */
	abstract protected String doIt() throws Exception;

	/***************************************************************************
	 * 
	 * /** Get Process Info
	 * 
	 * @return Process Info
	 */
	public ProcessInfo getProcessInfo() {
		return m_pi;
	} //  getProcessInfo

	/**
	 * Get Properties
	 * 
	 * @return Properties
	 */
	public Properties getCtx() {
		return m_ctx;
	} //  getCtx

	/**
	 * Get Name
	 * 
	 * @return Name
	 */
	protected String getName() {
		return m_pi.getTitle();
	} //  getName

	/**
	 * Get Process Instance
	 * 
	 * @return Process Instance
	 */
	protected int getAD_PInstance_ID() {
		return m_pi.getAD_PInstance_ID();
	} //  getAD_PInstance_ID

	/**
	 * Get Table_ID
	 * 
	 * @return AD_Table_ID
	 */
	protected int getTable_ID() {
		return m_pi.getTable_ID();
	} //  getRecord_ID

	/**
	 * Get Record_ID
	 * 
	 * @return Record_ID
	 */
	protected int getRecord_ID() {
		return m_pi.getRecord_ID();
	} //  getRecord_ID

	/**
	 * Get AD_User_ID
	 * 
	 * @return AD_User_ID of Process owner
	 */
	protected int getAD_User_ID() {
		if (m_pi.getAD_User_ID() == null) {
			String sql = "SELECT AD_User_ID, AD_Client_ID FROM AD_PInstance WHERE ID=?";
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				conn = QueryEngine.getInstance().getConnection();
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, m_pi.getAD_PInstance_ID());
				rs = pstmt.executeQuery();
				if (rs.next()) {
					m_pi.setAD_User_ID(rs.getInt(1));
					m_pi.setAD_Client_ID(rs.getInt(2));
				}

			} catch (Exception e) {
				log.error("getAD_User_ID", e);
			} finally {
				if (rs != null)
					try {
						rs.close();
					} catch (Exception e) {
					}
				if (pstmt != null)
					try {
						pstmt.close();
					} catch (Exception e) {
					}
				if (conn != null)
					try {
						conn.close();
					} catch (Exception e) {
					}
			}
		}
		if (m_pi.getAD_User_ID() == null)
			return 0;
		return m_pi.getAD_User_ID().intValue();
	} //  getAD_User_ID

	/**
	 * Get AD_User_ID
	 * 
	 * @return AD_User_ID of Process owner
	 */
	protected int getAD_Client_ID() {
		if (m_pi.getAD_Client_ID() == null) {
			getAD_User_ID(); //	sets also Client
			if (m_pi.getAD_Client_ID() == null)
				return 0;
		}
		return m_pi.getAD_Client_ID().intValue();
	} //	getAD_Client_ID

	/***************************************************************************
	 * Get Parameter
	 * 
	 * @return parameter
	 */
	protected ProcessInfoParameter[] getParameters() {
		ProcessInfoParameter[] retValue = m_pi.getParameters();
		if (retValue == null) {
			ProcessInfoUtil.setParameterFromDB(m_pi);
			retValue = m_pi.getParameters();
		}
		return retValue;
	} //	getParameter

	/***************************************************************************
	 * Add Log Entry
	 * 
	 * @param date
	 *            date or null
	 * @param id
	 *            record id or 0
	 * @param number
	 *            number or null
	 * @param msg
	 *            message or null
	 */
	public void addLog(String msg) {
		if (m_pi != null)
			m_pi.addLog(msg);
		else
			log.info("addLog - " + msg);
	} //	addLog

	/***************************************************************************
	 * Execute function
	 * 
	 * @param className
	 *            class
	 * @param methodName
	 *            method
	 * @param args
	 *            arguments
	 * @return result
	 */
	public Object doIt(String className, String methodName, Object args[]) {
		try {
			Class clazz = Class.forName(className);
			Object object = clazz.newInstance();
			Method[] methods = clazz.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(methodName))
					return methods[i].invoke(object, args);
			}
		} catch (Exception ex) {
			log.error("doIt", ex);
		}
		return null;
	} //	doIt

	/** ********************************************************************** */

	/**
	 * Lock Process Instance
	 */
	private void lock() {
		try {
			
			QueryEngine.getInstance().executeUpdate(
					"UPDATE AD_PInstance SET STATE='I' WHERE ID="
							+ m_pi.getAD_PInstance_ID());
		} catch (Throwable e) {
			log.error("Fail to do lock on process intance id="
					+ m_pi.getAD_PInstance_ID(), e);
		}
	} //  lock

	/**
	 * Unlock Process Instance. Update Process Instance DB and write option
	 * return message
	 */
	private void unlock() {
		try {
			StringBuffer sql = new StringBuffer(
					"UPDATE AD_PInstance SET STATE='M',ModifiedDate=SysDate,Result=");
			sql.append(m_pi.isError() ? "1" : "0");
			String msg = m_pi.getSummary();
			if (msg != null && msg.length() > 0)
				sql.append(", ErrorMsg=").append(
						QueryUtils.TO_STRING(msg, 2000));
			sql.append(" WHERE ID=").append(
					m_pi.getAD_PInstance_ID());
			QueryEngine.getInstance().executeUpdate(sql.toString());
			
		} catch (Throwable e) {
			log.error("Fail to do unlock on process intance id="
					+ m_pi.getAD_PInstance_ID(), e);
		}
	} //  unlock
	/**
	 * Create u_note for owner
	 * @param content
	 */
	protected void notifyOwner(String subject,String content, String fileLink){
		try{
			subject=nds.util.MessagesHolder.getInstance().translateMessage(subject,TableManager.getInstance().getDefaultLocale());
			content=nds.util.MessagesHolder.getInstance().translateMessage(content,TableManager.getInstance().getDefaultLocale());
			ArrayList params=new ArrayList();
			params.add(new Integer( this.getAD_User_ID()));// user
			params.add(subject);// subject
			params.add(content);//content
			params.add(fileLink); //url
			QueryEngine.getInstance().executeStoredProcedure("u_note_create",params,false);			
		}catch(Throwable t){
			log.error("Fail to create u_note for user:"+ this.getAD_User_ID(), t);
		}
	}
} //  SvrProcess
