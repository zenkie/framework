package nds.rest;
import java.util.*;
import org.json.*;

public class TransactionResponse implements JSONString{
	private String id;
	private int code;
	private String message;
	private Map data;
	private JSONObject joData;
	
	public TransactionResponse(){
		
	}
	public TransactionResponse(String id){
		this.id=id;
	}
	public TransactionResponse(String id,int code, String message){
		this.id=id;
		this.code=code;
		this.message =message;
	}
	public String toJSONString()  {
		try{
			return toJSONObject().toString();
		}catch(Throwable t){
			return "";
		}
	}
	public JSONObject toJSONObject() throws JSONException{
		JSONObject jo=new JSONObject();
		jo.put("id",id);
		jo.put("code",code);
		jo.put("message", message);
		if(data!=null){
			for(Iterator it=data.keySet().iterator();it.hasNext();){
				String key=String.valueOf(it.next());
				jo.put(key, data.get(key));
			}
		}
		if(joData!=null){
			for(Iterator it=joData.keys();it.hasNext();){
				String key=String.valueOf(it.next());
				jo.put(key, joData.get(key));
			}
		}
		return jo;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Map getData() {
		return data;
	}
	/**
	 * will be merged with other property, and priority higher then others
	 * @param jo
	 */
	public void putJSONObject(JSONObject jo){
		this.joData =jo;
	}
	public void addData(String key, Object value){
		if(data==null) data=new HashMap();
		data.put( key, value);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
