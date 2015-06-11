/*
 * Agile Control Technologies Ltd,. CO. http://www.agileControl.com
 */
package nds.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class LicenseWrapper {
	private License lic;

	LicenseWrapper(License lic) {
		this.lic = lic;
	}

	public long getLicenseID() {
		return lic.getLicenseID();
	}

	public String getProduct() {
		return lic.getProduct();
	}

	public String getVersion() {
		return lic.getVersion();
	}

	public License.LicenseType getLicenseType() {
		return lic.getLicenseType();
	}

	public String getName() {
		return lic.getName();
	}

	public String getCompany() {
		return lic.getCompany();
	}

	public int getNumUsers() {
		return lic.getNumUsers();
	}
	public int getNumOnlineUsers() {
		return lic.getNumOnlineUsers();
	}

	public int getNumPOS() {
		return lic.getNumPOS();
	}
	
	public int getPadPOS() {
		return lic.getPadPOS();
	}
	
	
	public String getSubsystems() {
		return lic.getSubsystems();
	}

	public String getURL() {
		return lic.getURL();
	}

	public Date getExpiresDate() {
		return lic.getExpiresDate();
	}

	public Date getMaintenanceExpiresDate() {
		return lic.getMaintenanceExpiresDate();
	}

	public Date getCreationDate() {
		return lic.getCreationDate();
	}

	public String getLicenseSignature() {
		return lic.getLicenseSignature();
	}

	public byte[] getFingerprint() {
		return lic.getFingerprint();
	}

	public boolean equals(Object o) {
		return lic.equals(o);
	}

	public String toString() {
		return lic.toString();
	}
	
	public boolean getExpdate() {
		// TODO Auto-generated method stub
		return lic.getExpdate();
	}
	
	public String getMms() {
		return lic.getMms();
	}

	public void setMms(String sendmss) {
		// TODO Auto-generated method stub
		lic.setMms(sendmss);
	}

	public void setExpdate(boolean b) {
		// TODO Auto-generated method stub
		lic.setExpdate(b);
	}

}

