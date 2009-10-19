package nds.rest;
import nds.util.*;
import nds.control.util.*;
import nds.security.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class RestUtils {
	public static ValueHolder sendRequest(String apiURL, Map<String, String> params, String method) throws Exception {

        String queryString = (null == params) ? "" : delimit(params.entrySet(),true	);
		
        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
		conn.setRequestMethod(method);
		conn.setDoOutput(true);
		conn.connect();
		conn.getOutputStream().write(queryString.getBytes());
		String charset = getChareset(conn.getContentType());
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		reader.close();
        SipStatus status =SipStatus.getStatus( conn.getHeaderField("sip_status"));
        conn.disconnect();
		String msg= buffer.toString();
		ValueHolder vh=new ValueHolder();

		int code;
		if(status.equals( SipStatus.SUCCESS)){
			code=0;
		}else{
			code=- nds.util.Tools.getInt(status.getCode(),1);
			if(code==0){
				// SipStatus.ERROR =0
				code=-1;
			}
		}
		
		vh.put("code",String.valueOf(code));
		vh.put("message", msg);
		vh.put("queryString",queryString );
        //SipResult result = new SipResult(SipStatus.getStatus(code),buffer.toString());

        return vh;
	}

	private static String getChareset(String contentType) {
		int i = contentType == null ? -1 : contentType.indexOf("charset=");
		return i == -1 ? "UTF-8" : contentType.substring(i + 8);
	}

	// Éú³Équerystring
	public  static String delimit(Collection<Map.Entry<String, String>> entries,
			boolean doEncode) throws Exception {
		if (entries == null || entries.isEmpty()) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		boolean notFirst = false;
		for (Map.Entry<String, ?> entry : entries) {
			if (notFirst) {
				buffer.append("&");
			} else {
				notFirst = true;
			}
			Object value = entry.getValue();
			buffer.append(entry.getKey()).append("=").append(
					doEncode ? URLEncoder.encode(value.toString(), "UTF8") : value);
		}
		return buffer.toString();
	}
}
