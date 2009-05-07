package nds.saasifc.fw086;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.StringUtils;

import java.io.*;


public class DesEncrypter {
	private Logger logger= LoggerManager.getInstance().getLogger(DesEncrypter.class.getName());
	
    private Cipher ecipher;
    private Cipher dcipher;

    public DesEncrypter(String key) throws Exception  {
/*
 * input: ¥˝º”√‹ ˝æ›(source_data)£¨√‹‘ø(source_key)

source_key_byte_array = convert_to_byte_array(source_key)

source_key_md5_byte_array = md5_encrypt(source_key_byte_array)

des_key = get_odd_byte(source_key_md5_byte_array)

des_iv = get_odd_byte(source_key_md5_byte_array)

source_data_md5_string = md5(source_data)

des_source_data = source_data_md5_string + source_data 

des_encrypted_data = des_encrypt(des_source_data, des_key, des_iv)

base64_encrypted_data = base64_encode(des_encrypted_data) 

output: base64_encrypted_data

 */    	logger.debug(key);
    	byte[] b=key.getBytes("UTF-8");
    	logger.debug( StringUtils.toHex(b));
    	//String md5= nds.util.MD5Sum.toCheckSumStr(key);
    	MessageDigest md =MessageDigest.getInstance( "MD5" );
    	b=md.digest(b);
    	logger.debug( StringUtils.toHex(b));
    	
    	byte[] k=new byte[8], iv= new byte[8];
    	for(int i=0;i< k.length;i++){
    		k[i]= b[i*2];
    		iv[i]=b[i*2];
    	}
    	logger.debug( StringUtils.toHex(k));

    	java.security.spec.AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

    	/*SecureRandom sr = new SecureRandom(iv);
    	KeyGenerator kg = KeyGenerator.getInstance ("DES" );
    	kg.init (sr);
    	SecretKey skey = kg.generateKey(); */
    	
    	SecretKey skey =(new SecretKeySpec(k, "DES"));
    	
    	ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        dcipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        ecipher.init(Cipher.ENCRYPT_MODE, skey,paramSpec);
        dcipher.init(Cipher.DECRYPT_MODE, skey,paramSpec);
    }

    

    public String encrypt(String str) {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF-8");
            
            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);
            
            // Encode bytes to base64 to get a string
            return new sun.misc.BASE64Encoder().encode(enc);
        } catch (javax.crypto.BadPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (UnsupportedEncodingException e) {
        } catch (java.io.IOException e) {
        }
        return null;
    }

    public String decrypt(String str) {
        try {
            // Decode base64 to get bytes
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);

            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (UnsupportedEncodingException e) {
        } catch (java.io.IOException e) {
        }
        return null;
    }
}
