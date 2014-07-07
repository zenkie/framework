package com.kin.weixin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WeixinBind {
	private final static Log log = LogFactory.getLog(WeixinBind.class);
	public final static String HOST = "http://mp.weixin.qq.com";
	public final static String LOGIN_URL = "https://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN";
	public final static String EDIT_DEV_URL = "https://mp.weixin.qq.com/misc/skeyform?form=advancedswitchform&lang=zh_CN";
	public final static String VERIFY_CODE = "http://mp.weixin.qq.com/cgi-bin/verifycode?";
	public final static String INDEX_URL = "http://mp.weixin.qq.com/cgi-bin/indexpage?t=wxm-index&lang=zh_CN";
	public final static String USER_AGENT_H = "User-Agent";
	public final static String REFERER_H = "Referer";
	public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22";
	public final static String UTF_8 = "UTF-8";
	public final static String COOKIE = "Cookie";

	private HttpClient client = new HttpClient();

	private Cookie[] cookies;
	private String cookiestr;
	private String token;
	private int loginErrCode;
	private String loginErrMsg;

	private String loginUser;
	private String loginPwd;
	public boolean isLogin = false;

	public WeixinBind(String user, String pwd, String cookestr) {
		this.loginUser = user;
		this.loginPwd = pwd;
		this.cookiestr = cookestr;
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
	 * ���͵�¼��Ϣ,��¼cookie����¼״̬��token����Ϣ
	 * 
	 * @return
	 */
	public boolean login(String vcode) {
		try {
			// vcode = code();
			PostMethod post = new PostMethod(LOGIN_URL);
			post.setRequestHeader(REFERER_H, "https://mp.weixin.qq.com/");
			post.setRequestHeader(USER_AGENT_H, USER_AGENT);
			post.setRequestHeader(COOKIE, this.cookiestr);
			post.setParameter("username", this.loginUser);
			post.setParameter("pwd",
					DigestUtils.md5Hex(this.loginPwd.getBytes()));
			post.setParameter("imgcode", vcode);
			// post.setParameter("imgcode", "");
			post.setParameter("f", "json");
			int status = client.executeMethod(post);
			// System.out.println("��¼��" + post.getResponseBodyAsString());
			if (status == HttpStatus.SC_OK) {
				String ret = post.getResponseBodyAsString();
				org.json.JSONObject json = new org.json.JSONObject(ret);
				org.json.JSONObject retcode = (org.json.JSONObject) json
						.get("base_resp");
				String token = json.optString("redirect_url");
				int retCode = Integer.parseInt(retcode.getString("ret"));
				if (retCode == 0) {
					this.cookies = client.getState().getCookies();
					StringBuffer cookie = new StringBuffer();
					for (Cookie c : client.getState().getCookies()) {
						cookie.append(c.getName()).append("=")
								.append(c.getValue()).append(";");
					}
					this.cookiestr = cookie.toString();
					this.isLogin = true;
					this.token = getToken(token);
					// return true;
				}
				this.loginErrCode = retCode;
				switch (retCode) {
				case -8:
					this.loginErrMsg = "��Ҫ��֤��";
					return false;
				case -21:
					this.loginErrMsg = "�����ڸ��û�";
					return false;
				case -23:
					this.loginErrMsg = "�������";
					return false;
				case -27:
					this.loginErrMsg = "��֤�����";
					return false;
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
			// System.err.println(info);
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
			// System.err.println(info);
			log.debug(info);
			log.info(info);
			return null;
		}
		return null;
	}

	/**
	 * ��ȡ��֤��
	 * 
	 * @throws HttpException
	 * @throws IOException
	 */
	public InputStream code() throws HttpException, IOException {
		BufferedImage image;
		GetMethod get = new GetMethod(VERIFY_CODE);
		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
		NameValuePair[] params = new NameValuePair[] {
				new NameValuePair("username", this.loginUser),
				new NameValuePair("r", "1365318662649") };
		get.setQueryString(params);
		int status = client.executeMethod(get);
		if (status == HttpStatus.SC_OK) {
			// image = ImageIO.read(get.getResponseBodyAsStream());
			// String checkCode = JOptionPane
			// .showInputDialog(new ImageIcon(image));
			// System.out.print("��֤�룺" + checkCode);
			this.cookies = client.getState().getCookies();
			StringBuffer cookie = new StringBuffer();
			for (Cookie c : client.getState().getCookies()) {
				cookie.append(c.getName()).append("=").append(c.getValue())
						.append(";");
			}
			this.cookiestr = cookie.toString();
			return get.getResponseBodyAsStream();
		}
		return null;
	}

	/**
	 * �޸Ĺ��ںŽӿ�������Ϣ
	 * 
	 * @param url
	 *            ,robackToken,token
	 * */
	public void editCommonInteface(String token, String application_url,
			String robackToken) throws Exception {

		String PostUrl = "https://mp.weixin.qq.com/advanced/callbackprofile?t=ajax-response&token="
				+ token + "&lang=zh_CN";
		String Header = "https://mp.weixin.qq.com/advanced/advanced?action=interface&t=advanced/interface&token="
				+ token + "&lang=zh_CN";

		PostMethod post = new PostMethod(PostUrl);
		post.setRequestHeader(REFERER_H, Header);
		post.setRequestHeader(USER_AGENT_H, USER_AGENT);
		post.setRequestHeader(COOKIE, this.cookiestr);
		post.setParameter("callback_token", robackToken);
		post.setParameter("url", application_url);

		int status = client.executeMethod(post);
		// System.out.println(post.getResponseBodyAsString());
		// System.out.println("�޸Ĺ��ںŽӿ�������Ϣ�������룺" + status);
	}

	/**
	 * �༭����ģʽΪtrue
	 * 
	 * @param token
	 * @throws Exception
	 */
	public void editDevInteface(String token) throws Exception {

		String PostUrl = "https://mp.weixin.qq.com/misc/skeyform?form=advancedswitchform&lang=zh_CN";
		String Header = "https://mp.weixin.qq.com/advanced/advanced?action=edit&t=advanced/edit&token="
				+ token + "&lang=zh_CN";
		PostMethod post = new PostMethod(PostUrl);
		post.setRequestHeader(REFERER_H, Header);
		post.setRequestHeader(USER_AGENT_H, USER_AGENT);
		post.setRequestHeader(COOKIE, this.cookiestr);
		post.setParameter("flag", "1");
		post.setParameter("type", "2");
		post.setParameter("token", token);
		int status = client.executeMethod(post);
		// System.out.println("status:  " + status);
		// System.out.println("�༭����ģʽ:" + post.getResponseBodyAsString());

	}

	/**
	 * ��ȡ�û���ά��
	 * 
	 * @param token
	 * @throws Exception
	 */
	public void getQrcode(String token, String path) throws Exception {

		String GetUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";
		String HeadUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
		get.setRequestHeader(REFERER_H, HeadUrl);
		get.setRequestHeader(COOKIE, this.cookiestr);
		int status = client.executeMethod(get);
		if (status == HttpStatus.SC_OK) {
			// System.err.println("������ת.....");
			String si = get.getResponseBodyAsString();
			Pattern p = Pattern
					.compile("(?<=(src=\"))/misc/getqrcode\\?[^\"]*(?=\")");
			Matcher m = p.matcher(si);
			String rs = "";
			while (m.find()) {
				rs += m.group();
			}
			rs = "https://mp.weixin.qq.com" + rs;
			// System.out.println(rs);
			getQrcodeImage(rs, path);// ���ض�ά��
		}
	}

	public void getQrcodeImage(String url, String path) throws Exception {
		GetMethod get = new GetMethod(url);
		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
		get.setRequestHeader(COOKIE, this.cookiestr);
		int status = client.executeMethod(get);
		BufferedImage image = ImageIO.read(get.getResponseBodyAsStream());
		File file = new File(path);
		ImageIO.write(image, "jpg", file);
		// System.out.println(file.getAbsolutePath());
	}

	/**
	 * �õ�΢�Ź��ںŵ�ԭʼID
	 * 
	 * @param token
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public String getOriginalID(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";
		String HeadUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
		get.setRequestHeader("Referer", HeadUrl);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = this.client.executeMethod(get);
		if (status != 200)
			return null;
		String si = get.getResponseBodyAsString();
		Pattern p = Pattern
				.compile(
						"<li[^>]+>\\s*[\\r\\n]*\\s*<\\s*h4\\s*>\\s*ԭʼID\\s*</h4>\\s*[\\r\\n]*\\s*<div[^>]*>\\s*[\\r\\n]*\\s*</div>\\s*[\\r\\n]*\\s*<div[^>]*>\\s*[\\r\\n]*\\s*<span>([^<]*)</span>\\s*[\\r\\n]*\\s*</div>\\s*[\\r\\n]*\\s*</li>",
						java.util.regex.Pattern.MULTILINE);
		Matcher m = p.matcher(si);
		String rs = "";
		while (m.find()) {
			rs = rs + m.group(1);
		}
		return rs;
	}

	/**
	 * �õ�΢�Ź��ںŵ�΢�ź�
	 * 
	 * @param token
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public String getWeixinAccount(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";
		String HeadUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
		get.setRequestHeader("Referer", HeadUrl);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = this.client.executeMethod(get);
		if (status != 200)
			return null;
		String si = get.getResponseBodyAsString();
		Pattern p = Pattern
				.compile(
						"<li[^>]+>\\s*[\\r\\n]*\\s*<\\s*h4\\s*>\\s*΢�ź�\\s*</h4>\\s*[\\r\\n]*\\s*<div[^>]*>\\s*[\\r\\n]*\\s*</div>\\s*[\\r\\n]*\\s*<div[^>]*>\\s*[\\r\\n]*\\s*<span>([^<]*)</span>\\s*[\\r\\n]*\\s*</div>\\s*[\\r\\n]*\\s*</li>",
						java.util.regex.Pattern.MULTILINE);
		Matcher m = p.matcher(si);
		String rs = "";
		while (m.find()) {
			rs = rs + m.group(1);
		}
		return rs;
	}

	/**
	 * �༭΢��OAuth2.0��ҳ��Ȩ
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public boolean editServiceOAuth(String token, String domain)
			throws Exception {
		String PostUrl = "https://mp.weixin.qq.com/merchant/myservice";
		String HeadUrl = "https://mp.weixin.qq.com/merchant/myservice?action=index&t=service/my_service&token="
				+ token + "&lang=zh_CN";
		PostMethod post = new PostMethod(PostUrl);
		post.setRequestHeader(USER_AGENT_H, USER_AGENT);
		post.setRequestHeader(REFERER_H, HeadUrl);
		post.setRequestHeader(COOKIE, this.cookiestr);
		post.setParameter("domain", domain);
		post.setParameter("token", token);
		post.setParameter("lang", "zh_CN");
		post.setParameter("random", new Random().nextDouble() + "");
		post.setParameter("f", "json");
		post.setParameter("ajax", "1");
		post.setParameter("action", "set_oauth_domain");
		int status = this.client.executeMethod(post);
		System.out.println(post.getResponseBodyAsString());
		if (status != 200)
			return false;
		return true;
	}
	
	/**
	 * �ж��û��Ƿ��Ϊ������
	 * @param token
	 * @return true������  false��δ��Ϊ������
	 * @throws Exception
	 */
	public boolean isDevUser(String token) throws Exception{
		String GetUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="+token+"&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="+token+"&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
		get.setRequestHeader("Referer", HeadUrl);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = this.client.executeMethod(get);
		if (status != 200)
			return false;
		String ret = get.getResponseBodyAsString();
		org.json.JSONObject json = new org.json.JSONObject(ret);
		org.json.JSONObject userInfo = (org.json.JSONObject) json
				.get("user_info");
		int is_dev_user = userInfo.getInt("is_dev_user");//0��δ��Ϊ������  1�Ѿ���Ϊ������
		if(is_dev_user == 1){
			return true;
		}else{
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		String LOGIN_USER = "chanpin@burgeon.cn";
		String LOGIN_PWD = "burgeonchanpin";
		WeixinBind wx = new WeixinBind(LOGIN_USER, LOGIN_PWD, "");
		wx.login("");
		System.out.println("���ں�ԭʼID��" + wx.getOriginalID(wx.getToken()));
		System.out.println("����΢�źţ�" + wx.getWeixinAccount(wx.getToken()));
		wx.editServiceOAuth(wx.getToken(), "test123123123123.next99.cn");
		// String LOGIN_USER = "chanpin@burgeon.cn";
		// String LOGIN_PWD = "burgeonchanpin";
		// WeixinBind wx = new WeixinBind(LOGIN_USER, LOGIN_PWD,"");
		// wx.login("xevp");
		// System.out.print(wx.getLoginErrMsg());
		// wx.editDevInteface(wx.getToken());//�༭����ģʽ
		// wx.editCommonInteface(wx.getToken(),//�޸�Token url
		// "http://wx.b.qq.com/weixin/addev/MOCYUBI",
		// "25b3e0b5ff5da2446986140f6be3b00a");
		// System.out.println("�û�token" + wx.getToken());
		// wx.getQrcode(wx.getToken(),"");
	}
}