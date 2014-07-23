package com.kin.weixin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

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
	 * 发送登录信息,记录cookie，登录状态，token等信息
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
			// System.out.println("登录：" + post.getResponseBodyAsString());
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
					this.loginErrMsg = "需要验证码";
					return false;
				case -21:
					this.loginErrMsg = "不存在该用户";
					return false;
				case -23:
					this.loginErrMsg = "密码错误";
					return false;
				case -27:
					this.loginErrMsg = "验证码错误";
					return false;
				case 0:
					this.loginErrMsg = "成功登陆，正在跳转...";
					return true;
				default:
					this.loginErrMsg = "未知的返回";
					return false;

				}
			}
		} catch (Exception e) {
			String info = "【登录失败】【发生异常：" + e.getMessage() + "】";
			// System.err.println(info);
			log.debug(info);
			log.info(info);
			return false;
		}
		return false;
	}

	/**
	 * 从登录成功的信息中分离出token信息
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
			String info = "【解析Token失败】【发生异常：" + e.getMessage() + "】";
			// System.err.println(info);
			log.debug(info);
			log.info(info);
			return null;
		}
		return null;
	}

	/**
	 * 获取验证码
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
			// System.out.print("验证码：" + checkCode);
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
	 * 修改公众号接口配置信息
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
		// System.out.println("修改公众号接口配置信息，返回码：" + status);
	}

	/**
	 * 编辑开发模式为true
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
		// System.out.println("编辑开发模式:" + post.getResponseBodyAsString());

	}

//	/**
//	 * 获取用户二维码
//	 * 
//	 * @param token
//	 * @throws Exception
//	 */
//	public void getQrcode(String token, String path) throws Exception {
//
//		String GetUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
//				+ token + "&lang=zh_CN";
//		String HeadUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
//				+ token + "&lang=zh_CN";
//
//		GetMethod get = new GetMethod(GetUrl);
//		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
//		get.setRequestHeader(REFERER_H, HeadUrl);
//		get.setRequestHeader(COOKIE, this.cookiestr);
//		int status = client.executeMethod(get);
//		if (status == HttpStatus.SC_OK) {
//			// System.err.println("正在跳转.....");
//			String si = get.getResponseBodyAsString();
//			Pattern p = Pattern
//					.compile("(?<=(src=\"))/misc/getqrcode\\?[^\"]*(?=\")");
//			Matcher m = p.matcher(si);
//			String rs = "";
//			while (m.find()) {
//				rs += m.group();
//			}
//			rs = "https://mp.weixin.qq.com" + rs;
//			// System.out.println(rs);
//			getQrcodeImage(rs, path);// 下载二维码
//		}
//	}
	
	/**
	 * 获取用户二维码
	 * 
	 * @param token
	 * @throws Exception
	 */
	public void getQrcode(String token, String path) throws Exception {

		String GetUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(USER_AGENT_H, USER_AGENT);
		get.setRequestHeader(REFERER_H, HeadUrl);
		get.setRequestHeader(COOKIE, this.cookiestr);
		int status = client.executeMethod(get);
		String rs = null;
		if (status == HttpStatus.SC_OK) {
			String ret = get.getResponseBodyAsString();
			org.json.JSONObject json = new org.json.JSONObject(ret);
			org.json.JSONObject userInfo = (org.json.JSONObject) json
					.get("user_info");
			String fakeId = userInfo.getString("fake_id");
			rs = "https://mp.weixin.qq.com/misc/getqrcode?fakeid=" + fakeId
					+ "&token=" + token + "&style=1";
			getQrcodeImage(rs, path);// 下载二维码
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
	 * 得到微信公众号的原始ID
	 * 
	 * @param token
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public String getOriginalID(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
		get.setRequestHeader("Referer", HeadUrl);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = this.client.executeMethod(get);
		String rs = null;
		if (status != 200)
			return rs;
		String ret = get.getResponseBodyAsString();
		org.json.JSONObject json = new org.json.JSONObject(ret);
		org.json.JSONObject settingInfo = (org.json.JSONObject) json
				.get("setting_info");
		rs = settingInfo.getString("original_username");
		return rs;
	}

	/**
	 * 得到微信公众号的微信号
	 * 
	 * @param token
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public String getWeixinAccount(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/cgi-bin/settingpage?t=setting/index&action=index&token="
				+ token + "&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
		get.setRequestHeader("Referer", HeadUrl);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = this.client.executeMethod(get);
		String rs = null;
		if (status != 200)
			return rs;
		String ret = get.getResponseBodyAsString();
		org.json.JSONObject json = new org.json.JSONObject(ret);
		org.json.JSONObject settingInfo = (org.json.JSONObject) json
				.get("setting_info");
		rs = settingInfo.getString("username");
		return rs;
	}

	/**
	 * 编辑微信OAuth2.0网页授权
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
	 * 判断用户是否成为开发者
	 * 
	 * @param token
	 * @return true开发者 false尚未成为开发者
	 * @throws Exception
	 */
	public boolean isDevUser(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
				+ token + "&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
				+ token + "&lang=zh_CN";

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
		int is_dev_user = userInfo.getInt("is_dev_user");// 0尚未成为开发者 1已经成为开发者
		if (is_dev_user == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断用户是否成微信认证 和 服务号类型
	 * 
	 * @param token
	 * @return 类型：   1订阅号 3认证订阅号 2服务号 4认证服务号
	 * @throws Exception
	 */

	public int getServiceType(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
				+ token + "&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
				+ token + "&lang=zh_CN";

		GetMethod get = new GetMethod(GetUrl);
		get.setRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
		get.setRequestHeader("Referer", HeadUrl);
		get.setRequestHeader("Cookie", this.cookiestr);
		int status = this.client.executeMethod(get);
		if (status != 200)
			return -1;
		String ret = get.getResponseBodyAsString();
		org.json.JSONObject json = new org.json.JSONObject(ret);
		org.json.JSONObject userInfo = (org.json.JSONObject) json
				.get("user_info");
		int type;
		int service_type = userInfo.getInt("service_type");// 1订阅号 2服务号
		int is_wx_verify = userInfo.getInt("is_wx_verify");// 0没有认证 1微信认证
		if (service_type == 1 && is_wx_verify == 0) {
			type = 1;
		} else if (service_type == 1 && is_wx_verify == 1) {
			type = 3;
		} else if (service_type == 2 && is_wx_verify == 0) {
			type = 2;
		} else {
			type = 4;
		}
		// 类型： 1订阅号 3认证订阅号 2服务号 4认证服务号
		return type;
	}

	/**
	 * 获取开发者AppId
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public String getAppId(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
				+ token + "&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
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
		String ret = get.getResponseBodyAsString();
		org.json.JSONObject json = new org.json.JSONObject(ret);
		org.json.JSONObject advancedInfo = (org.json.JSONObject) json
				.get("advanced_info");
		org.json.JSONObject devInfo = (org.json.JSONObject) advancedInfo
				.get("dev_info");
		String appId = null;
		if (devInfo.has("app_id")) {
			appId = devInfo.getString("app_id");
		}
		return appId;
	}

	/**
	 * 获取开发者appKey
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public String getAppKey(String token) throws Exception {
		String GetUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
				+ token + "&lang=zh_CN&f=json";
		String HeadUrl = "https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token="
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
		String ret = get.getResponseBodyAsString();
		org.json.JSONObject json = new org.json.JSONObject(ret);
		org.json.JSONObject advancedInfo = (org.json.JSONObject) json
				.get("advanced_info");
		org.json.JSONObject devInfo = (org.json.JSONObject) advancedInfo
				.get("dev_info");
		String appKey = null;
		if (devInfo.has("app_key")) {
			appKey = devInfo.getString("app_key");
		}
		return appKey;
	}

	public static void main(String[] args) throws Exception {
		String LOGIN_USER = "2994044970@qq.com";
		String LOGIN_PWD = "zt1314520";
		// String LOGIN_USER = "customer_insight";
		// String LOGIN_PWD = "burgeon123";
		WeixinBind wx = new WeixinBind(LOGIN_USER, LOGIN_PWD, "");
		wx.login("");// 1.登录,拿Token,拿cookie
		// System.out.println("token:"+wx.getToken()+" cookie:"+wx.getCookiestr());
		// 2.验证码
		// BufferedImage image = ImageIO.read(wx.code());
		// File file = new File("D://images//code.jpg");
		// ImageIO.write(image, "jpg", file);
//		 3.编辑开发者模式
		// wx.editCommonInteface(wx.getToken(),
		// "http://2look.xicp.net/servlets/binserv/nds.weixin.ext.RestWeixin?customid=pacozhhejm",
		// "test");
		// 4.是否开发者
		// System.out.println("是否为开发者:"+wx.isDevUser(wx.getToken()));
		// 5.oauth2.0
		// wx.editServiceOAuth(wx.getToken(), "www.baidu.com");
		// 6.用户头像
		 wx.getQrcode(wx.getToken(), "D://images//qrcode.jpg");
		// 7.用户公众号
		System.out.println("公众号:" + wx.getWeixinAccount(wx.getToken()));
		// 8.用户原始id
		System.out.println("原始id:" + wx.getOriginalID(wx.getToken()));
		// 9.获取开发者AppId 与 AppSecret
		System.out.println("appid:" + wx.getAppId(wx.getToken()));
		System.out.println("appKey:" + wx.getAppKey(wx.getToken()));
		// 10.ServiceType
		System.out.println("ServiceType:" + wx.getServiceType(wx.getToken()));
	}
}
