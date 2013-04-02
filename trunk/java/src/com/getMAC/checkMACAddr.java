package com.getMAC;

import java.util.HashSet;

public class checkMACAddr {
	/**
	 * �ж��Ƿ���Ȩ�ޣ� 0��û��  1����
	 * @param str �����ע���ַ���
	 * @return ����0��1
	 */
    public static int checkMAC(String str){
    	// 0:��  1����
    	// ��ȡ��ǰ��MAC��ַ
    	String currentMAC = GetMACH.getMach();
    	String[] currentMACAry = currentMAC.split(",");
    	String currentCpuNum = currentMACAry[0];
    	// ע��
    	String[] strOrigin = str.split(",");
    	String cpuNum = strOrigin[0];
    	// �ж�CPU�����Ƿ���ͬ
    	if(currentCpuNum.endsWith(cpuNum)){
    		// ��ͬ�ĸ���
    		double sameNum = findSameNum(currentMACAry,strOrigin);
    		if ((sameNum/(strOrigin.length-1)) >= 0.5){
    			return 1;
    		} else {
    			return 0;
    		}
    	}else {
    		return 0;
    	}
    }
    /**
     * �ж���ͬ��MAC��ַ�ĸ���
     * @param currentMACAry
     * @param strOrigin
     * @return ��ͬ��MAC��ַ��
     */
    private static double findSameNum(String[] currentMACAry,String[] strOrigin){
    	double num = 0.0;
    	// ����HashSet��Ѱ���ظ�Ԫ��
    	HashSet<String> set = new HashSet<String>();
    	for(int i=1;i<currentMACAry.length;i++){
    		set.add(currentMACAry[i]);
    	}
    	for(int i=1;i<strOrigin.length;i++){
    		if(!set.add(strOrigin[i]))
    			num += 1;
    	}
    	return num;
    }
   
}