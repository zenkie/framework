
package nds.export;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.net.Scheduler;
import nds.net.ThreadProcess;
import nds.query.QueryEngine;
import nds.util.*;

/**
 * Do export jobs
 */
public class ExportWorker implements Runnable{
    private Logger logger= LoggerManager.getInstance().getLogger(this.getClass().getName());
	
	private ObjectQueue idleWorkerQueue;
	
	private String workerId;
	private int jobId;
	public ExportWorker(ObjectQueue queue, String workerId){
		idleWorkerQueue= queue;
		this.workerId= workerId;
	}
	/**
	 * Asynchronized method to run job, notify ExportManager when finished.
	 * @param jobId
	 */
	public void handleJob(int jobId){
		// begin a new thread to do this
		this.jobId= jobId;
		Thread th=new Thread(this);
		th.start();
	}
	private void fakeJob()throws Exception{
		Thread.sleep(1000*100);
	}
	private void realJob() throws Exception{
		//get job
		ExportJob jb=new ExportJob(jobId);
		jb.loadFromDB();
		jb.execute();
	}
	public void run(){
		try{
			logger.debug(this + " handle job id="+ jobId);
			//fakeJob();
			realJob();
			//notify export manager that this job finished
			logger.debug(this + " job id="+ jobId+ " finished.");
		}catch(Exception e){
			logger.error(" Could not handle job id="+ jobId , e);
		}finally{
			idleWorkerQueue.addElement(this);
		}
	}
	public String toString(){
		return "ExportWorker_"+ workerId;
	}
}
