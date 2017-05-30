package com.getMAC;

import java.util.HashSet;

public class checkMACAddr {
	/**
	 * 判断是否有权限： 0：没有  1：有
	 * @param str 输入的注册字符串
	 * @return 返回0或1
	 */
    public static int checkMAC(String str){
    	// 0:否  1：是
    	// 获取当前的MAC地址
    	String currentMAC = GetMACH.getMach();
    	//System.out.println("currentMAC ->"+currentMAC);
    	String[] currentMACAry = currentMAC.split(",");
    	String currentCpuNum = currentMACAry[0];
    	String currentsign=currentMACAry[1];
    	System.out.println(currentCpuNum);
    	System.out.println(currentsign);
    	// 注册
    	String[] strOrigin = str.split(",");
    	String cpuNum = strOrigin[0];
    	String sign=strOrigin[1];
    	if(currentCpuNum.endsWith(cpuNum.trim())&&currentsign.equalsIgnoreCase(sign)){
    		return 1;
    	}
    	return 0;
    }
    /**
     * 判断相同的MAC地址的个数
     * @param currentMACAry
     * @param strOrigin
     * @return 相同的MAC地址数
     */
    @Deprecated
    private static double findSameNum(String[] currentMACAry,String[] strOrigin){
    	double num = 0.0;
    	// 利用HashSet来寻找重复元素
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
