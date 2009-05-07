package com.tenpay.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TenpayUtil {
	
	/**
	 * 把对象转换为int数值.
	 * 
	 * @param obj
	 *            包含数字的对象.
	 * @return int 转换后的数值,对不能转换的对象返回0。
	 */
	public static int toInt(Object obj) {
		int a = 0;
		try {
			if (obj != null)
				a = Integer.parseInt(obj.toString());
		} catch (Exception e) {

		}
		return a;
	}
	
	/**
	 * 把对象转换为long数值.
	 * 
	 * @param obj
	 *            包含数字的对象.
	 * @return long 转换后的数值,对不能转换的对象返回0。
	 */
	public static long toLong(Object obj) {
		long a = 0;
		try {
			if(obj != null)
				a = Long.parseLong(obj.toString());
		} catch (Exception e) {
			
		}
		return a;
	}
	
	/**
	 * 把对象转换成字符串
	 * @param obj
	 * @return String 转换成字符串,若对象为null,则返回空字符串.
	 */
	public static String toString(Object obj) {
		if(obj == null)
			return "";
		
		return obj.toString();
	}
	
	/**
	 * 元转换成为分 1元==100分
	 * 对于0.011元转换成为1分,小数点后面第3位以后的(包含第三位)将舍弃.
	 * @param money
	 * @return long
	 */
	public static long yuan2Fen(String money) {
		return TenpayUtil.yuan2Fen(Double.valueOf(money));
	}
	
	/**
	 * 元转换成分 1元==100分
	 * 对于0.011元转换成为1分,小数点后面第3位以后的(包含第三位)将舍弃.
	 * @param money
	 * @return long
	 */
	public static long yuan2Fen(double money) {		
		String strFen = (money * 100) + "";

		return Long.parseLong(strFen.substring(0,strFen.indexOf(".")));

	}
	
	/**
	 * 分转换成元 100分==1元
	 * @param money
	 * @return double
	 */
	public static double fen2Yuan(String money) {
		return TenpayUtil.fen2Yuan(Long.parseLong(money));
	}
	
	/**
	 * 分转换成元 100分==1元
	 * @param money
	 * @return double
	 */
	public static double fen2Yuan(long money) {
		return (double)money / 100;
	}
	
	/**
	 * 获取当前时间 yyyyMMddHHmmss
	 * @return String
	 */
	public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}
	
	/**
	 * 获取当前日期 yyyyMMdd
	 * @param date
	 * @return String
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String strDate = formatter.format(date);
		return strDate;
	}
	
	/**
	 * 
	 * @param str
	 * @return String
	 * @see java.net.URLEncoder.encode(String s, String enc)
	 */
	public static String URLEncode2GBK(String str) {
		
		if( null == str )
			return null;
		
		String strRet = "";
		try {
			strRet = java.net.URLEncoder.encode(str,"GBK");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strRet;
	}
	
	public static String URLDecode2GBK(String str) {
		if( null == str ) 
			return null;
		
		String strRet = "";
		try {
			strRet = java.net.URLDecoder.decode(str, "GBK");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strRet;
	}
	
	/**
	 * 以GBK编码encode参数的值
	 * @param parameters
	 * @return
	 */
	public static String encodeParameters2GBK(String parameters) {
		if (null == parameters || "".equals(parameters))
			return parameters;

		String[] parArray = parameters.split("&");
		if (null == parArray || parArray.length == 0)
			return parameters;

		StringBuffer bufRet = new StringBuffer();
		for (int i = 0; i < parArray.length; i++) {
			String[] keyAndValue = parArray[i].split("=");
			if (null != keyAndValue && keyAndValue.length == 2) {
				String key = keyAndValue[0];
				String value = keyAndValue[1];
				String encodeValue = URLEncode2GBK(value);
				TenpayUtil.addParameter(bufRet, key, encodeValue);
			} else {
				bufRet.append("&" + parArray[i]);
			}
		}

		return bufRet.toString();
	}
	
	/**
	 * 取出一个指定长度大小的随机正整数.
	 * 
	 * @param length
	 *            int 设定所取出随机数的长度。length小于11
	 * @return int 返回生成的随机数。
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
	
	/**
	 * 添加参数
	 * @param buf
	 * @param parameterName 参数名
	 * @param parameterValue 参数值
	 * @return StringBuffer
	 */
	public static StringBuffer addParameter(StringBuffer buf, 
			String parameterName,
			String parameterValue) {
		
		if("".equals(buf.toString())) {
			buf.append(parameterName);
			buf.append("=");
			buf.append(parameterValue);
		} else {
			buf.append("&");
			buf.append(parameterName);
			buf.append("=");
			buf.append(parameterValue);
		}
		
		return buf;
	}
	
	
	public static StringBuffer addParameter(StringBuffer buf, 
			String parameterName,
			int parameterValue) {
		
		if("".equals(buf.toString())) {
			buf.append(parameterName);
			buf.append("=");
			buf.append(parameterValue);
		} else {
			buf.append("&");
			buf.append(parameterName);
			buf.append("=");
			buf.append(parameterValue);
		}
		
		return buf;
	}
	/**
	 * 添加参数,若参数值不为空串,则添加。反之,不添加。
	 * @param buf
	 * @param parameterName
	 * @param parameterValue
	 * @return StringBuffer
	 */
	public static StringBuffer addBusParameter(StringBuffer buf,
			String parameterName,
			String parameterValue) {
		if( null == parameterValue || "".equals(parameterValue)) {
			return buf;
		}
		
		if("".equals(buf.toString())) {
			buf.append(parameterName);
			buf.append("=");
			buf.append(parameterValue);
		} else {
			buf.append("&");
			buf.append(parameterName);
			buf.append("=");
			buf.append(parameterValue);
		}
		return buf;
	}
	
	public static StringBuffer addBusParameter(StringBuffer buf,
			String parameterName,
			int parameterValue) {
		
			buf.append("&");
			buf.append(parameterName);
			buf.append("=");
			buf.append(parameterValue);
		return buf;
	}
	
	/**
	 * 跳转到显示的页面
	 * @param url 显示的页面,以绝对地址出现
	 * @param name 参数名字
	 * @param value 参数值
	 * @return String
	 * 		返回跳转的js字符串代码
	 */
	public static String gotoShow(String url, 
			String name, 
			String value) {
		String strScript = "<script language='javascript'>";
		strScript += "window.location.href='";
		strScript += url + "?" + name + "=" + value;
		strScript += "'</script>";
		
		return strScript;
	}
	
	/**
	 * 解析参数
	 * @param parameters 参数串
	 * @return Map<String,String>
	 */
	public static Map<String,String> parseParameters(String parameters) {
		if(null == parameters || "".equals(parameters)) return null;
		
		String[] parametersArray = parameters.split("&");
		if(null == parametersArray) return null;
		
		Map<String,String> m = new HashMap<String,String> ();
		for(int i = 0; i < parametersArray.length; i++) {
			String keyAndValue = parametersArray[i];
			String[] tempArray = keyAndValue.split("=");
			if(tempArray != null  &&  tempArray.length == 2) {
				String key = tempArray[0];
				String value = URLDecode2GBK(tempArray[1]);
				m.put(key, value);
			}
		}
		
		return m;
	}
	
	public static void main(String args[]) {
		System.out.println("----------");
		
		String fen = "100";
		System.out.println("100 fen is " + TenpayUtil.fen2Yuan(fen) + " yuan" );
		
		String yun = "2";
		System.out.println("fen=" + TenpayUtil.yuan2Fen(yun));
		
		String currTime = TenpayUtil.getCurrTime();
		System.out.println("currTime:" + currTime);
		
		String currDate = TenpayUtil.formatDate(new Date());
		System.out.println("currDate:" + currDate);
		
		String str = "88881491^1^2|68084040^1^1|468478488^1^4";
		String encodeGBK = TenpayUtil.URLEncode2GBK(str);
		System.out.println("encodeGBK:" + encodeGBK);
		
		int iRandom = TenpayUtil.buildRandom(1);
		System.out.println("iRandom:" + iRandom);
		
		String strShow = TenpayUtil.gotoShow("http://localhost:8080/tenpay_b2c_jsp",
				"msg","支付成功");
		System.out.println("strShow:" + strShow);
		
		String html = "<html><script language=\"javascript\">window.location.href='http://miklchen-pc:8080/tenpay_b2c_jsp/busSplitResponse.jsp?bargainor_id=1202952101&bus_args=88881491%5E1%5E2%7C68084040%5E1%5E1%7C468478488%5E1%5E4&bus_type=33&cmdno=3&fee_type=1&pay_info=ok&pay_result=0&sign=3EB3790277B5582B8EB212D8624C0C89&sp_billno=200808211431051471&total_fee=3&transaction_id=1202952101200808211431051471&version=4';</script></html>";
		String[] resArray = html.split("window.location.href='");
		if(null != resArray && resArray.length == 2) {
			String temp = resArray[1];
			String[] tempArray = temp.split("'");
			
		}
		
		String t = "a=中文&b=2&c";
		System.out.println(encodeParameters2GBK(t));
		
		System.out.println("----------");
	}
	
}
