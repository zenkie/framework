/*
 * Agile Control Technologies Ltd,. CO. http://www.agileControl.com
 */
package nds.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;

import com.getMAC.GetMACH;
import com.getMAC.checkMACAddr;
import com.getMAC.multiMac;

import java.io.*;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.KeyFactory;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.*;
import nds.util.AES;

public final class LicenseManager extends LicenseMake{ 
	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory.getLog(LicenseManager.class);

	private List licenses ;
	
//	public List getLicenses() {
//		return licenses;
//	}
	private  String pwd;
	
	public String getPwd() {
		return pwd;
	}

	public  void setPwd(String pwds) {
		pwd = pwds;
	}

	public LicenseManager() {
	}
	
	public void validateLicense(String product, String version, String licenseFile){
		
		validateLicense( product,  version,  licenseFile, false);
	}
	
	public void validateLicense(String product, String version, String licenseFile,boolean update)
			throws LicenseException {
		if (!licenses.isEmpty()) {
			float needsVersion = Float.parseFloat(version.substring(0, 3));
			for (int i = 0; i < licenses.size(); i++) {
				License license = (License) licenses.get(i);
				float hasVersion = Float.parseFloat(license.getVersion()
						.substring(0, 3));
				boolean validVersion = hasVersion >= needsVersion;
				logger.debug(license.toString());
				/*
				 * add check mac address is vaild
				 * */
				AES aes=new AES(pwd);
				int chekmac=0;
				try {
					chekmac = multiMac.multiMacJun(aes.decrypt(license.getCuscode()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("The license decrypt is wrong!!!");
					e.printStackTrace();
					throw new LicenseException("The license decrypt is wrong!!!");
				}
				if(chekmac==0){
					logger.error("The license is not valid for this machine!!!");
					throw new LicenseException("The license is not valid for this machine");
				}
				
					return;
				
			}

			String productNames = "";
			for (int i = 0; i < licenses.size(); i++) {
				License license = (License) licenses.get(i);
				if (i != 0)
					productNames = productNames + ", ";
				productNames = productNames + license.getProduct() + " "
						+ license.getVersion();
			}

			throw new LicenseException(
					"You are not licensed to use this product. You are licensed for the following product(s): "
							+ productNames
							+ "; required product: "
							+ product
							+ " " + version + ".");
		} else {
			throw new LicenseException(
					"You do not have a valid license for this product.");
		}

	}



	public Iterator getLicenses() {
		if (licenses == null)
			return Collections.EMPTY_LIST.iterator();
		List licenseProxies = new ArrayList();
		for (int i = 0; i < licenses.size(); i++)
			licenseProxies.add(new LicenseWrapper((License) licenses.get(i)));

		return licenseProxies.iterator();
	}  
	
	
	public String get_maconly(){
		AES aes=new AES(pwd);
    	String str;
		try {
			str = aes.encrypt(GetMACH.getMach());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			str="get machine code is error!!!";
			e.printStackTrace();
		}
		return str;
	}
	


	/**
	 * License contains <product>Jive Forums Basic</product> as <param>product</param> specified
	 * @param product
	 * @param license
	 * @return
	 */
	public boolean isValidProduct(String product, License license) {
		return  product.equals(license.getProduct());
	}
}