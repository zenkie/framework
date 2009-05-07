package nds.util;

import java.util.ArrayList;
import java.util.NoSuchElementException;
//import nds.log.*;
/**
 * Object Queue which support multi-thread consumer/supplier work
 */
public class ObjectQueue implements java.io.Serializable{
    //private Logger logger;
	private int maxLength;
	private ArrayList queue;
	private long waitTime;
	private boolean inPreparing;
    private boolean dying;// when queue is requested to die, this will set to true;
    private String name;
	/**
	* @param maxLength Maximum length of the queue, if element size is equal to this one, we
	* say queue is full. If no positive integer specified, will take as unlimited.
	*/
	public ObjectQueue(int maxLength){
		this(maxLength, 0, "queue");
	}
	/**
	 * Wait time out in miliseconds
	 * @param maxLength
	 * @param wait time out in miliseconds, if 0, will wait until programically interrupted
	 * @param queueName name of the queue
	 */
	public ObjectQueue(int maxLength, long wait, String queueName){
		this.maxLength= maxLength;
		queue=new ArrayList();
		inPreparing=false;
        dying =false;
        this.waitTime=wait;
        name=queueName;
        //logger= LoggerManager.getInstance().getLogger(this.getClass().getName()+"_"+queueName);
	}
	public String getName(){
		return name;
	}
	public int getMaxLength(){
		return maxLength;
	}
	/**
	 * Change max length of the queue. 
	 * @param maxLength
	 */
	public void setMaxLength(int maxLength){
		this.maxLength = maxLength;
	}
	/**
	 * How long will the supplier wait until there are space to store the object
	 * and the customer wait until there are objects to obtain 
	 * @param time
	 */
	public void setWaitTime(long time){
		this.waitTime=time;
	}
    /**
     * @return size of current queue.
     */
    public int size(){
        return queue.size();
    }
	/**
	* add a new object into queue, if queue is full, the operation will be
	* locked until element been retrieved out
	*/
	public synchronized void addElement(Object obj){
		while( !dying && isFull()){
			try {
                wait(waitTime);
            } catch (InterruptedException e) { }
		}
		queue.add(obj);
		notifyAll();
	}
	public synchronized boolean hasMoreElements(){
		if( !dying && queue.size() > 0  ) return true;
		while ( !dying && queue.size() == 0 && inPreparing ){
			try {
                log("wait to check hasMoreElements");
                wait(waitTime);
                log("check hasMoreElements: quesize="+queue.size()+", inpreparing="+inPreparing);
            } catch (InterruptedException e) { }
		}
        notifyAll();
		return ( queue.size()>0);
	}
	/**
	* Tell that data is just preparing, yet not OK
	* This is specillay important if you want consumers wait even when
	* suppliers has nothing to supply.
	*/
	public void setInDataPreparing(boolean b){
		inPreparing =b;
	}
	/**
	* after next element being retrieved, the object will be remove from queue
	* @throws NoSuchElementException if could not get next element either because
	* queue is empty or wait timeout
	*/
	public synchronized Object nextElement() throws NoSuchElementException  {
		if ( !dying && queue.size() == 0 && inPreparing ){
			try {
                log("wait to getNextele");
                long b= System.currentTimeMillis();
                wait(waitTime);
                log("out wait getNextele:"+queue.size() +", inprepare: "+inPreparing+" wait "+ (System.currentTimeMillis()- b)/1000+" sec, limit time="+(waitTime/1000));

            } catch (InterruptedException e) { }
		}

		if(  queue.size() > 0){
			try{
				Object obj= queue.remove( queue.size() -1);
                //if(queue.size()==0)log("queue empty");

				//System.out.println("Element extracted out of queue, size changed to "+queue.size());
				notifyAll();
/*        if( obj instanceof  farm.backup.common.RecordBlock ){
            farm.backup.common.RecordBlock block= (farm.backup.common.RecordBlock) obj;
            System.out.println("Block exacted out of queue, table="+
                block.getTableID()+
                "size="+ block.size());
        }*/

                return obj;
			}catch(Exception e){
				throw new NoSuchElementException("Error:"+e);
			}
		}
		
		throw new NoSuchElementException("No element found or time out");

	}
    /**
     * If max length specified by Constructor is -1 or 0, always return false
     */
	public boolean isFull(){
        if (maxLength <1 ) return false;
		return (queue.size() >= maxLength);
	}
	public boolean isEmpty(){
		return (queue.size() == 0);
	}
    /**
     * wait current thread until queue size is bigger(not equal) than specified value,
     * this method is helpful when supplier finished and want to sweep trash now
     */
    public synchronized void waitOnSizeBigger(int size){
        if(size <0) return;
        while( queue.size() > size){
			try {
                wait(waitTime);
            } catch (InterruptedException e) { }
        }
        notifyAll();
    }
	/**
	* this queue is no longer used
	*/
	public synchronized void destroy(){
        dying=true;
//		queue.clear(); can not clear queue because this would probably called in
                       // normal situation when supplier finish while consumer is
                       // still extracting objects
		notifyAll();
        log("ObjectQueue destroied");
	}
    private void log(String s){
      //logger.debug(s);
    }
	public String toString(){
		return getName()+", maxlength="+ getMaxLength()+",current="+ size();
	}

}

