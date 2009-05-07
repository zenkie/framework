package com.tenpay.bean;

/**
 * 支付应答bean,支付应答各个参数都包含在本类中,通过get/set方法进行获取或者赋值.
 * @version v1.0
 */
public class PayResponse {

	private String cmdno;
	
	private String pay_result;
	
	private String pay_info;
	
	private String date;
	
	private String bargainor_id;
	
	private String transaction_id;
	
	private String sp_billno;
	
	private long total_fee;
	
	private String fee_type;
	
	private String attach;
	
	private String sign;

	/**
	 * 获取业务代码
	 * @return String
	 */
	public String getCmdno() {
		return cmdno;
	}

	/**
	 * 设置业务代码
	 * @param cmdno 业务代码
	 */
	public void setCmdno(String cmdno) {
		this.cmdno = cmdno;
	}

	/**
	 * 获取支付结果
	 * @return String
	 */
	public String getPay_result() {
		return pay_result;
	}

	/**
	 * 设置支付结果
	 * @param pay_result 支付结果
	 */
	public void setPay_result(String pay_result) {
		this.pay_result = pay_result;
	}

	/**
	 * 获取支付结果信息，支付成功时为空
	 * @return String
	 */
	public String getPay_info() {
		return pay_info;
	}

	/**
	 * 设置支付结果信息，支付成功时为空
	 * @param pay_info 支付结果信息，支付成功时为空
	 */
	public void setPay_info(String pay_info) {
		this.pay_info = pay_info;
	}

	/**
	 * 获取商家日期
	 * @return String
	 */
	public String getDate() {
		return date;
	}

	/**
	 * 设置商家日期
	 * @param date 商家日期
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * 获取商户号
	 * @return String
	 */
	public String getBargainor_id() {
		return bargainor_id;
	}

	/**
	 * 设置商户号
	 * @param bargainor_id 商户号
	 */
	public void setBargainor_id(String bargainor_id) {
		this.bargainor_id = bargainor_id;
	}

	/**
	 * 获取交易单号
	 * @return String
	 */
	public String getTransaction_id() {
		return transaction_id;
	}

	/**
	 * 设置交易单号
	 * @param transaction_id 交易单号
	 */
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}

	/**
	 * 获取商家订单号
	 * @return String
	 */
	public String getSp_billno() {
		return sp_billno;
	}

	/**
	 * 设置商家订单号
	 * @param sp_billno 商家订单号
	 */
	public void setSp_billno(String sp_billno) {
		this.sp_billno = sp_billno;
	}

	/**
	 * 获取总金额,以分为单位.
	 * @return long
	 */
	public long getTotal_fee() {
		return total_fee;
	}

	/**
	 * 设置总金额,以分为单位.若有运费,请加在这里
	 * @param total_fee 总金额,以分为单位.若有运费,请加在这里
	 */
	public void setTotal_fee(long total_fee) {
		this.total_fee = total_fee;
	}

	/**
	 * 获取现金支付币种
	 * @return String
	 */
	public String getFee_type() {
		return fee_type;
	}

	/**
	 * 设置现金支付币种
	 * @param fee_type 现金支付币种
	 */
	public void setFee_type(String fee_type) {
		this.fee_type = fee_type;
	}

	/**
	 * 获取商家数据包,原样返回
	 * @return String
	 */
	public String getAttach() {
		return attach;
	}

	/**
	 * 设置商家数据包,原样返回
	 * @param attach 商家数据包,原样返回
	 */
	public void setAttach(String attach) {
		this.attach = attach;
	}

	/**
	 * 获取签名串
	 * @return String
	 */
	public String getSign() {
		return sign;
	}

	/**
	 * 设置签名串
	 * @param sign 签名串
	 */
	public void setSign(String sign) {
		this.sign = sign;
	}
	
}
