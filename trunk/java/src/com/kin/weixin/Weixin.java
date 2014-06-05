package com.kin.weixin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;


public class Weixin {
	private final static Log log = LogFactory.getLog(Weixin.class);
	public final static String HOST = "http://mp.weixin.qq.com";
	public final static String LOGIN_URL = "http://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN";
	public final static String INDEX_URL = "http://mp.weixin.qq.com/cgi-bin/indexpage?t=wxm-index&lang=zh_CN";
	public final static String SENDMSG_URL ="https://mp.weixin.qq.com/cgi-bin/singlesend";
	public final static String FANS_URL = "http://mp.weixin.qq.com/cgi-bin/contactmanagepage?t=wxm-friend&lang=zh_CN&pagesize=10&pageidx=0&type=0&groupid=0";
	public final static String LOGOUT_URL = "http://mp.weixin.qq.com/cgi-bin/logout?t=wxm-logout&lang=zh_CN";
	public final static String DOWNLOAD_URL = "http://mp.weixin.qq.com/cgi-bin/downloadfile?";
	public final static String VERIFY_CODE = "http://mp.weixin.qq.com/cgi-bin/verifycode?";
	public final static String POST_MSG = "https://mp.weixin.qq.com/cgi-bin/masssend?t=ajax-response";
	public final static String VIEW_HEAD_IMG = "http://mp.weixin.qq.com/cgi-bin/viewheadimg";
	public final static String GET_IMG_DATA = "http://mp.weixin.qq.com/cgi-bin/getimgdata";
	public final static String GET_REGIONS = "http://mp.weixin.qq.com/cgi-bin/getregions";
	public final static String GET_MESSAGE = "http://mp.weixin.qq.com/cgi-bin/getmessage";
	public final static String OPER_ADVANCED_FUNC = "http://mp.weixin.qq.com/cgi-bin/operadvancedfunc";
	public final static String MASSSEND_PAGE = "http://mp.weixin.qq.com/cgi-bin/masssendpage";
	public final static String FILE_MANAGE_PAGE = "http://mp.weixin.qq.com/cgi-bin/filemanagepage";
	public final static String OPERATE_APPMSG = "https://mp.weixin.qq.com/cgi-bin/operate_appmsg?token=416919388&lang=zh_CN&sub=edit&t=wxm-appmsgs-edit-new&type=10&subtype=3&ismul=1";
	public final static String FMS_TRANSPORT = "http://mp.weixin.qq.com/cgi-bin/fmstransport";
	//public final static String CONTACT_MANAGE_PAGE = "http://mp.weixin.qq.com/cgi-bin/contactmanagepage";
	public final static String CONTACT_MANAGE_PAGE = "http://mp.weixin.qq.com/cgi-bin/contactmanage";
	public final static String OPER_SELF_MENU = "http://mp.weixin.qq.com/cgi-bin/operselfmenu";
	public final static String REPLY_RULE_PAGE = "http://mp.weixin.qq.com/cgi-bin/replyrulepage";
	public final static String SINGLE_MSG_PAGE = "http://mp.weixin.qq.com/cgi-bin/singlemsgpage";
	public final static String USER_INFO_PAGE = "http://mp.weixin.qq.com/cgi-bin/userinfopage";
	public final static String DEV_APPLY = "http://mp.weixin.qq.com/cgi-bin/devapply";
	public final static String UPLOAD_MATERIAL = "https://mp.weixin.qq.com/cgi-bin/uploadmaterial?cgi=uploadmaterial&type=2&token=416919388&t=iframe-uploadfile&lang=zh_CN&formId=1";

	public final static String USER_AGENT_H = "User-Agent";
	public final static String REFERER_H = "Referer";
	public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22";
	public final static String UTF_8 = "UTF-8";

	private HttpClient client = new HttpClient();

	private Cookie[] cookies;
	private String cookiestr;

	private String token;
	private int loginErrCode;
	private String loginErrMsg;
	private int msgSendCode;
	private String msgSendMsg;
	private List<Fan> fans;

	private String loginUser;
	private String loginPwd;
	public boolean isLogin = false;

	public Weixin(String user, String pwd) {
		this.loginUser = user;
		this.loginPwd = pwd;
	}

	public Cookie[] getCookies() {
		return cookies;
	}

	public void setCookies(Cookie[] cookies) {
		this.cookies = cookies;
	}

	public String getCookiestr() {
		return cookiestr;
	}

	public void setCookiestr(String cookiestr) {
		this.cookiestr = cookiestr;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getLoginErrCode() {
		return loginErrCode;
	}

	public void setLoginErrCode(int loginErrCode) {
		this.loginErrCode = loginErrCode;
	}

	public String getLoginErrMsg() {
		return loginErrMsg;
	}

	public void setLoginErrMsg(String loginErrMsg) {
		this.loginErrMsg = loginErrMsg;
	}

	public int getMsgSendCode() {
		return msgSendCode;
	}

	public void setMsgSendCode(int msgSendCode) {
		this.msgSendCode = msgSendCode;
	}

	public String getMsgSendMsg() {
		return msgSendMsg;
	}

	public void setMsgSendMsg(String msgSendMsg) {
		this.msgSendMsg = msgSendMsg;
	}

	public String getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(String loginUser) {
		this.loginUser = loginUser;
	}

	public String getLoginPwd() {
		return loginPwd;
	}

	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}

	public boolean isLogin() {
		return isLogin;
	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}


	/**
	 * ��¼,��¼ʧ�ܻ��ظ������¼
	 */
	public void login() {
		boolean bool = _login();
		while (!bool) {
			String info = "����¼ʧ�ܡ���������룺" + this.loginErrMsg + "�����˺ţ�"
					+ this.loginUser + "�����ڳ������µ�¼....";
			log.debug(info);
			System.out.println(info);
			bool = _login();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				bool = _login();
			}

		}
		System.out.println("��½�ɹ���");
	}

	/**
	 * ���͵�¼��Ϣ,��¼cookie����¼״̬��token����Ϣ
	 *
	 * @return
	 */
	private boolean _login() {
		try {
			String vcode=code();
			PostMethod post = new PostMethod(LOGIN_URL);
			post.setRequestHeader("Referer", "https://mp.weixin.qq.com/");
			post.setRequestHeader(USER_AGENT_H, USER_AGENT);
			post.setRequestHeader("Cookie", this.cookiestr);
			NameValuePair[] params = new NameValuePair[]{
					new NameValuePair("username", this.loginUser),
					new NameValuePair("pwd", DigestUtils.md5Hex(this.loginPwd
							.getBytes())),
					new NameValuePair("imgcode",vcode),new NameValuePair("f", "json")};
			post.setQueryString(params);
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				String ret = post.getResponseBodyAsString();
				LoginJson retcode = JSON.parseObject(ret, LoginJson.class);
				com.alibaba.fastjson.JSONObject jo=JSON.parseObject(ret);
				String token=jo.getString("redirect_url");
				//System.out.println(retcode.getRet());

				if (retcode.getRet() == 0 && retcode.getErrCode() == 0) {
					this.cookies = client.getState().getCookies();
					StringBuffer cookie = new StringBuffer();
					for (Cookie c : client.getState().getCookies()) {
						cookie.append(c.getName()).append("=")
						.append(c.getValue()).append(";");
					}
					this.cookiestr = cookie.toString();
					this.isLogin = true;
					this.token = getToken(token);
					return true;
				}
				int errCode = retcode.getErrCode();
				this.loginErrCode = errCode;
				switch (errCode) {

				case -1:
					this.loginErrMsg = "ϵͳ����";
					return false;
				case -2:
					this.loginErrMsg = "�ʺŻ��������";
					return false;
				case -3:
					this.loginErrMsg = "�������";
					return false;
				case -4:
					this.loginErrMsg = "�����ڸ��ʻ�";
					return false;
				case -5:
					this.loginErrMsg = "��������";
					return false;
				case -6:
					this.loginErrMsg = "��Ҫ������֤��";
					return false;
				case -7:
					this.loginErrMsg = "���ʺ��Ѱ�˽��΢�źţ��������ڹ���ƽ̨��¼";
					return false;
				case -8:
					this.loginErrMsg = "�����Ѵ���";
					return false;
				case -32:
					this.loginErrMsg = "��֤���������";
					return false;
				case -200:
					this.loginErrMsg = "��Ƶ���ύ������ϣ����ʺű��ܾ���¼";
					return false;
				case -94:
					this.loginErrMsg = "��ʹ�������½";
					return false;
				case 10:
					this.loginErrMsg = "�ù��ڻ�����Ѿ����ڣ��޷��ٵ�¼ʹ��";
					return false;
				case 65201:
				case 65202:
					this.loginErrMsg = "�ɹ���½��������ת...";
					return true;
				case 0:
					this.loginErrMsg = "�ɹ���½��������ת...";
					return true;
				default:
					this.loginErrMsg = "δ֪�ķ���";
					return false;
				}
			}
		} catch (Exception e) {
			String info = "����¼ʧ�ܡ��������쳣��" + e.getMessage() + "��";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return false;
		}
		return false;
	}

	/**
	 * �ӵ�¼�ɹ�����Ϣ�з����token��Ϣ
	 *
	 * @param s
	 * @return
	 */
	private String getToken(String s) {
		try {
			if (StringUtils.isBlank(s))
				return null;
			String[] ss = StringUtils.split(s, "?");
			String[] params = null;
			if (ss.length == 2) {
				if (!StringUtils.isBlank(ss[1]))
					params = StringUtils.split(ss[1], "&");
			} else if (ss.length == 1) {
				if (!StringUtils.isBlank(ss[0]) && ss[0].indexOf("&") != -1)
					params = StringUtils.split(ss[0], "&");
			} else {
				return null;
			}
			for (String param : params) {
				if (StringUtils.isBlank(param))
					continue;
				String[] p = StringUtils.split(param, "=");
				if (null != p && p.length == 2
						&& StringUtils.equalsIgnoreCase(p[0], "token"))
					return p[1];

			}
		} catch (Exception e) {
			String info = "������Tokenʧ�ܡ��������쳣��" + e.getMessage() + "��";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return null;
		}
		return null;
	}

	/**
	 * ��ȡ��ҳ
	 *
	 * @throws HttpException
	 * @throws IOException
	 */
	public void index() throws HttpException, IOException {
		GetMethod get = new GetMethod(INDEX_URL);
		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = client.executeMethod(get);
		if (status == HttpStatus.SC_OK) {
			System.out.println(get.getResponseBodyAsString());
		}
	}

	/**
	 * �ǳ�����
	 *
	 * @throws HttpException
	 * @throws IOException
	 */
	public void logout() throws HttpException, IOException {
		GetMethod get = new GetMethod(LOGOUT_URL);
		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = client.executeMethod(get);
		if (status == HttpStatus.SC_OK) {
			System.err.println("-----------ע����¼�ɹ�-----------");
		}
	}

	/**
	 * ��ȡ��֤��
	 *
	 * @throws HttpException
	 * @throws IOException
	 */
	public String code() throws HttpException, IOException {
		GetMethod get = new GetMethod(VERIFY_CODE);
		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
		//get.setRequestHeader("Cookie", this.cookiestr);
		NameValuePair[] params = new NameValuePair[]{
				new NameValuePair("username", this.loginUser),
				new NameValuePair("r", "1365318662649")};
		get.setQueryString(params);
		int status = client.executeMethod(get);
		if (status == HttpStatus.SC_OK) {
			BufferedImage image =  ImageIO.read(get.getResponseBodyAsStream());
			String checkCode = JOptionPane.showInputDialog(new ImageIcon(image));
			System.out.print(checkCode);
			this.cookies = client.getState().getCookies();
			StringBuffer cookie = new StringBuffer();
			for (Cookie c : client.getState().getCookies()) {
				cookie.append(c.getName()).append("=")
				.append(c.getValue()).append(";");
			}
			this.cookiestr = cookie.toString();
			return checkCode;//get.getResponseBodyAsStream();
		}
		return null;
	}

	/**
	 * ��ȡ��˿�б����ط�˿�����������򷵻�-1
	 * @author trprebel
	 * @return
	 */
	public int getFans() {
		try {
			String paramStr = "?t=user/index&token=" + this.token
					+ "&lang=zh_CN&pagesize=10&pageidx=0&type=0&groupid=0";
			//String paramStr = "?t=user/index&pagesize=10&pageidx=0&type=4";
			if (!this.isLogin) {
				this._login();
			}
			if (this.isLogin) {
				GetMethod get = new GetMethod(CONTACT_MANAGE_PAGE + paramStr);
				get.setRequestHeader(REFERER_H, INDEX_URL);
				get.setRequestHeader("Cookie", this.cookiestr);
				int status = client.executeMethod(get);
				if (status == HttpStatus.SC_OK) {
					//return parseFansCount(get.getResponseBodyAsString());
					return parseFans(get.getResponseBodyAsString());
				}
				return -1;
			}
		} catch (Exception e) {
			String info = "����ȡ��˿��ʧ�ܡ������ܵ�¼���ڡ�";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return -1;
		}
		return -1;
	}

	/**
	 * �ӷ����ı�����ȡ��˿����
	 *
	 * @param text
	 * @return
	 */
	private int parseFansCount(String text) {
		try {


			StringBuffer json = new StringBuffer();
			final String start = "DATA.groupList =";
			for (int i = text.indexOf(start) + start.length(), len = text
					.length(); i < len; i++) {
				char ci = text.charAt(i);
				if (ci == ';') {
					break;
				}
				json.append(text.charAt(i));
			}
			String txt = json.toString().replaceAll("[*]1", "")
					.replaceAll("defaultGroupName\\[0\\] \\|\\|", "")
					.replaceAll("defaultGroupName\\[1\\] \\|\\|", "")
					.replaceAll("defaultGroupName\\[2\\] \\|\\|", "")
					.replaceAll("defaultGroupName\\[100\\] \\|\\|", "");
			List<FansCount> fans = JSON.parseArray(txt, FansCount.class);
			if (null != fans && !fans.isEmpty())
				for (FansCount fan : fans)
					if (fan.getId() == 0)
						return fan.getNum();
		} catch (Exception e) {
			String info = "��������˿��ʧ�ܡ� " + "\t\n���ı�����\t\n" + text + "\t\n"
					+ "�������쳣��" + e.getMessage() + "��";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return -1;
		}
		return -1;
	}
	
	
	
	
	/**������˿�б�����˿�б����List<fan>
	 * @param text
	 * @return
	 */
	private int parseFans(String text) {                
		try {

			int liststart=text.indexOf("cgiData")+8;
			int listend=text.indexOf("};", liststart)+1;
			text=text.substring(liststart, listend);
			//System.out.println(text);
			int friendliststart=text.indexOf("contacts")+10;
			int friendlistend=text.indexOf("contacts", friendliststart)-3;
			String friendlistjson=text.substring(friendliststart, friendlistend);
			//System.out.println(friendlistjson);
			fans=JSON.parseArray(friendlistjson,Fan.class);
			System.out.println("��˿�б�");
			
			
			/**
			 * id:���Ǻ���Ҫ�õ���fakeid
			 * nick_name:�ǳ�
			 * remark_name:��ע
			 * group_id:0Ϊδ����,���������ˣ��Լ�������
			 */
			for (Fan fan : fans) {
				System.out.println("ID:"+fan.getId()+" nick_name:"+fan.getNick_name()+" remark_name:"+fan.getRemark_name()+" group_id:"+fan.getGroup_id());
			}
			return fans.size();

		} catch (Exception e) {
			String info = "��������˿��ʧ�ܡ� " + "\t\n���ı�����\t\n" + text + "\t\n"
					+ "�������쳣��" + e.getMessage() + "��";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return -1;
		}
	}


	/**
	 *
	 * <strong>Ⱥ����Ϣ</strong>
	 * <p>
	 * ������˵��<br>
	 * 0�����ͳɹ�<br>
	 * 64004:�����Ⱥ�������ѵ����޷�Ⱥ��<br>
	 * -20000:���󱻽�ֹ������ϸ���token�Ƿ�Ϸ�<br>
	 * </p>
	 * <p>
	 * ��ͨ��msgSendCodeȡ�÷���״̬��
	 * </p>
	 *
	 * @by liaokai
	 *
	 */
	/**
	 * @param form
	 * @param type
	 * @return
	 */
	public boolean msgSend(MsgForm form, MsgType type) {
		try {
			if (!this.isLogin) {
				this._login();
			}
			if (this.isLogin) {
				form.setToken(this.token);
				PostMethod post = new PostMethod(POST_MSG);
				post.setRequestHeader(USER_AGENT_H, USER_AGENT);
				post.setRequestHeader(REFERER_H, INDEX_URL);
				post.setRequestHeader("Cookie", this.cookiestr);
				NameValuePair[] params = null;
				Part[] parts = null;
				switch (type) {
				case TEXT:
					// params = new NameValuePair[] {
					// new NameValuePair("type", form.getType()),
					// new NameValuePair("error", form.getError()),
					// new NameValuePair("needcomment",
					// form.getNeedcomment()),
					// new NameValuePair("groupid", form.getGroupid()),
					// new NameValuePair("sex", form.getSex()),
					// new NameValuePair("country", form.getCountry()),
					// new NameValuePair("province", form.getProvince()),
					// new NameValuePair("city", form.getCity()),
					// new NameValuePair("token", form.getToken()),
					// new NameValuePair("ajax", form.getAjax()),
					// new NameValuePair("t", "ajax-response") };
					parts = new Part[]{
							new StringPart("content", form.getContent(),
									"UTF-8"),
									new StringPart("type", form.getType()),
									new StringPart("error", form.getError()),
									new StringPart("needcomment", form.getNeedcomment()),
									new StringPart("groupid", form.getGroupid()),
									new StringPart("sex", form.getSex()),
									new StringPart("country", form.getCountry()),
									new StringPart("province", form.getProvince()),
									new StringPart("city", form.getCity()),
									new StringPart("token", form.getToken()),
									new StringPart("ajax", form.getAjax()),
									new StringPart("t", "ajax-response")};
					break;
				case IMAGE_TEXT:
					parts = new Part[]{
							new StringPart("content", form.getContent(),
									"UTF-8"),
									new StringPart("type", form.getType()),
									new StringPart("error", form.getError()),
									new StringPart("needcomment", form.getNeedcomment()),
									new StringPart("groupid", form.getGroupid()),
									new StringPart("sex", form.getSex()),
									new StringPart("country", form.getCountry()),
									new StringPart("province", form.getProvince()),
									new StringPart("city", form.getCity()),
									new StringPart("token", form.getToken()),
									new StringPart("ajax", form.getAjax()),
									new StringPart("t", "ajax-response")};
					break;
				default:
					parts = new Part[]{
							new StringPart("content", form.getContent(),
									"UTF-8"),
									new StringPart("type", form.getType()),
									new StringPart("error", form.getError()),
									new StringPart("needcomment", form.getNeedcomment()),
									new StringPart("groupid", form.getGroupid()),
									new StringPart("sex", form.getSex()),
									new StringPart("country", form.getCountry()),
									new StringPart("province", form.getProvince()),
									new StringPart("city", form.getCity()),
									new StringPart("token", form.getToken()),
									new StringPart("ajax", form.getAjax()),
									new StringPart("t", "ajax-response")};

					break;
				}
				RequestEntity entity = new MultipartRequestEntity(parts,
						post.getParams());
				post.setRequestEntity(entity);
				int status;
				status = client.executeMethod(post);
				if (status == HttpStatus.SC_OK) {
					String text = post.getResponseBodyAsString();
					try {
						MsgJson ret = JSON.parseObject(text, MsgJson.class);
						this.msgSendCode = ret.getRet();
						switch (this.msgSendCode) {
						case 0:
							this.msgSendMsg = "���ͳɹ�";
							return true;
						case -2:
							this.msgSendMsg = "������������ϸ���";
							return false;
						case 64004:
							this.msgSendMsg = "�����Ⱥ�������ѵ����޷�Ⱥ��";
							return false;
						case -20000:
							this.msgSendMsg = "���󱻽�ֹ������ϸ���token�Ƿ�Ϸ�";
							return false;
						default:
							this.msgSendMsg = "δ֪����!";
							return false;
						}
					} catch (Exception e) {
						String info = "��Ⱥ����Ϣʧ�ܡ�������json����" + e.getMessage()
								+ "\n\t���ı�:��\n\t" + text;
						System.err.println(info);
						log.debug(info);
						log.info(info);
						return false;
					}
				}
			}
		} catch (Exception e) {
			String info = "��Ⱥ����Ϣʧ�ܡ�" + e.getMessage();
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return false;
		}
		return false;
	}
	
	
	
	
	/**���˿������Ϣ��Ĭ�Ϸ��͵ڶ�����˿���벻�����Ƶ�Ⱥ������ѭ�����˿�б��еķ�˿������Ϣ
	 * @author trprebel
	 * @date 2013-11-20
	 */
	public boolean sendMsg(int i)
	{
		try {
			if (!this.isLogin) {
				this._login();
			}
			if (this.isLogin) {
				if (fans==null) {
					System.out.println("���Ȼ�ȡ��˿�б�");
					return false;
				}
				DefaultHttpClient httpClient = new DefaultHttpClient();          //����Ĭ�ϵ�httpClientʵ�� 
				X509TrustManager xtm = new X509TrustManager(){                   //����TrustManager 
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {} 
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {} 
					public X509Certificate[] getAcceptedIssuers() { return null; } 
				};
				SSLContext ctx = SSLContext.getInstance("TLS"); 

				//ʹ��TrustManager����ʼ���������ģ�TrustManagerֻ�Ǳ�SSL��Socket��ʹ�� 
				ctx.init(null, new TrustManager[]{xtm}, null); 

				//����SSLSocketFactory 
				SSLSocketFactory socketFactory = new SSLSocketFactory(ctx); 

				//ͨ��SchemeRegistry��SSLSocketFactoryע�ᵽ���ǵ�HttpClient�� 
				httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory)); 

				HttpPost post = new HttpPost(SENDMSG_URL);
				post.setHeader(USER_AGENT_H, USER_AGENT);
				post.setHeader(REFERER_H,"https://mp.weixin.qq.com/cgi-bin/singlesendpage?t=message/send&action=index&tofakeid="+fans.get(i).getId()+"&token="+this.token+"&lang=zh_CN");
				post.setHeader("Cookie", this.cookiestr);
				post.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
				post.setHeader("Accept-Encoding", "gzip, deflate");
				post.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
				post.setHeader("Cache-Control", "no-cache");
				post.setHeader("Connection", "keep-alive");
				//post.setHeader("Content-Length", "130");
				post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.setHeader("Host", "mp.weixin.qq.com");
				post.setHeader("Pragma", "no-cache");
				post.setHeader("X-Requested-With", "XMLHttpRequest");


				//NameValuePair[] params = null;
				List<BasicNameValuePair> formParams = new ArrayList<BasicNameValuePair>(); //����POST����ı����� 

				formParams.add(new BasicNameValuePair("content", "Hello,I didn't login!")); //˵������
				formParams.add(new BasicNameValuePair("imgcode", "")); 
				formParams.add(new BasicNameValuePair("lang", "zh_CN")); 
				formParams.add(new BasicNameValuePair("random", Math.random()+"8")); 
				formParams.add(new BasicNameValuePair("tofakeid",fans.get(i).getId())); 
				formParams.add(new BasicNameValuePair("token", this.token)); 
				formParams.add(new BasicNameValuePair("type", "1")); 
				formParams.add(new BasicNameValuePair("t", "ajax-response")); 
				
				
				post.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8")); 


				HttpResponse response = httpClient.execute(post);    //ִ��POST���� 
				//HttpEntity entity = response.getEntity();          //��ȡ��Ӧʵ�� 
				HttpEntity entity = response.getEntity();            //��ȡ��Ӧʵ�� 
				long responseLength = 0;                             //��Ӧ���� 
				String responseContent = null;                       //��Ӧ���� 
				if (null != entity) { 
					responseLength = entity.getContentLength(); 
					responseContent = EntityUtils.toString(entity, "UTF-8"); 
					EntityUtils.consume(entity); //Consume response content 
				} 
				System.out.println("�����ַ: " + post.getURI()); 
				System.out.println("��Ӧ״̬: " + response.getStatusLine()); 
				System.out.println("��Ӧ����: " + responseLength); 
				System.out.println("��Ӧ����: " + responseContent); 

			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return false;
	}

	//    public final static Pattern IMG_SUCCESS_REG = Pattern.compile("\.top\.W\.upload\.suc(\"")
	public void updateImg(ImgFileForm form) {
		try {
			if (!this.isLogin)
				this.isLogin();
			if (this.isLogin) {
				form.setToken(this.getToken());
				PostMethod post = new PostMethod(UPLOAD_MATERIAL);
				post.setRequestHeader(USER_AGENT_H, USER_AGENT);
				post.setRequestHeader(REFERER_H, INDEX_URL);
				post.setRequestHeader("Connection", "Keep-Alive");
				post.setRequestHeader("Cookie", this.cookiestr);
				post.setRequestHeader("Cache-Control", "no-cache");

				/**
				 *   private String cgi = "uploadmaterial";
                 private String type = "2";
                 private String token = "";
                 private String t = "iframe-uploadfile";
                 private String lang = "zh_CN";
                 private String formId = "1";
				 */
				FilePart file = new FilePart("uploadfile", form.getUploadfile(), "image/jpeg", "UTF-8");
				System.out.println(form.getToken());
				Part[] parts = new Part[]{
						new StringPart("cgi", form.getCgi()),
						new StringPart("type", form.getType()),
						new StringPart("token", form.getToken()),
						new StringPart("t", form.getT()),
						new StringPart("lang", form.getLang()),
						new StringPart("formId", form.getFormId()),
						file};
				MultipartRequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
				post.setRequestEntity(entity);
				int status = client.executeMethod(post);
				if (status == HttpStatus.SC_OK) {
					String text = post.getResponseBodyAsString();
					System.out.println(text);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ҳ����ת
	 *
	 * @param url
	 */
	public void redirect(String url) {
		if (url.indexOf("https://") == -1)
			url = HOST + url;
		try {
			if (this.isLogin) {
				GetMethod get = new GetMethod(url);
				get.setRequestHeader(USER_AGENT_H, USER_AGENT);
				get.setRequestHeader(REFERER_H, INDEX_URL);
				get.setRequestHeader("Cookie", this.cookiestr);
				int status = client.executeMethod(get);
				if (status == HttpStatus.SC_OK) {
					System.err.println("������ת.....");
					System.out.println(get.getResponseBodyAsString());
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * �޸Ĺ��ںŽӿ�������Ϣ
	 * @param url,robackToken,token
	 * */
	public void editCommonInteface(String token,String application_url,String robackToken)throws Exception{
		
		String PostUrl="https://mp.weixin.qq.com/cgi-bin/callbackprofile?t=ajax-response&token="+token+"&lang=zh_CN";
		
		String Header="https://mp.weixin.qq.com/cgi-bin/advanced?action=interface&t=advanced/interface&token="+token+"&lang=zh_CN";
		
		PostMethod post = new PostMethod(PostUrl);
		post.setRequestHeader("Referer", Header);
		post.setRequestHeader(USER_AGENT_H, USER_AGENT);
		post.setRequestHeader("Cookie", this.cookiestr);
		NameValuePair[] params = new NameValuePair[]{
				new NameValuePair("callback_token", robackToken),
				new NameValuePair("url", application_url)};
		post.setQueryString(params);
		int status = client.executeMethod(post);
		
		
		
	}
	
	/**
	 * ˵��:<br>
	 * new Weixin()�����ȵ�¼��ȡ��˿�����߷���Ϣ��<br>
	 * ����Ϣ��Ҫ����post�����е�content<br>
	 * �����еĳ����ӿ���ֱ�ӷ��Ͳ���ʹ��<a>��ǩ
	 * �����ң�trprebel���޸�֮�󣬴˷ݴ������2013��11��֮��ʹ��
	 * ��ֻ���˻�ȡ��˿�б�ͷ�����Ϣ����������δ��
	 * �����Ͽ��Ի�ȡ����˿�ĵ�ַ��ǩ����һ�����½���Եõ�����Ϣ
	 * ���������Ҫ���ڱ����ȵ�½������һ��΢�Ź���ƽ̨��ȡSSL֤��
	 * �˷ݴ������߽϶࣬����ǰ�涼������������ֻ�޸��˵�½����ȡ��˿�б�ͷ�����Ϣ
	 * ������������Ѿ��������˵��Ҳ�û��ɾ������������չ��������Ҳ�Ƚϴֲڣ�û����
	 * ԭ�ĵ�ַ��http://50vip.com/blog.php?i=268
	 * ʹ�õ��Ŀ⣺
	 * commons-codec-1.3.jar
	 * commons-httpclient-3.1.jar
	 * commons-lang.jar
	 * commons-logging-1.0.4.jar
	 * fastjson-1.1.15.jar
	 * gson-2.2.4.jar
	 * httpclient-4.1.3.jar
	 * httpcore-4.1.4.jar
	 * jsoup-1.5.2.jar
	 * ������JDK1.6
	 * @param args
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static void main(String[] args) throws HttpException, IOException {

		
		String LOGIN_USER = "customer_insight"; //��Ϊ��һ�����ߵ��û��������룬��ֹ��������÷��ֻ�����
		String LOGIN_PWD = "burgeon123";
		Weixin wx = new Weixin(LOGIN_USER, LOGIN_PWD);
		//wx.index();
		wx.login();
		wx.getCookiestr();
		//ImgFileForm form = new ImgFileForm();
		//form.setUploadfile(new File("D:\\Data\\image\\4.jpg"));
		// wx.updateImg(form);
		System.out.println("��˿����"+wx.getFans());
		//wx.sendMsg(1);//������б��еĵڼ������ѷ���Ϣ����0��ʼ
		wx.redirect(OPER_ADVANCED_FUNC);

	}
}