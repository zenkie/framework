package com.getMAC;


public class multiMac {
	/**
	 * 分号
	 */
	private static final String DISPATCH = ";";
	/**
	 * 对多台机器的Mac地址进行判断
	 * @param multiMac
	 * @return 是否为特定的Mac地址
	 */
	public static int multiMacJun(String multiMac){
		int returnValue = 0;
		// 如果当前为一台机器的Mac地址

		if(multiMac.indexOf(DISPATCH) == -1){
			  //  执行单一的测试 
			returnValue = checkMACAddr.checkMAC(multiMac);
		} else {
			 String[] macStrArr = new String[]{};
			 macStrArr = multiMac.trim().split(DISPATCH);
			 System.out.println("multiMacJun size ->"+macStrArr.length);
			 for(int i=0;i<macStrArr.length;i++){
				 String macArr = macStrArr[i];
				 System.out.println("macArr ->"+macArr);
				 returnValue = checkMACAddr.checkMAC(macArr);
				 System.out.println("returnValue ->"+returnValue);
				 if(returnValue == 1) break;
			 }
		}
		return returnValue;
	}
}
