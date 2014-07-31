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
    	//System.out.print(currentCpuNum);
    	// 注册
    	String[] strOrigin = str.split(",");
    	String cpuNum = strOrigin[0];
    	// 判断CPU核数是否相同
    	if(currentCpuNum.endsWith(cpuNum.trim())){
    		// 相同的个数
    		double sameNum = findSameNum(currentMACAry,strOrigin);
    		//System.out.println(sameNum );
    		//System.out.println(currentMACAry.length-1);
    		if (sameNum >= 0.5){
    			return 1;
    		} else {
    			return 0;
    		}
    	}else {
    		return 0;
    	}
    }
    /**
     * 判断相同的MAC地址的个数
     * @param currentMACAry
     * @param strOrigin
     * @return 相同的MAC地址数
     */
    private static double findSameNum(String[] currentMACAry,String[] strOrigin){
    	double num = 0.0;
    	// 利用HashSet来寻找重复元素
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
