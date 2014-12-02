package com.wxap.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;

import nds.log.Logger;
import nds.log.LoggerManager;

public class SignatureUtil {
	private static Logger logger=LoggerManager.getInstance().getLogger(SignatureUtil.class.getName());
	
	private String appId;
	public void setAppId(String id) {
		this.appId=id;
	}
	
	private String appKey;
	public void setAppKey(String key) {
		this.appKey=key;
	}

	private String appSecret;
	public void setAppSecret(String secret) {
		this.appSecret=secret;
	}
	
	private String partnerKey;
	public void setPartnerKey(String partnerkey) {
		this.partnerKey=partnerkey;
	}
	
	private String charset;
	public void setCharset(String charset) {
		this.charset=charset;
	}
	
	public String getCharset() {
		if(nds.util.Validator.isNull(charset)) {return "UTF-8";}
		else {return this.charset;}
	}
	
	public void init(String appid,String appkey,String appsecret,String partnerkey) {
		this.appId=appid;
		this.appKey=appkey;
		this.appSecret=appsecret;
		this.partnerKey=partnerkey;
	}
	
	/**
	 * ǩ������
	 * @param param Ҫǩ��������
	 * @param isSort �Ƿ�����
	 * @param isFiltNull ǩ��ʱ�Ƿ���˿�ֵ(true:���˿�ֵ����������ǩ��,false:�����˼���ֵ����ǩ��)
	 * @return
	 */
	public String doSignature(Map param, boolean isSort, boolean isFiltNull) {
		String signatureString=null;
		if(param==null||param.isEmpty()) {
			logger.debug("signature is null");
			return signatureString;
		}
		
		Map<String,String> signatureMap=null;
		if(isSort) {
			signatureMap=doSort(param,isFiltNull);
		}else {
			signatureMap=param;
		}
		
		String signStr=createSignatureString(signatureMap,isFiltNull);
		logger.debug("before md5 parmas is->"+signStr);
		System.out.println("before md5 parmas is->"+signStr);
		signatureString=DigestUtils.md5Hex(getContentBytes(signStr,getCharset()));
		if(nds.util.Validator.isNotNull(signatureString)) {signatureString=signatureString.toUpperCase();}
		logger.debug("after md5 params is->"+signatureString);
		System.out.println("after md5 params is->"+signatureString);
		
		return signatureString;
	}
	
	/**
	 * ����ǩ���ַ���
	 * @param param
	 * @param isFiltNull �Ƿ���˵���ֵ����
	 * @return
	 */
	public String createSignatureString(Map param, boolean isFiltNull) {
		Object value;
		StringBuffer sb = new StringBuffer();
		if(param==null||param.isEmpty()) {return sb.toString();}
		for(Object key:param.keySet()) {
			value=param.get(key);
			if(isFiltNull) {if(value==null||nds.util.Validator.isNull(String.valueOf(value))) {continue;}}
			if("sign".equalsIgnoreCase(String.valueOf(key))) {continue;}
			sb.append(key + "=" + value + "&");
		}
		sb.append("key="+this.partnerKey);
		
		return sb.toString();
	}
	
	/**
	 * ��������
	 * @param param			Ҫ���������
	 * @param isFiltNull	�Ƿ���˵���ֵ����
	 * @return
	 */
	public Map doSort(Map<String,?> param,boolean isFiltNull) {
		Map sortMap=null;
		if(param==null||param.isEmpty()) {return sortMap;}
		Object value;
		sortMap=new TreeMap<String,String>();
		for(String key:param.keySet()){
			value=param.get(key);
			if(isFiltNull&&(value==null||nds.util.Validator.isNull(String.valueOf(value)))) {continue;}
			sortMap.put(key, value);
		}
		
		return sortMap;
	}
	
	/**
	 * ���˿�ֵ
	 * @param param
	 * @return
	 */
	public Map<String,String> doFiltNull(Map<String,String> param){
		Map<String,String> filtMap=null;
		if(param==null||param.isEmpty()) {return filtMap;}
		
		String value=null;
		filtMap=new LinkedHashMap<String,String>();
		for(String key:param.keySet()) {
			value=param.get(key);
			if(nds.util.Validator.isNull(value)) {continue;}
			filtMap.put(key, value);
		}
		
		return filtMap;
	}

	private byte[] getContentBytes(String content, String charset) {
		if (charset == null || "".equals(charset)) {
			return content.getBytes();
		}
		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("MD5ǩ�������г��ִ���,ָ���ı��뼯����,��Ŀǰָ���ı��뼯��:"+ charset);
		}
	}

	// ��ȡpackage��ǩ����
	public String genPackage(SortedMap<String, String> packageParams)
			throws UnsupportedEncodingException {
		String sign = createSign(packageParams);

		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			sb.append(k + "=" + UrlEncode(v) + "&");
		}

		// ȥ�����һ��&
		String packageValue = sb.append("sign=" + sign).toString();
		System.out.println("packageValue=" + packageValue);
		return packageValue;
	}
	
	/**
	 * ����md5ժҪ,������:����������a-z����,������ֵ�Ĳ������μ�ǩ����
	 */
	public String createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + this.partnerKey);
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), this.getCharset())
				.toUpperCase();

		return sign;
	}
	
	public boolean verifySignature(Map param,boolean isSort,boolean isFiltNull) {
		boolean isVerify=false;
		String signatureString=doSignature(param,isSort,isFiltNull);
		String sign=String.valueOf(param.get("sign"));
		isVerify=sign.equals(signatureString);
		
		return isVerify;
	}
	
	public boolean verifySignature(String xml,boolean isSort,boolean isFiltNull) {
		boolean isVerify=false;
		Map m=null;
		try {
			m = XMLUtil.doXMLParse(xml);
		}catch (Exception e) {
			e.printStackTrace();
		}
		isVerify=verifySignature(m,isSort,isFiltNull);
		
		return isVerify;
	}
	
	public boolean verifySignature(JSONObject jo,boolean isSort,boolean isFiltNull) {
		boolean isVerify=false;
		Map m=jsonToMap(jo,isFiltNull);
		isVerify=verifySignature(m,isSort,isFiltNull);
		
		return isVerify;
	}
	
	// �����ַ�����
	public String UrlEncode(String src) throws UnsupportedEncodingException {
		return URLEncoder.encode(src, getCharset()).replace("+", "%20");
	}
	
	public JSONObject mapToJson(Map param,boolean isFiltNull) {
		JSONObject rjo=new JSONObject();
		
		Object key;
		Object value;
		Iterator<String> iter = param.keySet().iterator();
	
		try {
			while(iter.hasNext()) {
				key=iter.next();
				value=param.get(key);
				if(isFiltNull) {
					if(value==null) {continue;}
					if(value instanceof String) {
						if(nds.util.Validator.isNull(String.valueOf(value))) {continue;}
					}
				}
				rjo.put(String.valueOf(key), param.get(key));
			}
		}catch(Exception e) {
			logger.debug("map to json error->"+e.getLocalizedMessage());
		}
		return rjo;
	}
	
	public Map jsonToMap(JSONObject param,boolean isFiltNull) {
		Map m=new HashMap();
		if(param==null) {return m;}
		
		String key;
		Iterator<String> iter = param.keys();
		while(iter.hasNext()) {
			key=iter.next();
			try {
				m.put(key, param.get(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return m;
	}
}
