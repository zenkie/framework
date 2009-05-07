package nds.util;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
/**
 * Security algorithm using AES
 * 
 * 关于此加密算法的用法：
 *  本加密算法用于批量加密/解密，在构造对象的时候传入加密口令，然后批量调用encrypt/decrypt 算法即可
 *  
 *  解密时的加密口令首先应保证与加密时的口令一致。原始口令可以通过 StringUtils.hash() 通过MD5的算法
 *  保存在系统中。当口令一致后，即可执行解密算法，从而保证解密数据的正确性。
 * 
 * @see http://hi.baidu.com/621021021/blog/item/832ce524e1fbc1308744f98b.html
 * @see http://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html
 * @see http://www.javanb.com/java/1/17816.html
 * @author yfzhu
 *
 */
public class AES {
	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
	
	private transient Cipher cipherEncrypt;
	private transient Cipher cipherDecrypt;
	/**
	 * @param passwd Length of password in bytes
	 * @throws  java.lang.IllegalArgumentException
	 */
	public  AES(String password)  {
		if(password==null ) throw new java.lang.IllegalArgumentException("Password should not be null");
		byte[] passwd= StringUtils.hash(password).getBytes(); // 32 bytes
		byte[] key= new byte[16]; // 128 bits
		int i=0,j;
		while( i< 16){
			for(j=0;i< 16 && j< passwd.length;i++,j++ ) key[i]= passwd[j];
		}
		SecretKeySpec skeySpec=new SecretKeySpec(key, "AES");
		try{
			cipherEncrypt = Cipher.getInstance("AES");
			cipherEncrypt.init(Cipher.ENCRYPT_MODE, skeySpec);
			
			cipherDecrypt =Cipher.getInstance("AES");
			cipherDecrypt.init(Cipher.DECRYPT_MODE, skeySpec);
		}catch(Throwable t){
			throw new NDSRuntimeException("Found error", t);
		}
		
	}
	/**
	 * Remove internal security information
	 *
	 */
	public void destroy(){
		cipherEncrypt=null;
		cipherDecrypt=null;
	}
	/**
	 * 
	 * @param message
	 * @return  for message length 
	 *   less than 16, retuned size is 32, 
	 *   equal or greater than 16 return 64
	 *   equal or greater than 32 return 96
	 * @throws Exception
	 */
	public String encrypt(String message) throws Exception{
	       byte[] encrypted =cipherEncrypt.doFinal(message.getBytes());
	       return toString(encrypted);
	}
	public String decrypt(String encryptedStr) throws Exception{
	       byte[] encrypted = toBytesFromString(encryptedStr);
	       byte[] original =cipherDecrypt.doFinal(encrypted);
	       return  new String(original);
	}
	/**
	 * <p>
	 * Returns a string of hexadecimal digits from a byte array. Each byte is
	 * converted to 2 hex symbols; zero(es) included.
	 * </p>
	 * 
	 * <p>
	 * This method calls the method with same name and three arguments as:
	 * </p>
	 * 
	 * <pre>
	 * toString(ba, 0, ba.length);
	 * </pre>
	 * 
	 * @param ba
	 *            the byte array to convert.
	 * @return a string of hexadecimal characters (two for each byte)
	 *         representing the designated input byte array.
	 */
	public static String toString(byte[] ba) {
		return toString(ba, 0, ba.length);
	}

	/**
	 * <p>
	 * Returns a string of hexadecimal digits from a byte array, starting at
	 * <code>offset</code> and consisting of <code>length</code> bytes. Each
	 * byte is converted to 2 hex symbols; zero(es) included.
	 * </p>
	 * 
	 * @param ba
	 *            the byte array to convert.
	 * @param offset
	 *            the index from which to start considering the bytes to
	 *            convert.
	 * @param length
	 *            the count of bytes, starting from the designated offset to
	 *            convert.
	 * @return a string of hexadecimal characters (two for each byte)
	 *         representing the designated input byte sub-array.
	 */
	public static final String toString(byte[] ba, int offset, int length) {
		char[] buf = new char[length * 2];
		for (int i = 0, j = 0, k; i < length;) {
			k = ba[offset + i++];
			buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
			buf[j++] = HEX_DIGITS[k & 0x0F];
		}
		return new String(buf);
	}

	/**
	 * <p>
	 * Returns a string of hexadecimal digits from a byte array. Each byte is
	 * converted to 2 hex symbols; zero(es) included. The argument is treated as
	 * a large little-endian integer and is returned as a large big-endian
	 * integer.
	 * </p>
	 * 
	 * <p>
	 * This method calls the method with same name and three arguments as:
	 * </p>
	 * 
	 * <pre>
	 * toReversedString(ba, 0, ba.length);
	 * </pre>
	 * 
	 * @param ba
	 *            the byte array to convert.
	 * @return a string of hexadecimal characters (two for each byte)
	 *         representing the designated input byte array.
	 */
	public static String toReversedString(byte[] ba) {
		return toReversedString(ba, 0, ba.length);
	}

	/**
	 * <p>
	 * Returns a string of hexadecimal digits from a byte array, starting at
	 * <code>offset</code> and consisting of <code>length</code> bytes. Each
	 * byte is converted to 2 hex symbols; zero(es) included.
	 * </p>
	 * 
	 * <p>
	 * The byte array is treated as a large little-endian integer, and is
	 * returned as a large big-endian integer.
	 * </p>
	 * 
	 * @param ba
	 *            the byte array to convert.
	 * @param offset
	 *            the index from which to start considering the bytes to
	 *            convert.
	 * @param length
	 *            the count of bytes, starting from the designated offset to
	 *            convert.
	 * @return a string of hexadecimal characters (two for each byte)
	 *         representing the designated input byte sub-array.
	 */
	public static final String toReversedString(byte[] ba, int offset,
			int length) {
		char[] buf = new char[length * 2];
		for (int i = offset + length - 1, j = 0, k; i >= offset;) {
			k = ba[offset + i--];
			buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
			buf[j++] = HEX_DIGITS[k & 0x0F];
		}
		return new String(buf);
	}

	/**
	 * <p>
	 * Returns a byte array from a string of hexadecimal digits.
	 * </p>
	 * 
	 * @param s
	 *            a string of hexadecimal ASCII characters
	 * @return the decoded byte array from the input hexadecimal string.
	 */
	public static byte[] toBytesFromString(String s) {
		int limit = s.length();
		byte[] result = new byte[((limit + 1) / 2)];
		int i = 0, j = 0;
		if ((limit % 2) == 1) {
			result[j++] = (byte) fromDigit(s.charAt(i++));
		}
		while (i < limit) {
			result[j] = (byte) (fromDigit(s.charAt(i++)) << 4);
			result[j++] |= (byte) fromDigit(s.charAt(i++));
		}
		return result;
	}

	/**
	 * <p>
	 * Returns a byte array from a string of hexadecimal digits, interpreting
	 * them as a large big-endian integer and returning it as a large
	 * little-endian integer.
	 * </p>
	 * 
	 * @param s
	 *            a string of hexadecimal ASCII characters
	 * @return the decoded byte array from the input hexadecimal string.
	 */
	public static byte[] toReversedBytesFromString(String s) {
		int limit = s.length();
		byte[] result = new byte[((limit + 1) / 2)];
		int i = 0;
		if ((limit % 2) == 1) {
			result[i++] = (byte) fromDigit(s.charAt(--limit));
		}
		while (limit > 0) {
			result[i] = (byte) fromDigit(s.charAt(--limit));
			result[i++] |= (byte) (fromDigit(s.charAt(--limit)) << 4);
		}
		return result;
	}

     public static void main(String[] args) throws Exception {
    	 AES aes=new AES("12345678901234512345678901234511");
    	 int size=2000;
    	 String[] codes=new String[size];
    	 String[] encodes=new String[size];
    	 String[] decodes=new String[size];
    	 for(int i=0;i< size;i++){
    		 codes[i]=( String.valueOf(nds.control.web.test.RandomGen.getRandomStringOfFixedLength(3000)));
    		 
    	 }
    	 
    	 long time= System.currentTimeMillis();
    	 for(int i=0;i< codes.length;i++){
    		 encodes[i]=aes.encrypt(codes[i]);
    		 System.out.println("length for original length="+codes[i].length()+":"+encodes[i].length()  );
    	 }
    	 long duration=  System.currentTimeMillis() -time;
    	 
    	 System.out.println("Time duration for encode:"+ (duration));

    	 time= System.currentTimeMillis();
    	 for(int i=0;i< codes.length;i++){
    		 decodes[i]=aes.decrypt(encodes[i]);
    	 }
    	 duration=  System.currentTimeMillis() -time;
    	 
    	 System.out.println("Time duration for decode:"+ (duration));
    	 for(int i=0;i< codes.length;i++){
    		 if(!codes[i].equals(decodes[i])){
    			 System.out.println(" found error in "+ i+": code="+ codes[i]+", decode="+ decodes[i]+", encode="+ encodes[i]);
    		 }
    	 }
    	 
     }	
     /**
 	 * <p>
 	 * Returns a number from <code>0</code> to <code>15</code> corresponding
 	 * to the designated hexadecimal digit.
 	 * </p>
 	 * 
 	 * @param c
 	 *            a hexadecimal ASCII symbol.
 	 */
 	public static int fromDigit(char c) {
 		if (c >= '0' && c <= '9') {
 			return c - '0';
 		} else if (c >= 'A' && c <= 'F') {
 			return c - 'A' + 10;
 		} else if (c >= 'a' && c <= 'f') {
 			return c - 'a' + 10;
 		} else
 			throw new IllegalArgumentException("Invalid hexadecimal digit: "
 					+ c);
 	}	
}
