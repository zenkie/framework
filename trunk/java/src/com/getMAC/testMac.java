package com.getMAC;

import nds.util.B64Code;
import nds.util.Tools;
import nds.util.AES;

public class testMac {
   public static void main(String[] args) throws Exception {
	  
	AES aes=new AES("burgeon");
	//String str = Tools.encrypt(GetMACH.getMach());
	String str = GetMACH.get_maconly();//aes.encrypt(GetMACH.getMach());
	//System.out.print(GetMACH.getMach());
	//运行结果
	System.out.print(str);
	int result = checkMACAddr.checkMAC(aes.decrypt(str));
	if(result == 0){
		System.out.println("没有权限");
	} else if(result == 1){
		System.out.println("有权限");
	}
}
}
