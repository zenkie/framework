package com.getMAC;


public class multiMac {
	/**
	 * �ֺ�
	 */
	private static final String DISPATCH = ";";
	/**
	 * �Զ�̨������Mac��ַ�����ж�
	 * @param multiMac
	 * @return �Ƿ�Ϊ�ض���Mac��ַ
	 */
	public static int multiMacJun(String multiMac){
		int returnValue = 0;
		// �����ǰΪһ̨������Mac��ַ
		if(multiMac.indexOf(DISPATCH) == -1){
			  //  ִ�е�һ�Ĳ��� 
			returnValue = checkMACAddr.checkMAC(multiMac);
		} else {
			 String[] macStrArr = new String[]{};
			 macStrArr = multiMac.split(DISPATCH);
			 for(int i=0;i<macStrArr.length;i++){
				 String macArr = macStrArr[i];
				 returnValue = checkMACAddr.checkMAC(macArr);
				 if(returnValue == 1) break;
			 }
		}
		return returnValue;
	}
}