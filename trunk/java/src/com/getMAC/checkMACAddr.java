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
    	//System.out.println("currentMAC ->"+currentMAC);
    	String[] currentMACAry = currentMAC.split(",");
    	String currentCpuNum = currentMACAry[0];
    	String currentsign=currentMACAry[1];
    	System.out.println(currentCpuNum);
    	System.out.println(currentsign);
    	// ע��
    	String[] strOrigin = str.split(",");
    	String cpuNum = strOrigin[0];
    	String sign=strOrigin[1];
    	if(currentCpuNum.endsWith(cpuNum.trim())&&currentsign.equalsIgnoreCase(sign)){
    		return 1;
    	}
    	return 0;
    }
    /**
     * �ж���ͬ��MAC��ַ�ĸ���
     * @param currentMACAry
     * @param strOrigin
     * @return ��ͬ��MAC��ַ��
     */
    @Deprecated
    private static double findSameNum(String[] currentMACAry,String[] strOrigin){
    	double num = 0.0;
    	// ����HashSet��Ѱ���ظ�Ԫ��
    	HashSet<String> set = new HashSet<String>();
    	for(int i=1;i<currentMACAry.length;i++){
    		//System.out.println(currentMACAry[i]);
    		set.add(currentMACAry[i]);
    	}
    	for(int i=1;i<strOrigin.length;i++){
    		//System.out.println("strOrigin -> "+strOrigin[i]);
    		if(!set.add(strOrigin[i].replace(";","").trim())){
    			//System.out.println(strOrigin[i]);
    			num += 1;
    		}
    	}
    	return num;
    }
   
}
