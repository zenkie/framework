package nds.weixin.ext;

import java.util.List;

import org.json.JSONObject;

import nds.control.util.ValueHolder;
import nds.util.Tools;

public class WeUtils {
	private int ad_ClientId;
	private String url=null;
	private String customId=null;
	private String selfToken=null;
	private String appId=null;
	private String appSecret=null;
	private String wxnum=null;
	private String wxtype=null;
	private String domain=null;
	private String foldName=null;
	private String foldmall=null;
	private Boolean refshmem=false;
	
	public WeUtils(List l){
		this.ad_ClientId=Tools.getInt(l.get(0), -1);
		this.domain=(String)l.get(1);
		this.wxnum=(String)l.get(2);
		this.wxtype=(String)l.get(3);
		this.url=(String)l.get(4);
		this.appId=(String)l.get(5);
		this.appSecret=(String)l.get(6);
		this.customId=(String)l.get(7);
		this.selfToken=(String)l.get(8);
		this.foldName=(String)l.get(9);
		this.foldmall=(String)l.get(10);
	}
	
	public int getAd_client_id() {
		return this .ad_ClientId;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getWXName() {
		return this.wxnum;
	}
	
	public String getDoMain() {
		return this.domain;
	}
	
	public String getFoldName() {
		return this.foldName;
	}
	
	public String getFoldMall() {
		return this.foldmall;
	}
	
	public String getWXType() {
		return this.wxtype;
	}
	
	public String getCustomId(){
		return this.customId;
	}
	
	public String getSelfToken(){
		return this.selfToken;
	}
	
	public String getAppId(){
		return this.appId;
	}
	
	public String getAppSecret(){
		return this.appSecret;
	}
	
	
	public void setAppId(String appId){
		 this.appId=appId;
	}
	
	public void setAppSecret(String appSecret){
		 this.appSecret=appSecret;
	}
	
	public void setFoldName(String foldname) {
		this.foldName=foldname;
	}
	
	public void setFoldMall(String foldmall) {
		this.foldmall=foldmall;
	}

	public Boolean getRefshmem() {
		return refshmem;
	}

	public void setRefshmem(Boolean refshmem) {
		this.refshmem = refshmem;
	}
	
}
