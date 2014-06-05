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
	 * 登录,登录失败会重复请求登录
	 */
	public void login() {
		boolean bool = _login();
		while (!bool) {
			String info = "【登录失败】【错误代码：" + this.loginErrMsg + "】【账号："
					+ this.loginUser + "】正在尝试重新登录....";
			log.debug(info);
			System.out.println(info);
			bool = _login();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				bool = _login();
			}

		}
		System.out.println("登陆成功：");
	}

	/**
	 * 发送登录信息,记录cookie，登录状态，token等信息
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
					this.loginErrMsg = "系统错误";
					return false;
				case -2:
					this.loginErrMsg = "帐号或密码错误";
					return false;
				case -3:
					this.loginErrMsg = "密码错误";
					return false;
				case -4:
					this.loginErrMsg = "不存在该帐户";
					return false;
				case -5:
					this.loginErrMsg = "访问受限";
					return false;
				case -6:
					this.loginErrMsg = "需要输入验证码";
					return false;
				case -7:
					this.loginErrMsg = "此帐号已绑定私人微信号，不可用于公众平台登录";
					return false;
				case -8:
					this.loginErrMsg = "邮箱已存在";
					return false;
				case -32:
					this.loginErrMsg = "验证码输入错误";
					return false;
				case -200:
					this.loginErrMsg = "因频繁提交虚假资料，该帐号被拒绝登录";
					return false;
				case -94:
					this.loginErrMsg = "请使用邮箱登陆";
					return false;
				case 10:
					this.loginErrMsg = "该公众会议号已经过期，无法再登录使用";
					return false;
				case 65201:
				case 65202:
					this.loginErrMsg = "成功登陆，正在跳转...";
					return true;
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
			System.err.println(info);
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
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return null;
		}
		return null;
	}

	/**
	 * 获取首页
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
	 * 登出操作
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
			System.err.println("-----------注销登录成功-----------");
		}
	}

	/**
	 * 获取验证码
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
	 * 获取粉丝列表，返回粉丝数量，出错则返回-1
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
			String info = "【获取粉丝数失败】【可能登录过期】";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return -1;
		}
		return -1;
	}

	/**
	 * 从返回文本中提取粉丝数量
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
			String info = "【解析粉丝数失败】 " + "\t\n【文本：】\t\n" + text + "\t\n"
					+ "【发生异常：" + e.getMessage() + "】";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return -1;
		}
		return -1;
	}
	
	
	
	
	/**解析粉丝列表，将粉丝列表存入List<fan>
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
			System.out.println("粉丝列表：");
			
			
			/**
			 * id:就是后面要用到的fakeid
			 * nick_name:昵称
			 * remark_name:备注
			 * group_id:0为未分组,其他的忘了，自己摸索吧
			 */
			for (Fan fan : fans) {
				System.out.println("ID:"+fan.getId()+" nick_name:"+fan.getNick_name()+" remark_name:"+fan.getRemark_name()+" group_id:"+fan.getGroup_id());
			}
			return fans.size();

		} catch (Exception e) {
			String info = "【解析粉丝数失败】 " + "\t\n【文本：】\t\n" + text + "\t\n"
					+ "【发生异常：" + e.getMessage() + "】";
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return -1;
		}
	}


	/**
	 *
	 * <strong>群发消息</strong>
	 * <p>
	 * 返回码说明<br>
	 * 0：发送成功<br>
	 * 64004:今天的群发数量已到，无法群发<br>
	 * -20000:请求被禁止，请仔细检查token是否合法<br>
	 * </p>
	 * <p>
	 * 可通过msgSendCode取得发送状态码
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
							this.msgSendMsg = "发送成功";
							return true;
						case -2:
							this.msgSendMsg = "参数错误，请仔细检查";
							return false;
						case 64004:
							this.msgSendMsg = "今天的群发数量已到，无法群发";
							return false;
						case -20000:
							this.msgSendMsg = "请求被禁止，请仔细检查token是否合法";
							return false;
						default:
							this.msgSendMsg = "未知错误!";
							return false;
						}
					} catch (Exception e) {
						String info = "【群发信息失败】【解析json错误】" + e.getMessage()
								+ "\n\t【文本:】\n\t" + text;
						System.err.println(info);
						log.debug(info);
						log.info(info);
						return false;
					}
				}
			}
		} catch (Exception e) {
			String info = "【群发信息失败】" + e.getMessage();
			System.err.println(info);
			log.debug(info);
			log.info(info);
			return false;
		}
		return false;
	}
	
	
	
	
	/**向粉丝发送消息，默认发送第二个粉丝，想不受限制的群发可以循环向粉丝列表中的粉丝发送消息
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
					System.out.println("请先获取粉丝列表：");
					return false;
				}
				DefaultHttpClient httpClient = new DefaultHttpClient();          //创建默认的httpClient实例 
				X509TrustManager xtm = new X509TrustManager(){                   //创建TrustManager 
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {} 
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {} 
					public X509Certificate[] getAcceptedIssuers() { return null; } 
				};
				SSLContext ctx = SSLContext.getInstance("TLS"); 

				//使用TrustManager来初始化该上下文，TrustManager只是被SSL的Socket所使用 
				ctx.init(null, new TrustManager[]{xtm}, null); 

				//创建SSLSocketFactory 
				SSLSocketFactory socketFactory = new SSLSocketFactory(ctx); 

				//通过SchemeRegistry将SSLSocketFactory注册到我们的HttpClient上 
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
				List<BasicNameValuePair> formParams = new ArrayList<BasicNameValuePair>(); //构建POST请求的表单参数 

				formParams.add(new BasicNameValuePair("content", "Hello,I didn't login!")); //说话内容
				formParams.add(new BasicNameValuePair("imgcode", "")); 
				formParams.add(new BasicNameValuePair("lang", "zh_CN")); 
				formParams.add(new BasicNameValuePair("random", Math.random()+"8")); 
				formParams.add(new BasicNameValuePair("tofakeid",fans.get(i).getId())); 
				formParams.add(new BasicNameValuePair("token", this.token)); 
				formParams.add(new BasicNameValuePair("type", "1")); 
				formParams.add(new BasicNameValuePair("t", "ajax-response")); 
				
				
				post.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8")); 


				HttpResponse response = httpClient.execute(post);    //执行POST请求 
				//HttpEntity entity = response.getEntity();          //获取响应实体 
				HttpEntity entity = response.getEntity();            //获取响应实体 
				long responseLength = 0;                             //响应长度 
				String responseContent = null;                       //响应内容 
				if (null != entity) { 
					responseLength = entity.getContentLength(); 
					responseContent = EntityUtils.toString(entity, "UTF-8"); 
					EntityUtils.consume(entity); //Consume response content 
				} 
				System.out.println("请求地址: " + post.getURI()); 
				System.out.println("响应状态: " + response.getStatusLine()); 
				System.out.println("响应长度: " + responseLength); 
				System.out.println("响应内容: " + responseContent); 

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
	 * 页面跳转
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
					System.err.println("正在跳转.....");
					System.out.println(get.getResponseBodyAsString());
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 修改公众号接口配置信息
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
	 * 说明:<br>
	 * new Weixin()对象，先登录再取粉丝数和者发消息。<br>
	 * 发消息需要设置post参数中的content<br>
	 * 内容中的超链接可以直接发送不用使用<a>标签
	 * 经过我（trprebel）修改之后，此份代码可在2013年11月之后使用
	 * 我只做了获取粉丝列表和发送消息，其他部分未做
	 * 理论上可以获取到粉丝的地址，签名等一切你登陆可以得到的信息
	 * 另外可能需要你在本机先登陆过至少一次微信公众平台获取SSL证书
	 * 此份代码作者较多，函数前面都有作者名，我只修改了登陆，获取粉丝列表和发送消息
	 * 其他代码可能已经不能用了但我并没有删除，方便大家扩展，我做的也比较粗糙，没整理
	 * 原文地址：http://50vip.com/blog.php?i=268
	 * 使用到的库：
	 * commons-codec-1.3.jar
	 * commons-httpclient-3.1.jar
	 * commons-lang.jar
	 * commons-logging-1.0.4.jar
	 * fastjson-1.1.15.jar
	 * gson-2.2.4.jar
	 * httpclient-4.1.3.jar
	 * httpcore-4.1.4.jar
	 * jsoup-1.5.2.jar
	 * 环境：JDK1.6
	 * @param args
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static void main(String[] args) throws HttpException, IOException {

		
		String LOGIN_USER = "customer_insight"; //此为上一任作者的用户名和密码，截止到我最后用发现还能用
		String LOGIN_PWD = "burgeon123";
		Weixin wx = new Weixin(LOGIN_USER, LOGIN_PWD);
		//wx.index();
		wx.login();
		wx.getCookiestr();
		//ImgFileForm form = new ImgFileForm();
		//form.setUploadfile(new File("D:\\Data\\image\\4.jpg"));
		// wx.updateImg(form);
		System.out.println("粉丝数："+wx.getFans());
		//wx.sendMsg(1);//像好友列表中的第几个好友发消息，从0开始
		wx.redirect(OPER_ADVANCED_FUNC);

	}
}