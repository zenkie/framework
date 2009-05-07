package com.tenpay.bean;

/**
 * 支付请求bean,支付请求各个参数都包含在本类中,通过get/set方法进行获取或者赋值.
 * @version v1.0
 */
public class PayRequest {

	private String cmdno;
	
	private String date;
	
	private String bank_type;
	
	private String desc;
	
	private String purchaser_id;
	
	private String bargainor_id;
	
	private String transaction_id;
	
	private String sp_billno;
	
	private long total_fee;
	
	private String fee_type;
	
	private String return_url;
	
	private String attach;
	
	private String spbill_create_ip;

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
	 * 获取商户日期
	 * @return String
	 */
	public String getDate() {
		return date;
	}

	/**
	 * 设置商户日期
	 * @param date 商户日期
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * 获取银行类型
	 * @return String
	 */
	public String getBank_type() {
		return bank_type;
	}

	/**
	 * 设置银行类型
	 * @param bank_type 银行类型
	 */
	public void setBank_type(String bank_type) {
		this.bank_type = bank_type;
	}

	/**
	 * 获取商品名称
	 * @return String
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * 设置商品名称
	 * @param desc 商品名称
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * 获取买方财付通帐号,可以为空
	 * @return String
	 */
	public String getPurchaser_id() {
		return purchaser_id;
	}

	/**
	 * 设置买方财付通帐号
	 * @param purchaser_id 买方财付通帐号
	 */
	public void setPurchaser_id(String purchaser_id) {
		this.purchaser_id = purchaser_id;
	}

	/**
	 * 获取商家的商户号,以12开头的10位数字串
	 * @return String
	 */
	public String getBargainor_id() {
		return bargainor_id;
	}

	/**
	 * 设置商家的商户号, 以12开头的10位数字串
	 * @param bargainor_id 商家的商户号, 以12开头的10位数字串
	 */
	public void setBargainor_id(String bargainor_id) {
		this.bargainor_id = bargainor_id;
	}

	/**
	 * 获取财付通交易单号
	 * @return String
	 */
	public String getTransaction_id() {
		return transaction_id;
	}

	/**
	 * 设置财付通交易单号
	 * @param transaction_id 财付通交易单号
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
	 * 获取接收财付通返回结果的URL
	 * @return String
	 */
	public String getReturn_url() {
		return return_url;
	}

	/**
	 * 设置接收财付通返回结果的URL
	 * @param return_url 接收财付通返回结果的URL,以绝对地址形式出现,如:http://www.xxxx.com
	 */
	public void setReturn_url(String return_url) {
		this.return_url = return_url;
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

	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}

	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}
	
	
	
	
	
}
