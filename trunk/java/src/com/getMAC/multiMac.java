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
