package com.tenpay.helper;

import javax.servlet.http.HttpServletRequest;

import com.tenpay.bean.PayResponse;
import com.tenpay.util.MD5Util;
import com.tenpay.util.TenpayUtil;

public class PayResponseHelper {
	
	/** 交易密钥 */
	private String key;
	
	/** 支付应答bean */
	private PayResponse payResponse;
	
	/**
	 * 无参构造函数
	 */
	public PayResponseHelper() {
		payResponse = new PayResponse();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * 设置PayResponse
	 * @param request HttpServletRequest对象
	 */
	public void setPayResponse(HttpServletRequest request) {
		
		payResponse.setAttach(TenpayUtil.toString(
				request.getParameter("attach")));
		
		payResponse.setBargainor_id(TenpayUtil.toString(
				request.getParameter("bargainor_id")));
		
		payResponse.setCmdno(TenpayUtil.toString(
				request.getParameter("cmdno")));
		
		payResponse.setDate(TenpayUtil.toString(
				request.getParameter("date")));
		
		payResponse.setFee_type(TenpayUtil.toString(
				request.getParameter("fee_type")));
		
		payResponse.setPay_info(TenpayUtil.toString(
				request.getParameter("pay_info")));
		
		payResponse.setPay_result(TenpayUtil.toString(
				request.getParameter("pay_result")));
		
		payResponse.setSign(TenpayUtil.toString(
				request.getParameter("sign")));
		
		payResponse.setSp_billno(TenpayUtil.toString(
				request.getParameter("sp_billno")));
		
		payResponse.setTotal_fee(TenpayUtil.toLong(
				request.getParameter("total_fee")));
		
		payResponse.setTransaction_id(TenpayUtil.toString(
				request.getParameter("transaction_id")));
		
	}
	
	/**
	 * 获取应答bean
	 * @return PayResponse
	 */
	public PayResponse getPayResponse() {
		return this.payResponse;
	}
	
	/**
	 * 是否财付通签名 true为合法签名,false为非法签名
	 * @return boolean
	 * 			<p>true:合法</p>
	 * 			<p>false:不合法</p>
	 */
	public boolean isTenpaySign() {
		StringBuffer buf = new StringBuffer();
		TenpayUtil.addParameter(buf, 
				"cmdno", payResponse.getCmdno());
		
		TenpayUtil.addParameter(buf, 
				"pay_result", payResponse.getPay_result());
		
		TenpayUtil.addParameter(buf, 
				"date", payResponse.getDate());
		
		TenpayUtil.addParameter(buf, 
				"transaction_id", payResponse.getTransaction_id());
		
		TenpayUtil.addParameter(buf, 
				"sp_billno", payResponse.getSp_billno());
		
		TenpayUtil.addParameter(buf, 
				"total_fee", payResponse.getTotal_fee() + "");
		
		TenpayUtil.addParameter(buf, 
				"fee_type", payResponse.getFee_type());
		
		TenpayUtil.addParameter(buf, 
				"attach", payResponse.getAttach());
		
		TenpayUtil.addParameter(buf, 
				"key", this.key);
		
		String md5Sign = MD5Util.MD5Encode(buf.toString()).toUpperCase();
		if(md5Sign.equals(payResponse.getSign()))
			return true;
		
		return false;
	}
	
}
