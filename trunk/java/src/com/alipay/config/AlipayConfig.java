package com.alipay.config;

/* *
 *������AlipayConfig
 *���ܣ�����������
 *��ϸ�������ʻ��й���Ϣ������·��
 *�汾��3.3
 *���ڣ�2012-08-10
 *˵����
 *���´���ֻ��Ϊ�˷����̻����Զ��ṩ���������룬�̻����Ը����Լ���վ����Ҫ�����ռ����ĵ���д,����һ��Ҫʹ�øô��롣
 *�ô������ѧϰ���о�֧�����ӿ�ʹ�ã�ֻ���ṩһ���ο���
	
 *��ʾ����λ�ȡ��ȫУ����ͺ��������ID
 *1.������ǩԼ֧�����˺ŵ�¼֧������վ(www.alipay.com)
 *2.������̼ҷ���(https://b.alipay.com/order/myOrder.htm)
 *3.�������ѯ���������(PID)��������ѯ��ȫУ����(Key)��

 *��ȫУ����鿴ʱ������֧�������ҳ��ʻ�ɫ��������ô�죿
 *���������
 *1�������������ã������������������������
 *2���������������ԣ����µ�¼��ѯ��
 */

public class AlipayConfig {
	
 
	
	//�����˺�
	private  String paymail = "";
	//�����������������������������������Ļ�����Ϣ������������������������������
	// ���������ID����2088��ͷ��16λ��������ɵ��ַ���
	private  String partner = "";
	
	// ���װ�ȫ�����룬�����ֺ���ĸ��ɵ�32λ�ַ���
	// ���ǩ����ʽ����Ϊ��MD5��ʱ�������øò���
	private  String key = "";
	
    // �̻���˽Կ
    // ���ǩ����ʽ����Ϊ��0001��ʱ�������øò���
	public static String private_key = "";

    // ֧�����Ĺ�Կ
    // ���ǩ����ʽ����Ϊ��0001��ʱ�������øò���
	public static String ali_public_key = "";

	//�����������������������������������Ļ�����Ϣ������������������������������
	

	// �����ã�����TXT��־�ļ���·��
	public static String log_path = "D:\\";

	// �ַ������ʽ Ŀǰ֧��  utf-8
	public static String input_charset = "utf-8";
	
	// ǩ����ʽ��ѡ���0001(RSA)��MD5
	public static  String sign_type = "MD5";
	// ���ߵĲ�Ʒ�У�ǩ����ʽΪrsaʱ��sign_type�踳ֵΪ0001������RSA

	
	public  String getPartner() {
		return partner;
	}

	public  void setPartner(String partner) {
		this.partner = partner;
	}

	public  String getKey() {
		return key;
	}

	public  void setKey(String key) {
		this.key = key;
	}

	public  String getPaymail() {
		return paymail;
	}

 
	public  void setPaymail(String paymail) {
		this.paymail = paymail;
	}
	
	
	

}
