/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

import java.util.*;
import nds.log.*;
/**
 *  进行并发控制的管理器，用法：
 *  
 *  设置并发数和等待时间 
 *  LogManager lm= new LockManager(5, 10); // 5个并发，最大等待时间为10秒 
 *  
 *  // customer 申请锁
 *  Object lock=null;
 *  try{
 * 		lock= lm.waitForLock();
 *  }catch( NoSuchElementException e){
 * 		// fail to gain lock
 *      ...
 *  }
 *  // do actions
 *  ....
 *  // release lock
 *  lm.releseLock(lock); // this lock must be the one gained from lm
 * 
 *  // destroy the manager
 *  lm.destroy();
 */
public class LockManager{
	private static Logger logger=LoggerManager.getInstance().getLogger(LockManager.class.getName());
	protected ObjectQueue queue; // contains locks
	private ArrayList locks;
	public LockManager(int lockCount, long timeOutSeconds, String name){
		queue=new  ObjectQueue(lockCount, timeOutSeconds*1000, name);
		// so the consumers will wait quietly for time.
		queue.setInDataPreparing(true);
		genLocks();
	}
	protected void genLocks(){
		locks=new ArrayList();
		for(int i=0;i< queue.getMaxLength();i++){
			Lock lock= new Lock( (i+1), this.hashCode());
			queue.addElement(lock);
			locks.add(lock);
		}
			
	}
	public String toString(){
		return queue.toString();
	}
	public String getPerformanceInfo(){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i< locks.size();i++){
			Lock lock=(Lock)locks.get(i);
			
		}
		return sb.toString();
	}
	/**
	 * Not the lock should be returned to LockManager using releaseLock
	 * after handled specified job. Else the other consumers may not doing
	 * jobs.
	 * @return Lock object
	 * @throws java.util.NoSuchElementException if element could not obtain
	 * after specified time
	 */
	public Object waitForLock() throws java.util.NoSuchElementException{
		long begin= System.currentTimeMillis();
		LockManager.Lock o=(LockManager.Lock)queue.nextElement();
		o.beginLease();
		logger.debug( "waitForLock:(T"+ Thread.currentThread().hashCode()+") "+queue.getName()+ "_"+ o.getId()+", queue size="+ queue.size()+", wait time="+ (System.currentTimeMillis()-begin)/1000);
		return o;
	}
	/**
	 * 
	 * @param lock
	 * @throws IllegalArgumentException if lock is not originally come from this
	 * Manager
	 */
	public void releaseLock(Object lockObj) throws IllegalArgumentException{
		LockManager.Lock lock=null;
		if (lockObj instanceof LockManager.Lock){
			lock=(LockManager.Lock)lockObj;
		}
		if( lock==null || lock.getLockManagerId() != this.hashCode() || 
				(lock.getId()> queue.getMaxLength())||
				lock.getId() <1 ){
			throw new IllegalArgumentException("Not my Lock.");
		}
		logger.debug( "releaseLock:(T"+ Thread.currentThread().hashCode()+") "+queue.getName()+ "_"+ lock.getId()+", lease time="+  lock.beginTime+", return time=" + ( new Date())+", queue size="+ queue.size());
		
		lock.endLease();
		
		queue.addElement(lock);
	}
	/**
	 * Destroy the manager
	 *
	 */
	public void destroy(){
		queue.destroy();
	}
	class Lock{
		private int id;
		private int managerId;
		Date beginTime; // 当前使用开始时间
		long averageDuration; // 锁的平均使用时间，以millisecond计算
		int useCount; // 锁的使用次数
		private Lock(int lockId, int lockManagerId){
			id=lockId;
			managerId=lockManagerId ;
			averageDuration=0;
			useCount=0;
		}
		public int getId(){
			return id;
		}
		public int getLockManagerId(){
			return managerId;
		}
		void beginLease(){
			beginTime= new Date();
		}
		void endLease(){
			averageDuration =( averageDuration * useCount+  (( new Date()).getTime() - beginTime.getTime()))/ (useCount+1);
			useCount ++;
		}
	}
}