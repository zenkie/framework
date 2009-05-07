package nds.saasifc;
import nds.util.*;
import java.util.*;
/**
 * Factory for retrieveing VendorAPIManager
 * Currently only alisoft supported
 * 
 * @author yfzhu
 *
 */
public class VendorAPIManagerFactory implements nds.util.DestroyListener{
	/**
	 * Vendor tag
	 */
	public static final String VENDOR_ALISOFT="alisoft";
	public static final String VENDOR_SAASBB="saasbb";
	
	private PairTable managers=new PairTable();
	private static VendorAPIManagerFactory instance=null;
	private VendorAPIManagerFactory(){
		
	}
	
	public void init(Configurations conf){
		// init alisoft
		nds.saasifc.alisoft.VendorAPIManagerImpl manager= new nds.saasifc.alisoft.VendorAPIManagerImpl();
		manager.init(conf.getConfigurations("alisoft").getProperties());
		managers.put("alisoft",manager);
	}
	public void destroy(){
		managers.clear();
	}
	/**
	 * Currently only "alisoft" supported
	 * @param vendorName
	 * @return
	 */
	public VendorAPIManager getManager(String vendorName){
		
		return (VendorAPIManager)managers.get(vendorName);
	}
	
	public static synchronized VendorAPIManagerFactory getInstance(){
		if(instance==null){
			instance=new VendorAPIManagerFactory();
			Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
			instance.init(conf.getConfigurations("saas"));
		}
		return instance;
	}
}
