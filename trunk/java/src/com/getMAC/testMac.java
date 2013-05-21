package com.getMAC;

import nds.util.B64Code;
import nds.util.Tools;
import nds.util.AES;

public class testMac {
   public static void main(String[] args) throws Exception {
	  
	AES aes=new AES("burgeon");
	//String str = Tools.encrypt(GetMACH.getMach());
	String str = GetMACH.get_maconly();
	String str1 = aes.encrypt("4,6A:1E:20:52:41:53,58:94:23:54:8E:8E,00:03:05:17:03:25,68:94:23:54:8E:8D,2A:94:23:54:8E:8D;");
	String str2 = aes.encrypt("3,9A:1E:20:52:41:53,68:94:23:54:8E:8E,00:03:05:17:03:25,68:94:23:54:8E:8D,2A:94:23:54:8E:8D;");
	//System.out.print(aes.decrypt(str1+str+str2));
	//System.out.print(GetMACH.getMach());
	//运行结果
	//System.out.print(str);
	int result = multiMac.multiMacJun(aes.decrypt(str+str1+str2));
	if(result == 0){
		System.out.println("没有权限");
	} else if(result == 1){
		System.out.println("有权限");
	}
}
}
