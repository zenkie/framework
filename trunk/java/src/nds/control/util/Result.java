package nds.control.util;

import java.util.Map;
import org.json.*;
import nds.util.NDSRuntimeException;
/**
 * Used for remote client of dwr
 * 
 * @author yfzhu@agilecontrol.com
 */
public class Result implements  JSONString{
	
	private int code;
	private String message;
	private Object data;
	private String callBackEvent; // event name for client to dispatch
	public Result(){
		code=0;
		message="";
		
	}
	public int getCode() {
		return code;
	}
	public Object getData() {
		return data;
	}
	public String getMessage() {
		return message;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * This is used by client to dispatch event object to listener,
	 * event name will use this one 
	 * @return
	 */
	public String getCallbackEvent() {
		return callBackEvent;
	}
	public void setCallbackEvent(String eventName) {
		this.callBackEvent = eventName;
	}
	public String toJSONString() {
		JSONObject o=new JSONObject();
		try{
			o.put("code", code);
			o.put("message", message);
			o.put("data", data);
			o.put("callbackEvent", callBackEvent);
		}catch(Throwable t){
			throw new NDSRuntimeException("Fail to convert to json:"+ t,t);
		}
		return o.toString();
	}
}