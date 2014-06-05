package com.kin.weixin;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
//import org.junit.Test;

public class wxbindkey {
	public HttpClient client = new DefaultHttpClient();
	
	//@Test
	public void test() throws Exception{
		String username="***********";
		String password="*********";
		String oppenid1="*********";
		String oppenidw="*********";
		String url="http://www.****.com/WeiXingTest/coreServlet";
		String robackToken="woaimeiz";
		String token="";
		String checkCode=getCheckCode(username);
		password=MD5Encode(password);
		String content=getLogins(username,password,checkCode);
		token=getToken(content);
		//editAssistantOpenID(oppenid1,token);
		editCommonInteface(token,url,robackToken);
	}
	/**
	 * �޸Ĺ��ںŽӿ�������Ϣ
	 * @param url,robackToken,token
	 * */
	public void editCommonInteface(String token,String application_url,String robackToken)throws Exception{
		
		String PostUrl="https://mp.weixin.qq.com/cgi-bin/callbackprofile?t=ajax-response&token="+token+"&lang=zh_CN";
		
		String Header="https://mp.weixin.qq.com/cgi-bin/advanced?action=interface&t=advanced/interface&token="+token+"&lang=zh_CN";
		
		HttpPost cfgPost = new HttpPost(PostUrl);
		
		cfgPost.addHeader("Referer", Header);

		List<NameValuePair> nvp_2 = new ArrayList<NameValuePair>();
		
		nvp_2.add(new BasicNameValuePair("callback_token", robackToken));
		nvp_2.add(new BasicNameValuePair("url", application_url));
		cfgPost.setEntity(new UrlEncodedFormEntity(nvp_2));

		System.out.println(EntityUtils.toString(client.execute(cfgPost).getEntity()));
		//cfgPost.releaseConnection();
	}
	
	
	/**
	 * �޸Ĺ��ں��ֻ����ֵİ�΢�ź�
	 * @param openID,token
	 * */
	private void editAssistantOpenID(String openID,String token)throws Exception{
		
		String url="https://mp.weixin.qq.com/cgi-bin/binduser?cgi=binduser&t=ajax-response&binduser="+openID;
		
		HttpPost post3 = new HttpPost(url);
		
		post3.addHeader("Referer", "https://mp.weixin.qq.com/");
		
		List<NameValuePair> nvp3 = new ArrayList<NameValuePair>();
		nvp3.add(new BasicNameValuePair("ajax", "1"));
		nvp3.add(new BasicNameValuePair("token", token));
		post3.setEntity(new UrlEncodedFormEntity(nvp3));
		HttpResponse resp3 = client.execute(post3);
		//System.out.println(EntityUtils.toString(resp3.getEntity()));
		//post3.releaseConnection();
	}
	
	
	
	/**
	 * ��ȡToken
	 * @param reposne���ص�����
	 * @return Token
	 * */
	private String getToken(String response){
		String [] bs=response.split("&token=")[1].split("','");	
		String token=bs[0].split(",")[0];
		token=token.substring(0, token.length()-1);
		return token;
	}
	
	
	/**MD5����
	 * @param ����ǰ����
	 * @return ���ܺ�����
	 * */
	private String MD5Encode(String password) {
		MessageDigest md5 = null;
		byte[] bs = password.getBytes();
		StringBuffer buffer = new StringBuffer();
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(bs);
			bs = md5.digest();
			for (int i = 0; i < bs.length; i++) {

				if ((bs[i] & 0xff) < 0x10) {
					buffer.append("0");
				}
				buffer.append(Long.toString(bs[i] & 0xff, 16));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}
	/**
	 * ��ȡ��֤��
	 * @param �û�����
	 * @return ��֤��
	 * */ 
	private  String getCheckCode(String username)throws Exception{
		
		String url="https://mp.weixin.qq.com/cgi-bin/verifycode?username="+username+"&r="+System.currentTimeMillis();
		HttpGet get_1 = new HttpGet(url);
		HttpResponse resp_1 = client.execute(get_1);
		BufferedImage image =  ImageIO.read(resp_1.getEntity().getContent());
		String checkCode = JOptionPane.showInputDialog(new ImageIcon(image));
		return checkCode;
	}
	// �����¼
	private void getLogin()throws Exception{
		// ��ȡ��֤��
		HttpGet get_1 = new HttpGet("https://mp.weixin.qq.com/cgi-bin/verifycode?username=ejcall@163.com&r="+System.currentTimeMillis());
		HttpResponse resp_1 = client.execute(get_1);
		BufferedImage image =  ImageIO.read(resp_1.getEntity().getContent());
		String checkCode = JOptionPane.showInputDialog(new ImageIcon(image));
		System.out.println(checkCode);
		//get_1.releaseConnection();

		// �����¼
		HttpPost post = new HttpPost("https://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN");
		post.addHeader("Referer", "https://mp.weixin.qq.com/");

		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("f", "json"));
		nvp.add(new BasicNameValuePair("imgcode", checkCode));
		nvp.add(new BasicNameValuePair("pwd", "1d856dafc0930157041e8c7c4bb11ed3"));
		nvp.add(new BasicNameValuePair("username", "ejcall@163.com"));
		post.setEntity(new UrlEncodedFormEntity(nvp));
		System.out.println(EntityUtils.toString(client.execute(post).getEntity()));
		//post.releaseConnection();


		// �޸�����
		HttpPost cfgPost = new HttpPost("https://mp.weixin.qq.com/cgi-bin/callbackprofile?t=ajax-response&token=288164033&lang=zh_CN");
		cfgPost.addHeader("Referer", "https://mp.weixin.qq.com/cgi-bin/advanced?action=interface&t=advanced/interface&token=288164033=&lang=zh_CN");

		List<NameValuePair> nvp_2 = new ArrayList<NameValuePair>();
		nvp_2.add(new BasicNameValuePair("callback_token", "08712013191636045726"));
		nvp_2.add(new BasicNameValuePair("url", "http://0992.mpb.weduty.com/mp/B1334055059"));
		cfgPost.setEntity(new UrlEncodedFormEntity(nvp_2));

		System.out.println(EntityUtils.toString(client.execute(cfgPost).getEntity()));
		//cfgPost.releaseConnection();
	}
	/**
	 * �������
	 * @param �û���,���ܺ�����,��֤��
	 * @return  reposne���ص�����,����Token��String�ֶ�
	 * */
	private String getLogins(String user,String pas,String checkCode)throws Exception{
		String resultCont="";
		// �����¼
		HttpPost post = new HttpPost("https://mp.weixin.qq.com/cgi-bin/login?lang=zh_CN");
		post.addHeader("Referer", "https://mp.weixin.qq.com/");

		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("f", "json"));
		nvp.add(new BasicNameValuePair("imgcode", checkCode));
		nvp.add(new BasicNameValuePair("pwd", pas));
		nvp.add(new BasicNameValuePair("username", user));
		post.setEntity(new UrlEncodedFormEntity(nvp));
		resultCont=EntityUtils.toString(client.execute(post).getEntity());
		//post.releaseConnection();
		return resultCont;
	}

}
