/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schedule;

/**
 * 只有state=U 的进程才会被处理
 * @author yfzhu@agilecontrol.com
 */
public class OneTimeQueueExecuterJob extends QueueExecuterJob {
	private final static String LOAD_ONE_TIME_PROCESSES_BY_QUEUE="select i.ID,p.ID, p.CLASSNAME,p.PROCEDURENAME from ad_pinstance i, ad_process p where i.ad_processqueue_id=? and i.state='U' and i.isactive='Y' and p.id=i.ad_process_id order by i.orderno asc";
	/**
	 * will load all processes marked active and state="U" (unhandled)
	 * by order of the column "orderno" in ascending order
	 * @return sql for prepared statement, only has one question mark for queue id
	 */
	protected String getLoadJobSQL(){
		return LOAD_ONE_TIME_PROCESSES_BY_QUEUE;
	}	
}
