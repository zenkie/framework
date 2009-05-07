package com.tenpay.helper;

import com.tenpay.bean.PayRequest;
import com.tenpay.util.MD5Util;
import com.tenpay.util.TenpayUtil;

public class PayRequestHelper {

	/** 商家密钥 */
	private String key;
	
	/** 支付请求bean */
	private PayRequest payRequest;
	
	/** 签名信息 */
	private String sign;
	
	/** 目的URL */
	private final String targetURL = 
		"https://www.tenpay.com/cgi-bin/v1.0/pay_gate.cgi";
	
//	private final String targetURL = 
//		"https://www.tenpay.com/cgi-bin/v1.0/vast_pay_gate.cgi";
	
	/** 请求参数串 */
	private String requestParameters;
	
	/**
	 * 构造函数
	 * @param key 商家交易密钥
	 * @param payRequest 支付请求bean
	 */
	public PayRequestHelper(String key, PayRequest payRequest) {
		this.key = key;
		this.payRequest = payRequest;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public PayRequest getPayRequest() {
		return payRequest;
	}

	public void setPayRequest(PayRequest payRequest) {
		this.payRequest = payRequest;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getTargetURL() {
		return targetURL;
	}
	
	/**
	 * 获取发送的URL(包含参数)
	 * @return String
	 */
	public String getSendUrl() {
		this.createRequestParameters();
		return this.targetURL + "?" + this.requestParameters;
	}
	
	protected void createSign() {
		StringBuffer buf = new StringBuffer();
		TenpayUtil.addParameter(buf, "cmdno", payRequest.getCmdno());
		TenpayUtil.addParameter(buf, "date", payRequest.getDate());
		TenpayUtil.addParameter(buf, "bargainor_id", payRequest.getBargainor_id());
		TenpayUtil.addParameter(buf, "transaction_id", payRequest.getTransaction_id());
		TenpayUtil.addParameter(buf, "sp_billno", payRequest.getSp_billno());
		TenpayUtil.addParameter(buf, "total_fee", payRequest.getTotal_fee() + "");
		TenpayUtil.addParameter(buf, "fee_type", payRequest.getFee_type());
		TenpayUtil.addParameter(buf, "return_url", payRequest.getReturn_url());
		TenpayUtil.addParameter(buf, "attach", payRequest.getAttach());
		TenpayUtil.addBusParameter(buf, "spbill_create_ip", payRequest.getSpbill_create_ip());
		
		this.requestParameters = buf.toString();
		
		TenpayUtil.addParameter(buf, "key", this.key);
		
		//生成大写签名串
		this.sign = MD5Util.MD5Encode(buf.toString()).toUpperCase();
		
	}
	
	protected void createRequestParameters() {
		this.createSign();
		StringBuffer buf = new StringBuffer(this.requestParameters);
		TenpayUtil.addParameter(buf, "bank_type", payRequest.getBank_type());
		TenpayUtil.addParameter(buf, "purchaser_id", payRequest.getPurchaser_id());
		
		//商品名先encode再提交,这样可以解决乱码问题
		String encodeDesc = TenpayUtil.URLEncode2GBK(payRequest.getDesc());
		TenpayUtil.addParameter(buf, "desc", encodeDesc);
		TenpayUtil.addParameter(buf, "sign", this.sign);
		
		//组成参数请求串
		this.requestParameters = buf.toString();
		
	}
	
	public static void main(String args[]) {
		
		//System.out.println("--------------------");
		
		String bargainor_id = "1202437801";
		String key = "tenpaytesttenpaytesttenpaytest12";
		String currTime = TenpayUtil.getCurrTime();
		String strRandom = TenpayUtil.buildRandom(4) + "";
		
		PayRequest payRequest = new PayRequest();
		payRequest.setAttach("中文:中文");
		payRequest.setBank_type("0");
		payRequest.setBargainor_id(bargainor_id);
		payRequest.setCmdno("1");
		payRequest.setDate(currTime.substring(0,8));
		payRequest.setDesc("商品名称");
		payRequest.setFee_type("1");
		payRequest.setPurchaser_id("");
//		payRequest.setReturn_url("http://localhost:8080/tenpay/notify_handler.jsp");
		payRequest.setReturn_url("http://localhost:8780/szair/servlet/com.iss.szair.bank.tenpay.TenPayB2CServlet");
//		payRequest.setSp_billno("20080523095542000016");
		payRequest.setSp_billno("004913");
		payRequest.setTotal_fee(1);
		payRequest.setTransaction_id(bargainor_id + currTime + strRandom);
//		payRequest.setTransaction_id("1203292301200808140049131234");
		payRequest.setSpbill_create_ip("219.33.62.73");
		
		PayRequestHelper helper = new PayRequestHelper(key,payRequest);
		
		System.out.println(helper.getSendUrl());
		
		//System.out.println("--------------------");
	}

	
}
