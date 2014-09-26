package com.wxap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.wxap.util.Sha1Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.JDOMException;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
import com.wxap.util.HttpClientUtil;
//import com.sun.org.apache.xalan.internal.xsltc.runtime.*;
import com.wxap.util.MD5Util;
import com.wxap.util.TenpayUtil;
import com.wxap.util.XMLUtil;


/**
 * ΢��֧��������ǩ��֧������Ӧ����
 * api˵���� 
 *  getKey()/setKey(),��ȡ/������Կ
 *  getParameter()/setParameter(),��ȡ/���ò���ֵ getAllParameters(),��ȡ���в���
 *  isTenpaySign(),�Ƿ�Ƹ�ͨǩ��,true:�� false:��
 *   getDebugInfo(),��ȡdebug��Ϣ
 */
public class ResponseHandler {

	private String appkey = null;

	/** ��Կ */
	private String key;

	/** Ӧ��Ĳ��� */
	private SortedMap parameters;
	
	private SortedMap poseData;

	/** debug��Ϣ */
	private String debugInfo;

	private HttpServletRequest request;

	private HttpServletResponse response;

	private String uriEncoding;
	
	 private Hashtable xmlMap;

	private String k;
	
	public ResponseHandler() {
		this.doPase();
	}

	/**
	 * ���캯��
	 * 
	 * @param request
	 * @param response
	 */
	public ResponseHandler(HttpServletRequest request,HttpServletResponse response) {
		this.request = request;
		this.response = response;

		this.key = "";
		this.parameters = new TreeMap();
		this.poseData=new TreeMap();
		this.debugInfo = "";

		this.uriEncoding = "";

		Map m = this.request.getParameterMap();
		Iterator it = m.keySet().iterator();
		while (it.hasNext()) {
			String k = (String) it.next();
			String v = ((String[]) m.get(k))[0];
			this.setParameter(k, v);
		}
		this.doPase();
	}

	/**
	 *��ȡ��Կ
	 */
	public String getKey() {
		return key;
	}

	/**
	 *������Կ
	 */
	public void setKey(String key) {
		this.key = key;
	}

	public void setAppKey(String appkey) {
		this.appkey=appkey;
	}
	
	public String getAppKey() {
		return this.appkey;
	}
	
	/**
	 * ��ȡ����ֵ
	 * 
	 * @param parameter
	 *            ��������
	 * @return String
	 */
	public String getParameter(String parameter) {
		String s = (String) this.parameters.get(parameter);
		return (null == s) ? "" : s;
	}

	/**
	 * ���ò���ֵ
	 * 
	 * @param parameter
	 *            ��������
	 * @param parameterValue
	 *            ����ֵ
	 */
	public void setParameter(String parameter, String parameterValue) {
		String v = "";
		if (null != parameterValue) {
			v = parameterValue.trim();
		}
		this.parameters.put(parameter, v);
	}
	
	/**
	 * ����POST����
	 * @param parameter
	 * @param parameterValue
	 */
	public void setPostdata(String parameter, String parameterValue) {
		String v = "";
		if (null != parameterValue) {
			v = parameterValue.trim();
		}
		this.poseData.put(parameter, v);
	}
	
	/**
	 * ��ȡPOST����
	 * @return
	 */
	public Map getAllPostdata() {
		return this.poseData;
	}
	
	public String getPostdata(String parameter) {
		String s = (String) this.poseData.get(parameter);
		return (null == s) ? "" : s;
	}

	/**
	 * �������еĲ���
	 * 
	 * @return SortedMap
	 */
	public SortedMap getAllParameters() {
		return this.parameters;
	}
	
	public void doPase() {
		String charset=request.getCharacterEncoding();
		charset=charset==null?"gbk":charset;
		String result=null;
		System.out.println("weixin pay callback getdata->"+this.request.getQueryString());
		
		try {
			InputStream inputStream = request.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedInputStream bis = null;
			byte[] buf = new byte[1024];
			bis = new BufferedInputStream(inputStream);
			for (int len = 0; (len = bis.read(buf)) != -1;){
				baos.write(buf,0,len);
			}
		    inputStream.close(); 
		    result=baos.toString(charset);
		    
		    System.out.println("weixin pay callback postdata->"+result);
		}catch(Exception e) {
			System.out.println("weixin pay callback postdata error->"+e.getMessage());
		}
		this.poseData.clear();
		
		//����xml,�õ�map
		Map m;
		try {
			m = XMLUtil.doXMLParse(result);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//���ò���
		Iterator it = m.keySet().iterator();
		while(it.hasNext()) {
			String k = (String) it.next();
			String v = (String) m.get(k);
			this.setPostdata(k, v);
		}
	}
	
	public void doParse(String xmlContent) throws JDOMException, IOException {
		this.parameters.clear();
		//����xml,�õ�map
		Map m = XMLUtil.doXMLParse(xmlContent);
		
		//���ò���
		Iterator it = m.keySet().iterator();
		while(it.hasNext()) {
			String k = (String) it.next();
			String v = (String) m.get(k);
			this.setParameter(k, v);
		}
	}
	
	/**
	 * �Ƿ�Ƹ�ͨǩ��,������:����������a-z����,������ֵ�Ĳ������μ�ǩ����
	 * 
	 * @return boolean
	 */
	public boolean isValidSign() {
		StringBuffer sb = new StringBuffer();
		Set es = this.parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		sb.append("key=" + this.getKey());

		// ���ժҪ
		String enc = TenpayUtil.getCharacterEncoding(this.request,this.response);
		String sign = MD5Util.MD5Encode(sb.toString(), enc).toLowerCase();

		String ValidSign = this.getParameter("sign").toLowerCase();

		// debug��Ϣ
		System.out.println(sb.toString() + " => sign:" + sign + " ValidSign:" + ValidSign);
		this.setDebugInfo(sb.toString() + " => sign:" + sign + " ValidSign:" + ValidSign);

		return ValidSign.equals(sign);
	}
	
	/**
	 * �ж�΢��ǩ��
	 */
	public boolean isWXsign(){	
		 StringBuffer sb = new StringBuffer();
		 this.setPostdata("AppKey", this.appkey);
		 
		 Set es = this.poseData.entrySet();
		 Iterator it = es.iterator();
		 while (it.hasNext()){
			 	Map.Entry entry = (Map.Entry) it.next();
				String k = (String) entry.getKey();
				String v = (String) entry.getValue();
			 if (k != "SignMethod" && k != "AppSignature"){
				 if(sb.length()>0) {sb.append("&");}
				 sb.append(k.toLowerCase() + "=" + v);
			 }
		 }

         String sign = Sha1Util.getSha1(sb.toString()).toString().toLowerCase();
         String ValidSign = this.getPostdata("AppSignature").toLowerCase();

         System.out.println(sb.toString() + " =>sign:"+sign+" SHA1 sign:" + ValidSign);
         this.setDebugInfo(sb.toString() + " =>sign:"+sign+" SHA1 sign:" + ValidSign);

         return sign.equals(ValidSign);
       
	}
	
	//�ж�΢��άȨǩ��
	public boolean isWXsignfeedback(){
		
		StringBuffer sb = new StringBuffer();
		 Hashtable signMap = new Hashtable();
		 Set es = this.parameters.entrySet();
		 Iterator it = es.iterator();
		 while (it.hasNext()){
			 	Map.Entry entry = (Map.Entry) it.next();
				String k = (String) entry.getKey();
				String v = (String) entry.getValue();
			 if (k != "SignMethod" && k != "AppSignature"){
				 
				 sb.append(k + "=" + v + "&");
			 }
		 }
		 signMap.put("appkey", this.appkey);
		 
		// ArrayList akeys = new ArrayList();
        // akeys.Sort();
         while (it.hasNext()){ 
             String v = k;
             if (sb.length() == 0)
             {
                 sb.append(k + "=" + v);
             } 
             else
             {
                 sb.append("&" + k + "=" + v);
             }
         }

         String sign = Sha1Util.getSha1(sb.toString()).toString().toLowerCase();

         this.setDebugInfo(sb.toString() + " => SHA1 sign:" + sign);

         return sign.equals("App    Signature");
     }	
		
	/**
	 * ���ش��������Ƹ�ͨ��������
	 * 
	 * @param msg
	 * Success or fail
	 * @throws IOException
	 */
	public void sendToCFT(String msg) throws IOException {
		String strHtml = msg;
		PrintWriter out = this.getHttpServletResponse().getWriter();
		out.println(strHtml);
		out.flush();
		out.close();

	}

	/**
	 * ��ȡuri����
	 * 
	 * @return String
	 */
	public String getUriEncoding() {
		return uriEncoding;
	}

	/**
	 * ����uri����
	 * 
	 * @param uriEncoding
	 * @throws UnsupportedEncodingException
	 */
	public void setUriEncoding(String uriEncoding)
			throws UnsupportedEncodingException {
		if (!"".equals(uriEncoding.trim())) {
			this.uriEncoding = uriEncoding;

			// ����ת��
			String enc = TenpayUtil.getCharacterEncoding(request, response);
			Iterator it = this.parameters.keySet().iterator();
			while (it.hasNext()) {
				String k = (String) it.next();
				String v = this.getParameter(k);
				v = new String(v.getBytes(uriEncoding.trim()), enc);
				this.setParameter(k, v);
			}
		}
	}

	/**
	 *��ȡdebug��Ϣ
	 */
	public String getDebugInfo() {
		return debugInfo;
	}
	
	/**
	 *����debug��Ϣ
	 */
	protected void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}

	protected HttpServletRequest getHttpServletRequest() {
		return this.request;
	}

	protected HttpServletResponse getHttpServletResponse() {
		return this.response;
	}

}
