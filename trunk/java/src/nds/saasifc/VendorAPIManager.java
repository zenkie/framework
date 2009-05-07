package nds.saasifc;

import nds.control.util.ValueHolder;
import java.util.*;
/**
 * 
 * Vendor api manager
 * @author yfzhu
 *
 */
public interface VendorAPIManager {
	/**
	 * Call vendor api 
	 * @param apiName
	 * @param parameters
	 * @return contains "code" - 0 for ok, others fail, "message" contains information  
	 */
	public ValueHolder invokeAPI(String apiName,Map parameters) ;
	/**
	 * Single sign on support
	 * @param parameters deferent implementation has deferent parameter 
	 * @return true if has signed on
	 */
	public boolean hasSignedOn(String usrId, Map parameters);
	
	/**
	 * Init with parameters
	 * @param props
	 */
	public void init(Properties props);
}
