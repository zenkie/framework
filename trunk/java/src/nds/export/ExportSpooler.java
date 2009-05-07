/*
 * Created on 2004-11-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nds.export;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.*;
import java.sql.ResultSet;
/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExportSpooler {
	
    private static Logger logger= LoggerManager.getInstance().getLogger(ExportSpooler.class.getName());
	private final static String JOB_BEFORE="select count(*) from spo_export where priority>=(select priority from spo_export where id=?) and creationdate>(select creationdate from spo_export where id=?)";
	/**
	 * greater value of "priority" and ealier value of "creationDate" will be handled first 
	 * @return jobs that will be printed before specified job, -1 if exception found
	 */
	public static int getJobCountBeforeJob(int jobId){
		int i=-1;
		ResultSet res=null;
		try{
			res=QueryEngine.getInstance().doQuery("select count(*) from spo_export where priority>=(select priority from spo_export where id="+jobId+") and creationdate<(select creationdate from spo_export where id="+ jobId+")", true);
			if( res.next()) i= res.getInt(1);
		}catch(Exception e){
			logger.error("Found error when get job count:", e);
		}finally{
			if(res !=null)try{ res.close();}catch(Exception ex){}
		}
		return i;
	}
	/**
	 * Every user has default priority in export spooler, default to 1
	 * @param userId
	 * @return
	 */
	public static int getDefaultPriority(int userId){
		int i=1;
		ResultSet res=null;
		try{
			res=QueryEngine.getInstance().doQuery("select priority from spo_def_priority where userid="+userId, true);
			if( res.next()) i= res.getInt(1);
		}catch(Exception e){
			logger.error("Found error when get user priority:", e);
		}finally{
			if(res !=null)try{ res.close();}catch(Exception ex){}
		}
		return i;
		
	}
}
