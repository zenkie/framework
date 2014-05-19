/*
 * Agile Control Technologies Ltd,. CO. http://www.agileControl.com
 */
package nds.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;

import com.getMAC.checkMACAddr;
import com.getMAC.multiMac;

import java.io.*;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;
import nds.util.AES;

public final class LicenseManager {
	private static final Log logger = LogFactory.getLog(LicenseManager.class);

	private static List licenses = null;
	
	private static String pwd="eXFxbG1nczFjNg==";
	
	private LicenseManager() {
	}
	
	public static void validateLicense(String product, String version, String licenseFile){
		
		validateLicense( product,  version,  licenseFile, false);
	}
	
	public static void validateLicense(String product, String version, String licenseFile,boolean update)
			throws LicenseException {
		//cpuid is not easy to get when in dual kenerl of cpu, so disable checking from 2008-07-24
		if (update) {
			reloadLicenses(licenseFile);
		} else {
			loadLicenses(licenseFile);
		}
		if (!licenses.isEmpty()) {
			float needsVersion = Float.parseFloat(version.substring(0, 3));
			for (int i = 0; i < licenses.size(); i++) {
				License license = (License) licenses.get(i);
				float hasVersion = Float.parseFloat(license.getVersion()
						.substring(0, 3));
				boolean validVersion = hasVersion >= needsVersion;
				/*if (!validVersion && product.startsWith("Agile ERP")
						&& license.getMaintenanceExpiresDate() != null)
					try {
						Class versionClass = ClassUtils
								.forName("com.jivesoftware.forum.Version");
						Method versionMethod = versionClass.getMethod(
								"getBuildDate", (Class[]) null);
						Date buildDate = (Date) versionMethod.invoke(null,
								(Object[]) null);
						if (buildDate.before(license
								.getMaintenanceExpiresDate()))
							validVersion = true;
					} catch (Exception e) {
					}*/
				//增加产品名称对比
				if (isValidProduct(product, license))
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

	public static void reloadLicenses(String licenseFile) {
		if (licenses != null)
			licenses.clear();
		licenses = null;
		loadLicenses(licenseFile);
	}

	public static Iterator getLicenses() {
		if (licenses == null)
			return Collections.EMPTY_LIST.iterator();
		List licenseProxies = new ArrayList();
		for (int i = 0; i < licenses.size(); i++)
			licenseProxies.add(new LicenseWrapper((License) licenses.get(i)));

		return licenseProxies.iterator();
	}  
	

	static boolean validate(License license) throws Exception {
		// nds public key should be set fixed here, encoded using 
		String publicKey = "MIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYQAAoGAKBHVcsiTnPkA7uNMZ4CCU98CYOZ1gM7jyGyhihAj1UCkBqxYevubPpZ03nZoIZBJBvpNZ6qFYyPAc7j27abiwOiKA4XYtDCGfCQdOCKgWvJRM5WlrzV/UvFeucU3xA2wDWIh6HHHVgqPkEFMbt3mlbaV+rcYSuVfj+rWHUx0BwQ=";
		byte pub[] = B64Code.decode(publicKey.toCharArray());
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pub);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA");
		java.security.PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		Signature sig = Signature.getInstance("DSA");
		sig.initVerify(pubKey);
		logger.debug("Validating license. License fingerprint:");
		logger.debug(new String(B64Code.encode(license.getFingerprint())));
		sig.update(license.getFingerprint());

		return sig.verify(B64Code.decode(license.getLicenseSignature().toCharArray()));
//		return true;
	}

	private static synchronized void loadLicenses(String licenseFile) {

		int i;
		int chekmac;
		
		if (licenses != null)
			return;
		licenses = new ArrayList();
		//File file;

		//file = new File(licenseFile);
		License license;
		//logger.debug("Found potential license " + file.getName());
//		
//			BufferedReader in = new BufferedReader(new FileReader(file));
//			StringBuffer text = new StringBuffer();
//			char buf[] = new char[1024];
//			int len;
//			while ((len = in.read(buf)) >= 0) {
//				int j = 0;
//				while (j < len) {
//					char ch = buf[j];
//					if (Character.isLetter(ch) || Character.isDigit(ch)
//							|| ch == '+' || ch == '/' || ch == '=')
//						text.append(ch);
//					j++;
//				}
//			} 
//			in.close();
		try {
			/**
			 * 取消原先加密模式增加nds.util.aesutils方法解密
			 */
			//String xml = B64Code.decode(Tools.decrypt(licenseFile));
			AESUtils nes = new AESUtils();
			nes.setPwd(B64Code.decode(pwd));
			byte[] key = nes.initSecretKey();
			Key k=nes.setpwdkey(key);
			/*
			Key k = null;
			Class clazz = nds.util.AESUtils.class;
			for (Method method : clazz.getDeclaredMethods()) {
				if ("toaesKey".equals(method.getName())) {
					System.out.print("sadsfasd");
					method.setAccessible(true);
					k = (Key) method.invoke(clazz.newInstance(), key);
					break;
				}
			}
			 */
			byte[] decryptData = AESUtils.decrypt(
					Hex.decodeHex(licenseFile.toCharArray()), k);
			String xml = new String(decryptData);
			//System.out.println(xml);
			//logger.debug(xml);
			license = License.fromXML(xml);
			logger.debug("creationDate=" + license.getCreationDate());
			logger.debug("expiresDate=" + license.getExpiresDate());
			logger.debug("licenseid=" + license.getLicenseID());
			logger.debug("type=" + license.getLicenseType());
			logger.debug("date=" + license.getMaintenanceExpiresDate());
			logger.debug("version=" + license.getVersion());
			logger.debug("name=" + license.getName());
			logger.debug("company=" + license.getCompany());
			logger.debug("machineCode=" + license.getMachineCode());
			logger.debug("numUsers="+ license.getNumUsers());
			logger.debug("numOnlineUsers="+ license.getNumOnlineUsers());
			logger.debug("numPOS="+ license.getNumPOS());
			logger.debug("product=" + license.getProduct());
			logger.debug("cuscode=" + license.getCuscode());

			if (license.getLicenseID() == 1L) {
				logger.error("The license is out of date and is no longer "
						+ "valid. Please use a new license file.");
				return;

			}
			if(license.getExpiresDate() ==null ){
				logger.error("The license  has no expiration date.");
				//return;
			}
			if(license.getCreationDate() ==null ||  license.getCreationDate().getTime() > System.currentTimeMillis()){
				logger.error("The license  has no creation date or invalid creation date.");
				return;
			}
			long now = System.currentTimeMillis();
//			if (license.getExpiresDate().getTime() < now) {
//				logger.error("The license is expired.");
//				return;
//			}
			if( license.getMachineCode()!=null){
				String currentCodes= Tools.getCPUIDs(license.getMachineCode());
				boolean match=false;
				match=license.getMachineCode().equals(currentCodes);
				/*if(!match){
					// check one of the codes
					StringTokenizer st=new StringTokenizer(currentCodes,",");
					while(st.hasMoreTokens()){
						if(license.getMachineCode().equals(st.nextToken())){
							match=true;
							break;
						}
					}
				}*/ // no more tokens
				if(!match){
					logger.error("The license is not valid for machine code.");
					logger.debug("server code is :"+ currentCodes);
					return;
				}
				// check machine code length
				if(license.getMachineCode().length()<10){
					logger.error("machine code is not valid.");
					return;
				}
				
			}else{
				if(license.getLicenseType()==License.LicenseType.COMMERCIAL){
					//logger.error("The license should contain machine code.");
					//return;
					//if(license.getExpiresDate().getTime() - license.getCreationDate().getTime() > 1L * 100 * 24 * 3600* 1000  ){
						if(System.currentTimeMillis() >license.getExpiresDate().getTime()  ){
						//logger.error("Non-Commercial license valid duration should not be greater than 100 days");
						logger.error("your company service day is old!!!!!!!");
						//license.setMms(sendmss(license.getName(),license.getExpiresDate()));
					}
				}else if(license.getLicenseType()==License.LicenseType.NON_COMMERCIAL){
					//Non-Commercial should not have valid duration over 100 days
					if(license.getExpiresDate().getTime() - license.getCreationDate().getTime() > 1L * 100 * 24 * 3600* 1000  ){
						logger.error("Non-Commercial license valid duration should not be greater than 100 days");
						return;
					}
				}else if(license.getLicenseType()==License.LicenseType.EVALUATION){
					// Evaluation should not have valid duration over 31 days
					logger.debug("Evaluation should not have valid duration over");
					//if(license.getExpiresDate().getTime() - license.getCreationDate().getTime() > 1L * 31 * 24 * 3600* 1000  ){
					if(System.currentTimeMillis() >license.getExpiresDate().getTime()  ){
						logger.error("Evaluation license valid duration should not be greater than 30 days");
						//license.setExpdate(true);
						//return;
					}
				}
			}
			/*
			if (!validate(license)) {
				logger.error("The license is not valid.");
				return;
			}
			*/
			/*
			 * add check mac address is vaild
			 * */
			AES aes=new AES("bosxe");
			chekmac = multiMac.multiMacJun(aes.decrypt(license.getCuscode()));
			if(chekmac==0){
				logger.error("The license is not valid for this machine!!!");
				return;
			}
			licenses.add(license);
			if (licenses.isEmpty())
				logger.error("Not valid license file "+ licenseFile );
		} catch (Exception e) {
			//logger.debug(e.toString());
			logger.error("\n\r"+
                           "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\r"+
                           "     Invalid license for "+ licenseFile+"\n\r"+
                           "                                                \n\r"+
                           "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^", e);
		}
	}
/**
 *   正式客户如果发现超过截止日期 则发送系统消息 给用户提示 服务期到了
 */
	public static String sendmss(String cp,Date exp){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String cont=cp+">您的服务期已与"+df.format(exp)+"到期!请与产品客户联系！";
		String mss="art.dialog.notice({title: '系统消息',width: 220,content: '尊敬的客户<"+cont+"',icon: 'warning',time: 5});";
		return mss;
	}
	/**
	 * License contains <product>Jive Forums Basic</product> as <param>product</param> specified
	 * @param product
	 * @param license
	 * @return
	 */
	private static boolean isValidProduct(String product, License license) {
		return  product.equals(license.getProduct());
/*		product = product.intern();
		String licenseProduct = license.getProduct().intern();
		if (licenseProduct.indexOf("Jive Forums") >= 0) {
			if ("Jive Forums Lite" == product)
				return licenseProduct == "Jive Forums Lite"
						|| licenseProduct == "Jive Forums Professional"
						|| licenseProduct == "Jive Forums Enterprise"
						|| licenseProduct == "Jive Forums Expert Edition";
			if ("Jive Forums Professional" == product)
				return licenseProduct == "Jive Forums Professional"
						|| licenseProduct == "Jive Forums Enterprise"
						|| licenseProduct == "Jive Forums Expert Edition";
			if ("Jive Forums Enterprise" == product)
				return licenseProduct == "Jive Forums Enterprise"
						|| licenseProduct == "Jive Forums Expert Edition";
			if ("Jive Forums Expert Edition" == product)
				return licenseProduct == "Jive Forums Expert Edition";
			else
				return false;
		}
		if (licenseProduct.indexOf("Jive Knowledge Base") >= 0) {
			if ("Jive Knowledge Base Professional" == product)
				return licenseProduct == "Jive Knowledge Base Professional"
						|| licenseProduct == "Jive Knowledge Base Enterprise";
			if ("Jive Knowledge Base Enterprise" == product)
				return licenseProduct == "Jive Knowledge Base Enterprise";
			else
				return false;
		}
		if (licenseProduct.indexOf("Jive XMPP") >= 0)
			return product == licenseProduct;
		else
			return false;
*/
		//return true;
	}
}