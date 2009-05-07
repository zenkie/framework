/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.export;

import java.util.*;

/**
 * IDoc is for local document processing
 *  
 * @author yfzhu@agilecontrol.com
 */

public class IDoc {
	/**
	 * This will be used by server to ouput stream to ServletResponse
	 */
	public final static String MIME_TYPE="application/nea";
	
	public final static String FILE_EXTENSION="nea";
	
	
	
	private String docNo;
	private String wsdl;  // constructed by nds.control.web.binhandler.IDoc
	private String sessionId; // client session id
	private String user; // in format like root@shenli
	private String ipAddress ;// client ip
	
	
	public String getDocNo() {
		return docNo;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public String getSessionId() {
		return sessionId;
	}
	public String getUser() {
		return user;
	}
	public String getWsdl() {
		return wsdl;
	}
	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}
	public void setIpAddress(String IpAddress) {
		this.ipAddress = IpAddress;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public void setWsdl(String wsdl) {
		this.wsdl = wsdl;
	}
	public String getString(){
		/*[ClientInfo]
		 session=xke93ks03d-3923x9ksiew3       #来自于网页的当前连接
		 user=zhang@esprit						#登陆网站的客户端用户名
		 ip=192.168.1.102						#登陆网站的客户端IP
		 [WebService]
		 wsdl=http://222.71.209.250/services/DocUMO?wsdl
		 docNO=iweidk233923msd
		*/
		String lineSep= "\r\n";
		StringBuffer sb=new StringBuffer();
		sb.append("[ClientInfo]").append(lineSep);
		sb.append("session=").append(sessionId).append(lineSep);
		sb.append("user=").append(user).append(lineSep);
		sb.append("ip=").append(ipAddress).append(lineSep);
		sb.append("[WebService]").append(lineSep);
		sb.append("wsdl=").append(wsdl).append(lineSep);
		sb.append("docNO=").append(docNo).append(lineSep);
		return sb.toString();
		
	}
	/**
	 * Get bytes of the IDoc, which can be used by servlet to output to ServletOutputStream directly
	 */
	public byte[] getBytes()  {
		return getString().getBytes();
	}
	/**
	 * Load IDoc from db
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static IDoc load(int id) throws Exception{
		return null;
	}
}
