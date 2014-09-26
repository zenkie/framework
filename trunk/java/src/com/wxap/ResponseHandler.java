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
 * 微信支付服务器签名支付请求应答类
 * api说明： 
 *  getKey()/setKey(),获取/设置密钥
 *  getParameter()/setParameter(),获取/设置参数值 getAllParameters(),获取所有参数
 *  isTenpaySign(),是否财付通签名,true:是 false:否
 *   getDebugInfo(),获取debug信息
 */
public class ResponseHandler {

	private String appkey = null;

	/** 密钥 */
	private String key;

	/** 应答的参数 */
	private SortedMap parameters;
	
	private SortedMap poseData;

	/** debug信息 */
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
	 * 构造函数
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
	 *获取密钥
	 */
	public String getKey() {
		return key;
	}

	/**
	 *设置密钥
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
	 * 获取参数值
	 * 
	 * @param parameter
	 *            参数名称
	 * @return String
	 */
	public String getParameter(String parameter) {
		String s = (String) this.parameters.get(parameter);
		return (null == s) ? "" : s;
	}

	/**
	 * 设置参数值
	 * 
	 * @param parameter
	 *            参数名称
	 * @param parameterValue
	 *            参数值
	 */
	public void setParameter(String parameter, String parameterValue) {
		String v = "";
		if (null != parameterValue) {
			v = parameterValue.trim();
		}
		this.parameters.put(parameter, v);
	}
	
	/**
	 * 设置POST参数
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
	 * 获取POST参数
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
	 * 返回所有的参数
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
		
		//解析xml,得到map
		Map m;
		try {
			m = XMLUtil.doXMLParse(result);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//设置参数
		Iterator it = m.keySet().iterator();
		while(it.hasNext()) {
			String k = (String) it.next();
			String v = (String) m.get(k);
			this.setPostdata(k, v);
		}
	}
	
	public void doParse(String xmlContent) throws JDOMException, IOException {
		this.parameters.clear();
		//解析xml,得到map
		Map m = XMLUtil.doXMLParse(xmlContent);
		
		//设置参数
		Iterator it = m.keySet().iterator();
		while(it.hasNext()) {
			String k = (String) it.next();
			String v = (String) m.get(k);
			this.setParameter(k, v);
		}
	}
	
	/**
	 * 是否财付通签名,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
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

		// 算出摘要
		String enc = TenpayUtil.getCharacterEncoding(this.request,this.response);
		String sign = MD5Util.MD5Encode(sb.toString(), enc).toLowerCase();

		String ValidSign = this.getParameter("sign").toLowerCase();

		// debug信息
		System.out.println(sb.toString() + " => sign:" + sign + " ValidSign:" + ValidSign);
		this.setDebugInfo(sb.toString() + " => sign:" + sign + " ValidSign:" + ValidSign);

		return ValidSign.equals(sign);
	}
	
	/**
	 * 判断微信签名
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
	
	//判断微信维权签名
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
	 * 返回处理结果给财付通服务器。
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
	 * 获取uri编码
	 * 
	 * @return String
	 */
	public String getUriEncoding() {
		return uriEncoding;
	}

	/**
	 * 设置uri编码
	 * 
	 * @param uriEncoding
	 * @throws UnsupportedEncodingException
	 */
	public void setUriEncoding(String uriEncoding)
			throws UnsupportedEncodingException {
		if (!"".equals(uriEncoding.trim())) {
			this.uriEncoding = uriEncoding;

			// 编码转换
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
	 *获取debug信息
	 */
	public String getDebugInfo() {
		return debugInfo;
	}
	
	/**
	 *设置debug信息
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
