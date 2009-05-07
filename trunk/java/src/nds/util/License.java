/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class License {
	private long licenseID;

	private String product;

	private String version;

	private LicenseType licenseType;

	private String name;

	private String company;


	private String url;

	private Date expiresDate;

	private Date maintenanceExpiresDate;

	private Date creationDate;
	
	private String licenseSignature;
	
	private String machineCode;
	
	private int numUsers=1; // users created in db
	
	private int numOnlineUsers=1; // users online during check time
	
	private int numPOS=0; // pos count in db

	private static SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy/MM/dd");	

	License(long licenseID, String product, String version,
			LicenseType licenseType) {
		this.licenseID = licenseID;
		this.product = product;
		this.version = version;
		this.licenseType = licenseType;
		//this.licenseType = new LicenseType("Commercial");
		name = null;
		company = null;
		machineCode=null;
		product = null;
		url = null;
		expiresDate = null;
		maintenanceExpiresDate = null;
		creationDate = new Date();
		licenseSignature = null;
	}

	

	public long getLicenseID() {
		return licenseID;
	}

	public void setLicenseID(long licenseID) {
		this.licenseID = licenseID;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public LicenseType getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getMachineCode(){
		return machineCode;
	}
	public void setMachineCode(String mc) {
		this.machineCode = mc;
	}
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public Date getExpiresDate() {
		if (expiresDate == null)
			return null;
		else
			return new Date(expiresDate.getTime());
	}

	public void setExpiresDate(Date expiresDate) {
		this.expiresDate = expiresDate;
	}

	public Date getMaintenanceExpiresDate() {
		if (maintenanceExpiresDate == null)
			return null;
		else
			return new Date(maintenanceExpiresDate.getTime());
	}

	public void setMaintenanceExpiresDate(Date maintenanceExpiresDate) {
		this.maintenanceExpiresDate = maintenanceExpiresDate;
	}

	public Date getCreationDate() {
		return new Date(creationDate.getTime());
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getLicenseSignature() {
		return licenseSignature;
	}

	public void setLicenseSignature(String licenseSignature) {
		this.licenseSignature = licenseSignature;
	}

	public byte[] getFingerprint() {
		StringBuffer buf;
		char seperator='#';
		buf = new StringBuffer(100);
		buf.append(licenseID).append(seperator);
		buf.append(product).append(seperator);
		buf.append(version).append(seperator);
		buf.append(licenseType).append(seperator);
		buf.append(numUsers).append(seperator);
		buf.append(numOnlineUsers).append(seperator);
		buf.append(numPOS).append(seperator);
		try {
			if (expiresDate != null)
				buf.append(formatter.format(expiresDate)).append(seperator);;
			if (maintenanceExpiresDate != null)
				buf.append(formatter.format(maintenanceExpiresDate)).append(seperator);
			if (name != null)
				buf.append(name).append(seperator);
			if (company != null)
				buf.append(company).append(seperator);
			if (machineCode != null)
				buf.append(machineCode).append(seperator);
			/*if (product.indexOf("Knowledge Base") < 0
					|| version.compareTo("2.0") >= 0) {
				buf.append(numClusterMembers);
				if (url != null)
					buf.append(url);
			}*/
			
			if (url != null) buf.append(url).append(seperator);
			return buf.toString().getBytes("utf-8");
		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
		}
		return buf.toString().getBytes();
	}

	public boolean equals(Object o) {
		if (!(o instanceof License))
			return false;
		else
			return this == o || licenseID == ((License) o).getLicenseID();
	}

	public static String toXML(License license) throws Exception {
		Element el = new Element("license");
		el.addContent((new Element("licenseID")).addContent(""
				+ license.getLicenseID()));
		el
				.addContent((new Element("product")).addContent(license
						.getProduct()));
		el.addContent((new Element("licenseType")).addContent(license
				.getLicenseType().toString()));
		el
				.addContent((new Element("name"))
						.addContent(license.getName() != null ? license
								.getName() : ""));
		el.addContent((new Element("company"))
				.addContent(license.getCompany() != null ? license.getCompany()
						: ""));
		el.addContent((new Element("machineCode"))
				.addContent(license.getMachineCode() != null ? license.getMachineCode()
						: ""));		
		el.addContent((new Element("version")).addContent(license.getVersion()));
		el.addContent((new Element("numUsers")).addContent(""
				+ license.getNumUsers()));

		el.addContent((new Element("numOnlineUsers")).addContent(""
				+ license.getNumOnlineUsers()));
		el.addContent((new Element("numPOS")).addContent(""
				+ license.getNumPOS()));

		el.addContent((new Element("url"))
				.addContent(license.getURL() != null ? license.getURL() : ""));
		el.addContent((new Element("expiresDate")).addContent(license
				.getExpiresDate() != null ? formatter.format(license
				.getExpiresDate()) : ""));
		if (license.getMaintenanceExpiresDate() != null)
			el
					.addContent((new Element("maintenanceExpiresDate"))
							.addContent(license.getMaintenanceExpiresDate() != null ? formatter
									.format(license.getMaintenanceExpiresDate())
									: ""));
		el.addContent((new Element("creationDate")).addContent(formatter
				.format(license.getCreationDate())));
		el.addContent((new Element("signature")).addContent(license
				.getLicenseSignature() != null ? license.getLicenseSignature()
				: ""));
		Document doc = new Document(el);
		XMLOutputter outputter = new XMLOutputter();
		StringWriter writer = new StringWriter(500);
		outputter.output(doc, writer);
		return writer.toString();
	}

	public static License fromXML(String xml) throws Exception {
		System.out.println(xml);
		SAXBuilder builder = new SAXBuilder();
		StringReader in = new StringReader(xml);
		Document doc = builder.build(in);
		in.close();
		
		Element el = doc.getRootElement();
		int licenseID = Integer.parseInt(el.getChild("licenseID").getText());
		String product = el.getChild("product").getText();
		String version = el.getChild("version").getText();
		LicenseType licenseType = LicenseType.fromString(el.getChild(
				"licenseType").getText());
		License license = new License(licenseID, product, version, licenseType);
		String name = el.getChild("name").getText();
		if (name != null && !name.equals(""))
			license.setName(name);
		String company = el.getChild("company").getText();
		if (company != null && !company.equals(""))
			license.setCompany(company);
		String mc = el.getChild("machineCode").getText();
		if (mc != null && !mc.equals(""))
			license.setMachineCode(mc);

		license.setNumUsers(Integer.parseInt(el.getChild("numUsers").getText()));
		license.setNumOnlineUsers(Integer.parseInt(el.getChild("numOnlineUsers").getText()));
		license.setNumPOS(Integer.parseInt(el.getChild("numPOS").getText()));

		
		if (el.getChild("url") != null) {
			String url = el.getChild("url").getText();
			license.setURL(url);
		}
		String eDate = el.getChild("expiresDate").getText();
		if (eDate != null && !eDate.equals("")) {
			Date expiresDate = formatter.parse(eDate);
			license.setExpiresDate(expiresDate);
		}
		if (el.getChild("maintenanceExpiresDate") != null) {
			String meDate = el.getChild("maintenanceExpiresDate").getText();
			if (meDate != null && !meDate.equals("")) {
				Date maintenanceExpiresDate = formatter.parse(meDate);
				license.setMaintenanceExpiresDate(maintenanceExpiresDate);
			}
		}
		if (el.getChild("creationDate") != null) {
			String cDate = el.getChild("creationDate").getText();
			Date creationDate = formatter.parse(cDate);
			license.setCreationDate(creationDate);
		} else {
			license.setCreationDate(new Date(0xe94935288bL));
		}
		license.setLicenseSignature(el.getChild("signature").getText());
		return license;
	}

	public String toString() {
		return "License{licenseID=" + licenseID + ", product='" + product + "'"
				+ ", version='" + version + "'" + ", licenseType="
				+ licenseType + ", name='" + name + "'" + ", company='"
				+ company + "'" +", machineCode='"
				+ machineCode + "'"+ ", numUsers=" + numUsers +", numOnlineUsers="+ numOnlineUsers
				+ ", numPOS="+ numPOS
				+ ", url='" + url
				+ "'" + ", expiresDate=" + expiresDate
				+ ", maintenanceExpiresDate=" + maintenanceExpiresDate
				+ ", creationDate=" + creationDate + "}";
	}

	public static final class LicenseType {

		public String toString() {
			return name;
		}
		
		public static LicenseType fromString(String type) {
			if (NON_COMMERCIAL.toString().equals(type))
				return NON_COMMERCIAL;
			if (COMMERCIAL.toString().equals(type))
				return COMMERCIAL;
			if (EVALUATION.toString().equals(type))
				return EVALUATION;
			else
				return EVALUATION;
		}

		private final String name;

		public static final LicenseType NON_COMMERCIAL = new LicenseType(
				"Non-Commercial");

		public static final LicenseType COMMERCIAL = new LicenseType(
				"Commercial");

		public static final LicenseType EVALUATION = new LicenseType(
				"Evaluation");

		private LicenseType(String name) {
			this.name = name;
		}
	}

	public int getNumOnlineUsers() {
		return numOnlineUsers;
	}

	public void setNumOnlineUsers(int numOnlineUsers) {
		this.numOnlineUsers = numOnlineUsers;
	}

	public int getNumPOS() {
		return numPOS;
	}

	public void setNumPOS(int numPOS) {
		this.numPOS = numPOS;
	}

	public int getNumUsers() {
		return numUsers;
	}

	public void setNumUsers(int numUsers) {
		this.numUsers = numUsers;
	}


}

