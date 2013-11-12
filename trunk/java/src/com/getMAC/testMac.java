package com.getMAC;

import java.lang.reflect.Method;
import java.security.Key;

import nds.util.AESUtils;
import nds.util.B64Code;
import nds.util.Hex;
import nds.util.Tools;
import nds.util.AES;

public class testMac {
	//private static String aesKeyEncoded = "A9EB6B3CB752CA02E22446AAC0419A88D46CBAC7B78DC5AFDC5455DB911EBD1F172B7585ED94F3E408A5A049F9E2616F6607026D8137512C1F5FA98D45DAC5B55E81050FA3B1591116D204616874692C6FE71C9773C5568D035FD4E1566186A36C8642D6FE5D56F3DA101E3F026EFB8C92C15920F4B296E0B56164319B73A1DDEE4091B0494D6BBB12D4660F13A1F984773DD7C86E427DEB560D5F08FBEC87797A702CFFF5C43E1C9CAACF31C251821CF7AB03A5ACB710DEF3854F24FB42C08988D95C894331AC31BF7F005C20BE616EF42D07911C764CE302B0DD5ABB6A816847AF537036C3C164BB4F97B525685B29328DB78E5D7ADA920805F408093DC50F108879130C83960C6A226D77C5E9DCCCE4A1844C3431A70DBFB61E4FFAD15423C3DBDC5379DF75A56FA382D8388AD359195CF256222A9F0F669E29202E787C80B1AB44851BB26CE9FEACA0223328567E8803B16E901793E55375B57523D9A728D89B32CC6C979EF6C3B6A4B650A36A5DE74984952F6CC40E88D80C02CCCE65441476B975970ACF604E39D89F21B25BC0CB93B8769F13FFF6432A35AAB01485C654E9BCB156F222366FEA31078091A325C945CE39F9159C23E24D5E69B8C707ECF3445131606743EB611CB7557FBD4C25";
 
   public static void main(String[] args) throws Exception {
	  
	/*
	AES aes=new AES("burgeon");
	//AES aes1=new AES(aesKeyEncoded);
	//String str = Tools.encrypt(GetMACH.getMach());
	String str = GetMACH.get_maconly();
	String str1 = aes.encrypt("4,6A:1E:20:52:41:53,58:94:23:54:8E:8E,00:03:05:17:03:25,68:94:23:54:8E:8D,2A:94:23:54:8E:8D;");
	String str2 = aes.encrypt("3,9A:1E:20:52:41:53,68:94:23:54:8E:8E,00:03:05:17:03:25,68:94:23:54:8E:8D,2A:94:23:54:8E:8D;");
	String str4 =aes.encrypt("407A3442eA3Ac5A2A1432c6eA891858AA0e28cf7jnjb");
	//System.out.print(str4);
	String str3="ED2150CE36C51CE29B98ED6CFF16279D5502B119CDCDBC231B85A086336DC628F249ABE2D7FE379B30E9ED2A064E38DA";
	//System.out.print(aes1.decrypt(str3));
	//System.out.print(GetMACH.getMach());
	//运行结果
	//System.out.print(str);
	int result = multiMac.multiMacJun(aes.decrypt(str+str1+str2));
	if(result == 0){
		System.out.println("没有权限");
	} else if(result == 1){
		System.out.println("有权限");
	}
	*/
	  String license="asdfasdfasdfasdfasdf";
	  AESUtils aes=new AESUtils();
	  aes.setPwd("burgeon123");
	  byte[] key = aes.initSecretKey(); 
	  Key k = null;
/*
	  Class.forName("nds.util.AESUtils").getMethod("toKey",new Class[]{Key.class});
	  //byte[] encryptData = aes.encrypt(license.getBytes(), k);
	  //Hex.encodeHexStr(encryptData);
	*/
	  //aes.getDeclaredMethod("toKey");
	  Class clazz = nds.util.AESUtils.class;
	  for (Method method : clazz.getDeclaredMethods()) {
		  if ("toKey".equals(method.getName())) {
			  // System.out.print("sadsfasd");
			  method.setAccessible(true);
			  k = (Key) method.invoke(clazz.newInstance(), key);
			  break;
		  }
	  }
	  byte[] encryptData = aes.encrypt(license.getBytes(), k);
	   //System.out.print(Hex.encodeHexStr(encryptData));
	   String a="你好阿速度发送地方";
	   String b=Hex.encodeHexStr(a.getBytes());
	   //System.out.print(b);
	   //System.out.print(Hex.decodeHexStr(b.toCharArray()));
	  // System.out.print(B64Code.encode("yqqlmgs1c6"));
	   //System.out.print(B64Code.decode("eXFxbG1nczFjNg=="));
	   java.text.SimpleDateFormat df=new java.text.SimpleDateFormat("yyyy-MM-dd");
	   java.util.Date d=new java.util.Date();
	   System.out.print(d.getTime());
	   System.out.print(df.format(d));
	   

}
}
