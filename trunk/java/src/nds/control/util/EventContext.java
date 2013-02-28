package nds.control.util;

import java.sql.Connection;
import java.util.Properties;
import javax.transaction.UserTransaction;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.Tools;

public final class EventContext {
	private static Logger logger = LoggerManager.getInstance().getLogger(
			EventContext.class.getName());

	private UserTransaction ut = null;
	private Connection conn = null;
	private static int transactionTimeout = 1800;

	public final Connection getConnection() {
		return conn;
	}

	public final void closeConnection() {
		try {
			if (conn != null)
				conn.close();
			return;
		} catch (Throwable e) {
			logger.error("Fail to close connection", e);
		}
	}

	public final Connection createConnection() {
		try {
			conn = QueryEngine.getInstance().getConnection();
		} catch (Throwable e) {
			logger.error("Fail to create connection", e);
		}
		return conn;
	}

	public final boolean isInUserTransaction() {
		return ut != null;
	}

	public final UserTransaction getUserTransaction() {
		return ut;
	}

	public final UserTransaction beginUserTransaction() throws Exception {
		ut = EJBUtils.getUserTransaction();
		ut.setTransactionTimeout(transactionTimeout);
		ut.begin();
		return ut;
	}

	public final void rollbackUserTransaction() throws Exception {
		ut.rollback();
	}

	public final void commitUserTransaction() throws Exception {
		ut.commit();
	}

	static {
		int i;
		i = Tools.getInt(
				EJBUtils.getApplicationConfigurations().getProperty(
						"controller.transaction.timeout"), -1);
		if (i != -1)
			transactionTimeout = i * 60;
	}
}