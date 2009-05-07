package nds.web.alert;
/**
 * Used to clear alerters in cache
 * @author yfzhu
 *
 */
public class AlerterManager {
	private static  AlerterManager instance;
	/**
	 * Clear alerts in cache
	 *
	 */
	public void clear(){
		AuditStateAlerter.getInstance().clear();
		BshAlerter.getInstance().clear();
		LimitValueAlerter.getInstance().clear();
	}
	public static AlerterManager getInstance(){
		if(instance==null){
			instance= new AlerterManager();
		}
		return instance;
	}
	
}
