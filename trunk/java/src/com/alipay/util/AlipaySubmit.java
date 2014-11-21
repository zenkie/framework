package com.alipay.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jaxen.*;

import com.alipay.config.AlipayConfig;
import com.alipay.util.httpClient.HttpProtocolHandler;
import com.alipay.util.httpClient.HttpRequest;
import com.alipay.util.httpClient.HttpResponse;
import com.alipay.util.httpClient.HttpResultType;
import com.alipay.sign.MD5;
import com.alipay.sign.RSA;

/* *
 *������AlipaySubmit
 *���ܣ�֧�������ӿ������ύ��
 *��ϸ������֧�������ӿڱ�HTML�ı�����ȡԶ��HTTP����
 *�汾��3.3
 *���ڣ�2012-08-13
 *˵����
 *���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
 *�ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο���
 */

public class AlipaySubmit {

	
	private static AlipayConfig aliconf;
	
	
	 public  void setAlipayConfig(AlipayConfig aliconf) {
		 this.aliconf=aliconf;
	 }
	
	/**
	 * ����ǩ�����
	 * 
	 * @param sPara
	 *            Ҫǩ��������
	 * @return ǩ������ַ���
	 */
	public static String buildRequestMysign(Map<String, String> sPara) {
		String prestr = AlipayCore.createLinkString(sPara); // ����������Ԫ�أ����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
		String mysign = "";
		if (aliconf.sign_type.equals("MD5")) {
			mysign = MD5.sign(prestr, aliconf.getKey(),AlipayConfig.input_charset);
		}
		if (aliconf.sign_type.equals("0001")) {
			mysign = RSA.sign(prestr, AlipayConfig.private_key,AlipayConfig.input_charset);
		}
		return mysign;
	}

	/**
	 * ����Ҫ�����֧�����Ĳ�������
	 * 
	 * @param sParaTemp
	 *            ����ǰ�Ĳ�������
	 * @return Ҫ����Ĳ�������
	 */
	private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp) {
		// ��ȥ�����еĿ�ֵ��ǩ������
		Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
		// ����ǩ�����
		String mysign = buildRequestMysign(sPara);

		// ǩ�������ǩ����ʽ���������ύ��������
		sPara.put("sign", mysign);
		if (!sPara.get("service").equals("alipay.wap.trade.create.direct") 
			&& !sPara.get("service").equals("alipay.wap.auth.authAndExecute")) {
			sPara.put("sign_type", aliconf.sign_type);
		}

		return sPara;
	}

	/**
	 * ���������Ա�HTML��ʽ���죨Ĭ�ϣ�
	 * 
	 * @paramALIPAY_GATEWAY_NEW ֧�������ص�ַ
	 * @param sParaTemp
	 *            �����������
	 * @param strMethod
	 *            �ύ��ʽ������ֵ��ѡ��post��get
	 * @param strButtonName
	 *            ȷ�ϰ�ť��ʾ����
	 * @return �ύ��HTML�ı�
	 */
	public static String buildRequest(String ALIPAY_GATEWAY_NEW,Map<String, String> sParaTemp, String strMethod,String strButtonName) {
		// �������������
		Map<String, String> sPara = buildRequestPara(sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\""
				+ ALIPAY_GATEWAY_NEW
				+ "_input_charset="
				+ AlipayConfig.input_charset
				+ "\" method=\""
				+ strMethod
				+ "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);

			sbHtml.append("<input type=\"hidden\" name=\"" + name
					+ "\" value=\"" + value + "\"/>");
		}

		// submit��ť�ؼ��벻Ҫ����name����
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName
				+ "\" style=\"display:none;\"></form>");
		sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

		return sbHtml.toString();
	}

	/**
	 * ���������Ա�HTML��ʽ���죬���ļ��ϴ�����
	 * 
	 * @paramALIPAY_GATEWAY_NEW ֧�������ص�ַ
	 * @param sParaTemp
	 *            �����������
	 * @param strMethod
	 *            �ύ��ʽ������ֵ��ѡ��post��get
	 * @param strButtonName
	 *            ȷ�ϰ�ť��ʾ����
	 * @param strParaFileName
	 *            �ļ��ϴ��Ĳ�����
	 * @return �ύ��HTML�ı�
	 */
	public static String buildRequest(String ALIPAY_GATEWAY_NEW,Map<String, String> sParaTemp, String strMethod,String strButtonName, String strParaFileName) {
		// �������������
		Map<String, String> sPara = buildRequestPara(sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\"  enctype=\"multipart/form-data\" action=\""
				+ ALIPAY_GATEWAY_NEW
				+ "_input_charset="
				+ AlipayConfig.input_charset
				+ "\" method=\""
				+ strMethod
				+ "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);

			sbHtml.append("<input type=\"hidden\" name=\"" + name
					+ "\" value=\"" + value + "\"/>");
		}

		sbHtml.append("<input type=\"file\" name=\"" + strParaFileName
				+ "\" />");

		// submit��ť�ؼ��벻Ҫ����name����
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName
				+ "\" style=\"display:none;\"></form>");

		return sbHtml.toString();
	}

	/**
	 * ����������ģ��Զ��HTTP��POST����ʽ���첢��ȡ֧�����Ĵ�����
	 * ����ӿ���û���ϴ��ļ���������ôstrParaFileName��strFilePath����Ϊ��ֵ �磺buildRequest("",
	 * "",sParaTemp)
	 * 
	 * @paramALIPAY_GATEWAY_NEW ֧�������ص�ַ
	 * @param strParaFileName
	 *            �ļ����͵Ĳ�����
	 * @param strFilePath
	 *            �ļ�·��
	 * @param sParaTemp
	 *            �����������
	 * @return ֧����������
	 * @throws Exception
	 */
	public static String buildRequest(String ALIPAY_GATEWAY_NEW,String strParaFileName, String strFilePath,Map<String, String> sParaTemp) throws Exception {
		// �������������
		Map<String, String> sPara = buildRequestPara(sParaTemp);

		HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler.getInstance();

		HttpRequest request = new HttpRequest(HttpResultType.BYTES);
		// ���ñ��뼯
		request.setCharset(AlipayConfig.input_charset);

		request.setParameters(generatNameValuePair(sPara));
		request.setUrl(ALIPAY_GATEWAY_NEW + "_input_charset="+ AlipayConfig.input_charset);

		HttpResponse response = httpProtocolHandler.execute(request,strParaFileName, strFilePath);
		if (response == null) {
			return null;
		}

		String strResult = response.getStringResult();

		return strResult;
	}

	/**
	 * MAP��������ת����NameValuePair����
	 * 
	 * @param properties
	 *            MAP��������
	 * @return NameValuePair��������
	 */
	private static NameValuePair[] generatNameValuePair(
			Map<String, String> properties) {
		NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			nameValuePair[i++] = new NameValuePair(entry.getKey(),
					entry.getValue());
		}

		return nameValuePair;
	}

	/**
	 * ����Զ��ģ���ύ�󷵻ص���Ϣ�����token
	 * 
	 * @param text
	 *            Ҫ�������ַ���
	 * @return �������
	 * @throws Exception
	 */
	public static String getRequestToken(String text) throws Exception {
		String request_token = "";
		// �ԡ�&���ַ��и��ַ���
		String[] strSplitText = text.split("&");
		// ���и����ַ��������ɱ�������ֵ��ϵ��ֵ�����
		Map<String, String> paraText = new HashMap<String, String>();
		for (int i = 0; i < strSplitText.length; i++) {

			// ��õ�һ��=�ַ���λ��
			int nPos = strSplitText[i].indexOf("=");
			// ����ַ�������
			int nLen = strSplitText[i].length();
			// ��ñ�����
			String strKey = strSplitText[i].substring(0, nPos);
			// �����ֵ
			String strValue = strSplitText[i].substring(nPos + 1, nLen);
			// ����MAP����
			paraText.put(strKey, strValue);
		}

		//System.out.print("aaa");
		if (paraText.get("res_data") != null) {
			String res_data = paraText.get("res_data");
			// �������ܲ����ַ�����RSA��MD5�������һ�䣩
			//if (AlipayConfig.sign_type.equals("0001")) {
			//	res_data = RSA.decrypt(res_data, AlipayConfig.private_key,
				//		AlipayConfig.input_charset);
			//}

			// token��res_data�н���������Ҳ����˵res_data���Ѿ�����token�����ݣ�
			Document document = DocumentHelper.parseText(res_data);
			request_token = document.selectSingleNode("//direct_trade_create_res/request_token").getText();
			//System.out.print("aaa");
		}
		return request_token;
	}

	/**
	 * ���ڷ����㣬���ýӿ�query_timestamp����ȡʱ����Ĵ����� ע�⣺Զ�̽���XML������������Ƿ�֧��SSL�������й�
	 * 
	 * @return ʱ����ַ���
	 * @throws IOException
	 * @throws DocumentException
	 * @throws MalformedURLException
	 */
	public static String query_timestamp() throws MalformedURLException,
			DocumentException, IOException {

		// �������query_timestamp�ӿڵ�URL��
		String strUrl = "https://mapi.alipay.com/gateway.do?service=query_timestamp&partner="
				+ aliconf.getPartner();
		StringBuffer result = new StringBuffer();

		SAXReader reader = new SAXReader();
		Document doc = reader.read(new URL(strUrl).openStream());

		List<Node> nodeList = doc.selectNodes("//alipay/*");

		for (Node node : nodeList) {
			// ��ȡ���ֲ���Ҫ��������Ϣ
			if (node.getName().equals("is_success")
					&& node.getText().equals("T")) {
				// �ж��Ƿ��гɹ���ʾ
				List<Node> nodeList1 = doc
						.selectNodes("//response/timestamp/*");
				for (Node node1 : nodeList1) {
					result.append(node1.getText());
				}
			}
		}

		return result.toString();
	}
	
	public static void main(String[] args) throws Exception {
		
		String pdata="res_data=<?xml version=\"1.0\" encoding=\"utf-8\"?><direct_trade_create_res><request_token>2014070172ff41fa3c4b1f0da2c1308820e5cd49</request_token></direct_trade_create_res>&service=alipay.wap.trade.create.direct&sec_id=MD5&partner=2088101568358171&req_id=20140701161408&sign=bc969cb88918de34c0380003c9f65afc&v=2.0";
		String data=getRequestToken(pdata);
		System.out.print(data);
	
	}
}
