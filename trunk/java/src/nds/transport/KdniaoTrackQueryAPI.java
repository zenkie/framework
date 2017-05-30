package nds.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import nds.util.Configurations; 
/**
*
* ����������켣��ʱ��ѯ�ӿ�
*
* @����QQȺ: 456320272
* @see: http://www.kdniao.com/YundanChaxunAPI.aspx
* @copyright: �����п�����ݼ����������޹�˾
*
* DEMO�еĵ���ID��˽Կ���޲���ʹ�ã���ʽ�����뵥��ע���˺�
* ���ճ���500����ѯ������������ҷ������켣�������ͽӿ�
* 
* ID��Key�뵽�������룺http://www.kdniao.com/ServiceApply.aspx
*/

public class KdniaoTrackQueryAPI {
	
	
	private static Configurations conf=(nds.util.Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);
		
		
	//DEMO
//	public static void main(String[] args) {
//		KdniaoTrackQueryAPI api = new KdniaoTrackQueryAPI();
//		try {
//			String result = api.getOrderTracesByJson("ANE", "210001633605");
//			System.out.print(result);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	//����ID
	private String EBusinessID=conf.getProperty("wl.EBusinessID","1271357");
	//���̼���˽Կ��������ṩ��ע�Ᵽ�ܣ���Ҫй©
	//private String AppKey="f056bcb5-0c7c-47f8-8e31-b8338b78b24e";
	
	private String AppKey=conf.getProperty("wl.AppKey", "f056bcb5-0c7c-47f8-8e31-b8338b78b24e");//"f056bcb5-0c7c-47f8-8e31-b8338b78b24e";
	//����url
	private String ReqURL=conf.getProperty("wl.url","http://api.kdniao.cc/Ebusiness/EbusinessOrderHandle.aspx");	

	/**
    * Json��ʽ ��ѯ���������켣
	 * @throws Exception 
    */
	public String getOrderTracesByJson(String expCode, String expNo) throws Exception{
		String requestData= "{'OrderCode':'','ShipperCode':'" + expCode + "','LogisticCode':'" + expNo + "'}";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("RequestData", urlEncoder(requestData, "UTF-8"));
		params.put("EBusinessID", EBusinessID);
		params.put("RequestType", "1002");
		String dataSign=encrypt(requestData, AppKey, "UTF-8");
		params.put("DataSign", urlEncoder(dataSign, "UTF-8"));
		params.put("DataType", "2");
		
		String result=sendPost(ReqURL, params);	
		
		//���ݹ�˾ҵ�����ص���Ϣ......
		
		return result;
	}

	/**
    * MD5����
    * @param str ����       
    * @param charset ���뷽ʽ
	 * @throws Exception 
    */
	@SuppressWarnings("unused")
	private String MD5(String str, String charset) throws Exception {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(str.getBytes(charset));
	    byte[] result = md.digest();
	    StringBuffer sb = new StringBuffer(32);
	    for (int i = 0; i < result.length; i++) {
	        int val = result[i] & 0xff;
	        if (val <= 0xf) {
	            sb.append("0");
	        }
	        sb.append(Integer.toHexString(val));
	    }
	    return sb.toString().toLowerCase();
	}
	
	/**
    * base64����
    * @param str ����       
    * @param charset ���뷽ʽ
	 * @throws UnsupportedEncodingException 
    */
	private String base64(String str, String charset) throws UnsupportedEncodingException{
		String encoded = base64Encode(str.getBytes(charset));
		return encoded;    
	}	
	
	@SuppressWarnings("unused")
	private String urlEncoder(String str, String charset) throws UnsupportedEncodingException{
		String result = URLEncoder.encode(str, charset);
		return result;
	}
	
	/**
    * ����Signǩ������
    * @param content ����   
    * @param keyValue Appkey  
    * @param charset ���뷽ʽ
	 * @throws UnsupportedEncodingException ,Exception
	 * @return DataSignǩ��
    */
	@SuppressWarnings("unused")
	private String encrypt (String content, String keyValue, String charset) throws UnsupportedEncodingException, Exception
	{
		if (keyValue != null)
		{
			return base64(MD5(content + keyValue, charset), charset);
		}
		return base64(MD5(content, charset), charset);
	}
	
	 /**
    * ��ָ�� URL ����POST����������     
    * @param url ��������� URL    
    * @param params ����Ĳ�������     
    * @return Զ����Դ����Ӧ���
    */
	@SuppressWarnings("unused")
	private String sendPost(String url, Map<String, String> params) {
       OutputStreamWriter out = null;
       BufferedReader in = null;        
       StringBuilder result = new StringBuilder(); 
       try {
           URL realUrl = new URL(url);
           HttpURLConnection conn =(HttpURLConnection) realUrl.openConnection();
           // ����POST�������������������
           conn.setDoOutput(true);
           conn.setDoInput(true);
           // POST����
           conn.setRequestMethod("POST");
           // ����ͨ�õ���������
           conn.setRequestProperty("accept", "*/*");
           conn.setRequestProperty("connection", "Keep-Alive");
           conn.setRequestProperty("user-agent",
                   "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
           conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
           conn.connect();
           // ��ȡURLConnection�����Ӧ�������
           out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
           // �����������            
           if (params != null) {
		          StringBuilder param = new StringBuilder(); 
		          for (Map.Entry<String, String> entry : params.entrySet()) {
		        	  if(param.length()>0){
		        		  param.append("&");
		        	  }	        	  
		        	  param.append(entry.getKey());
		        	  param.append("=");
		        	  param.append(entry.getValue());		        	  
		        	  //System.out.println(entry.getKey()+":"+entry.getValue());
		          }
		          //System.out.println("param:"+param.toString());
		          out.write(param.toString());
           }
           // flush������Ļ���
           out.flush();
           // ����BufferedReader����������ȡURL����Ӧ
           in = new BufferedReader(
                   new InputStreamReader(conn.getInputStream(), "UTF-8"));
           String line;
           while ((line = in.readLine()) != null) {
               result.append(line);
           }
       } catch (Exception e) {            
           e.printStackTrace();
       }
       //ʹ��finally�����ر��������������
       finally{
           try{
               if(out!=null){
                   out.close();
               }
               if(in!=null){
                   in.close();
               }
           }
           catch(IOException ex){
               ex.printStackTrace();
           }
       }
       return result.toString();
   }
	
	
   private static char[] base64EncodeChars = new char[] { 
       'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
       'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 
       'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
       'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
       'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
       'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
       'w', 'x', 'y', 'z', '0', '1', '2', '3', 
       '4', '5', '6', '7', '8', '9', '+', '/' }; 
	
   public static String base64Encode(byte[] data) { 
       StringBuffer sb = new StringBuffer(); 
       int len = data.length; 
       int i = 0; 
       int b1, b2, b3; 
       while (i < len) { 
           b1 = data[i++] & 0xff; 
           if (i == len) 
           { 
               sb.append(base64EncodeChars[b1 >>> 2]); 
               sb.append(base64EncodeChars[(b1 & 0x3) << 4]); 
               sb.append("=="); 
               break; 
           } 
           b2 = data[i++] & 0xff; 
           if (i == len) 
           { 
               sb.append(base64EncodeChars[b1 >>> 2]); 
               sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]); 
               sb.append(base64EncodeChars[(b2 & 0x0f) << 2]); 
               sb.append("="); 
               break; 
           } 
           b3 = data[i++] & 0xff; 
           sb.append(base64EncodeChars[b1 >>> 2]); 
           sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]); 
           sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]); 
           sb.append(base64EncodeChars[b3 & 0x3f]); 
       } 
       return sb.toString(); 
   }
}
