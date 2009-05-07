package nds.saasifc.alisoft;

import com.alisoft.sip.sdk.isv.*;
import java.net.*;
import java.util.*;
import java.io.*;
import nds.control.util.ValueHolder;
/**
 * 与 RestConnector 不同的是，能够获取Connection Header的其他部分，而不仅仅是 code/message
 * 
 * 在 taobao.items.onsale.get 1004错误时，服务器返回的 sip_isp_loginurl 在Header里
 * @author yfzhu
 *
 */
public class RestConnector2 {
	private static Properties config;

    /**
     * 默认都进行签名
     * */
    public ValueHolder invoke(String sip_apiname, String apiURL, Map<String, String> params,String method) throws Exception {
       return invoke(sip_apiname,apiURL,params,method,false); 
    }

    /**
     * 免费的api，不需要签名
     * */
    public ValueHolder invokeFree(String sip_apiname, String apiURL, Map<String, String> params,String method) throws Exception {
       return invoke(sip_apiname,apiURL,params,method,true); 
    }

    //需要处理线程安全
    private  ValueHolder invoke(String sip_apiname, String apiURL, Map<String, String> params,String method,boolean isfree) throws Exception {

        if(apiURL== null)
            throw new IllegalArgumentException("apiURL is null");

        //验证系统配置参数
        if(config== null)
            throw new IllegalArgumentException("config is null");

        //sdk做的事情,获取系统参数
        String sip_appkey = config.getProperty(com.alisoft.sip.sdk.isv.Constants.PARAMETER_APPKEY);
        String sip_appsecret = config.getProperty(com.alisoft.sip.sdk.isv.Constants.PARAMETER_APPSECRET);

        if(sip_appkey == null)
            throw new IllegalArgumentException("sip_appkey is null in config");

        if(sip_appsecret== null)
            throw new IllegalArgumentException("sip_appsecret is null in config");

        if(sip_apiname== null)
            throw new IllegalArgumentException("sip_apiname is null");

        if(params== null)
            throw new IllegalArgumentException("params is null");


        if(method==null) method = com.alisoft.sip.sdk.isv.Constants.HTTP_METHOD_GET;

        String sip_timestamp = com.alisoft.sip.sdk.isv.Constants.SIP_TIMESTAMP_FORMATER.format(new Date());
        
        params.put(com.alisoft.sip.sdk.isv.Constants.PARAMETER_APPKEY,sip_appkey);
        params.put(com.alisoft.sip.sdk.isv.Constants.PARAMETER_APINAME,sip_apiname);
        params.put(com.alisoft.sip.sdk.isv.Constants.PARAMETER_TIMESTAMP,sip_timestamp);

        if(!isfree) {
            //作签名
           String sign = SignatureUtil.Signature(params,sip_appsecret);
           params.put(com.alisoft.sip.sdk.isv.Constants.PARAMETER_SIGN,sign);
        }

        return sendRequest(apiURL,params,method);
    }


	// 调用ISP服务
	private ValueHolder sendRequest(String apiURL, Map<String, String> params, String method) throws Exception {

        String queryString = (null == params) ? "" : delimit(params.entrySet(),true	);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
		conn.setRequestMethod(method);
		conn.setDoOutput(true);
		conn.connect();
		conn.getOutputStream().write(queryString.getBytes());
		String charset = this.getChareset(conn.getContentType());
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
		StringBuffer buffer = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		reader.close();
        SipStatus status =SipStatus.getStatus( conn.getHeaderField(com.alisoft.sip.sdk.isv.Constants.HTTP_SIP_STATUS));
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
		if(nds.util.Validator.isNull(msg)){
			vh.put("sip_isp_loginurl",  conn.getHeaderField("sip_isp_loginurl"));
			/*Map map=conn.getHeaderFields();
			for( Iterator it=map.keySet().iterator();it.hasNext();){
				String key=(String)it.next();
				buffer.append( key+"=="+conn.getHeaderField(key));
				
			}*/
		}
        //SipResult result = new SipResult(SipStatus.getStatus(code),buffer.toString());

        return vh;
	}

	private String getChareset(String contentType) {
		int i = contentType == null ? -1 : contentType.indexOf("charset=");
		return i == -1 ? "UTF-8" : contentType.substring(i + 8);
	}

	// 生成querystring
	private String delimit(Collection<Map.Entry<String, String>> entries,
			boolean doEncode) {
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
					doEncode ? SipUtil.encodeURL(value) : value);
		}
		return buffer.toString();
	}

    public static Properties getConfig() {
        return config;
    }

    public static void setConfig(Properties prop) {
        config = prop;
    }
}

