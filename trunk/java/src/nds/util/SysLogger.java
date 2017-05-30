/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nds.control.web.WebUtils;


;
/**
 * Log information to table c_syslog asynchronously
 * @author yfzhu@agilecontrol.com
 */

public class SysLogger {
	
	private static Log logger= LogFactory.getLog(SysLogger.class);	
	
	private static SysLogger instance=null;
	// how may LogEvent can be cached in queue, without leting program wait for the input time
	public static final int DEFAULT_QUEUE_SIZE=100;
	//syslog sleep time when no logevent,in seconds
	public static final int DEFAULT_SLEEP_SECONDS=5;
	private static final String INSERT_SYSLOG="insert into c_syslog(id, module, submodule,priority,operator,remote_host,message,ad_client_id)values(GET_SEQUENCES('C_SYSLOG'),?,?,?,?,?,?,?)"; 
	
	private static final int DEBUG=4;
	private static final int INFO=3;
	private static final int WARN=2;
	private static final int ERROR=1;
	private String datasourceName;
	private DataSource datasource;
	private ObjectQueue queue;
	private LogWriter writer;
	private class LogWriter implements Runnable{
		public void run(){
			
			while(queue.hasMoreElements()){
				LogEvent event=(LogEvent) queue.nextElement();
				doLog(event);
			}
		}
		private void doLog(LogEvent e){
	        Connection con=null;
	        PreparedStatement pstmt=null ;
	        try {
	        	con= getConnection() ;
	            pstmt= con.prepareStatement(INSERT_SYSLOG);
	            
	            pstmt.setString(1,e.module );
	            pstmt.setString(2,e.subModule );
	            pstmt.setString(3,String.valueOf(e.level) );
	            pstmt.setString(4,e.operator );
	            pstmt.setString(5,e.host );
	            pstmt.setString(6,e.message );
	            pstmt.setInt(7,e.adClientId );
	            pstmt.executeUpdate();
	        }
	        catch (Exception ex) {
	            logger.debug("Could not save event to syslog: "+ex, ex);
	        }finally{
	            try{ if(pstmt !=null)pstmt.close() ;}catch(Exception e2){}
	            try{ if(con!=null)con.close() ;} catch(Exception e3){}
	        }
		}
	}
	private class LogEvent{
		int level;
		int adClientId;
		String module;
		String subModule;
		String operator;
		String host;
		String message;
//		java.sql.Date creationDate;
		public LogEvent(int cid, int l, String m, String sm,String o, String h, String msg){
			level=l;
			module=m;
			subModule=sm;
			operator=o;
			host=h;
			message=msg;
			adClientId=cid;
//			creationDate=new java.sql.Date(System.currentTimeMillis());
		}
	}
	private SysLogger(){
		queue=new ObjectQueue(DEFAULT_QUEUE_SIZE, DEFAULT_SLEEP_SECONDS*1000, "syslogger");
		queue.setInDataPreparing(true);
		writer=new LogWriter();
		Thread t=new Thread(writer);
		t.start();
	}
	public void destroy(){
		queue.destroy();
	}
	/**
	 * 
	 * @param size
	 * @param wait in seconds
	 */
	public void init(String datasource, int size, int wait){
		datasourceName=datasource;
		queue.setMaxLength(size);
		queue.setWaitTime(wait*1000);
		logger.debug("SysLog("+size+","+wait+")");
	}
	/**
	 * Asynchronous
	 * @param level
	 * @param module
	 * @param operator
	 * @param host
	 * @param msg
	 */
	private void record(int level, String module,String submodule, String operator, String host, String msg,int adClientId){
	    Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
	    Boolean enableLog= Tools.getBoolean(conf.getProperty("portal.ensyslog"), false);
	    if(enableLog){
		LogEvent event= new LogEvent(adClientId,level, module,submodule,operator, host,msg );
		queue.addElement(event);
	    }
	}
    public void debug(String module,String submodule, String operator, String host, String msg,int adClientId){
    	record(DEBUG,  module,submodule,operator, host,msg ,adClientId );
    }
    public void error(String module,String submodule, String operator, String host, String msg,int adClientId){
    	record(ERROR,  module,submodule,operator, host,msg ,adClientId );
    }

    public void info(String module,String submodule, String operator, String host, String msg,int adClientId){
    	record(INFO,  module,submodule,operator, host,msg ,adClientId );
    }
    public void warning(String module,String submodule, String operator, String host, String msg,int adClientId){
    	record(WARN,  module,submodule,operator, host,msg ,adClientId );
    }
    /**
	 * Get connection to nds2 schema
	 * @return
	 * @throws Exception
	 */
	private Connection getConnection() throws Exception{
        // Get a context for the JNDI look up
		if(datasource==null){
	        Context ctx = new InitialContext();
	        // Look up myDataSource
	        datasource = (DataSource) ctx.lookup (datasourceName);
		}
		return datasource.getConnection();
	}
	public static SysLogger getInstance(){
		if(instance==null){
			instance= new SysLogger();
		}
		return instance;
	}
	
}
