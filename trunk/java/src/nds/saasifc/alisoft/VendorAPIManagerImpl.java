package nds.saasifc.alisoft;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nds.control.util.ValueHolder;
import nds.saasifc.*;
import nds.util.*;
import com.alisoft.sip.sdk.isv.*;


public class VendorAPIManagerImpl implements VendorAPIManager {
	private static final Log logger = LogFactory.getLog(VendorAPIManagerImpl.class);	
	
	private String appSecret;

	private String appKey;

	private String apiUrl;

	/**
	 * Single sign on support
	 * @param parameters deferent implementation has deferent parameter 
	 * @return true if has signed on
	 */
	public boolean hasSignedOn(String usrId, Map parameters){
		//is valid? 
		ValueHolder vh = invokeAPI("alisoft.validateUser",parameters);
		if(Tools.getInt( vh.get("code"), -1)!=0) return false;
		String message=(String)vh.get("message");
		logger.debug("check for user "+ usrId+", content:"+ message);
		int p=(message.indexOf("<String>"));
		int pe=message.indexOf("</String>");
		if(p>0){
			String value= message.substring(p+"<String>".length(), pe);
			int c=Tools.getInt(value, -1);
			if(c ==1 || c==0) return true;
		}
		return false;
	}
	/**
	 * Call vendor api 
	 * @param apiName
	 * @param parameters 
	 * @return code <> 0 for error with message set in "message"
	 */
	public ValueHolder invokeAPI(String apiName,Map parameters) {
		SipResult ret = null;
		
		// 
		Properties prop = new Properties();
		prop.setProperty(Constants.PARAMETER_APPKEY, appKey);
		prop.setProperty(Constants.PARAMETER_APPSECRET, appSecret);
		RestConnector2.setConfig(prop);
		ValueHolder vh;
		RestConnector2 rc = new RestConnector2();
		try {
			vh = rc.invoke(apiName, apiUrl, parameters, "POST");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			vh=new ValueHolder();
			vh.put("code","-1");
			vh.put("message", e.getMessage());
		}
		return vh;
		/*ValueHolder vh=new ValueHolder();
		int code;
		if(ret.getStatus().equals( SipStatus.SUCCESS)){
			code=0;
		}else{
			code=-Tools.getInt(ret.getStatus().getCode(),1);
			if(code==0){
				// SipStatus.ERROR =0
				code=-1;
			}
		}
		
		
		String message= ret.getContent();
		vh.put("code", String.valueOf(code));
		vh.put("message",message);
		return vh;*/
	}
	
	/**
	 * Init with parameters
	 * @param props
	 */
	public void init(Properties props){
		appSecret= props.getProperty("appsecret");
		appKey= props.getProperty("appkey");
		apiUrl= props.getProperty("appurl");
		if(appSecret==null ||appKey==null ||apiUrl==null )
				logger.warn("Properties not set for alisoft api, need appsecret,appkey and appurl");
	}
}
