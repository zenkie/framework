/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.portal.auth;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.liferay.portal.util.PropsUtil;

/**
 * Check Number changes in specified interval.
 * 
 * This is used for OTP authentication
 * 
 * @author yfzhu@agilecontrol.com
 */

public class CheckNO {
	private static final Log logger = LogFactory.getLog(CheckNO.class);	
	private static CheckNO instance=null;
	
    private  int checkInterval=-1;
    /**
     * Seed for generating random checkno
     */
    private long seed; 
    private  long nextChangeTime=0;
    private  int currentValue;
    private CheckNO(){
    	//logger.error("INSTANCE CREATED");
    }
    public static CheckNO getInstance(){
    	if(instance==null) instance= new CheckNO();
    	return instance;
    }
    private void computeValue(){
		if (checkInterval==-1){ 
	    	try{
				checkInterval=1000*Integer.parseInt((String) PropsUtil.get("auth.impl.otp.interval"));
			}catch(Throwable e){
				logger.error("Error auth.impl.otp.interval in portal.properties:"+PropsUtil.get("auth.impl.otp.interval") );
			}
			// different server request for checkno at different first time, so seed will
			// not be same and it will be hard to cracker to figure out this value.
    		seed=System.currentTimeMillis();
		}
		if(checkInterval<=0) checkInterval=120*1000;// default to 120 seconds
		long time = System.currentTimeMillis();
		nextChangeTime = ((time / checkInterval) * checkInterval)+checkInterval;
		//logger.error("current:"+ time+", next:"+ nextChangeTime);
		Random r = new Random((time / checkInterval) * checkInterval+seed);
		currentValue= Math.abs((Math.abs(r.nextInt()) + 1) % 9999);
		while(currentValue <999){
			// must be 4 digit
			currentValue= Math.abs((Math.abs(r.nextInt()) + 1) % 9999);
		}
		  	
    }
	/**
	 * Check Number changes in specified interval, check  
	 * portal.properties#auth.impl.otp.interval for that interval in seconds
	 * @return current check number.
	 */
    public int currentValue(){
		if(System.currentTimeMillis()>nextChangeTime){
			computeValue();
		}
		return currentValue;
    } 
    public static void main(String[] args) throws Exception{
    	int checkno1, checkno2=0;
    	while(true){
    		checkno1=nds.portal.auth.CheckNO.getInstance().currentValue() ;
    		if(checkno2!=checkno1){
    			System.out.println((new java.util.Date())+":"+ checkno1);
    		}
    		checkno2=checkno1;
    		Thread.sleep(1000);
    	}
    	
    }
}
